package com.ciallo.mixin;

import com.ciallo.module.render.Nick;
import com.ciallo.module.render.NoRender;
import com.ciallo.module.render.PopChams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Nick: rewrites the local player's name in outgoing chat messages.
 * PopChams: detects the "totem of undying used" entity event and starts the pop animation.
 * NoRender: cancels incoming title / subtitle packets.
 */
@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    @Unique
    private boolean hudbro$ignoreNextChat = false;

    @Inject(method = "setTitleText", at = @At("HEAD"), cancellable = true)
    private void hudbro$noTitle(ClientboundSetTitleTextPacket packet, CallbackInfo ci) {
        NoRender module = NoRender.INSTANCE;
        if (module != null && module.isAntiTitle()) {
            ci.cancel();
        }
    }

    @Inject(method = "setSubtitleText", at = @At("HEAD"), cancellable = true)
    private void hudbro$noSubtitle(ClientboundSetSubtitleTextPacket packet, CallbackInfo ci) {
        NoRender module = NoRender.INSTANCE;
        if (module != null && module.isAntiTitle()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleEntityEvent", at = @At("TAIL"))
    private void hudbro$onEntityEvent(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        // Vanilla id 35, fired when a totem of undying saves a living entity.
        if (packet.getEventId() != EntityEvent.PROTECTED_FROM_DEATH) {
            return;
        }
        PopChams module = PopChams.INSTANCE;
        if (module == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity entity = packet.getEntity(minecraft.level);
        if (entity instanceof Player player) {
            module.onTotemPop(player);
        }
    }

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void hudbro$onSendChat(String message, CallbackInfo ci) {
        if (this.hudbro$ignoreNextChat) {
            return;
        }
        Nick nick = Nick.INSTANCE;
        if (nick == null || !nick.isEnabled() || !nick.shouldReplaceInChat()) {
            return;
        }
        String processed = nick.replaceName(message);
        if (processed == null || processed.equals(message)) {
            return;
        }
        this.hudbro$ignoreNextChat = true;
        try {
            ((ClientPacketListener) (Object) this).sendChat(processed);
        } finally {
            this.hudbro$ignoreNextChat = false;
        }
        ci.cancel();
    }
}
