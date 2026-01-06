package com.exodus.survival.client.inventory.network;

import com.exodus.survival.inventory.network.TakeItemEntityPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Обработчик сетевых пакетов на клиенте
 */
public class ClientNetworkHandler {

    public static void sendTakeItemRequest(int itemId, int amount) {
        // playerId можно получить на сервере из контекста
        TakeItemEntityPacket packet = new TakeItemEntityPacket(itemId, amount);
        ClientPlayNetworking.send(packet);
    }
}