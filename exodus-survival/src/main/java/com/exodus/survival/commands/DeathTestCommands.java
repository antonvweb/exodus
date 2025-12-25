package com.exodus.survival.commands;

import com.exodus.core.ExodusCoreAPI;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class DeathTestCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        // /setblood <процент>
        dispatcher.register(Commands.literal("setblood")
                .then(Commands.argument("percent", FloatArgumentType.floatArg(0, 100))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            float percent = FloatArgumentType.getFloat(context, "percent");

                            ExodusCoreAPI.setBloodLevel(player, percent);
                            player.sendSystemMessage(Component.literal("Уровень крови: " + percent + "%"));

                            return 1;
                        })
                )
        );

        // /settemp <градусы>
        dispatcher.register(Commands.literal("settemp")
                .then(Commands.argument("celsius", FloatArgumentType.floatArg(20, 45))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            float temp = FloatArgumentType.getFloat(context, "celsius");

                            ExodusCoreAPI.setBodyTemperature(player, temp);
                            player.sendSystemMessage(Component.literal("Температура тела: " + temp + "°C"));

                            return 1;
                        })
                )
        );
    }
}