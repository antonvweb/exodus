package com.exodus.core.player.attributes;

import com.exodus.core.api.attributes.AttributeModifier;
import com.exodus.core.api.player.StatType;

/**
 * Формула связи стата и атрибута
 *
 * ЗАЧЕМ: Вместо копипасты кода, описываем связи декларативно
 *
 * ПРИМЕР:
 * new AttributeFormula(StatType.STRENGTH, 0.05f, Operation.ADD_MULTIPLIED_BASE)
 * Означает: "За каждое очко силы добавить +5% к атрибуту"
 */
public class AttributeFormula {

    private final StatType stat;                        // Какой стат влияет
    private final float multiplier;                     // Множитель (например 0.05 для 5%)
    private final AttributeModifier.Operation operation; // Тип операции

    public AttributeFormula(StatType stat, float multiplier, AttributeModifier.Operation operation) {
        this.stat = stat;
        this.multiplier = multiplier;
        this.operation = operation;
    }

    /**
     * Создать модификатор на основе формулы
     *
     * @param statValue Значение стата (например STR = 5)
     * @param attributeName Название атрибута для имени модификатора
     * @return Готовый модификатор
     */
    public AttributeModifier createModifier(int statValue, String attributeName) {
        // Вычисляем итоговое значение
        float amount = statValue * multiplier;

        // Создаём уникальное имя: "str_melee_damage"
        String modifierName = stat.getId() + "_" + attributeName;

        return new AttributeModifier(modifierName, amount, operation, "stats");
    }

    // Геттеры
    public StatType getStat() {
        return stat;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public AttributeModifier.Operation getOperation() {
        return operation;
    }
}