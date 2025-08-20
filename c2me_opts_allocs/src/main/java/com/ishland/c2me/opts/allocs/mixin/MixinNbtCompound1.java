package com.ishland.c2me.opts.allocs.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;

@Mixin(targets = "net.minecraft.nbt.CompoundTag$1")
public abstract class MixinNbtCompound1 implements TagType<CompoundTag> {

    @SuppressWarnings("UnnecessaryQualifiedMemberReference")
    @ModifyVariable(method = "load(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
    private Map<String, Tag> modifyMap(Map<String, Tag> map) {
        return new Object2ObjectOpenHashMap<>();
    }

    @SuppressWarnings("UnnecessaryQualifiedMemberReference")
    @Redirect(method = "load(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
    private <K, V> HashMap<K, V> redirectNewHashMap() {
        return null; // avoid double map creation
    }

}
