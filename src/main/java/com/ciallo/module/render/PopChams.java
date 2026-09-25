package com.ciallo.module.render;

import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.EnumSetting;
import com.ciallo.setting.NumberSetting;
import com.ciallo.util.Animation;
import com.ciallo.util.Easing;
import com.ciallo.util.MC;
import com.ciallo.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * PopChams module, ported from the original mod's {@code PopChams}.
 * Renders a fading copy of a player at the position where their totem popped.
 *
 * <p>The original implementation used the immediate-mode rendering API which no longer exists in
 * 1.21.11; this port builds the copy from the vanilla render state and submits it through
 * {@link SubmitNodeCollector#submitCustomGeometry}.</p>
 */
public class PopChams extends Module {
    public static PopChams INSTANCE;

    private final EnumSetting<Easing> ease = this.m28(new EnumSetting<>("Ease", Easing.CubicInOut));
    private final ColorSetting fill = this.m28(new ColorSetting("Fill", 0x64FFFFFF));
    private final ColorSetting line = this.m28(new ColorSetting("Line", 0x64FFFFFF));
    private final BooleanSetting alpha = this.m28(new BooleanSetting("Alpha", true));
    // Default off so the local player can see their own totem pop as well.
    private final BooleanSetting noSelf = this.m28(new BooleanSetting("NoSelf", false));
    private final NumberSetting fadeTime = this.m28(new NumberSetting("FadeTime", 300.0, 0.0, 1000.0, 1.0));
    private final NumberSetting yOffset = this.m28(new NumberSetting("YOffset", 0.0, -10.0, 10.0, 0.01));
    private final NumberSetting scale = this.m28(new NumberSetting("Scale", 1.0, 0.0, 2.0, 0.01));
    private final NumberSetting yaw = this.m28(new NumberSetting("Yaw", 0.0, 0.0, 720.0, 0.01));

    private final List<Person> popList = new CopyOnWriteArrayList<>();

    public PopChams() {
        super("PopChams", "Shows a fading copy of a player when their totem pops.", Category.RENDER);
        this.setChinese("爆图腾上色");
        this.setChineseDescription("图腾触发时在原地渲染一个淡出的玩家模型");
        INSTANCE = this;
    }

    /** Called every client tick, used to drop entries when the module is off. */
    public void onTick() {
        if (!this.isEnabled()) {
            this.popList.clear();
        }
    }

    public void onTotemPop(Player player) {
        if (!this.isEnabled() || player == null) {
            return;
        }
        if (this.noSelf.getValue() && player == MC.getMc().player) {
            return;
        }
        if (!(player instanceof AbstractClientPlayer clientPlayer)) {
            return;
        }
        EntityRenderDispatcher dispatcher = MC.getMc().getEntityRenderDispatcher();
        EntityRenderState extracted = dispatcher.extractEntity(clientPlayer, 1.0f);
        if (!(extracted instanceof AvatarRenderState renderState)) {
            return;
        }
        AvatarRenderer<AbstractClientPlayer> renderer = dispatcher.getPlayerRenderer(clientPlayer);
        if (renderer == null) {
            return;
        }
        Identifier texture = renderer.getTextureLocation(renderState);
        this.popList.add(new Person(renderState, renderer, texture, player.getX(), player.getY(), player.getZ(), extracted.lightCoords));
    }

    /** Called from the level renderer mixin after entity submission. */
    public void render(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (!this.isEnabled()) {
            this.popList.clear();
            return;
        }
        if (this.popList.isEmpty()) {
            return;
        }
        double cameraX = MC.getMc().gameRenderer.getMainCamera().position().x;
        double cameraY = MC.getMc().gameRenderer.getMainCamera().position().y;
        double cameraZ = MC.getMc().gameRenderer.getMainCamera().position().z;
        this.popList.removeIf(person -> person.render(poseStack, collector, this, cameraX, cameraY, cameraZ));
    }

    /** Simple holder so the module does not need to name the concrete player model class. */
    private static class Person {
        private final AvatarRenderState state;
        private final AvatarRenderer<AbstractClientPlayer> renderer;
        private final Identifier texture;
        private final Animation animation = new Animation();
        private final double x;
        private final double y;
        private final double z;
        private final int light;

        private Person(AvatarRenderState state, AvatarRenderer<AbstractClientPlayer> renderer, Identifier texture, double x, double y, double z, int light) {
            this.state = state;
            this.renderer = renderer;
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.z = z;
            this.light = light;
        }

        private boolean render(PoseStack poseStack, SubmitNodeCollector collector, PopChams module, double cameraX, double cameraY, double cameraZ) {
            double progress = this.animation.get(1.0, (long) module.fadeTime.getInt(), module.ease.getValue());
            if (progress >= 1.0) {
                return true;
            }
            float fade = module.alpha.getValue() ? (float) (1.0 - progress) : 1.0f;
            int fillColor = module.fill.getColor();
            int color = module.applyAlpha(fillColor, fade);
            float yOffset = module.yOffset.getFloat() * (float) progress;
            float scale = 1.0f + (module.scale.getFloat() - 1.0f) * (float) progress;
            float yaw = module.yaw.getFloat() * (float) progress;

            poseStack.pushPose();
            poseStack.translate(this.x - cameraX, this.y + yOffset - cameraY, this.z - cameraZ);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.scale(-scale, -scale, scale);
            poseStack.translate(0.0f, -1.501f, 0.0f);

            RenderType renderType = RenderTypes.entityTranslucent(this.texture);
            collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
                this.renderer.getModel().setupAnim(this.state);
                RenderUtil.renderModel(this.renderer.getModel(), pose, consumer, this.light, color);
            });

            poseStack.popPose();
            return false;
        }
    }

    public int applyAlpha(int color, float alpha) {
        int a = (int) ((color >> 24 & 0xFF) * alpha);
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
