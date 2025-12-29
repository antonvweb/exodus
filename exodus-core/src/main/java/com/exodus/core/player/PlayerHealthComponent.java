package com.exodus.core.player;

import com.exodus.core.api.player.BleedingType;
import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.player.PlayerHealthData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Компонент здоровья игрока
 * Система 6 частей тела
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

    // ============ HP ЧАСТЕЙ ТЕЛА ============

    /**
     * Нанести урон части тела
     */
    public void damageBodyPart(BodyPart part, float amount) {
        data.damageBodyPart(part, amount);
    }

    /**
     * Восстановить здоровье части тела
     */
    public void healBodyPart(BodyPart part, float amount) {
        data.healBodyPart(part, amount);
    }

    // ============ ЭФФЕКТЫ ============

    /**
     * Добавить кровотечение на часть тела
     */
    public void addBleeding(BodyPart part, BleedingType type) {
        data.addBleeding(part, type);
    }

    /**
     * Убрать кровотечение с части тела
     */
    public void removeBleeding(BodyPart part) {
        data.removeBleeding(part);
    }

    /**
     * Добавить перелом на часть тела
     */
    public void addFracture(BodyPart part, float intensity) {
        data.addFracture(part, intensity);
    }

    /**
     * Убрать перелом с части тела
     */
    public void removeFracture(BodyPart part) {
        data.removeFracture(part);
    }

    /**
     * Добавить боль (глобальную)
     */
    public void addPain(int duration, float intensity) {
        data.addPain(duration, intensity);
    }

    /**
     * Убрать боль
     */
    public void removePain() {
        data.removePain();
    }

    // ============ ОБНОВЛЕНИЕ ============

    /**
     * Обновление каждый тик
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