package com.exodus.core.stats;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер статов всех игроков
 * Управляет созданием, сохранением и загрузкой компонентов статов
 */
public class PlayerStatsManager {

    private static final Map<UUID, PlayerStatsComponent> playerStats = new HashMap<>();

    /**
     * Получить компонент статов игрока
     */
    public static PlayerStatsComponent getStats(Player player) {
        return playerStats.computeIfAbsent(player.getUUID(), uuid -> new PlayerStatsComponent(player));
    }

    /**
     * Удалить компонент статов (когда игрок выходит)
     */
    public static void removeStats(UUID playerUuid) {
        playerStats.remove(playerUuid);
    }

    /**
     * Регистрация событий для автоматического сохранения/загрузки
     */
    public static void registerEvents() {
        // При входе игрока - загрузить данные
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) { // Игрок умер и респавнился
                // Копируем статы из старого игрока
                PlayerStatsComponent oldStats = getStats(oldPlayer);
                PlayerStatsComponent newStats = getStats(newPlayer);

                // Сохраняем в NBT и загружаем обратно (клонирование)
                CompoundTag nbt = new CompoundTag();
                oldStats.writeNbt(nbt);
                newStats.readNbt(nbt);
            }
        });

        // При выходе игрока - сохранить данные
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            // Копируем данные при переходе между мирами
            PlayerStatsComponent oldStats = getStats(oldPlayer);
            PlayerStatsComponent newStats = getStats(newPlayer);

            CompoundTag nbt = new CompoundTag();
            oldStats.writeNbt(nbt);
            newStats.readNbt(nbt);
        });
    }

    /**
     * Сохранить статы игрока в NBT (вызывается при сохранении мира)
     */
    public static CompoundTag savePlayerData(ServerPlayer player) {
        CompoundTag nbt = new CompoundTag();
        PlayerStatsComponent stats = getStats(player);
        stats.writeNbt(nbt);
        return nbt;
    }

    /**
     * Загрузить статы игрока из NBT (вызывается при загрузке мира)
     */
    public static void loadPlayerData(ServerPlayer player, CompoundTag nbt) {
        PlayerStatsComponent stats = getStats(player);
        stats.readNbt(nbt);
    }
}