package com.ishland.c2me.fixes.worldgen.threading_issues;

import com.ishland.c2me.fixes.worldgen.threading_issues.common.Config;
import net.minecraftforge.fml.common.Mod;

@Mod("c2me_fixes_worldgen_threading_issues")
public class ModuleEntryPoint {

    private static final boolean enabled = true;

    static {
        Config.init();
    }

}
