package com.exodus.core.api.stats;

public enum StatType {
    HEALTH("health", "HP", 0xFFFF0000),
    OXYGEN("oxygen", "O2", 0xFF00AAFF),
    THIRST("thirst", "H2O", 0xFF00BFFF),
    HUNGER("hunger", "FOOD", 0xFFFFAA00),
    STAMINA("stamina", "STAM", 0xFFFFFF00),
    TEMPERATURE("temperature", "TEMP", 0xFFFF8800),
    RADIATION("radiation", "RAD", 0xFF00FF00);

    private final String id;
    private final String displayName;
    private final int defaultColor;

    StatType(String id, String displayName, int defaultColor) {
        this.id = id;
        this.displayName = displayName;
        this.defaultColor = defaultColor;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    /**
     * Получить тип стата по ID
     */
    public static StatType fromId(String id) {
        for (StatType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }
}