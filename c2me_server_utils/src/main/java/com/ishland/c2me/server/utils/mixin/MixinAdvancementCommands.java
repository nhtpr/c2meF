package com.ishland.c2me.server.utils.mixin;

import com.ishland.c2me.server.utils.common.C2MECommands;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.AdvancementCommands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementCommands.class)
public class MixinAdvancementCommands {

    @Inject(method = "register", at = @At("TAIL"))
    private static void registerCommands(CommandDispatcher<CommandSourceStack> p_136311_, CallbackInfo ci) {
        C2MECommands.register(p_136311_);
    }
}
