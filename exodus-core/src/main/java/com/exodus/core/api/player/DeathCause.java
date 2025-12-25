package com.exodus.core.api.player;

/**
 * Причины смерти игрока
 */
public enum DeathCause {
    HEAD_DESTROYED("Голова уничтожена", true),           // Мгновенная
    CRITICAL_HP_CHANCE("Критическое HP", false),         // RNG смерть
    STARVATION("Истощение", false),                      // Голод + жажда
    BLOOD_LOSS("Кровопотеря", false),                    // Кровотечение
    HYPOTHERMIA("Переохлаждение", false),                // Холод
    HYPERTHERMIA("Перегрев", false),                     // Жара
    RADIATION("Радиация", false),                        // Радиация
    POISON("Отравление", false),                         // Яд
    GENERIC("Общая", true);                              // Остальное

    private final String displayName;
    private final boolean instant;

    DeathCause(String displayName, boolean instant) {
        this.displayName = displayName;
        this.instant = instant;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isInstant() {
        return instant;
    }
}