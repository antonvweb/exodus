package com.exodus.core.api.events;

import com.exodus.core.api.stats.VitalType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;

/**
 * Событие изменения показателя выживания игрока
 * Другие моды могут подписаться на это событие
 */
public interface VitalChangeCallback {

    Event<VitalChangeCallback> EVENT = EventFactory.createArrayBacked(
            VitalChangeCallback.class,
            (listeners) -> (player, type, oldValue, newValue) -> {
                for (VitalChangeCallback listener : listeners) {
                    listener.onVitalChange(player, type, oldValue, newValue);
                }
            }
    );

    /**
     * Вызывается когда показатель игрока изменяется
     * @param player игрок
     * @param type тип показателя
     * @param oldValue старое значение
     * @param newValue новое значение
     */
    void onVitalChange(Player player, VitalType type, float oldValue, float newValue);
}