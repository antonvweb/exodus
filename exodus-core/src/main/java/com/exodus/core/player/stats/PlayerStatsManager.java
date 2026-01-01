package com.exodus.core.player.stats;

import com.exodus.core.api.player.StatType;
import com.exodus.core.player.attributes.AttributeManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер статов игроков
 *
 * ОТВЕТСТВЕННОСТЬ:
 * - Хранит компоненты статов всех игроков
 * - Предоставляет API для получения/изменения статов
 * - Вызывает AttributeManager.recalculate() при изменении статов
 */
public class PlayerStatsManager {

    private static final Map<UUID, PlayerStatsComponent> components = new HashMap<>();

    // ==================== ПОЛУЧЕНИЕ КОМПОНЕНТА ====================

    /**
     * Получить компонент статов игрока (создаёт если не существует)
     */
    public static PlayerStatsComponent getComponent(Player player) {
        return components.computeIfAbsent(
                player.getUUID(),
                uuid -> new PlayerStatsComponent(player)
        );
    }

    /**
     * Удалить компонент игрока
     */
    public static void removeComponent(UUID uuid) {
        components.remove(uuid);
    }

    // ==================== API СТАТОВ ====================

    /**
     * Получить значение стата
     */
    public static int getStat(Player player, StatType stat) {
        return getComponent(player).getData().getStat(stat);
    }

    /**
     * Установить значение стата
     * ВАЖНО: Вызывает AttributeManager.recalculate()!
     */
    public static void setStat(Player player, StatType stat, int value) {
        getComponent(player).getData().setStat(stat, value);

        // ✅ Пересчитываем атрибуты после изменения стата!
        AttributeManager.recalculate(player);
    }

    /**
     * Увеличить стат на 1 (если есть свободные очки)
     * ВАЖНО: Вызывает AttributeManager.recalculate()!
     */
    public static boolean increaseStat(Player player, StatType stat) {
        boolean increased = getComponent(player).getData().increaseStat(stat);

        if (increased) {
            // ✅ Пересчитываем атрибуты после увеличения!
            AttributeManager.recalculate(player);
        }

        return increased;
    }

    // ==================== API ПРОГРЕССИИ ====================

    /**
     * Получить уровень
     */
    public static int getLevel(Player player) {
        return getComponent(player).getData().getLevel();
    }

    /**
     * Получить опыт
     */
    public static float getExperience(Player player) {
        return getComponent(player).getData().getExperience();
    }

    /**
     * Добавить опыт (с учётом бонуса от INT через атрибуты)
     */
    public static void addExperience(Player player, float amount) {
        // ✅ Бонус от INT уже учтён в AttributeManager!
        // Можно использовать AttributeManager.getValue(player, EXPERIENCE_GAIN)
        // для дополнительного бонуса

        getComponent(player).getData().addExperience(amount);

        // Если игрок получил уровень - пересчитываем атрибуты
        // (в addExperience() может произойти levelUp())
    }

    /**
     * Получить свободные очки
     */
    public static int getFreePoints(Player player) {
        return getComponent(player).getData().getFreePoints();
    }

    // ==================== РЕГИСТРАЦИЯ СОБЫТИЙ ====================

    /**
     * Зарегистрировать события
     * Вызывается при инициализации мода
     */
    public static void registerEvents() {
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->{
            AttributeManager.recalculate(newPlayer);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->{
            AttributeManager.recalculate(handler.getPlayer());
        });
    }
}