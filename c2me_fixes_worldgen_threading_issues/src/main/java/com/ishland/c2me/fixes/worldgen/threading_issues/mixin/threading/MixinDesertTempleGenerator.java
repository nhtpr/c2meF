package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReferenceArray;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidPiece;

@Mixin(DesertPyramidPiece.class)
public abstract class MixinDesertTempleGenerator {

    private final AtomicReferenceArray<Boolean> hasPlacedChestAtomic = new AtomicReferenceArray<>(new Boolean[4]);

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        for (int i = 0; i < this.hasPlacedChestAtomic.length(); i ++) {
            if (this.hasPlacedChestAtomic.get(i) == null) {
                this.hasPlacedChestAtomic.set(i, false);
            }
        }
    }

    @Dynamic
    @SuppressWarnings({"InvalidInjectorMethodSignature", "RedundantSuppression"})
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/levelgen/structure/structures/DesertPyramidPiece;hasPlacedChest:[Z", opcode = Opcodes.GETFIELD, args = "array=set"))
    private void redirectSetHasPlacedChest(boolean[] array, int index, boolean value) {
        this.hasPlacedChestAtomic.compareAndSet(index, false, value);
    }

    @Dynamic
    @SuppressWarnings({"InvalidInjectorMethodSignature", "RedundantSuppression"})
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/levelgen/structure/structures/DesertPyramidPiece;hasPlacedChest:[Z", opcode = Opcodes.GETFIELD, args = "array=get"))
    private boolean redirectGetHasPlacedChest(boolean[] array, int index) {
        final Boolean aBoolean = this.hasPlacedChestAtomic.get(index);
        return aBoolean != null ? aBoolean : false;
    }

}
