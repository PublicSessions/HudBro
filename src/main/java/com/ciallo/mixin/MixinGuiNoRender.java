package com.ciallo.mixin;

import com.ciallo.module.render.NoRender;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NoRender: removes the nether portal overlay, the nausea (confusion) overlay and the
 * mob effect icons from the HUD.
 */
@Mixin(Gui.class)
public class MixinGuiNoRender {

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void hudbro$noPortalOverlay(GuiGraphics context, float alpha, CallbackInfo ci) {
        NoRender module = NoRender.INSTANCE;
        if (module != null && module.isPortal()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderConfusionOverlay", at = @At("HEAD"), cancellable = true)
    private void hudbro$noConfusionOverlay(GuiGraphics context, float alpha, CallbackInfo ci) {
        NoRender module = NoRender.INSTANCE;
        if (module != null && module.isNausea()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void hudbro$noEffectIcons(GuiGraphics context, DeltaTracker deltaTracker, CallbackInfo ci) {
        NoRender module = NoRender.INSTANCE;
        if (module != null && module.isPotionsIcon()) {
            ci.cancel();
        }
    }
}
