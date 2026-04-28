package com.clean.keystrokes.display.hud;

import com.clean.keystrokes.CleanKeyStrokes;
import com.clean.keystrokes.display.config.KeystrokeConfig;
import com.clean.keystrokes.display.util.CpsCounter;
import com.clean.keystrokes.display.util.KeyPressAnimator;
import com.clean.keystrokes.display.util.MouseTracker;
import com.clean.keystrokes.display.util.RainbowColor;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public class KeystrokeHud {

    public static final CpsCounter lmbCps = new CpsCounter();
    public static final CpsCounter rmbCps = new CpsCounter();

    private static boolean loggedHudHiddenSkip;
    private static boolean loggedFirstRender;

    private long lastSyncedKeyPressTick = Long.MIN_VALUE;
    private final java.util.IdentityHashMap<KeyBinding, Boolean> tickSyncedKeyStates = new java.util.IdentityHashMap<>();

    private boolean shouldHideCleanKeystrokesForDebug(MinecraftClient client) {
        return client.getDebugHud().shouldShowDebugHud() && !client.getEntityRenderDispatcher().shouldRenderHitboxes();
    }

    public void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || shouldHideCleanKeystrokesForDebug(client)) return;

        if (client.options.hudHidden) {
            if (!loggedHudHiddenSkip) {
                CleanKeyStrokes.LOGGER.info("Skipping Clean Keystrokes HUD render because hudHidden=true.");
                loggedHudHiddenSkip = true;
            }
            return;
        }

        KeystrokeConfig cfg  = KeystrokeConfig.get();
        HudLayout lay        = new HudLayout(cfg);
        int kS               = lay.keySize;
        int hH               = lay.halfHeight;
        float delta          = tickCounter.getFixedDeltaTicks();
        boolean anim         = cfg.pressAnimation;
        boolean showInputs   = client.currentScreen == null || client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen;
        boolean showClicks   = client.currentScreen == null;

        int rainbowFgIdle = cfg.rainbowKeyNormal
                ? RainbowColor.get((cfg.keyColor >>> 24) & 0xFF, cfg.rainbowSpeed)
                : 0;
        int rainbowFgPressed = cfg.rainbowKeyPressed
                ? RainbowColor.getOpposite((cfg.keyPressedColor >>> 24) & 0xFF, cfg.rainbowSpeed)
                : 0;
        int rainbowBgIdle = cfg.rainbowBackgroundNormal
                ? RainbowColor.getOpposite((cfg.keyBackgroundColor >>> 24) & 0xFF, cfg.rainbowSpeed)
                : 0;
        int rainbowBgPressed = cfg.rainbowBackgroundPressed
                ? RainbowColor.get((cfg.keyPressedBackgroundColor >>> 24) & 0xFF, cfg.rainbowSpeed)
                : 0;
        int rainbowShadowIdle = cfg.rainbowKeyTextShadow
                ? RainbowColor.getShifted((cfg.keyTextShadowColor >>> 24) & 0xFF, 0.25f, cfg.rainbowSpeed)
                : 0;
        int rainbowShadowPressed = cfg.rainbowKeyPressedTextShadow
                ? RainbowColor.getShifted((cfg.keyPressedTextShadowColor >>> 24) & 0xFF, 0.75f, cfg.rainbowSpeed)
                : 0;

        if (cfg.tickSyncedKeyPresses) {
            syncTickSyncedKeyStates(client);
        }

        logFirstRender(client, cfg, lay, showInputs, showClicks);

        MouseTracker.tick(delta);
        MouseTracker.updateRenderPos(
                lay.stripCenterX,
                lay.rowMouse,
                lay.stripCenterW,
                hH,
                lay.dotSize,
                delta
        );

        long win = client.getWindow().getHandle();

        // WASD
        drawMovementKey(ctx, cfg, lay.col1, lay.rowW, kS, kS, client.options.forwardKey, HudTextures.KEY_W, anim, delta, rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, rainbowShadowIdle, rainbowShadowPressed, showInputs);
        drawMovementKey(ctx, cfg, lay.col0, lay.rowASD, kS, kS, client.options.leftKey, HudTextures.KEY_A, anim, delta, rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, rainbowShadowIdle, rainbowShadowPressed, showInputs);
        drawMovementKey(ctx, cfg, lay.col1, lay.rowASD, kS, kS, client.options.backKey, HudTextures.KEY_S, anim, delta, rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, rainbowShadowIdle, rainbowShadowPressed, showInputs);
        drawMovementKey(ctx, cfg, lay.col2, lay.rowASD, kS, kS, client.options.rightKey, HudTextures.KEY_D, anim, delta, rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, rainbowShadowIdle, rainbowShadowPressed, showInputs);

        // SPACE
        drawKey(ctx, cfg, lay.spaceX, lay.rowSpace, lay.spaceW, hH, "—", HudTextures.KEY_SPACE, "SPACE", client.options.jumpKey, anim, delta, rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, rainbowShadowIdle, rainbowShadowPressed, showInputs);
        if (cfg.showSneakSprintRow) {
            drawKey(ctx, cfg, lay.sneakX, lay.rowSneakSprint, lay.sneakW, hH, "SNK", HudTextures.KEY_SNK, "SNK", client.options.sneakKey, anim, delta, rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, rainbowShadowIdle, rainbowShadowPressed, showInputs);
            drawKey(ctx, cfg, lay.sprintX, lay.rowSneakSprint, lay.sprintW, hH, "SPR", HudTextures.KEY_SPR, "SPR", client.options.sprintKey, anim, delta, rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, rainbowShadowIdle, rainbowShadowPressed, showInputs);
        }

        // Mouse Row
        boolean lmbDown = showClicks && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT)  == GLFW.GLFW_PRESS;
        boolean rmbDown = showClicks && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        float lmbT = anim ? KeyPressAnimator.update("LMB", lmbDown, delta) : (lmbDown ? 1f : 0f);
        float rmbT = anim ? KeyPressAnimator.update("RMB", rmbDown, delta) : (rmbDown ? 1f : 0f);

        HudRenderer.drawTexture(ctx, HudTextures.KEY_LMB, lay.stripLmbX, lay.rowMouse, lay.stripLmbW, hH, resolveBg(cfg, lmbT, rainbowBgIdle, rainbowBgPressed));
        HudRenderer.drawCenteredNumber(ctx, showClicks ? lmbCps.getCps() : 0, lay.stripLmbX, lay.rowMouse, lay.stripLmbW, hH, resolveFg(cfg, lmbT, rainbowFgIdle, rainbowFgPressed), resolveTextShadow(cfg, lmbT), resolveTextShadowColor(cfg, lmbT, rainbowShadowIdle, rainbowShadowPressed), resolveTextShadowUsesCustomColor(cfg, lmbT), lay.scale);
        HudRenderer.drawTexture(ctx, HudTextures.KEY_MOUSE_CENTER, lay.stripCenterX, lay.rowMouse, lay.stripCenterW, hH, resolveIdleBg(cfg, rainbowBgIdle));
        HudRenderer.drawDotWithTrail(ctx, lay.dotSize, resolveIdleFg(cfg, rainbowFgIdle), resolveTextShadow(cfg, 0.0f), resolveTextShadowColor(cfg, 0.0f, rainbowShadowIdle, rainbowShadowPressed), resolveTextShadowUsesCustomColor(cfg, 0.0f));
        HudRenderer.drawTexture(ctx, HudTextures.KEY_RMB, lay.stripRmbX, lay.rowMouse, lay.stripRmbW, hH, resolveBg(cfg, rmbT, rainbowBgIdle, rainbowBgPressed));
        HudRenderer.drawCenteredNumber(ctx, showClicks ? rmbCps.getCps() : 0, lay.stripRmbX, lay.rowMouse, lay.stripRmbW, hH, resolveFg(cfg, rmbT, rainbowFgIdle, rainbowFgPressed), resolveTextShadow(cfg, rmbT), resolveTextShadowColor(cfg, rmbT, rainbowShadowIdle, rainbowShadowPressed), resolveTextShadowUsesCustomColor(cfg, rmbT), lay.scale);
    }

    private void logFirstRender(MinecraftClient client, KeystrokeConfig cfg, HudLayout lay,
                                boolean showInputs, boolean showClicks) {
        if (loggedFirstRender) {
            return;
        }
        loggedFirstRender = true;

        String screenName = client.currentScreen == null ? "null" : client.currentScreen.getClass().getName();
        CleanKeyStrokes.LOGGER.info(
                "First Clean Keystrokes HUD render reached. scaled={}x{}, hudScale={}, position={}, origin=({}, {}), showInputs={}, showClicks={}, hudHidden={}, debugHud={}, screen={}, configPath='{}', showSneakSprintRow={}, tickSyncedKeyPresses={}, rainbowKeyNormal={}, rainbowKeyPressed={}, rainbowBackgroundNormal={}, rainbowBackgroundPressed={}, rainbowKeyTextShadow={}, rainbowKeyPressedTextShadow={}, keyTextShadow={}, keyPressedTextShadow={}, useCustomTextShadowColor={}, preset={}, colors={key={}, bg={}, pressedKey={}, pressedBg={}, shadow={}, pressedShadow={}}",
                client.getWindow().getScaledWidth(),
                client.getWindow().getScaledHeight(),
                cfg.hudScale,
                cfg.position,
                lay.originX,
                lay.originY,
                showInputs,
                showClicks,
                client.options.hudHidden,
                client.getDebugHud().shouldShowDebugHud(),
                screenName,
                KeystrokeConfig.getPath(),
                cfg.showSneakSprintRow,
                cfg.tickSyncedKeyPresses,
                cfg.rainbowKeyNormal,
                cfg.rainbowKeyPressed,
                cfg.rainbowBackgroundNormal,
                cfg.rainbowBackgroundPressed,
                cfg.rainbowKeyTextShadow,
                cfg.rainbowKeyPressedTextShadow,
                cfg.keyTextShadow,
                cfg.keyPressedTextShadow,
                cfg.useCustomTextShadowColor,
                cfg.colorPreset,
                toArgbHex(cfg.keyColor),
                toArgbHex(cfg.keyBackgroundColor),
                toArgbHex(cfg.keyPressedColor),
                toArgbHex(cfg.keyPressedBackgroundColor),
                toArgbHex(cfg.keyTextShadowColor),
                toArgbHex(cfg.keyPressedTextShadowColor)
        );
    }

    private String toArgbHex(int color) {
        return String.format("0x%08X", color);
    }

    private void drawKey(DrawContext ctx, KeystrokeConfig cfg,
                         int x, int y, int w, int h,
                         String label, net.minecraft.util.Identifier tex,
                         String animKey, KeyBinding key,
                         boolean anim, float delta,
                         int rainbowFgIdle, int rainbowFgPressed,
                         int rainbowBgIdle, int rainbowBgPressed,
                         int rainbowShadowIdle, int rainbowShadowPressed,
                         boolean showInputs) {
        boolean pressed = showInputs && isKeyDown(key, cfg);
        float t = anim ? KeyPressAnimator.update(animKey, pressed, delta) : (pressed ? 1f : 0f);

        int bg = resolveBg(cfg, t, rainbowBgIdle, rainbowBgPressed);
        int fg = resolveFg(cfg, t, rainbowFgIdle, rainbowFgPressed);

        HudRenderer.drawLabelKey(ctx, x, y, w, h, label, tex, bg, fg, resolveTextShadow(cfg, t), resolveTextShadowColor(cfg, t, rainbowShadowIdle, rainbowShadowPressed), resolveTextShadowUsesCustomColor(cfg, t), cfg.hudScale);
    }

    private void drawMovementKey(DrawContext ctx, KeystrokeConfig cfg,
                                 int x, int y, int w, int h,
                                 KeyBinding key, net.minecraft.util.Identifier tex,
                                 boolean anim, float delta,
                                 int rainbowFgIdle, int rainbowFgPressed,
                                 int rainbowBgIdle, int rainbowBgPressed,
                                 int rainbowShadowIdle, int rainbowShadowPressed,
                                 boolean showInputs) {
        String label = getKeyLabel(key);
        drawKey(ctx, cfg, x, y, w, h, label, tex, label, key, anim, delta, rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, rainbowShadowIdle, rainbowShadowPressed, showInputs);
    }

    // Rainbow Colors
    private int resolveFg(KeystrokeConfig cfg, float t, int rainbowIdle, int rainbowPressed) {
        int idleColor = cfg.rainbowKeyNormal ? rainbowIdle : cfg.keyColor;
        int pressedColor = cfg.rainbowKeyPressed ? rainbowPressed : cfg.keyPressedColor;
        return KeyPressAnimator.blendColor(idleColor, pressedColor, t);
    }
    private int resolveBg(KeystrokeConfig cfg, float t, int rainbowIdle, int rainbowPressed) {
        int idleColor = cfg.rainbowBackgroundNormal ? rainbowIdle : cfg.keyBackgroundColor;
        int pressedColor = cfg.rainbowBackgroundPressed ? rainbowPressed : cfg.keyPressedBackgroundColor;
        return KeyPressAnimator.blendColor(idleColor, pressedColor, t);
    }
    private int resolveIdleFg(KeystrokeConfig cfg, int rainbowColor) {
        return cfg.rainbowKeyNormal ? rainbowColor : cfg.keyColor;
    }
    private int resolveIdleBg(KeystrokeConfig cfg, int rainbowBackgroundColor) {
        return cfg.rainbowBackgroundNormal ? rainbowBackgroundColor : cfg.keyBackgroundColor;
    }
    // Text Shadow
    private boolean resolveTextShadow(KeystrokeConfig cfg, float t) {
        return t >= 0.5f ? cfg.keyPressedTextShadow : cfg.keyTextShadow;
    }
    private int resolveTextShadowColor(KeystrokeConfig cfg, float t, int rainbowIdle, int rainbowPressed) {
        int idleColor = cfg.rainbowKeyTextShadow ? rainbowIdle : cfg.keyTextShadowColor;
        int pressedColor = cfg.rainbowKeyPressedTextShadow ? rainbowPressed : cfg.keyPressedTextShadowColor;
        return KeyPressAnimator.blendColor(idleColor, pressedColor, t);
    }

    private boolean resolveTextShadowUsesCustomColor(KeystrokeConfig cfg, float t) {
        return cfg.useCustomTextShadowColor || (t >= 0.5f ? cfg.rainbowKeyPressedTextShadow : cfg.rainbowKeyTextShadow);
    }

    private String getKeyLabel(KeyBinding key) {
        int code = InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey()).getCode();
        return switch (code) {
            case GLFW.GLFW_KEY_SPACE -> "SPC";
            case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> "SNK";
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> "CTL";
            case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> "ALT";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_DOWN -> "DN";
            case GLFW.GLFW_KEY_LEFT -> "LFT";
            case GLFW.GLFW_KEY_RIGHT -> "RGT";
            default -> {
                String name = GLFW.glfwGetKeyName(code, 0);
                yield (name != null && !name.isEmpty()) ? name.toUpperCase() : "?";
            }
        };
    }

    private void syncTickSyncedKeyStates(MinecraftClient client) {
        long gameTime = client.world == null ? 0L : client.world.getLevelProperties().getTime();
        if (lastSyncedKeyPressTick == gameTime) {
            return;
        }

        lastSyncedKeyPressTick = gameTime;
        tickSyncedKeyStates.put(client.options.forwardKey, client.options.forwardKey.isPressed());
        tickSyncedKeyStates.put(client.options.leftKey, client.options.leftKey.isPressed());
        tickSyncedKeyStates.put(client.options.backKey, client.options.backKey.isPressed());
        tickSyncedKeyStates.put(client.options.rightKey, client.options.rightKey.isPressed());
        tickSyncedKeyStates.put(client.options.jumpKey, client.options.jumpKey.isPressed());
        tickSyncedKeyStates.put(client.options.sneakKey, client.options.sneakKey.isPressed());
        tickSyncedKeyStates.put(client.options.sprintKey, client.options.sprintKey.isPressed());
    }

    private boolean isKeyDown(KeyBinding key, KeystrokeConfig cfg) {
        if (cfg.tickSyncedKeyPresses) {
            return tickSyncedKeyStates.getOrDefault(key, key.isPressed());
        }

        boolean physicallyDown = isPhysicalKeyDown(key);
        if (physicallyDown) {
            return true;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (key == client.options.sneakKey || key == client.options.sprintKey) {
            return key.isPressed();
        }

        return false;
    }

    private boolean isPhysicalKeyDown(KeyBinding key) {
        int code = InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey()).getCode();
        if (code < 0) return false;
        long win = MinecraftClient.getInstance().getWindow().getHandle();
        return GLFW.glfwGetKey(win, code) == GLFW.GLFW_PRESS;
    }
}