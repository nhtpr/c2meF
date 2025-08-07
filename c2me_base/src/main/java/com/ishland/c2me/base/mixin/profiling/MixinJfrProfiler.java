package com.ishland.c2me.base.mixin.profiling;

import com.ishland.c2me.base.common.profiling.ChunkLoadScheduleEvent;
import com.ishland.c2me.base.common.profiling.IVanillaJfrProfiler;
import jdk.jfr.Event;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.jfr.JfrProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(JfrProfiler.class)
public class MixinJfrProfiler implements IVanillaJfrProfiler {

    @Mutable
    @Shadow @Final private static List<Class<? extends Event>> CUSTOM_EVENTS;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void preInit(CallbackInfo ci) {
        ArrayList<Class<? extends Event>> copy = new ArrayList<>(CUSTOM_EVENTS);
        copy.add(ChunkLoadScheduleEvent.class);
        CUSTOM_EVENTS = List.copyOf(copy);
    }


    @Override
    public ProfiledDuration startChunkLoadSchedule(ChunkPos chunkPos, ResourceKey<Level> world, String targetStatus) {
        if (!ChunkLoadScheduleEvent.TYPE.isEnabled()) {
            return null;
        } else {
            ChunkLoadScheduleEvent event = new ChunkLoadScheduleEvent(chunkPos, world, targetStatus);
            event.begin();
            return event::commit;
        }
    }
}
