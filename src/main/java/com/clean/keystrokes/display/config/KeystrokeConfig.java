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

    public enum ColorPreset {
        CLASSIC("Classic", 0xFFFFFFFF, 0xAA000000, 0xFF000000, 0xAAFFFFFF),
        INVERTED("Inverted", 0xFF000000, 0xAAFFFFFF, 0xFFFFFFFF, 0xAA000000),
        CLEAN("Clean", 0xFFFFFFFF, 0x00FFFFFF, 0xFF000000, 0x00FFFFFF),
        BEEHIVE("Beehive", 0xFFFFD000, 0xFF000000, 0xFF000000, 0xFFFFD000),
        MINT_CREAM("Mint & Cream", 0xFF00A896, 0xFFF0F3BD, 0xFF05668D, 0xFF02C39A),
        CYBER_GRAPE("Cyber Grape", 0xFFC7EDE4, 0xFF820B8A, 0xFF672A4E, 0xFFAF9AB2),
        VIBRANT_MANGO("Vibrant Mango", 0xFF4200FF, 0xFFFFBC42, 0xFF000000, 0xFFD81159),
        DIRT("Dirt", 0xFF753A38, 0xFFA1BA5A, 0xFFC4DF96, 0xFF633332),
        RETRO("Retro", 0xFFA71D31, 0xFFF1F0CC, 0xFF3F0D12, 0xFFD5BF86),
        CUSTOM("Custom", 0, 0, 0, 0);

        private final String displayName;
        private final int keyColor;
        private final int keyBackgroundColor;
        private final int keyPressedColor;
        private final int keyPressedBackgroundColor;

        ColorPreset(String displayName, int keyColor, int keyBackgroundColor, int keyPressedColor, int keyPressedBackgroundColor) {
            this.displayName = displayName;
            this.keyColor = keyColor;
            this.keyBackgroundColor = keyBackgroundColor;
            this.keyPressedColor = keyPressedColor;
            this.keyPressedBackgroundColor = keyPressedBackgroundColor;
        }

        public boolean isCustom() {
            return this == CUSTOM;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static final int DEFAULT_KEY_COLOR = 0xFFFFFFFF;
    private static final int DEFAULT_KEY_BACKGROUND_COLOR = 0xAA000000;
    private static final int DEFAULT_KEY_PRESSED_COLOR = 0xFF000000;
    private static final int DEFAULT_KEY_PRESSED_BACKGROUND_COLOR = 0xAAFFFFFF;
    private static final int DEFAULT_KEY_TEXT_SHADOW_COLOR = 0xFF000000;
    private static final int DEFAULT_KEY_PRESSED_TEXT_SHADOW_COLOR = 0xFF000000;

    public CornerPosition position             = CornerPosition.TOP_RIGHT;
    public boolean useCustomPosition           = false;
    public double  customPositionXPercent      = 50.0;
    public double  customPositionYPercent      = 50.0;
    public int  keyColor                       = DEFAULT_KEY_COLOR;
    public int  keyBackgroundColor             = DEFAULT_KEY_BACKGROUND_COLOR;
    public int  keyPressedColor                = DEFAULT_KEY_PRESSED_COLOR;
    public int  keyPressedBackgroundColor      = DEFAULT_KEY_PRESSED_BACKGROUND_COLOR;
    public boolean pressAnimation              = true;
    public boolean tickSyncedKeyPresses        = false;
    public boolean rainbowKeyNormal            = false;
    public boolean rainbowKeyPressed           = false;
    public boolean rainbowBackgroundNormal     = false;
    public boolean rainbowBackgroundPressed    = false;
    public boolean keyTextShadow               = false;
    public boolean keyPressedTextShadow        = false;
    public boolean useCustomTextShadowColor    = false;
    public int  keyTextShadowColor             = DEFAULT_KEY_TEXT_SHADOW_COLOR;
    public int  keyPressedTextShadowColor      = DEFAULT_KEY_PRESSED_TEXT_SHADOW_COLOR;
    public boolean showSneakSprintRow          = true;
    public ColorPreset colorPreset             = ColorPreset.CLASSIC;

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
                if (instance.colorPreset == null) {
                    instance.colorPreset = instance.matchesKnownPreset();
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

    public void applyPreset(ColorPreset preset) {
        if (preset == null) {
            return;
        }
        this.colorPreset = preset;
        if (preset.isCustom()) {
            return;
        }

        this.keyColor = preset.keyColor;
        this.keyBackgroundColor = preset.keyBackgroundColor;
        this.keyPressedColor = preset.keyPressedColor;
        this.keyPressedBackgroundColor = preset.keyPressedBackgroundColor;
    }

    public ColorPreset refreshPresetFromCurrentColors() {
        this.colorPreset = matchesKnownPreset();
        if (this.colorPreset.isCustom()) {
            this.colorPreset = ColorPreset.CUSTOM;
        }
        return this.colorPreset;
    }

    private ColorPreset matchesKnownPreset() {
        for (ColorPreset preset : ColorPreset.values()) {
            if (preset.isCustom()) {
                continue;
            }
            if (preset.keyColor == keyColor
                    && preset.keyBackgroundColor == keyBackgroundColor
                    && preset.keyPressedColor == keyPressedColor
                    && preset.keyPressedBackgroundColor == keyPressedBackgroundColor) {
                return preset;
            }
        }
        return ColorPreset.CUSTOM;
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