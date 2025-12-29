package com.exodus.health;

import com.exodus.health.network.ClientNetworkHandler;
import net.fabricmc.api.ClientModInitializer;

public class ExodusHealthClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("=== EXODUS HEALTH CLIENT INIT ===");

        // Регистрируем HUD для отображения здоровья
        BodyHealthHud.register();

        // ✅ Регистрируем обработчики сетевых пакетов
        ClientNetworkHandler.register();

        System.out.println("=== EXODUS HEALTH CLIENT READY ===");
    }
}