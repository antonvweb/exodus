package com.exodus.core.player.attributes;

import com.exodus.core.api.attributes.AttributeModifier;
import com.exodus.core.api.attributes.AttributeType;

import java.util.*;

public class AttributeInstance {
    private final AttributeType type;
    private final Map<UUID, AttributeModifier> modifiers;
    private float cachedValue;
    private boolean dirty;

    public AttributeInstance (AttributeType type){
        this.type = type;
        this.modifiers = new HashMap<>();
        this.cachedValue = type.getBaseValue();
        this.dirty = false;
    }

    /**
     * Пересчитать финальное значение атрибута
     *
     * ПОРЯДОК ПРИМЕНЕНИЯ МОДИФИКАТОРОВ (как в Minecraft):
     * 1. ADD - просто складываем
     * 2. ADD_MULTIPLIED_BASE - процент от базового значения
     * 3. ADD_MULTIPLIED_TOTAL - процент от текущего значения
     *
     * ПРИМЕР:
     * Базовое: 150
     * ADD: +50, +30 → 150 + 50 + 30 = 230
     * ADD_MULTIPLIED_BASE: +20%, +10% → 230 + (150 × 0.2) + (150 × 0.1) = 230 + 30 + 15 = 275
     * ADD_MULTIPLIED_TOTAL: -30% → 275 × (1 - 0.3) = 192.5
     */
    private float calculateValue() {
        float baseValue = type.getBaseValue();  // Берём базу из enum

        // ШАГ 1: Собираем модификаторы по типам операций
        float addAmount = 0.0f;                 // Сумма ADD модификаторов
        float baseMultiplier = 0.0f;            // Сумма ADD_MULTIPLIED_BASE
        float totalMultiplier = 0.0f;           // Сумма ADD_MULTIPLIED_TOTAL

        for (AttributeModifier modifier : modifiers.values()) {
            switch (modifier.getOperation()) {
                case ADD:
                    addAmount += modifier.getAmount();
                    break;
                case ADD_MULTIPLIED_BASE:
                    baseMultiplier += modifier.getAmount();
                    break;
                case ADD_MULTIPLIED_TOTAL:
                    totalMultiplier += modifier.getAmount();
                    break;
            }
        }

        // ШАГ 2: Применяем модификаторы по порядку
        float result = baseValue;

        // 1. ADD
        result += addAmount;

        // 2. ADD_MULTIPLIED_BASE (процент от БАЗОВОГО значения)
        result += baseValue * baseMultiplier;

        // 3. ADD_MULTIPLIED_TOTAL (процент от ТЕКУЩЕГО значения)
        result *= (1.0f + totalMultiplier);

        // ШАГ 3: Ограничиваем диапазоном атрибута
        result = type.clamp(result);

        return result;
    }

    /**
     * Получить финальное значение атрибута
     * Использует кеширование для производительности
     */
    public float getValue() {
        if(this.dirty){
            cachedValue = calculateValue();
            dirty = false;
        }

        return cachedValue;
    }

    /**
     * Добавить модификатор
     * ВАЖНО: если модификатор с таким UUID уже есть - заменить его
     * После добавления пометить dirty = true
     */
    public void addModifier(AttributeModifier modifier) {
        modifiers.put(modifier.getId(), modifier);  // put() автоматически заменяет если ключ есть
        dirty = true;
    }

    /**
     * Удалить модификатор по UUID
     * После удаления пометить dirty = true
     */
    public void removeModifier(UUID modifierId) {
        if (modifiers.remove(modifierId) != null) {
            dirty = true;
        }
    }

    /**
     * Удалить ВСЕ модификаторы от определённого источника
     * Например: удалить все модификаторы от брони при её снятии
     *
     * @param source источник (например "armor", "buff_strength", "fracture")
     * @return количество удалённых модификаторов
     */
    public int removeModifiersBySource(String source) {
        int count = 0;

        // Используем итератор для безопасного удаления
        Iterator<AttributeModifier> iterator = modifiers.values().iterator();

        while (iterator.hasNext()) {
            AttributeModifier modifier = iterator.next();

            if (modifier.getSource().equals(source)) {
                iterator.remove();  // ✅ Безопасное удаление через итератор
                count++;
            }
        }

        // Ставим dirty только если что-то удалили
        if (count > 0) {
            dirty = true;
        }

        return count;
    }

    /**
     * Проверить есть ли модификатор с таким UUID
     */
    public boolean hasModifier(UUID modifierId) {
        return this.modifiers.containsKey(modifierId);
    }

    /**
     * Получить все модификаторы (копию, не оригинал!)
     */
    public Collection<AttributeModifier> getModifiers() {
        return new ArrayList<>(modifiers.values());
    }
}
