package com.ciallo.mixin;

import com.ciallo.config.GlobalConfig;
import com.ciallo.gui.UiTheme;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets the HudBro "Blur" slider control the menu background blur while a HudBro screen is open,
 * without touching the vanilla accessibility option.
 */
@Mixin(Options.class)
public class MixinOptions {

    @Inject(method = "getMenuBackgroundBlurriness", at = @At("RETURN"), cancellable = true)
    private void hudbro$overrideBlur(CallbackInfoReturnable<Integer> cir) {
        if (UiTheme.isHudBroScreenOpen()) {
            cir.setReturnValue(GlobalConfig.blur());
        }
    }
}
