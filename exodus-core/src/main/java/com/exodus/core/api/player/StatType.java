package com.exodus.core.api.player;

/**
 * Основные статы персонажа (PRIMARY STATS)
 * Все статы начинаются с 5 пунктов + 7 свободных для распределения
 */
public enum StatType {
    // 💪 СИЛА - урон ближнего боя, carry weight, сопротивление переломам
    STRENGTH("strength", "Сила", 5),
    
    // 🎯 ЛОВКОСТЬ - точность, скорость атаки, уклонение, перезарядка
    DEXTERITY("dexterity", "Ловкость", 5),
    
    // 🛡️ ВЫНОСЛИВОСТЬ - макс HP, сопротивление кровотечению, стамина
    CONSTITUTION("constitution", "Выносливость", 5),
    
    // 🧠 ИНТЕЛЛЕКТ - эффективность лечения, крафт, XP, кислород
    INTELLIGENCE("intelligence", "Интеллект", 5),
    
    // 👁️ ВОСПРИЯТИЕ - крит шанс/урон, дальность, обнаружение
    PERCEPTION("perception", "Восприятие", 5),
    
    // 💬 КРАСНОРЕЧИЕ - торговля, квесты, диалоги
    CHARISMA("charisma", "Красноречие", 5),
    
    // 🍀 УДАЧА - крит шанс, лут, избежание дебафов
    LUCK("luck", "Удача", 5);

    private final String id;
    private final String displayName;
    private final int baseValue; // Стартовое значение (5)

    StatType(String id, String displayName, int baseValue) {
        this.id = id;
        this.displayName = displayName;
        this.baseValue = baseValue;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBaseValue() {
        return baseValue;
    }

    /**
     * Получить стат по ID
     */
    public static StatType fromId(String id) {
        for (StatType stat : values()) {
            if (stat.getId().equals(id)) {
                return stat;
            }
        }
        return null;
    }

    /**
     * Максимальное значение стата (без модификаторов)
     */
    public static final int MAX_VALUE = 10;

    /**
     * Свободные очки при создании персонажа
     */
    public static final int FREE_POINTS = 7;
}
