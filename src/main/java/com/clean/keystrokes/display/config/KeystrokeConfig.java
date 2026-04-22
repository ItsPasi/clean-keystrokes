package com.clean.keystrokes.display.config;

import com.clean.keystrokes.CleanKeyStrokes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class KeystrokeConfig {

    public enum CornerPosition { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    private static final int DEFAULT_KEY_COLOR = 0xFFFFFFFF;
    private static final int DEFAULT_KEY_BACKGROUND_COLOR = 0xAA000000;
    private static final int DEFAULT_KEY_PRESSED_COLOR = 0xFF000000;
    private static final int DEFAULT_KEY_PRESSED_BACKGROUND_COLOR = 0xAAFFFFFF;

    public CornerPosition position             = CornerPosition.TOP_RIGHT;
    public int  keyColor                       = DEFAULT_KEY_COLOR;
    public int  keyBackgroundColor             = DEFAULT_KEY_BACKGROUND_COLOR;
    public int  keyPressedColor                = DEFAULT_KEY_PRESSED_COLOR;
    public int  keyPressedBackgroundColor      = DEFAULT_KEY_PRESSED_BACKGROUND_COLOR;
    public boolean textShadow                  = false;
    public boolean pressAnimation              = true;
    public boolean rainbowText                 = false;
    public boolean showSneakSprintRow          = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("clean-keystrokes.json");

    private static KeystrokeConfig instance;

    public static KeystrokeConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static Path getPath() {
        return PATH;
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try {
                instance = GSON.fromJson(Files.readString(PATH), KeystrokeConfig.class);
                if (instance == null) {
                    CleanKeyStrokes.LOGGER.warn("Config file '{}' was empty or invalid, using defaults.", PATH);
                    instance = new KeystrokeConfig();
                }
                if (instance.ensureVisibleColors("load")) {
                    instance.save();
                }
                return;
            } catch (IOException e) {
                CleanKeyStrokes.LOGGER.error("Failed to read config '{}'. Using defaults.", PATH, e);
            }
        }
        instance = new KeystrokeConfig();
        instance.ensureVisibleColors("defaults");
    }

    public void save() {
        ensureVisibleColors("save");
        try {
            Files.writeString(PATH, GSON.toJson(this));
        } catch (IOException e) {
            CleanKeyStrokes.LOGGER.error("Failed to save config '{}'.", PATH, e);
        }
    }

    private boolean ensureVisibleColors(String source) {
        if (!areAllHudColorsFullyTransparent()) {
            return false;
        }

        keyColor = withAlpha(keyColor, (DEFAULT_KEY_COLOR >>> 24) & 0xFF);
        keyBackgroundColor = withAlpha(keyBackgroundColor, (DEFAULT_KEY_BACKGROUND_COLOR >>> 24) & 0xFF);
        keyPressedColor = withAlpha(keyPressedColor, (DEFAULT_KEY_PRESSED_COLOR >>> 24) & 0xFF);
        keyPressedBackgroundColor = withAlpha(keyPressedBackgroundColor, (DEFAULT_KEY_PRESSED_BACKGROUND_COLOR >>> 24) & 0xFF);

        CleanKeyStrokes.LOGGER.warn(
                "All Clean Keystrokes HUD colors were fully transparent during {}. Restored default alpha values so the HUD cannot become fully invisible.",
                source
        );
        return true;
    }

    public boolean areAllHudColorsFullyTransparent() {
        return isFullyTransparent(keyColor)
                && isFullyTransparent(keyBackgroundColor)
                && isFullyTransparent(keyPressedColor)
                && isFullyTransparent(keyPressedBackgroundColor);
    }

    private static boolean isFullyTransparent(int color) {
        return ((color >>> 24) & 0xFF) == 0;
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }
}