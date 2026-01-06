package com.exodus.survival.client.events;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.inventory.PlayerInventoryData;
import com.exodus.core.player.inventory.PlayerInventoryManager;
import com.exodus.survival.client.ExodusKeyBindings;
import com.exodus.survival.client.hud.BodyHealthHud;
import com.exodus.survival.client.inventory.network.ClientNetworkHandler;
import com.exodus.survival.client.raycast.ItemRaycast;
import com.exodus.survival.client.screen.ExodusInventoryScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Обработка клиентских событий
 */
public class ExodusClientEvents {

    /**
     * Регистрация всех событий
     */
    public static void register() {
        // Событие: конец каждого клиентского тика
        ClientTickEvents.END_CLIENT_TICK.register(ExodusClientEvents::onClientTick);
    }

    /**
     * Вызывается каждый тик (~20 раз в секунду)
     */
    private static void onClientTick(Minecraft client) {
        if (client.player == null) return;

        if (ExodusKeyBindings.OPEN_INVENTORY.consumeClick()) {
            Screen currentScreen = client.screen;

            if (currentScreen == null) {
                PlayerInventoryData data = ExodusCoreAPI.getInventoryData(client.player);
                client.setScreen(new ExodusInventoryScreen(data));
            }
        }

        ItemEntity lookAt = ItemRaycast.findLookedAtItem(client.player);
        BodyHealthHud.setIsLookAt(lookAt != null);

        if(ExodusKeyBindings.PICK_ITEM.consumeClick() && lookAt != null){
            ItemStack stack = lookAt.getItem();

            System.out.println("=== CLIENT: press pick item button ===");
            System.out.println("Item ID: " + lookAt.getId());
            System.out.println("Item: " + lookAt.getItem().getDisplayName().getString());

            ClientNetworkHandler.sendTakeItemRequest(lookAt.getId(), stack.getCount());
        }
    }
}