package com.exodus.survival.systems;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.stats.IPlayerStat;
import com.exodus.core.api.stats.VitalType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Optional;

/**
 * Система энергии
 * Управляет усталостью и влиянием на скорость игрока
 */
public class EnergySystem {

    private static final int EFFECT_CHECK_INTERVAL = 20; // Проверяем раз в секунду
    private static int tickCounter = 0;

    public static void tick(ServerPlayer player) {
        tickCounter++;

        if (tickCounter >= EFFECT_CHECK_INTERVAL) {
            tickCounter = 0;

            applyEnergyEffects(player);
            checkEnergyReplenishment(player);
        }

        // Увеличенный расход энергии при активных действиях
        checkActiveEnergyConsumption(player);
    }

    /**
     * Применить эффекты от усталости
     */
    private static void applyEnergyEffects(ServerPlayer player) {
        Optional<IPlayerStat> energyOpt = ExodusCoreAPI.getVital(player, VitalType.ENERGY);

        if (energyOpt.isEmpty()) {
            return;
        }

        IPlayerStat energy = energyOpt.get();
        float energyPercent = energy.getPercentage();

        // Критическая усталость (0-10%)
        if (energyPercent <= 0.1f) {
            // Сильное замедление + слабость + тошнота
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 2, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 2, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 0, false, false));
        }
        // Сильная усталость (10-25%)
        else if (energyPercent <= 0.25f) {
            // Замедление + слабость
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 1, false, false));
        }
        // Средняя усталость (25-50%)
        else if (energyPercent <= 0.5f) {
            // Легкое замедление
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, false, false));
        }
        // Легкая усталость (50-75%)
        else if (energyPercent <= 0.75f) {
            // Минимальное замедление копания
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, false, false));
        }
        // Нормальная энергия (75-100%) - без эффектов
    }

    /**
     * Проверить восстановление энергии
     */
    private static void checkEnergyReplenishment(ServerPlayer player) {
        Optional<IPlayerStat> energyOpt = ExodusCoreAPI.getVital(player, VitalType.ENERGY);

        if (energyOpt.isEmpty()) {
            return;
        }

        IPlayerStat energy = energyOpt.get();

        // Восстановление энергии когда игрок стоит на месте
        if (!player.isSprinting() &&
                !player.isSwimming() &&
                player.getDeltaMovement().horizontalDistanceSqr() < 0.001) {

            // Медленное восстановление энергии
            float restoreRate = 0.5f; // 0.5 единицы в секунду
            float newValue = Math.min(energy.getCurrent() + restoreRate, energy.getMax());
            energy.setCurrent(newValue);
        }

        // Ускоренное восстановление если игрок не двигается и присел
        if (player.isCrouching() &&
                player.getDeltaMovement().horizontalDistanceSqr() < 0.001) {

            float fastRestoreRate = 1.5f; // 1.5 единицы в секунду
            float newValue = Math.min(energy.getCurrent() + fastRestoreRate, energy.getMax());
            energy.setCurrent(newValue);
        }
    }

    /**
     * Проверить увеличенный расход энергии при активности
     */
    private static void checkActiveEnergyConsumption(ServerPlayer player) {
        Optional<IPlayerStat> energyOpt = ExodusCoreAPI.getVital(player, VitalType.ENERGY);

        if (energyOpt.isEmpty()) {
            return;
        }

        IPlayerStat energy = energyOpt.get();

        // Увеличенный расход при спринте (каждый тик)
        if (player.isSprinting()) {
            energy.add(-0.05f); // Дополнительные -0.05 за тик = -1 в секунду
        }

        // Увеличенный расход при плавании
        if (player.isSwimming()) {
            energy.add(-0.03f); // -0.03 за тик = -0.6 в секунду
        }

        // Увеличенный расход при прыжках (проверяем что игрок в воздухе и движется вверх)
        if (!player.onGround() && player.getDeltaMovement().y > 0) {
            energy.add(-0.01f); // -0.01 за тик полета = -0.2 в секунду
        }

        // Не даем энергии уйти в минус
        if (energy.getCurrent() < 0) {
            energy.setCurrent(0);
        }
    }

    /**
     * Восстановить энергию (от еды/отдыха)
     */
    public static void replenishEnergy(ServerPlayer player, float amount) {
        ExodusCoreAPI.getVital(player, VitalType.ENERGY).ifPresent(energy -> {
            float newValue = Math.min(energy.getCurrent() + amount, energy.getMax());
            energy.setCurrent(newValue);
        });
    }

    /**
     * Проверить истощен ли игрок
     */
    public static boolean isExhausted(ServerPlayer player) {
        return ExodusCoreAPI.getVital(player, VitalType.ENERGY)
                .map(energy -> energy.getPercentage() <= 0.25f)
                .orElse(false);
    }
}