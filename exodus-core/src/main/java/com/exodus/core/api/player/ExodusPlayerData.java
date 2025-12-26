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

    // ============ ЧАСТИ ТЕЛА ============
    private Map<BodyPart, BodyPartData> bodyParts;

    /**
     * Данные одной части тела
     */
    public static class BodyPartData {
        public float currentHP;
        public float maxHP;
        public boolean isBroken;

        public BodyPartData(float maxHP) {
            this.maxHP = maxHP;
            this.currentHP = maxHP;
            this.isBroken = false;
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
            return nbt;
        }

        /**
         * Загрузить из NBT
         */
        public void readNbt(CompoundTag nbt) {
            this.currentHP = nbt.getFloat("currentHP");
            this.maxHP = nbt.getFloat("maxHP");
            this.isBroken = nbt.getBoolean("isBroken");
        }
    }

    public ExodusPlayerData() {
        this.characterName = "";
        this.bodyParts = new EnumMap<>(BodyPart.class);

        // Инициализируем все части тела
        for (BodyPart part : BodyPart.values()) {
            bodyParts.put(part, new BodyPartData(part.getBaseMaxHP()));
        }
    }

    // ============ ГЕТТЕРЫ/СЕТТЕРЫ: БАЗОВАЯ ИНФОРМАЦИЯ ============

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
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

        // Сохраняем части тела
        CompoundTag partsNbt = new CompoundTag();
        for (Map.Entry<BodyPart, BodyPartData> entry : bodyParts.entrySet()) {
            CompoundTag partNbt = entry.getValue().writeNbt(new CompoundTag());
            partsNbt.put(entry.getKey().name(), partNbt);
        }
        nbt.put("bodyParts", partsNbt);

        return nbt;
    }

    public void readNbt(CompoundTag nbt) {
        // Загружаем базовую информацию
        if (nbt.contains("characterName")) {
            this.characterName = nbt.getString("characterName");
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
    }
}