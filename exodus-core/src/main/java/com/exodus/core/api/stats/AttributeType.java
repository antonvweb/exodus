package com.exodus.core.api.stats;

/**
 * Прокачиваемые атрибуты персонажа
 * Влияют на показатели выживания и способности
 */
public enum AttributeType {
    ENDURANCE("endurance", "Выносливость", "END", 0xFF00AA00),
    INTELLIGENCE("intelligence", "Интеллект", "INT", 0xFF0088FF),
    STRENGTH("strength", "Сила", "STR", 0xFFFF4444),
    AGILITY("agility", "Ловкость", "AGI", 0xFFFFAA00),
    LUCK("luck", "Удача", "LCK", 0xFFFFFF00),
    SANITY("sanity", "Психика", "SAN", 0xFFAA00FF);

    private final String id;
    private final String displayName;
    private final String shortName;
    private final int defaultColor;

    AttributeType(String id, String displayName, String shortName, int defaultColor) {
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
     * Получить тип атрибута по ID
     */
    public static AttributeType fromId(String id) {
        for (AttributeType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Описание влияния атрибута
     */
    public String getDescription() {
        return switch (this) {
            case ENDURANCE -> "Увеличивает максимальное HP и Энергию. Замедляет усталость.";
            case INTELLIGENCE -> "Улучшает крафт и изучение. Снижает расход кислорода.";
            case STRENGTH -> "Увеличивает урон и грузоподъемность. Снижает расход энергии при работе.";
            case AGILITY -> "Повышает скорость передвижения и точность. Снижает расход энергии при беге.";
            case LUCK -> "Влияет на критические попадания и качество лута.";
            case SANITY -> "Увеличивает максимальный кислород. Защищает от стресса и галлюцинаций.";
        };
    }
}