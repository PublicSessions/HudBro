package com.ciallo.setting;

public abstract class Setting {
    protected final String name;
    protected final String description;

    public Setting(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}

