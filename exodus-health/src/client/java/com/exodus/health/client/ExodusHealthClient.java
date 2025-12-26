package com.exodus.health.client;

import net.fabricmc.api.ClientModInitializer;

public class ExodusHealthClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Регистрируем HUD для отображения здоровья
        HealthHud.register();
    }
}
