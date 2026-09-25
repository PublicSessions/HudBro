package com.ciallo.mixin;

import com.ciallo.module.render.Chams;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndCrystalEntityRenderer.class)
public class EndCrystalRendererMixin {
    @Inject(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void onRender(EndCrystalEntity crystal, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (Chams.INSTANCE != null && Chams.INSTANCE.isEnabled() && Chams.INSTANCE.crystal.getValue()) {
            Chams.INSTANCE.incrementAge();
            
            float spin = Chams.INSTANCE.spinSync.getValue() ? Chams.INSTANCE.getAge() * Chams.INSTANCE.spinSpeed.getFloat() : crystal.age * Chams.INSTANCE.spinSpeed.getFloat();
            float bounce = (float) Math.sin(Chams.INSTANCE.getAge() * Chams.INSTANCE.bounceSpeed.getFloat()) * Chams.INSTANCE.bounceHeight.getFloat() + Chams.INSTANCE.yOffset.getFloat();
            float scale = Chams.INSTANCE.scale.getFloat();
            
            matrices.push();
            matrices.translate(0, bounce, 0);
            matrices.scale(scale, scale, scale);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin));
        }
    }

    @Inject(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    private void onRenderPost(EndCrystalEntity crystal, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (Chams.INSTANCE != null && Chams.INSTANCE.isEnabled() && Chams.INSTANCE.crystal.getValue()) {
            matrices.pop();
        }
    }
}