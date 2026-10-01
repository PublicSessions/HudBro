package com.ciallo.mixin;

import com.ciallo.module.render.NoRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NoRender: hides GUI toasts (advancements, recipes, system messages).
 */
@Mixin(ToastManager.class)
public class MixinToastManager {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void hudbro$noToasts(GuiGraphics context, CallbackInfo ci) {
        NoRender module = NoRender.INSTANCE;
        if (module != null && module.isGuiToast()) {
            ci.cancel();
        }
    }
}
