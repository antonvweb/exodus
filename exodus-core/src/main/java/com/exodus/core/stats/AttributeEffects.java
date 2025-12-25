package com.exodus.core.stats;

import com.exodus.core.api.stats.AttributeType;
import com.exodus.core.api.stats.VitalType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Система расчета влияния атрибутов на показатели персонажа
 * Все формулы и коэффициенты находятся здесь
 */
public class AttributeEffects {

    /**
     * Рассчитать максимальное значение показателя с учетом атрибутов
     * @param vitalType тип показателя
     * @param attributes текущие значения атрибутов
     * @return максимальное значение показателя
     */
    public static float calculateMaxVital(VitalType vitalType, Map<AttributeType, Integer> attributes) {
        float baseMax = vitalType.getBaseMax();
        float bonus = 0f;

        switch (vitalType) {
            case HEALTH -> {
                // Выносливость: +5 HP за уровень
                bonus += getAttributeValue(attributes, AttributeType.ENDURANCE) * 5f;
            }
            case OXYGEN -> {
                // Психика: +5 O2 за уровень
                bonus += getAttributeValue(attributes, AttributeType.SANITY) * 5f;
            }
            case ENERGY -> {
                // Выносливость: +10 энергии за уровень
                bonus += getAttributeValue(attributes, AttributeType.ENDURANCE) * 10f;
            }
            case HUNGER, THIRST -> {
                // Голод и жажда имеют фиксированный максимум
                // Но могут быть бонусы от других эффектов
            }
        }

        return baseMax + bonus;
    }

    /**
     * Рассчитать модификатор скорости уменьшения показателя
     * @param vitalType тип показателя
     * @param attributes текущие значения атрибутов
     * @return множитель скорости уменьшения (1.0 = нормально, 0.8 = медленнее на 20%)
     */
    public static float calculateDecayModifier(VitalType vitalType, Map<AttributeType, Integer> attributes) {
        float modifier = 1.0f;

        switch (vitalType) {
            case OXYGEN -> {
                // Интеллект: -1% расхода за уровень
                int intelligence = getAttributeValue(attributes, AttributeType.INTELLIGENCE);
                modifier -= intelligence * 0.01f;
            }
            case ENERGY -> {
                // Выносливость: +2% восстановления за уровень (обратный эффект)
                int endurance = getAttributeValue(attributes, AttributeType.ENDURANCE);
                modifier -= endurance * 0.02f;

                // Сила: -1% расхода при работе за уровень
                int strength = getAttributeValue(attributes, AttributeType.STRENGTH);
                modifier -= strength * 0.01f;
            }
            case HUNGER, THIRST -> {
                // Выносливость немного замедляет голод и жажду
                int endurance = getAttributeValue(attributes, AttributeType.ENDURANCE);
                modifier -= endurance * 0.005f; // -0.5% за уровень
            }
        }

        // Не даем модификатору стать отрицательным
        return Math.max(0.1f, modifier);
    }

    /**
     * Рассчитать бонус к скорости передвижения
     * @param attributes текущие значения атрибутов
     * @return множитель скорости (1.0 = нормально, 1.1 = +10%)
     */
    public static float calculateMovementSpeed(Map<AttributeType, Integer> attributes) {
        float baseSpeed = 1.0f;

        // Ловкость: +1% скорости за уровень
        int agility = getAttributeValue(attributes, AttributeType.AGILITY);
        baseSpeed += agility * 0.01f;

        return baseSpeed;
    }

    /**
     * Рассчитать бонус к урону
     * @param attributes текущие значения атрибутов
     * @return множитель урона (1.0 = нормально, 1.15 = +15%)
     */
    public static float calculateDamageBonus(Map<AttributeType, Integer> attributes) {
        float baseBonus = 1.0f;

        // Сила: +3% урона за уровень
        int strength = getAttributeValue(attributes, AttributeType.STRENGTH);
        baseBonus += strength * 0.03f;

        return baseBonus;
    }

    /**
     * Рассчитать шанс критического урона
     * @param attributes текущие значения атрибутов
     * @return шанс крита в процентах (0-100)
     */
    public static float calculateCritChance(Map<AttributeType, Integer> attributes) {
        float baseCrit = 5.0f; // 5% базовый шанс

        // Удача: +1% крита за уровень
        int luck = getAttributeValue(attributes, AttributeType.LUCK);
        baseCrit += luck * 1.0f;

        // Ловкость: +0.5% крита за уровень
        int agility = getAttributeValue(attributes, AttributeType.AGILITY);
        baseCrit += agility * 0.5f;

        return Math.min(baseCrit, 75.0f); // Максимум 75%
    }

    /**
     * Рассчитать максимальную грузоподъемность
     * @param attributes текущие значения атрибутов
     * @return максимальный вес в кг
     */
    public static float calculateMaxCarryWeight(Map<AttributeType, Integer> attributes) {
        float baseWeight = 50.0f; // 50 кг базово

        // Сила: +5 кг за уровень
        int strength = getAttributeValue(attributes, AttributeType.STRENGTH);
        baseWeight += strength * 5.0f;

        // Выносливость: +2 кг за уровень
        int endurance = getAttributeValue(attributes, AttributeType.ENDURANCE);
        baseWeight += endurance * 2.0f;

        return baseWeight;
    }

    /**
     * Рассчитать бонус к качеству крафта
     * @param attributes текущие значения атрибутов
     * @return множитель качества (1.0 = нормально, 1.2 = +20%)
     */
    public static float calculateCraftQuality(Map<AttributeType, Integer> attributes) {
        float baseQuality = 1.0f;

        // Интеллект: +1% качества за уровень
        int intelligence = getAttributeValue(attributes, AttributeType.INTELLIGENCE);
        baseQuality += intelligence * 0.01f;

        return baseQuality;
    }

    /**
     * Рассчитать сопротивление стрессу
     * @param attributes текущие значения атрибутов
     * @return сопротивление в процентах (0-100)
     */
    public static float calculateStressResistance(Map<AttributeType, Integer> attributes) {
        float baseResistance = 0.0f;

        // Психика: +3% сопротивления за уровень
        int sanity = getAttributeValue(attributes, AttributeType.SANITY);
        baseResistance += sanity * 3.0f;

        // Выносливость: +1% сопротивления за уровень
        int endurance = getAttributeValue(attributes, AttributeType.ENDURANCE);
        baseResistance += endurance * 1.0f;

        return Math.min(baseResistance, 90.0f); // Максимум 90%
    }

    /**
     * Получить значение атрибута из Map, или 0 если не найден
     */
    private static int getAttributeValue(Map<AttributeType, Integer> attributes, AttributeType type) {
        return attributes.getOrDefault(type, 0);
    }

    /**
     * Создать начальные значения атрибутов
     * Каждый атрибут начинается с уровня 1
     */
    public static Map<AttributeType, Integer> createDefaultAttributes() {
        Map<AttributeType, Integer> attributes = new EnumMap<>(AttributeType.class);
        for (AttributeType type : AttributeType.values()) {
            attributes.put(type, 1); // Все атрибуты начинаются с 1
        }
        return attributes;
    }
}