package com.ishland.c2me.opts.allocs.mixin;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagTypes;

@Mixin(ListTag.class)
public abstract class MixinNbtList extends CollectionTag<Tag> {

    @Shadow private byte type;

    @Shadow @Final private List<Tag> list;

    @Shadow protected abstract boolean updateType(Tag element);

    /**
     * @author ishland
     * @reason copy using fastutil list
     */
    @Overwrite
    public ListTag copy() {
        Iterable<Tag> iterable = TagTypes.getType(this.type).isValue() ? this.list : Iterables.transform(this.list, Tag::copy);
        List<Tag> list = new ObjectArrayList<>(this.list.size());
        iterable.forEach(list::add);
        return new ListTag(list, this.type);
    }

    @ModifyArg(method = "<init>()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/ListTag;<init>(Ljava/util/List;B)V"), index = 0)
    private static List<Tag> modifyList(List<Tag> list) {
        return new ObjectArrayList<>();
    }

    @Redirect(method = "<init>()V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;", remap = false))
    private static <E> ArrayList<E> redirectNewArrayList() {
        return null; // avoid double list creation
    }

    @Override
    public boolean add(Tag element) {
        if (this.updateType(element)) {
            this.list.add(element);
            return true;
        } else {
            return false;
        }
    }
}
