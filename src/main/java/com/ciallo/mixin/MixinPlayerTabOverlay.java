package com.ciallo.mixin;

import com.ciallo.module.render.Nick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nick: shows the nickname instead of the real name in the tab list.
 */
@Mixin(PlayerTabOverlay.class)
public class MixinPlayerTabOverlay {

    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void hudbro$replaceNickInTabList(PlayerInfo playerInfo, CallbackInfoReturnable<Component> cir) {
        Nick nick = Nick.INSTANCE;
        if (nick == null || !nick.isEnabled() || !nick.shouldReplaceInTabList()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || playerInfo == null) {
            return;
        }
        String entryName = playerInfo.getProfile().name();
        if (entryName == null || !entryName.equals(mc.player.getName().getString())) {
            return;
        }
        Component original = cir.getReturnValue();
        MutableComponent replacement = Component.literal(nick.getNickName());
        if (original != null) {
            replacement.setStyle(original.getStyle());
        }
        cir.setReturnValue(replacement);
    }
}
