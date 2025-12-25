package com.exodus.core.api.player;

import net.minecraft.nbt.CompoundTag;
import java.util.EnumMap;
import java.util.Map;

/**
 * Расширенные данные игрока Project Exodus
 * Содержит всю дополнительную информацию о персонаже
 */
public class ExodusPlayerData {

    // ============ БАЗОВАЯ ИНФОРМАЦИЯ ============
    private String characterName;
    private int level;
    private int experience;

    // ============ ЧАСТИ ТЕЛА ============
    private Map<BodyPart, BodyPartData> bodyParts;

    // ============ ФИЗИОЛОГИЯ ============
    private float bloodLevel = 100f;              // Уровень крови (0-100%)
    private float bodyTemperature = 36.6f;        // Температура тела (°C)
    private long lastCriticalHPCheck = 0;         // Время последней проверки критического HP
    private long starvationStartTime = 0;         // Когда начался голод=0
    private long dehydrationStartTime = 0;        // Когда началась жажда=0

    /**
     * Данные одной части тела
     */
    public static class BodyPartData {
        public float currentHP;
        public float maxHP;
        public boolean isBroken;
        public boolean isBleeding;

        public BodyPartData(float maxHP) {
            this.maxHP = maxHP;
            this.currentHP = maxHP;
            this.isBroken = false;
            this.isBleeding = false;
        }

        /**
         * Получить процент HP (0.0 - 1.0)
         */
        public float getPercentage() {
            if (maxHP <= 0) {
                return 0f;
            }
            return Math.max(0f, Math.min(1f, currentHP / maxHP));
        }

        /**
         * Сохранить в NBT
         */
        public CompoundTag writeNbt(CompoundTag nbt) {
            nbt.putFloat("currentHP", currentHP);
            nbt.putFloat("maxHP", maxHP);
            nbt.putBoolean("isBroken", isBroken);
            nbt.putBoolean("isBleeding", isBleeding);
            return nbt;
        }

        /**
         * Загрузить из NBT
         */
        public void readNbt(CompoundTag nbt) {
            this.currentHP = nbt.getFloat("currentHP");
            this.maxHP = nbt.getFloat("maxHP");
            this.isBroken = nbt.getBoolean("isBroken");
            this.isBleeding = nbt.getBoolean("isBleeding");
        }
    }

    public ExodusPlayerData() {
        this.characterName = "";
        this.level = 1;
        this.experience = 0;
        this.bodyParts = new EnumMap<>(BodyPart.class);

        // Инициализируем все части тела
        for (BodyPart part : BodyPart.values()) {
            bodyParts.put(part, new BodyPartData(part.getBaseMaxHP()));
        }
    }

    // ============ ГЕТТЕРЫ/СЕТТЕРЫ: ФИЗИОЛОГИЯ ============

    public float getBloodLevel() {
        return bloodLevel;
    }

    public void setBloodLevel(float level) {
        this.bloodLevel = Math.max(0, Math.min(100, level));
    }

    public void loseBlood(float amount) {
        this.bloodLevel = Math.max(0, bloodLevel - amount);
    }

    public void restoreBlood(float amount) {
        this.bloodLevel = Math.min(100, bloodLevel + amount);
    }

    public float getBodyTemperature() {
        return bodyTemperature;
    }

    public void setBodyTemperature(float temp) {
        this.bodyTemperature = temp;
    }

    public long getLastCriticalHPCheck() {
        return lastCriticalHPCheck;
    }

    public void setLastCriticalHPCheck(long time) {
        this.lastCriticalHPCheck = time;
    }

    public long getStarvationStartTime() {
        return starvationStartTime;
    }

    public void setStarvationStartTime(long time) {
        this.starvationStartTime = time;
    }

    public long getDehydrationStartTime() {
        return dehydrationStartTime;
    }

    public void setDehydrationStartTime(long time) {
        this.dehydrationStartTime = time;
    }

    // ============ ГЕТТЕРЫ/СЕТТЕРЫ: БАЗОВАЯ ИНФОРМАЦИЯ ============

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    // ============ ГЕТТЕРЫ/СЕТТЕРЫ: ЧАСТИ ТЕЛА ============

    public BodyPartData getBodyPart(BodyPart part) {
        return bodyParts.get(part);
    }

    public Map<BodyPart, BodyPartData> getAllBodyParts() {
        return bodyParts;
    }

    /**
     * Получить суммарное HP всех частей тела
     */
    public float getTotalHP() {
        float total = 0;
        for (BodyPartData data : bodyParts.values()) {
            total += data.currentHP;
        }
        return total;
    }

    /**
     * Получить суммарное максимальное HP
     */
    public float getTotalMaxHP() {
        float total = 0;
        for (BodyPartData data : bodyParts.values()) {
            total += data.maxHP;
        }
        return total;
    }

    // ============ NBT СОХРАНЕНИЕ/ЗАГРУЗКА ============

    public CompoundTag writeNbt(CompoundTag nbt) {
        // Сохраняем базовую информацию
        nbt.putString("characterName", characterName);
        nbt.putInt("level", level);
        nbt.putInt("experience", experience);

        // Сохраняем части тела
        CompoundTag partsNbt = new CompoundTag();
        for (Map.Entry<BodyPart, BodyPartData> entry : bodyParts.entrySet()) {
            CompoundTag partNbt = entry.getValue().writeNbt(new CompoundTag());
            partsNbt.put(entry.getKey().name(), partNbt);
        }
        nbt.put("bodyParts", partsNbt);

        // Сохраняем физиологию
        nbt.putFloat("bloodLevel", bloodLevel);
        nbt.putFloat("bodyTemperature", bodyTemperature);
        nbt.putLong("lastCriticalHPCheck", lastCriticalHPCheck);
        nbt.putLong("starvationStartTime", starvationStartTime);
        nbt.putLong("dehydrationStartTime", dehydrationStartTime);

        return nbt;
    }

    public void readNbt(CompoundTag nbt) {
        // Загружаем базовую информацию
        if (nbt.contains("characterName")) {
            this.characterName = nbt.getString("characterName");
        }
        if (nbt.contains("level")) {
            this.level = nbt.getInt("level");
        }
        if (nbt.contains("experience")) {
            this.experience = nbt.getInt("experience");
        }

        // Загружаем части тела
        if (nbt.contains("bodyParts")) {
            CompoundTag partsNbt = nbt.getCompound("bodyParts");
            for (BodyPart part : BodyPart.values()) {
                if (partsNbt.contains(part.name())) {
                    CompoundTag partNbt = partsNbt.getCompound(part.name());
                    BodyPartData data = getBodyPart(part);
                    data.readNbt(partNbt);
                }
            }
        }

        // Загружаем физиологию
        if (nbt.contains("bloodLevel")) {
            this.bloodLevel = nbt.getFloat("bloodLevel");
        }
        if (nbt.contains("bodyTemperature")) {
            this.bodyTemperature = nbt.getFloat("bodyTemperature");
        }
        if (nbt.contains("lastCriticalHPCheck")) {
            this.lastCriticalHPCheck = nbt.getLong("lastCriticalHPCheck");
        }
        if (nbt.contains("starvationStartTime")) {
            this.starvationStartTime = nbt.getLong("starvationStartTime");
        }
        if (nbt.contains("dehydrationStartTime")) {
            this.dehydrationStartTime = nbt.getLong("dehydrationStartTime");
        }
    }
}