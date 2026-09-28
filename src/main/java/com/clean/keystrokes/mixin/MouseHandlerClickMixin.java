package com.clean.keystrokes.mixin;

import com.clean.keystrokes.display.hud.KeystrokeHud;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerClickMixin {

    @Inject(method = "onButton", at = @At("HEAD"))
    private void onMouseButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        // action == 1 is GLFW_PRESS (leading edge only, not hold)
        if (action == 1) {
            if (rawButtonInfo.button() == InputConstants.MOUSE_BUTTON_LEFT) KeystrokeHud.lmbCps.registerClick();
            if (rawButtonInfo.button() == InputConstants.MOUSE_BUTTON_RIGHT) KeystrokeHud.rmbCps.registerClick();
        }
    }
}