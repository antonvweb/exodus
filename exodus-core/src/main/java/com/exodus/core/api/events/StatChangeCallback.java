package com.exodus.core.api.events;

import com.exodus.core.api.stats.StatType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;

/**
 * Событие изменения стата игрока
 * Другие моды могут подписаться на это событие
 */
public interface StatChangeCallback {

    Event<StatChangeCallback> EVENT = EventFactory.createArrayBacked(
            StatChangeCallback.class,
            (listeners) -> (player, type, oldValue, newValue) -> {
                for (StatChangeCallback listener : listeners) {
                    listener.onStatChange(player, type, oldValue, newValue);
                }
            }
    );

    /**
     * Вызывается когда стат игрока изменяется
     * @param player игрок
     * @param type тип стата
     * @param oldValue старое значение
     * @param newValue новое значение
     */
    void onStatChange(Player player, StatType type, float oldValue, float newValue);
}