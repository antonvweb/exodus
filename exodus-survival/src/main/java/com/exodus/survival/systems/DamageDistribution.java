package com.exodus.survival.systems;

import com.exodus.core.api.player.BodyPart;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

import java.util.HashMap;
import java.util.Map;

public class DamageDistribution {

    public static Map<BodyPart, Float> getDamageDistribution(ServerPlayer player, DamageSource source, float amount) {

        // === МГНОВЕННАЯ СМЕРТЬ ===
        // ИСПРАВЛЕНО: OUT_OF_WORLD → FELL_OUT_OF_WORLD
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.GENERIC)) {
            return createInstantDeath();
        }

        // === ПАДЕНИЕ ===
        if (source.is(DamageTypes.FALL) || source.is(DamageTypes.FLY_INTO_WALL)) {
            return createFallDamage(amount);
        }

        // === УТОПЛЕНИЕ/УДУШЬЕ ===
        if (source.is(DamageTypes.IN_WALL)) {
            return createSuffocationDamage(amount);
        }

        // === ОГОНЬ/ЛАВА ===
        if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) ||
                source.is(DamageTypes.LAVA) || source.is(DamageTypes.HOT_FLOOR)) {
            return createFireDamage(amount);
        }

        // === ВЗРЫВ ===
        if (source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION)) {
            return createExplosionDamage(amount);
        }

        // === МОЛНИЯ ===
        if (source.is(DamageTypes.LIGHTNING_BOLT)) {
            return createLightningDamage(amount);
        }

        // === ИССУШЕНИЕ/ЯД (DoT) ===
        if (source.is(DamageTypes.WITHER) || source.is(DamageTypes.MAGIC)) {
            return createPoisonDamage(amount);
        }

        // === ЗАМЕРЗАНИЕ ===
        if (source.is(DamageTypes.FREEZE)) {
            return createFreezeDamage(amount);
        }

        // === КАКТУС/ШИПЫ ===
        if (source.is(DamageTypes.CACTUS) || source.is(DamageTypes.SWEET_BERRY_BUSH)) {
            return createPiercingDamage(amount);
        }

        // === ПАДАЮЩИЕ БЛОКИ ===
        // ИСПРАВЛЕНО: убрали ANVIL и STALAGMITE (их нет в DamageTypes)
        if (source.is(DamageTypes.FALLING_BLOCK) || source.is(DamageTypes.FALLING_STALACTITE)) {
            return createCrushingDamage(amount);
        }

        // === АТАКИ (меч, стрела, моб) ===
        return null;
    }

    // === ВСЕ МЕТОДЫ createXXXDamage остаются БЕЗ ИЗМЕНЕНИЙ ===

    private static Map<BodyPart, Float> createInstantDeath() {
        Map<BodyPart, Float> damage = new HashMap<>();
        for (BodyPart part : BodyPart.values()) {
            damage.put(part, 9999f);
        }
        return damage;
    }

    private static Map<BodyPart, Float> createFallDamage(float amount) {
        Map<BodyPart, Float> damage = new HashMap<>();
        damage.put(BodyPart.LEFT_LEG, amount * 0.5f);
        damage.put(BodyPart.RIGHT_LEG, amount * 0.5f);
        return damage;
    }

    private static Map<BodyPart, Float> createSuffocationDamage(float amount) {
        Map<BodyPart, Float> damage = new HashMap<>();
        damage.put(BodyPart.HEAD, amount * 0.7f);
        damage.put(BodyPart.TORSO, amount * 0.3f);
        return damage;
    }

    private static Map<BodyPart, Float> createStarvationDamage(float amount) {
        Map<BodyPart, Float> damage = new HashMap<>();
        damage.put(BodyPart.TORSO, amount);
        return damage;
    }

    private static Map<BodyPart, Float> createFireDamage(float amount) {
        Map<BodyPart, Float> damage = new HashMap<>();
        float perPart = amount / 6f;
        for (BodyPart part : BodyPart.values()) {
            damage.put(part, perPart);
        }
        return damage;
    }

    private static Map<BodyPart, Float> createExplosionDamage(float amount) {
        Map<BodyPart, Float> damage = new HashMap<>();
        damage.put(BodyPart.HEAD, amount * 0.1f);
        damage.put(BodyPart.TORSO, amount * 0.3f);
        damage.put(BodyPart.LEFT_ARM, amount * 0.15f);
        damage.put(BodyPart.RIGHT_ARM, amount * 0.15f);
        damage.put(BodyPart.LEFT_LEG, amount * 0.15f);
        damage.put(BodyPart.RIGHT_LEG, amount * 0.15f);
        return damage;
    }

    private static Map<BodyPart, Float> createLightningDamage(float amount) {
        Map<BodyPart, Float> damage = new HashMap<>();
        for (BodyPart part : BodyPart.values()) {
            damage.put(part, amount * 1.5f / 6f);
        }
        return damage;
    }

    private static Map<BodyPart, Float> createPoisonDamage(float amount) {
        Map<BodyPart, Float> damage = new HashMap<>();
        float perPart = amount / 6f;
        for (BodyPart part : BodyPart.values()) {
            damage.put(part, perPart);
        }
        return damage;
    }

    private static Map<BodyPart, Float> createFreezeDamage(float amount) {
        Map<BodyPart, Float> damage = new HashMap<>();
        damage.put(BodyPart.LEFT_ARM, amount * 0.25f);
        damage.put(BodyPart.RIGHT_ARM, amount * 0.25f);
        damage.put(BodyPart.LEFT_LEG, amount * 0.25f);
        damage.put(BodyPart.RIGHT_LEG, amount * 0.25f);
        return damage;
    }

    private static Map<BodyPart, Float> createPiercingDamage(float amount) {
        Map<BodyPart, Float> damage = new HashMap<>();
        BodyPart[] possibleParts = {BodyPart.TORSO, BodyPart.LEFT_LEG, BodyPart.RIGHT_LEG};
        BodyPart hitPart = possibleParts[(int)(Math.random() * possibleParts.length)];
        damage.put(hitPart, amount);
        return damage;
    }

    private static Map<BodyPart, Float> createCrushingDamage(float amount) {
        Map<BodyPart, Float> damage = new HashMap<>();
        damage.put(BodyPart.HEAD, amount * 0.5f);
        damage.put(BodyPart.LEFT_ARM, amount * 0.25f);
        damage.put(BodyPart.RIGHT_ARM, amount * 0.25f);
        return damage;
    }
}