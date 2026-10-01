package com.ciallo.module.render;

import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.util.MC;
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
 */
public class FreeLook extends Module {
    public static FreeLook INSTANCE;

    private static final float SENSITIVITY = 0.15f;

    private float fakeYaw;
    private float fakePitch;
    private float prevFakeYaw;
    private float prevFakePitch;

    public FreeLook() {
        super("FreeLook", "Look around without turning your body.", Category.RENDER);
        this.setChinese("自由视角");
        this.setChineseDescription("按住时只转动视角，不改变身体朝向");
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
    }

    /** Called at the start of every client tick so the camera can interpolate. */
    public void startTick() {
        this.prevFakeYaw = this.fakeYaw;
        this.prevFakePitch = this.fakePitch;
    }

    /** Mouse delta from {@code MouseHandler}; keeps the fake rotation in sync. */
    public void onMouseDelta(double yawDelta, double pitchDelta) {
        this.fakeYaw += (float) yawDelta * SENSITIVITY;
        this.fakePitch = Mth.clamp(this.fakePitch + (float) pitchDelta * SENSITIVITY, -90.0f, 90.0f);
    }

    public float getFakeYaw(float partialTick) {
        return Mth.lerp(partialTick, this.prevFakeYaw, this.fakeYaw);
    }

    public float getFakePitch(float partialTick) {
        return Mth.lerp(partialTick, this.prevFakePitch, this.fakePitch);
    }
}
