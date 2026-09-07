package com.clean.keystrokes.display.util;

import net.minecraft.client.MinecraftClient;

public class MouseTracker {

    private static final int TRAIL_CAPACITY = 100;
    private static final int MAX_SAMPLES_PER_UPDATE = 24;
    private static final double TRAIL_SAMPLE_SPACING_PX = 1.0;
    private static final double MIN_SAMPLE_DISTANCE_PX = 0.01;

    private static double smoothX = 0;
    private static double smoothY = 0;

    private static double renderPxX = -1;
    private static double renderPxY = -1;

    private static double centerPxX = 0;
    private static double centerPxY = 0;

    private static final double[] trailPxX = new double[TRAIL_CAPACITY];
    private static final double[] trailPxY = new double[TRAIL_CAPACITY];
    private static final long[] trailTimeMs = new long[TRAIL_CAPACITY];
    private static int trailHead = -1;
    private static int trailCount = 0;
    private static boolean initialized = false;

    private static double pendingDx = 0;
    private static double pendingDy = 0;
    private static double aspectRatio = 1.0;

    // ── Tuneable constants ────────────────────────────────────────
    private static final double SENSITIVITY = 0.02;
    private static final double DECAY = 0.8;
    private static final float LERP_SPEED = 0.5f;

    public static void onMouseMove(double dx, double dy) {
        pendingDx += dx;
        pendingDy += dy;
    }

    public static void tick(float delta) {
        double adjDecay = Math.pow(DECAY, delta);

        smoothX = smoothX * adjDecay + pendingDx * SENSITIVITY;
        smoothY = smoothY * adjDecay + pendingDy * SENSITIVITY * aspectRatio;

        smoothX = Math.max(-1.0, Math.min(1.0, smoothX));
        smoothY = Math.max(-1.0, Math.min(1.0, smoothY));

        if (Math.abs(smoothX) < 0.001) smoothX = 0;
        if (Math.abs(smoothY) < 0.001) smoothY = 0;

        pendingDx = 0;
        pendingDy = 0;
    }

    public static void updateRenderPos(int areaX, int areaY, int areaW, int areaH, int dotSizeGui, float delta, long now) {
        double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
        double radiusX = (areaW - dotSizeGui) * scale / 2.0;
        double radiusY = (areaH - dotSizeGui) * scale / 2.0;

        aspectRatio = radiusY == 0.0 ? 1.0 : radiusX / radiusY;

        centerPxX = Math.floor((areaX + areaW / 2.0) * scale + 0.5);
        centerPxY = Math.floor((areaY + areaH / 2.0) * scale + 0.5);

        double targetPxX = Math.floor(centerPxX + smoothX * radiusX + 0.5);
        double targetPxY = Math.floor(centerPxY + smoothY * radiusY + 0.5);

        if (!initialized) {
            renderPxX = targetPxX;
            renderPxY = targetPxY;
            appendTrailSample(targetPxX, targetPxY, now);
            initialized = true;
            return;
        }

        double prevRenderPxX = renderPxX;
        double prevRenderPxY = renderPxY;

        float adjusted = 1.0f - (float) Math.pow(1.0f - LERP_SPEED, delta);
        renderPxX += (targetPxX - renderPxX) * adjusted;
        renderPxY += (targetPxY - renderPxY) * adjusted;

        if (Math.abs(renderPxX - targetPxX) < 0.5) renderPxX = targetPxX;
        if (Math.abs(renderPxY - targetPxY) < 0.5) renderPxY = targetPxY;

        double dx = renderPxX - prevRenderPxX;
        double dy = renderPxY - prevRenderPxY;
        double dist = Math.hypot(dx, dy);
        if (dist < MIN_SAMPLE_DISTANCE_PX) {
            return;
        }

        int steps = Math.max(1, Math.min(MAX_SAMPLES_PER_UPDATE,
                (int) Math.ceil(dist / TRAIL_SAMPLE_SPACING_PX)
        ));
        for (int s = 1; s <= steps; s++) {
            double t = (double) s / steps;
            appendTrailSample(
                    prevRenderPxX + dx * t,
                    prevRenderPxY + dy * t,
                    now
            );
        }
    }

    private static void appendTrailSample(double pxX, double pxY, long timeMs) {
        trailHead = (trailHead + 1) % TRAIL_CAPACITY;
        trailPxX[trailHead] = pxX;
        trailPxY[trailHead] = pxY;
        trailTimeMs[trailHead] = timeMs;
        if (trailCount < TRAIL_CAPACITY) {
            trailCount++;
        }
    }

    private static int trailIndex(int newestFirstIndex) {
        if (newestFirstIndex < 0 || newestFirstIndex >= trailCount) {
            throw new IndexOutOfBoundsException(
                    "Trail index: " + newestFirstIndex + ", size: " + trailCount
            );
        }
        int index = trailHead - newestFirstIndex;
        return index < 0 ? index + TRAIL_CAPACITY : index;
    }

    public static double getRenderPxX() { return renderPxX; }
    public static double getRenderPxY() { return renderPxY; }
    public static double getCenterPxX() { return centerPxX; }
    public static double getCenterPxY() { return centerPxY; }
    public static double getTrailPxX(int i) { return trailPxX[trailIndex(i)]; }
    public static double getTrailPxY(int i) { return trailPxY[trailIndex(i)]; }
    public static long getTrailTimeMs(int i) { return trailTimeMs[trailIndex(i)]; }
    public static int getTrailLength() { return trailCount; }
    public static int getTrailCapacity() { return TRAIL_CAPACITY; }
}
