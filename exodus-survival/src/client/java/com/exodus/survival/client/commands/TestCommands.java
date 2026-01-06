package com.exodus.survival.client.commands;

import com.exodus.survival.client.screen.ExodusInventoryScreen;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;

public class TestCommands {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(TestCommands::registerCommands);
    }

    private static void registerCommands(
            CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandBuildContext registryAccess
    ) {
        dispatcher.register(
                ClientCommandManager.literal("testinv")
                        .executes(context -> {
                            System.out.println("=== TESTINV COMMAND EXECUTED ===");

                            Minecraft mc = Minecraft.getInstance();
                            System.out.println("Current screen BEFORE: " + mc.screen);


                            return 1;
                        })
        );
    }
}