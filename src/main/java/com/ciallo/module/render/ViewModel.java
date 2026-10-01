package com.ciallo.module.render;

import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.NumberSetting;

/**
 * View model module, ported from the original mod's {@code ViewModel}.
 * Moves / rotates / scales the first person hand item and can slow the swing animation.
 */
public class ViewModel extends Module {
    public static ViewModel INSTANCE;

    private final BooleanSetting mainhandSwap = this.m28(new BooleanSetting("MainhandSwap", true));
    private final BooleanSetting offhandSwap = this.m28(new BooleanSetting("OffhandSwap", true));

    private final NumberSetting scaleMainX = this.m28(new NumberSetting("ScaleMainX", 1.0, 0.1, 5.0, 0.01));
    private final NumberSetting scaleMainY = this.m28(new NumberSetting("ScaleMainY", 1.0, 0.1, 5.0, 0.01));
    private final NumberSetting scaleMainZ = this.m28(new NumberSetting("ScaleMainZ", 1.0, 0.1, 5.0, 0.01));
    private final NumberSetting positionMainX = this.m28(new NumberSetting("PositionMainX", 0.0, -3.0, 3.0, 0.01));
    private final NumberSetting positionMainY = this.m28(new NumberSetting("PositionMainY", 0.0, -3.0, 3.0, 0.01));
    private final NumberSetting positionMainZ = this.m28(new NumberSetting("PositionMainZ", 0.0, -3.0, 3.0, 0.01));
    private final NumberSetting rotationMainX = this.m28(new NumberSetting("RotationMainX", 0.0, -180.0, 180.0, 0.01));
    private final NumberSetting rotationMainY = this.m28(new NumberSetting("RotationMainY", 0.0, -180.0, 180.0, 0.01));
    private final NumberSetting rotationMainZ = this.m28(new NumberSetting("RotationMainZ", 0.0, -180.0, 180.0, 0.01));

    private final NumberSetting scaleOffX = this.m28(new NumberSetting("ScaleOffX", 1.0, 0.1, 5.0, 0.01));
    private final NumberSetting scaleOffY = this.m28(new NumberSetting("ScaleOffY", 1.0, 0.1, 5.0, 0.01));
    private final NumberSetting scaleOffZ = this.m28(new NumberSetting("ScaleOffZ", 1.0, 0.1, 5.0, 0.01));
    private final NumberSetting positionOffX = this.m28(new NumberSetting("PositionOffX", 0.0, -3.0, 3.0, 0.01));
    private final NumberSetting positionOffY = this.m28(new NumberSetting("PositionOffY", 0.0, -3.0, 3.0, 0.01));
    private final NumberSetting positionOffZ = this.m28(new NumberSetting("PositionOffZ", 0.0, -3.0, 3.0, 0.01));
    private final NumberSetting rotationOffX = this.m28(new NumberSetting("RotationOffX", 0.0, -180.0, 180.0, 0.01));
    private final NumberSetting rotationOffY = this.m28(new NumberSetting("RotationOffY", 0.0, -180.0, 180.0, 0.01));
    private final NumberSetting rotationOffZ = this.m28(new NumberSetting("RotationOffZ", 0.0, -180.0, 180.0, 0.01));

    private final BooleanSetting slowAnimation = this.m28(new BooleanSetting("SwingSpeed", true));
    private final NumberSetting slowAnimationVal = this.m28(new NumberSetting("SwingValue", 6.0, 1.0, 50.0, 1.0));

    public ViewModel() {
        super("ViewModel", "Custom first person hand item transform.", Category.RENDER);
        this.setChinese("手持模型");
        this.setChineseDescription("调整第一人称手持物品的位置/旋转/缩放与挥动速度");
        INSTANCE = this;
    }

    public boolean isMainhandSwap() {
        return mainhandSwap.getValue();
    }

    public boolean isOffhandSwap() {
        return offhandSwap.getValue();
    }

    public boolean isSwingSpeed() {
        return slowAnimation.getValue();
    }

    /** 6 = vanilla speed, larger = slower swing. */
    public float getSwingValue() {
        return slowAnimationVal.getFloat();
    }

    public float getScaleMainX() {
        return scaleMainX.getFloat();
    }

    public float getScaleMainY() {
        return scaleMainY.getFloat();
    }

    public float getScaleMainZ() {
        return scaleMainZ.getFloat();
    }

    public float getPositionMainX() {
        return positionMainX.getFloat();
    }

    public float getPositionMainY() {
        return positionMainY.getFloat();
    }

    public float getPositionMainZ() {
        return positionMainZ.getFloat();
    }

    public float getRotationMainX() {
        return rotationMainX.getFloat();
    }

    public float getRotationMainY() {
        return rotationMainY.getFloat();
    }

    public float getRotationMainZ() {
        return rotationMainZ.getFloat();
    }

    public float getScaleOffX() {
        return scaleOffX.getFloat();
    }

    public float getScaleOffY() {
        return scaleOffY.getFloat();
    }

    public float getScaleOffZ() {
        return scaleOffZ.getFloat();
    }

    public float getPositionOffX() {
        return positionOffX.getFloat();
    }

    public float getPositionOffY() {
        return positionOffY.getFloat();
    }

    public float getPositionOffZ() {
        return positionOffZ.getFloat();
    }

    public float getRotationOffX() {
        return rotationOffX.getFloat();
    }

    public float getRotationOffY() {
        return rotationOffY.getFloat();
    }

    public float getRotationOffZ() {
        return rotationOffZ.getFloat();
    }
}
