package com.exodus.core;

import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.player.ExodusPlayerData;
import com.exodus.core.player.ExodusPlayerComponent;
import com.exodus.core.player.ExodusPlayerManager;
import net.minecraft.world.entity.player.Player;

/**
 * Главный API класс для Exodus Core
 * Другие моды используют этот класс для взаимодействия с системой статов
 */
public class ExodusCoreAPI {
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
}