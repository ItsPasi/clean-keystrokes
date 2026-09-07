package com.clean.keystrokes.display.util;

public final class RainbowColor {

    private static final float CYCLE_MS = 10000.0f;
    private static final double TWO_PI = Math.PI * 2.0;

    private RainbowColor() {}

    public static int getRgbShifted(long nowMs, float shift, double speed) {
        double phase = ((nowMs / (double) CYCLE_MS) * Math.max(0.05, speed)) % 1.0;
        phase = (phase + shift) % 1.0;
        if (phase < 0.0) phase += 1.0;

        int r = wave(phase, 0.0f);
        int g = wave(phase, 1.0f / 3.0f);
        int b = wave(phase, 2.0f / 3.0f);
        return (r << 16) | (g << 8) | b;
    }

    public static int withAlpha(int rgb, int alpha) {
        return ((alpha & 0xFF) << 24) | (rgb & 0x00FFFFFF);
    }

    private static int wave(double phase, float offset) {
        double value = Math.sin((phase + offset) * TWO_PI) * 0.5 + 0.5;
        return Math.max(0, Math.min(255, (int) Math.round(value * 255.0)));
    }
}
