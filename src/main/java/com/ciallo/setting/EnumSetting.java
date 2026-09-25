package com.ciallo.setting;

/**
 * Setting holding one value of an enum.
 */
public class EnumSetting<T extends Enum<T>> extends Setting {
    private final T[] values;
    private T value;
    private final T defaultValue;

    public EnumSetting(String name, T defaultValue) {
        super(name, "");
        this.values = defaultValue.getDeclaringClass().getEnumConstants();
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T[] getValues() {
        return values;
    }

    /** Cycles to the next enum constant, used by the settings screen. */
    public void cycle() {
        int index = value.ordinal();
        value = values[(index + 1) % values.length];
    }

    public void cycleBack() {
        int index = value.ordinal();
        value = values[(index - 1 + values.length) % values.length];
    }

    public String getNameValue() {
        return value.name();
    }

    public boolean setByName(String name) {
        for (T candidate : values) {
            if (candidate.name().equalsIgnoreCase(name)) {
                this.value = candidate;
                return true;
            }
        }
        return false;
    }
}
