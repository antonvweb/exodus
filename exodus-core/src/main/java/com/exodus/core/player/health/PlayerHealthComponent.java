package com.exodus.core.player.health;

import com.exodus.core.api.player.PlayerHealthData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Компонент здоровья игрока (УПРОЩЁННЫЙ)
 * Система 6 частей тела
 * 
 * Отвечает ТОЛЬКО за:
 * - Хранение данных
 * - Обновление эффектов (tick)
 * - Сохранение/загрузку (NBT)
 * 
 * ВСЯ ЛОГИКА находится в PlayerHealthData!
 * ПУБЛИЧНЫЙ API находится в ExodusCoreAPI!
 */
public class PlayerHealthComponent {

    private final Player player;
    private final PlayerHealthData data;

    public PlayerHealthComponent(Player player) {
        this.player = player;
        this.data = new PlayerHealthData();
    }

    /**
     * Получить данные здоровья
     */
    public PlayerHealthData getData() {
        return data;
    }

    // ============ ОБНОВЛЕНИЕ ============

    /**
     * Обновление каждый тик
     * Обновляет таймеры эффектов
     */
    public void tick() {
        data.tickEffects();
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
