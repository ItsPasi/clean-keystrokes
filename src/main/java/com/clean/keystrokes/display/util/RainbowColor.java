package com.clean.keystrokes.display.util;

public final class RainbowColor {

    private RainbowColor() {}

    // One full cycle every 10 seconds
    private static final float CYCLE_MS = 10000.0f;

    public static int get(int alpha) {
        float hue = (System.currentTimeMillis() % (long) CYCLE_MS) / CYCLE_MS;
        int i = (int)(hue * 6);
        float f = hue * 6 - i;
        float q = 1 - f;
        int r, g, b;
        switch (i % 6) {
            case 0 -> { r = 255; g = (int)(f*255); b = 0; }
            case 1 -> { r = (int)(q*255); g = 255; b = 0; }
            case 2 -> { r = 0; g = 255; b = (int)(f*255); }
            case 3 -> { r = 0; g = (int)(q*255); b = 255; }
            case 4 -> { r = (int)(f*255); g = 0; b = 255; }
            default-> { r = 255; g = 0; b = (int)(q*255); }
        }
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }
}