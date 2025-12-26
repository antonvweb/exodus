package com.exodus.health.mixin.client;

import com.exodus.health.client.effects.CameraShake;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin для применения тряски камеры
 */
@Mixin(LocalPlayer.class)
public abstract class CameraShakeMixin {

    private static boolean debugPrinted = false;

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void applyCameraShake(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;

        // Применяем смещение камеры
        float shakeYaw = CameraShake.getShakeYaw();
        float shakePitch = CameraShake.getShakePitch();

        if (shakeYaw != 0.0f || shakePitch != 0.0f) {
            // Debug (только первый раз)
            if (!debugPrinted) {
                System.out.println("=== CAMERA SHAKE ACTIVE! Yaw: " + shakeYaw + ", Pitch: " + shakePitch + " ===");
                debugPrinted = true;
            }

            player.turn(shakeYaw, shakePitch);
        } else {
            debugPrinted = false; // Сброс для следующей тряски
        }
    }
}