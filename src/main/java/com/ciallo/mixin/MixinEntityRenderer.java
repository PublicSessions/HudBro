package com.ciallo.mixin;

import com.ciallo.module.render.Nick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nick: replaces the local player's own name tag with the nickname.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(method = "getNameTag", at = @At("RETURN"), cancellable = true)
    private void hudbro$replaceNickInNameTag(Entity entity, CallbackInfoReturnable<Component> cir) {
        Nick nick = Nick.INSTANCE;
        if (nick == null || !nick.isEnabled() || !nick.shouldReplaceInNametags()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!(entity instanceof Player player) || mc.player == null || player != mc.player) {
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
