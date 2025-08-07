package com.ishland.c2me.base.common.scheduler;

import net.minecraft.server.level.ChunkHolder;

public class ThreadLocalWorldGenSchedulingState {

    private static final ThreadLocal<ChunkHolder> chunkHolder = new ThreadLocal<>();

    public static ChunkHolder getChunkHolder() {
        return chunkHolder.get();
    }

    public static void setChunkHolder(ChunkHolder holder) {
        chunkHolder.set(holder);
    }

    public static void clearChunkHolder() {
        chunkHolder.remove();
    }

}
