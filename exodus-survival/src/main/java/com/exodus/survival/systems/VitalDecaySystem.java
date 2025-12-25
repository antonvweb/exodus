package com.exodus.survival.systems;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.stats.IPlayerStat;
import com.exodus.core.api.stats.VitalType;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Система уменьшения показателей выживания
 * Отвечает за постепенное уменьшение кислорода, голода, жажды и энергии
 */
public class VitalDecaySystem {

    private static final int DECAY_INTERVAL = 20; // Раз в секунду
    private static int tickCounter = 0;

    public static void tick(ServerPlayer player) {
        tickCounter++;

        if (tickCounter >= DECAY_INTERVAL) {
            tickCounter = 0;

            // Уменьшаем все показатели
            decayOxygen(player);
            decayHunger(player);
            decayThirst(player);
            decayEnergy(player);
        }
    }

    /**
     * Уменьшить кислород с учетом модификаторов от атрибутов
     */
    private static void decayOxygen(ServerPlayer player) {
        Optional<IPlayerStat> oxygenOpt = ExodusCoreAPI.getVital(player, VitalType.OXYGEN);

        if (oxygenOpt.isEmpty()) {
            return;
        }

        IPlayerStat oxygen = oxygenOpt.get();

        // Получаем базовую скорость уменьшения
        float baseDecayRate = VitalType.OXYGEN.getDecayRate();

        // Получаем модификатор от атрибутов (интеллект снижает расход)
        float modifier = ExodusCoreAPI.calculateDecayModifier(player, VitalType.OXYGEN);

        // Вычисляем реальное уменьшение
        float actualDecay = baseDecayRate * modifier;

        // Уменьшаем значение
        float newValue = Math.max(0, oxygen.getCurrent() - actualDecay);
        oxygen.setCurrent(newValue);
    }

    /**
     * Уменьшить голод с учетом модификаторов от атрибутов
     */
    private static void decayHunger(ServerPlayer player) {
        Optional<IPlayerStat> hungerOpt = ExodusCoreAPI.getVital(player, VitalType.HUNGER);

        if (hungerOpt.isEmpty()) {
            return;
        }

        IPlayerStat hunger = hungerOpt.get();

        float baseDecayRate = VitalType.HUNGER.getDecayRate();
        float modifier = ExodusCoreAPI.calculateDecayModifier(player, VitalType.HUNGER);
        float actualDecay = baseDecayRate * modifier;

        float newValue = Math.max(0, hunger.getCurrent() - actualDecay);
        hunger.setCurrent(newValue);
    }

    /**
     * Уменьшить жажду с учетом модификаторов от атрибутов
     */
    private static void decayThirst(ServerPlayer player) {
        Optional<IPlayerStat> thirstOpt = ExodusCoreAPI.getVital(player, VitalType.THIRST);

        if (thirstOpt.isEmpty()) {
            return;
        }

        IPlayerStat thirst = thirstOpt.get();

        float baseDecayRate = VitalType.THIRST.getDecayRate();
        float modifier = ExodusCoreAPI.calculateDecayModifier(player, VitalType.THIRST);
        float actualDecay = baseDecayRate * modifier;

        float newValue = Math.max(0, thirst.getCurrent() - actualDecay);
        thirst.setCurrent(newValue);
    }

    /**
     * Уменьшить энергию с учетом модификаторов от атрибутов
     */
    private static void decayEnergy(ServerPlayer player) {
        Optional<IPlayerStat> energyOpt = ExodusCoreAPI.getVital(player, VitalType.ENERGY);

        if (energyOpt.isEmpty()) {
            return;
        }

        IPlayerStat energy = energyOpt.get();

        float baseDecayRate = VitalType.ENERGY.getDecayRate();
        float modifier = ExodusCoreAPI.calculateDecayModifier(player, VitalType.ENERGY);
        float actualDecay = baseDecayRate * modifier;

        // Энергия уменьшается базово
        float newValue = Math.max(0, energy.getCurrent() - actualDecay);
        energy.setCurrent(newValue);

        // Дополнительное уменьшение от активности уже происходит в EnergySystem
    }
}