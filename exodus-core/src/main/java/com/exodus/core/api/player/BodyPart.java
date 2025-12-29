package com.exodus.core.api.player;

/**
 * Части тела игрока
 */
public enum BodyPart {
    HEAD("head", "Голова", 35.0f, true),           // Критическая - смерть при 0
    TORSO("torso", "Торс", 85.0f, true),           // Важная - кровотечение при низком HP
    LEFT_ARM("left_arm", "Левая рука", 45.0f, false),
    RIGHT_ARM("right_arm", "Правая рука", 45.0f, false),
    LEFT_LEG("left_leg", "Левая нога", 55.0f, false),
    RIGHT_LEG("right_leg", "Правая нога", 55.0f, false);

    private final String id;
    private final String displayName;
    private final float maxHP;
    private final boolean critical; // Смерть при 0 HP

    BodyPart(String id, String displayName, float maxHP, boolean critical) {
        this.id = id;
        this.displayName = displayName;
        this.maxHP = maxHP;
        this.critical = critical;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getMaxHP() {
        return maxHP;
    }

    public boolean isCritical() {
        return critical;
    }

    /**
     * Получить состояние части тела по проценту HP
     */
    public BodyPartState getState(float hpPercentage) {
        if (hpPercentage <= 0) {
            return BodyPartState.DESTROYED;
        } else if (hpPercentage < 0.4f) {
            return BodyPartState.CRITICAL;
        } else if (hpPercentage < 0.7f) {
            return BodyPartState.INJURED;
        } else {
            return BodyPartState.HEALTHY;
        }
    }

    /**
     * Состояние части тела (для текстур)
     */
    public enum BodyPartState {
        HEALTHY("healthy"),       // 70-100% HP
        INJURED("injured"),       // 40-70% HP
        CRITICAL("critical"),     // 10-40% HP
        DESTROYED("destroyed");   // 0% HP

        private final String textureSuffix;

        BodyPartState(String textureSuffix) {
            this.textureSuffix = textureSuffix;
        }

        public String getTextureSuffix() {
            return textureSuffix;
        }
    }
}
