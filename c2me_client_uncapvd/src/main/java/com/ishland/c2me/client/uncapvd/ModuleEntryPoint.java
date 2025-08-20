package com.ishland.c2me.client.uncapvd;

import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.client.uncapvd.common.Config;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod("c2me_client_uncapvd")
public class ModuleEntryPoint {

    private static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("clientSideConfig.modifyMaxVDConfig.enabled")
            .comment("Whether to modify maximum view distance")
            .incompatibleMod("bobby", "*")
            .getBoolean(true, false);

    static {
        Config.init();
    }

}
