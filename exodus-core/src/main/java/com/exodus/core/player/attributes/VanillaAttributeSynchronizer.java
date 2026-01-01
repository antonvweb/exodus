package com.exodus.core.player.attributes;

import com.exodus.core.api.attributes.AttributeType;
import com.exodus.core.api.player.StatType;
import com.exodus.core.player.stats.PlayerStatsManager;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class VanillaAttributeSynchronizer {
    // ✅ ДОБАВЬ ЭТО В НАЧАЛО КЛАССА:
    private static final Logger LOGGER = LoggerFactory.getLogger("ExodusAttributes");

    private static final UUID MOVEMENT_SPEED_MODIFIER_UUID =
            UUID.fromString("8f3e7a2b-4c1d-4b9e-a5f6-1234567890ab");
    private static final UUID ATTACK_SPEED_MODIFIER_UUID =
            UUID.fromString("a07f84cd-5d7d-4aac-bcca-05ebbf8896ed");
    private static final UUID ARMOR_MODIFIER_UUID =
            UUID.fromString("44d9f882-ec0c-496c-8b0d-35117391edd9");

    public static void synchronizeMovementSpeed(Player player) {
        float ourSpeed = AttributeManager.getValue(player, AttributeType.MOVEMENT_SPEED);

        AttributeInstance vanillaSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (vanillaSpeed == null) {
            return;
        }

        vanillaSpeed.removeModifier(MOVEMENT_SPEED_MODIFIER_UUID);

        float bonus = ourSpeed - 1.0f;

        AttributeModifier newModifier = new AttributeModifier(
                MOVEMENT_SPEED_MODIFIER_UUID,
                "dex_speed_bonus",
                bonus,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        vanillaSpeed.addTransientModifier(newModifier);
    }

    public static void synchronizeAttackSpeed(Player player) {
        float ourSpeed = AttributeManager.getValue(player, AttributeType.ATTACK_SPEED);

        AttributeInstance vanillaSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (vanillaSpeed == null) {
            return;
        }

        vanillaSpeed.removeModifier(ATTACK_SPEED_MODIFIER_UUID);

        float bonus = ourSpeed - 1.0f;

        AttributeModifier newModifier = new AttributeModifier(
                ATTACK_SPEED_MODIFIER_UUID,
                "attack_speed_bonus",
                bonus,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        vanillaSpeed.addTransientModifier(newModifier);
    }

    public static void synchronizeArmor(Player player) {
        float ourArmor = AttributeManager.getValue(player, AttributeType.ARMOR);

        AttributeInstance vanillaArmor = player.getAttribute(Attributes.ARMOR);
        if (vanillaArmor == null) {
            return;
        }

        vanillaArmor.removeModifier(ARMOR_MODIFIER_UUID);

        AttributeModifier newModifier = new AttributeModifier(
                ARMOR_MODIFIER_UUID,
                "armor_bonus",
                ourArmor,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        vanillaArmor.addTransientModifier(newModifier);
    }
}
