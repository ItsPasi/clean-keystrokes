package com.clean.keystrokes;

import com.clean.keystrokes.display.hud.KeystrokeHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanKeyStrokes implements ClientModInitializer {

	public static final String MOD_ID = "clean-keystrokes";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        KeystrokeHud hud = new KeystrokeHud();
        HudRenderCallback.EVENT.register((ctx, tickDelta) -> hud.onHudRender(ctx));
        LOGGER.info("Registered Clean Keystrokes HUD");
    }
}