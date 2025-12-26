package com.exodus.core;

import net.fabricmc.api.ClientModInitializer;

public class ExodusCoreClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ExodusCore.LOGGER.info("Exodus Core - Client initialization");
    }
}