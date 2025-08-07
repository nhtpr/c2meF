package com.ishland.c2me.base;

import net.minecraftforge.fml.common.Mod;
import com.ishland.c2me.base.common.config.ConfigSystem;

@Mod("c2me_base")
public class C2MEBaseMod {

    public C2MEBaseMod() {
        ConfigSystem.flushConfig();
    }

}
