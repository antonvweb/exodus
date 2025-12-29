package com.exodus.core.api.player;

/**
 * Типы кровотечения
 */
public enum BleedingType {
    // Слабое: 30-60 сек, 1-2 HP/сек, БЕЗ боли
    WEAK("weak", "Слабое", 30, 60, 1.0f, 2.0f, false, 0),

    // Среднее: 90-120 сек, 5-8 HP/сек, С болью (боль остается 25 сек)
    MEDIUM("medium", "Среднее", 90, 120, 5.0f, 8.0f, true, 25),

    // Сильное: БЕСКОНЕЧНОЕ, 5-10 HP/сек, С болью (боль остается 105 сек после лечения)
    STRONG("strong", "Сильное", -1, -1, 5.0f, 10.0f, true, 105); // -1 = бесконечное

    private final String id;
    private final String displayName;
    private final int minDuration; // В секундах, -1 = бесконечное
    private final int maxDuration;
    private final float minDamage;  // HP в секунду
    private final float maxDamage;
    private final boolean causesPain;
    private final int painDurationAfter; // Секунды боли после окончания кровотечения

    BleedingType(String id, String displayName, int minDuration, int maxDuration,
                 float minDamage, float maxDamage, boolean causesPain, int painDurationAfter) {
        this.id = id;
        this.displayName = displayName;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
        this.causesPain = causesPain;
        this.painDurationAfter = painDurationAfter;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinDuration() {
        return minDuration;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public float getMinDamage() {
        return minDamage;
    }

    public float getMaxDamage() {
        return maxDamage;
    }

    public boolean causesPain() {
        return causesPain;
    }

    public int getPainDurationAfter() {
        return painDurationAfter;
    }

    /**
     * Бесконечное ли кровотечение
     */
    public boolean isInfinite() {
        return minDuration == -1;
    }

    /**
     * Получить случайную длительность для этого типа кровотечения
     * Возвращает -1 для бесконечного
     */
    public int getRandomDuration() {
        if (isInfinite()) {
            return -1; // Бесконечное
        }
        return minDuration + (int) (Math.random() * (maxDuration - minDuration));
    }

    /**
     * Получить случайный урон в секунду для этого типа
     */
    public float getRandomDamage() {
        return minDamage + (float) (Math.random() * (maxDamage - minDamage));
    }
}
