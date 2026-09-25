package com.ciallo.mixin;

import com.ciallo.module.render.Nick;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nick: replaces the local player's name on sign text.
 */
@Mixin(SignText.class)
public class MixinSignText {

    @Inject(method = "getMessage", at = @At("RETURN"), cancellable = true)
    private void hudbro$replaceNickInSign(int line, boolean filtered, CallbackInfoReturnable<Component> cir) {
        Nick nick = Nick.INSTANCE;
        if (nick == null || !nick.isEnabled() || !nick.shouldReplaceInSigns()) {
            return;
        }
        Component text = cir.getReturnValue();
        if (text == null) {
            return;
        }
        String original = text.getString();
        String processed = nick.replaceName(original);
        if (processed != null && !processed.equals(original)) {
            cir.setReturnValue(Component.literal(processed).setStyle(text.getStyle()));
        }
    }
}
