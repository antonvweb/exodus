package com.exodus.health.effects;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.StatusEffect;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Менеджер статусных эффектов
 * Применяет урон и дебафы от эффектов каждый тик
 */
public class StatusEffectManager {

    private static int tickCounter = 0;

    /**
     * Регистрировать систему эффектов
     */
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tickEffects(player);
            }
        });
    }

    /**
     * Обработка эффектов для одного игрока
     */
    private static void tickEffects(ServerPlayer player) {
        // Обновляем таймеры эффектов
        ExodusCoreAPI.getHealthComponent(player).tick();

        // Применяем эффекты каждые 20 тиков (1 секунда)
        if (tickCounter % 20 == 0) {
            applyBleedingDamage(player);
            applyPoisonDamage(player);
        }

        // Применяем дебафы каждый тик
        applyFractureDebuff(player);
        applyPainDebuff(player);
    }

    /**
     * Кровотечение - урон со временем
     */
    private static void applyBleedingDamage(ServerPlayer player) {
        if (!ExodusCoreAPI.hasEffect(player, StatusEffect.BLEEDING)) {
            return;
        }

        float intensity = ExodusCoreAPI.getEffectIntensity(player, StatusEffect.BLEEDING);
        
        // Урон 1-5 HP/сек в зависимости от интенсивности
        float damage = 1.0f + (intensity * 4.0f);
        
        ExodusCoreAPI.damage(player, damage);
    }

    /**
     * Отравление - урон + тошнота
     */
    private static void applyPoisonDamage(ServerPlayer player) {
        if (!ExodusCoreAPI.hasEffect(player, StatusEffect.POISONED)) {
            return;
        }

        float intensity = ExodusCoreAPI.getEffectIntensity(player, StatusEffect.POISONED);
        
        // Урон 0.5-2 HP/сек
        float damage = 0.5f + (intensity * 1.5f);
        ExodusCoreAPI.damage(player, damage);

        // Эффект тошноты
        int duration = (int) (intensity * 40); // 1-2 секунды
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0, false, false));
    }

    /**
     * Перелом - замедление
     */
    private static void applyFractureDebuff(ServerPlayer player) {
        if (!ExodusCoreAPI.hasEffect(player, StatusEffect.FRACTURE)) {
            return;
        }

        float intensity = ExodusCoreAPI.getEffectIntensity(player, StatusEffect.FRACTURE);
        
        // Замедление (уровень зависит от интенсивности)
        int amplifier = (int) (intensity * 3); // 0-3 уровень
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, amplifier, false, false));
    }

    /**
     * Боль - тошнота + замедление
     */
    private static void applyPainDebuff(ServerPlayer player) {
        if (!ExodusCoreAPI.hasEffect(player, StatusEffect.PAIN)) {
            return;
        }

        float intensity = ExodusCoreAPI.getEffectIntensity(player, StatusEffect.PAIN);
        
        // Слабое замедление
        int slowness = (int) (intensity * 1); // 0-1 уровень
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, slowness, false, false));

        // При сильной боли - тошнота
        if (intensity > 0.5f) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 2, 0, false, false));
        }
    }
}
