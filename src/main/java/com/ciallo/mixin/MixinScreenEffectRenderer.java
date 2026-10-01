package com.ciallo.mixin;

import com.ciallo.module.render.NoRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NoRender: removes the "standing in fire" and "under water" screen overlays.
 *
 * <p>Both are the conditions of the vanilla calls, so they are redirected back to the original
 * method when the toggle is off (no private member access needed).</p>
 */
@Mixin(ScreenEffectRenderer.class)
public class MixinScreenEffectRenderer {

    @Inject(method = "renderItemActivationAnimation", at = @At("HEAD"), cancellable = true)
    private void hudbro$noTotemAnimation(PoseStack poseStack, float partialTick, SubmitNodeCollector collector, CallbackInfo ci) {
        NoRender module = NoRender.INSTANCE;
        if (module != null && module.isTotem()) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "renderScreenEffect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z")
    )
    private boolean hudbro$noFireOverlay(LocalPlayer player) {
        NoRender module = NoRender.INSTANCE;
        if (module != null && module.isFireOverlay()) {
            return false;
        }
        return player.isOnFire();
    }

    @Redirect(
            method = "renderScreenEffect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;getViewBlockingState(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private static BlockState hudbro$noBlockOverlay(Player player) {
        NoRender module = NoRender.INSTANCE;
        if (module != null && module.isBlockOverlay()) {
            return null;
        }
        return MixinScreenEffectAccess.hudbro$getViewBlockingState(player);
    }

    @Redirect(
            method = "renderScreenEffect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z")
    )
    private boolean hudbro$noWaterOverlay(LocalPlayer player, TagKey<Fluid> fluid) {
        NoRender module = NoRender.INSTANCE;
        if (module != null && module.isWaterOverlay()) {
            return false;
        }
        return player.isEyeInFluid(fluid);
    }
}
