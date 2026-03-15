package com.clean.keystrokes.display.hud;

import com.clean.keystrokes.display.config.KeystrokeConfig;
import net.minecraft.client.MinecraftClient;

public final class HudLayout {

    public static final int KEY_SIZE    = 21;
    public static final int HALF_HEIGHT = 15;
    public static final int GAP         = 2;
    public static final int CENTER_GAP  = GAP * 4;
    public static final int MARGIN      = 4;
    public static final int DOT_SIZE    = 3;

    public final int originX, originY;
    public final int gridW, gridH;

    // WASD columns
    public final int col0, col1, col2;

    // Row Y positions
    public final int rowW, rowASD, rowSpace, rowSneakSprint, rowMouse;

    // Space row
    public final int spaceX, spaceW;

    // Sneak/sprint row
    public final int sneakX, sneakW;
    public final int sprintX, sprintW;

    // Mouse row
    public final int stripLmbX,    stripLmbW;
    public final int stripCenterX, stripCenterW;
    public final int stripRmbX,    stripRmbW;

    public HudLayout(KeystrokeConfig cfg) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        int kS = KEY_SIZE;
        int g  = GAP;
        int cg = CENTER_GAP;

        gridW = kS * 3 + g * 2;
        gridH = kS * 2              // W + ASD
                + HALF_HEIGHT * 3   // space + sneakSprint + mouse rows
                + g * 4;            // gaps between each row

        int ox, oy;
        switch (cfg.position) {
            case TOP_RIGHT    -> { ox = screenW - gridW - MARGIN; oy = MARGIN; }
            case BOTTOM_LEFT  -> { ox = MARGIN;                   oy = screenH - gridH - MARGIN; }
            case BOTTOM_RIGHT -> { ox = screenW - gridW - MARGIN; oy = screenH - gridH - MARGIN; }
            default           -> { ox = MARGIN;                   oy = MARGIN; }
        }
        originX = ox;
        originY = oy;

        col0 = originX;
        col1 = originX + kS + g;
        col2 = originX + (kS + g) * 2;

        rowW           = originY;
        rowASD         = originY + kS + g;
        rowSpace       = rowASD + kS + g;
        rowSneakSprint = rowSpace + HALF_HEIGHT + g;
        rowMouse       = rowSneakSprint + HALF_HEIGHT + g;

        // Space: full grid width
        spaceX = originX;
        spaceW = gridW;

        // Sneak/sprint: each half = (gridW - CENTER_GAP) / 2
        int halfW = (gridW - cg) / 2;
        sneakX  = originX;
        sneakW  = halfW;
        sprintX = originX + halfW + cg;
        sprintW = gridW - halfW - cg; // handles odd gridW

        // Mouse strip
        stripLmbX    = originX;
        stripLmbW    = kS;
        stripCenterX = originX + kS;
        stripCenterW = kS + g * 2;
        stripRmbX    = originX + kS + stripCenterW;
        stripRmbW    = kS;
    }
}