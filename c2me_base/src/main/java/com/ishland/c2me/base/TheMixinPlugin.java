package com.ishland.c2me.base;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import io.izzel.arclight.common.mod.mixins.MixinProcessor;
import io.izzel.arclight.common.mod.mixins.TransformAccessProcessor;
import io.izzel.arclight.mixin.MixinTools;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;

/**
 * Used internally for c2me-base, do not subclass.
 */
public final class TheMixinPlugin extends ModuleMixinPlugin {

    // SJhub start - thanks for Arclight
    private final List<MixinProcessor> postProcessors = List.of(
            new TransformAccessProcessor()
    );
    // SJhub end - thanks for Arclight

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!super.shouldApplyMixin(targetClassName, mixinClassName)) {
            return false;
        }

        if (mixinClassName.startsWith("com.ishland.c2me.base.mixin.util.log4j2shutdownhookisnomore."))
            return ModuleEntryPoint.disableLoggingShutdownHook;

        return true;
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // SJhub start - thanks for Arclight
        for (var processor : this.postProcessors) {
            processor.accept(targetClassName, targetClass, mixinInfo);
        }
        MixinTools.onPostMixin(targetClass);
        // SJhub end - thanks for Arclight
    }
}
