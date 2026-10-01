package com.ciallo.mixin;

import com.ciallo.module.render.ParrotPet;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ParrotPet: after a player avatar has been submitted, adds the configured shoulder parrots.
 */
@Mixin(LivingEntityRenderer.class)
public class MixinParrotPet {

    @Unique
    private ParrotModel hudbro$parrotModel;

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("TAIL")
    )
    private void hudbro$renderParrotPet(LivingEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        ParrotPet module = ParrotPet.INSTANCE;
        if (module == null || !module.isEnabled()) {
            return;
        }
        if (!(state instanceof AvatarRenderState avatar)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || avatar.id != minecraft.player.getId()) {
            return;
        }
        if (this.hudbro$parrotModel == null) {
            this.hudbro$parrotModel = new ParrotModel(minecraft.getEntityModels().bakeLayer(ModelLayers.PARROT));
        }
        module.renderShoulders(poseStack, collector, avatar, this.hudbro$parrotModel);
    }
}
