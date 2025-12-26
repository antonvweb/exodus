package com.exodus.core;

import com.exodus.core.player.PlayerHealthManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExodusCore implements ModInitializer {
    public static final String MOD_ID = "exodus-core";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Exodus Core - Initialization");

        PlayerHealthManager.registerEvents();

        LOGGER.info("Exodus Core - Initialized successfully");
    }
}
