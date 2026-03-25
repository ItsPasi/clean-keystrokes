package com.clean.keystrokes.mixin;

import com.clean.keystrokes.display.util.MouseTracker;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class CameraMovementMixin {

    @Unique
    private float prevYaw   = Float.NaN;
    @Unique
    private float prevPitch = Float.NaN;

    @Inject(method = "turnPlayer", at = @At("TAIL"))
    private void onUpdateMouse(double timeDelta, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        float yaw   = client.player.getYRot();
        float pitch = client.player.getXRot();

        if (!Float.isNaN(prevYaw)) {
            double yawDelta   = yaw   - prevYaw;
            double pitchDelta = pitch - prevPitch;
            MouseTracker.onMouseMove(yawDelta, pitchDelta);
        }

        prevYaw   = yaw;
        prevPitch = pitch;
    }
}