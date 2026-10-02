package com.ciallo.mixin;

import com.ciallo.module.render.CameraOverriddenEntity;
import com.ciallo.module.render.FreeLook;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FreeLook: while free looking, the rotation the player asks for is diverted into a separate camera
 * rotation instead of turning the body, and {@code turn} is cancelled so the body stays put.
 *
 * <p>The 0.15 factor has to be applied here. {@code MouseHandler#turnPlayer} has already scaled the
 * deltas by the mouse sensitivity option and corrected them for the invert options, but the conversion
 * from that raw value to degrees lives in the body of {@code Entity#turn} itself. Cancelling the method
 * skips it, so without this factor the camera would swing about 6.7 times faster than the player's own
 * look speed.</p>
 */
@Mixin(Entity.class)
public abstract class MixinEntityFreeLook implements CameraOverriddenEntity {
    @Unique
    private float hudbro$cameraPitch;

    @Unique
    private float hudbro$cameraYaw;

    /** Yaw the head is centred on while free looking, used by the head yaw limit. */
    @Unique
    private float hudbro$anchorYaw;

    @Unique
    private boolean hudbro$hasAnchor;

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void hudbro$freeLookTurn(double yawDelta, double pitchDelta, CallbackInfo ci) {
        FreeLook module = FreeLook.INSTANCE;
        if (module != null && module.isFreeLooking() && (Object) this instanceof LocalPlayer) {
            float maxHeadYaw = module.getMaxHeadYaw();
            // Entity#turn scales the raw mouse deltas to degrees before applying them, and cancelling it
            // skips that step. Same factor, so the camera keeps up with the player's own look speed.
            float yawDeltaDegrees = (float) yawDelta * 0.15f;
            float pitchDeltaDegrees = (float) pitchDelta * 0.15f;
            if (!this.hudbro$hasAnchor) {
                // Centre the limit on wherever the head was pointing when free looking started.
                this.hudbro$anchorYaw = this.hudbro$cameraYaw;
                this.hudbro$hasAnchor = true;
            }
            this.hudbro$cameraPitch = Mth.clamp(this.hudbro$cameraPitch + pitchDeltaDegrees, -90.0f, 90.0f);
            if (maxHeadYaw >= 360.0f) {
                this.hudbro$cameraYaw += yawDeltaDegrees;
            } else {
                this.hudbro$cameraYaw = Mth.clamp(this.hudbro$cameraYaw + yawDeltaDegrees,
                        this.hudbro$anchorYaw - maxHeadYaw, this.hudbro$anchorYaw + maxHeadYaw);
            }
            // The body keeps facing where it was facing.
            ci.cancel();
        } else if (this.hudbro$hasAnchor) {
            this.hudbro$hasAnchor = false;
        }
    }

    @Unique
    @Override
    public float freelook$getCameraPitch() {
        return this.hudbro$cameraPitch;
    }

    @Unique
    @Override
    public float freelook$getCameraYaw() {
        return this.hudbro$cameraYaw;
    }

    @Unique
    @Override
    public void freelook$setCameraPitch(float pitch) {
        this.hudbro$cameraPitch = pitch;
    }

    @Unique
    @Override
    public void freelook$setCameraYaw(float yaw) {
        this.hudbro$cameraYaw = yaw;
        this.hudbro$anchorYaw = yaw;
        this.hudbro$hasAnchor = true;
    }
}