package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.asm;

import io.izzel.arclight.common.mod.mixins.annotation.TransformAccess;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StrongholdPieces.PortalRoom.class)
public class StrongholdPieces$PortalRoomASM {

    @Shadow
    @TransformAccess(Opcodes.ACC_VOLATILE)
    private boolean hasPlacedSpawner;
}
