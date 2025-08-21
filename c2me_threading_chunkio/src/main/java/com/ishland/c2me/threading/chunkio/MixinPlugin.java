package com.ishland.c2me.threading.chunkio;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import net.sjhub.c2me.utils.ModUtil;

public class MixinPlugin extends ModuleMixinPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!super.shouldApplyMixin(targetClassName, mixinClassName)) return false;

        if (mixinClassName.startsWith("com.ishland.c2me.threading.chunkio.mixin.gc_free_serializer.")) {
            return ModUtil.isModLoaded("c2me_rewrites_chunk_serializer");
        }

        return true;
    }
}
