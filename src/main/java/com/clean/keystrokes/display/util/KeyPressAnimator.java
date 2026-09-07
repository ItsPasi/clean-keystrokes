package com.clean.keystrokes.display.util;

public final class KeyPressAnimator {

    public static final int W = 0;
    public static final int A = 1;
    public static final int S = 2;
    public static final int D = 3;
    public static final int SPACE = 4;
    public static final int SNEAK = 5;
    public static final int SPRINT = 6;
    public static final int LMB = 7;
    public static final int RMB = 8;

    private static final float PRESS_SPEED = 0.7f;
    private static final float RELEASE_SPEED = 0.6f;
    private static final float[] STATES = new float[9];

    private static int cachedDeltaBits = Integer.MIN_VALUE;
    private static float pressFactor;
    private static float releaseFactor;

    private KeyPressAnimator() {}

    public static float update(int key, boolean pressed, float delta) {
        updateFactors(delta);

        float target = pressed ? 1f : 0f;
        float next = STATES[key] + (target - STATES[key]) * (pressed ? pressFactor : releaseFactor);
        if (Math.abs(next - target) < 0.005f) next = target;

        return STATES[key] = next;
    }

    private static void updateFactors(float delta) {
        int bits = Float.floatToRawIntBits(delta);
        if (bits == cachedDeltaBits) return;

        cachedDeltaBits = bits;
        pressFactor = 1f - (float) Math.pow(1f - PRESS_SPEED, delta);
        releaseFactor = 1f - (float) Math.pow(1f - RELEASE_SPEED, delta);
    }

    public static int blendColor(int a, int b, float t) {
        int aA = (a >> 24) & 0xFF, rA = (a >> 16) & 0xFF, gA = (a >> 8) & 0xFF, bA = a & 0xFF;
        int aB = (b >> 24) & 0xFF, rB = (b >> 16) & 0xFF, gB = (b >> 8) & 0xFF, bB = b & 0xFF;
        return ((int)(aA + (aB - aA) * t) << 24) | ((int)(rA + (rB - rA) * t) << 16)
                | ((int)(gA + (gB - gA) * t) << 8) | (int)(bA + (bB - bA) * t);
    }
}
