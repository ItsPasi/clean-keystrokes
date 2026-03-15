package com.clean.keystrokes.display.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class KeystrokeConfig {

    public enum CornerPosition { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    public CornerPosition position             = CornerPosition.TOP_RIGHT;
    public int  keyColor                       = 0xFFFFFFFF;
    public int  keyBackgroundColor             = 0xAA000000;
    public int  keyPressedColor                = 0xFF000000;
    public int  keyPressedBackgroundColor      = 0xAAFFFFFF;
    public boolean textShadow                  = false;
    public boolean pressAnimation              = true;
    public boolean rainbowText                 = false;

    // ── Persistence ───────────────────────────────────────────────
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("clean-keystrokes.json");

    private static KeystrokeConfig instance;

    public static KeystrokeConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try {
                instance = GSON.fromJson(Files.readString(PATH), KeystrokeConfig.class);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        instance = new KeystrokeConfig();
    }

    public void save() {
        try {
            Files.writeString(PATH, GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}