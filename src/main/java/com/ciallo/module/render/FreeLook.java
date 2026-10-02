package com.ciallo.module.render;

import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.util.MC;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;

/**
 * Free look module, ported from the original mod's {@code FreeLook}.
 *
 * <p>While enabled the mouse turns a separate "fake" rotation instead of the player, and the camera
 * is rotated to that fake rotation afterwards, so you can look around without turning your body.
 * Implemented with a redirect on {@code MouseHandler#turnPlayer} plus a hook on {@code Camera#setup}
 * (the original posted a {@code LookDirectionEvent} from code that is not part of this port).</p>
 *
 * <p>Note: the crosshair target still follows the camera rotation, since 1.21.11 computes it from
 * the camera.</p>
 *
 * <p>The mouse deltas are taken from {@code LocalPlayer#turn}, which {@code MouseHandler} has already
 * scaled by the sensitivity option and corrected for the invert-mouse options, so they are forwarded
 * unscaled.</p>
 *
 * <p>Enabling switches the perspective to third person, because in first person the free rotation
 * only spins the view with nothing to look at. Vanilla cuts between perspectives instantly, so the
 * switch is animated here instead: {@link #transitionFactor()} runs from 0 to 1 while enabling and
 * from 1 to 0 while disabling, and the camera mixin scales the third person dolly by it, which slides
 * the camera out of the player's head and back in.</p>
 */
public class FreeLook extends Module {
    public static FreeLook INSTANCE;

    /** Ticks the camera takes to slide between first and third person, about a quarter second. */
    private static final int TRANSITION_TICKS = 5;

    private float fakeYaw;
    private float fakePitch;
    private float prevFakeYaw;
    private float prevFakePitch;
    private CameraType previousCameraType;
    private boolean perspectiveChanged;
    private boolean restoringPerspective;
    private int transitionTicks;
    private float startFactor;

    public FreeLook() {
        super("FreeLook", "Look around without turning your body.", Category.RENDER);
        this.setChinese("自由视角");
        this.setChineseDescription("只转动视角，不改变身体朝向，开启时自动切换第三人称");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (MC.getMc().player == null) {
            return;
        }
        this.fakeYaw = MC.getMc().player.getYRot();
        this.fakePitch = MC.getMc().player.getXRot();
        this.prevFakeYaw = this.fakeYaw;
        this.prevFakePitch = this.fakePitch;

        this.previousCameraType = MC.getMc().options.getCameraType();
        this.perspectiveChanged = this.previousCameraType.isFirstPerson();
        this.restoringPerspective = false;
        // Start from the player's head, so the camera slides out instead of jumping.
        this.startFactor = 0.0f;
        this.transitionTicks = this.perspectiveChanged ? TRANSITION_TICKS : 0;
        if (this.perspectiveChanged) {
            MC.getMc().options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
    }

    @Override
    public void onDisable() {
        // Drop the fake rotation, so re-enabling starts from the direction the body is facing.
        this.fakeYaw = 0.0f;
        this.fakePitch = 0.0f;
        this.prevFakeYaw = 0.0f;
        this.prevFakePitch = 0.0f;

        // Slide back into the player's head first, then hand the perspective back, so the animation is
        // not cut off half way. Pick up wherever the outgoing animation had got to, so toggling quickly
        // does not jump.
        this.startFactor = this.transitionFactor();
        this.restoringPerspective = this.perspectiveChanged;
        this.transitionTicks = this.perspectiveChanged ? TRANSITION_TICKS : 0;
    }

    /**
     * Called at the start of every client tick: captures the previous fake rotation so the camera can
     * interpolate, and advances the perspective animation. Runs whether or not the module is enabled,
     * because the animation continues after the module was switched off.
     */
    public void startTick() {
        this.prevFakeYaw = this.fakeYaw;
        this.prevFakePitch = this.fakePitch;
        if (this.transitionTicks > 0) {
            this.transitionTicks--;
            if (this.transitionTicks == 0 && this.restoringPerspective) {
                this.restoringPerspective = false;
                if (this.previousCameraType != null) {
                    MC.getMc().options.setCameraType(this.previousCameraType);
                }
                this.perspectiveChanged = false;
            }
        }
    }

    /**
     * Mouse delta taken from {@code MouseHandler}, already scaled by the mouse sensitivity option and
     * already corrected for the invert-mouse options, so it is used as it is. Applying a sensitivity
     * factor again here would make the free look speed disagree with the player's own look speed.
     */
    public void onMouseDelta(double yawDelta, double pitchDelta) {
        this.fakeYaw += (float) yawDelta;
        this.fakePitch = Mth.clamp(this.fakePitch + (float) pitchDelta, -90.0f, 90.0f);
    }

    public float getFakeYaw(float partialTick) {
        // rotLerp, so crossing the +/-180 degree seam does not spin the camera the long way round.
        return Mth.rotLerp(partialTick, this.prevFakeYaw, this.fakeYaw);
    }

    public float getFakePitch(float partialTick) {
        return Mth.lerp(partialTick, this.prevFakePitch, this.fakePitch);
    }

    /** How far along the perspective animation is, 0 at the head and 1 at the third person distance. */
    public float transitionFactor() {
        if (this.transitionTicks <= 0) {
            return 1.0f;
        }
        float progress = (float) (TRANSITION_TICKS - this.transitionTicks) / TRANSITION_TICKS;
        // Ease in and out, so the camera does not start and stop abruptly.
        float eased = progress * progress * (3.0f - 2.0f * progress);
        // Sliding out ends at the third person distance, sliding back in ends at the head.
        float target = this.restoringPerspective ? 0.0f : 1.0f;
        return Mth.lerp(eased, this.startFactor, target);
    }

    /** Whether the perspective animation is still running. */
    public boolean isTransitioning() {
        return this.transitionTicks > 0;
    }
}