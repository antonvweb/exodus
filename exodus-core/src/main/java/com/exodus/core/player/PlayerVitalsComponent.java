package com.exodus.core.player;

import com.exodus.core.api.player.PlayerVitalsData;
import com.exodus.core.api.player.VitalType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Компонент витальных показателей игрока
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

    // ============ СЫТОСТЬ ============

    public float getHunger() {
        return data.getHunger();
    }

    public void setHunger(float value) {
        data.setHunger(value);
    }

    public void addHunger(float amount) {
        data.addHunger(amount);
    }

    // ============ ЖАЖДА ============

    public float getThirst() {
        return data.getThirst();
    }

    public void setThirst(float value) {
        data.setThirst(value);
    }

    public void addThirst(float amount) {
        data.addThirst(amount);
    }

    // ============ ЭНЕРГИЯ ============

    public float getEnergy() {
        return data.getEnergy();
    }

    public void setEnergy(float value) {
        data.setEnergy(value);
    }

    public void addEnergy(float amount) {
        data.addEnergy(amount);
    }

    public float getMaxEnergy() {
        return data.getMaxEnergy();
    }

    public boolean canPerformAction(float cost) {
        return data.canPerformAction(cost);
    }

    public boolean consumeEnergy(float cost) {
        return data.consumeEnergy(cost);
    }

    // ============ КИСЛОРОД ============

    public float getOxygen() {
        return data.getOxygen();
    }

    public void setOxygen(float value) {
        data.setOxygen(value);
    }

    public void addOxygen(float amount) {
        data.addOxygen(amount);
    }

    public float getMaxOxygen() {
        return data.getMaxOxygen();
    }

    // ============ ТЕМПЕРАТУРА ============

    public float getTemperature() {
        return data.getTemperature();
    }

    public void setTemperature(float value) {
        data.setTemperature(value);
    }

    public void addTemperature(float amount) {
        data.addTemperature(amount);
    }

    // ============ ПСИХИКА ============

    public float getMental() {
        return data.getMental();
    }

    public void setMental(float value) {
        data.setMental(value);
    }

    public void addMental(float amount) {
        data.addMental(amount);
    }

    // ============ ОБЩИЕ ============

    public float getVital(VitalType type) {
        return data.getVital(type);
    }

    public void setVital(VitalType type, float value) {
        data.setVital(type, value);
    }

    // ============ ОБНОВЛЕНИЕ ============

    /**
     * Обновить максимальные значения на основе статов
     */
    public void updateMaxValues(PlayerStatsComponent statsComponent) {
        data.updateMaxValues(statsComponent.getData());
    }

    // ============ NBT ============

    public void writeNbt(CompoundTag nbt) {
        data.writeNbt(nbt);
    }

    public void readNbt(CompoundTag nbt) {
        data.readNbt(nbt);
    }
}
