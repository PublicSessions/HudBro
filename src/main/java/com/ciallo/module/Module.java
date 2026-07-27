package com.ciallo.module;

import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.Setting;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected final String name;
    protected final String description;
    protected final Category category;
    protected boolean enabled;
    protected final List<Setting> settings;
    protected Setting colorSetting;
    protected String chineseName;
    protected String chineseDescription;

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = false;
        this.settings = new ArrayList<>();
    }

    public void setChinese(String chineseName) {
        this.chineseName = chineseName;
    }

    public void setChineseDescription(String chineseDescription) {
        this.chineseDescription = chineseDescription;
    }

    public <T extends Setting> T m28(T setting) {
        settings.add(setting);
        return setting;
    }

    public void setFlag3(boolean enabled) {
        this.enabled = enabled;
    }

    public ColorSetting getColorSetting2() {
        if (colorSetting == null || !(colorSetting instanceof ColorSetting)) {
            colorSetting = new ColorSetting("Color", -1);
        }
        return (ColorSetting) colorSetting;
    }

    public void onEnable() {}

    public void onDisable() {}

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        if (enabled) {
            onDisable();
        } else {
            onEnable();
        }
        enabled = !enabled;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public List<Setting> getSettings() {
        return settings;
    }
}

