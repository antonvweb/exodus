package com.exodus.survival;

import com.exodus.survival.commands.HealthCommand;
import com.exodus.survival.health.effects.StatusEffectManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExodusSurvival implements ModInitializer {
    public static final String MOD_ID = "exodus-survival";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ExodusSounds.register();      // ← ДОБАВЬ ЭТУ СТРОЧКУ
        StatusEffectManager.register();
        HealthCommand.register();
    }
}