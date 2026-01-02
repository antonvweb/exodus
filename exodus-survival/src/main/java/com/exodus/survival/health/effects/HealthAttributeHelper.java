package com.exodus.survival.health.effects;

import com.exodus.core.player.attributes.AttributeManager;
import com.exodus.core.api.attributes.AttributeModifier;
import com.exodus.core.api.attributes.AttributeType;
import com.exodus.core.api.player.BodyPart;
import net.minecraft.world.entity.player.Player;

/**
 * Вспомогательный класс для применения дебафов через атрибуты
 */
public class HealthAttributeHelper {

    /**
     * Применить штраф скорости от перелома ноги
     */
    public static void applyLegFracture(Player player, BodyPart leg, float intensity) {
        String modifierName = "fracture_" + leg.getId();

        AttributeModifier modifier = new AttributeModifier(
                modifierName,
                -0.30f * intensity, // -30% за полный перелом (intensity = 1.0)
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                "fracture"
        );

        AttributeManager.addModifier(player, AttributeType.MOVEMENT_SPEED, modifier);
    }

    /**
     * Применить штраф копания от перелома руки
     */
    public static void applyArmFracture(Player player, BodyPart arm, float intensity) {
        String modifierName = "fracture_" + arm.getId();

        // Mining Speed
        AttributeModifier miningModifier = new AttributeModifier(
                modifierName + "_mining",
                -0.40f * intensity,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                "fracture"
        );
        AttributeManager.addModifier(player, AttributeType.MINING_SPEED, miningModifier);

        // Attack Speed
        AttributeModifier attackModifier = new AttributeModifier(
                modifierName + "_attack",
                -0.30f * intensity,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                "fracture"
        );
        AttributeManager.addModifier(player, AttributeType.ATTACK_SPEED, attackModifier);
    }

    /**
     * Применить дебафы от боли
     */
    public static void applyPain(Player player, float intensity) {
        // Mining Speed
        AttributeModifier miningModifier = new AttributeModifier(
                "pain_mining",
                -0.20f * intensity,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                "pain"
        );
        AttributeManager.addModifier(player, AttributeType.MINING_SPEED, miningModifier);

        // Movement Speed (только если боль > 50%)
        if (intensity > 0.5f) {
            AttributeModifier movementModifier = new AttributeModifier(
                    "pain_movement",
                    -0.10f * intensity,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                    "pain"
            );
            AttributeManager.addModifier(player, AttributeType.MOVEMENT_SPEED, movementModifier);
        }
    }

    /**
     * Применить ЭКСТРЕМАЛЬНЫЙ штраф от УНИЧТОЖЕННОЙ ноги (HP = 0)
     * Намного сильнее чем перелом!
     */
    public static void applyDestroyedLeg(Player player, BodyPart leg) {
        String modifierName = "destroyed_" + leg.getId();

        AttributeModifier modifier = new AttributeModifier(
                modifierName,
                -0.80f, // -80% скорости (почти не ходить)
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                "destroyed_limb"  // ← Отдельный источник от переломов!
        );

        AttributeManager.addModifier(player, AttributeType.MOVEMENT_SPEED, modifier);
    }

    /**
     * Применить ЭКСТРЕМАЛЬНЫЙ штраф если ОБЕ ноги уничтожены
     */
    public static void applyBothLegsDestroyed(Player player) {
        AttributeModifier modifier = new AttributeModifier(
                "both_legs_destroyed",
                -0.95f, // -95% скорости (можно только ползти)
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                "destroyed_limb"
        );

        AttributeManager.addModifier(player, AttributeType.MOVEMENT_SPEED, modifier);
    }

    /**
     * Применить ЭКСТРЕМАЛЬНЫЙ штраф от УНИЧТОЖЕННОЙ руки (HP = 0)
     */
    public static void applyDestroyedArm(Player player, BodyPart arm) {
        String modifierName = "destroyed_" + arm.getId();

        // Mining Speed
        AttributeModifier miningModifier = new AttributeModifier(
                modifierName + "_mining",
                -0.70f, // -70% скорости копания
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                "destroyed_limb"
        );
        AttributeManager.addModifier(player, AttributeType.MINING_SPEED, miningModifier);

        // Attack Speed
        AttributeModifier attackModifier = new AttributeModifier(
                modifierName + "_attack",
                -0.50f, // -50% скорости атаки
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                "destroyed_limb"
        );
        AttributeManager.addModifier(player, AttributeType.ATTACK_SPEED, attackModifier);
    }

    /**
     * Применить ЭКСТРЕМАЛЬНЫЙ штраф если ОБЕ руки уничтожены
     */
    public static void applyBothArmsDestroyed(Player player) {
        // Mining Speed
        AttributeModifier miningModifier = new AttributeModifier(
                "both_arms_destroyed_mining",
                -0.90f, // -90% копания (почти невозможно копать)
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                "destroyed_limb"
        );
        AttributeManager.addModifier(player, AttributeType.MINING_SPEED, miningModifier);

        // Attack Speed
        AttributeModifier attackModifier = new AttributeModifier(
                "both_arms_destroyed_attack",
                -0.80f, // -80% скорости атаки
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                "destroyed_limb"
        );
        AttributeManager.addModifier(player, AttributeType.ATTACK_SPEED, attackModifier);
    }

    /**
     * Убрать ВСЕ дебафы от уничтоженных конечностей
     */
    public static void clearDestroyedLimbDebuffs(Player player) {
        AttributeManager.removeModifiersBySource(player, AttributeType.MOVEMENT_SPEED, "destroyed_limb");
        AttributeManager.removeModifiersBySource(player, AttributeType.MINING_SPEED, "destroyed_limb");
        AttributeManager.removeModifiersBySource(player, AttributeType.ATTACK_SPEED, "destroyed_limb");
    }

    /**
     * Убрать ВСЕ дебафы от переломов
     */
    public static void clearFractureDebuffs(Player player) {
        AttributeManager.removeModifiersBySource(player, AttributeType.MOVEMENT_SPEED, "fracture");
        AttributeManager.removeModifiersBySource(player, AttributeType.MINING_SPEED, "fracture");
        AttributeManager.removeModifiersBySource(player, AttributeType.ATTACK_SPEED, "fracture");
    }

    /**
     * Убрать дебафы от боли
     */
    public static void clearPainDebuffs(Player player) {
        AttributeManager.removeModifiersBySource(player, AttributeType.MOVEMENT_SPEED, "pain");
        AttributeManager.removeModifiersBySource(player, AttributeType.MINING_SPEED, "pain");
    }

    /**
     * Убрать дебафы от температуры
     */
    public static void clearTemperatureDebuffs(Player player) {
        AttributeManager.removeModifiersBySource(player, AttributeType.MOVEMENT_SPEED, "temperature");
        AttributeManager.removeModifiersBySource(player, AttributeType.STAMINA_REGEN, "temperature");
        AttributeManager.removeModifiersBySource(player, AttributeType.THIRST_DRAIN_RATE, "temperature");
    }
}