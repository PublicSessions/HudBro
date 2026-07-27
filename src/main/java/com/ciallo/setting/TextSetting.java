package com.ciallo.setting;

public class TextSetting extends Setting {
    private String value;

    public TextSetting(String name, String defaultValue, String description) {
        super(name, description);
        this.value = defaultValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

