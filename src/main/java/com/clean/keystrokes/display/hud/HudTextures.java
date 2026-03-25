package com.clean.keystrokes.display.hud;

import net.minecraft.resources.Identifier;

public final class HudTextures {

    private HudTextures() {}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("clean-keystrokes", "textures/" + path);
    }

    public static final Identifier KEY_W            = id("key_w.png");
    public static final Identifier KEY_A            = id("key_a.png");
    public static final Identifier KEY_S            = id("key_s.png");
    public static final Identifier KEY_D            = id("key_d.png");
    public static final Identifier KEY_SPACE        = id("key_space.png");
    public static final Identifier KEY_SNK          = id("key_snk.png");
    public static final Identifier KEY_SPR          = id("key_spr.png");
    public static final Identifier KEY_LMB          = id("key_lmb.png");
    public static final Identifier KEY_RMB          = id("key_rmb.png");
    public static final Identifier KEY_MOUSE_CENTER = id("key_mouse_center.png");
    public static final Identifier DOT              = id("mouse_dot.png");
}