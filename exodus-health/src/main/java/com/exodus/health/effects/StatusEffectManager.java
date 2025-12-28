package com.exodus.health.effects;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.StatusEffect;
import com.exodus.health.damage.DeathHandler;
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

        updatePainEffect(player);

        // Применяем урон от эффектов каждые 20 тиков (1 секунда)
        if (tickCounter % 20 == 0) {
            applyBleedingDamage(player);

            checkDeathFromEffects(player);
        }

        // Применяем дебафы каждый тик
        applyFractureDebuff(player);
        applyPainDebuff(player);
    }

    /**
     * Проверка смерти от эффектов (кровотечения и т.д.)
     */
    private static void checkDeathFromEffects(ServerPlayer player) {
        if (!ExodusCoreAPI.isAlive(player)) {
            // Игрок умер от кровотечения или другого эффекта
            DeathHandler.checkDeath(player, player.damageSources().starve());
        }
    }

    /**
     * Автоматическое наложение боли
     * Боль накладывается пока есть кровотечение или перелом
     */
    private static void updatePainEffect(ServerPlayer player) {
        boolean hasBleeding = ExodusCoreAPI.hasEffect(player, StatusEffect.BLEEDING);
        boolean hasFracture = ExodusCoreAPI.hasEffect(player, StatusEffect.FRACTURE);

        if (hasBleeding || hasFracture) {
            // Рассчитываем интенсивность боли на основе других эффектов
            float painIntensity = 0f;

            if (hasBleeding) {
                float bleedingIntensity = ExodusCoreAPI.getEffectIntensity(player, StatusEffect.BLEEDING);
                painIntensity = Math.max(painIntensity, bleedingIntensity * 0.7f);
            }

            if (hasFracture) {
                float fractureIntensity = ExodusCoreAPI.getEffectIntensity(player, StatusEffect.FRACTURE);
                painIntensity = Math.max(painIntensity, fractureIntensity * 0.8f);
            }

            // Накладываем боль (продлеваем каждый тик = постоянный эффект)
            // Длительность 2 тика чтобы не пропадал между обновлениями
            ExodusCoreAPI.addEffect(player, StatusEffect.PAIN, 2, painIntensity);
        }
        // Если нет кровотечения и перелома - боль сама истечёт
    }

    /**
     * Кровотечение - урон со временем
     */
    private static void applyBleedingDamage(ServerPlayer player) {
        if (!ExodusCoreAPI.hasEffect(player, StatusEffect.BLEEDING)) {
            return;
        }

        float intensity = ExodusCoreAPI.getEffectIntensity(player, StatusEffect.BLEEDING);

        // Урон 0.5-3 HP/сек в зависимости от интенсивности
        float damage = 0.5f + (intensity * 2.5f);

        ExodusCoreAPI.damage(player, damage);

        // Логируем для отладки
        if (ExodusCoreAPI.getCurrentHP(player) <= 10) {
            System.out.println("=== LOW HP FROM BLEEDING! HP: " + ExodusCoreAPI.getCurrentHP(player) + " ===");
        }
    }

    /**
     * Перелом - сильное замедление
     * ПОСТОЯННЫЙ ЭФФЕКТ (убирается только лечением)
     */
    private static void applyFractureDebuff(ServerPlayer player) {
        if (!ExodusCoreAPI.hasEffect(player, StatusEffect.FRACTURE)) {
            return;
        }

        float intensity = ExodusCoreAPI.getEffectIntensity(player, StatusEffect.FRACTURE);

        // Сильное замедление (уровень зависит от интенсивности)
        // 0.3-1.0 интенсивность = 1-4 уровень замедления
        int amplifier = Math.max(0, (int) (intensity * 4));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, amplifier, false, false));
    }

    /**
     * Боль - замедление копания + лёгкое замедление движения
     * АВТОМАТИЧЕСКИЙ ЭФФЕКТ (пока есть кровотечение или перелом)
     */
    private static void applyPainDebuff(ServerPlayer player) {
        if (!ExodusCoreAPI.hasEffect(player, StatusEffect.PAIN)) {
            return;
        }

        float intensity = ExodusCoreAPI.getEffectIntensity(player, StatusEffect.PAIN);

        // Замедление копания (Mining Fatigue)
        // 0.3-1.0 интенсивность = 0-2 уровень
        int miningAmplifier = Math.max(0, (int) (intensity * 2));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 2, miningAmplifier, false, false));

        // Лёгкое замедление движения
        int movementAmplifier = (int) (intensity * 0.5f); // 0 уровень в большинстве случаев
        if (movementAmplifier > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, movementAmplifier, false, false));
        }

        // При сильной боли (>70%) - тошнота
        if (intensity > 0.7f) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 2, 0, false, false));
        }
    }
}