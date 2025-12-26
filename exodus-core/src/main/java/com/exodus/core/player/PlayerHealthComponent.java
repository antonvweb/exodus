package com.exodus.core.player;

import com.exodus.core.api.player.PlayerHealthData;
import com.exodus.core.api.player.StatusEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Компонент здоровья игрока
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

    /**
     * Нанести урон
     */
    public void damage(float amount) {
        data.damage(amount);
    }

    /**
     * Восстановить здоровье
     */
    public void heal(float amount) {
        data.heal(amount);
    }

    /**
     * Добавить статусный эффект
     */
    public void addEffect(StatusEffect effect, int duration, float intensity) {
        data.addEffect(effect, duration, intensity);
    }

    /**
     * Убрать статусный эффект
     */
    public void removeEffect(StatusEffect effect) {
        data.removeEffect(effect);
    }

    /**
     * Обновление каждый тик
     */
    public void tick() {
        data.tickEffects();
    }

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
