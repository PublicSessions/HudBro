package com.ciallo.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import com.ciallo.module.Module;
import com.ciallo.module.ModuleManager;
import com.ciallo.setting.NumberSetting;
import com.ciallo.setting.TextSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.Setting;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

public class HudConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "hudbro.json";

    private static File getFile() {
        return Path.of(Minecraft.getInstance().gameDirectory.getPath(), "config", FILE_NAME).toFile();
    }

    public static void load() {
        File file = getFile();
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (Module module : ModuleManager.INSTANCE.getModules()) {
                if (!root.has(module.getName())) continue;
                JsonObject modObj = root.getAsJsonObject(module.getName());
                if (modObj.has("enabled")) {
                    module.setFlag3(modObj.get("enabled").getAsBoolean());
                }
                for (Setting setting : module.getSettings()) {
                    if (!modObj.has(setting.getName())) continue;
                    if (setting instanceof NumberSetting num) {
                        num.setValue(modObj.get(setting.getName()).getAsDouble());
                    } else if (setting instanceof TextSetting txt) {
                        txt.setValue(modObj.get(setting.getName()).getAsString());
                    } else if (setting instanceof ColorSetting color) {
                        color.setColor(modObj.get(setting.getName()).getAsInt());
                    } else if (setting instanceof BooleanSetting bool) {
                        bool.setValue(modObj.get(setting.getName()).getAsBoolean());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        for (Module module : ModuleManager.INSTANCE.getModules()) {
            JsonObject modObj = new JsonObject();
            modObj.addProperty("enabled", module.isEnabled());
            for (Setting setting : module.getSettings()) {
                if (setting instanceof NumberSetting num) {
                    modObj.addProperty(setting.getName(), num.getValue());
                } else if (setting instanceof TextSetting txt) {
                    modObj.addProperty(setting.getName(), txt.getValue());
                } else if (setting instanceof ColorSetting color) {
                    modObj.addProperty(setting.getName(), color.getColor());
                } else if (setting instanceof BooleanSetting bool) {
                    modObj.addProperty(setting.getName(), bool.getValue());
                }
            }
            root.add(module.getName(), modObj);
        }
        File file = getFile();
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(root, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

