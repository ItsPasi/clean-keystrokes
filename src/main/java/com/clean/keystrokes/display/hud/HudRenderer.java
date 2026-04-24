package com.clean.keystrokes.display.hud;

import com.clean.keystrokes.display.util.MouseTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class HudRenderer {

    private HudRenderer() {}

    // Fade duration in ms
    private static final float TRAIL_FADE_MS = 300f;

    public static void drawTexture(GuiGraphicsExtractor ctx, Identifier tex,
                                   int x, int y, int w, int h, int color) {
        ctx.blit(RenderPipelines.GUI_TEXTURED, tex,
                x, y, 0f, 0f, w, h, 1, 1, 1, 1, color);
    }

    public static void drawLabelKey(GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, Identifier tex, int bgColor, int fgColor, boolean shadow, int shadowColor, boolean customShadowColor) {
        drawTexture(ctx, tex, x, y, w, h, bgColor);
        var tr = Minecraft.getInstance().font;
        drawText(ctx, label, x + (w - tr.width(label) + 1) / 2, y + (int) Math.round((h - tr.lineHeight) / 2.0) + 1, fgColor, shadow, shadowColor, customShadowColor);
    }

    public static void drawCenteredNumber(GuiGraphicsExtractor ctx, int value, int x, int y, int w, int h, int color, boolean shadow, int shadowColor, boolean customShadowColor) {
        var tr = Minecraft.getInstance().font;
        String str = String.valueOf(value);
        drawText(ctx, str, x + (w - tr.width(str) + 1) / 2, y + (int) Math.round((h - tr.lineHeight) / 2.0) + 1, color, shadow, shadowColor, customShadowColor);
    }

    private static void drawText(GuiGraphicsExtractor ctx, String text, int x, int y, int color, boolean shadow, int shadowColor, boolean customShadowColor) {
        var tr = Minecraft.getInstance().font;
        Component component = Component.literal(text);

        if (!shadow) {
            ctx.text(tr, component, x, y, color, false);
            return;
        }
        if (!customShadowColor) {
            ctx.text(tr, component, x, y, color, true);
            return;
        }

        ctx.text(tr, component, x + 1, y + 1, shadowColor, false);
        ctx.text(tr, component, x, y, color, false);
    }

    public static void drawDotWithTrail(GuiGraphicsExtractor ctx, int dotSize, int fgColor, boolean shadow, int shadowColor, boolean customShadowColor) {
        double scale    = Minecraft.getInstance().getWindow().getGuiScale();
        int    trailLen = MouseTracker.getTrailLength();
        long   now      = System.currentTimeMillis();

        for (int i = trailLen - 1; i >= 1; i--) {
            long age   = now - MouseTracker.getTrailTimeMs(i);
            float timeFrac = 1f - Math.min(age / TRAIL_FADE_MS, 1f);
            float posFrac  = 1f - (float) i / trailLen;
            float frac     = timeFrac * posFrac; // both time AND position affect fade
            if (frac <= 0) continue;
            int trailColor = (((int)(frac * 0.1f * 255)) << 24) | (fgColor & 0x00FFFFFF);
            drawDotAtWithShadow(ctx, MouseTracker.getTrailPxX(i), MouseTracker.getTrailPxY(i),
                    scale, dotSize, trailColor, shadow, shadowColor, customShadowColor);
        }

        // Static center dot at half opacity
        int centerColor = ((((fgColor >> 24) & 0xFF) / 2) << 24) | (fgColor & 0x00FFFFFF);
        drawDotAtWithShadow(ctx, MouseTracker.getCenterPxX(), MouseTracker.getCenterPxY(),
                scale, dotSize, centerColor, shadow, shadowColor, customShadowColor);

        // Main moving dot
        drawDotAtWithShadow(ctx, MouseTracker.getRenderPxX(), MouseTracker.getRenderPxY(),
                scale, dotSize, fgColor, shadow, shadowColor, customShadowColor);
    }

    private static void drawDotAtWithShadow(GuiGraphicsExtractor ctx, double pxX, double pxY,
                                             double scale, int size, int color,
                                             boolean shadow, int shadowColor, boolean customShadowColor) {
        if (shadow) {
            int alpha = (color >>> 24) & 0xFF;
            int resolvedShadowColor = customShadowColor
                    ? withAlpha(shadowColor, multiplyAlpha(shadowColor, alpha))
                    : defaultShadowColor(color);
            drawDotAt(ctx, pxX + scale, pxY + scale, scale, size, resolvedShadowColor);
        }
        drawDotAt(ctx, pxX, pxY, scale, size, color);
    }

    private static int defaultShadowColor(int color) {
        int alpha = (color >>> 24) & 0xFF;
        int r = ((color >> 16) & 0xFF) / 4;
        int g = ((color >> 8) & 0xFF) / 4;
        int b = (color & 0xFF) / 4;
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    private static int multiplyAlpha(int color, int alpha) {
        int colorAlpha = (color >>> 24) & 0xFF;
        return colorAlpha * alpha / 255;
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    private static void drawDotAt(GuiGraphicsExtractor ctx, double pxX, double pxY,
                                  double scale, int size, int color) {
        double sx = Math.round(pxX - size * scale / 2.0 - 0.5) / scale;
        double sy = Math.round(pxY - size * scale / 2.0 - 0.5) / scale;
        int    bx = (int) Math.floor(sx);
        int    by = (int) Math.floor(sy);
        float  fx = (float)(sx - bx), fy = (float)(sy - by);
        ctx.pose().translate(fx, fy);
        ctx.blit(RenderPipelines.GUI_TEXTURED, HudTextures.DOT,
                bx, by, 0f, 0f, size, size, 1, 1, 1, 1, color);
        ctx.pose().translate(-fx, -fy);
    }
}