package com.ishland.c2me.rewrites.chunkio;

import com.ishland.c2me.base.common.config.ConfigSystem;
import net.minecraftforge.fml.common.Mod;

@Mod("c2me_rewrites_chunkio")
public class ModuleEntryPoint {

    private static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("ioSystem.replaceImpl")
            .comment("Whether to use the optimized implementation of IO system")
            .getBoolean(com.ishland.c2me.base.ModuleEntryPoint.globalExecutorParallelism >= 2, false);

}
