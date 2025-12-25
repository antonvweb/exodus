package com.exodus.survival.systems;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.stats.IPlayerStat;
import com.exodus.core.api.stats.VitalType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Optional;

/**
 * Система голода и жажды
 * Управляет негативными эффектами от недостатка еды и воды
 */
public class HungerThirstSystem {

    private static final int EFFECT_CHECK_INTERVAL = 20; // Проверяем раз в секунду
    private static int tickCounter = 0;

    public static void tick(ServerPlayer player) {
        tickCounter++;

        if (tickCounter >= EFFECT_CHECK_INTERVAL) {
            tickCounter = 0;

            applyHungerEffects(player);
            applyThirstEffects(player);
        }
    }

    /**
     * Применить эффекты от голода
     */
    private static void applyHungerEffects(ServerPlayer player) {
        Optional<IPlayerStat> hungerOpt = ExodusCoreAPI.getVital(player, VitalType.HUNGER);

        if (hungerOpt.isEmpty()) {
            return;
        }

        IPlayerStat hunger = hungerOpt.get();
        float hungerPercent = hunger.getPercentage();

        // Критический голод (0-25%)
        if (hungerPercent <= 0.25f) {
            // Слабость III + урон
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 2, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false));

            // НОВОЕ: Наносим урон в торс если голод = 0
            if (hungerPercent == 0) {
                ExodusCoreAPI.damageBodyPart(player, BodyPart.TORSO, 0.5f);

                // Синхронизируем ванильное HP
                float totalHP = ExodusCoreAPI.getTotalHP(player);
                player.setHealth(Math.max(0.5f, totalHP));
            }
        }
        // Средний голод (25-50%)
        else if (hungerPercent <= 0.5f) {
            // Слабость I
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false));
        }
        // Легкий голод (50-75%)
        else if (hungerPercent <= 0.75f) {
            // Замедление регенерации (визуальный индикатор)
            // Реальное влияние будет в системе здоровья
        }
        // Нормальный голод (75-100%) - без эффектов
    }

    /**
     * Применить эффекты от жажды
     */
    private static void applyThirstEffects(ServerPlayer player) {
        Optional<IPlayerStat> thirstOpt = ExodusCoreAPI.getVital(player, VitalType.THIRST);

        if (thirstOpt.isEmpty()) {
            return;
        }

        IPlayerStat thirst = thirstOpt.get();
        float thirstPercent = thirst.getPercentage();

        // Критическая жажда (0-25%)
        if (thirstPercent <= 0.25f) {
            // Тошнота + усталость + медлительность
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 1, false, false));

            // Ускоренная потеря энергии
            ExodusCoreAPI.getVital(player, VitalType.ENERGY).ifPresent(energy -> {
                energy.add(-0.5f); // Дополнительная потеря энергии
            });
        }
        // Сильная жажда (25-50%)
        else if (thirstPercent <= 0.5f) {
            // Замедление + усталость
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, false, false));
        }
        // Средняя жажда (50-75%)
        else if (thirstPercent <= 0.75f) {
            // Легкое замедление копания
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, false, false));
        }
        // Нормальная жажда (75-100%) - без эффектов
    }

    /**
     * Восстановить голод (от еды)
     */
    public static void replenishHunger(ServerPlayer player, float amount) {
        ExodusCoreAPI.getVital(player, VitalType.HUNGER).ifPresent(hunger -> {
            float newValue = Math.min(hunger.getCurrent() + amount, hunger.getMax());
            hunger.setCurrent(newValue);
        });
    }

    /**
     * Восстановить жажду (от питья)
     */
    public static void replenishThirst(ServerPlayer player, float amount) {
        ExodusCoreAPI.getVital(player, VitalType.THIRST).ifPresent(thirst -> {
            float newValue = Math.min(thirst.getCurrent() + amount, thirst.getMax());
            thirst.setCurrent(newValue);
        });
    }

    /**
     * Проверить голоден ли игрок
     */
    public static boolean isStarving(ServerPlayer player) {
        return ExodusCoreAPI.getVital(player, VitalType.HUNGER)
                .map(hunger -> hunger.getPercentage() <= 0.25f)
                .orElse(false);
    }

    /**
     * Проверить испытывает ли игрок жажду
     */
    public static boolean isDehydrated(ServerPlayer player) {
        return ExodusCoreAPI.getVital(player, VitalType.THIRST)
                .map(thirst -> thirst.getPercentage() <= 0.25f)
                .orElse(false);
    }
}