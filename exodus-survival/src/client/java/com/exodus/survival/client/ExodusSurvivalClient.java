package com.exodus.survival.client;

import com.exodus.survival.client.health.BodyHealthHud;
import com.exodus.survival.client.health.network.ClientNetworkHandler;
import net.fabricmc.api.ClientModInitializer;

public class ExodusSurvivalClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientNetworkHandler.register();
        BodyHealthHud.register();
    }
}
