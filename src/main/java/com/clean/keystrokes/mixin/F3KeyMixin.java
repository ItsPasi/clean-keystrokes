package com.clean.keystrokes.mixin;

import com.clean.keystrokes.display.util.F3KeyTracker;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public class F3KeyMixin {

    @Inject(method = "processF3", at = @At("RETURN"))
    private void onProcessF3(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            F3KeyTracker.onDebugCombination();
        }
    }
}
