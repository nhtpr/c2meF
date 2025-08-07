package com.ishland.c2me.base.mixin.access;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructurePiece.class)
public interface IStructurePiece {

    @Accessor
    StructurePieceType getType();

    @Accessor
    BoundingBox getBoundingBox();

    @Accessor("orientation")
    Direction getFacing();

    @Accessor
    Mirror getMirror();

    @Accessor
    Rotation getRotation();

    @Accessor("genDepth")
    int getChainLength();
}
