package com.exodus.core.stats;

import com.exodus.core.api.stats.AttributeType;
import com.exodus.core.api.stats.IPlayerStat;
import com.exodus.core.api.stats.VitalType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Компонент для хранения ВСЕХ статов игрока:
 * - Атрибуты (прокачиваемые характеристики)
 * - Показатели (ресурсы выживания)
 */
public class PlayerStatsComponent {

    private final Player player;

    // Атрибуты - прокачиваемые характеристики
    private final Map<AttributeType, Integer> attributes = new EnumMap<>(AttributeType.class);

    // Показатели выживания
    private final Map<VitalType, PlayerVital> vitals = new EnumMap<>(VitalType.class);

    public PlayerStatsComponent(Player player) {
        this.player = player;
        // Инициализируем атрибуты начальными значениями
        for (AttributeType type : AttributeType.values()) {
            attributes.put(type, 1); // Все начинаются с уровня 1
        }
    }

    // ==================== АТРИБУТЫ ====================

    /**
     * Получить уровень атрибута
     */
    public int getAttribute(AttributeType type) {
        return attributes.getOrDefault(type, 1);
    }

    /**
     * Установить уровень атрибута
     */
    public void setAttribute(AttributeType type, int level) {
        int oldLevel = attributes.getOrDefault(type, 1);
        attributes.put(type, Math.max(1, level)); // Минимум 1

        // Если атрибут изменился - пересчитываем максимумы показателей
        if (oldLevel != level) {
            recalculateVitalsMax();
        }
    }

    /**
     * Увеличить атрибут на 1
     */
    public void increaseAttribute(AttributeType type) {
        setAttribute(type, getAttribute(type) + 1);
    }

    /**
     * Получить все атрибуты
     */
    public Map<AttributeType, Integer> getAttributes() {
        return new EnumMap<>(attributes);
    }

    // ==================== ПОКАЗАТЕЛИ ====================

    /**
     * Получить показатель
     */
    public Optional<PlayerVital> getVital(VitalType type) {
        return Optional.ofNullable(vitals.get(type));
    }

    /**
     * Получить или создать показатель
     */
    public PlayerVital getOrCreateVital(VitalType type) {
        return vitals.computeIfAbsent(type, t -> {
            // Рассчитываем максимум с учетом атрибутов
            float maxValue = AttributeEffects.calculateMaxVital(t, attributes);
            return new PlayerVital(t, player, maxValue);
        });
    }

    /**
     * Установить показатель
     */
    public void setVital(VitalType type, PlayerVital vital) {
        vitals.put(type, vital);
    }

    /**
     * Получить все показатели
     */
    public Collection<PlayerVital> getAllVitals() {
        return vitals.values();
    }

    /**
     * Проверить есть ли показатель
     */
    public boolean hasVital(VitalType type) {
        return vitals.containsKey(type);
    }

    /**
     * Удалить показатель
     */
    public void removeVital(VitalType type) {
        vitals.remove(type);
    }

    /**
     * Пересчитать максимумы всех показателей на основе текущих атрибутов
     */
    public void recalculateVitalsMax() {
        for (Map.Entry<VitalType, PlayerVital> entry : vitals.entrySet()) {
            VitalType type = entry.getKey();
            PlayerVital vital = entry.getValue();

            float newMax = AttributeEffects.calculateMaxVital(type, attributes);
            vital.setMax(newMax);
        }
    }

    // ==================== NBT ====================

    /**
     * Сохранить в NBT
     */
    public CompoundTag writeNbt(CompoundTag nbt) {
        // Сохраняем атрибуты
        CompoundTag attributesNbt = new CompoundTag();
        for (Map.Entry<AttributeType, Integer> entry : attributes.entrySet()) {
            attributesNbt.putInt(entry.getKey().getId(), entry.getValue());
        }
        nbt.put("exodus_attributes", attributesNbt);

        // Сохраняем показатели
        CompoundTag vitalsNbt = new CompoundTag();
        for (Map.Entry<VitalType, PlayerVital> entry : vitals.entrySet()) {
            CompoundTag vitalNbt = new CompoundTag();
            entry.getValue().writeNbt(vitalNbt);
            vitalsNbt.put(entry.getKey().getId(), vitalNbt);
        }
        nbt.put("exodus_vitals", vitalsNbt);

        return nbt;
    }

    /**
     * Загрузить из NBT
     */
    public void readNbt(CompoundTag nbt) {
        // Загружаем атрибуты
        if (nbt.contains("exodus_attributes")) {
            CompoundTag attributesNbt = nbt.getCompound("exodus_attributes");
            for (AttributeType type : AttributeType.values()) {
                if (attributesNbt.contains(type.getId())) {
                    attributes.put(type, attributesNbt.getInt(type.getId()));
                }
            }
        }

        // Загружаем показатели
        if (nbt.contains("exodus_vitals")) {
            CompoundTag vitalsNbt = nbt.getCompound("exodus_vitals");
            for (String key : vitalsNbt.getAllKeys()) {
                VitalType type = VitalType.fromId(key);
                if (type != null) {
                    CompoundTag vitalNbt = vitalsNbt.getCompound(key);
                    PlayerVital vital = getOrCreateVital(type);
                    vital.readNbt(vitalNbt);
                }
            }
        }

        // Пересчитываем максимумы на основе загруженных атрибутов
        recalculateVitalsMax();
    }
}