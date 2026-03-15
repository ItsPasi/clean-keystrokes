package com.clean.keystrokes.display.hud;

import com.clean.keystrokes.display.config.KeystrokeConfig;
import com.clean.keystrokes.display.util.CpsCounter;
import com.clean.keystrokes.display.util.KeyPressAnimator;
import com.clean.keystrokes.display.util.MouseTracker;
import com.clean.keystrokes.display.util.RainbowColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeystrokeHud {

    public static final CpsCounter lmbCps = new CpsCounter();
    public static final CpsCounter rmbCps = new CpsCounter();

    public void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getDebugHud().shouldShowDebugHud()) return;

        KeystrokeConfig cfg  = KeystrokeConfig.get();
        HudLayout lay        = new HudLayout(cfg);
        int kS               = HudLayout.KEY_SIZE;
        int hH               = HudLayout.HALF_HEIGHT;
        float delta          = tickCounter.getFixedDeltaTicks();
        boolean shadow       = cfg.textShadow;
        boolean anim         = cfg.pressAnimation;
        boolean showInputs   = client.currentScreen == null || client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen;
        boolean showClicks   = client.currentScreen == null;
        int rainbowColor     = cfg.rainbowText ? RainbowColor.get((cfg.keyColor >> 24) & 0xFF) : 0;

        MouseTracker.tick(delta);
        MouseTracker.updateRenderPos(lay.stripCenterX, lay.rowMouse,
                lay.stripCenterW, hH, HudLayout.DOT_SIZE, delta);

        long win = client.getWindow().getHandle();

        // WASD
        drawMovementKey(ctx, cfg, lay.col1, lay.rowW,   kS, kS,
                client.options.forwardKey, HudTextures.KEY_W, anim, delta, shadow, cfg.rainbowText, rainbowColor, showInputs);
        drawMovementKey(ctx, cfg, lay.col0, lay.rowASD, kS, kS,
                client.options.leftKey,    HudTextures.KEY_A, anim, delta, shadow, cfg.rainbowText, rainbowColor, showInputs);
        drawMovementKey(ctx, cfg, lay.col1, lay.rowASD, kS, kS,
                client.options.backKey,    HudTextures.KEY_S, anim, delta, shadow, cfg.rainbowText, rainbowColor, showInputs);
        drawMovementKey(ctx, cfg, lay.col2, lay.rowASD, kS, kS,
                client.options.rightKey,   HudTextures.KEY_D, anim, delta, shadow, cfg.rainbowText, rainbowColor, showInputs);

        // SPACE
        drawKey(ctx, cfg, lay.spaceX, lay.rowSpace, lay.spaceW, hH,
                "—", HudTextures.KEY_SPACE, "SPACE", client.options.jumpKey,
                anim, delta, shadow, cfg.rainbowText, rainbowColor, showInputs);

        // SNK / SPR
        drawKey(ctx, cfg, lay.sneakX, lay.rowSneakSprint, lay.sneakW, hH,
                "SNK", HudTextures.KEY_SNK, "SNK", client.options.sneakKey,
                anim, delta, shadow, cfg.rainbowText, rainbowColor, showInputs);
        drawKey(ctx, cfg, lay.sprintX, lay.rowSneakSprint, lay.sprintW, hH,
                "SPR", HudTextures.KEY_SPR, "SPR", client.options.sprintKey,
                anim, delta, shadow, cfg.rainbowText, rainbowColor, showInputs);

        // Mouse row
        boolean lmbDown = showClicks && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT)  == GLFW.GLFW_PRESS;
        boolean rmbDown = showClicks && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        float lmbT = anim ? KeyPressAnimator.update("LMB", lmbDown, delta) : (lmbDown ? 1f : 0f);
        float rmbT = anim ? KeyPressAnimator.update("RMB", rmbDown, delta) : (rmbDown ? 1f : 0f);

        HudRenderer.drawTexture(ctx, HudTextures.KEY_LMB,
                lay.stripLmbX, lay.rowMouse, lay.stripLmbW, hH,
                KeyPressAnimator.blendColor(cfg.keyBackgroundColor, cfg.keyPressedBackgroundColor, lmbT));
        HudRenderer.drawCenteredNumber(ctx, showClicks ? lmbCps.getCps() : 0,
                lay.stripLmbX, lay.rowMouse, lay.stripLmbW, hH,
                resolveFg(cfg, lmbT, cfg.rainbowText, rainbowColor), shadow);

        HudRenderer.drawTexture(ctx, HudTextures.KEY_MOUSE_CENTER,
                lay.stripCenterX, lay.rowMouse, lay.stripCenterW, hH, cfg.keyBackgroundColor);
        HudRenderer.drawDotWithTrail(ctx, HudLayout.DOT_SIZE,
                cfg.rainbowText ? rainbowColor : cfg.keyColor);

        HudRenderer.drawTexture(ctx, HudTextures.KEY_RMB,
                lay.stripRmbX, lay.rowMouse, lay.stripRmbW, hH,
                KeyPressAnimator.blendColor(cfg.keyBackgroundColor, cfg.keyPressedBackgroundColor, rmbT));
        HudRenderer.drawCenteredNumber(ctx, showClicks ? rmbCps.getCps() : 0,
                lay.stripRmbX, lay.rowMouse, lay.stripRmbW, hH,
                resolveFg(cfg, rmbT, cfg.rainbowText, rainbowColor), shadow);
    }

    private void drawKey(DrawContext ctx, KeystrokeConfig cfg,
                         int x, int y, int w, int h,
                         String label, net.minecraft.util.Identifier tex,
                         String animKey, KeyBinding key,
                         boolean anim, float delta, boolean shadow,
                         boolean rainbow, int rainbowColor, boolean showInputs) {
        boolean pressed = showInputs && isKeyDown(key);
        float t  = anim ? KeyPressAnimator.update(animKey, pressed, delta) : (pressed ? 1f : 0f);
        int   bg = KeyPressAnimator.blendColor(cfg.keyBackgroundColor, cfg.keyPressedBackgroundColor, t);
        int   fg = resolveFg(cfg, t, rainbow, rainbowColor);
        HudRenderer.drawLabelKey(ctx, x, y, w, h, label, tex, bg, fg, shadow);
    }

    private void drawMovementKey(DrawContext ctx, KeystrokeConfig cfg,
                                 int x, int y, int w, int h,
                                 KeyBinding key, net.minecraft.util.Identifier tex,
                                 boolean anim, float delta, boolean shadow,
                                 boolean rainbow, int rainbowColor, boolean showInputs) {
        String label = getKeyLabel(key);
        drawKey(ctx, cfg, x, y, w, h, label, tex, label, key,
                anim, delta, shadow, rainbow, rainbowColor, showInputs);
    }

    private int resolveFg(KeystrokeConfig cfg, float t, boolean rainbow, int rainbowColor) {
        if (rainbow) return (rainbowColor & 0x00FFFFFF) | ((cfg.keyColor >> 24) & 0xFF) << 24;
        return KeyPressAnimator.blendColor(cfg.keyColor, cfg.keyPressedColor, t);
    }

    private String getKeyLabel(KeyBinding key) {
        int code = InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey()).getCode();
        return switch (code) {
            case GLFW.GLFW_KEY_SPACE                                     -> "SPC";
            case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT    -> "SNK";
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> "CTL";
            case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT        -> "ALT";
            case GLFW.GLFW_KEY_UP    -> "UP";
            case GLFW.GLFW_KEY_DOWN  -> "DN";
            case GLFW.GLFW_KEY_LEFT  -> "LFT";
            case GLFW.GLFW_KEY_RIGHT -> "RGT";
            default -> {
                String name = GLFW.glfwGetKeyName(code, 0);
                yield (name != null && !name.isEmpty()) ? name.toUpperCase() : "?";
            }
        };
    }

    private boolean isKeyDown(KeyBinding key) {
        int code = InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey()).getCode();
        if (code < 0) return false;
        long win = MinecraftClient.getInstance().getWindow().getHandle();
        return GLFW.glfwGetKey(win, code) == GLFW.GLFW_PRESS;
    }
}