package com.exodus.survival;

import com.exodus.core.api.player.DeathCause;
import com.exodus.survival.commands.DeathTestCommands;
import com.exodus.survival.systems.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Главный класс мода Exodus Survival
 * Управляет всеми системами выживания
 */
public class ExodusSurvival implements ModInitializer {
    public static final String MOD_ID = "exodus-survival";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Exodus Survival - Initialization");

        registerSurvivalSystems();

        // ДОБАВЬ ЭТО:
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            DeathTestCommands.register(dispatcher);
        });

        LOGGER.info("Exodus Survival - Systems initialized successfully");
    }

    private void registerSurvivalSystems() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Каждую секунду (20 тиков)
            if (server.getTickCount() % 20 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    // Существующие системы
                    VitalDecaySystem.tick(player);
                    OxygenSystem.tick(player);
                    HungerThirstSystem.tick(player);
                    EnergySystem.tick(player);

                    // ДОБАВЬ ЭТО: Проверка условий смерти
                    DeathCause deathCause = DeathConditionSystem.checkDeathConditions(player);
                    if (deathCause != null) {
                        DeathConditionSystem.killPlayer(player, deathCause);
                    }
                }
            }
        });

        LOGGER.info("Survival systems registered");
    }
}