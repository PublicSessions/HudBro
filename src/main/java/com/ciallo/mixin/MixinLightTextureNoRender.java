package com.ciallo.mixin;

import com.ciallo.module.render.NoRender;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NoRender: removes the darkness effect dimming.
 */
@Mixin(LightTexture.class)
public class MixinLightTextureNoRender {

    @Inject(method = "calculateDarknessScale", at = @At("HEAD"), cancellable = true)
    private void hudbro$noDarkness(LivingEntity entity, float partialTick, float darkenWorldAmount, CallbackInfoReturnable<Float> cir) {
        NoRender module = NoRender.INSTANCE;
        if (module != null && module.isDarkness()) {
            cir.setReturnValue(0.0f);
        }
    }
}
