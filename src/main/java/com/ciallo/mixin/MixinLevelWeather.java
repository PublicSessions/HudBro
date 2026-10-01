package com.ciallo.mixin;

import com.ciallo.module.render.NoRender;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NoRender: removes rain / snow / thunder (the weather renderer reads these values).
 */
@Mixin(Level.class)
public abstract class MixinLevelWeather {

    /** Only the client side should have its weather hidden, so the integrated server is untouched. */
    private boolean hudbro$shouldHideWeather() {
        NoRender module = NoRender.INSTANCE;
        return module != null && module.isWeather() && ((Level) (Object) this).isClientSide();
    }

    @Inject(method = "getRainLevel", at = @At("RETURN"), cancellable = true)
    private void hudbro$noRain(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (hudbro$shouldHideWeather()) {
            cir.setReturnValue(0.0f);
        }
    }

    @Inject(method = "getThunderLevel", at = @At("RETURN"), cancellable = true)
    private void hudbro$noThunder(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (hudbro$shouldHideWeather()) {
            cir.setReturnValue(0.0f);
        }
    }
}
