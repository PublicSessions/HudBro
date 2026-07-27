package com.ciallo.setting;

public class ColorSetting extends Setting {
    private int color;

    public ColorSetting(String name, int defaultColor) {
        super(name, "");
        this.color = defaultColor;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setObj94(int color) {
        this.color = color;
    }
}

