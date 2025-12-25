package com.exodus.core;

import com.exodus.core.network.StatsNetworking;
import com.exodus.core.player.ExodusPlayerManager;
import com.exodus.core.stats.PlayerStatsManager;
import com.exodus.core.test.TestStatsSystem;
import com.exodus.core.test.TestPlayerCommand;  // ← ДОБАВЬ ИМПОРТ
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;  // ← ДОБАВЬ ИМПОРТ
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExodusCore implements ModInitializer {
    public static final String MOD_ID = "exodus-core";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Exodus Core - Initialization");

        PlayerStatsManager.registerEvents();
        ExodusPlayerManager.registerEvents();

        // Регистрируем тестовые команды ← ДОБАВЬ ЭТО
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TestPlayerCommand.register(dispatcher);
        });

        // Тикер для синхронизации статов с клиентом
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    TestStatsSystem.initializeTestStats(player);
                    StatsNetworking.syncStatsToClient(player);
                }
            }
        });

        LOGGER.info("Exodus Core - API initialized successfully");
    }
}