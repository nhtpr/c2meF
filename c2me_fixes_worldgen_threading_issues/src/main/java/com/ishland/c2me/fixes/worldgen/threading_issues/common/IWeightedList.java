package com.ishland.c2me.fixes.worldgen.threading_issues.common;

import net.minecraft.world.entity.ai.behavior.ShufflingList;

public interface IWeightedList<U> {

    public ShufflingList<U> shuffleVanilla();

}
