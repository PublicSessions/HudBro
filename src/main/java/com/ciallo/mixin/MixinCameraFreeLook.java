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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FreeLook: rotates the camera to the fake rotation after vanilla has set it up.
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
}
