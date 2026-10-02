package com.ciallo.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ciallo.HudBro;
import net.minecraft.client.Minecraft;
import com.ciallo.module.Module;
import com.ciallo.module.ModuleManager;
import com.ciallo.setting.EnumSetting;
import com.ciallo.setting.NumberSetting;
import com.ciallo.setting.TextSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.Setting;

import java.nio.file.Path;

/**
 * Enabled state and settings of every registered module, stored in {@code config/hudbro.json}.
 *
 * <p>Loading never aborts because of a single bad entry: an unusable value is reported in the log and
 * skipped so one hand edited or outdated field cannot throw away the whole configuration.</p>
 */
public class HudConfig {
    private static final String FILE_NAME = "hudbro.json";

    private HudConfig() {
    }

    public static Path getFile() {
        return ConfigStore.getFile(FILE_NAME);
    }

    public static void load() {
        loadFrom(getFile());
    }

    public static void save() {
        saveTo(getFile());
    }

    static void loadFrom(Path file) {
        JsonObject root = ConfigStore.read(file);
        if (root == null) {
            return;
        }
        int screenWidth = screenWidth();
        int screenHeight = screenHeight();
        for (Module module : ModuleManager.INSTANCE.getModules()) {
            try {
                loadModule(root, module, screenWidth, screenHeight);
            } catch (Exception e) {
                HudBro.LOGGER.error("Could not load the saved settings of {}", module.getName(), e);
            }
        }
    }

    static void saveTo(Path file) {
        JsonObject root = new JsonObject();
        for (Module module : ModuleManager.INSTANCE.getModules()) {
            JsonObject modObj = new JsonObject();
            modObj.addProperty("enabled", module.isEnabled());
            for (Setting setting : module.getSettings()) {
                try {
                    store(modObj, setting);
                } catch (Exception e) {
                    // A single unusable setting must not cost the whole configuration.
                    HudBro.LOGGER.error("Could not store {}.{}", module.getName(), setting.getName(), e);
                }
            }
            if (root.has(module.getName())) {
                HudBro.LOGGER.error("Two modules share the name {}, only one can be stored", module.getName());
            }
            root.add(module.getName(), modObj);
        }
        ConfigStore.write(file, root);
    }

    private static void loadModule(JsonObject root, Module module, int screenWidth, int screenHeight) {
        JsonElement element = root.get(module.getName());
        if (element == null || !element.isJsonObject()) {
            return;
        }
        JsonObject modObj = element.getAsJsonObject();
        if (modObj.has("enabled")) {
            module.setFlag3(modObj.get("enabled").getAsBoolean());
        }
        migrateLegacyPosition(modObj, screenWidth, screenHeight);
        for (Setting setting : module.getSettings()) {
            JsonElement value = modObj.get(setting.getName());
            if (value == null || value.isJsonNull()) {
                continue;
            }
            try {
                restore(setting, value);
            } catch (Exception e) {
                HudBro.LOGGER.warn("Ignoring the invalid saved value of {}.{}", module.getName(), setting.getName());
            }
        }
    }

    /**
     * Configs written by the original mod stored absolute pixel positions; they are converted to the
     * relative {@code RelX} / {@code RelY} pair the HUD editor uses now.
     */
    private static void migrateLegacyPosition(JsonObject modObj, int screenWidth, int screenHeight) {
        if (!modObj.has("X") || !modObj.has("Y") || modObj.has("RelX") || modObj.has("RelY")) {
            return;
        }
        double oldX = modObj.get("X").getAsDouble();
        double oldY = modObj.get("Y").getAsDouble();
        modObj.addProperty("RelX", Math.max(0, Math.min(1, oldX / screenWidth)));
        modObj.addProperty("RelY", Math.max(0, Math.min(1, oldY / screenHeight)));
    }

    private static void store(JsonObject modObj, Setting setting) {
        if (setting instanceof NumberSetting num) {
            modObj.addProperty(setting.getName(), num.getValue());
        } else if (setting instanceof TextSetting txt) {
            modObj.addProperty(setting.getName(), txt.getValue());
        } else if (setting instanceof ColorSetting color) {
            modObj.addProperty(setting.getName(), color.getColor());
        } else if (setting instanceof BooleanSetting bool) {
            modObj.addProperty(setting.getName(), bool.getValue());
        } else if (setting instanceof EnumSetting<?> enumSetting) {
            modObj.addProperty(setting.getName(), enumSetting.getNameValue());
        }
    }

    private static void restore(Setting setting, JsonElement value) {
        if (setting instanceof NumberSetting num) {
            num.setValue(value.getAsDouble());
        } else if (setting instanceof TextSetting txt) {
            txt.setValue(value.getAsString());
        } else if (setting instanceof ColorSetting color) {
            color.setColor(value.getAsInt());
        } else if (setting instanceof BooleanSetting bool) {
            bool.setValue(value.getAsBoolean());
        } else if (setting instanceof EnumSetting<?> enumSetting) {
            enumSetting.setByName(value.getAsString());
        }
    }

    private static int screenWidth() {
        try {
            int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            return width > 0 ? width : 960;
        } catch (Throwable t) {
            return 960;
        }
    }

    private static int screenHeight() {
        try {
            int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            return height > 0 ? height : 540;
        } catch (Throwable t) {
            return 540;
        }
    }
}
