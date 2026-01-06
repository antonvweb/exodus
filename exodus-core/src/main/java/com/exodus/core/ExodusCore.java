package com.exodus.core;

import com.exodus.core.player.health.PlayerHealthManager;
import com.exodus.core.player.inventory.PlayerInventoryManager;
import com.exodus.core.player.stats.PlayerStatsManager;
import com.exodus.core.player.vitals.PlayerVitalsManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExodusCore implements ModInitializer {
    public static final String MOD_ID = "exodus-core";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Exodus Core - Initialization");

        // Регистрируем события игрока
        PlayerHealthManager.registerEvents();
        PlayerStatsManager.registerEvents();
        PlayerVitalsManager.registerEvents();
        PlayerInventoryManager.registerEvents();

        LOGGER.info("Exodus Core - Initialized successfully");
    }
}
