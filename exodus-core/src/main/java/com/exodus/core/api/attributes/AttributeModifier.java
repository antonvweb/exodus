package com.exodus.core.api.attributes;

import java.util.Objects;
import java.util.UUID;

/**
 * Модификатор атрибута - изменяет значение атрибута
 *
 * ЗАЧЕМ: Позволяет добавлять бонусы/штрафы от разных источников
 * ПРИМЕРЫ:
 * - Броня даёт +20% защиты
 * - Перелом ноги даёт -30% скорости
 * - Баф даёт +50 макс стамины
 *
 * КАК РАБОТАЕТ:
 * 1. Применяются ADD модификаторы (сложение)
 * 2. Применяются ADD_MULTIPLIED_BASE модификаторы (процент от базы)
 * 3. Применяются ADD_MULTIPLIED_TOTAL модификаторы (процент от текущего)
 */
public class AttributeModifier {

    /**
     * Операция модификатора
     */
    public enum Operation {
        /**
         * Прибавить значение: result = base + modifier
         * Пример: +50 к макс стамине
         */
        ADD(0),

        /**
         * Прибавить процент от базового значения: result = base * (1 + modifier)
         * Пример: +20% от базовой стамины (150 * 1.2 = 180)
         *
         * ВАЖНО: Это процент от БАЗОВОГО значения, не текущего!
         */
        ADD_MULTIPLIED_BASE(1),

        /**
         * Прибавить процент от текущего значения: result = current * (1 + modifier)
         * Пример: -50% от текущей скорости (после всех других модификаторов)
         *
         * ВАЖНО: Применяется ПОСЛЕДНИМ, влияет на уже рассчитанное значение!
         */
        ADD_MULTIPLIED_TOTAL(2);

        private final int priority;

        Operation(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    // ==================== ПОЛЯ ====================

    private final UUID id;              // Уникальный ID модификатора
    private final String name;          // Название (для отладки и UI)
    private final float amount;         // Значение модификатора
    private final Operation operation;  // Тип операции
    private final String source;        // Источник (для группировки и удаления)

    // ==================== КОНСТРУКТОР ====================

    /**
     * Создать модификатор с автоматическим UUID
     *
     * @param name Название модификатора (например "strength_bonus")
     * @param amount Значение (например 0.2 для +20%)
     * @param operation Тип операции
     */
    public AttributeModifier(String name, float amount, Operation operation) {
        this(UUID.randomUUID(), name, amount, operation, "unknown");
    }

    /**
     * Создать модификатор с указанным источником
     *
     * @param name Название
     * @param amount Значение
     * @param operation Операция
     * @param source Источник (например "armor", "buff", "fracture")
     */
    public AttributeModifier(String name, float amount, Operation operation, String source) {
        this(UUID.randomUUID(), name, amount, operation, source);
    }

    /**
     * Создать модификатор с конкретным UUID (для сохранения/загрузки)
     */
    public AttributeModifier(UUID id, String name, float amount, Operation operation, String source) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.operation = operation;
        this.source = source;
    }

    // ==================== ГЕТТЕРЫ ====================

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getAmount() {
        return amount;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getSource() {
        return source;
    }

    // ==================== EQUALS/HASHCODE ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AttributeModifier)) return false;
        AttributeModifier other = (AttributeModifier) obj;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("AttributeModifier{name='%s', amount=%.2f, operation=%s, source='%s'}",
                name, amount, operation, source);
    }
}