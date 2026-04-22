package com.clean.keystrokes.mixin;

import com.clean.keystrokes.display.hud.KeystrokeHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBinding.class)
public class MouseClickMixin {

    @Inject(method = "setKeyPressed", at = @At("HEAD"))
    private static void onSetKeyPressed(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
        if (pressed) {
            MinecraftClient client = MinecraftClient.getInstance();

            // Check if the key being pressed is the one bound to Attack (LMB)
            if (key.equals(client.options.attackKey.getDefaultKey())) {
                KeystrokeHud.lmbCps.registerClick();
            }

            // Check if the key being pressed is the one bound to Use (RMB)
            if (key.equals(client.options.useKey.getDefaultKey())) {
                KeystrokeHud.rmbCps.registerClick();
            }
        }
    }
}