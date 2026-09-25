package com.ciallo.mixin;

import com.ciallo.module.render.Nick;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Nick: rewrites incoming chat messages so the local player's real name shows up as the nickname.
 */
@Mixin(ChatComponent.class)
public class MixinChatComponent {

    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Component hudbro$replaceNickInMessage(Component message) {
        Nick nick = Nick.INSTANCE;
        if (nick == null || !nick.isEnabled() || !nick.shouldReplaceInChat()) {
            return message;
        }
        return nick.replaceComponent(message);
    }

    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Component hudbro$replaceNickInSimpleMessage(Component message) {
        Nick nick = Nick.INSTANCE;
        if (nick == null || !nick.isEnabled() || !nick.shouldReplaceInChat()) {
            return message;
        }
        return nick.replaceComponent(message);
    }
}
