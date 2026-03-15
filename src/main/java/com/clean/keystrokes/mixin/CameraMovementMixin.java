package com.clean.keystrokes.mixin;

import com.clean.keystrokes.display.util.MouseTracker;
import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class CameraMovementMixin {

    @Unique
    private float prevYaw   = Float.NaN;
    @Unique
    private float prevPitch = Float.NaN;

    @Inject(method = "updateMouse", at = @At("TAIL"))
    private void onUpdateMouse(double timeDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        float yaw   = client.player.getYaw();
        float pitch = client.player.getPitch();

        if (!Float.isNaN(prevYaw)) {
            double yawDelta   = yaw   - prevYaw;
            double pitchDelta = pitch - prevPitch;
            MouseTracker.onMouseMove(yawDelta, pitchDelta);
        }

        prevYaw   = yaw;
        prevPitch = pitch;
    }
}