package com.ciallo.mixin;

import com.ciallo.module.render.FreeLook;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FreeLook: rotates the camera to the fake rotation after vanilla has set it up, and animates the
 * slide between the first and third person perspective.
 */
@Mixin(Camera.class)
public abstract class MixinCameraFreeLook {

    @Shadow
    protected abstract void setRotation(float pitch, float yaw);

    @Inject(method = "setup", at = @At("TAIL"))
    private void hudbro$freeLookCamera(Level level, Entity entity, boolean thirdPerson, boolean inverseView, float partialTick, CallbackInfo ci) {
        FreeLook module = FreeLook.INSTANCE;
        if (module == null || !module.isEnabled()) {
            return;
        }
        if (entity != Minecraft.getInstance().player) {
            return;
        }
        this.setRotation(module.getFakePitch(partialTick), module.getFakeYaw(partialTick));
    }

    /**
     * Scales the distance {@code Camera#move} steps backwards by the perspective animation factor.
     *
     * <p>{@code setup} already switched to third person when the module was enabled, so the camera sits
     * at its normal third person position. Multiplying the distance slides it out of the player's head
     * (factor 0) to that position (factor 1) and back, instead of cutting between the two.</p>
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