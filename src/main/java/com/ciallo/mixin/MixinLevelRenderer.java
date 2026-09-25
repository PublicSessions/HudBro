package com.ciallo.mixin;

import com.ciallo.module.render.PopChams;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * PopChams: hooks the point where all entity render states have been submitted so the
 * extra totem-pop copies can be pushed into the same submit node collector.
 */
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Inject(method = "submitEntities", at = @At("TAIL"))
    private void hudbro$renderPopChams(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector collector, CallbackInfo ci) {
        PopChams module = PopChams.INSTANCE;
        if (module != null && module.isEnabled()) {
            module.render(poseStack, collector, levelRenderState.cameraRenderState);
        }
    }
}
