package com.ciallo.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.NumberSetting;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

/**
 * Global HudBro appearance settings, edited from the main screen ({@code /hudbro} or the key bind).
 * Stored next to the module config in {@code config/hudbro-global.json}.
 */
public class GlobalConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
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

    private static File getFile() {
        return Path.of(Minecraft.getInstance().gameDirectory.getPath(), "config", FILE_NAME).toFile();
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
        File file = getFile();
        if (!file.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.has("themeColor")) {
                themeColor.setColor(root.get("themeColor").getAsInt());
            }
            if (root.has("uiScale")) {
                uiScale.setValue(root.get("uiScale").getAsDouble());
            }
            if (root.has("uiOpacity")) {
                uiOpacity.setValue(root.get("uiOpacity").getAsDouble());
            }
            if (root.has("uiBlur")) {
                uiBlur.setValue(root.get("uiBlur").getAsDouble());
            }
            if (root.has("editorShowDisabled")) {
                editorShowDisabled.setValue(root.get("editorShowDisabled").getAsBoolean());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        root.addProperty("themeColor", themeColor.getColor());
        root.addProperty("uiScale", uiScale.getValue());
        root.addProperty("uiOpacity", uiOpacity.getValue());
        root.addProperty("uiBlur", uiBlur.getValue());
        root.addProperty("editorShowDisabled", editorShowDisabled.getValue());
        File file = getFile();
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(root, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
