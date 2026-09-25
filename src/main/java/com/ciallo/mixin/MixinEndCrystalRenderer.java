package com.ciallo.mixin;

import com.ciallo.module.render.Chams;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.object.crystal.EndCrystalModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Chams: re-renders the end crystal with a custom tint / scale / spin and alpha.
 *
 * <p>Vanilla's {@code EndCrystalRenderer#submit} only does
 * {@code push -> scale(2) -> translate(0,-0.5,0) -> submitModel -> pop} plus the beam; all of the
 * spinning / bouncing happens inside {@code EndCrystalModel#setupAnim(renderState)} which
 * {@code submitModel} calls for us. This port therefore only changes the render state values, the
 * outer pose and the tint colour instead of re-implementing the model transforms.</p>
 */
@Mixin(EndCrystalRenderer.class)
public abstract class MixinEndCrystalRenderer {

    @Shadow
    @Final
    private static Identifier END_CRYSTAL_LOCATION;

    @Shadow
    @Final
    private EndCrystalModel model;

    @Shadow
    @Final
    private static RenderType RENDER_TYPE;

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hudbro$customCrystal(EndCrystalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        Chams module = Chams.INSTANCE;
        if (module == null || !module.customCrystal()) {
            return;
        }
        ci.cancel();

        // SpinSync uses the module's own tick counter instead of the crystal's age.
        if (module.isSpinSync()) {
            state.ageInTicks = module.age;
        }
        // SpinSpeed scales the animation age (spin and bounce speed together).
        state.ageInTicks *= module.getSpinValue();

        float scale = module.getScale();
        int color = module.getCrystalColor();

        // Per layer switches. skipDraw only skips that part's own cuboids, so the children of a
        // hidden layer still render and the three switches stay independent of each other.
        this.model.outerGlass.skipDraw = !module.isOuterFrame();
        this.model.innerGlass.skipDraw = !module.isInnerFrame();
        this.model.cube.skipDraw = !module.isCore();

        // Keep the vanilla cutout render type unless the tint is transparent, so the default look
        // stays exactly the same and translucent sorting is only used when alpha is wanted.
        RenderType renderType = (color >>> 24) >= 255
                ? RENDER_TYPE
                : RenderTypes.entityTranslucent(END_CRYSTAL_LOCATION);

        poseStack.pushPose();
        poseStack.translate(0.0f, module.getFloatOffset(), 0.0f);
        poseStack.scale(2.0f * scale, 2.0f * scale, 2.0f * scale);
        poseStack.translate(0.0f, -0.5f, 0.0f);
        collector.submitModel(
                this.model,
                state,
                poseStack,
                renderType,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                color,
                null,
                state.outlineColor,
                null
        );
        poseStack.popPose();

        // Same beam handling as vanilla.
        Vec3 beam = state.beamOffset;
        if (beam != null) {
            float y = EndCrystalRenderer.getY(state.ageInTicks);
            float beamX = (float) beam.x;
            float beamY = (float) beam.y;
            float beamZ = (float) beam.z;
            poseStack.translate(beam);
            EnderDragonRenderer.submitCrystalBeams(-beamX, -beamY + y, -beamZ, state.ageInTicks, poseStack, collector, state.lightCoords);
        }
    }
}
