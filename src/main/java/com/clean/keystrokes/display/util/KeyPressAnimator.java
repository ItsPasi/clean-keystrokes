package com.clean.keystrokes.display.util;

import java.util.HashMap;
import java.util.Map;

public final class KeyPressAnimator {

    public static final float PRESS_SPEED   = 0.7f;
    public static final float RELEASE_SPEED = 0.6f;

    private static final Map<String, Float> states = new HashMap<>();

    public static float update(String key, boolean pressed, float delta) {
        float current = states.getOrDefault(key, 0f);
        float target  = pressed ? 1f : 0f;
        float speed   = pressed ? PRESS_SPEED : RELEASE_SPEED;
        float next    = current + (target - current) * (1f - (float) Math.pow(1f - speed, delta));
        if (Math.abs(next - target) < 0.005f) next = target;
        states.put(key, next);
        return next;
    }

    public static int blendColor(int a, int b, float t) {
        int aA = (a >> 24) & 0xFF, rA = (a >> 16) & 0xFF, gA = (a >> 8) & 0xFF, bA = a & 0xFF;
        int aB = (b >> 24) & 0xFF, rB = (b >> 16) & 0xFF, gB = (b >> 8) & 0xFF, bB = b & 0xFF;
        return ((int)(aA + (aB - aA) * t) << 24) | ((int)(rA + (rB - rA) * t) << 16)
                | ((int)(gA + (gB - gA) * t) << 8)  |  (int)(bA + (bB - bA) * t);
    }
}