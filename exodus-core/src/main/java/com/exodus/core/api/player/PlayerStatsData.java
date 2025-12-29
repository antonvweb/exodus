package com.exodus.core.api.player;

import net.minecraft.nbt.CompoundTag;
import java.util.EnumMap;
import java.util.Map;

/**
 * Данные статов игрока
 * Система 7 основных статов
 */
public class PlayerStatsData {

    // Значения статов (5-10 без модификаторов)
    private final Map<StatType, Integer> stats;

    // Уровень игрока
    private int level;
    
    // Текущий опыт
    private float experience;
    
    // Свободные очки для распределения
    private int freePoints;

    public PlayerStatsData() {
        this.stats = new EnumMap<>(StatType.class);
        this.level = 1;
        this.experience = 0f;
        this.freePoints = StatType.FREE_POINTS; // 7 свободных очков при старте

        // Инициализируем все статы базовым значением (5)
        for (StatType stat : StatType.values()) {
            stats.put(stat, stat.getBaseValue());
        }
    }

    // ============ СТАТЫ ============

    /**
     * Получить значение стата
     */
    public int getStat(StatType stat) {
        return stats.getOrDefault(stat, stat.getBaseValue());
    }

    /**
     * Установить значение стата
     * Ограничивается диапазоном 1-10
     */
    public void setStat(StatType stat, int value) {
        int clamped = Math.max(1, Math.min(StatType.MAX_VALUE, value));
        stats.put(stat, clamped);
    }

    /**
     * Увеличить стат на 1 (если есть свободные очки)
     */
    public boolean increaseStat(StatType stat) {
        int current = getStat(stat);
        
        if (current >= StatType.MAX_VALUE) {
            return false; // Уже максимум
        }
        
        if (freePoints <= 0) {
            return false; // Нет свободных очков
        }
        
        setStat(stat, current + 1);
        freePoints--;
        return true;
    }

    // ============ УРОВЕНЬ И ОПЫТ ============

    /**
     * Получить уровень
     */
    public int getLevel() {
        return level;
    }

    /**
     * Установить уровень
     */
    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    /**
     * Получить опыт
     */
    public float getExperience() {
        return experience;
    }

    /**
     * Установить опыт
     */
    public void setExperience(float experience) {
        this.experience = Math.max(0, experience);
    }

    /**
     * Добавить опыт
     * Автоматически повышает уровень при достижении порога
     */
    public void addExperience(float amount) {
        // Модификатор от интеллекта (+3% за очко)
        int intelligence = getStat(StatType.INTELLIGENCE);
        float bonus = 1.0f + (intelligence * 0.03f);
        
        this.experience += amount * bonus;

        // Проверяем повышение уровня
        while (this.experience >= getExperienceForNextLevel()) {
            levelUp();
        }
    }

    /**
     * Сколько опыта нужно для следующего уровня
     */
    public float getExperienceForNextLevel() {
        // Экспоненциальная прогрессия: 100 * 1.5^(level-1)
        return (float) (100 * Math.pow(1.5, level - 1));
    }

    /**
     * Повысить уровень
     */
    private void levelUp() {
        this.experience -= getExperienceForNextLevel();
        this.level++;

        // Каждые 2 уровня даём +1 очко стата
        if (level % 2 == 0) {
            freePoints++;
        }
    }

    /**
     * Получить свободные очки
     */
    public int getFreePoints() {
        return freePoints;
    }

    /**
     * Установить свободные очки
     */
    public void setFreePoints(int points) {
        this.freePoints = Math.max(0, points);
    }

    // ============ ПРОИЗВОДНЫЕ ПАРАМЕТРЫ ============

    /**
     * Урон ближнего боя: +5% за очко силы
     */
    public float getMeleeDamageModifier() {
        int strength = getStat(StatType.STRENGTH);
        return 1.0f + (strength * 0.05f);
    }

    /**
     * Переносимый вес: 50 + (STR × 5 кг)
     */
    public float getCarryWeight() {
        int strength = getStat(StatType.STRENGTH);
        return 50.0f + (strength * 5.0f);
    }

    /**
     * Сопротивление переломам: +3% за очко силы
     */
    public float getFractureResistanceFromStrength() {
        return getStat(StatType.STRENGTH) * 0.03f;
    }

    /**
     * Отдача от оружия: -3% за очко силы
     */
    public float getRecoilReduction() {
        return getStat(StatType.STRENGTH) * 0.03f;
    }

    /**
     * Скорость добычи: +2% за очко силы
     */
    public float getMiningSpeedBonus() {
        return getStat(StatType.STRENGTH) * 0.02f;
    }

    /**
     * Точность: +4% за очко ловкости
     */
    public float getAccuracyFromDexterity() {
        return getStat(StatType.DEXTERITY) * 0.04f;
    }

    /**
     * Скорость атаки: +3% за очко ловкости
     */
    public float getAttackSpeedBonus() {
        return getStat(StatType.DEXTERITY) * 0.03f;
    }

    /**
     * Уклонение: +2% за очко ловкости
     */
    public float getEvasionFromDexterity() {
        return getStat(StatType.DEXTERITY) * 0.02f;
    }

    /**
     * Скорость перезарядки: +3% за очко ловкости
     */
    public float getReloadSpeedBonus() {
        return getStat(StatType.DEXTERITY) * 0.03f;
    }

    /**
     * Крит шанс от ловкости: +1% за очко
     */
    public float getCritChanceFromDexterity() {
        return getStat(StatType.DEXTERITY) * 0.01f;
    }

    /**
     * Модификатор максимального HP: +2% за очко выносливости
     */
    public float getMaxHPModifier() {
        int constitution = getStat(StatType.CONSTITUTION);
        return 1.0f + (constitution * 0.02f);
    }

    /**
     * Сопротивление кровотечению: +5% за очко выносливости
     */
    public float getBleedResistanceFromConstitution() {
        return getStat(StatType.CONSTITUTION) * 0.05f;
    }

    /**
     * Сопротивление боли: +4% за очко выносливости
     */
    public float getPainResistance() {
        return getStat(StatType.CONSTITUTION) * 0.04f;
    }

    /**
     * Максимальная стамина: 100 + (CON × 10)
     */
    public float getMaxStamina() {
        int constitution = getStat(StatType.CONSTITUTION);
        return 100.0f + (constitution * 10.0f);
    }

    /**
     * Бонус регенерации: +3% за очко выносливости
     */
    public float getRegenerationBonus() {
        return getStat(StatType.CONSTITUTION) * 0.03f;
    }

    /**
     * Эффективность лечения: +6% за очко интеллекта
     */
    public float getHealingEfficiency() {
        int intelligence = getStat(StatType.INTELLIGENCE);
        return 1.0f + (intelligence * 0.06f);
    }

    /**
     * Скорость крафта: +5% за очко интеллекта
     */
    public float getCraftingSpeedBonus() {
        return getStat(StatType.INTELLIGENCE) * 0.05f;
    }

    /**
     * Качество крафта: +2% за очко интеллекта
     */
    public float getCraftingQualityBonus() {
        return getStat(StatType.INTELLIGENCE) * 0.02f;
    }

    /**
     * Экономия ресурсов: -2% за очко интеллекта
     */
    public float getResourceSaving() {
        return getStat(StatType.INTELLIGENCE) * 0.02f;
    }

    /**
     * Длительность бафов: +4% за очко интеллекта
     */
    public float getBuffDurationBonus() {
        return getStat(StatType.INTELLIGENCE) * 0.04f;
    }

    /**
     * Максимальный кислород: 300 + (INT × 20) секунд
     */
    public int getMaxOxygen() {
        int intelligence = getStat(StatType.INTELLIGENCE);
        return 300 + (intelligence * 20);
    }

    /**
     * Сопротивление психическим дебафам: -1% за очко интеллекта
     */
    public float getMentalDebuffResistance() {
        return getStat(StatType.INTELLIGENCE) * 0.01f;
    }

    /**
     * Крит шанс от восприятия: +2% за очко
     */
    public float getCritChanceFromPerception() {
        return getStat(StatType.PERCEPTION) * 0.02f;
    }

    /**
     * Крит урон: +5% за очко восприятия
     */
    public float getCritDamageBonus() {
        return getStat(StatType.PERCEPTION) * 0.05f;
    }

    /**
     * Дальность обнаружения: +10% за очко восприятия
     */
    public float getDetectionRangeBonus() {
        return getStat(StatType.PERCEPTION) * 0.10f;
    }

    /**
     * Качество лута: +3% за очко восприятия
     */
    public float getLootQualityBonus() {
        return getStat(StatType.PERCEPTION) * 0.03f;
    }

    /**
     * Обнаружение секретов: +8% за очко восприятия
     */
    public float getSecretDetectionBonus() {
        return getStat(StatType.PERCEPTION) * 0.08f;
    }

    /**
     * Точность от восприятия: +2% за очко
     */
    public float getAccuracyFromPerception() {
        return getStat(StatType.PERCEPTION) * 0.02f;
    }

    /**
     * Дальность атаки: +5% за очко восприятия
     */
    public float getAttackRangeBonus() {
        return getStat(StatType.PERCEPTION) * 0.05f;
    }

    /**
     * Урон дальнего боя: +2% за очко восприятия
     */
    public float getRangedDamageModifier() {
        int perception = getStat(StatType.PERCEPTION);
        return 1.0f + (perception * 0.02f);
    }

    /**
     * Скидка у торговцев: -4% за очко красноречия
     */
    public float getTradeDiscount() {
        return getStat(StatType.CHARISMA) * 0.04f;
    }

    /**
     * Награды за квесты: +5% за очко красноречия
     */
    public float getQuestRewardBonus() {
        return getStat(StatType.CHARISMA) * 0.05f;
    }

    /**
     * Шанс диалоговых проверок: +6% за очко красноречия
     */
    public float getDialogueSuccessBonus() {
        return getStat(StatType.CHARISMA) * 0.06f;
    }

    /**
     * Репутация: +3% за очко красноречия
     */
    public float getReputationBonus() {
        return getStat(StatType.CHARISMA) * 0.03f;
    }

    /**
     * Крит шанс от удачи: +1.5% за очко
     */
    public float getCritChanceFromLuck() {
        return getStat(StatType.LUCK) * 0.015f;
    }

    /**
     * Качество лута от удачи: +5% за очко
     */
    public float getLootQualityFromLuck() {
        return getStat(StatType.LUCK) * 0.05f;
    }

    /**
     * Избежание негативных эффектов: +4% за очко удачи
     */
    public float getDebuffAvoidance() {
        return getStat(StatType.LUCK) * 0.04f;
    }

    /**
     * Прочность экипировки: +3% за очко удачи
     */
    public float getDurabilityBonus() {
        return getStat(StatType.LUCK) * 0.03f;
    }

    /**
     * Шанс "последнего шанса": +2% за очко удачи
     */
    public float getLastStandChance() {
        return getStat(StatType.LUCK) * 0.02f;
    }

    /**
     * ИТОГОВЫЙ критический шанс: базовый 5% + PER + DEX + LUCK
     */
    public float getTotalCritChance() {
        return 0.05f + 
               getCritChanceFromPerception() + 
               getCritChanceFromDexterity() + 
               getCritChanceFromLuck();
    }

    /**
     * ИТОГОВОЕ уклонение: DEX + LUCK
     */
    public float getTotalEvasion() {
        int dexterity = getStat(StatType.DEXTERITY);
        int luck = getStat(StatType.LUCK);
        return (dexterity * 0.02f) + (luck * 0.015f);
    }

    /**
     * ИТОГОВАЯ точность: базовая 70% + DEX + PER
     */
    public float getTotalAccuracy() {
        return 0.70f + getAccuracyFromDexterity() + getAccuracyFromPerception();
    }

    /**
     * ИТОГОВОЕ сопротивление кровотечению: CON + STR
     */
    public float getTotalBleedResistance() {
        int constitution = getStat(StatType.CONSTITUTION);
        int strength = getStat(StatType.STRENGTH);
        return (constitution * 0.05f) + (strength * 0.03f);
    }

    /**
     * ИТОГОВОЕ сопротивление переломам: STR + CON
     */
    public float getTotalFractureResistance() {
        int strength = getStat(StatType.STRENGTH);
        int constitution = getStat(StatType.CONSTITUTION);
        return (strength * 0.03f) + (constitution * 0.02f);
    }

    // ============ NBT ============

    public CompoundTag writeNbt(CompoundTag nbt) {
        // Статы
        CompoundTag statsNbt = new CompoundTag();
        for (StatType stat : StatType.values()) {
            statsNbt.putInt(stat.getId(), getStat(stat));
        }
        nbt.put("stats", statsNbt);

        // Уровень и опыт
        nbt.putInt("level", level);
        nbt.putFloat("experience", experience);
        nbt.putInt("freePoints", freePoints);

        return nbt;
    }

    public void readNbt(CompoundTag nbt) {
        // Статы
        if (nbt.contains("stats")) {
            CompoundTag statsNbt = nbt.getCompound("stats");
            for (StatType stat : StatType.values()) {
                if (statsNbt.contains(stat.getId())) {
                    setStat(stat, statsNbt.getInt(stat.getId()));
                }
            }
        }

        // Уровень и опыт
        if (nbt.contains("level")) {
            level = nbt.getInt("level");
        }
        if (nbt.contains("experience")) {
            experience = nbt.getFloat("experience");
        }
        if (nbt.contains("freePoints")) {
            freePoints = nbt.getInt("freePoints");
        }
    }
}
