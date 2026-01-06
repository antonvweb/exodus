package com.exodus.survival.inventory.network;

import com.exodus.core.api.player.inventory.PlayerInventoryData;
import com.exodus.core.player.inventory.PlayerInventoryManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

public class ServerNetworkHandler {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(
                TakeItemEntityPacket.TYPE,
                (packet, player, responseSender) -> {
                    Entity entity = player.level().getEntity(packet.itemId());

                    if (entity instanceof ItemEntity item) {
                        PlayerInventoryData inventory = PlayerInventoryManager.getComponent(player).getData();
                        if (inventory.addItem(item.getItem())) {
                            item.discard(); // Удаляем предмет из мира
                            System.out.println("=== Add item ===");
                        }
                    }
                }
        );
    }
}
