package com.exodus.core.network;

import com.exodus.core.api.stats.VitalType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.EnumMap;
import java.util.Map;

/**
 * Пакет для синхронизации показателей между сервером и клиентом
 */
public class SyncStatsPayload {

    private final Map<VitalType, StatData> stats;

    public SyncStatsPayload(Map<VitalType, StatData> stats) {
        this.stats = stats;
    }

    public Map<VitalType, StatData> getStats() {
        return stats;
    }

    /**
     * Данные одного показателя
     */
    public static class StatData {
        private final float current;
        private final float max;

        public StatData(float current, float max) {
            this.current = current;
            this.max = max;
        }

        public float getCurrent() {
            return current;
        }

        public float getMax() {
            return max;
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeFloat(current);
            buf.writeFloat(max);
        }

        public static StatData read(FriendlyByteBuf buf) {
            return new StatData(buf.readFloat(), buf.readFloat());
        }
    }

    /**
     * Записать пакет в буфер
     */
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(stats.size());
        for (Map.Entry<VitalType, StatData> entry : stats.entrySet()) {
            buf.writeUtf(entry.getKey().getId());
            entry.getValue().write(buf);
        }
    }

    /**
     * Прочитать пакет из буфера
     */
    public static SyncStatsPayload read(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<VitalType, StatData> stats = new EnumMap<>(VitalType.class);

        for (int i = 0; i < size; i++) {
            String typeId = buf.readUtf();
            VitalType type = VitalType.fromId(typeId);
            StatData data = StatData.read(buf);

            if (type != null) {
                stats.put(type, data);
            }
        }

        return new SyncStatsPayload(stats);
    }
}