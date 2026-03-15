package com.clean.keystrokes.mixin;

import com.clean.keystrokes.display.hud.KeystrokeHud;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseClickMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
        // action == 1 is GLFW_PRESS (leading edge only, not hold)
        if (action == 1) {
            if (input.button() == 0) KeystrokeHud.lmbCps.registerClick();
            if (input.button() == 1) KeystrokeHud.rmbCps.registerClick();
        }
    }
}