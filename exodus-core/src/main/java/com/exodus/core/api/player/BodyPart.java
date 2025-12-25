package com.exodus.core.api.player;

public enum BodyPart {
    HEAD("head", "Голова", 35f, 3.0f, true),
    TORSO("torso", "Торс", 85f, 1.0f, true),
    LEFT_ARM("left_arm", "Левая рука", 60f, 0.8f, false),
    RIGHT_ARM("right_arm", "Правая рука", 60f, 0.8f, false),
    LEFT_LEG("left_leg", "Левая нога", 65f, 0.9f, false),
    RIGHT_LEG("right_leg", "Правая нога", 65f, 0.9f, false);

    private final String id;
    private final String displayName;
    private final float baseMaxHP;
    private final float damageMultiplier;
    private final boolean critical; // Критическая ли часть (смерть при 0 HP)

    BodyPart(String id, String displayName, float baseMaxHP, float damageMultiplier, boolean critical) {
        this.id = id;
        this.displayName = displayName;
        this.baseMaxHP = baseMaxHP;
        this.damageMultiplier = damageMultiplier;
        this.critical = critical;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getBaseMaxHP() {
        return baseMaxHP;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public boolean isCritical() {
        return critical;
    }

    public static BodyPart fromId(String id) {
        for (BodyPart part : values()) {
            if (part.id.equals(id)) {
                return part;
            }
        }
        return null;
    }
}