package com.ishland.c2me.threading.lighting;

import net.minecraftforge.fml.common.Mod;
import net.sjhub.c2me.utils.ModUtil;

@Mod("c2me_threading_lighting")
public class ModuleEntryPoint {

    private static final boolean enabled = !ModUtil.isModLoaded("lightbench");
}
