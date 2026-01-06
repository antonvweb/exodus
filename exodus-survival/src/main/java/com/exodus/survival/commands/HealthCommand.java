package com.exodus.survival.commands;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.health.BodyPart;
import com.exodus.core.api.player.health.BleedingType;
import com.exodus.core.api.player.health.PlayerHealthData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class HealthCommand {

    /**
     * Регистрировать команду
     */
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerHealthCommand(dispatcher);
        });
    }

    /**
     * Зарегистрировать команду /health
     */
    private static void registerHealthCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("health")
                        .executes(HealthCommand::executeHealth)
        );
    }

    /**
     * Выполнить команду /health
     */
    private static int executeHealth(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("§cТолько игрок может использовать эту команду!"));
            return 0;
        }

        PlayerHealthData data = ExodusCoreAPI.getHealthData(player);

        // === ЗАГОЛОВОК ===
        player.sendSystemMessage(Component.literal("§6§l========== ЗДОРОВЬЕ =========="));

        // === HP ЧАСТЕЙ ТЕЛА ===
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§e§lЧАСТИ ТЕЛА:"));

        for (BodyPart part : BodyPart.values()) {
            float currentHP = ExodusCoreAPI.getBodyPartHP(player, part);
            float maxHP = ExodusCoreAPI.getMaxBodyPartHP(player, part);
            float percentage = ExodusCoreAPI.getBodyPartHPPercentage(player, part) * 100;

            // Цвет в зависимости от HP
            String color;
            if (percentage >= 70) {
                color = "§a"; // Зелёный
            } else if (percentage >= 40) {
                color = "§e"; // Жёлтый
            } else {
                color = "§c"; // Красный
            }

            String text = String.format("%s  %s: %s%.1f§7/§f%.1f §7(%.0f%%)",
                    color, part.getDisplayName(), color, currentHP, maxHP, percentage);

            player.sendSystemMessage(Component.literal(text));
        }

        // === КРОВОТЕЧЕНИЯ ===
        boolean hasAnyBleeding = false;
        for (BodyPart part : BodyPart.values()) {
            if (data.hasBleeding(part)) {
                if (!hasAnyBleeding) {
                    player.sendSystemMessage(Component.literal(""));
                    player.sendSystemMessage(Component.literal("§c§lКРОВОТЕЧЕНИЯ:"));
                    hasAnyBleeding = true;
                }

                BleedingType type = data.getBleedingType(part);
                float damage = data.getBleedingDamage(part);

                String text = String.format("§c  %s: §f%s §7(%.1f HP/сек)",
                        part.getDisplayName(), type.getDisplayName(), damage);

                player.sendSystemMessage(Component.literal(text));
            }
        }

        // === ПЕРЕЛОМЫ ===
        boolean hasAnyFracture = false;
        for (BodyPart part : BodyPart.values()) {
            if (data.hasFracture(part)) {
                if (!hasAnyFracture) {
                    player.sendSystemMessage(Component.literal(""));
                    player.sendSystemMessage(Component.literal("§f§lПЕРЕЛОМЫ:"));
                    hasAnyFracture = true;
                }

                float intensity = data.getFractureIntensity(part);
                int intensityPercent = (int)(intensity * 100);

                String text = String.format("§f  %s: §7Интенсивность §c%d%%",
                        part.getDisplayName(), intensityPercent);

                player.sendSystemMessage(Component.literal(text));
            }
        }

        // === БОЛЬ ===
        if (data.hasPain()) {
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§6§lБОЛЬ:"));

            int intensity = (int)(data.getPainIntensity() * 100);
            String text = String.format("§6  Интенсивность: §c%d%%", intensity);

            player.sendSystemMessage(Component.literal(text));
        }

        // === ИТОГ ===
        player.sendSystemMessage(Component.literal(""));

        if (data.isAlive()) {
            player.sendSystemMessage(Component.literal("§a§lСостояние: ЖИВ"));
        } else {
            player.sendSystemMessage(Component.literal("§4§lСостояние: МЁРТВ"));
        }

        player.sendSystemMessage(Component.literal("§6§l================================"));

        return 1;
    }
}