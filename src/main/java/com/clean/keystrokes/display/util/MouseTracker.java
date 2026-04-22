package com.clean.keystrokes.display.util;

import net.minecraft.client.MinecraftClient;

public class MouseTracker {

    private static final int TRAIL_LENGTH = 100;

    private static double smoothX = 0;
    private static double smoothY = 0;

    private static double renderPxX = -1;
    private static double renderPxY = -1;

    private static double centerPxX = 0;
    private static double centerPxY = 0;

    private static final double[] trailPxX    = new double[TRAIL_LENGTH];
    private static final double[] trailPxY    = new double[TRAIL_LENGTH];
    private static final long[]   trailTimeMs = new long[TRAIL_LENGTH];
    private static boolean initialized = false;

    private static double pendingDx = 0;
    private static double pendingDy = 0;
    private static double aspectRatio = 1.0;

    // ── Tuneable constants ────────────────────────────────────────
    private static final double SENSITIVITY = 0.02;
    private static final double DECAY       = 0.8;
    private static final float  LERP_SPEED  = 0.5f;

    public static void onMouseMove(double dx, double dy) {
        pendingDx += dx;
        pendingDy += dy;
    }

    public static void tick(float delta) {
        double adjDecay = Math.pow(DECAY, delta);

        smoothX = smoothX * adjDecay + pendingDx * SENSITIVITY;
        smoothY = smoothY * adjDecay + pendingDy * SENSITIVITY * aspectRatio;

        smoothX = Math.clamp(smoothX, -1.0, 1.0);
        smoothY = Math.clamp(smoothY, -1.0, 1.0);

        if (Math.abs(smoothX) < 0.001) smoothX = 0;
        if (Math.abs(smoothY) < 0.001) smoothY = 0;

        pendingDx = 0;
        pendingDy = 0;
    }

    public static void updateRenderPos(int areaX, int areaY, int areaW, int areaH, int dotSizeGui, float delta) {
        double scale   = MinecraftClient.getInstance().getWindow().getScaleFactor();
        double radiusX = (areaW - dotSizeGui) * scale / 2.0;
        double radiusY = (areaH - dotSizeGui) * scale / 2.0;

        aspectRatio = radiusX / radiusY;

        centerPxX = Math.floor((areaX + areaW / 2.0) * scale + 0.5);
        centerPxY = Math.floor((areaY + areaH / 2.0) * scale + 0.5);

        double targetPxX = Math.floor(centerPxX + smoothX * radiusX + 0.5);
        double targetPxY = Math.floor(centerPxY + smoothY * radiusY + 0.5);

        double prevRenderPxX;
        double prevRenderPxY;
        if (!initialized) {
            renderPxX     = targetPxX;
            renderPxY     = targetPxY;
            long now = System.currentTimeMillis();
            for (int i = 0; i < TRAIL_LENGTH; i++) {
                trailPxX[i]    = targetPxX;
                trailPxY[i]    = targetPxY;
                trailTimeMs[i] = now;
            }
            initialized = true;
            return;
        }

        prevRenderPxX = renderPxX;
        prevRenderPxY = renderPxY;

        float adjusted = 1.0f - (float) Math.pow(1.0f - LERP_SPEED, delta);
        renderPxX += (targetPxX - renderPxX) * adjusted;
        renderPxY += (targetPxY - renderPxY) * adjusted;

        if (Math.abs(renderPxX - targetPxX) < 0.5) renderPxX = targetPxX;
        if (Math.abs(renderPxY - targetPxY) < 0.5) renderPxY = targetPxY;

        double dx   = renderPxX - prevRenderPxX;
        double dy   = renderPxY - prevRenderPxY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        int steps   = Math.max(1, (int) Math.ceil(dist));
        long now    = System.currentTimeMillis();

        for (int s = steps - 1; s >= 0; s--) {
            double t  = (double) s / steps;
            double px = prevRenderPxX + dx * (1.0 - t);
            double py = prevRenderPxY + dy * (1.0 - t);
            for (int i = TRAIL_LENGTH - 1; i > 0; i--) {
                trailPxX[i]    = trailPxX[i - 1];
                trailPxY[i]    = trailPxY[i - 1];
                trailTimeMs[i] = trailTimeMs[i - 1];
            }
            trailPxX[0]    = px;
            trailPxY[0]    = py;
            trailTimeMs[0] = now;
        }
    }

    public static double getRenderPxX()        { return renderPxX; }
    public static double getRenderPxY()        { return renderPxY; }
    public static double getCenterPxX()        { return centerPxX; }
    public static double getCenterPxY()        { return centerPxY; }
    public static double getTrailPxX(int i)    { return trailPxX[i]; }
    public static double getTrailPxY(int i)    { return trailPxY[i]; }
    public static long   getTrailTimeMs(int i) { return trailTimeMs[i]; }
    public static int    getTrailLength()      { return TRAIL_LENGTH; }
}