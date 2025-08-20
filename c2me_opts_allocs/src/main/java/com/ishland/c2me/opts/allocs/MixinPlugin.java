package com.ishland.c2me.opts.allocs;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import net.minecraftforge.fml.ModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinPlugin extends ModuleMixinPlugin {

    // TODO: find better way then Modlist.get()
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!super.shouldApplyMixin(targetClassName, mixinClassName)) return false;
        if (ModList.get() == null) return true;

        if (mixinClassName.equals("com.ishland.c2me.opts.allocs.mixin.MixinNbtCompound") ||
                mixinClassName.equals("com.ishland.c2me.opts.allocs.mixin.MixinNbtCompound1"))
            return !ModList.get().isLoaded("lithium") &&
                    !ModList.get().isLoaded("radium") &&
                    !ModList.get().isLoaded("canary");

        if (mixinClassName.startsWith("com.ishland.c2me.opts.allocs.mixin.surfacebuilder.")) {
            return !ModList.get().isLoaded("quilted_fabric_api") &&
                   !ModList.get().isLoaded("frozenlib");
        }

        return true;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        super.preApply(targetClassName, targetClass, mixinClassName, mixinInfo);
//        ASMTransformerNbtOpsMapBuilderFastUtilMap.transform(targetClass); // SJhub - I don't need this on forge
    }

}
