package com.ishland.c2me.server.utils.common;

import com.ishland.c2me.base.mixin.access.IServerChunkManager;
import com.ishland.c2me.notickvd.common.IChunkTicketManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraftforge.fml.loading.FMLLoader;

public class C2MECommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("c2me")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.literal("notick")
                                        .requires(unused -> com.ishland.c2me.notickvd.ModuleEntryPoint.enabled)
                                        .executes(C2MECommands::noTickCommand)
                        )
                        .then(
                                Commands.literal("debug")
                                        .requires(unused -> !FMLLoader.isProduction())
//                                        .then(
//                                                CommandManager.literal("mobcaps")
//                                                        .requires(unused -> com.ishland.c2me.notickvd.ModuleEntryPoint.enabled)
//                                                        .executes(C2MECommands::mobcapsCommand)
//                                        )
                        )
        );
    }

    private static int noTickCommand(CommandContext<CommandSourceStack> ctx) {
        final ServerChunkCache chunkManager = ctx.getSource().getLevel().getLevel().getChunkSource();
        final DistanceManager ticketManager = ((IServerChunkManager) chunkManager).getTicketManager();
        final int noTickOnlyChunks = ((IChunkTicketManager) ticketManager).getNoTickOnlyChunks().size();
        final int noTickPendingTicketUpdates = ((IChunkTicketManager) ticketManager).getNoTickPendingTicketUpdates();
        ctx.getSource().sendSuccess(() -> Component.nullToEmpty(String.format("No-tick chunks: %d", noTickOnlyChunks)), true);
        ctx.getSource().sendSuccess(() -> Component.nullToEmpty(String.format("No-tick chunk pending ticket updates: %d", noTickPendingTicketUpdates)), true);

        return 0;
    }

//    private static int mobcapsCommand(CommandContext<ServerCommandSource> ctx) {
//        final ServerWorld serverWorld = ctx.getSource().getWorld().toServerWorld();
//        final ServerChunkManager chunkManager = serverWorld.getChunkManager();
//        final ChunkTicketManager ticketManager = ((IServerChunkManager) chunkManager).getTicketManager();
//        final LongSet noTickOnlyChunks = ((IChunkTicketManager) ticketManager).getNoTickOnlyChunks();
//        final Iterable<Entity> iterable;
//        if (noTickOnlyChunks == null) {
//            iterable = serverWorld.iterateEntities();
//        } else {
//            iterable = new FilteringIterable<>(serverWorld.iterateEntities(), entity -> !noTickOnlyChunks.contains(entity.getChunkPos().toLong()));
//        }
//
//        ctx.getSource().sendFeedback(Text.of("Mobcap details"), true);
//        for (Entity entity : iterable) {
//            if (entity instanceof MobEntity mobEntity) {
//                ctx.getSource().sendFeedback(Text.of(String.format("%s: ", mobEntity.getType().getSpawnGroup().asString())).(mobEntity.getDisplayName()).append(String.format(" in %s", mobEntity.getChunkPos())), true);
//            }
//        }
//        return 0;
//    }

}
