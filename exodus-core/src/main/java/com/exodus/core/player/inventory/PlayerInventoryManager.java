package com.exodus.core.player.inventory;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInventoryManager {
    private static final Map<UUID, PlayerInventoryComponent> components = new HashMap<>();

    public static PlayerInventoryComponent getComponent(Player player) {
        return components.computeIfAbsent(
                player.getUUID(),
                uuid -> new PlayerInventoryComponent(player)
        );
    }

    public static void removeComponent(UUID uuid) {
        components.remove(uuid);
    }

    public static void registerEvents() {
        // ===== СОБЫТИЕ 1: ВХОД =====
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();

            // Просто получаем компонент - всё!
            // NBT загрузится автоматически через Mixin
            getComponent(player);
        });

        // ===== СОБЫТИЕ 2: ВЫХОД =====
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();

            // NBT сохранится автоматически через Mixin
            // Опционально удаляем из памяти
            removeComponent(player.getUUID());
        });

        // ===== СОБЫТИЕ 3: СМЕРТЬ =====
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {  // Это смерть
                PlayerInventoryComponent inventory = getComponent(oldPlayer);

                // Очищаем инвентарь
                inventory.getData().clear();
            }
        });

        // ===== СОБЫТИЕ 4: ТЕЛЕПОРТ =====
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            // Ничего не делаем - компонент один и тот же!
        });
    }
}
