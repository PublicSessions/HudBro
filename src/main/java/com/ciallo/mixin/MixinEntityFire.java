package com.ciallo.mixin;

import com.ciallo.module.render.NoRender;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NoRender: hides the burning overlay on entities.
 */
@Mixin(Entity.class)
public class MixinEntityFire {

    @Inject(method = "displayFireAnimation", at = @At("RETURN"), cancellable = true)
    private void hudbro$noEntityFire(CallbackInfoReturnable<Boolean> cir) {
        NoRender module = NoRender.INSTANCE;
        if (module != null && module.isFireEntity()) {
            cir.setReturnValue(false);
        }
    }
}
