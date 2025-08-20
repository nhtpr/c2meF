package com.ishland.c2me.rewrites.chunk_serializer.common;

import com.ishland.c2me.rewrites.chunk_serializer.common.utils.StringBytesConvertible;
import com.ishland.c2me.rewrites.chunk_serializer.common.utils.UnsafeUtils;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterable;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.io.UTFDataFormatException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("WeakerAccess")
public class NbtWriter {
    private static final int INCREMENT = 2;
    private static final Unsafe UNSAFE = UnsafeUtils.UNSAFE;

    private static final int BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
    private static final int INT_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(int[].class);
    private static final int LONG_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(long[].class);

    private long size = 1024 * 64;
    private long buffer = UNSAFE.allocateMemory(this.size);
    private long remaining = this.size;
    private long pointer = this.buffer;

    private NbtWriterVisitor visitor;

    private void claimCapacity(long extra) {
        this.remaining -= extra;
        while (this.remaining < 0) {
            this.remaining += this.size * (INCREMENT - 1);
            this.size *= INCREMENT;
            long offset = this.getOffset();
            this.buffer = UNSAFE.reallocateMemory(this.buffer, this.size);
            this.pointer = this.buffer + offset;
        }
    }

    public long getOffset() {
        return this.pointer - this.buffer;
    }

    public void start(byte type) {
        this.putByteEntry(type);
        //assert getOffset() == 1;
        if (type != Tag.TAG_END) {
            this.putShortEntry((short) 0);
            //assert getOffset() == 5;
        }
    }

    //put helpers

    private void insertByte(byte value) {
        UNSAFE.putByte(this.pointer, value);
        this.pointer++;
    }

    private void insertShort(short value) {
        UNSAFE.putShort(this.pointer, Short.reverseBytes(value));
        this.pointer += 2;
    }

    private void insertInt(int value) {
        UNSAFE.putInt(this.pointer, Integer.reverseBytes(value));
        this.pointer += 4;
    }

    private void insertLong(long value) {
        UNSAFE.putLong(this.pointer, Long.reverseBytes(value));
        this.pointer += 8;
    }

    private void insertFloat(float value) {
        this.insertInt(Float.floatToRawIntBits(value));
    }

    private void insertDouble(double value) {
        this.insertLong(Double.doubleToRawLongBits(value));
    }

    private void insertByteArray(byte[] value) {
        UNSAFE.copyMemory(value, BYTE_ARRAY_OFFSET, null, this.pointer, value.length);
        this.pointer += value.length;
    }

    private void insertIntArray(int[] value) {
        for (int i : value) {
            UNSAFE.putInt(this.pointer, Integer.reverseBytes(i));
            this.pointer += 4;
        }
    }

    private void insertLongArray(long[] value) {
        for (long i : value) {
            UNSAFE.putLong(this.pointer, Long.reverseBytes(i));
            this.pointer += 8;
        }
    }

    private void insertLongArray(LongIterable value) {
        value.forEach(i -> {
            UNSAFE.putLong(this.pointer, Long.reverseBytes(i));
            this.pointer += 8;
        });
    }

    //region list entries

    public void putBooleanEntry(boolean value) {
        this.putByteEntry(value ? (byte) 1 : 0);
    }


    public void putByteEntry(byte value) {
        this.claimCapacity(1);
        this.insertByte(value);
    }


    public void putShortEntry(short value) {
        this.claimCapacity(2);
        this.insertShort(value);
    }


    public void putIntEntry(int value) {
        this.claimCapacity(4);
        this.insertInt(value);
    }


    public void putLongEntry(long value) {
        this.claimCapacity(8);
        this.insertLong(value);
    }


    public void putFloatEntry(float value) {
        this.claimCapacity(4);
        this.insertFloat(value);
    }


    public void putDoubleEntry(double value) {
        this.claimCapacity(8);
        this.insertDouble(value);
    }

    // byte array

    public void putByteArrayEntry(byte[] value) {
        this.claimCapacity(4L + value.length);
        this.insertInt(value.length);
        // use unsafe to copy the bytes to the pointer location
        this.insertByteArray(value);
    }

    // int array

    public void putIntArrayEntry(int[] value) {
        this.claimCapacity(4L + value.length * 4L);
        this.putIntEntry(value.length);
        this.insertIntArray(value);
    }

    // long array

    public void putLongArrayEntry(long[] value) {
        this.claimCapacity(4L + value.length * 8L);
        this.putIntEntry(value.length);
        this.insertLongArray(value);
    }

    //string

    public void putStringEntry(byte[] s) {
        this.claimCapacity(s.length);
        this.insertByteArray(s);
    }


    public void putStringEntry(String s) {
        this.putStringEntry(getStringBytes(s));
    }

    public <T> void putRegistryEntry(Registry<T> registry, T value) {
        this.putStringEntry(getNameBytesFromRegistry(registry, value));
    }


    public <T> void putRegistryEntry(Holder<T> registryEntry) {
        this.putStringEntry(getNameBytesFromRegistry(registryEntry));
    }


    public void compoundEntryStart() {
        // nop
    }


    @Deprecated
    public void putElementEntry(Tag data) {
        data.accept(this.getVisitor());
    }


    @Deprecated
    public void putElementList(String name, List<? extends Tag> element) {
        if (element.isEmpty()) {
            this.startFixedList(NbtWriter.getStringBytes(name), element.size(), (byte) 0);
        } else {
            this.startFixedList(NbtWriter.getStringBytes(name), element.size(), element.get(0).getId());
        }
        for (Tag elementBase : element) {
            elementBase.accept(this.getVisitor());
        }
    }


    public void startFixedListEntry(int size, byte heldType) {
        this.claimCapacity(1L + 4);
        this.insertByte(heldType);
        this.insertInt(size);
    }
    //endregion

    //region compound entries
    //boolean

    public void putBoolean(byte[] name, boolean value) {
        this.putByte(name, value ? (byte) 1 : 0);
    }

    //byte

    public void putByte(byte[] name, byte value) {
        this.claimCapacity(1L + name.length + 1);
        this.insertByte(Tag.TAG_BYTE);
        this.insertByteArray(name);
        this.insertByte(value);
    }

    //short

    public void putShort(byte[] name, short value) {
        this.claimCapacity(1L + name.length + 2);
        this.insertByte(Tag.TAG_SHORT);
        this.insertByteArray(name);
        this.insertShort(value);
    }

    //int

    public void putInt(byte[] name, int value) {
        this.claimCapacity(1L + name.length + 4);
        this.insertByte(Tag.TAG_INT);
        this.insertByteArray(name);
        this.insertInt(value);
    }

    //long

    public void putLong(byte[] name, long value) {
        this.claimCapacity(1L + name.length + 8);
        this.insertByte(Tag.TAG_LONG);
        this.insertByteArray(name);
        this.insertLong(value);
    }

    //float

    public void putFloat(byte[] name, float value) {
        this.claimCapacity(1L + name.length + 4);
        this.insertByte(Tag.TAG_FLOAT);
        this.insertByteArray(name);
        this.insertFloat(value);
    }

    //double

    public void putDouble(byte[] name, double value) {
        this.claimCapacity(1L + name.length + 8);
        this.insertByte(Tag.TAG_DOUBLE);
        this.insertByteArray(name);
        this.insertDouble(value);
    }

    //string

    @Deprecated
    public void putString(byte[] name, String value) {
        this.putString(name, getStringBytes(value));
    }


    public void putString(byte[] name, byte[] value) {
        this.claimCapacity(1L + name.length + value.length);
        this.insertByte(Tag.TAG_STRING);
        this.insertByteArray(name);
        this.insertByteArray(value);
    }


    public NbtWriter startCompound(byte[] name) {
        this.claimCapacity(1L + name.length);
        this.insertByte(Tag.TAG_COMPOUND);
        this.insertByteArray(name);
        return this;
    }


    public void finishCompound() {
        this.claimCapacity(1);
        this.insertByte(Tag.TAG_END);
    }


    public void putDoubles(byte[] name, double[] value) {
        this.startFixedList(name, value.length, Tag.TAG_DOUBLE);
        for (double d : value) {
            this.putDoubleEntry(d);
        }
    }


    public <T> void putRegistry(byte[] name, Registry<T> registry, T value) {
        // todo: optimize
        this.putString(name, getNameBytesFromRegistry(registry, value));
    }


    public long startList(byte[] name, byte type) {
        this.claimCapacity(1L + name.length + 1 + 4);
        this.insertByte(Tag.TAG_LIST);
        this.insertByteArray(name);
        this.insertByte(type);

        long offset = this.getOffset();
        this.pointer += 4;
        return offset;
    }


    public void finishList(long indicesStart, int indicesCount) {
        UNSAFE.putInt(this.buffer + indicesStart, Integer.reverseBytes(indicesCount));
    }


    public void startFixedList(byte[] name, int size, byte type) {
        this.claimCapacity(1L + name.length + 1 + 4);
        this.insertByte(Tag.TAG_LIST);
        this.insertByteArray(name);
        this.insertByte(type);
        this.insertInt(size);
    }


    public void putByteArray(byte[] name, byte[] value) {
        this.claimCapacity(1L + name.length + 4 + value.length);
        this.insertByte(Tag.TAG_BYTE_ARRAY);
        this.insertByteArray(name);
        this.insertInt(value.length);
        this.insertByteArray(value);
    }


    public void putIntArray(byte[] name, int[] value) {
        this.claimCapacity(1L + name.length + 4 + value.length * 4L);
        this.insertByte(Tag.TAG_INT_ARRAY);
        this.insertByteArray(name);
        this.insertInt(value.length);
        this.insertIntArray(value);
    }


    public void putLongArray(byte[] name, long[] value) {
        this.claimCapacity(1L + name.length + 4 + value.length * 8L);
        this.insertByte(Tag.TAG_LONG_ARRAY);
        this.insertByteArray(name);
        this.insertInt(value.length);
        this.insertLongArray(value);
    }

    public void putLongArray(byte[] name, LongCollection value) {
        this.claimCapacity(1L + name.length + 4 + value.size() * 8L);
        this.insertByte(Tag.TAG_LONG_ARRAY);
        this.insertByteArray(name);
        this.insertInt(value.size());
        this.insertLongArray(value);
    }


    @Deprecated
    public void putElement(String name, Tag data) {
        this.getVisitor().visit(name, data);
    }

    @Deprecated
    public void putElement(byte[] name, Tag data) {
        this.getVisitor().visit(name, data);
    }

    //endregion


    public static byte @NotNull [] getAsciiStringBytes(String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        for (byte aByte : bytes) {
            if (aByte <= 0) {
                throw new IllegalArgumentException("String contains invalid characters");
            }
        }
        return wrapAsciiBytes(bytes);
    }

    @NotNull
    private static byte[] wrapAsciiBytes(byte[] bytes) {
        byte[] wrappedBytes = new byte[bytes.length + 2];
        // store length in first 2 bytes
        wrappedBytes[0] = (byte) (bytes.length >> 8);
        wrappedBytes[1] = (byte) (bytes.length);
        System.arraycopy(bytes, 0, wrappedBytes, 2, bytes.length);
        return wrappedBytes;
    }

    public static byte @NotNull [] getStringBytes(String string) {
        ;
        byte[] res = new byte[string.length() * 3 + 2];
        int index = 2;
        for (char c : string.toCharArray()) {
            if (c >= '\u0001' && c <= '\u007f') {
                res[index++] = (byte) c;
            } else if (c <= '\u07ff') {
                res[index++] = (byte) (0xc0 | (0x1f & (c >> 6)));
                res[index++] = (byte) (0x80 | (0x3f & c));
            } else {
                res[index++] = (byte) (0xe0 | (0x0f & (c >> 12)));
                res[index++] = (byte) (0x80 | (0x3f & (c >> 6)));
                res[index++] = (byte) (0x80 | (0x3f & c));
            }
        }

        int length = index - 2;

        if (length > 65535) {
            throw new RuntimeException(new UTFDataFormatException("String too large"));
        }

        res[0] = (byte) (length >> 8);
        res[1] = (byte) (length);

        return Arrays.copyOf(res, index);
    }

    public static <T> byte @NotNull [] getNameBytesFromRegistry(Registry<T> registry, T value) {
        return getNameBytesFromId(registry.getKey(value));
    }

    public static <T> byte @NotNull [] getNameBytesFromRegistry(Holder<T> value) {
        return getNameBytesFromId(value.unwrapKey().get().location());
    }

    public static <T> byte @NotNull [] getNameBytesFromId(ResourceLocation id) {
        if (id instanceof StringBytesConvertible stringBytesConvertible) {
            return stringBytesConvertible.getStringBytes();
        }
        return getAsciiStringBytes(id.toString());
    }

    public NbtWriterVisitor getVisitor() {
        if (this.visitor == null) {
            this.visitor = new NbtWriterVisitor(this);
        }
        return this.visitor;
    }

    public byte[] toByteArray() {
        var bytes = new byte[(int) (this.getOffset())];
        UNSAFE.copyMemory(null, this.buffer, bytes, BYTE_ARRAY_OFFSET, bytes.length);
        return bytes;
    }

    public void release() {
        if (this.buffer != 0) {
            UNSAFE.freeMemory(this.buffer);
            this.buffer = 0;
        }
    }
}