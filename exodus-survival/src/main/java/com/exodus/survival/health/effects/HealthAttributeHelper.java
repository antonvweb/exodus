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
}