package com.exodus.core;

import com.exodus.core.api.stats.VitalType;
import com.exodus.core.network.StatsNetworking;
import com.exodus.core.network.SyncStatsPayload;
import com.exodus.core.stats.PlayerVital;
import com.exodus.core.stats.PlayerStatsComponent;
import com.exodus.core.stats.PlayerStatsManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class ExodusCoreClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ExodusCore.LOGGER.info("Exodus Core - Client initialization");

        // Регистрируем обработчик пакета синхронизации
        ClientPlayNetworking.registerGlobalReceiver(StatsNetworking.SYNC_STATS_PACKET, (client, handler, buf, responseSender) -> {
            // Читаем пакет
            SyncStatsPayload payload = SyncStatsPayload.read(buf);

            // Обрабатываем на главном потоке клиента
            client.execute(() -> {
                Player player = client.player;
                if (player != null) {
                    PlayerStatsComponent component = PlayerStatsManager.getStats(player);

                    // Обновляем показатели из пакета
                    for (Map.Entry<VitalType, SyncStatsPayload.StatData> entry : payload.getStats().entrySet()) {
                        PlayerVital vital = component.getOrCreateVital(entry.getKey());
                        vital.setCurrent(entry.getValue().getCurrent());
                        vital.setMax(entry.getValue().getMax());
                    }
                }
            });
        });

        ExodusCore.LOGGER.info("Exodus Core - Client initialized successfully");
    }
}