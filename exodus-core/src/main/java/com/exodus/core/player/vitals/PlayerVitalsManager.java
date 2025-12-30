package com.exodus.core.player.vitals;

import com.exodus.core.player.attributes.AttributeManager;
import com.exodus.core.api.attributes.AttributeType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер витальных показателей игроков
 *
 * ОТВЕТСТВЕННОСТЬ:
 * - Хранит компоненты виталов всех игроков
 * - Обновляет максимумы на основе атрибутов
 * - Тикает расход/восстановление виталов
 */
public class PlayerVitalsManager {

    private static final Map<UUID, PlayerVitalsComponent> components = new HashMap<>();

    private static int tickCounter = 0;

    // ==================== ПОЛУЧЕНИЕ КОМПОНЕНТА ====================

    /**
     * Получить компонент виталов игрока (создаёт если не существует)
     */
    public static PlayerVitalsComponent getComponent(Player player) {
        return components.computeIfAbsent(player.getUUID(), uuid -> {
            PlayerVitalsComponent vitals = new PlayerVitalsComponent(player);

            // ✅ Обновляем максимумы из AttributeManager
            updateMaxValues(player);

            return vitals;
        });
    }

    /**
     * Удалить компонент игрока
     */
    public static void removeComponent(UUID uuid) {
        components.remove(uuid);
    }

    // ==================== ОБНОВЛЕНИЕ МАКСИМУМОВ ====================

    /**
     * Обновить максимальные значения виталов из AttributeManager
     *
     * КОГДА ВЫЗЫВАТЬ:
     * - При изменении статов (автоматически из AttributeManager.recalculate)
     * - При надевании/снятии брони/экипировки
     */
    public static void updateMaxValues(Player player) {
        PlayerVitalsComponent vitals = getComponent(player);

        // ✅ Получаем максимумы из AttributeManager
        float maxStamina = AttributeManager.getValue(player, AttributeType.MAX_STAMINA);
        float maxOxygen = AttributeManager.getValue(player, AttributeType.MAX_OXYGEN);

        // Применяем
        vitals.getData().setMaxEnergy(maxStamina);
        vitals.getData().setMaxOxygen(maxOxygen);
    }

    // ==================== API ВИТАЛОВ ====================

    // Сытость
    public static float getHunger(Player player) {
        return getComponent(player).getData().getHunger();
    }

    public static void setHunger(Player player, float value) {
        getComponent(player).getData().setHunger(value);
    }

    public static void addHunger(Player player, float amount) {
        getComponent(player).getData().addHunger(amount);
    }

    // Жажда
    public static float getThirst(Player player) {
        return getComponent(player).getData().getThirst();
    }

    public static void setThirst(Player player, float value) {
        getComponent(player).getData().setThirst(value);
    }

    public static void addThirst(Player player, float amount) {
        getComponent(player).getData().addThirst(amount);
    }

    // Энергия/Стамина
    public static float getEnergy(Player player) {
        return getComponent(player).getData().getEnergy();
    }

    public static void setEnergy(Player player, float value) {
        getComponent(player).getData().setEnergy(value);
    }

    public static void addEnergy(Player player, float amount) {
        getComponent(player).getData().addEnergy(amount);
    }

    public static float getMaxEnergy(Player player) {
        return getComponent(player).getData().getMaxEnergy();
    }

    public static boolean canPerformAction(Player player, float cost) {
        return getComponent(player).getData().canPerformAction(cost);
    }

    public static boolean consumeEnergy(Player player, float cost) {
        return getComponent(player).getData().consumeEnergy(cost);
    }

    // Кислород
    public static float getOxygen(Player player) {
        return getComponent(player).getData().getOxygen();
    }

    public static void setOxygen(Player player, float value) {
        getComponent(player).getData().setOxygen(value);
    }

    public static void addOxygen(Player player, float amount) {
        getComponent(player).getData().addOxygen(amount);
    }

    public static float getMaxOxygen(Player player) {
        return getComponent(player).getData().getMaxOxygen();
    }

    // Температура
    public static float getTemperature(Player player) {
        return getComponent(player).getData().getTemperature();
    }

    public static void setTemperature(Player player, float value) {
        getComponent(player).getData().setTemperature(value);
    }

    public static void addTemperature(Player player, float amount) {
        getComponent(player).getData().addTemperature(amount);
    }

    // Психика
    public static float getMental(Player player) {
        return getComponent(player).getData().getMental();
    }

    public static void setMental(Player player, float value) {
        getComponent(player).getData().setMental(value);
    }

    public static void addMental(Player player, float amount) {
        getComponent(player).getData().addMental(amount);
    }

    // ==================== ТИКИНГ ВИТАЛОВ ====================

    /**
     * Зарегистрировать события
     */
    public static void registerEvents() {
        // Тикаем виталы каждую секунду (20 тиков)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;

            if (tickCounter % 20 == 0) {  // Каждую секунду
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    tickVitals(player);
                }
            }
        });
    }

    /**
     * Обновление виталов для одного игрока
     */
    private static void tickVitals(ServerPlayer player) {
        // TODO: Реализовать расход/восстановление виталов
        // - Расход сытости (с учётом AttributeManager.getValue(HUNGER_DRAIN_RATE))
        // - Расход жажды
        // - Восстановление стамины
        // - Изменение температуры
        // - Изменение психики

        // Это большая система, сделаем позже!
    }
}