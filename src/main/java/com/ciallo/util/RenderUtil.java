package com.ciallo.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Helpers for pushing model geometry into a {@code SubmitNodeCollector}.
 *
 * <p>{@code SubmitNodeCollector.CustomGeometryRenderer} hands out a {@link PoseStack.Pose}
 * snapshot rather than the original {@link PoseStack}, while {@code ModelPart#render} and
 * {@code Model#renderToBuffer} still expect a {@code PoseStack}. These helpers rebuild a temporary
 * stack from the snapshot.</p>
 */
public final class RenderUtil {
    private RenderUtil() {
    }

    /** Builds a one entry PoseStack holding a copy of the given pose. */
    public static PoseStack fromPose(PoseStack.Pose pose) {
        PoseStack stack = new PoseStack();
        stack.last().set(pose);
        return stack;
    }

    /** Renders a single model part with a tint colour through a vertex consumer. */
    public static void renderPart(ModelPart part, PoseStack.Pose pose, VertexConsumer consumer, int light, int color) {
        if (part == null) {
            return;
        }
        part.render(fromPose(pose), consumer, light, OverlayTexture.NO_OVERLAY, color);
    }

    /** Renders a whole model with a tint colour through a vertex consumer. */
    public static void renderModel(Model<?> model, PoseStack.Pose pose, VertexConsumer consumer, int light, int color) {
        if (model == null) {
            return;
        }
        model.renderToBuffer(fromPose(pose), consumer, light, OverlayTexture.NO_OVERLAY, color);
    }
}
