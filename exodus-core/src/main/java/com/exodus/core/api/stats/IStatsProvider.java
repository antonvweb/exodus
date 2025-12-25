package com.exodus.core.api.stats;

import net.minecraft.world.entity.player.Player;
import java.util.List;
import java.util.Optional;

/**
 * Провайдер статов для получения информации о показателях игрока
 * HUD использует этот интерфейс для отображения
 */
public interface IStatsProvider {

    /**
     * Получить все активные показатели игрока (для отображения)
     * @param player игрок
     * @return список показателей для отображения
     */
    List<IPlayerStat> getStats(Player player);

    /**
     * Получить конкретный показатель игрока
     * @param player игрок
     * @param type тип показателя
     * @return показатель или пусто если не найден
     */
    Optional<IPlayerStat> getVital(Player player, VitalType type);

    /**
     * Проверить есть ли показатель у игрока
     * @param player игрок
     * @param type тип показателя
     * @return true если показатель активен
     */
    default boolean hasVital(Player player, VitalType type) {
        return getVital(player, type).isPresent();
    }

    // ==================== ОБРАТНАЯ СОВМЕСТИМОСТЬ ====================

    /**
     * @deprecated Используйте getVital(Player, VitalType)
     */
    @Deprecated
    default Optional<IPlayerStat> getStat(Player player, StatType type) {
        // Конвертация StatType -> VitalType для обратной совместимости
        VitalType vitalType = convertStatTypeToVitalType(type);
        if (vitalType != null) {
            return getVital(player, vitalType);
        }
        return Optional.empty();
    }

    /**
     * @deprecated Используйте hasVital(Player, VitalType)
     */
    @Deprecated
    default boolean hasStat(Player player, StatType type) {
        VitalType vitalType = convertStatTypeToVitalType(type);
        return vitalType != null && hasVital(player, vitalType);
    }

    /**
     * Конвертация старого StatType в VitalType
     */
    private VitalType convertStatTypeToVitalType(StatType type) {
        return switch (type) {
            case HEALTH -> VitalType.HEALTH;
            case OXYGEN -> VitalType.OXYGEN;
            case THIRST -> VitalType.THIRST;
            case HUNGER -> VitalType.HUNGER;
            case STAMINA -> VitalType.ENERGY;
            default -> null;
        };
    }
}