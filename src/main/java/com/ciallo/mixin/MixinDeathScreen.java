package com.ciallo.mixin;

import com.ciallo.module.render.Nick;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Nick: replaces the local player's name inside the death screen message.
 */
@Mixin(DeathScreen.class)
public class MixinDeathScreen {

    // Must be static: the injection point runs before super() is called in the constructor.
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static Component hudbro$replaceNickInDeathMessage(Component causeOfDeath) {
        Nick nick = Nick.INSTANCE;
        if (nick == null || !nick.isEnabled() || !nick.shouldReplaceInDeathMessages()) {
            return causeOfDeath;
        }
        return nick.replaceComponent(causeOfDeath);
    }
}
