package com.exodus.survival.health.effects;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.player.PlayerHealthData;
import com.exodus.survival.health.damage.DeathHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Менеджер статусных эффектов
 * Система 6 частей тела + переливание урона от кровотечения
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

        // Применяем урон от эффектов каждую секунду
        if (tickCounter % 20 == 0) {
            applyBleedingDamage(player);

            // ✅ ПРОВЕРКА ТАЙМЕРА ТОРСА
            checkTorsoDeathChance(player);

            // ✅ ПРОВЕРКА СМЕРТИ
            checkDeathFromEffects(player);
        }

        // Применяем дебафы каждый тик
        applyFractureDebuffs(player);
        applyPainDebuffs(player);

        // ✅ Дебафы от уничтоженных конечностей
        applyDestroyedLimbsDebuffs(player);
    }

    /**
     * ✅ Проверка таймера торса - прогрессирующий шанс смерти
     */
    private static void checkTorsoDeathChance(ServerPlayer player) {
        PlayerHealthData data = ExodusCoreAPI.getHealthData(player);

        if (!data.isTorsoDestroyed()) {
            return;
        }

        float deathChance = data.getTorsoDeathChance();

        // Если шанс 100% или выпал случайный шанс - смерть
        if (deathChance >= 1.0f || Math.random() < deathChance) {
            DeathHandler.checkDeath(player, player.damageSources().generic());
        }
    }

    /**
     * Проверка смерти
     */
    private static void checkDeathFromEffects(ServerPlayer player) {
        if (!ExodusCoreAPI.isAlive(player)) {
            PlayerHealthData.DeathCause cause = ExodusCoreAPI.getHealthData(player).getDeathCause();

            if (cause == PlayerHealthData.DeathCause.BLEEDING) {
                DeathHandler.checkDeath(player, player.damageSources().starve());
            } else {
                DeathHandler.checkDeath(player, player.damageSources().generic());
            }
        }
    }

    /**
     * ✅ Кровотечение - урон с ПЕРЕЛИВАНИЕМ при уничтожении части
     *
     * Механика:
     * 1. Часть жива → урон идёт по ней
     * 2. Часть уничтожена (HP = 0) → урон переливается на ВСЕ живые части ПОРОВНУ
     * 3. СУММИРОВАНИЕ: если уничтожено несколько частей, урон складывается
     */
    private static void applyBleedingDamage(ServerPlayer player) {
        PlayerHealthData data = ExodusCoreAPI.getHealthData(player);

        // Сначала применяем урон ко всем кровоточащим ЖИВЫМ частям
        for (BodyPart part : BodyPart.values()) {
            if (data.hasBleeding(part)) {
                float partHP = ExodusCoreAPI.getBodyPartHP(player, part);

                if (partHP > 0) {
                    // ✅ Часть жива - урон идёт по ней
                    float damage = data.getBleedingDamage(part);
                    ExodusCoreAPI.damageBodyPart(player, part, damage);
                }
            }
        }

        // ✅ Затем применяем ПЕРЕЛИВАНИЕ от всех уничтоженных частей
        float totalOverflowDamage = 0f;

        for (BodyPart part : BodyPart.values()) {
            if (data.hasBleeding(part)) {
                float partHP = ExodusCoreAPI.getBodyPartHP(player, part);

                if (partHP <= 0) {
                    // ✅ Часть уничтожена - суммируем её урон для переливания
                    float damage = data.getBleedingDamage(part);
                    totalOverflowDamage += damage;
                }
            }
        }

        // Если есть переливающийся урон - распределяем его
        if (totalOverflowDamage > 0) {
            applyOverflowDamage(player, totalOverflowDamage);
        }
    }

    /**
     * ✅ ПЕРЕЛИВАНИЕ урона от уничтоженных частей
     *
     * Урон распределяется ПОРОВНУ на все живые части тела
     */
    private static void applyOverflowDamage(ServerPlayer player, float totalDamage) {
        // Считаем количество живых частей
        int aliveCount = 0;
        for (BodyPart part : BodyPart.values()) {
            if (ExodusCoreAPI.getBodyPartHP(player, part) > 0) {
                aliveCount++;
            }
        }

        if (aliveCount == 0) {
            // Все части мертвы - игрок умирает
            return;
        }

        // Распределяем урон ПОРОВНУ
        float damagePerPart = totalDamage / aliveCount;

        for (BodyPart part : BodyPart.values()) {
            if (ExodusCoreAPI.getBodyPartHP(player, part) > 0) {
                ExodusCoreAPI.damageBodyPart(player, part, damagePerPart);
            }
        }
    }

    /**
     * Перелом - дебафы в зависимости от части тела
     */
    private static void applyFractureDebuffs(ServerPlayer player) {
        PlayerHealthData data = ExodusCoreAPI.getHealthData(player);

        boolean torsoFracture = data.hasFracture(BodyPart.TORSO);
        boolean leftArmFracture = data.hasFracture(BodyPart.LEFT_ARM);
        boolean rightArmFracture = data.hasFracture(BodyPart.RIGHT_ARM);
        boolean leftLegFracture = data.hasFracture(BodyPart.LEFT_LEG);
        boolean rightLegFracture = data.hasFracture(BodyPart.RIGHT_LEG);

        // НОГИ - Замедление
        if (leftLegFracture || rightLegFracture) {
            float legIntensity = 0f;
            if (leftLegFracture) legIntensity = Math.max(legIntensity, data.getFractureIntensity(BodyPart.LEFT_LEG));
            if (rightLegFracture) legIntensity = Math.max(legIntensity, data.getFractureIntensity(BodyPart.RIGHT_LEG));

            int amplifier = (leftLegFracture && rightLegFracture) ?
                    Math.max(2, (int)(legIntensity * 5)) :
                    Math.max(1, (int)(legIntensity * 3));

            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, amplifier, false, false));
        }

        // РУКИ - Mining Fatigue + Weakness
        if (leftArmFracture || rightArmFracture) {
            float armIntensity = 0f;
            if (leftArmFracture) armIntensity = Math.max(armIntensity, data.getFractureIntensity(BodyPart.LEFT_ARM));
            if (rightArmFracture) armIntensity = Math.max(armIntensity, data.getFractureIntensity(BodyPart.RIGHT_ARM));

            int amplifier = (leftArmFracture && rightArmFracture) ?
                    Math.max(2, (int)(armIntensity * 4)) :
                    Math.max(1, (int)(armIntensity * 2));

            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 2, amplifier, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 2, 0, false, false));
        }

        // ТОРС - Hunger
        if (torsoFracture) {
            float torsoIntensity = data.getFractureIntensity(BodyPart.TORSO);
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 2, (int)(torsoIntensity * 2), false, false));
        }
    }

    /**
     * Боль - глобальные дебафы
     */
    private static void applyPainDebuffs(ServerPlayer player) {
        if (!ExodusCoreAPI.hasPain(player)) {
            return;
        }

        float intensity = ExodusCoreAPI.getPainIntensity(player);

        // Mining Fatigue
        int miningAmplifier = Math.max(0, (int) (intensity * 2));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 2, miningAmplifier, false, false));

        // Slowness
        int movementAmplifier = (int) (intensity * 0.5f);
        if (movementAmplifier > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, movementAmplifier, false, false));
        }

        // Nausea при >70%
        if (intensity > 0.7f) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 2, 0, false, false));
        }
    }

    /**
     * ✅ Дебафы от УНИЧТОЖЕННЫХ конечностей (HP = 0)
     */
    private static void applyDestroyedLimbsDebuffs(ServerPlayer player) {
        boolean leftArmDestroyed = ExodusCoreAPI.getBodyPartHP(player, BodyPart.LEFT_ARM) <= 0;
        boolean rightArmDestroyed = ExodusCoreAPI.getBodyPartHP(player, BodyPart.RIGHT_ARM) <= 0;
        boolean leftLegDestroyed = ExodusCoreAPI.getBodyPartHP(player, BodyPart.LEFT_LEG) <= 0;
        boolean rightLegDestroyed = ExodusCoreAPI.getBodyPartHP(player, BodyPart.RIGHT_LEG) <= 0;

        // ✅ РУКИ УНИЧТОЖЕНЫ
        if (leftArmDestroyed || rightArmDestroyed) {
            if (leftArmDestroyed && rightArmDestroyed) {
                // ОБЕ руки → СИЛЬНОЕ утомление
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 2, 4, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 2, 2, false, false));
            } else {
                // ОДНА рука → среднее утомление
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 2, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 2, 1, false, false));
            }
        }

        // ✅ НОГИ УНИЧТОЖЕНЫ
        if (leftLegDestroyed || rightLegDestroyed) {
            if (leftLegDestroyed && rightLegDestroyed) {
                // ОБЕ ноги → СИЛЬНОЕ замедление (почти не ходить)
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 4, false, false));
            } else {
                // ОДНА нога → хромание (можно ходить, но медленно)
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 2, false, false));
            }
        }
    }
}