package com.clean.keystrokes;

import com.clean.keystrokes.display.hud.KeystrokeHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanKeyStrokes implements ClientModInitializer {

	public static final String MOD_ID = "clean-keystrokes";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		KeystrokeHud hud = new KeystrokeHud();
		HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerAfter(
				IdentifiedLayer.MISC_OVERLAYS,
				Identifier.of(MOD_ID, "keystroke_hud"),
				hud::onHudRender
		));
		LOGGER.info("Registered keystroke HUD with addLast at the end of the HUD render chain.");
	}
}