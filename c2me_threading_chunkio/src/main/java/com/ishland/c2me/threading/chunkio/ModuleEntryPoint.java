package com.ishland.c2me.threading.chunkio;

import com.ishland.c2me.base.common.config.ConfigSystem;
import net.minecraftforge.fml.common.Mod;

@Mod("c2me_threading_chunkio")
public class ModuleEntryPoint {

    private static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("ioSystem.async")
            .comment("Whether to use async chunk loading & unloading")
            .incompatibleMod("radon", "*")
            .getBoolean(true, false);

}
