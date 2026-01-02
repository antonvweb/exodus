package com.exodus.survival.client.health.network;

import com.exodus.survival.client.health.effects.*;
import com.exodus.survival.client.health.effects.sounds.StatusSounds;
import com.exodus.survival.health.network.DamagePacket;
import com.exodus.survival.health.network.CameraShakePacket;
import com.exodus.survival.health.network.FracturePacket;
import com.exodus.survival.health.network.HeadSpinPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

/**
 * Обработчик сетевых пакетов на клиенте
 */
public class ClientNetworkHandler {

    public static void register() {
          ClientPlayNetworking.registerGlobalReceiver(DamagePacket.TYPE, (packet, player, responseSender) -> {
            float damage = packet.getDamage();

            Minecraft.getInstance().execute(() -> {
                DamageShake.addShake(damage);

                if (damage >= 5.0f) {
                    DamageSounds.playCriticalHurtSound(damage);
                } else {
                    DamageSounds.playHurtSound(damage);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(CameraShakePacket.TYPE, (packet, player, responseSender) -> {
            boolean activate = packet.shouldActivate();
            float intensity = packet.getIntensity();

            Minecraft.getInstance().execute(() -> {
                if (activate) {
                    CameraShakeEffect.activate(intensity);

                    StatusSounds.updateHypothermiaAmbient(true, 34.0f);
                } else {
                    CameraShakeEffect.deactivate();
                    StatusSounds.updateHypothermiaAmbient(false, 37.0f);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(HeadSpinPacket.TYPE, (packet, player, responseSender) -> {
            boolean activate = packet.shouldActivate();
            float intensity = packet.getIntensity();

            Minecraft.getInstance().execute(() -> {
                if (activate) {
                    HeadSpinEffect.activate(intensity);
                    StatusSounds.updateHyperthermiaBreath(true, 39.0f);
                } else {
                    HeadSpinEffect.deactivate();
                    StatusSounds.updateHyperthermiaBreath(false, 37.0f);
                }
            });
        });

        // В методе register() добавьте:
        ClientPlayNetworking.registerGlobalReceiver(FracturePacket.TYPE, (packet, player, responseSender) -> {
            String bodyPart = packet.getBodyPart();
            float intensity = packet.getIntensity();

            Minecraft.getInstance().execute(() -> {
                // Проигрываем звук перелома
                StatusSounds.playFractureSound();

                // Можно также добавить другие эффекты:
                // - Визуальные частицы
                // - Экранный эффект
                // - Сообщение в чат
                if (intensity > 0.7f) {
                    DamageShake.addShake(3.0f); // Лёгкая тряска для сильных переломов
                }
            });
        });
    }
}