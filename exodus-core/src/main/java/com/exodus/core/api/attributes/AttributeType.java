package com.exodus.core.api.attributes;

public enum AttributeType {

    // ==================== БОЕВЫЕ АТРИБУТЫ ====================
    // (уже созданные остаются без изменений)
    MELEE_DAMAGE("melee_damage", "Урон ближнего боя", 1.0f, 0.1f, 10.0f),
    RANGED_DAMAGE("ranged_damage", "Урон дальнего боя", 1.0f, 0.1f, 10.0f),
    ATTACK_SPEED("attack_speed", "Скорость атаки", 1.0f, 0.1f, 3.0f),
    CRIT_CHANCE("crit_chance", "Шанс крита", 0.05f, 0.0f, 0.6f),
    CRIT_DAMAGE("crit_damage", "Урон крита", 1.5f, 1.0f, 3.0f),
    ACCURACY("accuracy", "Точность", 0.7f, 0.0f, 1.3f),
    ATTACK_RANGE("attack_range", "Дальность атаки", 1.0f, 0.5f, 2.0f),
    RELOAD_SPEED("reload_speed", "Скорость перезарядки", 1.0f, 0.5f, 2.0f),

    // ✨ НОВЫЙ - Снижение отдачи оружия
    /**
     * Снижение отдачи оружия при стрельбе
     * Базовое: 0.0 (0%), Min: 0.0, Max: 0.6 (60% снижения)
     * Источник: STR × 3%
     */
    RECOIL_REDUCTION("recoil_reduction", "Снижение отдачи", 0.0f, 0.0f, 0.6f),

    // ==================== ЗАЩИТНЫЕ АТРИБУТЫ ====================
    ARMOR("armor", "Броня", 0.0f, 0.0f, 0.8f),
    BLEED_RESISTANCE("bleed_resistance", "Сопротивление кровотечению", 0.0f, 0.0f, 0.9f),
    FRACTURE_RESISTANCE("fracture_resistance", "Сопротивление переломам", 0.0f, 0.0f, 0.8f),
    PAIN_RESISTANCE("pain_resistance", "Сопротивление боли", 0.0f, 0.0f, 0.6f),
    EVASION("evasion", "Уклонение", 0.0f, 0.0f, 0.5f),

    // ✨ НОВЫЙ - Сопротивление дебафам
    /**
     * Шанс избежать негативных эффектов (яд, слепота, и т.д.)
     * Базовое: 0.0 (0%), Min: 0.0, Max: 0.6 (60%)
     * Источник: LUCK × 4%
     */
    DEBUFF_AVOIDANCE("debuff_avoidance", "Избежание дебафов", 0.0f, 0.0f, 0.6f),

    // ==================== ЗДОРОВЬЕ И HP ====================

    // ✨ НОВЫЙ - Модификатор максимального HP
    /**
     * Множитель HP всех частей тела
     * Базовое: 1.0 (100%), Min: 0.5, Max: 2.0 (200%)
     * Источник: CON × 2%
     *
     * ПРИМЕНЕНИЕ:
     * Голова: 35 × maxHealthMultiplier
     * Торс: 85 × maxHealthMultiplier
     * и т.д.
     */
    MAX_HEALTH_MULTIPLIER("max_health_multiplier", "Множитель HP", 1.0f, 0.5f, 2.0f),

    /**
     * Скорость регенерации HP (естественной)
     * Базовое: 1.0 (множитель), Min: 0.0, Max: 3.0
     * Источник: CON × 3%
     */
    HEALTH_REGEN("health_regen", "Регенерация HP", 1.0f, 0.0f, 3.0f),

    // ==================== ВИТАЛЬНЫЕ АТРИБУТЫ ====================

    MAX_STAMINA("max_stamina", "Макс. энергия", 150.0f, 50.0f, 500.0f),
    MAX_OXYGEN("max_oxygen", "Макс. кислород", 400.0f, 200.0f, 800.0f),
    STAMINA_REGEN("stamina_regen", "Регенерация стамины", 1.0f, 0.0f, 3.0f),

    // ✨ НОВЫЕ - Расход виталов
    /**
     * Скорость расхода сытости
     * Базовое: 1.0 (множитель), Min: 0.5, Max: 2.0
     * МЕНЬШЕ = лучше (медленнее голодаешь)
     */
    HUNGER_DRAIN_RATE("hunger_drain_rate", "Расход сытости", 1.0f, 0.5f, 2.0f),

    /**
     * Скорость расхода жажды
     * Базовое: 1.0 (множитель), Min: 0.5, Max: 2.0
     * МЕНЬШЕ = лучше (медленнее хочется пить)
     */
    THIRST_DRAIN_RATE("thirst_drain_rate", "Расход жажды", 1.0f, 0.5f, 2.0f),

    /**
     * Устойчивость к температуре (жара/холод)
     * Базовое: 0.0 (0%), Min: 0.0, Max: 0.8 (80%)
     * Источник: CON × 2%
     * БОЛЬШЕ = лучше (медленнее меняется температура)
     */
    TEMPERATURE_RESISTANCE("temperature_resistance", "Устойчивость к температуре", 0.0f, 0.0f, 0.8f),

    // ==================== ПСИХИКА ====================

    // ✨ НОВЫЕ - Психическое состояние
    /**
     * Сопротивление психическим дебафам
     * Базовое: 0.0 (0%), Min: 0.0, Max: 0.5 (50%)
     * Источник: INT × 1%
     * Снижает скорость потери психики
     */
    MENTAL_RESISTANCE("mental_resistance", "Сопротивление психическим дебафам", 0.0f, 0.0f, 0.5f),

    /**
     * Скорость восстановления психики
     * Базовое: 1.0 (множитель), Min: 0.5, Max: 2.0
     * Источник: INT × 2%
     */
    MENTAL_RECOVERY("mental_recovery", "Восстановление психики", 1.0f, 0.5f, 2.0f),

    /**
     * Максимальная устойчивость к стрессу
     * Базовое: 0.0 (0%), Min: 0.0, Max: 0.4 (40%)
     * Уменьшает потерю психики от боёв/одиночества
     */
    STRESS_RESISTANCE("stress_resistance", "Устойчивость к стрессу", 0.0f, 0.0f, 0.4f),

    // ==================== УТИЛИТАРНЫЕ АТРИБУТЫ ====================

    CARRY_WEIGHT("carry_weight", "Переносимый вес", 75.0f, 25.0f, 200.0f),
    HEALING_EFFICIENCY("healing_efficiency", "Эффективность лечения", 1.0f, 0.5f, 2.0f),
    MINING_SPEED("mining_speed", "Скорость добычи", 1.0f, 0.5f, 2.0f),
    CRAFTING_SPEED("crafting_speed", "Скорость крафта", 1.0f, 0.5f, 2.0f),
    CRAFTING_QUALITY("crafting_quality", "Качество крафта", 0.0f, 0.0f, 0.5f),
    RESOURCE_SAVING("resource_saving", "Экономия ресурсов", 0.0f, 0.0f, 0.4f),
    EXPERIENCE_GAIN("experience_gain", "Получаемый опыт", 1.0f, 0.5f, 2.0f),
    VISIBILITY("visibility", "Видимость", 1.0f, 0.5f, 1.5f),
    DETECTION_RANGE("detection_range", "Дальность обнаружения", 1.0f, 0.5f, 2.0f),

    // ✨ НОВЫЕ - Дополнительные утилиты
    /**
     * Длительность бафов
     * Базовое: 1.0 (множитель), Min: 0.5, Max: 2.0
     * Источник: INT × 4%
     */
    BUFF_DURATION("buff_duration", "Длительность бафов", 1.0f, 0.5f, 2.0f),

    /**
     * Прочность экипировки (медленнее ломается)
     * Базовое: 1.0 (множитель), Min: 0.5, Max: 2.0
     * Источник: LUCK × 3%
     * БОЛЬШЕ = лучше (экипировка служит дольше)
     */
    DURABILITY_BONUS("durability_bonus", "Прочность экипировки", 1.0f, 0.5f, 2.0f),

    // ==================== ЛУТ И НАХОДКИ ====================

    // ✨ НОВЫЕ - Качество дропа
    /**
     * Качество лута (шанс лучшего дропа)
     * Базовое: 0.0 (0%), Min: 0.0, Max: 0.8 (80%)
     * Источник: PER × 3% + LUCK × 5%
     */
    LOOT_QUALITY("loot_quality", "Качество лута", 0.0f, 0.0f, 0.8f),

    /**
     * Обнаружение секретов/ловушек/скрытых предметов
     * Базовое: 0.0 (0%), Min: 0.0, Max: 0.8 (80%)
     * Источник: PER × 8%
     */
    SECRET_DETECTION("secret_detection", "Обнаружение секретов", 0.0f, 0.0f, 0.8f),

    /**
     * Шанс "последнего шанса" (выжить с 1 HP)
     * Базовое: 0.0 (0%), Min: 0.0, Max: 0.2 (20%)
     * Источник: LUCK × 2%
     */
    LAST_STAND_CHANCE("last_stand_chance", "Шанс последнего шанса", 0.0f, 0.0f, 0.2f),

    // ==================== ТОРГОВЛЯ И СОЦИАЛЬНОЕ ====================

    // ✨ НОВЫЕ - Взаимодействие с NPC
    /**
     * Скидка у торговцев
     * Базовое: 0.0 (0%), Min: 0.0, Max: 0.6 (60%)
     * Источник: CHA × 4%
     * Цена покупки: базовая × (1 - discount)
     * Цена продажи: базовая × (1 + discount)
     */
    TRADE_DISCOUNT("trade_discount", "Скидка у торговцев", 0.0f, 0.0f, 0.6f),

    /**
     * Бонус наград за квесты
     * Базовое: 0.0 (0%), Min: 0.0, Max: 0.8 (80%)
     * Источник: CHA × 5%
     * Награда: базовая × (1 + bonus)
     */
    QUEST_REWARD_BONUS("quest_reward_bonus", "Бонус наград за квесты", 0.0f, 0.0f, 0.8f),

    /**
     * Шанс успеха диалоговых проверок
     * Базовое: 0.0 (0%), Min: 0.0, Max: 0.8 (80%)
     * Источник: CHA × 6%
     */
    DIALOGUE_SUCCESS("dialogue_success", "Успех в диалогах", 0.0f, 0.0f, 0.8f),

    /**
     * Скорость роста репутации с фракциями
     * Базовое: 1.0 (множитель), Min: 0.5, Max: 2.0
     * Источник: CHA × 3%
     */
    REPUTATION_GAIN("reputation_gain", "Рост репутации", 1.0f, 0.5f, 2.0f),

    /**
     * Шанс избежать боя переговорами
     * Базовое: 0.0 (0%), Min: 0.0, Max: 0.5 (50%)
     * Источник: CHA × 5%
     */
    COMBAT_AVOIDANCE("combat_avoidance", "Избежать боя", 0.0f, 0.0f, 0.5f),

    MOVEMENT_SPEED("movement_speed", "Скорость движения", 1.0f, 0.3f, 2.0f);

    // ==================== ПОЛЯ И МЕТОДЫ (без изменений) ====================

    private final String id;
    private final String displayName;
    private final float baseValue;
    private final float minValue;
    private final float maxValue;

    AttributeType(String id, String displayName, float baseValue, float minValue, float maxValue) {
        this.id = id;
        this.displayName = displayName;
        this.baseValue = baseValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getBaseValue() {
        return baseValue;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public float clamp(float value) {
        return Math.max(minValue, Math.min(maxValue, value));
    }

    public static AttributeType fromId(String id) {
        for (AttributeType type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}