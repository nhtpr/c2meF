package com.ishland.c2me.notickvd.common;

import net.minecraft.server.level.ServerPlayer;

public class NoTickChunkSendingInterceptor {

    public static boolean onChunkSending(ServerPlayer player, long pos) {
        return true;
    }

}
