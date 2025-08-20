package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.asm;

import io.izzel.arclight.common.mod.mixins.annotation.TransformAccess;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(OceanMonumentPieces.MonumentBuilding.class)
public class OceanMonumentPieces$MonumentBuildingASM {

    @Shadow
    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_VOLATILE)
    private OceanMonumentPieces.RoomDefinition sourceRoom;

    @Shadow
    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_VOLATILE)
    private OceanMonumentPieces.RoomDefinition coreRoom;
}
