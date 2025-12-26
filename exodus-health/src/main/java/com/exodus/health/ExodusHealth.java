package com.exodus.health;

import com.exodus.health.effects.StatusEffectManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExodusHealth implements ModInitializer {

    public static final String MOD_ID = "exodus-health";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Exodus Health - Initialization");

        // ✅ В Fabric API 1.20.1 пакеты регистрируются автоматически
        // Нет необходимости вызывать DamagePacket.register()

        // Регистрируем систему статусных эффектов
        StatusEffectManager.register();

        LOGGER.info("Exodus Health - Initialized successfully");
    }
}