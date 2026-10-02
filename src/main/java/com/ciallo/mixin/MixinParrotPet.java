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
 *
 * <p>The parrots are submitted just before {@code LivingEntityRenderer} pops its pose, not at the end
 * of the method. That pose is the flipped model space ({@code scale(-1, -1, 1)} plus the
 * {@code translate(0, -1.501, 0)} model offset) that vanilla render layers such as
 * {@code ParrotOnShoulderLayer} are given, and the shoulder offsets in {@link ParrotPet} are written in
 * that space. Running after the pop would render the parrots un-flipped and one and a half blocks too
 * low, which looks like an upside down parrot at the player's feet.</p>
 */
@Mixin(LivingEntityRenderer.class)
public class MixinParrotPet {

    @Unique
    private ParrotModel hudbro$parrotModel;

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V")
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
