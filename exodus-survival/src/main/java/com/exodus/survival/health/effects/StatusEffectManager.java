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
 *
 * ОСНОВНЫЕ ИЗМЕНЕНИЯ:
 * 1. Упрощено applyBleedingDamage (overflow автоматический)
 * 2. Убрана checkTorsoDeathChance (торс = instant death)
 * 3. Убрано переливание урона от кровотечения
 */
public class StatusEffectManager {

    private static int tickCounter = 0;

    private static final Map<UUID, Boolean> lastShakeState = new HashMap<>();
    private static final Map<UUID, Boolean> lastSpinState = new HashMap<>();
    private static final Map<UUID, Boolean> lastLowHealthState = new HashMap<>();

    /**
     * Регистрация системы эффектов
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

        // Применяем эффекты каждую секунду
        if (tickCounter % 20 == 0) {
            applyBleedingDamage(player);      // ← Упрощено!
            checkDeathFromEffects(player);
            applyTemperatureDamage(player);
        }

        PlayerHealthData data = ExodusCoreAPI.getHealthData(player);

        // Проверяем смерть СНАЧАЛА
        if (!data.isAlive()) {
            Boolean lastState = lastLowHealthState.get(player.getUUID());

            if (lastState == null || lastState) {
                ServerPlayNetworking.send(player, new BreathPacket(false));
                lastLowHealthState.put(player.getUUID(), false);
            }

            return; // Мертв → не обрабатываем дебафы
        }

        // Дыхание при низком HP
        boolean isLowHp = data.getFullHp(player) < 0.25f;
        boolean torsoDestroyed = ExodusCoreAPI.getBodyPartHP(player, BodyPart.TORSO) <= 0;

        PlayerVitalsData vitals = PlayerVitalsManager.getComponent(player).getData();
        boolean isHot = vitals.isSevereHyperthermia();

        boolean shouldBreatheHeavy = isLowHp || torsoDestroyed || isHot;

        Boolean lastState = lastLowHealthState.get(player.getUUID());

        if (lastState == null || lastState != shouldBreatheHeavy) {
            ServerPlayNetworking.send(player, new BreathPacket(shouldBreatheHeavy));
            lastLowHealthState.put(player.getUUID(), shouldBreatheHeavy);
        }

        // Остальные дебафы
        applyFractureDebuffs(player);
        applyDestroyedLimbsDebuffs(player);
        applyTemperatureDebuffs(player, vitals);
    }

    /**
     * КРОВОТЕЧЕНИЕ - УПРОЩЕННАЯ ВЕРСИЯ
     *
     * ЧТО ИЗМЕНИЛОСЬ:
     * - Убрана логика переливания
     * - Просто наносим урон на часть
     * - Overflow срабатывает АВТОМАТИЧЕСКИ в PlayerHealthData!
     *
     * КАК РАБОТАЕТ:
     * 1. Часть жива → урон идет на неё
     * 2. Часть черная → damageBodyPart() вызовет overflow автоматически
     */
    private static void applyBleedingDamage(ServerPlayer player) {
        PlayerHealthData data = ExodusCoreAPI.getHealthData(player);

        // Просто наносим урон на каждую кровоточащую часть
        for (BodyPart part : BodyPart.values()) {
            if (data.hasBleeding(part)) {
                float damage = data.getBleedingDamage(part);

                // Наносим урон → overflow автоматически сработает если часть черная!
                ExodusCoreAPI.damageBodyPart(player, part, damage);
            }
        }
    }

    /**
     * Проверка смерти от эффектов
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
     * Урон от температуры
     */
    private static void applyTemperatureDamage(ServerPlayer player) {
        PlayerVitalsData vitals = PlayerVitalsManager.getComponent(player).getData();

        float damage = 0.0f;

        if (vitals.isSevereHypothermia()) {
            damage = 0.5f; // 0.5 HP/сек
        } else if (vitals.isSevereHyperthermia()) {
            damage = 0.3f; // 0.3 HP/сек
        }

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
     * Дебафы от температуры
     */
    private static void applyTemperatureDebuffs(ServerPlayer player, PlayerVitalsData vitals) {
        HealthAttributeHelper.clearTemperatureDebuffs(player);

        float speedPenalty = 0.0f;
        float staminaPenalty = 0.0f;
        float thirstPenalty = 0.0f;

        if (vitals.isSevereHypothermia()) {
            speedPenalty = -0.3f;
            staminaPenalty = -1.0f;
        } else if (vitals.isHypothermia()) {
            speedPenalty = -0.1f;
            staminaPenalty = -0.3f;
        } else if (vitals.isSevereHyperthermia()) {
            thirstPenalty = 0.5f;
        } else if (vitals.isHyperthermia()) {
            staminaPenalty = -0.2f;
            thirstPenalty = 0.2f;
        }

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

        // Эффекты камеры
        boolean shouldShake = vitals.isSevereHypothermia();
        Boolean lastShake = lastShakeState.get(player.getUUID());

        if (lastShake == null || lastShake != shouldShake) {
            float intensity = shouldShake ? 1.2f : 0.0f;
            ServerPlayNetworking.send(player, new CameraShakePacket(shouldShake, intensity));
            lastShakeState.put(player.getUUID(), shouldShake);
        }

        boolean shouldSpin = vitals.isSevereHyperthermia();
        Boolean lastSpin = lastSpinState.get(player.getUUID());

        if (lastSpin == null || lastSpin != shouldSpin) {
            float intensity = shouldSpin ? 15.0f : 0.0f;
            ServerPlayNetworking.send(player, new HeadSpinPacket(shouldSpin, intensity));
            lastSpinState.put(player.getUUID(), shouldSpin);
        }

        VanillaAttributeSynchronizer.synchronizeMovementSpeed(player);
    }

    /**
     * Дебафы от переломов
     */
    private static void applyFractureDebuffs(ServerPlayer player) {
        PlayerHealthData data = ExodusCoreAPI.getHealthData(player);

        HealthAttributeHelper.clearFractureDebuffs(player);

        // Ноги
        if (data.hasFracture(BodyPart.LEFT_LEG)) {
            float intensity = data.getFractureIntensity(BodyPart.LEFT_LEG);
            HealthAttributeHelper.applyLegFracture(player, BodyPart.LEFT_LEG, intensity);
        }

        if (data.hasFracture(BodyPart.RIGHT_LEG)) {
            float intensity = data.getFractureIntensity(BodyPart.RIGHT_LEG);
            HealthAttributeHelper.applyLegFracture(player, BodyPart.RIGHT_LEG, intensity);
        }

        // Руки
        if (data.hasFracture(BodyPart.LEFT_ARM)) {
            float intensity = data.getFractureIntensity(BodyPart.LEFT_ARM);
            HealthAttributeHelper.applyArmFracture(player, BodyPart.LEFT_ARM, intensity);
        }

        if (data.hasFracture(BodyPart.RIGHT_ARM)) {
            float intensity = data.getFractureIntensity(BodyPart.RIGHT_ARM);
            HealthAttributeHelper.applyArmFracture(player, BodyPart.RIGHT_ARM, intensity);
        }

        VanillaAttributeSynchronizer.synchronizeMovementSpeed(player);
        VanillaAttributeSynchronizer.synchronizeAttackSpeed(player);
    }

    /**
     * Дебафы от уничтоженных конечностей
     */
    private static void applyDestroyedLimbsDebuffs(ServerPlayer player) {
        HealthAttributeHelper.clearDestroyedLimbDebuffs(player);

        boolean leftArmDestroyed = ExodusCoreAPI.getBodyPartHP(player, BodyPart.LEFT_ARM) <= 0;
        boolean rightArmDestroyed = ExodusCoreAPI.getBodyPartHP(player, BodyPart.RIGHT_ARM) <= 0;
        boolean leftLegDestroyed = ExodusCoreAPI.getBodyPartHP(player, BodyPart.LEFT_LEG) <= 0;
        boolean rightLegDestroyed = ExodusCoreAPI.getBodyPartHP(player, BodyPart.RIGHT_LEG) <= 0;

        // Ноги
        if (leftLegDestroyed && rightLegDestroyed) {
            HealthAttributeHelper.applyBothLegsDestroyed(player);
        } else {
            if (leftLegDestroyed) {
                HealthAttributeHelper.applyDestroyedLeg(player, BodyPart.LEFT_LEG);
            }
            if (rightLegDestroyed) {
                HealthAttributeHelper.applyDestroyedLeg(player, BodyPart.RIGHT_LEG);
            }
        }

        // Руки
        if (leftArmDestroyed && rightArmDestroyed) {
            HealthAttributeHelper.applyBothArmsDestroyed(player);
        } else {
            if (leftArmDestroyed) {
                HealthAttributeHelper.applyDestroyedArm(player, BodyPart.LEFT_ARM);
            }
            if (rightArmDestroyed) {
                HealthAttributeHelper.applyDestroyedArm(player, BodyPart.RIGHT_ARM);
            }
        }

        VanillaAttributeSynchronizer.synchronizeMovementSpeed(player);
        VanillaAttributeSynchronizer.synchronizeAttackSpeed(player);
    }
}