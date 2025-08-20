package com.ishland.c2me.opts.allocs.mixin.object_pooling_caching;

import com.ishland.c2me.opts.allocs.common.ObjectCachingUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.BitSet;
import net.minecraft.world.level.levelgen.feature.OreFeature;

@Mixin(OreFeature.class)
public class MixinOreFeature {

    @Redirect(method = "doPlace", at = @At(value = "NEW", target = "java/util/BitSet"))
    private BitSet redirectNewBitSet(int nbits) {
        return ObjectCachingUtils.getCachedOrNewBitSet(nbits);
    }

}
