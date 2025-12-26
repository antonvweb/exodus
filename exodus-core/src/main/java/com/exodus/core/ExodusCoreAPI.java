package com.exodus.core;

import com.exodus.core.api.player.PlayerHealthData;
import com.exodus.core.api.player.StatusEffect;
import com.exodus.core.player.PlayerHealthComponent;
import com.exodus.core.player.PlayerHealthManager;
import net.minecraft.world.entity.player.Player;

/**
 * Главный API класс для Exodus Core
 */
public class ExodusCoreAPI {

    // ==================== ЗДОРОВЬЕ ====================

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

    /**
     * Получить текущее HP
     */
    public static float getCurrentHP(Player player) {
        return getHealthData(player).getCurrentHP();
    }

    /**
     * Получить максимальное HP
     */
    public static float getMaxHP(Player player) {
        return getHealthData(player).getMaxHP();
    }

    /**
     * Нанести урон
     */
    public static void damage(Player player, float amount) {
        getHealthComponent(player).damage(amount);
    }

    /**
     * Восстановить здоровье
     */
    public static void heal(Player player, float amount) {
        getHealthComponent(player).heal(amount);
    }

    /**
     * Жив ли игрок
     */
    public static boolean isAlive(Player player) {
        return getHealthData(player).isAlive();
    }

    // ==================== СТАТУСНЫЕ ЭФФЕКТЫ ====================

    /**
     * Добавить статусный эффект
     * @param effect эффект
     * @param duration длительность в секундах
     * @param intensity интенсивность (0.0 - 1.0)
     */
    public static void addEffect(Player player, StatusEffect effect, int duration, float intensity) {
        getHealthComponent(player).addEffect(effect, duration * 20, intensity);
    }

    /**
     * Убрать статусный эффект
     */
    public static void removeEffect(Player player, StatusEffect effect) {
        getHealthComponent(player).removeEffect(effect);
    }

    /**
     * Проверить есть ли эффект
     */
    public static boolean hasEffect(Player player, StatusEffect effect) {
        return getHealthData(player).hasEffect(effect);
    }

    /**
     * Получить интенсивность эффекта
     */
    public static float getEffectIntensity(Player player, StatusEffect effect) {
        return getHealthData(player).getEffectIntensity(effect);
    }
}
