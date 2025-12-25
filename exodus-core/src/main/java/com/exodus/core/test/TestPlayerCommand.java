package com.exodus.core.test;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.BodyPart;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class TestPlayerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("testplayer")
                        .executes(TestPlayerCommand::execute)
        );

        dispatcher.register(
                Commands.literal("damageleg")
                        .executes(TestPlayerCommand::damageLeg)
        );

        dispatcher.register(
                Commands.literal("healleg")
                        .executes(TestPlayerCommand::healLeg)
        );

        dispatcher.register(
                Commands.literal("showbody")
                        .executes(TestPlayerCommand::showBodyStatus)
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            player.sendSystemMessage(Component.literal("=== Exodus Player Test ==="));
            player.sendSystemMessage(Component.literal("Доступные команды:"));
            player.sendSystemMessage(Component.literal("/damageleg - Повредить левую ногу (-30 HP)"));
            player.sendSystemMessage(Component.literal("/healleg - Вылечить левую ногу (+50 HP)"));
            player.sendSystemMessage(Component.literal("/showbody - Показать состояние всех частей тела"));

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Ошибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int damageLeg(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            // Наносим урон левой ноге
            ExodusCoreAPI.damageBodyPart(player, BodyPart.LEFT_LEG, 30f);

            // Получаем текущее состояние
            float legHP = ExodusCoreAPI.getBodyPart(player, BodyPart.LEFT_LEG).currentHP;
            float legMaxHP = ExodusCoreAPI.getBodyPart(player, BodyPart.LEFT_LEG).maxHP;
            boolean isBroken = ExodusCoreAPI.getBodyPart(player, BodyPart.LEFT_LEG).isBroken;

            player.sendSystemMessage(Component.literal("Нанесено 30 урона левой ноге!"));
            player.sendSystemMessage(Component.literal("Левая нога: " + legHP + "/" + legMaxHP + " HP"));
            if (isBroken) {
                player.sendSystemMessage(Component.literal("⚠ НОГА СЛОМАНА!"));
            }

            float totalHP = ExodusCoreAPI.getTotalHP(player);
            player.sendSystemMessage(Component.literal("Общее HP: " + totalHP));

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Ошибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int healLeg(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            // Лечим левую ногу
            ExodusCoreAPI.healBodyPart(player, BodyPart.LEFT_LEG, 50f);

            // Получаем текущее состояние
            float legHP = ExodusCoreAPI.getBodyPart(player, BodyPart.LEFT_LEG).currentHP;
            float legMaxHP = ExodusCoreAPI.getBodyPart(player, BodyPart.LEFT_LEG).maxHP;
            boolean isBroken = ExodusCoreAPI.getBodyPart(player, BodyPart.LEFT_LEG).isBroken;

            player.sendSystemMessage(Component.literal("Восстановлено 50 HP левой ноги!"));
            player.sendSystemMessage(Component.literal("Левая нога: " + legHP + "/" + legMaxHP + " HP"));
            if (!isBroken) {
                player.sendSystemMessage(Component.literal("✓ Нога восстановлена!"));
            }

            float totalHP = ExodusCoreAPI.getTotalHP(player);
            player.sendSystemMessage(Component.literal("Общее HP: " + totalHP));

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Ошибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int showBodyStatus(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            player.sendSystemMessage(Component.literal("=== Состояние частей тела ==="));

            // Показываем все части тела
            for (BodyPart part : BodyPart.values()) {
                float hp = ExodusCoreAPI.getBodyPart(player, part).currentHP;
                float maxHP = ExodusCoreAPI.getBodyPart(player, part).maxHP;
                boolean isBroken = ExodusCoreAPI.getBodyPart(player, part).isBroken;

                String status = isBroken ? " [СЛОМАНА]" : "";
                player.sendSystemMessage(Component.literal(
                        part.getDisplayName() + ": " +
                                String.format("%.1f", hp) + "/" +
                                String.format("%.1f", maxHP) + " HP" +
                                status
                ));
            }

            player.sendSystemMessage(Component.literal("---"));
            player.sendSystemMessage(Component.literal("Всего HP: " +
                    String.format("%.1f", ExodusCoreAPI.getTotalHP(player)) + "/" +
                    String.format("%.1f", ExodusCoreAPI.getTotalMaxHP(player))
            ));

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Ошибка: " + e.getMessage()));
            return 0;
        }
    }
}