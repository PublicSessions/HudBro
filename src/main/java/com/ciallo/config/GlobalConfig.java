package com.ciallo.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ciallo.HudBro;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.NumberSetting;
import com.ciallo.setting.Setting;

import java.nio.file.Path;

/**
 * Global HudBro appearance settings, edited from the main screen ({@code /hudbro} or the key bind).
 * Stored next to the module config in {@code config/hudbro-global.json}.
 */
public class GlobalConfig {
    private static final String FILE_NAME = "hudbro-global.json";

    /** Accent colour used for panel borders, titles and handles. */
    public static final ColorSetting themeColor = new ColorSetting("ThemeColor", 0xFFFF4444);
    /** Scale of the HudBro screens, 0.5 - 2.0. */
    public static final NumberSetting uiScale = new NumberSetting("UiScale", 1.0, 0.5, 2.0, 0.05);
    /** Opacity of the screen panels, 0 - 255. */
    public static final NumberSetting uiOpacity = new NumberSetting("UiOpacity", 210.0, 20.0, 255.0, 1.0);
    /** Background blur strength behind HudBro screens, 0 - 10 (0 = off). */
    public static final NumberSetting uiBlur = new NumberSetting("UiBlur", 0.0, 0.0, 10.0, 1.0);
    /** Whether the HUD editor also draws HUDs that are currently disabled. */
    public static final BooleanSetting editorShowDisabled = new BooleanSetting("EditorShowDisabled", false);

    private GlobalConfig() {
    }

    private static Path getFile() {
        return ConfigStore.getFile(FILE_NAME);
    }

    public static float scale() {
        return (float) uiScale.getValue();
    }

    /** Panel opacity as 0..255. */
    public static int opacity() {
        return (int) Math.max(0.0, Math.min(255.0, uiOpacity.getValue()));
    }

    public static int blur() {
        return (int) Math.max(0.0, Math.min(10.0, uiBlur.getValue()));
    }

    public static int accent() {
        return themeColor.getColor();
    }

    /** Accent colour with the given extra 0..1 fade factor, keeping the configured alpha. */
    public static int accent(float fade) {
        int c = themeColor.getColor();
        int a = (int) (((c >>> 24) & 0xFF) * fade);
        return (a << 24) | (c & 0xFFFFFF);
    }

    /** Panel background colour, scaled by the configured opacity and fade factor. */
    public static int panel(int baseAlpha, float fade) {
        int a = (int) (baseAlpha * (opacity() / 255.0f) * fade);
        return (Math.max(0, Math.min(255, a)) << 24) | 0x0A0A0A;
    }

    public static void load() {
        JsonObject root = ConfigStore.read(getFile());
        if (root == null) {
            return;
        }
        restore(root, "themeColor", themeColor);
        restore(root, "uiScale", uiScale);
        restore(root, "uiOpacity", uiOpacity);
        restore(root, "uiBlur", uiBlur);
        restore(root, "editorShowDisabled", editorShowDisabled);
    }

    public static void save() {
        JsonObject root = new JsonObject();
        store(root, "themeColor", themeColor);
        store(root, "uiScale", uiScale);
        store(root, "uiOpacity", uiOpacity);
        store(root, "uiBlur", uiBlur);
        store(root, "editorShowDisabled", editorShowDisabled);
        ConfigStore.write(getFile(), root);
    }

    private static void store(JsonObject root, String key, Setting setting) {
        if (setting instanceof ColorSetting color) {
            root.addProperty(key, color.getColor());
        } else if (setting instanceof NumberSetting number) {
            root.addProperty(key, number.getValue());
        } else if (setting instanceof BooleanSetting bool) {
            root.addProperty(key, bool.getValue());
        }
    }

    private static void restore(JsonObject root, String key, Setting setting) {
        JsonElement value = root.get(key);
        if (value == null || value.isJsonNull()) {
            return;
        }
        try {
            if (setting instanceof ColorSetting color) {
                color.setColor(value.getAsInt());
            } else if (setting instanceof NumberSetting number) {
                number.setValue(value.getAsDouble());
            } else if (setting instanceof BooleanSetting bool) {
                bool.setValue(value.getAsBoolean());
            }
        } catch (Exception e) {
            HudBro.LOGGER.warn("Ignoring the invalid saved value of {}", key);
        }
    }
}
