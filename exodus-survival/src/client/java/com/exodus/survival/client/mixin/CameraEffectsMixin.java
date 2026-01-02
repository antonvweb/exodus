package com.exodus.survival.client.mixin;

import com.exodus.survival.client.health.effects.DamageShake;
import com.exodus.survival.client.health.effects.CameraShakeEffect;
import com.exodus.survival.client.health.effects.HeadSpinEffect;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin для применения тряски камеры
 * Применяется каждый кадр после обычной настройки камеры
 */
@Mixin(Camera.class)
public abstract class CameraEffectsMixin {

    /**
     * Применяем тряску ПОСЛЕ того как камера настроена
     * Это гарантирует что наше смещение применится последним
     */
    @Inject(
            method = "setup",
            at = @At("RETURN")
    )
    private void applyCameraShake(CallbackInfo ci) {
        // Обновляем тряску
        DamageShake.tick();
        HeadSpinEffect.tick();
        CameraShakeEffect.tick();

        // Применяем к камере
        Camera camera = (Camera) (Object) this;
        DamageShake.applyToCamera(camera);
        CameraShakeEffect.applyToCamera(camera);
        HeadSpinEffect.applyToCamera(camera);
    }
}