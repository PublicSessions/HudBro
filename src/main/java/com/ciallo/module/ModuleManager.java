package com.ciallo.module;

import com.ciallo.module.hud.AbstractHudModule;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final ModuleManager INSTANCE = new ModuleManager();
    private final List<Module> modules = new ArrayList<>();
    private final List<AbstractHudModule> hudModules = new ArrayList<>();

    private ModuleManager() {}

    public void register(Module module) {
        modules.add(module);
        if (module instanceof AbstractHudModule) {
            hudModules.add((AbstractHudModule) module);
        }
    }

    public List<Module> getModules() {
        return new ArrayList<>(modules);
    }

    public List<Module> getModulesByCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }

    public List<AbstractHudModule> getHudModules() {
        return new ArrayList<>(hudModules);
    }

    public Module getModuleByName(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }
}

