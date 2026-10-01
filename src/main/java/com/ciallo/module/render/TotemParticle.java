package com.ciallo.module.render;

import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.NumberSetting;

/**
 * Custom totem particle module, ported from the original mod's {@code TotemParticle}.
 * Tweaks the velocity and colour of the totem pop particles.
 */
public class TotemParticle extends Module {
    public static TotemParticle INSTANCE;

    private final ColorSetting color = this.m28(new ColorSetting("Color", 0xFFFFFFFF));
    private final ColorSetting color2 = this.m28(new ColorSetting("Color2", 0xFF000000));
    private final NumberSetting velocityXZ = this.m28(new NumberSetting("VelocityXZ", 100.0, 0.0, 500.0, 1.0));
    private final NumberSetting velocityY = this.m28(new NumberSetting("VelocityY", 100.0, 0.0, 500.0, 1.0));

    public TotemParticle() {
        super("TotemParticle", "Custom totem particle colour and velocity.", Category.RENDER);
        this.setChinese("自定义图腾粒子");
        this.setChineseDescription("修改图腾爆开粒子的颜色与速度");
        INSTANCE = this;
    }

    /** Horizontal velocity multiplier, 1.0 = vanilla. */
    public float getVelocityXZ() {
        return (float) (velocityXZ.getValue() / 100.0);
    }

    /** Vertical velocity multiplier, 1.0 = vanilla. */
    public float getVelocityY() {
        return (float) (velocityY.getValue() / 100.0);
    }

    /** Randomly blended colour between the two configured colours (ARGB). */
    public int getParticleColor() {
        int first = color.getColor();
        int second = color2.getColor();
        double t = Math.random();
        int a = (int) (((first >>> 24) & 0xFF) + (((second >>> 24) & 0xFF) - ((first >>> 24) & 0xFF)) * t);
        int r = (int) (((first >> 16) & 0xFF) + (((second >> 16) & 0xFF) - ((first >> 16) & 0xFF)) * t);
        int g = (int) (((first >> 8) & 0xFF) + (((second >> 8) & 0xFF) - ((first >> 8) & 0xFF)) * t);
        int b = (int) ((first & 0xFF) + ((second & 0xFF) - (first & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
