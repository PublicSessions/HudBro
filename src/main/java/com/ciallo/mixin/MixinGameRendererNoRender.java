package com.ciallo.mixin;

import com.ciallo.module.render.NoRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NoRender: removes the "hurt camera" tilt.
 */
@Mixin(GameRenderer.class)
public class MixinGameRendererNoRender {

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void hudbro$noHurtCam(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        NoRender module = NoRender.INSTANCE;
        if (module != null && module.isHurtCam()) {
            ci.cancel();
        }
    }
}
