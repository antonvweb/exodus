package com.exodus.survival.client;

import com.exodus.survival.client.commands.TestCommands;
import com.exodus.survival.client.events.ExodusClientEvents;
import com.exodus.survival.client.hud.BodyHealthHud;
import com.exodus.survival.client.health.network.ClientNetworkHandler;
import net.fabricmc.api.ClientModInitializer;

public class ExodusSurvivalClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientNetworkHandler.register();
        BodyHealthHud.register();
        ExodusKeyBindings.register();
        ExodusClientEvents.register();
        TestCommands.register();
    }
}
