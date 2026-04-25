package com.clean.keystrokes.display.hud;

import com.clean.keystrokes.display.util.MouseTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class HudRenderer {

    private HudRenderer() {}

    // Fade duration in ms
    private static final float TRAIL_FADE_MS = 300f;

    public static void drawTexture(DrawContext ctx, Identifier tex, int x, int y, int w, int h, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        setShaderColor(color);
        ctx.drawTexture(tex, x, y, w, h, 0f, 0f, 1, 1, 1, 1);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    public static void drawLabelKey(DrawContext ctx, int x, int y, int w, int h, String label, Identifier tex, int bgColor, int fgColor, boolean shadow, int shadowColor, boolean customShadowColor, double textScale) {
        drawTexture(ctx, tex, x, y, w, h, bgColor);
        var tr = MinecraftClient.getInstance().textRenderer;
        int textW = (int) Math.round(tr.getWidth(label) * textScale);
        int textH = (int) Math.round(tr.fontHeight * textScale);
        drawText(ctx, label, x + (w - textW + 1) / 2, y + (h - textH) / 2 + Math.max(1, (int) Math.round(textScale)), fgColor, shadow, shadowColor, customShadowColor, textScale);
    }

    public static void drawCenteredNumber(DrawContext ctx, int value, int x, int y, int w, int h, int color, boolean shadow, int shadowColor, boolean customShadowColor, double textScale) {
        var tr = MinecraftClient.getInstance().textRenderer;
        String str = String.valueOf(value);
        int textW = (int) Math.round(tr.getWidth(str) * textScale);
        int textH = (int) Math.round(tr.fontHeight * textScale);
        drawText(ctx, str, x + (w - textW + 1) / 2, y + (h - textH) / 2 + Math.max(1, (int) Math.round(textScale)), color, shadow, shadowColor, customShadowColor, textScale);
    }

    private static void drawText(DrawContext ctx, String text, int x, int y, int color, boolean shadow, int shadowColor, boolean customShadowColor, double textScale) {
        var tr = MinecraftClient.getInstance().textRenderer;
        Text component = Text.literal(text);

        if (Math.abs(textScale - 1.0) < 0.001) {
            if (!shadow) {
                ctx.drawText(tr, component, x, y, color, false);
                return;
            }
            if (!customShadowColor) {
                ctx.drawText(tr, component, x, y, color, true);
                return;
            }

            ctx.drawText(tr, component, x + 1, y + 1, shadowColor, false);
            ctx.drawText(tr, component, x, y, color, false);
            return;
        }

        float s = (float) textScale;

        ctx.getMatrices().push();
        ctx.getMatrices().translate((float) x, (float) y, 0.0f);
        ctx.getMatrices().scale(s, s, 1.0f);

        if (!shadow) {
            ctx.drawText(tr, component, 0, 0, color, false);
        } else if (!customShadowColor) {
            ctx.drawText(tr, component, 0, 0, color, true);
        } else {
            ctx.drawText(tr, component, 1, 1, shadowColor, false);
            ctx.drawText(tr, component, 0, 0, color, false);
        }

        ctx.getMatrices().pop();
    }

    public static void drawDotWithTrail(DrawContext ctx, int dotSize, int fgColor, boolean shadow, int shadowColor, boolean customShadowColor) {
        double scale    = MinecraftClient.getInstance().getWindow().getScaleFactor();
        int    trailLen = MouseTracker.getTrailLength();
        long   now      = System.currentTimeMillis();

        for (int i = trailLen - 1; i >= 1; i--) {
            long age       = now - MouseTracker.getTrailTimeMs(i);
            float timeFrac = 1f - Math.min(age / TRAIL_FADE_MS, 1f);
            float posFrac  = 1f - (float) i / trailLen;
            float frac     = timeFrac * posFrac;
            if (frac <= 0) continue;
            int trailColor = (((int)(frac * 0.1f * 255)) << 24) | (fgColor & 0x00FFFFFF);
            drawDotAtWithShadow(ctx, MouseTracker.getTrailPxX(i), MouseTracker.getTrailPxY(i), scale, dotSize, trailColor, shadow, shadowColor, customShadowColor);
        }

        // Static center dot at half opacity
        int centerColor = ((((fgColor >> 24) & 0xFF) / 2) << 24) | (fgColor & 0x00FFFFFF);
        drawDotAtWithShadow(ctx, MouseTracker.getCenterPxX(), MouseTracker.getCenterPxY(), scale, dotSize, centerColor, shadow, shadowColor, customShadowColor);

        // Main moving dot
        drawDotAtWithShadow(ctx, MouseTracker.getRenderPxX(), MouseTracker.getRenderPxY(), scale, dotSize, fgColor, shadow, shadowColor, customShadowColor);
    }

    private static void drawDotAtWithShadow(DrawContext ctx, double pxX, double pxY, double scale, int size, int color, boolean shadow, int shadowColor, boolean customShadowColor) {
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

    private static void drawDotAt(DrawContext ctx, double pxX, double pxY, double scale, int size, int color) {
        double sx = Math.round(pxX - size * scale / 2.0 - 0.5) / scale;
        double sy = Math.round(pxY - size * scale / 2.0 - 0.5) / scale;
        int    bx = (int) Math.floor(sx);
        int    by = (int) Math.floor(sy);

        float fx = (float) (sx - bx);
        float fy = (float) (sy - by);

        ctx.getMatrices().push();
        ctx.getMatrices().translate(fx, fy, 0.0f);
        drawTexture(ctx, HudTextures.DOT, bx, by, size, size, color);
        ctx.getMatrices().pop();
    }

    private static void setShaderColor(int color) {
        float a = ((color >>> 24) & 0xFF) / 255.0f;
        float r = ((color >>> 16) & 0xFF) / 255.0f;
        float g = ((color >>> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        RenderSystem.setShaderColor(r, g, b, a);
    }
}