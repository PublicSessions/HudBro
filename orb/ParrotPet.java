package dev.lumn.mod.modules.impl.render;

import dev.lumn.mod.modules.Module;
import dev.lumn.mod.modules.settings.impl.BooleanSetting;
import dev.lumn.mod.modules.settings.impl.EnumSetting;
import dev.lumn.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ParrotEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ParrotPet extends Module {

    public static ParrotPet INSTANCE;

    private ParrotEntityModel parrotModel;

    public enum Variant {
        RedBlue,
        Blue,
        Green,
        YellowBlue,
        Gray
    }

    private final EnumSetting<Variant> variantLeft = this.add(new EnumSetting<>("VariantLeft", Variant.RedBlue));
    private final EnumSetting<Variant> variantRight = this.add(new EnumSetting<>("VariantRight", Variant.Blue));
    private final BooleanSetting left = this.add(new BooleanSetting("Left", true));
    private final BooleanSetting right = this.add(new BooleanSetting("Right", true));
    private final SliderSetting scaleSetting = this.add(new SliderSetting("Scale", 0.25, 0.1, 1.0, 0.01));

    public ParrotPet() {
        super("ParrotPet", Category.Render);
        this.setChinese("鹦鹉宠物");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (ParrotPet.nullCheck()) {
            return;
        }
        if (parrotModel == null) {
            ModelPart root = mc.getEntityModelLoader().getModelPart(EntityModelLayers.PARROT);
            parrotModel = new ParrotEntityModel(root);
        }
    }

    public void renderInPlayerContext(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            PlayerEntity player, float limbAngle, float limbDistance,
            float headYaw, float headPitch) {
        if (!this.isOn() || player != mc.player) {
            return;
        }
        if (parrotModel == null) {
            if (ParrotPet.nullCheck()) {
                return;
            }
            ModelPart root = mc.getEntityModelLoader().getModelPart(EntityModelLayers.PARROT);
            parrotModel = new ParrotEntityModel(root);
        }

        float s = scaleSetting.getValueFloat();

        if (right.getValue()) {
            renderShoulder(matrices, vertexConsumers, light, player,
                    limbAngle, limbDistance, headYaw, headPitch, false,
                    getParrotVariant(variantRight.getValue()), s);
        }
        if (left.getValue()) {
            renderShoulder(matrices, vertexConsumers, light, player,
                    limbAngle, limbDistance, headYaw, headPitch, true,
                    getParrotVariant(variantLeft.getValue()), s);
        }
    }

    private void renderShoulder(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            PlayerEntity player, float limbAngle, float limbDistance,
            float headYaw, float headPitch, boolean leftShoulder,
            ParrotEntity.Variant parrotVariant, float scale) {
        matrices.push();

        matrices.translate(
                leftShoulder ? 0.4f : -0.4f,
                player.isInSneakingPose() ? -1.3f : -1.5f,
                0
        );

        matrices.scale(scale, scale, scale);

        VertexConsumer vc = vertexConsumers.getBuffer(
                parrotModel.getLayer(ParrotEntityRenderer.getTexture(parrotVariant))
        );

        parrotModel.poseOnShoulder(matrices, vc, light, OverlayTexture.DEFAULT_UV,
                limbAngle, limbDistance, headYaw, headPitch, player.age);

        matrices.pop();
    }

    private ParrotEntity.Variant getParrotVariant(Variant variant) {
        return switch (variant) {
            case Blue ->
                ParrotEntity.Variant.BLUE;
            case Green ->
                ParrotEntity.Variant.GREEN;
            case YellowBlue ->
                ParrotEntity.Variant.YELLOW_BLUE;
            case Gray ->
                ParrotEntity.Variant.GRAY;
            default ->
                ParrotEntity.Variant.RED_BLUE;
        };
    }
}
