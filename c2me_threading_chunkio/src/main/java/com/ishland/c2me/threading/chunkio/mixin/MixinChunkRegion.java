package com.ishland.c2me.threading.chunkio.mixin;

import com.ishland.c2me.threading.chunkio.common.ProtoChunkExtension;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;

@Mixin(WorldGenRegion.class)
public abstract class MixinChunkRegion implements WorldGenLevel {

    @WrapOperation(method = "setBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;onBlockStateChange(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private void waitForFutureBeforeNotifyChanges(ServerLevel instance, BlockPos pos, BlockState oldBlock, BlockState newBlock, Operation<Void> operation) {
        final ChunkAccess chunk = this.getChunk(pos);
        if (chunk instanceof ProtoChunk protoChunk) {
            final CompletableFuture<Void> future = ((ProtoChunkExtension) protoChunk).getInitialMainThreadComputeFuture();
            if (future != null && !future.isDone()) {
                future.thenRun(() -> operation.call(instance, pos, oldBlock, newBlock));
                return;
            }
        }
        operation.call(instance, pos, oldBlock, newBlock);
    }

}
