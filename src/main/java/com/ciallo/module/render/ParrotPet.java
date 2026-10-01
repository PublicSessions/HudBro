package com.ciallo.module.render;

import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.EnumSetting;
import com.ciallo.setting.NumberSetting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.parrot.Parrot;

/**
 * Parrot pet module, ported from the original mod's {@code ParrotPet}.
 * Draws configurable parrots on the local player's shoulders.
 *
 * <p>1.21.11 renders entities from render states, so the parrots are submitted through the
 * {@link SubmitNodeCollector} exactly like vanilla's {@code ParrotOnShoulderLayer} does.</p>
 */
public class ParrotPet extends Module {
    public static ParrotPet INSTANCE;

    private final EnumSetting<Parrot.Variant> variantLeft = this.m28(new EnumSetting<>("VariantLeft", Parrot.Variant.RED_BLUE));
    private final EnumSetting<Parrot.Variant> variantRight = this.m28(new EnumSetting<>("VariantRight", Parrot.Variant.BLUE));
    private final BooleanSetting left = this.m28(new BooleanSetting("Left", true));
    private final BooleanSetting right = this.m28(new BooleanSetting("Right", true));
    private final NumberSetting scale = this.m28(new NumberSetting("Scale", 0.25, 0.1, 1.0, 0.01));

    public ParrotPet() {
        super("ParrotPet", "Shows parrots on your shoulders.", Category.RENDER);
        this.setChinese("鹦鹉宠物");
        this.setChineseDescription("在自己肩膀上渲染可配置的鹦鹉");
        INSTANCE = this;
    }

    /** Submits both shoulder parrots using the vanilla shoulder transforms. */
    public void renderShoulders(PoseStack poseStack, SubmitNodeCollector collector, AvatarRenderState avatar, ParrotModel model) {
        float s = this.scale.getFloat();
        if (this.right.getValue()) {
            submitShoulder(poseStack, collector, avatar, model, this.variantRight.getValue(), false, s);
        }
        if (this.left.getValue()) {
            submitShoulder(poseStack, collector, avatar, model, this.variantLeft.getValue(), true, s);
        }
    }

    private void submitShoulder(PoseStack poseStack, SubmitNodeCollector collector, AvatarRenderState avatar, ParrotModel model,
                                Parrot.Variant variant, boolean leftShoulder, float scale) {
        poseStack.pushPose();
        poseStack.translate(leftShoulder ? 0.4f : -0.4f, avatar.isCrouching ? -1.3f : -1.5f, 0.0f);
        poseStack.scale(scale, scale, scale);

        ParrotRenderState state = new ParrotRenderState();
        state.pose = ParrotModel.Pose.ON_SHOULDER;
        state.ageInTicks = avatar.ageInTicks;
        state.walkAnimationPos = avatar.walkAnimationPos;
        state.walkAnimationSpeed = avatar.walkAnimationSpeed;
        state.yRot = avatar.yRot;
        state.xRot = avatar.xRot;

        collector.submitModel(
                model,
                state,
                poseStack,
                model.renderType(ParrotRenderer.getVariantTexture(variant)),
                avatar.lightCoords,
                OverlayTexture.NO_OVERLAY,
                avatar.outlineColor,
                null
        );
        poseStack.popPose();
    }
}
