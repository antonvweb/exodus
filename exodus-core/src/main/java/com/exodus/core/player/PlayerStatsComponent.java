package com.exodus.core.player;

import com.exodus.core.api.player.PlayerStatsData;
import com.exodus.core.api.player.StatType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Компонент статов игрока
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

    // ============ СТАТЫ ============

    public int getStat(StatType stat) {
        return data.getStat(stat);
    }

    public void setStat(StatType stat, int value) {
        data.setStat(stat, value);
    }

    public boolean increaseStat(StatType stat) {
        return data.increaseStat(stat);
    }

    // ============ УРОВЕНЬ И ОПЫТ ============

    public int getLevel() {
        return data.getLevel();
    }

    public void setLevel(int level) {
        data.setLevel(level);
    }

    public float getExperience() {
        return data.getExperience();
    }

    public void setExperience(float experience) {
        data.setExperience(experience);
    }

    public void addExperience(float amount) {
        data.addExperience(amount);
    }

    public float getExperienceForNextLevel() {
        return data.getExperienceForNextLevel();
    }

    public int getFreePoints() {
        return data.getFreePoints();
    }

    public void setFreePoints(int points) {
        data.setFreePoints(points);
    }

    // ============ NBT ============

    public void writeNbt(CompoundTag nbt) {
        data.writeNbt(nbt);
    }

    public void readNbt(CompoundTag nbt) {
        data.readNbt(nbt);
    }
}
