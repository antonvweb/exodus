package com.exodus.core.player;

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
    private static final Map<UUID, PlayerStatsComponent> statsComponents = new HashMap<>();
    private static final Map<UUID, PlayerHealthComponent> components = new HashMap<>();
    private static final Map<UUID, PlayerVitalsComponent> vitalsComponents = new HashMap<>();

    // ============ СТАТЫ ============

    /**
     * Получить компонент статов игрока (создаёт если не существует)
     */
    public static PlayerStatsComponent getStatsComponent(Player player) {
        return statsComponents.computeIfAbsent(
                player.getUUID(),
                uuid -> new PlayerStatsComponent(player)
        );
    }

    /**
     * Удалить компонент статов игрока
     */
    public static void removeStatsComponent(UUID uuid) {
        statsComponents.remove(uuid);
    }

    // ============ ВИТАЛЬНЫЕ ПОКАЗАТЕЛИ ============

    /**
     * Получить компонент витальных показателей игрока (создаёт если не существует)
     */
    public static PlayerVitalsComponent getVitalsComponent(Player player) {
        return vitalsComponents.computeIfAbsent(
                player.getUUID(),
                uuid -> {
                    PlayerVitalsComponent vitals = new PlayerVitalsComponent(player);
                    // Обновляем максимумы на основе статов
                    vitals.updateMaxValues(getStatsComponent(player));
                    return vitals;
                }
        );
    }

    /**
     * Удалить компонент витальных показателей игрока
     */
    public static void removeVitalsComponent(UUID uuid) {
        vitalsComponents.remove(uuid);
    }

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
                PlayerStatsComponent statsComp = getStatsComponent(newPlayer);
                PlayerVitalsComponent vitalsComp = getVitalsComponent(newPlayer);

                // ✅ Восстанавливаем HP всех частей тела
                for (BodyPart part : BodyPart.values()) {
                    healthComp.getData().setBodyPartHP(part, part.getMaxHP());
                }

                // ✅ Убираем ВСЕ кровотечения
                for (BodyPart part : BodyPart.values()) {
                    if (healthComp.getData().hasBleeding(part)) {
                        healthComp.removeBleeding(part);
                    }
                }

                // ✅ Убираем ВСЕ переломы
                for (BodyPart part : BodyPart.values()) {
                    if (healthComp.getData().hasFracture(part)) {
                        healthComp.removeFracture(part);
                    }
                }

                // ✅ Убираем боль
                healthComp.removePain();

                // ✅ Восстанавливаем витальные показатели
                vitalsComp.setHunger(100.0f);
                vitalsComp.setThirst(100.0f);
                vitalsComp.setEnergy(vitalsComp.getMaxEnergy());
                vitalsComp.setOxygen(vitalsComp.getMaxOxygen());
                vitalsComp.setTemperature(37.0f);
                vitalsComp.setMental(100.0f);

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

            // ✅ Копируем статы
            PlayerStatsComponent oldStatsComp = getStatsComponent(oldPlayer);
            PlayerStatsComponent newStatsComp = getStatsComponent(newPlayer);

            CompoundTag statsNbt = new CompoundTag();
            oldStatsComp.writeNbt(statsNbt);
            newStatsComp.readNbt(statsNbt);

            // ✅ Копируем витальные показатели
            PlayerVitalsComponent oldVitalsComp = getVitalsComponent(oldPlayer);
            PlayerVitalsComponent newVitalsComp = getVitalsComponent(newPlayer);

            CompoundTag vitalsNbt = new CompoundTag();
            oldVitalsComp.writeNbt(vitalsNbt);
            newVitalsComp.readNbt(vitalsNbt);
        });
    }
}
