package com.exodus.core.stats;

import com.exodus.core.api.stats.IPlayerStat;
import com.exodus.core.api.stats.IStatsProvider;
import com.exodus.core.api.stats.VitalType;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Базовая реализация провайдера статов
 * HUD будет использовать этот класс для получения данных
 */
public class StatsProvider implements IStatsProvider {

    @Override
    public List<IPlayerStat> getStats(Player player) {
        PlayerStatsComponent component = PlayerStatsManager.getStats(player);
        return component.getAllVitals()
                .stream()
                .map(vital -> (IPlayerStat) vital)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<IPlayerStat> getVital(Player player, VitalType type) {
        PlayerStatsComponent component = PlayerStatsManager.getStats(player);
        return component.getVital(type).map(vital -> (IPlayerStat) vital);
    }

    @Override
    public boolean hasVital(Player player, VitalType type) {
        PlayerStatsComponent component = PlayerStatsManager.getStats(player);
        return component.hasVital(type);
    }
}