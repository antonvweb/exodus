package com.exodus.core.stats;

import com.exodus.core.api.events.StatChangeCallback;
import com.exodus.core.api.stats.IPlayerStat;
import com.exodus.core.api.stats.StatType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Базовая реализация стата игрока (DEPRECATED - используйте PlayerVital)
 * Оставлено для обратной совместимости
 */
@Deprecated
public class PlayerStat implements IPlayerStat {

    private final StatType type;
    private final Player player;
    private float current;
    private float max;

    public PlayerStat(StatType type, Player player, float max) {
        this.type = type;
        this.player = player;
        this.current = max;
        this.max = max;
    }

    @Override
    public Object getType() {
        return type;
    }

    /**
     * Получить тип стата (типизированная версия)
     */
    public StatType getStatType() {
        return type;
    }

    @Override
    public float getCurrent() {
        return current;
    }

    @Override
    public float getMax() {
        return max;
    }

    @Override
    public void setCurrent(float value) {
        float oldValue = this.current;
        this.current = Math.max(0, Math.min(value, max));

        // Вызываем событие если значение изменилось
        if (oldValue != this.current) {
            StatChangeCallback.EVENT.invoker().onStatChange(player, type, oldValue, this.current);
        }
    }

    @Override
    public void setMax(float max) {
        this.max = Math.max(1, max);
        // Корректируем текущее значение если оно больше нового максимума
        if (this.current > this.max) {
            setCurrent(this.max);
        }
    }

    @Override
    public String getDisplayName() {
        return type.getDisplayName();
    }

    @Override
    public int getColor() {
        return type.getDefaultColor();
    }

    /**
     * Сохранить в NBT
     */
    public CompoundTag writeNbt(CompoundTag nbt) {
        nbt.putFloat("current", current);
        nbt.putFloat("max", max);
        return nbt;
    }

    /**
     * Загрузить из NBT
     */
    public void readNbt(CompoundTag nbt) {
        this.current = nbt.getFloat("current");
        this.max = nbt.getFloat("max");
    }
}