package com.exodus.survival.systems;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.player.DeathCause;
import com.exodus.core.api.player.ExodusPlayerData;
import com.exodus.core.api.stats.IPlayerStat;
import com.exodus.core.api.stats.VitalType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Система проверки условий смерти
 * Вызывается каждую секунду для каждого игрока
 */
public class DeathConditionSystem {

    /**
     * Проверить все условия смерти
     * @return DeathCause если игрок должен умереть, иначе null
     */
    public static DeathCause checkDeathConditions(ServerPlayer player) {
        // ДОБАВЬ ЭТО: Не проверяем если игрок уже мёртв
        if (!player.isAlive() || player.isDeadOrDying()) {
            return null;
        }

        ExodusPlayerData data = ExodusCoreAPI.getPlayerData(player);

        // 1. ГОЛОВА = 0 HP (мгновенная смерть)
        if (checkHeadDestroyed(player, data)) {
            return DeathCause.HEAD_DESTROYED;
        }

        // 2. КРИТИЧЕСКОЕ HP (0-3%) - RNG смерть
        DeathCause criticalDeath = checkCriticalHP(player, data);
        if (criticalDeath != null) {
            return criticalDeath;
        }

        // 3. ИСТОЩЕНИЕ (голод + жажда = 0)
        if (checkStarvation(player)) {
            return DeathCause.STARVATION;
        }

        // 4. КРОВОПОТЕРЯ
        if (checkBloodLoss(player, data)) {
            return DeathCause.BLOOD_LOSS;
        }

        // 5. ПЕРЕОХЛАЖДЕНИЕ
        if (checkHypothermia(player, data)) {
            return DeathCause.HYPOTHERMIA;
        }

        // 6. ПЕРЕГРЕВ
        if (checkHyperthermia(player, data)) {
            return DeathCause.HYPERTHERMIA;
        }

        // Игрок жив
        return null;
    }

    /**
     * 1. Проверка: Голова = 0 HP
     */
    private static boolean checkHeadDestroyed(ServerPlayer player, ExodusPlayerData data) {
        ExodusPlayerData.BodyPartData head = data.getBodyPart(BodyPart.HEAD);
        return head.currentHP <= 0;
    }

    /**
     * 2. Проверка: Критическое HP (0-3%) с RNG
     */
    private static DeathCause checkCriticalHP(ServerPlayer player, ExodusPlayerData data) {
        float totalHP = data.getTotalHP();
        float maxHP = player.getMaxHealth();
        float hpPercent = (totalHP / maxHP) * 100f;

        // Только если HP между 0 и 3%
        if (hpPercent <= 3f && hpPercent > 0) {
            long currentTime = player.level().getGameTime();
            long lastCheck = data.getLastCriticalHPCheck();

            // Проверяем раз в секунду (20 тиков)
            if (currentTime - lastCheck >= 20) {
                data.setLastCriticalHPCheck(currentTime);

                // Вычисляем шанс смерти
                float deathChance = calculateCriticalDeathChance(hpPercent);

                // Бросаем кубик
                if (Math.random() * 100 < deathChance) {
                    // Игрок умирает!
                    player.sendSystemMessage(Component.literal("§c💀 Ваше сердце остановилось..."));
                    return DeathCause.CRITICAL_HP_CHANCE;
                } else {
                    // Выжил в этот раз
                    player.sendSystemMessage(Component.literal("§e⚠ Критическое состояние! HP: " + String.format("%.1f", hpPercent) + "%"));
                }
            }
        }

        return null;
    }

    /**
     * Рассчитать шанс смерти при критическом HP
     */
    private static float calculateCriticalDeathChance(float hpPercent) {
        if (hpPercent <= 0.5f) {
            return 90f; // 90% шанс при почти 0 HP
        } else if (hpPercent <= 1f) {
            return 40f; // 40% при 1% HP
        } else if (hpPercent <= 2f) {
            return 15f; // 15% при 2% HP
        } else {
            return 5f;  // 5% при 3% HP
        }
    }

    /**
     * 3. Проверка: Истощение (голод + жажда)
     */
    private static boolean checkStarvation(ServerPlayer player) {
        Optional<IPlayerStat> hungerOpt = ExodusCoreAPI.getVital(player, VitalType.HUNGER);
        Optional<IPlayerStat> thirstOpt = ExodusCoreAPI.getVital(player, VitalType.THIRST);

        if (hungerOpt.isEmpty() || thirstOpt.isEmpty()) {
            return false;
        }

        ExodusPlayerData data = ExodusCoreAPI.getPlayerData(player);
        long currentTime = player.level().getGameTime();

        float hunger = hungerOpt.get().getCurrent();
        float thirst = thirstOpt.get().getCurrent();

        // Отслеживаем голод
        if (hunger <= 0) {
            if (data.getStarvationStartTime() == 0) {
                data.setStarvationStartTime(currentTime);
            }
        } else {
            data.setStarvationStartTime(0); // Сброс
        }

        // Отслеживаем жажду
        if (thirst <= 0) {
            if (data.getDehydrationStartTime() == 0) {
                data.setDehydrationStartTime(currentTime);
            }
        } else {
            data.setDehydrationStartTime(0); // Сброс
        }

        long hungerTime = data.getStarvationStartTime() > 0 ?
                currentTime - data.getStarvationStartTime() : 0;
        long thirstTime = data.getDehydrationStartTime() > 0 ?
                currentTime - data.getDehydrationStartTime() : 0;

        // Условия смерти:
        // 1. Голод=0 И Жажда=0 более 5 минут (6000 тиков)
        if (hunger <= 0 && thirst <= 0 && Math.min(hungerTime, thirstTime) >= 6000) {
            return true;
        }

        // 2. Только голод=0 более 15 минут (18000 тиков)
        if (hunger <= 0 && hungerTime >= 18000) {
            return true;
        }

        // 3. Только жажда=0 более 10 минут (12000 тиков)
        if (thirst <= 0 && thirstTime >= 12000) {
            return true;
        }

        return false;
    }

    /**
     * 4. Проверка: Кровопотеря
     */
    private static boolean checkBloodLoss(ServerPlayer player, ExodusPlayerData data) {
        float bloodLevel = data.getBloodLevel();

        // Смерть при потере >80% крови
        return bloodLevel <= 20f;
    }

    /**
     * 5. Проверка: Переохлаждение
     */
    private static boolean checkHypothermia(ServerPlayer player, ExodusPlayerData data) {
        float temperature = data.getBodyTemperature();

        // Смерть при температуре <30°C
        return temperature < 30f;
    }

    /**
     * 6. Проверка: Перегрев
     */
    private static boolean checkHyperthermia(ServerPlayer player, ExodusPlayerData data) {
        float temperature = data.getBodyTemperature();

        // Смерть при температуре >42°C
        return temperature > 42f;
    }

    /**
     * Убить игрока с указанной причиной
     */
    public static void killPlayer(ServerPlayer player, DeathCause cause) {
        // ДОБАВЬ ЭТО: Проверяем что игрок ещё жив
        if (!player.isAlive() || player.isDeadOrDying()) {
            return; // Уже мёртв, не убиваем повторно
        }

        // Сообщение о смерти (ОДИН РАЗ)
        player.sendSystemMessage(Component.literal("§4☠ Причина смерти: " + cause.getDisplayName()));

        // Убиваем игрока
        player.kill();
    }
}