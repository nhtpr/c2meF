package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RandomizedIntStateProvider.class)
public class MixinRandomizedIntBlockStateProvider {

    @Shadow @Nullable private IntegerProperty property;

    @Redirect(method = "getState", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/levelgen/feature/stateproviders/RandomizedIntStateProvider;property:Lnet/minecraft/world/level/block/state/properties/IntegerProperty;", opcode = Opcodes.PUTFIELD))
    private void redirectGetProperty(RandomizedIntStateProvider randomizedIntBlockStateProvider, IntegerProperty value) {
        if (this.property != null) System.err.println("Detected different property settings in RandomizedIntBlockStateProvider! Expected " + this.property + " but got " + value);
        synchronized (this) {
            this.property = value;
        }
    }

}
