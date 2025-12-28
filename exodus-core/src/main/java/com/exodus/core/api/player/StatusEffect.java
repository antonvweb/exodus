package com.exodus.core.api.player;

/**
 * Статусные эффекты игрока
 */
public enum StatusEffect {
    BLEEDING("bleeding", "Кровотечение", 0xFF0000),      // Красный
    FRACTURE("fracture", "Перелом", 0xFFFFFF),            // Белый
    PAIN("pain", "Боль", 0xFF6600);                       // Оранжевый

    private final String id;
    private final String displayName;
    private final int color;

    StatusEffect(String id, String displayName, int color) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }
}