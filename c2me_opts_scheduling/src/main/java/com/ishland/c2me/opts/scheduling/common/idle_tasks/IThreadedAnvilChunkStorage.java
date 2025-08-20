package com.ishland.c2me.opts.scheduling.common.idle_tasks;

import net.minecraft.world.level.ChunkPos;

public interface IThreadedAnvilChunkStorage {

    void enqueueDirtyChunkPosForAutoSave(ChunkPos chunkPos);

    boolean runOneChunkAutoSave();

}
