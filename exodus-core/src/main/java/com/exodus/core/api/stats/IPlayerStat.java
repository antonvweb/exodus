package com.exodus.core.api.stats;

/**
 * Интерфейс для кастомного стата игрока
 * Другие моды реализуют этот интерфейс для своих систем
 */
public interface IPlayerStat {

    /**
     * Получить тип стата (возвращает Object для совместимости с VitalType и StatType)
     */
    Object getType();

    /**
     * Получить текущее значение
     */
    float getCurrent();

    /**
     * Получить максимальное значение
     */
    float getMax();

    /**
     * Установить текущее значение
     * @param value новое значение (будет ограничено между 0 и max)
     */
    void setCurrent(float value);

    /**
     * Установить максимальное значение
     */
    void setMax(float max);

    /**
     * Добавить к текущему значению
     * @param amount количество (может быть отрицательным)
     */
    default void add(float amount) {
        setCurrent(getCurrent() + amount);
    }

    /**
     * Получить название для отображения
     */
    String getDisplayName();

    /**
     * Получить цвет для отображения (ARGB формат)
     */
    int getColor();

    /**
     * Проверить заполнен ли стат полностью
     */
    default boolean isFull() {
        return getCurrent() >= getMax();
    }

    /**
     * Проверить пустой ли стат
     */
    default boolean isEmpty() {
        return getCurrent() <= 0;
    }

    /**
     * Получить процент заполнения (0.0 - 1.0)
     */
    default float getPercentage() {
        return getMax() > 0 ? getCurrent() / getMax() : 0;
    }
}