package com.exodus.core;

import com.exodus.core.api.player.*;
import com.exodus.core.player.PlayerHealthComponent;
import com.exodus.core.player.PlayerHealthManager;
import com.exodus.core.player.PlayerStatsComponent;
import com.exodus.core.player.PlayerVitalsComponent;
import net.minecraft.world.entity.player.Player;

/**
 * Главный API класс для Exodus Core
 * Управляет: здоровьем (6 частей тела), статами (7 статов), витальными показателями (6 виталов)
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

    // ==================== КОМПОНЕНТ СТАТОВ ====================

    /**
     * Получить компонент статов игрока
     */
    public static PlayerStatsComponent getStatsComponent(Player player) {
        return PlayerHealthManager.getStatsComponent(player);
    }

    /**
     * Получить данные статов игрока
     */
    public static PlayerStatsData getStatsData(Player player) {
        return getStatsComponent(player).getData();
    }

    // ==================== СТАТЫ ====================

    /**
     * Получить значение стата
     */
    public static int getStat(Player player, StatType stat) {
        return getStatsData(player).getStat(stat);
    }

    /**
     * Установить значение стата
     */
    public static void setStat(Player player, StatType stat, int value) {
        getStatsData(player).setStat(stat, value);
    }

    /**
     * Увеличить стат на 1
     */
    public static boolean increaseStat(Player player, StatType stat) {
        return getStatsData(player).increaseStat(stat);
    }

    // ==================== УРОВЕНЬ И ОПЫТ ====================

    /**
     * Получить уровень игрока
     */
    public static int getLevel(Player player) {
        return getStatsData(player).getLevel();
    }

    /**
     * Установить уровень игрока
     */
    public static void setLevel(Player player, int level) {
        getStatsData(player).setLevel(level);
    }

    /**
     * Получить текущий опыт
     */
    public static float getExperience(Player player) {
        return getStatsData(player).getExperience();
    }

    /**
     * Добавить опыт (с учётом бонуса от интеллекта)
     */
    public static void addExperience(Player player, float amount) {
        getStatsData(player).addExperience(amount);
    }

    /**
     * Получить свободные очки статов
     */
    public static int getFreePoints(Player player) {
        return getStatsData(player).getFreePoints();
    }

    // ==================== ПРОИЗВОДНЫЕ ПАРАМЕТРЫ ОТ СТАТОВ ====================

    /**
     * Получить модификатор урона ближнего боя
     */
    public static float getMeleeDamageModifier(Player player) {
        return getStatsData(player).getMeleeDamageModifier();
    }

    /**
     * Получить переносимый вес
     */
    public static float getCarryWeight(Player player) {
        return getStatsData(player).getCarryWeight();
    }

    /**
     * Получить модификатор максимального HP
     */
    public static float getMaxHPModifier(Player player) {
        return getStatsData(player).getMaxHPModifier();
    }

    /**
     * Получить максимальную стамину
     */
    public static float getMaxStamina(Player player) {
        return getStatsData(player).getMaxStamina();
    }

    /**
     * Получить эффективность лечения
     */
    public static float getHealingEfficiency(Player player) {
        return getStatsData(player).getHealingEfficiency();
    }

    /**
     * Получить критический шанс
     */
    public static float getTotalCritChance(Player player) {
        return getStatsData(player).getTotalCritChance();
    }

    /**
     * Получить точность
     */
    public static float getTotalAccuracy(Player player) {
        return getStatsData(player).getTotalAccuracy();
    }

    /**
     * Получить уклонение
     */
    public static float getTotalEvasion(Player player) {
        return getStatsData(player).getTotalEvasion();
    }

    /**
     * Получить сопротивление кровотечению
     */
    public static float getTotalBleedResistance(Player player) {
        return getStatsData(player).getTotalBleedResistance();
    }

    /**
     * Получить сопротивление переломам
     */
    public static float getTotalFractureResistance(Player player) {
        return getStatsData(player).getTotalFractureResistance();
    }

    // ==================== КОМПОНЕНТ ВИТАЛЬНЫХ ПОКАЗАТЕЛЕЙ ====================

    /**
     * Получить компонент витальных показателей игрока
     */
    public static PlayerVitalsComponent getVitalsComponent(Player player) {
        return PlayerHealthManager.getVitalsComponent(player);
    }

    /**
     * Получить данные витальных показателей игрока
     */
    public static PlayerVitalsData getVitalsData(Player player) {
        return getVitalsComponent(player).getData();
    }

    // ==================== СЫТОСТЬ ====================

    /**
     * Получить сытость
     */
    public static float getHunger(Player player) {
        return getVitalsData(player).getHunger();
    }

    /**
     * Установить сытость
     */
    public static void setHunger(Player player, float value) {
        getVitalsData(player).setHunger(value);
    }

    /**
     * Добавить/убавить сытость
     */
    public static void addHunger(Player player, float amount) {
        getVitalsData(player).addHunger(amount);
    }

    // ==================== ЖАЖДА ====================

    /**
     * Получить жажду
     */
    public static float getThirst(Player player) {
        return getVitalsData(player).getThirst();
    }

    /**
     * Установить жажду
     */
    public static void setThirst(Player player, float value) {
        getVitalsData(player).setThirst(value);
    }

    /**
     * Добавить/убавить жажду
     */
    public static void addThirst(Player player, float amount) {
        getVitalsData(player).addThirst(amount);
    }

    // ==================== ЭНЕРГИЯ/СТАМИНА ====================

    /**
     * Получить энергию
     */
    public static float getEnergy(Player player) {
        return getVitalsData(player).getEnergy();
    }

    /**
     * Установить энергию
     */
    public static void setEnergy(Player player, float value) {
        getVitalsData(player).setEnergy(value);
    }

    /**
     * Добавить/убавить энергию
     */
    public static void addEnergy(Player player, float amount) {
        getVitalsData(player).addEnergy(amount);
    }

    /**
     * Получить максимальную энергию
     */
    public static float getMaxEnergy(Player player) {
        return getVitalsData(player).getMaxEnergy();
    }

    /**
     * Можно ли выполнить действие (хватает ли энергии)
     */
    public static boolean canPerformAction(Player player, float cost) {
        return getVitalsData(player).canPerformAction(cost);
    }

    /**
     * Потратить энергию на действие
     */
    public static boolean consumeEnergy(Player player, float cost) {
        return getVitalsData(player).consumeEnergy(cost);
    }

    // ==================== КИСЛОРОД ====================

    /**
     * Получить кислород
     */
    public static float getOxygen(Player player) {
        return getVitalsData(player).getOxygen();
    }

    /**
     * Установить кислород
     */
    public static void setOxygen(Player player, float value) {
        getVitalsData(player).setOxygen(value);
    }

    /**
     * Добавить/убавить кислород
     */
    public static void addOxygen(Player player, float amount) {
        getVitalsData(player).addOxygen(amount);
    }

    /**
     * Получить максимальный кислород
     */
    public static float getMaxOxygen(Player player) {
        return getVitalsData(player).getMaxOxygen();
    }

    // ==================== ТЕМПЕРАТУРА ====================

    /**
     * Получить температуру тела
     */
    public static float getTemperature(Player player) {
        return getVitalsData(player).getTemperature();
    }

    /**
     * Установить температуру тела
     */
    public static void setTemperature(Player player, float value) {
        getVitalsData(player).setTemperature(value);
    }

    /**
     * Добавить/убавить температуру
     */
    public static void addTemperature(Player player, float amount) {
        getVitalsData(player).addTemperature(amount);
    }

    // ==================== ПСИХИКА ====================

    /**
     * Получить психику/рассудок
     */
    public static float getMental(Player player) {
        return getVitalsData(player).getMental();
    }

    /**
     * Установить психику
     */
    public static void setMental(Player player, float value) {
        getVitalsData(player).setMental(value);
    }

    /**
     * Добавить/убавить психику
     */
    public static void addMental(Player player, float amount) {
        getVitalsData(player).addMental(amount);
    }

    /**
     * Получить состояние психики
     */
    public static PlayerVitalsData.MentalState getMentalState(Player player) {
        return getVitalsData(player).getMentalState();
    }

    // ==================== ОБЩИЕ МЕТОДЫ ДЛЯ ВИТАЛОВ ====================

    /**
     * Получить значение витального показателя
     */
    public static float getVital(Player player, VitalType type) {
        return getVitalsData(player).getVital(type);
    }

    /**
     * Установить значение витального показателя
     */
    public static void setVital(Player player, VitalType type, float value) {
        getVitalsData(player).setVital(type, value);
    }

    /**
     * Получить максимальное значение витального показателя
     */
    public static float getMaxVital(Player player, VitalType type) {
        return getVitalsData(player).getMaxVital(type);
    }

    /**
     * Получить процент витального показателя
     */
    public static float getVitalPercentage(Player player, VitalType type) {
        return getVitalsData(player).getVitalPercentage(type);
    }
}
