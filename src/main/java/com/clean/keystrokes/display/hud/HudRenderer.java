package com.clean.keystrokes.display.hud;

import com.clean.keystrokes.display.util.MouseTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class HudRenderer {

    private HudRenderer() {}

    // Fade duration in ms — tune here
    private static final float TRAIL_FADE_MS = 300f;

    public static void drawTexture(DrawContext ctx, Identifier tex,
                                   int x, int y, int w, int h, int color) {
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, tex,
                x, y, 0f, 0f, w, h, 1, 1, 1, 1, color);
    }

    public static void drawLabelKey(DrawContext ctx, int x, int y, int w, int h,
                                    String label, Identifier tex,
                                    int bgColor, int fgColor, boolean shadow) {
        drawTexture(ctx, tex, x, y, w, h, bgColor);
        var tr = MinecraftClient.getInstance().textRenderer;
        ctx.drawText(tr, Text.literal(label),
                x + (w - tr.getWidth(label) + 1) / 2,
                y + (int) Math.round((h - tr.fontHeight) / 2.0) + 1,
                fgColor, shadow);
    }

    public static void drawCenteredNumber(DrawContext ctx, int value,
                                          int x, int y, int w, int h,
                                          int color, boolean shadow) {
        var tr = MinecraftClient.getInstance().textRenderer;
        String str = String.valueOf(value);
        ctx.drawText(tr, Text.literal(str),
                x + (w - tr.getWidth(str) + 1) / 2,
                y + (int) Math.round((h - tr.fontHeight) / 2.0) + 1,
                color, shadow);
    }

    public static void drawDotWithTrail(DrawContext ctx, int dotSize, int fgColor) {
        double scale    = MinecraftClient.getInstance().getWindow().getScaleFactor();
        int    trailLen = MouseTracker.getTrailLength();
        long   now      = System.currentTimeMillis();

        int sourceAlpha = (fgColor >>> 24) & 0xFF;

        for (int i = trailLen - 1; i >= 1; i--) {
            long age   = now - MouseTracker.getTrailTimeMs(i);
            float timeFrac = 1f - Math.min(age / TRAIL_FADE_MS, 1f);
            float posFrac  = 1f - (float) i / trailLen;
            float frac     = timeFrac * posFrac; // both time AND position affect fade
            if (frac <= 0 || sourceAlpha == 0) continue;

            int trailAlpha = Math.clamp(Math.round(sourceAlpha * 0.1f * frac), 0, 255);
            drawDotAt(ctx, MouseTracker.getTrailPxX(i), MouseTracker.getTrailPxY(i),
                    scale, dotSize, (trailAlpha << 24) | (fgColor & 0x00FFFFFF));
        }

        // Static center dot at half opacity
        int centerColor = ((((fgColor >> 24) & 0xFF) / 2) << 24) | (fgColor & 0x00FFFFFF);
        drawDotAt(ctx, MouseTracker.getCenterPxX(), MouseTracker.getCenterPxY(),
                scale, dotSize, centerColor);

        // Main moving dot
        drawDotAt(ctx, MouseTracker.getRenderPxX(), MouseTracker.getRenderPxY(),
                scale, dotSize, fgColor);
    }

    private static void drawDotAt(DrawContext ctx, double pxX, double pxY,
                                  double scale, int size, int color) {
        double sx = Math.round(pxX - size * scale / 2.0 - 0.5) / scale;
        double sy = Math.round(pxY - size * scale / 2.0 - 0.5) / scale;
        int    bx = (int) Math.floor(sx);
        int    by = (int) Math.floor(sy);
        float  fx = (float)(sx - bx), fy = (float)(sy - by);
        ctx.getMatrices().translate(fx, fy);
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, HudTextures.DOT,
                bx, by, 0f, 0f, size, size, 1, 1, 1, 1, color);
        ctx.getMatrices().translate(-fx, -fy);
    }
}