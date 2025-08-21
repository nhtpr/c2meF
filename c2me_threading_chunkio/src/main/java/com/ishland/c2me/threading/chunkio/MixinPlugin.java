package com.ishland.c2me.threading.chunkio;

import com.ishland.c2me.base.common.ModuleMixinPlugin;

public class MixinPlugin extends ModuleMixinPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!super.shouldApplyMixin(targetClassName, mixinClassName)) return false;

        if (mixinClassName.startsWith("com.ishland.c2me.threading.chunkio.mixin.gc_free_serializer.")) {
            return true;
            // return FabricLoader.getInstance().isModLoaded("c2me-rewrites-chunk-serializer"); // SJhub - find better way than ModList.get()
        }

        return true;
    }
}
