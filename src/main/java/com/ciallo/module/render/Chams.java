package com.ciallo.module.render;

import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.NumberSetting;

public class Chams extends Module {
    public static Chams INSTANCE;
    
    public final BooleanSetting crystal = this.m28(new BooleanSetting("Crystal", true));
    
    public final BooleanSetting wireframe = this.m28(new BooleanSetting("Wireframe", false));
    public final ColorSetting wireColor = this.m28(new ColorSetting("WireColor", -1));
    
    public final BooleanSetting custom = this.m28(new BooleanSetting("Custom", false));
    public final BooleanSetting depth = this.m28(new BooleanSetting("Depth", false));
    public final BooleanSetting chamsTexture = this.m28(new BooleanSetting("ChamsTexture", true));
    public final ColorSetting fill = this.m28(new ColorSetting("Fill", 0x64FFFFFF));
    public final ColorSetting line = this.m28(new ColorSetting("Line", 0x64FFFFFF));
    public final ColorSetting core = this.m28(new ColorSetting("Core", -1));
    public final ColorSetting outerFrame = this.m28(new ColorSetting("OuterFrame", -1));
    public final ColorSetting innerFrame = this.m28(new ColorSetting("InnerFrame", -1));
    public final BooleanSetting glint = this.m28(new BooleanSetting("Glint", true));
    public final BooleanSetting texture = this.m28(new BooleanSetting("Texture", true));
    public final BooleanSetting spinSync = this.m28(new BooleanSetting("SpinSync", false));
    public final NumberSetting scale = this.m28(new NumberSetting("Scale", 1.0, 0.0, 3.0, 0.01));
    public final NumberSetting spinSpeed = this.m28(new NumberSetting("SpinSpeed", 1.0, 0.0, 3.0, 0.01));
    public final NumberSetting bounceHeight = this.m28(new NumberSetting("BounceHeight", 1.0, 0.0, 3.0, 0.01));
    public final NumberSetting bounceSpeed = this.m28(new NumberSetting("BounceSpeed", 1.0, 0.0, 3.0, 0.01));
    public final NumberSetting yOffset = this.m28(new NumberSetting("YOffset", 0.0, -1.0, 1.0, 0.01));
    
    private int age;

    public int getAge() {
        return age;
    }

    public void incrementAge() {
        age++;
    }

    public Chams() {
        super("Chams", "Custom end crystal rendering", Category.RENDER);
        this.setChinese("水晶渲染");
        INSTANCE = this;
    }

    public boolean customCrystal() {
        return this.isEnabled() && this.crystal.getValue();
    }

    @Override
    public void onEnable() {
        age = 0;
    }
}