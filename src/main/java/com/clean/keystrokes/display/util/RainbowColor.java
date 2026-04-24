package com.clean.keystrokes.display.util;

public final class RainbowColor {

    private RainbowColor() {}

    // One full cycle every 10 seconds at 1.0x speed
    private static final float CYCLE_MS = 10000.0f;
    private static final double TWO_PI = Math.PI * 2.0;

    public static int get(int alpha, double speed) {
        return getShifted(alpha, 0.0f, speed);
    }

    public static int getOpposite(int alpha, double speed) {
        return getShifted(alpha, 0.5f, speed);
    }

    public static int getShifted(int alpha, float shift, double speed) {
        double safeSpeed = Math.max(0.05, speed);
        double phase = ((System.currentTimeMillis() / (double) CYCLE_MS) * safeSpeed) % 1.0;
        phase = (phase + shift) % 1.0;
        if (phase < 0.0) {
            phase += 1.0;
        }

        int r = wave(phase, 0.0f);
        int g = wave(phase, 1.0f / 3.0f);
        int b = wave(phase, 2.0f / 3.0f);
        return ((alpha & 0xFF) << 24) | (r << 16) | (g << 8) | b;
    }

    private static int wave(double phase, float offset) {
        double value = Math.sin((phase + offset) * TWO_PI) * 0.5 + 0.5;
        return clampToByte((int)Math.round(value * 255.0));
    }

    private static int clampToByte(int value) {
        if (value < 0) return 0;
        return Math.min(value, 255);
    }
}