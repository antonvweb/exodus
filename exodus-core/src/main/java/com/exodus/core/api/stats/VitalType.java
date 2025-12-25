package com.exodus.core.api.stats;

/**
 * Показатели выживания персонажа
 * Изменяются в процессе игры и зависят от атрибутов
 */
public enum VitalType {
    HEALTH("health", "Здоровье", "HP", 0xFFFF0000),
    OXYGEN("oxygen", "Кислород", "O2", 0xFF00AAFF),
    HUNGER("hunger", "Голод", "FOOD", 0xFFFFAA00),
    THIRST("thirst", "Жажда", "H2O", 0xFF00BFFF),
    ENERGY("energy", "Энергия", "EN", 0xFFFFFF00);

    private final String id;
    private final String displayName;
    private final String shortName;
    private final int defaultColor;

    VitalType(String id, String displayName, String shortName, int defaultColor) {
        this.id = id;
        this.displayName = displayName;
        this.shortName = shortName;
        this.defaultColor = defaultColor;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortName() {
        return shortName;
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    /**
     * Получить тип показателя по ID
     */
    public static VitalType fromId(String id) {
        for (VitalType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Получить базовое максимальное значение (без бонусов от атрибутов)
     */
    public float getBaseMax() {
        return switch (this) {
            case HEALTH -> 100f;
            case OXYGEN -> 100f;
            case HUNGER -> 20f;
            case THIRST -> 20f;
            case ENERGY -> 100f;
        };
    }

    /**
     * Скорость уменьшения показателя в секунду (базовая)
     */
    public float getDecayRate() {
        return switch (this) {
            case HEALTH -> 0f; // HP не уменьшается само по себе
            case OXYGEN -> 1f; // 1 единица в секунду
            case HUNGER -> 0.05f; // Медленное уменьшение
            case THIRST -> 0.1f; // Быстрее чем голод
            case ENERGY -> 0.2f; // Уменьшается при активности
        };
    }

    /**
     * Описание показателя
     */
    public String getDescription() {
        return switch (this) {
            case HEALTH -> "Очки здоровья. При достижении 0 - смерть.";
            case OXYGEN -> "Запас кислорода. Без кислорода персонаж задыхается.";
            case HUNGER -> "Уровень сытости. Низкий голод замедляет регенерацию.";
            case THIRST -> "Уровень гидратации. Жажда ускоряет усталость.";
            case ENERGY -> "Запас энергии. При низкой энергии персонаж двигается медленнее.";
        };
    }
}