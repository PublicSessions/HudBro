package com.ciallo.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Shared JSON persistence for the HudBro configs.
 *
 * <p>Two properties matter here, because a config that is written incorrectly looks exactly like a
 * config that is never saved:
 * <ul>
 *   <li>{@link #write} writes to a temporary file and then moves it over the target, so an
 *       interrupted save (crash, kill, power loss) can never leave a half written file behind that
 *       would be rejected on the next launch.</li>
 *   <li>{@link #read} never throws. An unreadable file is moved aside as {@code *.broken} so the
 *       game still starts with defaults instead of silently losing the config on every launch.</li>
 * </ul>
 */
public final class ConfigStore {
    private static final Logger LOGGER = LoggerFactory.getLogger("hudbro/config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path configDir;

    private ConfigStore() {
    }

    /** The directory holding the HudBro config files, resolved once and cached. */
    public static Path getConfigDir() {
        Path cached = configDir;
        if (cached != null) {
            return cached;
        }
        Path dir = null;
        try {
            dir = FabricLoader.getInstance().getConfigDir();
        } catch (Throwable t) {
            LOGGER.warn("Fabric config directory unavailable, falling back to the game directory", t);
        }
        if (dir == null) {
            try {
                Minecraft client = Minecraft.getInstance();
                if (client != null && client.gameDirectory != null) {
                    dir = Path.of(client.gameDirectory.getPath(), "config");
                }
            } catch (Throwable t) {
                LOGGER.warn("Game directory unavailable, falling back to ./config", t);
            }
        }
        configDir = dir != null ? dir : Path.of("config");
        return configDir;
    }

    /** Absolute path of a config file inside the config directory. */
    public static Path getFile(String fileName) {
        return getConfigDir().resolve(fileName);
    }

    /**
     * Reads a config file.
     *
     * @return the parsed object, or {@code null} when the file is missing or cannot be parsed
     */
    public static JsonObject read(Path file) {
        if (file == null || !Files.isRegularFile(file)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            LOGGER.error("Could not read {}, keeping the current settings", file, e);
            moveAside(file);
            return null;
        }
    }

    /** Writes a config file atomically, replacing the previous content in one step. */
    public static void write(Path file, JsonObject root) {
        if (file == null || root == null) {
            return;
        }
        Path temp = null;
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            temp = Files.createTempFile(parent, file.getFileName().toString(), ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
            move(temp, file);
            temp = null;
        } catch (Exception e) {
            LOGGER.error("Could not write {}", file, e);
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (Exception ignored) {
                    // Nothing else can be done about a leftover temporary file.
                }
            }
        }
    }

    private static void move(Path from, Path to) throws Exception {
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void moveAside(Path file) {
        try {
            Path backup = file.resolveSibling(file.getFileName() + ".broken");
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.error("{} was unreadable and has been renamed to {}", file, backup);
        } catch (Exception e) {
            LOGGER.error("Could not move the unreadable config {} aside", file, e);
        }
    }
}
