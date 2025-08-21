package com.ishland.c2me.threading.worldgen.mixin;

import com.ishland.c2me.threading.worldgen.common.Config;
import com.ishland.c2me.threading.worldgen.common.IChunkStatus;
import com.ishland.c2me.threading.worldgen.common.debug.StacktraceRecorder;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

@Mixin(WorldGenRegion.class)
public class MixinChunkRegion {

    @Shadow
    @Final
    private ChunkAccess center;
    @Shadow
    @Final
    private ChunkPos firstPos;
    @Shadow
    @Final
    private ChunkPos lastPos;

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private ChunkStatus generatingStatus;
    private ChunkPos lowerReducedCorner = null;
    private ChunkPos upperReducedCorner = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(ServerLevel world, List<ChunkAccess> list, ChunkStatus chunkStatus, int placementRadius, CallbackInfo ci) {
        if (Config.reduceLockRadius) {
            final int reducedTaskRadius = ((IChunkStatus) chunkStatus).getReducedTaskRadius();
            lowerReducedCorner = new ChunkPos(center.getPos().x - reducedTaskRadius, center.getPos().z - reducedTaskRadius);
            upperReducedCorner = new ChunkPos(center.getPos().x + reducedTaskRadius, center.getPos().z + reducedTaskRadius);
        } else {
            lowerReducedCorner = firstPos;
            upperReducedCorner = lastPos;
        }
    }

    @Inject(method = "getChunk(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/ChunkAccess;", at = @At("HEAD"))
    private void onGetChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<ChunkAccess> cir) {
        if (Config.reduceLockRadius && !isInsideReducedTaskRadius(chunkX, chunkZ) && generatingStatus != ChunkStatus.STRUCTURE_REFERENCES) {
            StacktraceRecorder.record();
        }
    }

    @Inject(method = "hasChunk", at = @At("HEAD"))
    private void onIsChunkLoaded(int chunkX, int chunkZ, CallbackInfoReturnable<Boolean> cir) {
        if (Config.reduceLockRadius && !isInsideReducedTaskRadius(chunkX, chunkZ) && generatingStatus != ChunkStatus.STRUCTURE_REFERENCES) {
            StacktraceRecorder.record();
        }
    }

    @Unique
    private boolean isInsideReducedTaskRadius(int chunkX, int chunkZ) {
        return chunkX >= this.lowerReducedCorner.x &&
                chunkX <= this.upperReducedCorner.x &&
                chunkZ >= this.lowerReducedCorner.z &&
                chunkZ <= this.upperReducedCorner.z;
    }

}
