package com.ciallo.mixin;

import com.ciallo.module.render.ViewModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ViewModel: applies the configured transform to the first person hand item, and can skip the
 * item swap raise animation.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRenderer {

    @Shadow
    private ItemStack mainHandItem;
    @Shadow
    private ItemStack offHandItem;
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float oMainHandHeight;
    @Shadow
    private float offHandHeight;
    @Shadow
    private float oOffHandHeight;

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
    private void hudbro$swapAnimation(float partialTicks, PoseStack poseStack, SubmitNodeCollector collector, LocalPlayer player, int light, CallbackInfo ci) {
        ViewModel module = ViewModel.INSTANCE;
        if (module == null || !module.isEnabled()) {
            return;
        }
        if (!module.isMainhandSwap()) {
            this.mainHandHeight = 1.0f;
            this.oMainHandHeight = 1.0f;
            this.mainHandItem = player.getMainHandItem();
        }
        if (!module.isOffhandSwap()) {
            this.offHandHeight = 1.0f;
            this.oOffHandHeight = 1.0f;
            this.offHandItem = player.getOffhandItem();
        }
    }

    @Inject(
            method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
            at = @At("HEAD")
    )
    private void hudbro$viewModelTransform(LivingEntity entity, ItemStack stack, ItemDisplayContext context, PoseStack poseStack, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        ViewModel module = ViewModel.INSTANCE;
        if (module == null || !module.isEnabled()) {
            return;
        }
        if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            poseStack.translate(module.getPositionMainX(), module.getPositionMainY(), module.getPositionMainZ());
            poseStack.scale(module.getScaleMainX(), module.getScaleMainY(), module.getScaleMainZ());
            poseStack.mulPose(Axis.XP.rotationDegrees(module.getRotationMainX()));
            poseStack.mulPose(Axis.YP.rotationDegrees(module.getRotationMainY()));
            poseStack.mulPose(Axis.ZP.rotationDegrees(module.getRotationMainZ()));
        } else if (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            poseStack.translate(module.getPositionOffX(), module.getPositionOffY(), module.getPositionOffZ());
            poseStack.scale(module.getScaleOffX(), module.getScaleOffY(), module.getScaleOffZ());
            poseStack.mulPose(Axis.XP.rotationDegrees(module.getRotationOffX()));
            poseStack.mulPose(Axis.YP.rotationDegrees(module.getRotationOffY()));
            poseStack.mulPose(Axis.ZP.rotationDegrees(module.getRotationOffZ()));
        }
    }
}
