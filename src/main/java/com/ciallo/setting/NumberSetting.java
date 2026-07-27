package com.ciallo.setting;

import java.util.function.Supplier;

public class NumberSetting extends Setting {
    private double value;
    private final double min;
    private final double max;
    private final double step;
    private final double defaultValue;
    private Supplier<Boolean> visible;

    public NumberSetting(String name, double value, double min, double max, double step) {
        this(name, value, min, max, step, value, () -> true, null, "", false);
    }

    public NumberSetting(String name, double value, double min, double max, double step, double defaultValue) {
        this(name, value, min, max, step, defaultValue, () -> true, null, "", false);
    }

    public NumberSetting(String name, double value, double min, double max, double step, double defaultValue, Supplier<Boolean> visible, Object parent, String description, boolean hidden) {
        super(name, description);
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
        this.defaultValue = defaultValue;
        this.visible = visible;
    }

    public double getValue() {
        return value;
    }

    public float getFloat() {
        return (float) value;
    }

    public int getInt() {
        return (int) Math.round(value);
    }

    public void setValue(double value) {
        this.value = Math.max(min, Math.min(max, value));
    }

    public void setValueWithoutClamp(double value) {
        this.value = value;
    }

    public void setInt(int value) {
        this.value = Math.max(min, Math.min(max, value));
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public double getDefaultValue() {
        return defaultValue;
    }
}

