package com.ishland.c2me.threading.chunkio.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

public interface ISerializingRegionBasedStorage {

    void update(ChunkPos pos, CompoundTag tag);

}
