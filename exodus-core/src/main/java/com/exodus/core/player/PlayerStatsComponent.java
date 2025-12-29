package com.exodus.core.player;

import com.exodus.core.api.player.PlayerStatsData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Компонент статов игрока (УПРОЩЁННЫЙ)
 * 
 * Отвечает ТОЛЬКО за:
 * - Хранение данных
 * - Сохранение/загрузку (NBT)
 * 
 * ВСЯ ЛОГИКА находится в PlayerStatsData!
 * ПУБЛИЧНЫЙ API находится в ExodusCoreAPI!
 */
public class PlayerStatsComponent {

    private final Player player;
    private final PlayerStatsData data;

    public PlayerStatsComponent(Player player) {
        this.player = player;
        this.data = new PlayerStatsData();
    }

    /**
     * Получить данные статов
     */
    public PlayerStatsData getData() {
        return data;
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
