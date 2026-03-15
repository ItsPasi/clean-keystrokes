package com.clean.keystrokes;

import com.clean.keystrokes.display.hud.KeystrokeHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.util.Identifier;

public class CleanKeyStrokes implements ClientModInitializer {

	public static final String MOD_ID = "clean-keystrokes";

	@Override
	public void onInitializeClient() {
		KeystrokeHud hud = new KeystrokeHud();
		HudElementRegistry.attachElementAfter(
				VanillaHudElements.MISC_OVERLAYS,
				Identifier.of(MOD_ID, "keystroke_hud"),
				hud::onHudRender
		);
	}
}