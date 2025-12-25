package com.exodus.hud;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExodusHUD implements ModInitializer {
    public static final String MOD_ID = "exodus-hud";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Exodus HUD - Server/Common initialization");

        // Здесь будет общая логика (регистрация пакетов, атрибутов и т.д.)
    }
}