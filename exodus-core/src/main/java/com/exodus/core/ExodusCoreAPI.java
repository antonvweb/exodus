package com.exodus.core;

import com.exodus.core.api.player.*;
import com.exodus.core.api.player.health.BleedingType;
import com.exodus.core.api.player.health.BodyPart;
import com.exodus.core.api.player.health.PlayerHealthData;
import com.exodus.core.api.player.inventory.InventorySlot;
import com.exodus.core.api.player.inventory.PlayerInventoryData;
import com.exodus.core.player.health.PlayerHealthComponent;
import com.exodus.core.player.health.PlayerHealthManager;
import com.exodus.core.player.inventory.PlayerInventoryComponent;
import com.exodus.core.player.inventory.PlayerInventoryManager;
import com.exodus.core.player.stats.PlayerStatsManager;
import com.exodus.core.player.vitals.PlayerVitalsManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Главный API класс для Exodus Core
 * Управляет: здоровьем (6 частей тела), статами (7 статов), витальными показателями (6 виталов)
 */
public class ExodusCoreAPI {

    // ==================== КОМПОНЕНТ ЗДОРОВЬЯ ====================

    /**
     * Получить компонент здоровья игрока
     */
    public static PlayerHealthComponent getHealthComponent(Player player) {
        return PlayerHealthManager.getComponent(player);
    }

    /**
     * Получить данные здоровья игрока
     */
    public static PlayerHealthData getHealthData(Player player) {
        return getHealthComponent(player).getData();
    }

    // ==================== HP ЧАСТЕЙ ТЕЛА ====================

    /**
     * Получить HP части тела
     */
    public static float getBodyPartHP(Player player, BodyPart part) {
        return getHealthData(player).getBodyPartHP(part);
    }

    /**
     * Установить HP части тела
     */
    public static void setBodyPartHP(Player player, BodyPart part, float hp) {
        getHealthData(player).setBodyPartHP(part, hp, player);
    }

    /**
     * Нанести урон части тела
     */
    public static void damageBodyPart(Player player, BodyPart part, float damage) {
        getHealthData(player).damageBodyPart(part, damage, player);
    }

    /**
     * Восстановить HP части тела
     */
    public static void healBodyPart(Player player, BodyPart part, float amount) {
        getHealthData(player).healBodyPart(part, amount, player);
    }

    /**
     * Получить процент HP части тела
     */
    public static float getMaxBodyPartHP(Player player, BodyPart part) {
        return getHealthData(player).getMaxBodyPartHP(part, player);
    }

    public static float getBodyPartHPPercentage(Player player, BodyPart part) {
        return getHealthData(player).getBodyPartHPPercentage(part, player); // ✅
    }

    public static BodyPart.BodyPartState getBodyPartState(Player player, BodyPart part) {
        return getHealthData(player).getBodyPartState(part, player); // ✅
    }



    /**
     * Жив ли игрок
     */
    public static boolean isAlive(Player player) {
        return getHealthData(player).isAlive();
    }

    // ==================== КРОВОТЕЧЕНИЕ ====================

    /**
     * Добавить кровотечение на часть тела
     */
    public static void addBleeding(Player player, BodyPart part, BleedingType type) {
        getHealthData(player).addBleeding(part, type);
    }

    /**
     * Убрать кровотечение с части тела
     */
    public static void removeBleeding(Player player, BodyPart part) {
        getHealthData(player).removeBleeding(part, player);
    }

    /**
     * Проверить есть ли кровотечение на части тела
     */
    public static boolean hasBleeding(Player player, BodyPart part) {
        return getHealthData(player).hasBleeding(part);
    }

    /**
     * Получить тип кровотечения
     */
    public static BleedingType getBleedingType(Player player, BodyPart part) {
        return getHealthData(player).getBleedingType(part);
    }

    // ==================== ПЕРЕЛОМ ====================

    /**
     * Добавить перелом на часть тела (БЕСКОНЕЧНЫЙ)
     */
    public static void addFracture(Player player, BodyPart part, float intensity) {
        getHealthData(player).addFracture(part, intensity);
    }

    /**
     * Убрать перелом с части тела
     */
    public static void removeFracture(Player player, BodyPart part) {
        getHealthData(player).removeFracture(part, player);
    }

    /**
     * Проверить есть ли перелом на части тела
     */
    public static boolean hasFracture(Player player, BodyPart part) {
        return getHealthData(player).hasFracture(part);
    }

    /**
     * Получить интенсивность перелома
     */
    public static float getFractureIntensity(Player player, BodyPart part) {
        return getHealthData(player).getFractureIntensity(part);
    }

    // ==================== БОЛЬ (ГЛОБАЛЬНАЯ) ====================

    /**
     * Добавить боль
     */
    public static void addPain(Player player, int duration, float intensity) {
        getHealthData(player).addPain(duration, intensity);
    }

    /**
     * Убрать боль
     */
    public static void removePain(Player player) {
        getHealthData(player).removePain();
    }

    /**
     * Проверить есть ли боль
     */
    public static boolean hasPain(Player player) {
        return getHealthData(player).hasPain();
    }

    /**
     * Получить интенсивность боли
     */
    public static float getPainIntensity(Player player) {
        return getHealthData(player).getPainIntensity();
    }

    // ==================== КОМПОНЕНТ СТАТОВ ====================

    /**
     * Получить данные статов игрока
     */
    public static int getStat(Player player, StatType stat) {
        return PlayerStatsManager.getStat(player, stat);
    }

    /**
     * Установить значение стата
     */
    public static void setStat(Player player, StatType stat, int value) {
        PlayerStatsManager.setStat(player, stat, value);
    }

    /**
     * Увеличить стат на 1
     */
    public static boolean increaseStat(Player player, StatType stat) {
        return PlayerStatsManager.increaseStat(player, stat);
    }

    // ==================== УРОВЕНЬ И ОПЫТ ====================

    /**
     * Получить уровень игрока
     */
    public static int getLevel(Player player) {
        return PlayerStatsManager.getLevel(player);
    }

    /**
     * Получить текущий опыт
     */
    public static float getExperience(Player player) {
        return PlayerStatsManager.getExperience(player);
    }

    /**
     * Добавить опыт (с учётом бонуса от интеллекта)
     */
    public static void addExperience(Player player, float amount) {
        PlayerStatsManager.addExperience(player, amount);
    }

    /**
     * Получить свободные очки статов
     */
    public static int getFreePoints(Player player) {
        return PlayerStatsManager.getFreePoints(player);
    }

    /**
     * Получить сытость
     */
    public static float getHunger(Player player) {
        return PlayerVitalsManager.getHunger(player);
    }

    /**
     * Установить сытость
     */
    public static void setHunger(Player player, float value) {
        PlayerVitalsManager.setHunger(player, value);
    }

    /**
     * Добавить/убавить сытость
     */
    public static void addHunger(Player player, float amount) {
        PlayerVitalsManager.addHunger(player, amount);
    }

    // ==================== ЖАЖДА ====================

    /**
     * Получить жажду
     */
    public static float getThirst(Player player) {
        return PlayerVitalsManager.getThirst(player);
    }

    /**
     * Установить жажду
     */
    public static void setThirst(Player player, float value) {
        PlayerVitalsManager.setThirst(player, value);
    }

    /**
     * Добавить/убавить жажду
     */
    public static void addThirst(Player player, float amount) {
        PlayerVitalsManager.addThirst(player, amount);
    }

    // ==================== ЭНЕРГИЯ/СТАМИНА ====================

    /**
     * Получить энергию
     */
    public static float getEnergy(Player player) {
        return PlayerVitalsManager.getEnergy(player);
    }

    /**
     * Установить энергию
     */
    public static void setEnergy(Player player, float value) {
        PlayerVitalsManager.setEnergy(player, value);
    }

    /**
     * Добавить/убавить энергию
     */
    public static void addEnergy(Player player, float amount) {
        PlayerVitalsManager.addEnergy(player, amount);
    }

    /**
     * Получить максимальную энергию
     */
    public static float getMaxEnergy(Player player) {
        return PlayerVitalsManager.getMaxEnergy(player);
    }

    /**
     * Можно ли выполнить действие (хватает ли энергии)
     */
    public static boolean canPerformAction(Player player, float cost) {
        return PlayerVitalsManager.canPerformAction(player, cost);
    }

    /**
     * Потратить энергию на действие
     */
    public static boolean consumeEnergy(Player player, float cost) {
        return PlayerVitalsManager.consumeEnergy(player, cost);
    }

    // ==================== КИСЛОРОД ====================

    /**
     * Получить кислород
     */
    public static float getOxygen(Player player) {
        return PlayerVitalsManager.getOxygen(player);
    }

    /**
     * Установить кислород
     */
    public static void setOxygen(Player player, float value) {
        PlayerVitalsManager.setOxygen(player, value);
    }

    /**
     * Добавить/убавить кислород
     */
    public static void addOxygen(Player player, float amount) {
        PlayerVitalsManager.addOxygen(player ,amount);
    }

    /**
     * Получить максимальный кислород
     */
    public static float getMaxOxygen(Player player) {
        return PlayerVitalsManager.getMaxOxygen(player);
    }

    // ==================== ТЕМПЕРАТУРА ====================

    /**
     * Получить температуру тела
     */
    public static float getTemperature(Player player) {
        return PlayerVitalsManager.getTemperature(player);
    }

    /**
     * Установить температуру тела
     */
    public static void setTemperature(Player player, float value) {
        PlayerVitalsManager.setTemperature(player, value);
    }

    /**
     * Добавить/убавить температуру
     */
    public static void addTemperature(Player player, float amount) {
        PlayerVitalsManager.addTemperature(player, amount);
    }

    // ==================== ПСИХИКА ====================

    /**
     * Получить психику/рассудок
     */
    public static float getMental(Player player) {
        return PlayerVitalsManager.getMental(player);
    }

    /**
     * Установить психику
     */
    public static void setMental(Player player, float value) {
        PlayerVitalsManager.setMental(player, value);
    }

    /**
     * Добавить/убавить психику
     */
    public static void addMental(Player player, float amount) {
        PlayerVitalsManager.addMental(player, amount);
    }

    // ==================== КАСТОМНЫЙ ИНВЕНТАРЬ ====================

    /**
     * Получить компонент кастомного инвентаря игрока
     */
    public static PlayerInventoryComponent getInventoryComponent(Player player) {
        return PlayerInventoryManager.getComponent(player);
    }

    /**
     * Получить данные кастомного инвентаря игрока
     */
    public static PlayerInventoryData getInventoryData(Player player) {
        return getInventoryComponent(player).getData();
    }

    /**
     * Добавить предмет в кастомный инвентарь игрока
     * @return true если предмет полностью добавлен, false если инвентарь полон
     */
    public static boolean addItemToInventory(Player player, ItemStack stack) {
        return getInventoryData(player).addItem(stack);
    }

    /**
     * Очистить весь кастомный инвентарь игрока
     */
    public static void clearInventory(Player player) {
        getInventoryData(player).clear();
    }

    /**
     * Получить размер кастомного инвентаря (всегда 16)
     */
    public static int getInventorySize(Player player) {
        return PlayerInventoryData.INVENTORY_SIZE;
    }

    /**
     * Получить слот по индексу (0-15)
     */
    public static InventorySlot getInventorySlot(Player player, int index) {
        if (index < 0 || index >= PlayerInventoryData.INVENTORY_SIZE) {
            return null;
        }
        return getInventoryData(player).slots.get(index);
    }

    /**
     * Получить ItemStack из слота (может быть ItemStack.EMPTY)
     */
    public static ItemStack getItemInSlot(Player player, int index) {
        InventorySlot slot = getInventorySlot(player, index);
        return slot != null ? slot.getItemStack() : ItemStack.EMPTY;
    }

    /**
     * Установить предмет в конкретный слот (перезапись)
     */
    public static void setItemInSlot(Player player, int index, ItemStack stack) {
        InventorySlot slot = getInventorySlot(player, index);
        if (slot != null) {
            if (stack.isEmpty()) {
                slot.clear();
            } else {
                ItemStack copy = stack.copy();
                copy.setCount(Math.min(copy.getCount(), copy.getMaxStackSize()));
                slot.setItemStack(copy);
            }
        }
    }

    /**
     * Проверить, пустой ли слот
     */
    public static boolean isSlotEmpty(Player player, int index) {
        InventorySlot slot = getInventorySlot(player, index);
        return slot == null || slot.isEmpty();
    }
}
