package com.ishland.c2me.rewrites.chunkio.common;

import com.ibm.asyncutil.util.Either;
import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.base.common.structs.RawByteArrayOutputStream;
import com.ishland.c2me.base.common.util.SneakyThrow;
import com.ishland.c2me.base.mixin.access.IRegionBasedStorage;
import com.ishland.c2me.base.mixin.access.IRegionFile;
import com.ishland.c2me.opts.chunkio.common.ConfigConstants;
import it.unimi.dsi.fastutil.longs.Long2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

public class C2MEStorageThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger("C2ME Storage");

    private static final AtomicLong SERIAL = new AtomicLong(0);

    private final AtomicBoolean closing = new AtomicBoolean(false);
    private final CompletableFuture<Void> closeFuture = new CompletableFuture<>();

    private final RegionFileStorage storage;
    private final Long2ReferenceLinkedOpenHashMap<Either<CompoundTag, byte[]>> writeBacklog = new Long2ReferenceLinkedOpenHashMap<>();
    private final Long2ReferenceLinkedOpenHashMap<Either<CompoundTag, byte[]>> cache = new Long2ReferenceLinkedOpenHashMap<>();
    private final ConcurrentLinkedQueue<ReadRequest> pendingReadRequests = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<WriteRequest> pendingWriteRequests = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Runnable> pendingTasks = new ConcurrentLinkedQueue<>();
    private final Executor executor = command -> {
        if (Thread.currentThread() == this) {
            command.run();
        } else {
            final boolean empty = pendingTasks.isEmpty();
            pendingTasks.add(command);
            if (empty) this.wakeUp();
        }
    };
    private final ObjectArraySet<CompletableFuture<Void>> writeFutures = new ObjectArraySet<>();
    private final Object sync = new Object();

    public C2MEStorageThread(Path directory, boolean dsync, String name) {
        this.storage = new RegionFileStorage(directory, dsync);
        this.setName("C2ME Storage #%d".formatted(SERIAL.incrementAndGet()));
        this.setDaemon(true);
        this.setUncaughtExceptionHandler((t, e) -> LOGGER.error("Thread %s died".formatted(t), e));
        this.start();
    }

    @Override
    public void run() {
        main_loop:
        while (true) {
            boolean hasWork = false;
            hasWork |= pollTasks();

            runWriteFutureGC();

            if (!hasWork) {
                if (this.closing.get()) {
                    flush0(true);
                    try {
                        this.storage.close();
                    } catch (Throwable t) {
                        LOGGER.error("Error closing storage", t);
                    }
                    this.closeFuture.complete(null);
                    break;
                } else {
                    // attempt to spin-wait before sleeping
                    if (!pollTasks()) {
                        Thread.interrupted(); // clear interrupt flag
                        for (int i = 0; i < 5000; i ++) {
                            if (pollTasks()) continue main_loop;
                            LockSupport.parkNanos("Spin-waiting for tasks", 10_000); // 100us
                        }
                    }
                    synchronized (sync) {
                        if (this.hasPendingTasks() || this.closing.get()) continue main_loop;
                        try {
                            sync.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        }
        LOGGER.info("Storage thread {} stopped", this);
    }

    private boolean pollTasks() {
        boolean hasWork = false;
        hasWork = handleTasks() || hasWork;
        hasWork = handlePendingWrites() || hasWork;
        hasWork = handlePendingReads() || hasWork;
        hasWork = writeBacklog() || hasWork;
        return hasWork;
    }

    private boolean hasPendingTasks() {
        return !this.pendingTasks.isEmpty() || !this.pendingReadRequests.isEmpty() || !this.pendingWriteRequests.isEmpty() || !this.writeBacklog.isEmpty();
    }

    private void wakeUp() {
        synchronized (sync) {
            sync.notifyAll();
        }
    }

    /**
     * Read chunk data from storage
     * @param pos target pos
     * @param scanner if null then ignored, if non-null then used and produce null future
     * @return future
     */
    public CompletableFuture<CompoundTag> getChunkData(long pos, StreamTagVisitor scanner) {
        final CompletableFuture<CompoundTag> future = new CompletableFuture<>();
        if (this.closing.get()) {
            future.completeExceptionally(new CancellationException());
            return future.thenApply(Function.identity());
        }
        final boolean empty = this.pendingReadRequests.isEmpty();
        this.pendingReadRequests.add(new ReadRequest(pos, future, scanner));
        if (empty) this.wakeUp();
        future.thenApply(Function.identity()).orTimeout(60, TimeUnit.SECONDS).exceptionally(throwable -> {
            if (throwable instanceof TimeoutException) {
                LOGGER.warn("Chunk read at pos {} took too long (> 1min)", new ChunkPos(pos).toLong());
            }
            return null;
        });
        return future
                .thenApply(Function.identity());
    }

    public void setChunkData(long pos, @Nullable CompoundTag nbt) {
        final boolean empty = this.pendingWriteRequests.isEmpty();
        this.pendingWriteRequests.add(new WriteRequest(pos, nbt != null ? Either.left(nbt) : null));
        if (empty) this.wakeUp();
    }

    public void setChunkData(long pos, @Nullable byte[] data) {
        final boolean empty = this.pendingWriteRequests.isEmpty();
        this.pendingWriteRequests.add(new WriteRequest(pos, data != null ? Either.right(data) : null));
        if (empty) this.wakeUp();
    }

    public CompletableFuture<Void> flush(boolean sync) {
        return CompletableFuture.runAsync(() -> flush0(sync), this.executor);
    }

    private void flush0(boolean sync) {
        try {
            while (true) {
                runWriteFutureGC();
                if (handleTasks()) continue;
                if (handlePendingReads()) continue;
                if (handlePendingWrites()) continue;
                if (writeBacklog()) continue;

                break;
            }
            flushBacklog();
            if (sync) this.storage.flush();
        } catch (Throwable t) {
            LOGGER.error("Error flushing storage", t);
        }
    }

    public CompletableFuture<Void> close() {
        this.closing.set(true);
        this.wakeUp();
        return this.closeFuture.thenApply(Function.identity());
    }

    private boolean handleTasks() {
        boolean hasWork = false;
        Runnable runnable;
        while ((runnable = this.pendingTasks.poll()) != null) {
            hasWork = true;
            try {
                runnable.run();
            } catch (Throwable t) {
                LOGGER.error("Error while executing task", t);
            }
        }
        return hasWork;
    }

    private boolean handlePendingWrites() {
        boolean hasWork = false;
        WriteRequest writeRequest;
        while ((writeRequest = this.pendingWriteRequests.poll()) != null) {
            hasWork = true;
            this.cache.put(writeRequest.pos, writeRequest.nbt);
            this.writeBacklog.put(writeRequest.pos, writeRequest.nbt);
        }
        return hasWork;
    }

    private boolean handlePendingReads() {
        boolean hasWork = false;
        while (!pendingReadRequests.isEmpty()) {
            ReadRequest readRequest = this.pendingReadRequests.poll();
            hasWork = true;
            assert readRequest != null;
            final long pos = readRequest.pos;
            final CompletableFuture<CompoundTag> future = readRequest.future;
            final StreamTagVisitor scanner = readRequest.scanner;
            if (this.cache.containsKey(pos)) {
                final Either<CompoundTag, byte[]> cached = this.cache.get(pos);
                if (cached == null) {
                    future.complete(null);
                } else if (cached.left().isPresent()) {
                    if (scanner != null) {
                        GlobalExecutors.executor.execute(() -> {
                            try {
                                cached.left().get().acceptAsRoot(scanner);
                                future.complete(null);
                            } catch (Throwable t) {
                                future.completeExceptionally(t);
                            }
                        });
                    } else {
                        future.complete(cached.left().get());
                    }
                } else {
                    CompletableFuture.supplyAsync(() -> {
                                try {
                                    final DataInputStream input = new DataInputStream(new ByteArrayInputStream(cached.right().get()));
                                    if (scanner != null) {
                                        NbtIo.parse(input, scanner);
                                        return null;
                                    } else {
                                        final CompoundTag compound = NbtIo.read(input);
                                        return compound;
                                    }
                                } catch (IOException e) {
                                    SneakyThrow.sneaky(e);
                                    return null; // unreachable
                                }
                            }, GlobalExecutors.executor)
                            .thenAccept(future::complete)
                            .exceptionally(throwable -> {
                                future.completeExceptionally(throwable);
                                return null;
                            });
                }
                continue;
            }
            scheduleChunkRead(pos, future, scanner);
        }
        return hasWork;
    }

    private boolean writeBacklog() {
        if (!this.writeBacklog.isEmpty()) {
            final long pos = this.writeBacklog.firstLongKey();
            final Either<CompoundTag, byte[]> nbt = this.writeBacklog.removeFirst();
            writeChunk(pos, nbt);
            return true;
        }
        return false;
    }

    private void runWriteFutureGC() {
        this.writeFutures.removeIf(CompletableFuture::isDone);
    }

    private void flushBacklog() {
        while (!this.writeFutures.isEmpty()) {
            while (writeBacklog()) ;
            runWriteFutureGC();
            final CompletableFuture<Void> allFuture = CompletableFuture.allOf(this.writeFutures.stream()
                    .map(future -> future.exceptionally(unused -> null))
                    .distinct()
                    .toArray(CompletableFuture[]::new));
            while (!allFuture.isDone()) {
                handleTasks();
            }
            runWriteFutureGC();
        }
    }

    private void scheduleChunkRead(long pos, CompletableFuture<CompoundTag> future, StreamTagVisitor scanner) {
        try {
            final ChunkPos pos1 = new ChunkPos(pos);
            final RegionFile regionFile = ((IRegionBasedStorage) (Object) this.storage).invokeGetRegionFile(pos1);
            final DataInputStream chunkInputStream = regionFile.getChunkDataInputStream(pos1);
            if (chunkInputStream == null) {
                future.complete(null);
                return;
            }
            CompletableFuture.supplyAsync(() -> {
                try {
                    try (DataInputStream inputStream = chunkInputStream) {
                        if (scanner != null) {
                            NbtIo.parse(inputStream, scanner);
                            return null;
                        } else {
                            return NbtIo.read(inputStream);
                        }
                    }
                } catch (Throwable t) {
                    SneakyThrow.sneaky(t);
                    return null; // Unreachable anyway
                }
            }, GlobalExecutors.executor).handle((compound, throwable) -> {
                if (throwable != null) future.completeExceptionally(throwable);
                else future.complete(compound);
                return null;
            });
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
    }

    private void writeChunk(long pos, Either<CompoundTag, byte[]> nbt) {
        if (nbt == null) {
            if (this.cache.get(pos) == null) {
                try {
                    final ChunkPos pos1 = new ChunkPos(pos);
                    final RegionFile regionFile = ((IRegionBasedStorage) (Object) this.storage).invokeGetRegionFile(pos1);
                    regionFile.clear(pos1);
                } catch (Throwable t) {
                    LOGGER.error("Error writing chunk %s".formatted(new ChunkPos(pos)), t);
                }
                this.cache.remove(pos);
            }
        } else {
            final CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                try {
                    final RawByteArrayOutputStream out = new RawByteArrayOutputStream(8096);
                    // TODO [VanillaCopy] RegionFile.ChunkBuffer
                    out.write(0);
                    out.write(0);
                    out.write(0);
                    out.write(0);
                    out.write(ConfigConstants.CHUNK_STREAM_VERSION.getId());
                    try (DataOutputStream dataOutputStream = new DataOutputStream(ConfigConstants.CHUNK_STREAM_VERSION.wrap(out))) {
                        if (nbt.left().isPresent()) {
                            NbtIo.write(nbt.left().get(), dataOutputStream);
                        } else {
                            dataOutputStream.write(nbt.right().get());
                        }
                    }
                    return out;
                } catch (Throwable t) {
                    SneakyThrow.sneaky(t);
                    return null; // Unreachable anyway
                }
            }, GlobalExecutors.executor).thenAcceptAsync(bytes -> {
                if (nbt == this.cache.get(pos)) { // only write if match to avoid overwrites
                    try {
                        final ChunkPos pos1 = new ChunkPos(pos);
                        final RegionFile regionFile = ((IRegionBasedStorage) (Object) this.storage).invokeGetRegionFile(pos1);
                        ByteBuffer byteBuffer = bytes.asByteBuffer();
                        // TODO [VanillaCopy] RegionFile.ChunkBuffer
                        byteBuffer.putInt(0, bytes.size() - 5 + 1);
                        ((IRegionFile) regionFile).invokeWriteChunk(pos1, byteBuffer);
                    } catch (Throwable t) {
                        SneakyThrow.sneaky(t);
                    }
                    this.cache.remove(pos);
                }
            }, this.executor).handleAsync((unused, throwable) -> {
                if (throwable != null) LOGGER.error("Error writing chunk %s".formatted(new ChunkPos(pos)), throwable);
                // TODO error retry

                return null;
            }, this.executor);
            this.writeFutures.add(future);
        }
    }

    private record ReadRequest(long pos, CompletableFuture<CompoundTag> future, @Nullable StreamTagVisitor scanner) {
    }

    private record WriteRequest(long pos, Either<CompoundTag, byte[]> nbt) {
    }

}
