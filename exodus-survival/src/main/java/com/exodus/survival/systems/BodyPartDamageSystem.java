package com.exodus.survival.systems;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.player.ExodusPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;

import java.util.Map;

/**
 * Главная система обработки урона по частям тела
 */
public class BodyPartDamageSystem {

    /**
     * Обработать урон игроку
     */
    public static void handleDamage(ServerPlayer player, DamageSource source, float amount) {
        // Игнорируем нулевой урон
        if (amount <= 0) {
            return;
        }

        // 1. Проверяем тип урона - нужно ли распределять или попадание в конкретную часть
        Map<BodyPart, Float> distribution = DamageDistribution.getDamageDistribution(player, source, amount);

        if (distribution != null) {
            // Урон распределяется по нескольким частям (огонь, взрыв, падение и т.д.)
            applyDistributedDamage(player, distribution);
        } else {
            // Точечный урон - определяем куда попали
            BodyPart hitPart = HitDetection.detectHitLocation(player, source);

            if (hitPart != null) {
                applySingleHitDamage(player, hitPart, amount);
            } else {
                // Fallback - урон в торс
                applySingleHitDamage(player, BodyPart.TORSO, amount);
            }
        }

        playHurtSound(player);

        // 2. Синхронизируем ванильное HP
        syncVanillaHealth(player);

        // 3. Проверяем смерть
        checkDeath(player);
    }

    private static void playHurtSound(ServerPlayer player) {
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.PLAYER_HURT,
                SoundSource.PLAYERS,
                1.0f,
                1.0f
        );
    }

    /**
     * Применить урон в одну конкретную часть тела
     */
    private static void applySingleHitDamage(ServerPlayer player, BodyPart part, float damage) {
        // Применяем множитель урона части тела
        float actualDamage = damage * part.getDamageMultiplier();

        // Наносим урон
        ExodusCoreAPI.damageBodyPart(player, part, actualDamage);

        // Если часть убита - переливаем урон на торс
        handleOverflowDamage(player, part);
    }

    /**
     * Применить распределённый урон по нескольким частям
     */
    private static void applyDistributedDamage(ServerPlayer player, Map<BodyPart, Float> distribution) {
        for (Map.Entry<BodyPart, Float> entry : distribution.entrySet()) {
            BodyPart part = entry.getKey();
            float damage = entry.getValue();

            if (damage > 0) {
                // Применяем множитель части тела
                float actualDamage = damage * part.getDamageMultiplier();
                ExodusCoreAPI.damageBodyPart(player, part, actualDamage);

                // Обрабатываем перелив
                handleOverflowDamage(player, part);
            }
        }
    }

    /**
     * Обработать перелив урона (если часть убита)
     */
    private static void handleOverflowDamage(ServerPlayer player, BodyPart part) {
        // Голова и торс не переливают урон
        if (part == BodyPart.HEAD || part == BodyPart.TORSO) {
            return;
        }

        ExodusPlayerData.BodyPartData partData = ExodusCoreAPI.getBodyPart(player, part);

        // Если часть ушла в минус - перелив на торс
        if (partData.currentHP < 0) {
            float overflow = Math.abs(partData.currentHP);
            ExodusCoreAPI.damageBodyPart(player, BodyPart.TORSO, overflow);

            // Обнуляем HP части (не может быть меньше 0)
            partData.currentHP = 0;
        }
    }

    /**
     * Синхронизировать ванильное HP с нашей системой
     */
    private static void syncVanillaHealth(ServerPlayer player) {
        float totalHP = ExodusCoreAPI.getTotalHP(player);

        // Ограничиваем минимум и максимум
        totalHP = Math.max(0.5f, Math.min(totalHP, player.getMaxHealth()));

        player.setHealth(totalHP);
    }

    /**
     * Проверить условия смерти
     */
    private static void checkDeath(ServerPlayer player) {
        ExodusPlayerData.BodyPartData head = ExodusCoreAPI.getBodyPart(player, BodyPart.HEAD);
        ExodusPlayerData.BodyPartData torso = ExodusCoreAPI.getBodyPart(player, BodyPart.TORSO);

        // Смерть если:
        // 1. Голова уничтожена (0 HP)
        // 2. Торс уничтожен (0 HP)
        if (head.currentHP <= 0 || torso.currentHP <= 0) {
            // Убиваем игрока через ванильную систему
            player.setHealth(0);
            player.die(player.damageSources().generic());
        }
    }
}