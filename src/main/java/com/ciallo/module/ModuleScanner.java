package com.ciallo.module;

import com.ciallo.HudBro;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Finds every {@link Module} implementation inside the HudBro jar and registers the ones that are not
 * registered yet, so a newly added HUD (or render module) shows up in the HUD editor and in
 * {@code /hudbro} without having to be added to the registration list by hand.
 *
 * <p>Classes are visited in a stable (alphabetical) order and appended after the explicitly
 * registered modules, which keeps the hand written order of the editor list intact.</p>
 */
public final class ModuleScanner {
    private static final String PACKAGE = "com.ciallo.module";
    private static final String PACKAGE_PATH = PACKAGE.replace('.', '/');
    private static final String CLASS_SUFFIX = ".class";

    private ModuleScanner() {
    }

    /**
     * Registers every module class shipped in the mod jar that has no instance yet.
     *
     * @return how many modules were added
     */
    public static int discover(ModuleManager manager) {
        List<String> classNames = findModuleClasses();
        int added = 0;
        for (String className : classNames) {
            Class<?> type = loadClass(className);
            if (type == null || manager.isRegistered(type)) {
                continue;
            }
            try {
                if (manager.register((Module) type.getDeclaredConstructor().newInstance())) {
                    added++;
                }
            } catch (Throwable t) {
                HudBro.LOGGER.error("Could not auto-register {}", className, t);
            }
        }
        if (added > 0) {
            HudBro.LOGGER.info("Auto-registered {} newly added module(s) from the mod jar", added);
        }
        return added;
    }

    private static List<String> findModuleClasses() {
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(HudBro.MOD_ID);
        if (container.isEmpty()) {
            HudBro.LOGGER.warn("The {} mod container is missing, no modules were auto-registered", HudBro.MOD_ID);
            return List.of();
        }
        // Sorted so the discovered order does not depend on the file system.
        TreeSet<String> classNames = new TreeSet<>();
        for (Path root : container.get().getRootPaths()) {
            collect(root, classNames);
        }
        return new ArrayList<>(classNames);
    }

    private static void collect(Path root, TreeSet<String> classNames) {
        Path base;
        try {
            base = root.resolve(PACKAGE_PATH);
            if (!Files.isDirectory(base)) {
                return;
            }
        } catch (Exception e) {
            HudBro.LOGGER.warn("Could not inspect the module classes under {}", root, e);
            return;
        }
        try (Stream<Path> stream = Files.walk(base)) {
            for (Path path : (Iterable<Path>) stream.filter(ModuleScanner::isClassFile).toList()) {
                String className = toClassName(root, path);
                if (className != null && isInstantiableModule(className)) {
                    classNames.add(className);
                }
            }
        } catch (Exception e) {
            HudBro.LOGGER.error("Could not scan the module classes under {}", base, e);
        }
    }

    private static boolean isClassFile(Path path) {
        String name = path.getFileName().toString();
        // Inner and anonymous classes are never modules on their own.
        return name.endsWith(CLASS_SUFFIX) && name.indexOf('$') < 0;
    }

    private static String toClassName(Path root, Path path) {
        try {
            String relative = root.relativize(path).toString();
            relative = relative.replace('\\', '.').replace('/', '.');
            return relative.substring(0, relative.length() - CLASS_SUFFIX.length());
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isInstantiableModule(String className) {
        Class<?> type = loadClass(className);
        if (type == null || !Module.class.isAssignableFrom(type)) {
            return false;
        }
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers()) || type.isAnonymousClass()) {
            return false;
        }
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            return Modifier.isPublic(constructor.getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        } catch (Throwable t) {
            // A missing class in a constructor signature must not stop the scan.
            HudBro.LOGGER.error("Could not inspect the constructor of {}", className, t);
            return false;
        }
    }

    private static Class<?> loadClass(String className) {
        try {
            // Not initialising keeps classes that turn out to be irrelevant free of side effects.
            return Class.forName(className, false, ModuleScanner.class.getClassLoader());
        } catch (Throwable t) {
            HudBro.LOGGER.error("Could not load the module class {}", className, t);
            return null;
        }
    }
}
