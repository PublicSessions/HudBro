package com.ciallo.module.render;

/**
 * Implemented by {@link net.minecraft.world.entity.Entity} through the HudBro mixin, so the free look
 * camera rotation can be stored on the entity itself.
 *
 * <p>The player's own rotation is never changed while free looking, so the camera rotation has to live
 * somewhere else. Keeping it on the entity means it survives re-rendering, world changes and module
 * re-instantiation.</p>
 */
public interface CameraOverriddenEntity {
    float freelook$getCameraPitch();

    float freelook$getCameraYaw();

    void freelook$setCameraPitch(float pitch);

    /** Sets the camera yaw and re-anchors the head yaw limit to it. */
    void freelook$setCameraYaw(float yaw);
}