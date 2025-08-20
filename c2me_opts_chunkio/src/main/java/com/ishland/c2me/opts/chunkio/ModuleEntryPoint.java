package com.ishland.c2me.opts.chunkio;

import com.ishland.c2me.opts.chunkio.common.Config;
import net.minecraftforge.fml.common.Mod;

@Mod("c2me_opts_chunkio")
public class ModuleEntryPoint {

    private static final boolean enabled = true;

    static {
        Config.init();
    }

}
