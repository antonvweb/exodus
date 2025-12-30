package com.exodus.core.player.vitals;

import com.exodus.core.api.player.PlayerVitalsData;
import com.exodus.core.player.stats.PlayerStatsComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Компонент витальных показателей игрока (УПРОЩЁННЫЙ)
 * 
 * Отвечает ТОЛЬКО за:
 * - Хранение данных
 * - Обновление максимумов на основе статов
 * - Сохранение/загрузку (NBT)
 * 
 * ВСЯ ЛОГИКА находится в PlayerVitalsData!
 * ПУБЛИЧНЫЙ API находится в ExodusCoreAPI!
 */
public class PlayerVitalsComponent {

    private final Player player;
    private final PlayerVitalsData data;

    public PlayerVitalsComponent(Player player) {
        this.player = player;
        this.data = new PlayerVitalsData();
    }

    /**
     * Получить данные витальных показателей
     */
    public PlayerVitalsData getData() {
        return data;
    }

    // ============ ОБНОВЛЕНИЕ МАКСИМУМОВ ============

    /**
     * Обновить максимальные значения на основе статов
     * Вызывается когда статы изменяются
     */
    public void updateMaxValues(PlayerStatsComponent statsComponent) {
        data.updateMaxValues(statsComponent.getData());
    }

    // ============ NBT ============

    /**
     * Сохранить в NBT
     */
    public void writeNbt(CompoundTag nbt) {
        data.writeNbt(nbt);
    }

    /**
     * Загрузить из NBT
     */
    public void readNbt(CompoundTag nbt) {
        data.readNbt(nbt);
    }
}
