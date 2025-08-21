package com.ishland.c2me.threading.worldgen;

import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.threading.worldgen.common.Config;
import net.minecraftforge.fml.common.Mod;

import static com.ishland.c2me.base.ModuleEntryPoint.globalExecutorParallelism;

@Mod("c2me_threading_worldgen")
public class ModuleEntryPoint {

    public static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("threadedWorldGen.enabled")
            .comment("Whether to enable this feature")
            .getBoolean(globalExecutorParallelism >= 3, false);

    static {
        Config.init();
    }

}
