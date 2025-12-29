package com.exodus.core;

import com.exodus.core.api.player.BleedingType;
import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.player.PlayerHealthData;
import com.exodus.core.player.PlayerHealthComponent;
import com.exodus.core.player.PlayerHealthManager;
import net.minecraft.world.entity.player.Player;

/**
 * Главный API класс для Exodus Core
 * Система 6 частей тела
 */
public class ExodusCoreAPI {

    // ==================== КОМПОНЕНТ ЗДОРОВЬЯ ====================

    /**
     * Получить компонент здоровья игрока
     */
    public static PlayerHealthComponent getHealthComponent(Player player) {
        return PlayerHealthManager.getComponent(player);
    }

    /**
     * Получить данные здоровья игрока
     */
    public static PlayerHealthData getHealthData(Player player) {
        return getHealthComponent(player).getData();
    }

    // ==================== HP ЧАСТЕЙ ТЕЛА ====================

    /**
     * Получить HP части тела
     */
    public static float getBodyPartHP(Player player, BodyPart part) {
        return getHealthData(player).getBodyPartHP(part);
    }

    /**
     * Установить HP части тела
     */
    public static void setBodyPartHP(Player player, BodyPart part, float hp) {
        getHealthData(player).setBodyPartHP(part, hp);
    }

    /**
     * Нанести урон части тела
     */
    public static void damageBodyPart(Player player, BodyPart part, float damage) {
        getHealthData(player).damageBodyPart(part, damage);
    }

    /**
     * Восстановить HP части тела
     */
    public static void healBodyPart(Player player, BodyPart part, float amount) {
        getHealthData(player).healBodyPart(part, amount);
    }

    /**
     * Получить процент HP части тела
     */
    public static float getBodyPartHPPercentage(Player player, BodyPart part) {
        return getHealthData(player).getBodyPartHPPercentage(part);
    }

    /**
     * Получить состояние части тела
     */
    public static BodyPart.BodyPartState getBodyPartState(Player player, BodyPart part) {
        return getHealthData(player).getBodyPartState(part);
    }

    /**
     * Жив ли игрок
     */
    public static boolean isAlive(Player player) {
        return getHealthData(player).isAlive();
    }

    // ==================== КРОВОТЕЧЕНИЕ ====================

    /**
     * Добавить кровотечение на часть тела
     */
    public static void addBleeding(Player player, BodyPart part, BleedingType type) {
        getHealthData(player).addBleeding(part, type);
    }

    /**
     * Убрать кровотечение с части тела
     */
    public static void removeBleeding(Player player, BodyPart part) {
        getHealthData(player).removeBleeding(part);
    }

    /**
     * Проверить есть ли кровотечение на части тела
     */
    public static boolean hasBleeding(Player player, BodyPart part) {
        return getHealthData(player).hasBleeding(part);
    }

    /**
     * Получить тип кровотечения
     */
    public static BleedingType getBleedingType(Player player, BodyPart part) {
        return getHealthData(player).getBleedingType(part);
    }

    // ==================== ПЕРЕЛОМ ====================

    /**
     * Добавить перелом на часть тела (БЕСКОНЕЧНЫЙ)
     */
    public static void addFracture(Player player, BodyPart part, float intensity) {
        getHealthData(player).addFracture(part, intensity);
    }

    /**
     * Убрать перелом с части тела
     */
    public static void removeFracture(Player player, BodyPart part) {
        getHealthData(player).removeFracture(part);
    }

    /**
     * Проверить есть ли перелом на части тела
     */
    public static boolean hasFracture(Player player, BodyPart part) {
        return getHealthData(player).hasFracture(part);
    }

    /**
     * Получить интенсивность перелома
     */
    public static float getFractureIntensity(Player player, BodyPart part) {
        return getHealthData(player).getFractureIntensity(part);
    }

    // ==================== БОЛЬ (ГЛОБАЛЬНАЯ) ====================

    /**
     * Добавить боль
     */
    public static void addPain(Player player, int duration, float intensity) {
        getHealthData(player).addPain(duration, intensity);
    }

    /**
     * Убрать боль
     */
    public static void removePain(Player player) {
        getHealthData(player).removePain();
    }

    /**
     * Проверить есть ли боль
     */
    public static boolean hasPain(Player player) {
        return getHealthData(player).hasPain();
    }

    /**
     * Получить интенсивность боли
     */
    public static float getPainIntensity(Player player) {
        return getHealthData(player).getPainIntensity();
    }
}
