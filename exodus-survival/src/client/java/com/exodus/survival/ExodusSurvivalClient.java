package com.exodus.survival;

import net.fabricmc.api.ClientModInitializer;

/**
 * Клиентская часть Exodus Survival
 */
public class ExodusSurvivalClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ExodusSurvival.LOGGER.info("Exodus Survival - Client initialization");

        // Здесь будут клиентские системы (звуки, визуальные эффекты)

        ExodusSurvival.LOGGER.info("Exodus Survival - Client initialized successfully");
    }
}