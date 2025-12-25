package com.exodus.core.stats;

import com.exodus.core.api.events.VitalChangeCallback;
import com.exodus.core.api.stats.IPlayerStat;
import com.exodus.core.api.stats.VitalType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Показатель выживания игрока (Health, Oxygen, Hunger, etc)
 * В отличие от атрибутов, показатели изменяются динамически
 */
public class PlayerVital implements IPlayerStat {

    private final VitalType type;
    private final Player player;
    private float current;
    private float max;

    public PlayerVital(VitalType type, Player player, float max) {
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
     * Получить тип показателя (типизированная версия)
     */
    public VitalType getVitalType() {
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
            VitalChangeCallback.EVENT.invoker().onVitalChange(player, type, oldValue, this.current);
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
     * Получить скорость уменьшения показателя
     */
    public float getDecayRate() {
        return type.getDecayRate();
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