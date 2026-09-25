package com.ciallo.mixin;

import com.ciallo.module.render.Nick;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nick: replaces the local player's name in scoreboard team display names.
 */
@Mixin(PlayerTeam.class)
public class MixinPlayerTeam {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void hudbro$replaceNickInScoreboard(CallbackInfoReturnable<Component> cir) {
        Nick nick = Nick.INSTANCE;
        if (nick == null || !nick.isEnabled() || !nick.shouldReplaceInScoreboard()) {
            return;
        }
        Component displayName = cir.getReturnValue();
        if (displayName == null) {
            return;
        }
        String original = displayName.getString();
        String processed = nick.replaceName(original);
        if (processed != null && !processed.equals(original)) {
            cir.setReturnValue(Component.literal(processed).setStyle(displayName.getStyle()));
        }
    }
}
