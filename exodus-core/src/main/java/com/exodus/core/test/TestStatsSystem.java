package com.exodus.core.test;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.stats.AttributeType;
import com.exodus.core.api.stats.IPlayerStat;
import com.exodus.core.api.stats.VitalType;
import net.minecraft.server.level.ServerPlayer;

/**
 * ВРЕМЕННАЯ система для тестирования API
 * Создает тестовые статы и постепенно их уменьшает
 * Демонстрирует работу системы атрибутов
 */
public class TestStatsSystem {

    public static void initializeTestStats(ServerPlayer player) {
        // Инициализируем атрибуты (если нужно для теста)
        // По умолчанию все атрибуты = 1, но можем увеличить для демонстрации

        // Например, даем игроку бонусные уровни для теста:
        ExodusCoreAPI.setAttribute(player, AttributeType.ENDURANCE, 5); // +25 HP, +50 Energy
        ExodusCoreAPI.setAttribute(player, AttributeType.SANITY, 3); // +15 O2
        ExodusCoreAPI.setAttribute(player, AttributeType.INTELLIGENCE, 2); // -2% расход O2

        // Создаем показатели если их нет
        if (!ExodusCoreAPI.hasVital(player, VitalType.HEALTH)) {
            IPlayerStat health = ExodusCoreAPI.getOrCreateVital(player, VitalType.HEALTH);
            health.setCurrent(health.getMax()); // Полное HP
        }

        if (!ExodusCoreAPI.hasVital(player, VitalType.OXYGEN)) {
            IPlayerStat oxygen = ExodusCoreAPI.getOrCreateVital(player, VitalType.OXYGEN);
            oxygen.setCurrent(oxygen.getMax()); // Полный кислород
        }

        if (!ExodusCoreAPI.hasVital(player, VitalType.THIRST)) {
            IPlayerStat thirst = ExodusCoreAPI.getOrCreateVital(player, VitalType.THIRST);
            thirst.setCurrent(thirst.getMax());
        }

        if (!ExodusCoreAPI.hasVital(player, VitalType.HUNGER)) {
            IPlayerStat hunger = ExodusCoreAPI.getOrCreateVital(player, VitalType.HUNGER);
            hunger.setCurrent(hunger.getMax());
        }

        if (!ExodusCoreAPI.hasVital(player, VitalType.ENERGY)) {
            IPlayerStat energy = ExodusCoreAPI.getOrCreateVital(player, VitalType.ENERGY);
            energy.setCurrent(energy.getMax());
        }
    }

    public static void updateTestStats(ServerPlayer player) {
        // Обновляем каждый показатель с учетом модификаторов от атрибутов

        // КИСЛОРОД - уменьшается с модификатором от интеллекта
        ExodusCoreAPI.getVital(player, VitalType.OXYGEN).ifPresent(stat -> {
            float decayRate = VitalType.OXYGEN.getDecayRate();
            float modifier = ExodusCoreAPI.calculateDecayModifier(player, VitalType.OXYGEN);
            float actualDecay = decayRate * modifier;

            float newValue = stat.getCurrent() - actualDecay;
            if (newValue < 0) {
                newValue = 0;
                // TODO: В будущем добавить урон от удушья
            }
            stat.setCurrent(newValue);
        });

        // ЖАЖДА - уменьшается с модификатором от выносливости
        ExodusCoreAPI.getVital(player, VitalType.THIRST).ifPresent(stat -> {
            float decayRate = VitalType.THIRST.getDecayRate();
            float modifier = ExodusCoreAPI.calculateDecayModifier(player, VitalType.THIRST);
            float actualDecay = decayRate * modifier;

            float newValue = stat.getCurrent() - actualDecay;
            if (newValue < 0) {
                newValue = 0;
                // TODO: В будущем добавить негативные эффекты от обезвоживания
            }
            stat.setCurrent(newValue);
        });

        // ГОЛОД - уменьшается с модификатором от выносливости
        ExodusCoreAPI.getVital(player, VitalType.HUNGER).ifPresent(stat -> {
            float decayRate = VitalType.HUNGER.getDecayRate();
            float modifier = ExodusCoreAPI.calculateDecayModifier(player, VitalType.HUNGER);
            float actualDecay = decayRate * modifier;

            float newValue = stat.getCurrent() - actualDecay;
            if (newValue < 0) {
                newValue = 0;
                // TODO: В будущем добавить негативные эффекты от голода
            }
            stat.setCurrent(newValue);
        });

        // ЭНЕРГИЯ - уменьшается с модификатором от выносливости и силы
        ExodusCoreAPI.getVital(player, VitalType.ENERGY).ifPresent(stat -> {
            float decayRate = VitalType.ENERGY.getDecayRate();
            float modifier = ExodusCoreAPI.calculateDecayModifier(player, VitalType.ENERGY);
            float actualDecay = decayRate * modifier;

            float newValue = stat.getCurrent() - actualDecay;
            if (newValue < 0) {
                newValue = 0;
                // TODO: В будущем добавить замедление при низкой энергии
            }
            stat.setCurrent(newValue);
        });

        // ЗДОРОВЬЕ не уменьшается само по себе
    }

    /**
     * Вспомогательный метод для отладки - выводит информацию о текущих бонусах
     */
    public static void debugPrintBonuses(ServerPlayer player) {
        System.out.println("=== Player Stats Debug ===");
        System.out.println("Attributes:");
        for (AttributeType type : AttributeType.values()) {
            int level = ExodusCoreAPI.getAttribute(player, type);
            System.out.println("  " + type.getDisplayName() + ": " + level);
        }

        System.out.println("\nCalculated Bonuses:");
        System.out.println("  Max Health: " + ExodusCoreAPI.calculateMaxVital(player, VitalType.HEALTH));
        System.out.println("  Max Oxygen: " + ExodusCoreAPI.calculateMaxVital(player, VitalType.OXYGEN));
        System.out.println("  Max Energy: " + ExodusCoreAPI.calculateMaxVital(player, VitalType.ENERGY));
        System.out.println("  Movement Speed: " + (ExodusCoreAPI.calculateMovementSpeed(player) * 100 - 100) + "%");
        System.out.println("  Damage Bonus: " + (ExodusCoreAPI.calculateDamageBonus(player) * 100 - 100) + "%");
        System.out.println("  Crit Chance: " + ExodusCoreAPI.calculateCritChance(player) + "%");
        System.out.println("  Max Carry Weight: " + ExodusCoreAPI.calculateMaxCarryWeight(player) + " kg");
        System.out.println("=======================");
    }
}