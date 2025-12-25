package com.exodus.core.network;

import com.exodus.core.ExodusCore;
import com.exodus.core.api.stats.VitalType;
import com.exodus.core.stats.PlayerVital;
import com.exodus.core.stats.PlayerStatsComponent;
import com.exodus.core.stats.PlayerStatsManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import io.netty.buffer.Unpooled;

import java.util.EnumMap;
import java.util.Map;

/**
 * Класс для настройки и управления сетевой синхронизацией статов
 */
public class StatsNetworking {

    public static final ResourceLocation SYNC_STATS_PACKET = new ResourceLocation(ExodusCore.MOD_ID, "sync_stats");

    /**
     * Регистрация пакетов (вызывается при инициализации мода)
     */
    public static void registerPackets() {
        // В 1.20.1 регистрация происходит на клиенте
        // См. ExodusCoreClient
    }

    /**
     * Отправить статы игрока на клиент
     */
    public static void syncStatsToClient(ServerPlayer player) {
        PlayerStatsComponent component = PlayerStatsManager.getStats(player);

        // Собираем данные всех показателей (vitals)
        Map<VitalType, SyncStatsPayload.StatData> statsData = new EnumMap<>(VitalType.class);
        for (PlayerVital vital : component.getAllVitals()) {
            statsData.put(vital.getVitalType(), new SyncStatsPayload.StatData(vital.getCurrent(), vital.getMax()));
        }

        // Создаем пакет
        SyncStatsPayload payload = new SyncStatsPayload(statsData);

        // Создаем буфер и записываем данные
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buf);

        // Отправляем пакет
        ServerPlayNetworking.send(player, SYNC_STATS_PACKET, buf);
    }
}