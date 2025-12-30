package com.exodus.core.player.health;

import com.exodus.core.api.player.BodyPart;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
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
        // При респавне - полное восстановление
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) { // Игрок умер и респавнился
                PlayerHealthComponent healthComp = getComponent(newPlayer);

                // ✅ Восстанавливаем HP всех частей тела
                for (BodyPart part : BodyPart.values()) {
                    healthComp.getData().setBodyPartHP(part, part.getMaxHP());
                }

                // ✅ Убираем ВСЕ кровотечения
                for (BodyPart part : BodyPart.values()) {
                    if (healthComp.getData().hasBleeding(part)) {
                        healthComp.getData().removeBleeding(part);
                    }
                }

                // ✅ Убираем ВСЕ переломы
                for (BodyPart part : BodyPart.values()) {
                    if (healthComp.getData().hasFracture(part)) {
                        healthComp.getData().removeFracture(part);
                    }
                }

                // ✅ Убираем боль
                healthComp.getData().removePain();

                // ✅ Статы сохраняются после смерти (не сбрасываются)
            }
        });

        // При переходе между мирами - копируем данные
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            // Копируем здоровье
            PlayerHealthComponent oldHealthComp = getComponent(oldPlayer);
            PlayerHealthComponent newHealthComp = getComponent(newPlayer);

            CompoundTag healthNbt = new CompoundTag();
            oldHealthComp.writeNbt(healthNbt);
            newHealthComp.readNbt(healthNbt);
        });
    }
}
