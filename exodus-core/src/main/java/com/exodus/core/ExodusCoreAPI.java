package com.exodus.core;

import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.player.ExodusPlayerData;
import com.exodus.core.api.stats.*;
import com.exodus.core.player.ExodusPlayerComponent;
import com.exodus.core.player.ExodusPlayerManager;
import com.exodus.core.stats.AttributeEffects;
import com.exodus.core.stats.PlayerStatsComponent;
import com.exodus.core.stats.PlayerStatsManager;
import com.exodus.core.stats.StatsProvider;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.Optional;

/**
 * Главный API класс для Exodus Core
 * Другие моды используют этот класс для взаимодействия с системой статов
 */
public class ExodusCoreAPI {

    private static final IStatsProvider statsProvider = new StatsProvider();

    // ==================== ПРОВАЙДЕР ====================

    /**
     * Получить провайдер статов
     * Используется HUD для отображения
     */
    public static IStatsProvider getStatsProvider() {
        return statsProvider;
    }

    /**
     * Получить компонент статов игрока
     * Используется другими модами для изменения статов
     */
    public static PlayerStatsComponent getPlayerStats(Player player) {
        return PlayerStatsManager.getStats(player);
    }

    // ==================== АТРИБУТЫ ====================

    /**
     * Получить уровень атрибута игрока
     */
    public static int getAttribute(Player player, AttributeType type) {
        return getPlayerStats(player).getAttribute(type);
    }

    /**
     * Установить уровень атрибута
     */
    public static void setAttribute(Player player, AttributeType type, int level) {
        getPlayerStats(player).setAttribute(type, level);
    }

    /**
     * Увеличить атрибут на 1 уровень
     */
    public static void increaseAttribute(Player player, AttributeType type) {
        getPlayerStats(player).increaseAttribute(type);
    }

    /**
     * Получить все атрибуты игрока
     */
    public static Map<AttributeType, Integer> getAttributes(Player player) {
        return getPlayerStats(player).getAttributes();
    }

    // ==================== ПОКАЗАТЕЛИ ====================

    /**
     * Получить или создать конкретный показатель
     * @param player игрок
     * @param type тип показателя
     */
    public static IPlayerStat getOrCreateVital(Player player, VitalType type) {
        return getPlayerStats(player).getOrCreateVital(type);
    }

    /**
     * Получить показатель если он существует
     * @param player игрок
     * @param type тип показателя
     */
    public static Optional<IPlayerStat> getVital(Player player, VitalType type) {
        return statsProvider.getVital(player, type);
    }

    /**
     * Проверить есть ли показатель у игрока
     */
    public static boolean hasVital(Player player, VitalType type) {
        return statsProvider.hasVital(player, type);
    }

    // ==================== РАСЧЕТЫ ====================

    /**
     * Рассчитать максимальное значение показателя с учетом атрибутов
     */
    public static float calculateMaxVital(Player player, VitalType type) {
        Map<AttributeType, Integer> attributes = getAttributes(player);
        return AttributeEffects.calculateMaxVital(type, attributes);
    }

    /**
     * Рассчитать модификатор скорости уменьшения показателя
     */
    public static float calculateDecayModifier(Player player, VitalType type) {
        Map<AttributeType, Integer> attributes = getAttributes(player);
        return AttributeEffects.calculateDecayModifier(type, attributes);
    }

    /**
     * Рассчитать бонус к скорости передвижения
     */
    public static float calculateMovementSpeed(Player player) {
        Map<AttributeType, Integer> attributes = getAttributes(player);
        return AttributeEffects.calculateMovementSpeed(attributes);
    }

    /**
     * Рассчитать бонус к урону
     */
    public static float calculateDamageBonus(Player player) {
        Map<AttributeType, Integer> attributes = getAttributes(player);
        return AttributeEffects.calculateDamageBonus(attributes);
    }

    /**
     * Рассчитать шанс критического урона
     */
    public static float calculateCritChance(Player player) {
        Map<AttributeType, Integer> attributes = getAttributes(player);
        return AttributeEffects.calculateCritChance(attributes);
    }

    /**
     * Рассчитать максимальную грузоподъемность
     */
    public static float calculateMaxCarryWeight(Player player) {
        Map<AttributeType, Integer> attributes = getAttributes(player);
        return AttributeEffects.calculateMaxCarryWeight(attributes);
    }

    /**
     * Рассчитать бонус к качеству крафта
     */
    public static float calculateCraftQuality(Player player) {
        Map<AttributeType, Integer> attributes = getAttributes(player);
        return AttributeEffects.calculateCraftQuality(attributes);
    }

    /**
     * Рассчитать сопротивление стрессу
     */
    public static float calculateStressResistance(Player player) {
        Map<AttributeType, Integer> attributes = getAttributes(player);
        return AttributeEffects.calculateStressResistance(attributes);
    }

    // ==================== ОБРАТНАЯ СОВМЕСТИМОСТЬ ====================
    // Старые методы для StatType - помечены как @Deprecated

    /**
     * @deprecated Используйте getOrCreateVital(Player, VitalType)
     */
    @Deprecated
    public static IPlayerStat getOrCreateStat(Player player, StatType type, float defaultMax) {
        VitalType vitalType = convertStatTypeToVitalType(type);
        if (vitalType != null) {
            return getOrCreateVital(player, vitalType);
        }
        throw new IllegalArgumentException("Неизвестный тип стата: " + type);
    }

    /**
     * @deprecated Используйте getVital(Player, VitalType)
     */
    @Deprecated
    public static Optional<IPlayerStat> getStat(Player player, StatType type) {
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
    public static boolean hasStat(Player player, StatType type) {
        VitalType vitalType = convertStatTypeToVitalType(type);
        if (vitalType != null) {
            return hasVital(player, vitalType);
        }
        return false;
    }

    /**
     * Конвертировать старый StatType в новый VitalType
     */
    private static VitalType convertStatTypeToVitalType(StatType type) {
        return switch (type) {
            case HEALTH -> VitalType.HEALTH;
            case OXYGEN -> VitalType.OXYGEN;
            case THIRST -> VitalType.THIRST;
            case HUNGER -> VitalType.HUNGER;
            case STAMINA -> VitalType.ENERGY;
            default -> null;
        };
    }
    // ==================== ИГРОК ====================

    /**
     * Получить компонент расширенных данных игрока
     */
    public static ExodusPlayerComponent getPlayerComponent(Player player) {
        return ExodusPlayerManager.getComponent(player);
    }

    /**
     * Получить данные игрока
     */
    public static ExodusPlayerData getPlayerData(Player player) {
        return getPlayerComponent(player).getData();
    }

    // ==================== ЧАСТИ ТЕЛА ====================

    /**
     * Получить данные части тела
     */
    public static ExodusPlayerData.BodyPartData getBodyPart(Player player, BodyPart part) {
        return getPlayerComponent(player).getBodyPart(part);
    }

    /**
     * Нанести урон части тела
     */
    public static void damageBodyPart(Player player, BodyPart part, float damage) {
        getPlayerComponent(player).damageBodyPart(part, damage);
    }

    /**
     * Вылечить часть тела
     */
    public static void healBodyPart(Player player, BodyPart part, float amount) {
        getPlayerComponent(player).healBodyPart(part, amount);
    }

    /**
     * Получить суммарное HP всех частей тела
     */
    public static float getTotalHP(Player player) {
        return getPlayerData(player).getTotalHP();
    }

    /**
     * Получить суммарное максимальное HP
     */
    public static float getTotalMaxHP(Player player) {
        return getPlayerData(player).getTotalMaxHP();
    }

    // ==================== ФИЗИОЛОГИЯ ====================

    /**
     * Получить уровень крови (0-100%)
     */
    public static float getBloodLevel(Player player) {
        return getPlayerData(player).getBloodLevel();
    }

    /**
     * Установить уровень крови
     */
    public static void setBloodLevel(Player player, float level) {
        getPlayerData(player).setBloodLevel(level);
    }

    /**
     * Потерять кровь
     */
    public static void loseBlood(Player player, float amount) {
        getPlayerData(player).loseBlood(amount);
    }

    /**
     * Восстановить кровь
     */
    public static void restoreBlood(Player player, float amount) {
        getPlayerData(player).restoreBlood(amount);
    }

    /**
     * Получить температуру тела
     */
    public static float getBodyTemperature(Player player) {
        return getPlayerData(player).getBodyTemperature();
    }

    /**
     * Установить температуру тела
     */
    public static void setBodyTemperature(Player player, float temp) {
        getPlayerData(player).setBodyTemperature(temp);
    }
}