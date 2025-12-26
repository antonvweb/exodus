package com.exodus.core.player;

import com.exodus.core.api.player.StatusEffect;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер компонентов здоровья игроков
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
        // При респавне - полное восстановление HP
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) { // Игрок умер и респавнился
                PlayerHealthComponent component = getComponent(newPlayer);
                component.getData().setCurrentHP(component.getData().getMaxHP());
                
                // Убираем все эффекты
                for (var effect : StatusEffect.values()) {
                    component.removeEffect(effect);
                }
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
