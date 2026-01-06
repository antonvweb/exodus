package com.exodus.core.player.health;

import com.exodus.core.api.player.health.BodyPart;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер компонентов игрока
 * Управляет: здоровьем, статами, витальными показателями
 */
public class PlayerHealthManager {
    private static final Map<UUID, PlayerHealthComponent> components = new HashMap<>();
    // ============ ЗДОРОВЬЕ ============

    /**
     * Получить компонент здоровья игрока (создаёт если не существует)
     */
    public static PlayerHealthComponent getComponent(Player player) {
        return components.computeIfAbsent(
                player.getUUID(),
                uuid -> new PlayerHealthComponent(player)
        );
    }

    /**
     * Удалить компонент здоровья игрока
     */
    public static void removeComponent(UUID uuid) {
        components.remove(uuid);
    }

    // ============ РЕГИСТРАЦИЯ СОБЫТИЙ ============

    /**
     * Регистрация событий
     */
    public static void registerEvents() {
        // ✅ При входе игрока - инициализируем HP с учётом CON
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Player player = handler.getPlayer();
            PlayerHealthComponent health = getComponent(player);

            // Инициализируем HP с учётом атрибутов
            health.getData().initializeHP(player);
        });

        // При респавне - полное восстановление
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                PlayerHealthComponent healthComp = getComponent(newPlayer);

                // ✅ Восстанавливаем HP с учётом CON
                for (BodyPart part : BodyPart.values()) {
                    float maxHP = healthComp.getData().getMaxBodyPartHP(part, newPlayer);
                    healthComp.getData().setBodyPartHP(part, maxHP, newPlayer);
                }

                // Убираем эффекты (без изменений)
                for (BodyPart part : BodyPart.values()) {
                    if (healthComp.getData().hasBleeding(part)) {
                        healthComp.getData().removeBleeding(part, newPlayer);
                    }
                }

                for (BodyPart part : BodyPart.values()) {
                    if (healthComp.getData().hasFracture(part)) {
                        healthComp.getData().removeFracture(part, newPlayer);
                    }
                }

                healthComp.getData().removePain();
            }
        });

        // При переходе между мирами (без изменений)
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            PlayerHealthComponent oldHealthComp = getComponent(oldPlayer);
            PlayerHealthComponent newHealthComp = getComponent(newPlayer);

            CompoundTag healthNbt = new CompoundTag();
            oldHealthComp.writeNbt(healthNbt);
            newHealthComp.readNbt(healthNbt);
        });
    }
}
