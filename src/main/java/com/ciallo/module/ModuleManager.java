package com.ciallo.module;

import com.ciallo.HudBro;
import com.ciallo.module.hud.AbstractHudModule;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final ModuleManager INSTANCE = new ModuleManager();
    private final List<Module> modules = new ArrayList<>();
    private final List<AbstractHudModule> hudModules = new ArrayList<>();

    private ModuleManager() {}

    /**
     * Adds a module to the registry.
     *
     * <p>Duplicates are rejected: the same instance twice, or a second module with the same name, could
     * not be told apart in the config file or by name, so it is dropped instead.</p>
     *
     * @return whether the module was added
     */
    public boolean register(Module module) {
        if (modules.contains(module)) {
            HudBro.LOGGER.warn("{} is already registered, ignoring the duplicate instance",
                    module.getClass().getName());
            return false;
        }
        for (Module existing : modules) {
            if (existing.getName().equals(module.getName())) {
                HudBro.LOGGER.error("{} has the same name as the already registered {}, ignoring it",
                        module.getClass().getName(), existing.getClass().getName());
                return false;
            }
        }
        modules.add(module);
        if (module instanceof AbstractHudModule) {
            hudModules.add((AbstractHudModule) module);
        }
        return true;
    }

    /**
     * Whether a module of exactly this class is registered. Used by the class path scanner to tell an
     * already registered module from a newly added one.
     */
    public boolean isRegistered(Class<?> type) {
        for (Module module : modules) {
            if (module.getClass() == type) {
                return true;
            }
        }
        return false;
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
