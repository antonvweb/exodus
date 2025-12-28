package com.exodus.health.network;

import com.exodus.health.effects.CameraShake;
import com.exodus.health.effects.DamageSounds;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

/**
 * Обработчик сетевых пакетов на клиенте
 */
public class ClientNetworkHandler {

    /**
     * Регистрация обработчиков пакетов
     */
    public static void register() {
        System.out.println("=== REGISTERING CLIENT NETWORK HANDLERS ===");

        // Обработчик пакета урона
        ClientPlayNetworking.registerGlobalReceiver(DamagePacket.TYPE, (packet, player, responseSender) -> {
            float damage = packet.getDamage();

            System.out.println("=== RECEIVED DAMAGE PACKET! Damage: " + damage + " ===");

            // Выполняем на главном потоке клиента
            Minecraft.getInstance().execute(() -> {
                System.out.println("=== APPLYING CLIENT EFFECTS... ===");

                // ✅ Тряска камеры (простая как в ванилле)
                CameraShake.addShake(damage);

                // ✅ Звук удара (зависит от силы урона)
                if (damage >= 5.0f) {
                    // Тяжёлый удар
                    DamageSounds.playCriticalHurtSound(damage);
                } else {
                    // Обычный удар
                    DamageSounds.playHurtSound(damage);
                }

                System.out.println("=== CLIENT EFFECTS APPLIED! ===");
            });
        });

        System.out.println("=== CLIENT NETWORK HANDLERS REGISTERED ===");
    }
}