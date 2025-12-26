package com.exodus.health.mixin.client;

import com.exodus.health.client.effects.BloodOverlay;
import com.exodus.health.client.effects.CameraShake;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

/**
 * Клиентский Mixin для визуальных эффектов при получении урона
 */
@Mixin(LocalPlayer.class)
public abstract class ClientDamageEffectsMixin {

    @Unique
    private static Field hurtTimeField;
    @Unique
    private static Field lastHurtField;

    static {
        try {
            // Получаем поля через рефлексию
            hurtTimeField = net.minecraft.world.entity.LivingEntity.class.getDeclaredField("hurtTime");
            hurtTimeField.setAccessible(true);

            lastHurtField = net.minecraft.world.entity.LivingEntity.class.getDeclaredField("lastHurt");
            lastHurtField.setAccessible(true);
        } catch (Exception e) {
            System.err.println("Failed to get hurt fields: " + e.getMessage());
        }
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;

        try {
            int hurtTime = (int) hurtTimeField.get(player);

            if (hurtTime == 10) {
                float damage = (float) lastHurtField.get(player);

                System.out.println("=== CLIENT DAMAGE DETECTED! Damage: " + damage + ", hurtTime: " + hurtTime + " ===");

                if (damage > 0) {
                    CameraShake.addShake(damage);
                    BloodOverlay.addBloodSplatter(damage);
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }
}