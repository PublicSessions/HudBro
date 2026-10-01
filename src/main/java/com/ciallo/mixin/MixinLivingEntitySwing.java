package com.ciallo.mixin;

import com.ciallo.module.render.ViewModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ViewModel: slows (or speeds up) the arm swing animation.
 *
 * <p>Vanilla computes {@code attackAnim = swingTime / swingDuration} in
 * {@code LivingEntity#updateSwingTime}; the value is remapped to {@code attackAnim^(value / 6)} so
 * 6 keeps the vanilla curve, larger values lag behind (slower swing) and smaller values run ahead.</p>
 */
@Mixin(LivingEntity.class)
public class MixinLivingEntitySwing {

    @Shadow
    public float attackAnim;

    @Inject(method = "updateSwingTime", at = @At("TAIL"))
    private void hudbro$swingSpeed(CallbackInfo ci) {
        ViewModel module = ViewModel.INSTANCE;
        if (module == null || !module.isEnabled() || !module.isSwingSpeed()) {
            return;
        }
        float value = Math.max(1.0f, module.getSwingValue());
        if (Math.abs(value - 6.0f) < 0.01f) {
            return;
        }
        this.attackAnim = (float) Math.pow(this.attackAnim, value / 6.0);
    }
}
