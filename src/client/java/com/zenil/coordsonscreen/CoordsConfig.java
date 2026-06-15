package com.zenil.coordsonscreen;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plain config model with its own Gson-backed file I/O. This is the source of
 * truth for settings and is fully usable by hand-editing
 * {@code config/coordsonscreen.json} — no Cloth Config required. Cloth Config
 * (via {@link CoordsConfigScreen}) is an optional in-game editor for the same
 * values; it calls {@link #save()} after applying changes.
 *
 * <p>This class deliberately has no dependency on Cloth Config so the mod loads
 * and is configurable even when Cloth is not installed.
 */
public class CoordsConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("coordsonscreen");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH =
        FabricLoader.getInstance().getConfigDir().resolve("coordsonscreen.json");

    private static CoordsConfig instance;

    // ---- Settings (hand-editable in config/coordsonscreen.json) ----
    public HudPosition position = HudPosition.TOP_LEFT;
    public int xPadding = 5;            // 0..200
    public int yPadding = 5;            // 0..200
    // On-screen text size as a percentage, independent of the GUI Scale setting.
    // 100% renders at the same size as GUI Scale 2 and stays constant regardless
    // of the player's actual GUI Scale.
    public int fontSizePercent = 100;   // 50..300
    public boolean showMainCoords = true;
    public boolean showAltCoords = true;
    public boolean showBiome = true;
    public boolean useBiomeColor = true;
    public boolean showStructure = true;

    public enum HudPosition {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    /** Returns the active config, loading it from disk on first access. */
    public static CoordsConfig get() {
        if (instance == null) load();
        return instance;
    }

    /**
     * Loads the config from disk, creating it with defaults if it is missing or
     * unreadable. Missing fields fall back to their defaults, and out-of-range
     * values are clamped. The (possibly completed/corrected) config is written
     * back so the file always reflects the effective settings.
     */
    public static void load() {
        CoordsConfig loaded = null;
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                loaded = GSON.fromJson(reader, CoordsConfig.class);
            } catch (IOException | JsonParseException e) {
                LOGGER.warn("[CoordsOnScreen] Could not read {}, regenerating defaults", PATH, e);
            }
        }
        instance = loaded != null ? loaded : new CoordsConfig();
        instance.clamp();
        save();
    }

    /** Writes the active config to disk. */
    public static void save() {
        if (instance == null) return;
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            LOGGER.warn("[CoordsOnScreen] Could not save {}", PATH, e);
        }
    }

    /** Guards against nulls and keeps numeric values within valid ranges. */
    private void clamp() {
        if (position == null) position = HudPosition.TOP_LEFT;
        xPadding = Math.max(0, Math.min(200, xPadding));
        yPadding = Math.max(0, Math.min(200, yPadding));
        fontSizePercent = Math.max(50, Math.min(300, fontSizePercent));
    }
}
