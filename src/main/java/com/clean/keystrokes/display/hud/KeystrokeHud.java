package com.clean.keystrokes.display.hud;

import com.clean.keystrokes.CleanKeyStrokes;
import com.clean.keystrokes.display.config.KeystrokeConfig;
import com.clean.keystrokes.display.util.CpsCounter;
import com.clean.keystrokes.display.util.KeyPressAnimator;
import com.clean.keystrokes.display.util.MouseTracker;
import com.clean.keystrokes.display.util.RainbowColor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

public class KeystrokeHud {

    public static final CpsCounter lmbCps = new CpsCounter();
    public static final CpsCounter rmbCps = new CpsCounter();

    private static boolean loggedHudHiddenSkip;
    private static boolean loggedFirstRender;

    private long lastSyncedKeyPressTick = Long.MIN_VALUE;
    private final java.util.IdentityHashMap<KeyMapping, Boolean> tickSyncedKeyStates = new java.util.IdentityHashMap<>();

    public void onHudRender(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        if (client.options.hideGui) {
            if (!loggedHudHiddenSkip) {
                CleanKeyStrokes.LOGGER.info("Skipping Clean Keystrokes HUD render because hideGui=true.");
                loggedHudHiddenSkip = true;
            }
            return;
        }

        KeystrokeConfig cfg  = KeystrokeConfig.get();
        HudLayout lay        = new HudLayout(cfg);
        int kS               = HudLayout.KEY_SIZE;
        int hH               = HudLayout.HALF_HEIGHT;
        float delta          = tickCounter.getRealtimeDeltaTicks();
        boolean shadow       = cfg.textShadow;
        boolean anim         = cfg.pressAnimation;
        boolean showInputs   = client.screen == null || client.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
        boolean showClicks   = client.screen == null;

        int rainbowFgIdle = cfg.rainbowKeyNormal
                ? RainbowColor.get((cfg.keyColor >>> 24) & 0xFF)
                : 0;
        int rainbowFgPressed = cfg.rainbowKeyPressed
                ? RainbowColor.getOpposite((cfg.keyPressedColor >>> 24) & 0xFF)
                : 0;
        int rainbowBgIdle = cfg.rainbowBackgroundNormal
                ? RainbowColor.getOpposite((cfg.keyBackgroundColor >>> 24) & 0xFF)
                : 0;
        int rainbowBgPressed = cfg.rainbowBackgroundPressed
                ? RainbowColor.get((cfg.keyPressedBackgroundColor >>> 24) & 0xFF)
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
                HudLayout.DOT_SIZE,
                delta
        );

        long win = client.getWindow().handle();

        // WASD
        drawMovementKey(ctx, cfg, lay.col1, lay.rowW, kS, kS,
                client.options.keyUp, HudTextures.KEY_W,
                anim, delta, shadow,
                rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, showInputs);

        drawMovementKey(ctx, cfg, lay.col0, lay.rowASD, kS, kS,
                client.options.keyLeft, HudTextures.KEY_A,
                anim, delta, shadow,
                rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, showInputs);

        drawMovementKey(ctx, cfg, lay.col1, lay.rowASD, kS, kS,
                client.options.keyDown, HudTextures.KEY_S,
                anim, delta, shadow,
                rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, showInputs);

        drawMovementKey(ctx, cfg, lay.col2, lay.rowASD, kS, kS,
                client.options.keyRight, HudTextures.KEY_D,
                anim, delta, shadow,
                rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, showInputs);

        // SPACE
        drawKey(ctx, cfg, lay.spaceX, lay.rowSpace, lay.spaceW, hH,
                "—", HudTextures.KEY_SPACE, "SPACE", client.options.keyJump,
                anim, delta, shadow,
                rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, showInputs);

        if (cfg.showSneakSprintRow) {
            drawKey(ctx, cfg, lay.sneakX, lay.rowSneakSprint, lay.sneakW, hH,
                    "SNK", HudTextures.KEY_SNK, "SNK", client.options.keyShift,
                    anim, delta, shadow,
                    rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, showInputs);

            drawKey(ctx, cfg, lay.sprintX, lay.rowSneakSprint, lay.sprintW, hH,
                    "SPR", HudTextures.KEY_SPR, "SPR", client.options.keySprint,
                    anim, delta, shadow,
                    rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, showInputs);
        }

        // Mouse row
        boolean lmbDown = showClicks && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT)  == GLFW.GLFW_PRESS;
        boolean rmbDown = showClicks && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        float lmbT = anim ? KeyPressAnimator.update("LMB", lmbDown, delta) : (lmbDown ? 1f : 0f);
        float rmbT = anim ? KeyPressAnimator.update("RMB", rmbDown, delta) : (rmbDown ? 1f : 0f);

        HudRenderer.drawTexture(ctx, HudTextures.KEY_LMB,
                lay.stripLmbX, lay.rowMouse, lay.stripLmbW, hH,
                resolveBg(cfg, lmbT, rainbowBgIdle, rainbowBgPressed));

        HudRenderer.drawCenteredNumber(ctx, showClicks ? lmbCps.getCps() : 0,
                lay.stripLmbX, lay.rowMouse, lay.stripLmbW, hH,
                resolveFg(cfg, lmbT, rainbowFgIdle, rainbowFgPressed), shadow);

        HudRenderer.drawTexture(ctx, HudTextures.KEY_MOUSE_CENTER,
                lay.stripCenterX, lay.rowMouse, lay.stripCenterW, hH,
                resolveIdleBg(cfg, rainbowBgIdle));

        HudRenderer.drawDotWithTrail(ctx, HudLayout.DOT_SIZE,
                resolveIdleFg(cfg, rainbowFgIdle));

        HudRenderer.drawTexture(ctx, HudTextures.KEY_RMB,
                lay.stripRmbX, lay.rowMouse, lay.stripRmbW, hH,
                resolveBg(cfg, rmbT, rainbowBgIdle, rainbowBgPressed));

        HudRenderer.drawCenteredNumber(ctx, showClicks ? rmbCps.getCps() : 0,
                lay.stripRmbX, lay.rowMouse, lay.stripRmbW, hH,
                resolveFg(cfg, rmbT, rainbowFgIdle, rainbowFgPressed), shadow);
    }

    private void logFirstRender(Minecraft client, KeystrokeConfig cfg, HudLayout lay,
                                boolean showInputs, boolean showClicks) {
        if (loggedFirstRender) {
            return;
        }
        loggedFirstRender = true;

        String screenName = client.screen == null ? "null" : client.screen.getClass().getName();
        CleanKeyStrokes.LOGGER.info(
                "First Clean Keystrokes HUD render reached. scaled={}x{}, position={}, origin=({}, {}), showInputs={}, showClicks={}, hideGui={}, debugHud={}, screen={}, configPath='{}', showSneakSprintRow={}, tickSyncedKeyPresses={}, rainbowKeyNormal={}, rainbowKeyPressed={}, rainbowBackgroundNormal={}, rainbowBackgroundPressed={}, preset={}, colors={key={}, bg={}, pressedKey={}, pressedBg={}}",
                client.getWindow().getGuiScaledWidth(),
                client.getWindow().getGuiScaledHeight(),
                cfg.position,
                lay.originX,
                lay.originY,
                showInputs,
                showClicks,
                client.options.hideGui,
                client.getDebugOverlay().showDebugScreen(),
                screenName,
                KeystrokeConfig.getPath(),
                cfg.showSneakSprintRow,
                cfg.tickSyncedKeyPresses,
                cfg.rainbowKeyNormal,
                cfg.rainbowKeyPressed,
                cfg.rainbowBackgroundNormal,
                cfg.rainbowBackgroundPressed,
                cfg.colorPreset,
                toArgbHex(cfg.keyColor),
                toArgbHex(cfg.keyBackgroundColor),
                toArgbHex(cfg.keyPressedColor),
                toArgbHex(cfg.keyPressedBackgroundColor)
        );
    }

    private String toArgbHex(int color) {
        return String.format("0x%08X", color);
    }

    private void drawKey(GuiGraphicsExtractor ctx, KeystrokeConfig cfg,
                         int x, int y, int w, int h,
                         String label, net.minecraft.resources.Identifier tex,
                         String animKey, KeyMapping key,
                         boolean anim, float delta, boolean shadow,
                         int rainbowFgIdle, int rainbowFgPressed,
                         int rainbowBgIdle, int rainbowBgPressed,
                         boolean showInputs) {
        boolean pressed = showInputs && isKeyDown(key, cfg);
        float t = anim ? KeyPressAnimator.update(animKey, pressed, delta) : (pressed ? 1f : 0f);

        int bg = resolveBg(cfg, t, rainbowBgIdle, rainbowBgPressed);
        int fg = resolveFg(cfg, t, rainbowFgIdle, rainbowFgPressed);

        HudRenderer.drawLabelKey(ctx, x, y, w, h, label, tex, bg, fg, shadow);
    }

    private void drawMovementKey(GuiGraphicsExtractor ctx, KeystrokeConfig cfg,
                                 int x, int y, int w, int h,
                                 KeyMapping key, net.minecraft.resources.Identifier tex,
                                 boolean anim, float delta, boolean shadow,
                                 int rainbowFgIdle, int rainbowFgPressed,
                                 int rainbowBgIdle, int rainbowBgPressed,
                                 boolean showInputs) {
        String label = getKeyLabel(key);
        drawKey(ctx, cfg, x, y, w, h, label, tex, label, key,
                anim, delta, shadow,
                rainbowFgIdle, rainbowFgPressed, rainbowBgIdle, rainbowBgPressed, showInputs);
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

    private String getKeyLabel(KeyMapping key) {
        int code = InputConstants.getKey(key.saveString()).getValue();
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

    private void syncTickSyncedKeyStates(Minecraft client) {
        long gameTime = client.level == null ? 0L : client.level.getGameTime();
        if (lastSyncedKeyPressTick == gameTime) {
            return;
        }

        lastSyncedKeyPressTick = gameTime;
        tickSyncedKeyStates.put(client.options.keyUp, client.options.keyUp.isDown());
        tickSyncedKeyStates.put(client.options.keyLeft, client.options.keyLeft.isDown());
        tickSyncedKeyStates.put(client.options.keyDown, client.options.keyDown.isDown());
        tickSyncedKeyStates.put(client.options.keyRight, client.options.keyRight.isDown());
        tickSyncedKeyStates.put(client.options.keyJump, client.options.keyJump.isDown());
        tickSyncedKeyStates.put(client.options.keyShift, client.options.keyShift.isDown());
        tickSyncedKeyStates.put(client.options.keySprint, client.options.keySprint.isDown());
    }

    private boolean isKeyDown(KeyMapping key, KeystrokeConfig cfg) {
        if (cfg.tickSyncedKeyPresses) {
            return tickSyncedKeyStates.getOrDefault(key, key.isDown());
        }

        boolean physicallyDown = isPhysicalKeyDown(key);
        if (physicallyDown) {
            return true;
        }

        Minecraft client = Minecraft.getInstance();
        if (key == client.options.keyShift || key == client.options.keySprint) {
            return key.isDown();
        }

        return false;
    }

    private boolean isPhysicalKeyDown(KeyMapping key) {
        int code = InputConstants.getKey(key.saveString()).getValue();
        if (code < 0) return false;
        long win = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetKey(win, code) == GLFW.GLFW_PRESS;
    }
}