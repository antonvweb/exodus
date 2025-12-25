package com.exodus.survival.systems;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.stats.IPlayerStat;
import com.exodus.core.api.stats.VitalType;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Система кислорода
 * Управляет расходом кислорода и уроном от удушья
 */
public class OxygenSystem {

    private static final float SUFFOCATION_DAMAGE = 1.0f; // Урон в секунду
    private static final int SUFFOCATION_INTERVAL = 20; // Раз в секунду
    private static int tickCounter = 0;

    public static void tick(ServerPlayer player) {
        Optional<IPlayerStat> oxygenOpt = ExodusCoreAPI.getVital(player, VitalType.OXYGEN);

        if (oxygenOpt.isEmpty()) {
            return;
        }

        IPlayerStat oxygen = oxygenOpt.get();

        // Если кислород закончился - наносим урон через систему здоровья
        if (oxygen.getCurrent() <= 0) {
            tickCounter++;

            if (tickCounter >= SUFFOCATION_INTERVAL) {
                tickCounter = 0;

                // НОВОЕ: Наносим урон в голову через BodyPartDamageSystem
                ExodusCoreAPI.damageBodyPart(player, BodyPart.HEAD, SUFFOCATION_DAMAGE);

                // Синхронизируем ванильное HP
                float totalHP = ExodusCoreAPI.getTotalHP(player);
                player.setHealth(Math.max(0.5f, totalHP));
            }
        } else {
            tickCounter = 0;
        }

        // Проверяем условия окружения для восстановления кислорода
        checkOxygenReplenishment(player, oxygen);
    }

    /**
     * Проверить можно ли восстановить кислород
     */
    private static void checkOxygenReplenishment(ServerPlayer player, IPlayerStat oxygen) {
        // Если игрок в воздухе (не под водой, не в лаве)
        if (!player.isUnderWater() && !player.isInLava()) {
            // Проверяем есть ли воздух вокруг (не в блоке)
            if (!player.level().getBlockState(player.blockPosition()).isSolid()) {
                // Восстанавливаем кислород постепенно
                float replenishRate = 2.0f; // 2 единицы в секунду

                // Восстанавливаем только каждый 10-й тик (2 раза в секунду)
                if (player.tickCount % 10 == 0) {
                    float newValue = Math.min(oxygen.getCurrent() + (replenishRate / 2), oxygen.getMax());
                    oxygen.setCurrent(newValue);
                }
            }
        }
    }

    /**
     * Принудительное восстановление кислорода (например, от предмета)
     */
    public static void replenishOxygen(ServerPlayer player, float amount) {
        ExodusCoreAPI.getVital(player, VitalType.OXYGEN).ifPresent(oxygen -> {
            float newValue = Math.min(oxygen.getCurrent() + amount, oxygen.getMax());
            oxygen.setCurrent(newValue);
        });
    }

    /**
     * Проверить задыхается ли игрок
     */
    public static boolean isSuffocating(ServerPlayer player) {
        return ExodusCoreAPI.getVital(player, VitalType.OXYGEN)
                .map(oxygen -> oxygen.getCurrent() <= 0)
                .orElse(false);
    }
}