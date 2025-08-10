package com.ishland.c2me.fixes.worldgen.threading_issues;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import com.ishland.c2me.fixes.worldgen.threading_issues.common.debug.SMAPPool;
import io.izzel.arclight.common.mod.mixins.MixinProcessor;
import io.izzel.arclight.common.mod.mixins.TransformAccessProcessor;
import io.izzel.arclight.mixin.MixinTools;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

import java.util.List;

public class MixinPlugin extends ModuleMixinPlugin {

    // SJhub start - thanks for Arclight
    private final List<MixinProcessor> postProcessors = List.of(
            new TransformAccessProcessor()
    );
    // SJhub end - thanks for Arclight

    @Override
    public void onLoad(String mixinPackage) {
        super.onLoad(mixinPackage);
        if (MixinEnvironment.getCurrentEnvironment().getActiveTransformer() instanceof IMixinTransformer transformer &&
            transformer.getExtensions() instanceof Extensions extensions) {
            extensions.add(new IExtension() {
                @Override
                public boolean checkActive(MixinEnvironment environment) {
                    return true;
                }

                @Override
                public void preApply(ITargetClassContext context) {

                }

                @Override
                public void postApply(ITargetClassContext context) {

                }

                @Override
                public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
                    SMAPPool.put(name, classNode);
                }
            });
        } else {
            System.err.println("Failed to initialize SMAP parser for safe world random access, mod information for mixin injected methods will not be available");
        }
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
//        ASMTransformerMakeVolatile.transform(targetClass); // SJhub - I don't need this on forge
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
