package com.exodus.health.mixin.client;

import com.exodus.health.effects.CameraShake;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin для применения тряски камеры каждый кадр
 */
@Mixin(LocalPlayer.class)
public abstract class CameraShakeMixin {

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void applyCameraShake(CallbackInfo ci) {
        // Применяем тряску каждый кадр
        CameraShake.applyToCamera();
    }
}