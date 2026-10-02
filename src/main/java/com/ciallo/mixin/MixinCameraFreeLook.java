package com.ciallo.mixin;

import com.ciallo.module.render.CameraOverriddenEntity;
import com.ciallo.module.render.FreeLook;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FreeLook: points the camera at the free look rotation instead of the body's, and animates the slide
 * between the first and third person perspective.
 */
@Mixin(Camera.class)
public abstract class MixinCameraFreeLook {

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    /**
     * Overrides the camera rotation with the free look rotation.
     *
     * <p>This has to happen right after {@code Camera#setup} reads the body's rotation, and not at the
     * end of the method. {@code setup} positions the camera by calling {@code move}, and {@code move}
     * works off the rotation quaternion that {@code setRotation} has just built. Overriding the rotation
     * later would look the right way but leave the camera sitting behind the direction the body faces,
     * so the player would drift away from the centre of the screen.</p>
     *
     * <p>Ordinal 1 is the {@code setRotation(getViewYRot, getViewXRot)} call on the normal path. The
     * call before it (ordinal 0) belongs to the minecart position/rotation interpolation branch, and the
     * one after it flips the rotation for the front facing third person view.</p>
     *
     * <p>Note the argument order: the yaw comes first. The method reads as
     * {@code setRotation(float yaw, float pitch)}.</p>
     */
    @Inject(
            method = "setup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setRotation(FF)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void hudbro$freeLookCamera(Level level, Entity entity, boolean thirdPerson, boolean inverseView, float partialTick, CallbackInfo ci) {
        FreeLook module = FreeLook.INSTANCE;
        if (module == null || !module.isFreeLooking()) {
            return;
        }
        if (!(entity instanceof LocalPlayer) || !(entity instanceof CameraOverriddenEntity camera)) {
            return;
        }
        this.setRotation(camera.freelook$getCameraYaw(), camera.freelook$getCameraPitch());
    }

    /**
     * Scales the distance {@code Camera#move} steps backwards by the perspective animation factor.
     *
     * <p>The perspective has already been switched to third person when free looking started, so the
     * camera sits at its normal third person position. Multiplying the distance slides it out of the
     * player's head (factor 0) to that position (factor 1) and back, instead of cutting between the two.
     * Vanilla has no transition of its own here: {@code Camera} is only told whether it is detached by a
     * boolean and never sees the {@code CameraType}.</p>
     */
    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float hudbro$freeLookDistance(float distance, float y, float z) {
        FreeLook module = FreeLook.INSTANCE;
        if (module == null || !module.isTransitioning()) {
            return distance;
        }
        return distance * module.transitionFactor();
    }
}