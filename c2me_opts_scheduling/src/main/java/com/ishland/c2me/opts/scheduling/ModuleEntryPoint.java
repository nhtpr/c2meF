package com.ishland.c2me.opts.scheduling;

import com.ishland.c2me.opts.scheduling.common.Config;
import net.minecraftforge.fml.common.Mod;

@Mod("c2me_opts_scheduling")
public class ModuleEntryPoint {

    private static final boolean enabled = true;

    static {
        Config.init();
    }

}
