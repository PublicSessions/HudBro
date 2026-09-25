package com.ciallo.module.render;

import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.NumberSetting;

/**
 * Chams module, ported from the original mod's {@code Chams}.
 *
 * <p>Only the end crystal part is ported to 1.21.11. The crystal is re-rendered with a custom
 * tint / alpha / scale / spin, and each of the three crystal layers (outer glass, inner glass and
 * the inner core cube) can be switched off individually — see {@code MixinEndCrystalRenderer}.</p>
 *
 * <p>The original mod's "chams" for living entities (through wall tinted models) and the first
 * person hand colour / glow had no equivalent in the rewritten 1.21.11 render pipeline, which
 * removed {@code RenderSystem.setShaderColor}, the depth / blend state setters and the immediate
 * mode entity render hook, so they are not part of this port.</p>
 */
public class Chams extends Module {
    public static Chams INSTANCE;

    // Crystal
    private final BooleanSetting crystal = this.m28(new BooleanSetting("Crystal", true));
    private final BooleanSetting custom = this.m28(new BooleanSetting("Custom", false));
    private final ColorSetting crystalColor = this.m28(new ColorSetting("Color", 0xFFFFFFFF));

    // Per layer render switches (same idea as the original OuterFrame / InnerFrame / Core toggles)
    private final BooleanSetting outerFrame = this.m28(new BooleanSetting("OuterFrame", true));
    private final BooleanSetting innerFrame = this.m28(new BooleanSetting("InnerFrame", true));
    private final BooleanSetting core = this.m28(new BooleanSetting("Core", true));

    private final BooleanSetting spinSync = this.m28(new BooleanSetting("SpinSync", false));
    private final NumberSetting scale = this.m28(new NumberSetting("Scale", 1.0, 0.0, 3.0, 0.01));
    private final NumberSetting spinValue = this.m28(new NumberSetting("SpinSpeed", 1.0, 0.0, 3.0, 0.01));
    private final NumberSetting floatOffset = this.m28(new NumberSetting("YOffset", 0.0, -1.0, 1.0, 0.01));

    public int age;

    public Chams() {
        super("Chams", "Custom end crystal rendering.", Category.RENDER);
        this.setChinese("模型上色");
        this.setChineseDescription("自定义末影水晶的渲染、配色与各层开关");
        INSTANCE = this;
    }

    /** Called every client tick. */
    public void onTick() {
        this.age++;
    }

    public boolean customCrystal() {
        return this.isEnabled() && this.crystal.getValue();
    }

    /** Tint colour of the custom crystal. */
    public int getCrystalColor() {
        return this.custom.getValue() ? this.crystalColor.getColor() : 0xFFFFFFFF;
    }

    public boolean isOuterFrame() {
        return this.outerFrame.getValue();
    }

    public boolean isInnerFrame() {
        return this.innerFrame.getValue();
    }

    public boolean isCore() {
        return this.core.getValue();
    }

    public boolean isCustom() {
        return this.custom.getValue();
    }

    public boolean isSpinSync() {
        return this.spinSync.getValue();
    }

    public float getScale() {
        return this.scale.getFloat();
    }

    public float getSpinValue() {
        return this.spinValue.getFloat();
    }

    public float getFloatOffset() {
        return this.floatOffset.getFloat();
    }
}
