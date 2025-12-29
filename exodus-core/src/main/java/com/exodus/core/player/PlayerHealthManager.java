package com.exodus.core.player;

import com.exodus.core.api.player.BodyPart;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер компонентов здоровья игроков
 * Система 6 частей тела
 */
public class PlayerHealthManager {

    private static final Map<UUID, PlayerHealthComponent> components = new HashMap<>();

    /**
     * Получить компонент игрока (создаёт если не существует)
     */
    public static PlayerHealthComponent getComponent(Player player) {
        return components.computeIfAbsent(
                player.getUUID(),
                uuid -> new PlayerHealthComponent(player)
        );
    }

    /**
     * Удалить компонент игрока
     */
    public static void removeComponent(UUID uuid) {
        components.remove(uuid);
    }

    /**
     * Регистрация событий
     */
    public static void registerEvents() {
        // При респавне - полное восстановление HP всех частей тела
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) { // Игрок умер и респавнился
                PlayerHealthComponent component = getComponent(newPlayer);

                // ✅ Восстанавливаем HP всех частей тела
                for (BodyPart part : BodyPart.values()) {
                    component.getData().setBodyPartHP(part, part.getMaxHP());
                }

                // ✅ Убираем ВСЕ кровотечения
                for (BodyPart part : BodyPart.values()) {
                    if (component.getData().hasBleeding(part)) {
                        component.removeBleeding(part);
                    }
                }

                // ✅ Убираем ВСЕ переломы (включая бесконечные)
                for (BodyPart part : BodyPart.values()) {
                    if (component.getData().hasFracture(part)) {
                        component.removeFracture(part);
                    }
                }

                // ✅ Убираем боль
                component.removePain();
            }
        });

        // При переходе между мирами - копируем данные
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            PlayerHealthComponent oldComp = getComponent(oldPlayer);
            PlayerHealthComponent newComp = getComponent(newPlayer);

            CompoundTag nbt = new CompoundTag();
            oldComp.writeNbt(nbt);
            newComp.readNbt(nbt);
        });
    }
}