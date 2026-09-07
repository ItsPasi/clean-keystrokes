package com.clean.keystrokes.display.hud;

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
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.lwjgl.glfw.GLFW;

import java.util.IdentityHashMap;

public class KeystrokeHud {

    public static final CpsCounter lmbCps = new CpsCounter();
    public static final CpsCounter rmbCps = new CpsCounter();

    private long lastSyncedKeyPressTick = Long.MIN_VALUE;
    private boolean tickForwardPressed;
    private boolean tickLeftPressed;
    private boolean tickBackPressed;
    private boolean tickRightPressed;
    private boolean tickJumpPressed;
    private boolean tickSneakPressed;
    private boolean tickSprintPressed;

    private final IdentityHashMap<KeyMapping, KeyInfo> keyInfoCache = new IdentityHashMap<>();

    private boolean hideOverlayOnDebugScreen(Minecraft client) {
        return client.debugEntries.isCurrentlyEnabled(DebugScreenEntries.GAME_VERSION);
    }

    private boolean isHudHidden(Minecraft client) {
        Boolean hudHidden = getBooleanMethod(client.gui.hud, "hidden");
        if (hudHidden != null) return hudHidden;

        hudHidden = getBooleanMethod(client.gui.hud, "isHidden");
        if (hudHidden != null) return hudHidden;

        hudHidden = getBooleanField(client.gui.hud, "hidden");
        if (hudHidden != null) return hudHidden;

        hudHidden = getBooleanField(client.gui.hud, "hudHidden");
        if (hudHidden != null) return hudHidden;

        hudHidden = getBooleanField(client.options, "hideGui");
        if (hudHidden != null) return hudHidden;

        hudHidden = getBooleanField(client.options, "hudHidden");
        return hudHidden != null && hudHidden;
    }

    private Boolean getBooleanMethod(Object target, String name) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                java.lang.reflect.Method method = type.getDeclaredMethod(name);
                if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) {
                    method.setAccessible(true);
                    return (Boolean) method.invoke(target);
                }
                return null;
            } catch (NoSuchMethodException ignored) {
                type = type.getSuperclass();
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                return null;
            }
        }
        return null;
    }

    private Boolean getBooleanField(Object target, String name) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                java.lang.reflect.Field field = type.getDeclaredField(name);
                if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                    field.setAccessible(true);
                    return (Boolean) field.get(target);
                }
                return null;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                return null;
            }
        }
        return null;
    }

    public void onHudRender(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || hideOverlayOnDebugScreen(client)) return;
        if (isHudHidden(client)) return;

        long frameNow = System.currentTimeMillis();
        KeystrokeConfig cfg = KeystrokeConfig.get();
        HudLayout lay = new HudLayout(cfg);
        int kS = lay.keySize;
        int hH = lay.halfHeight;
        float delta = tickCounter.getRealtimeDeltaTicks();
        boolean anim = cfg.pressAnimation;
        Screen currentScreen = client.gui.screen();
        boolean showInputs = currentScreen == null || currentScreen instanceof AbstractContainerScreen;
        boolean showClicks = currentScreen == null;

        int rainbowRgbNormal = (cfg.rainbowKeyNormal || cfg.rainbowBackgroundPressed)
                ? RainbowColor.getRgbShifted(frameNow, 0.0f, cfg.rainbowSpeed) : 0;
        int rainbowRgbOpposite = (cfg.rainbowKeyPressed || cfg.rainbowBackgroundNormal)
                ? RainbowColor.getRgbShifted(frameNow, 0.5f, cfg.rainbowSpeed) : 0;
        int rainbowRgbShadowIdle = cfg.rainbowKeyTextShadow
                ? RainbowColor.getRgbShifted(frameNow, 0.25f, cfg.rainbowSpeed) : 0;
        int rainbowRgbShadowPressed = cfg.rainbowKeyPressedTextShadow
                ? RainbowColor.getRgbShifted(frameNow, 0.75f, cfg.rainbowSpeed) : 0;

        int rainbowFgIdle = cfg.rainbowKeyNormal
                ? RainbowColor.withAlpha(rainbowRgbNormal, (cfg.keyColor >>> 24) & 0xFF) : 0;
        int rainbowFgPressed = cfg.rainbowKeyPressed
                ? RainbowColor.withAlpha(rainbowRgbOpposite, (cfg.keyPressedColor >>> 24) & 0xFF) : 0;
        int rainbowBgIdle = cfg.rainbowBackgroundNormal
                ? RainbowColor.withAlpha(rainbowRgbOpposite, (cfg.keyBackgroundColor >>> 24) & 0xFF) : 0;
        int rainbowBgPressed = cfg.rainbowBackgroundPressed
                ? RainbowColor.withAlpha(rainbowRgbNormal, (cfg.keyPressedBackgroundColor >>> 24) & 0xFF) : 0;
        int rainbowShadowIdle = cfg.rainbowKeyTextShadow
                ? RainbowColor.withAlpha(rainbowRgbShadowIdle, (cfg.keyTextShadowColor >>> 24) & 0xFF) : 0;
        int rainbowShadowPressed = cfg.rainbowKeyPressedTextShadow
                ? RainbowColor.withAlpha(rainbowRgbShadowPressed, (cfg.keyPressedTextShadowColor >>> 24) & 0xFF) : 0;

        if (cfg.tickSyncedKeyPresses) syncTickSyncedKeyStates(client);

        MouseTracker.tick(delta);
        MouseTracker.updateRenderPos(
                lay.stripCenterX,
                lay.rowMouse,
                lay.stripCenterW,
                hH,
                lay.dotSize,
                delta,
                frameNow
        );

        long win = client.getWindow().handle();

        // WASD
        drawMovementKey(ctx, cfg, lay.col1, lay.rowW, kS, kS,
                client.options.keyUp, HudTextures.KEY_W, KeyPressAnimator.W,
                anim, delta, rainbowFgIdle, rainbowFgPressed,
                rainbowBgIdle, rainbowBgPressed,
                rainbowShadowIdle, rainbowShadowPressed, showInputs);
        drawMovementKey(ctx, cfg, lay.col0, lay.rowASD, kS, kS,
                client.options.keyLeft, HudTextures.KEY_A, KeyPressAnimator.A,
                anim, delta, rainbowFgIdle, rainbowFgPressed,
                rainbowBgIdle, rainbowBgPressed,
                rainbowShadowIdle, rainbowShadowPressed, showInputs);
        drawMovementKey(ctx, cfg, lay.col1, lay.rowASD, kS, kS,
                client.options.keyDown, HudTextures.KEY_S, KeyPressAnimator.S,
                anim, delta, rainbowFgIdle, rainbowFgPressed,
                rainbowBgIdle, rainbowBgPressed,
                rainbowShadowIdle, rainbowShadowPressed, showInputs);
        drawMovementKey(ctx, cfg, lay.col2, lay.rowASD, kS, kS,
                client.options.keyRight, HudTextures.KEY_D, KeyPressAnimator.D,
                anim, delta, rainbowFgIdle, rainbowFgPressed,
                rainbowBgIdle, rainbowBgPressed,
                rainbowShadowIdle, rainbowShadowPressed, showInputs);

        // SPACE
        drawKey(ctx, cfg, lay.spaceX, lay.rowSpace, lay.spaceW, hH,
                "—", HudTextures.KEY_SPACE, KeyPressAnimator.SPACE,
                client.options.keyJump, anim, delta,
                rainbowFgIdle, rainbowFgPressed,
                rainbowBgIdle, rainbowBgPressed,
                rainbowShadowIdle, rainbowShadowPressed, showInputs);

        if (cfg.showSneakSprintRow) {
            drawKey(ctx, cfg, lay.sneakX, lay.rowSneakSprint, lay.sneakW, hH,
                    "SNK", HudTextures.KEY_SNK, KeyPressAnimator.SNEAK,
                    client.options.keyShift, anim, delta,
                    rainbowFgIdle, rainbowFgPressed,
                    rainbowBgIdle, rainbowBgPressed,
                    rainbowShadowIdle, rainbowShadowPressed, showInputs);
            drawKey(ctx, cfg, lay.sprintX, lay.rowSneakSprint, lay.sprintW, hH,
                    "SPR", HudTextures.KEY_SPR, KeyPressAnimator.SPRINT,
                    client.options.keySprint, anim, delta,
                    rainbowFgIdle, rainbowFgPressed,
                    rainbowBgIdle, rainbowBgPressed,
                    rainbowShadowIdle, rainbowShadowPressed, showInputs);
        }

        // Mouse Row
        boolean lmbDown = showClicks
                && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rmbDown = showClicks
                && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        float lmbT = anim
                ? KeyPressAnimator.update(KeyPressAnimator.LMB, lmbDown, delta)
                : (lmbDown ? 1f : 0f);
        float rmbT = anim
                ? KeyPressAnimator.update(KeyPressAnimator.RMB, rmbDown, delta)
                : (rmbDown ? 1f : 0f);

        int lmbBg = resolveBg(cfg, lmbT, rainbowBgIdle, rainbowBgPressed);
        int lmbFg = resolveFg(cfg, lmbT, rainbowFgIdle, rainbowFgPressed);
        boolean lmbShadow = resolveTextShadow(cfg, lmbT);
        int lmbShadowColor = lmbShadow
                ? resolveTextShadowColor(cfg, lmbT, rainbowShadowIdle, rainbowShadowPressed)
                : 0;
        boolean lmbCustomShadowColor =
                lmbShadow && resolveTextShadowUsesCustomColor(cfg, lmbT);

        int rmbBg = resolveBg(cfg, rmbT, rainbowBgIdle, rainbowBgPressed);
        int rmbFg = resolveFg(cfg, rmbT, rainbowFgIdle, rainbowFgPressed);
        boolean rmbShadow = resolveTextShadow(cfg, rmbT);
        int rmbShadowColor = rmbShadow
                ? resolveTextShadowColor(cfg, rmbT, rainbowShadowIdle, rainbowShadowPressed)
                : 0;
        boolean rmbCustomShadowColor =
                rmbShadow && resolveTextShadowUsesCustomColor(cfg, rmbT);

        boolean idleShadow = cfg.keyTextShadow;
        int idleShadowColor = idleShadow
                ? (cfg.rainbowKeyTextShadow ? rainbowShadowIdle : cfg.keyTextShadowColor)
                : 0;
        boolean idleCustomShadowColor = idleShadow
                && (cfg.useCustomTextShadowColor || cfg.rainbowKeyTextShadow);

        HudRenderer.drawTexture(
                ctx, HudTextures.KEY_LMB,
                lay.stripLmbX, lay.rowMouse, lay.stripLmbW, hH, lmbBg
        );
        HudRenderer.drawCenteredNumber(
                ctx, showClicks ? lmbCps.getCps(frameNow) : 0,
                lay.stripLmbX, lay.rowMouse, lay.stripLmbW, hH,
                lmbFg, lmbShadow, lmbShadowColor, lmbCustomShadowColor, lay.scale
        );

        HudRenderer.drawTexture(
                ctx, HudTextures.KEY_MOUSE_CENTER,
                lay.stripCenterX, lay.rowMouse, lay.stripCenterW, hH,
                resolveIdleBg(cfg, rainbowBgIdle)
        );
        HudRenderer.drawDotWithTrail(
                ctx, lay.dotSize, resolveIdleFg(cfg, rainbowFgIdle),
                idleShadow, idleShadowColor, idleCustomShadowColor, frameNow
        );

        HudRenderer.drawTexture(
                ctx, HudTextures.KEY_RMB,
                lay.stripRmbX, lay.rowMouse, lay.stripRmbW, hH, rmbBg
        );
        HudRenderer.drawCenteredNumber(
                ctx, showClicks ? rmbCps.getCps(frameNow) : 0,
                lay.stripRmbX, lay.rowMouse, lay.stripRmbW, hH,
                rmbFg, rmbShadow, rmbShadowColor, rmbCustomShadowColor, lay.scale
        );
    }

    private void drawKey(GuiGraphicsExtractor ctx, KeystrokeConfig cfg,
                         int x, int y, int w, int h,
                         String label, net.minecraft.resources.Identifier tex,
                         int animKey, KeyMapping key,
                         boolean anim, float delta,
                         int rainbowFgIdle, int rainbowFgPressed,
                         int rainbowBgIdle, int rainbowBgPressed,
                         int rainbowShadowIdle, int rainbowShadowPressed,
                         boolean showInputs) {
        boolean pressed = showInputs && isKeyDown(key, cfg);
        float t = anim
                ? KeyPressAnimator.update(animKey, pressed, delta)
                : (pressed ? 1f : 0f);

        int bg = resolveBg(cfg, t, rainbowBgIdle, rainbowBgPressed);
        int fg = resolveFg(cfg, t, rainbowFgIdle, rainbowFgPressed);
        boolean textShadow = resolveTextShadow(cfg, t);
        int textShadowColor = textShadow
                ? resolveTextShadowColor(cfg, t, rainbowShadowIdle, rainbowShadowPressed)
                : 0;
        boolean customTextShadowColor =
                textShadow && resolveTextShadowUsesCustomColor(cfg, t);

        HudRenderer.drawLabelKey(
                ctx, x, y, w, h, label, tex,
                bg, fg, textShadow, textShadowColor,
                customTextShadowColor, cfg.hudScale
        );
    }

    private void drawMovementKey(GuiGraphicsExtractor ctx, KeystrokeConfig cfg,
                                 int x, int y, int w, int h,
                                 KeyMapping key, net.minecraft.resources.Identifier tex,
                                 int animKey,
                                 boolean anim, float delta,
                                 int rainbowFgIdle, int rainbowFgPressed,
                                 int rainbowBgIdle, int rainbowBgPressed,
                                 int rainbowShadowIdle, int rainbowShadowPressed,
                                 boolean showInputs) {
        drawKey(
                ctx, cfg, x, y, w, h,
                getKeyLabel(key), tex, animKey, key,
                anim, delta,
                rainbowFgIdle, rainbowFgPressed,
                rainbowBgIdle, rainbowBgPressed,
                rainbowShadowIdle, rainbowShadowPressed,
                showInputs
        );
    }

    // Rainbow Colors
    private int resolveFg(KeystrokeConfig cfg, float t,
                          int rainbowIdle, int rainbowPressed) {
        int idleColor = cfg.rainbowKeyNormal ? rainbowIdle : cfg.keyColor;
        if (t <= 0f) return idleColor;

        int pressedColor = cfg.rainbowKeyPressed
                ? rainbowPressed : cfg.keyPressedColor;
        if (t >= 1f) return pressedColor;

        return KeyPressAnimator.blendColor(idleColor, pressedColor, t);
    }

    private int resolveBg(KeystrokeConfig cfg, float t,
                          int rainbowIdle, int rainbowPressed) {
        int idleColor = cfg.rainbowBackgroundNormal
                ? rainbowIdle : cfg.keyBackgroundColor;
        if (t <= 0f) return idleColor;

        int pressedColor = cfg.rainbowBackgroundPressed
                ? rainbowPressed : cfg.keyPressedBackgroundColor;
        if (t >= 1f) return pressedColor;

        return KeyPressAnimator.blendColor(idleColor, pressedColor, t);
    }

    private int resolveIdleFg(KeystrokeConfig cfg, int rainbowColor) {
        return cfg.rainbowKeyNormal ? rainbowColor : cfg.keyColor;
    }

    private int resolveIdleBg(KeystrokeConfig cfg, int rainbowBackgroundColor) {
        return cfg.rainbowBackgroundNormal
                ? rainbowBackgroundColor : cfg.keyBackgroundColor;
    }

    // Text Shadow
    private boolean resolveTextShadow(KeystrokeConfig cfg, float t) {
        return t >= 0.5f ? cfg.keyPressedTextShadow : cfg.keyTextShadow;
    }

    private int resolveTextShadowColor(KeystrokeConfig cfg, float t,
                                       int rainbowIdle, int rainbowPressed) {
        int idleColor = cfg.rainbowKeyTextShadow
                ? rainbowIdle : cfg.keyTextShadowColor;
        if (t <= 0f) return idleColor;

        int pressedColor = cfg.rainbowKeyPressedTextShadow
                ? rainbowPressed : cfg.keyPressedTextShadowColor;
        if (t >= 1f) return pressedColor;

        return KeyPressAnimator.blendColor(idleColor, pressedColor, t);
    }

    private boolean resolveTextShadowUsesCustomColor(KeystrokeConfig cfg, float t) {
        return cfg.useCustomTextShadowColor
                || (t >= 0.5f
                ? cfg.rainbowKeyPressedTextShadow
                : cfg.rainbowKeyTextShadow);
    }

    private String getKeyLabel(KeyMapping key) {
        return getKeyInfo(key).label();
    }

    private KeyInfo getKeyInfo(KeyMapping key) {
        String saveString = key.saveString();
        KeyInfo cached = keyInfoCache.get(key);
        if (cached != null && cached.saveString().equals(saveString)) {
            return cached;
        }

        int code = InputConstants.getKey(saveString).getValue();
        String label = switch (code) {
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
                yield (name != null && !name.isEmpty())
                        ? name.toUpperCase()
                        : "?";
            }
        };

        KeyInfo updated = new KeyInfo(saveString, code, label);
        keyInfoCache.put(key, updated);
        return updated;
    }

    private void syncTickSyncedKeyStates(Minecraft client) {
        long gameTime = client.level == null ? 0L : client.level.getGameTime();
        if (lastSyncedKeyPressTick == gameTime) return;

        lastSyncedKeyPressTick = gameTime;
        tickForwardPressed = client.options.keyUp.isDown();
        tickLeftPressed = client.options.keyLeft.isDown();
        tickBackPressed = client.options.keyDown.isDown();
        tickRightPressed = client.options.keyRight.isDown();
        tickJumpPressed = client.options.keyJump.isDown();
        tickSneakPressed = client.options.keyShift.isDown();
        tickSprintPressed = client.options.keySprint.isDown();
    }

    private boolean isKeyDown(KeyMapping key, KeystrokeConfig cfg) {
        if (cfg.tickSyncedKeyPresses) {
            Minecraft client = Minecraft.getInstance();
            if (key == client.options.keyUp) return tickForwardPressed;
            if (key == client.options.keyLeft) return tickLeftPressed;
            if (key == client.options.keyDown) return tickBackPressed;
            if (key == client.options.keyRight) return tickRightPressed;
            if (key == client.options.keyJump) return tickJumpPressed;
            if (key == client.options.keyShift) return tickSneakPressed;
            if (key == client.options.keySprint) return tickSprintPressed;
            return key.isDown();
        }

        if (isPhysicalKeyDown(key)) return true;

        Minecraft client = Minecraft.getInstance();
        if (key == client.options.keyShift || key == client.options.keySprint) {
            return key.isDown();
        }

        return false;
    }

    private boolean isPhysicalKeyDown(KeyMapping key) {
        int code = getKeyInfo(key).code();
        if (code < 0) return false;

        long win = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetKey(win, code) == GLFW.GLFW_PRESS;
    }

    private record KeyInfo(String saveString, int code, String label) {}
}
