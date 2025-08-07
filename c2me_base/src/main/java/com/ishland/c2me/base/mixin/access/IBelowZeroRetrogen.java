package com.ishland.c2me.base.mixin.access;

import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.BitSet;


@Mixin(value = BelowZeroRetrogen.class)
public interface IBelowZeroRetrogen {
    @Accessor
    BitSet getMissingBedrock();

    @Invoker("targetStatus")
    ChunkStatus invokeGetTargetStatus();
}
