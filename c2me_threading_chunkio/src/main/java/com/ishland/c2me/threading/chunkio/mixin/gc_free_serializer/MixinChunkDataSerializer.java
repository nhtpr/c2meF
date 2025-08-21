package com.ishland.c2me.threading.chunkio.mixin.gc_free_serializer;

import com.ishland.c2me.threading.chunkio.common.AsyncSerializationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;

@Pseudo
@Mixin(targets = "com.ishland.c2me.rewrites.chunk_serializer.common.ChunkDataSerializer")
public class MixinChunkDataSerializer {

    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getBlockEntitiesPos()Ljava/util/Set;"))
    private static Set<BlockPos> onChunkGetBlockEntityPositions(ChunkAccess chunk) {
        final AsyncSerializationManager.Scope scope = AsyncSerializationManager.getScope(chunk.getPos());
        return scope != null ? scope.blockEntityPositions : chunk.getBlockEntitiesPos();
    }

    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getBlockEntityNbtForSaving(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/nbt/CompoundTag;"))
    private static CompoundTag onChunkGetPackedBlockEntityNbt(ChunkAccess chunk, BlockPos pos) {
        final AsyncSerializationManager.Scope scope = AsyncSerializationManager.getScope(chunk.getPos());
        if (scope == null) return chunk.getBlockEntityNbtForSaving(pos);
        final BlockEntity blockEntity = scope.blockEntities.get(pos);
        if (blockEntity != null) {
            final CompoundTag nbtCompound = blockEntity.saveWithFullMetadata();
            if (chunk instanceof LevelChunk) nbtCompound.putBoolean("keepPacked", false);
            return nbtCompound;
        } else {
            final CompoundTag nbtCompound = scope.pendingBlockEntityNbtsPacked.get(pos);
            if (nbtCompound != null && chunk instanceof LevelChunk) nbtCompound.putBoolean("keepPacked", true);
//            if (nbtCompound == null) LOGGER.warn("Block Entity at {} for block {} doesn't exist", pos, chunk.getBlockState(pos).getBlock());
            return nbtCompound;
        }
    }

    @Redirect(method = "writeSectionDataVanilla", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;getLayerListener(Lnet/minecraft/world/level/LightLayer;)Lnet/minecraft/world/level/lighting/LayerLightEventListener;"))
    private static LayerLightEventListener onLightingProviderGet(LevelLightEngine lightingProvider, LightLayer lightType) {
        final AsyncSerializationManager.Scope scope = AsyncSerializationManager.getScope(null);
        return scope != null ? scope.lighting.get(lightType) : lightingProvider.getLayerListener(lightType);
    }

}
