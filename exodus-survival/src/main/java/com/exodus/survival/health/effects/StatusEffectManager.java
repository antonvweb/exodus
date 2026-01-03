package com.exodus.survival.health.effects;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.attributes.AttributeModifier;
import com.exodus.core.api.attributes.AttributeType;
import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.player.PlayerHealthData;
import com.exodus.core.api.player.PlayerVitalsData;
import com.exodus.core.player.attributes.AttributeManager;
import com.exodus.core.player.attributes.VanillaAttributeSynchronizer;
import com.exodus.core.player.vitals.PlayerVitalsManager;
import com.exodus.survival.health.damage.DeathHandler;
import com.exodus.survival.health.network.CameraShakePacket;
import com.exodus.survival.health.network.HeadSpinPacket;
import com.exodus.survival.health.network.BreathPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

/**
 * Менеджер статусных эффектов
 * Система 6 частей тела + переливание урона от кровотечения
 */
public class StatusEffectManager {

    private static int tickCounter = 0;

    private static final Map<UUID, Boolean> lastShakeState = new HashMap<>();
    private static final Map<UUID, Boolean> lastSpinState = new HashMap<>();
    private static final Map<UUID, Boolean> lastLowHealthState  = new HashMap<>();

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

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            lastShakeState.remove(handler.getPlayer().getUUID());
            lastSpinState.remove(handler.getPlayer().getUUID());
            lastLowHealthState.remove(handler.getPlayer().getUUID());
        });
    }

    /**
     * Обработка эффектов для одного игрока
     */
    private static void tickEffects(ServerPlayer player) {
        ExodusCoreAPI.getHealthComponent(player).tick();

        // Применяем урон каждую секунду
        if (tickCounter % 20 == 0) {
            applyBleedingDamage(player);
            checkTorsoDeathChance(player);
            checkDeathFromEffects(player);
            applyTemperatureDamage(player);
        }

        PlayerHealthData data = ExodusCoreAPI.getHealthData(player);

        // ✅ ПРОВЕРЯЕМ СМЕРТЬ СНАЧАЛА
        if (!data.isAlive()) {
            // Игрок мёртв - выключаем все звуки
            Boolean lastState = lastLowHealthState.get(player.getUUID());

            if (lastState == null || lastState) {
                ServerPlayNetworking.send(player, new BreathPacket(false));
                lastLowHealthState.put(player.getUUID(), false);
            }

            return; // ← ВЫХОДИМ, не обрабатываем дебафы
        }

        // Если жив - проверяем дыхание
        boolean isLowHp = data.getFullHp(player) < 0.25f;
        boolean torsoDestroyed = data.isTorsoDestroyed();

        PlayerVitalsData vitals = PlayerVitalsManager.getComponent(player).getData();
        boolean isHot = vitals.isSevereHyperthermia();

        boolean shouldBreatheHeavy = isLowHp || torsoDestroyed || isHot;

        Boolean lastState = lastLowHealthState.get(player.getUUID());

        if(lastState == null || lastState != shouldBreatheHeavy){
            ServerPlayNetworking.send(player, new BreathPacket(shouldBreatheHeavy));
            lastLowHealthState.put(player.getUUID(), shouldBreatheHeavy);
        }

        // Остальные дебафы
        applyFractureDebuffs(player);
        applyDestroyedLimbsDebuffs(player);
        applyTemperatureDebuffs(player, vitals);
    }

    /**
     * Дебафы от температуры (скорость, стамина, жажда)
     * Вызывается КАЖДЫЙ ТИК
     */
    private static void applyTemperatureDebuffs(ServerPlayer player, PlayerVitalsData vitals) {
        // ✅ СНАЧАЛА очищаем старые модификаторы
        HealthAttributeHelper.clearTemperatureDebuffs(player);

        // Определяем дебафы в зависимости от температуры
        float speedPenalty = 0.0f;
        float staminaPenalty = 0.0f;
        float thirstPenalty = 0.0f;

        if (vitals.isSevereHypothermia()) {
            // <35°C - ТЯЖЁЛАЯ гипотермия
            speedPenalty = -0.3f;       // -30% скорости
            staminaPenalty = -1.0f;     // Стамина НЕ восстанавливается (-100%)
        } else if (vitals.isHypothermia()) {
            // 35-36°C - гипотермия
            speedPenalty = -0.1f;       // -10% скорости
            staminaPenalty = -0.3f;     // -30% восстановления стамины
        } else if (vitals.isSevereHyperthermia()) {
            // >38.5°C - ТЯЖЁЛАЯ гипертермия
            thirstPenalty = 0.5f;       // +50% расхода жажды (в 1.5 раза быстрее!)
        } else if (vitals.isHyperthermia()) {
            // 37.6-38.5°C - гипертермия
            staminaPenalty = -0.2f;     // -20% восстановления стамины
            thirstPenalty = 0.2f;       // +20% расхода жажды
        }

        // Применяем модификаторы (только если есть штраф)

        if (speedPenalty != 0) {
            AttributeModifier speedMod = new AttributeModifier(
                    "temperature_speed",
                    speedPenalty,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                    "temperature"
            );
            AttributeManager.addModifier(player, AttributeType.MOVEMENT_SPEED, speedMod);
        }

        if (staminaPenalty != 0) {
            AttributeModifier staminaMod = new AttributeModifier(
                    "temperature_stamina",
                    staminaPenalty,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                    "temperature"
            );
            AttributeManager.addModifier(player, AttributeType.STAMINA_REGEN, staminaMod);
        }

        if (thirstPenalty != 0) {
            AttributeModifier thirstMod = new AttributeModifier(
                    "temperature_thirst",
                    thirstPenalty,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                    "temperature"
            );
            AttributeManager.addModifier(player, AttributeType.THIRST_DRAIN_RATE, thirstMod);
        }

        boolean shouldShake = vitals.isSevereHypothermia();
        Boolean lastShake = lastShakeState.get(player.getUUID());

        if (lastShake == null || lastShake != shouldShake) {
            float intensity = shouldShake ? 1.2f : 0.0f; // 1.2° дрожи

            ServerPlayNetworking.send(
                    player,
                    new CameraShakePacket(shouldShake, intensity)
            );

            lastShakeState.put(player.getUUID(), shouldShake);
        }

        // ✅ КРУЖЕНИЕ от жары (плавное)
        boolean shouldSpin = vitals.isSevereHyperthermia();
        Boolean lastSpin = lastSpinState.get(player.getUUID());

        if (lastSpin == null || lastSpin != shouldSpin) {
            float intensity = shouldSpin ? 15.0f : 0.0f; // 15°/сек кружение

            ServerPlayNetworking.send(
                    player,
                    new HeadSpinPacket(shouldSpin, intensity)
            );

            lastSpinState.put(player.getUUID(), shouldSpin);
        }

        // ✅ Синхронизируем с ванилью
        VanillaAttributeSynchronizer.synchronizeMovementSpeed(player);
    }

    /**
     * Урон от экстремальной температуры
     * Вызывается РАЗ В СЕКУНДУ
     */
    private static void applyTemperatureDamage(ServerPlayer player) {
        PlayerVitalsData vitals = PlayerVitalsManager.getComponent(player).getData();

        float damage = 0.0f;

        if (vitals.isSevereHypothermia()) {
            damage = 0.5f; // 0.5 HP/сек
        } else if (vitals.isSevereHyperthermia()) {
            damage = 0.3f; // 0.3 HP/сек
        }

        // Применяем урон на случайную живую часть
        if (damage > 0) {
            BodyPart randomPart = getRandomAliveBodyPart(player);

            if (randomPart != null) {
                ExodusCoreAPI.damageBodyPart(player, randomPart, damage);
            }
        }
    }

    /**
     * Получить случайную живую часть тела
     */
    private static BodyPart getRandomAliveBodyPart(ServerPlayer player) {
        List<BodyPart> aliveParts = new ArrayList<>();

        for (BodyPart part : BodyPart.values()) {
            if (ExodusCoreAPI.getBodyPartHP(player, part) > 0) {
                aliveParts.add(part);
            }
        }

        if (aliveParts.isEmpty()) {
            return null;
        }

        int randomIndex = (int) (Math.random() * aliveParts.size());
        return aliveParts.get(randomIndex);
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

        // ✅ СНАЧАЛА очищаем старые модификаторы от переломов
        HealthAttributeHelper.clearFractureDebuffs(player);

        // НОГИ
        if (data.hasFracture(BodyPart.LEFT_LEG)) {
            float intensity = data.getFractureIntensity(BodyPart.LEFT_LEG);
            HealthAttributeHelper.applyLegFracture(player, BodyPart.LEFT_LEG, intensity);
        }

        if (data.hasFracture(BodyPart.RIGHT_LEG)) {
            float intensity = data.getFractureIntensity(BodyPart.RIGHT_LEG);
            HealthAttributeHelper.applyLegFracture(player, BodyPart.RIGHT_LEG, intensity);
        }

        // РУКИ (ИСПРАВЛЕНО!)
        if (data.hasFracture(BodyPart.LEFT_ARM)) {
            float intensity = data.getFractureIntensity(BodyPart.LEFT_ARM);
            HealthAttributeHelper.applyArmFracture(player, BodyPart.LEFT_ARM, intensity); // ← applyArmFracture, не Leg!
        }

        if (data.hasFracture(BodyPart.RIGHT_ARM)) {
            float intensity = data.getFractureIntensity(BodyPart.RIGHT_ARM);
            HealthAttributeHelper.applyArmFracture(player, BodyPart.RIGHT_ARM, intensity); // ← applyArmFracture!
        }

        // ✅ Синхронизируем с ванилью
        VanillaAttributeSynchronizer.synchronizeMovementSpeed(player);
        VanillaAttributeSynchronizer.synchronizeAttackSpeed(player);
    }

    /**
     * ✅ Дебафы от УНИЧТОЖЕННЫХ конечностей (HP = 0)
     */
    /**
     * ✅ Дебафы от УНИЧТОЖЕННЫХ конечностей (HP = 0)
     * НАМНОГО СИЛЬНЕЕ чем от переломов!
     */
    private static void applyDestroyedLimbsDebuffs(ServerPlayer player) {
        // ✅ СНАЧАЛА очищаем старые модификаторы от уничтоженных конечностей
        HealthAttributeHelper.clearDestroyedLimbDebuffs(player);

        boolean leftArmDestroyed = ExodusCoreAPI.getBodyPartHP(player, BodyPart.LEFT_ARM) <= 0;
        boolean rightArmDestroyed = ExodusCoreAPI.getBodyPartHP(player, BodyPart.RIGHT_ARM) <= 0;
        boolean leftLegDestroyed = ExodusCoreAPI.getBodyPartHP(player, BodyPart.LEFT_LEG) <= 0;
        boolean rightLegDestroyed = ExodusCoreAPI.getBodyPartHP(player, BodyPart.RIGHT_LEG) <= 0;

        // ✅ НОГИ УНИЧТОЖЕНЫ
        if (leftLegDestroyed && rightLegDestroyed) {
            // ОБЕ ноги → ЭКСТРЕМАЛЬНОЕ замедление (-95%)
            HealthAttributeHelper.applyBothLegsDestroyed(player);
        } else {
            // ОДНА нога → сильное замедление (-80%)
            if (leftLegDestroyed) {
                HealthAttributeHelper.applyDestroyedLeg(player, BodyPart.LEFT_LEG);
            }
            if (rightLegDestroyed) {
                HealthAttributeHelper.applyDestroyedLeg(player, BodyPart.RIGHT_LEG);
            }
        }

        // ✅ РУКИ УНИЧТОЖЕНЫ
        if (leftArmDestroyed && rightArmDestroyed) {
            // ОБЕ руки → ЭКСТРЕМАЛЬНОЕ утомление (-90% mining, -80% attack)
            HealthAttributeHelper.applyBothArmsDestroyed(player);
        } else {
            // ОДНА рука → сильное утомление (-70% mining, -50% attack)
            if (leftArmDestroyed) {
                HealthAttributeHelper.applyDestroyedArm(player, BodyPart.LEFT_ARM);
            }
            if (rightArmDestroyed) {
                HealthAttributeHelper.applyDestroyedArm(player, BodyPart.RIGHT_ARM);
            }
        }

        // ✅ Синхронизируем с ванилью
        VanillaAttributeSynchronizer.synchronizeMovementSpeed(player);
        VanillaAttributeSynchronizer.synchronizeAttackSpeed(player);
    }
}