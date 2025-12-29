package com.exodus.core.api.player;

/**
 * Типы жизненных показателей (VITAL STATS)
 * Динамические параметры, которые постоянно меняются
 */
public enum VitalType {
    HUNGER("hunger", "Сытость", 100.0f, "%"),
    THIRST("thirst", "Жажда", 100.0f, "%"),
    ENERGY("energy", "Энергия", 150.0f, ""), // Базовое, реальное зависит от CON
    OXYGEN("oxygen", "Кислород", 400.0f, "сек"), // Базовое, реальное зависит от INT
    TEMPERATURE("temperature", "Температура", 37.0f, "°C"),
    MENTAL("mental", "Психика", 100.0f, "%");

    private final String id;
    private final String displayName;
    private final float defaultMax; // Базовое максимальное значение
    private final String unit; // Единица измерения

    VitalType(String id, String displayName, float defaultMax, String unit) {
        this.id = id;
        this.displayName = displayName;
        this.defaultMax = defaultMax;
        this.unit = unit;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getDefaultMax() {
        return defaultMax;
    }

    public String getUnit() {
        return unit;
    }

    /**
     * Получить витал по ID
     */
    public static VitalType fromId(String id) {
        for (VitalType vital : values()) {
            if (vital.getId().equals(id)) {
                return vital;
            }
        }
        return null;
    }
}
