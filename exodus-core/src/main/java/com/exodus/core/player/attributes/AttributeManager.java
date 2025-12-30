package com.exodus.core.player.attributes;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.attributes.AttributeModifier;
import com.exodus.core.api.attributes.AttributeType;
import com.exodus.core.api.player.PlayerStatsData;
import com.exodus.core.api.player.StatType;
import com.exodus.core.player.stats.PlayerStatsManager;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Центральная система управления атрибутами игроков
 *
 * ОТВЕТСТВЕННОСТЬ:
 * - Хранит атрибуты всех игроков
 * - Предоставляет API для получения/изменения атрибутов
 * - Пересчитывает атрибуты при изменении статов
 * - Очищает данные при выходе игрока
 */
public class AttributeManager {

    // ==================== ХРАНИЛИЩЕ ====================

    /**
     * UUID игрока → Map атрибутов
     * Внутренняя Map: AttributeType → AttributeInstance
     */
    private static final Map<UUID, Map<AttributeType, AttributeInstance>> playerAttributes = new HashMap<>();

    /**
     * Таблица связей: Атрибут → Список формул от статов
     *
     * Каждый атрибут может зависеть от нескольких статов
     * Например: ACCURACY зависит от DEX (4%) и PER (2%)
     */
    private static final Map<AttributeType, List<AttributeFormula>> ATTRIBUTE_FORMULAS = new HashMap<>();

    /**
     * Инициализация таблицы формул
     * Вызывается ОДИН раз при загрузке мода
     */
    static {
        // Используем короткие алиасы для читаемости
        var ADD = AttributeModifier.Operation.ADD;
        var MULT_BASE = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
        var MULT_TOTAL = AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;

        // ============ БОЕВЫЕ АТРИБУТЫ ============

        // MELEE_DAMAGE: +5% за STR
        addFormula(AttributeType.MELEE_DAMAGE,
                new AttributeFormula(StatType.STRENGTH, 0.05f, MULT_BASE)
        );

        // RANGED_DAMAGE: +2% за PER
        addFormula(AttributeType.RANGED_DAMAGE,
                new AttributeFormula(StatType.PERCEPTION, 0.02f, MULT_BASE)
        );

        // ATTACK_SPEED: +3% за DEX
        addFormula(AttributeType.ATTACK_SPEED,
                new AttributeFormula(StatType.DEXTERITY, 0.03f, MULT_BASE)
        );

        // CRIT_CHANCE: +2% PER + 1% DEX + 1.5% LUCK
        addFormula(AttributeType.CRIT_CHANCE,
                new AttributeFormula(StatType.PERCEPTION, 0.02f, ADD),
                new AttributeFormula(StatType.DEXTERITY, 0.01f, ADD),
                new AttributeFormula(StatType.LUCK, 0.015f, ADD)
        );

        // CRIT_DAMAGE: +5% за PER
        addFormula(AttributeType.CRIT_DAMAGE,
                new AttributeFormula(StatType.PERCEPTION, 0.05f, MULT_BASE)
        );

        // ACCURACY: +4% DEX + 2% PER
        addFormula(AttributeType.ACCURACY,
                new AttributeFormula(StatType.DEXTERITY, 0.04f, ADD),
                new AttributeFormula(StatType.PERCEPTION, 0.02f, ADD)
        );

        // ATTACK_RANGE: +5% за PER
        addFormula(AttributeType.ATTACK_RANGE,
                new AttributeFormula(StatType.PERCEPTION, 0.05f, MULT_BASE)
        );

        // RELOAD_SPEED: +3% за DEX
        addFormula(AttributeType.RELOAD_SPEED,
                new AttributeFormula(StatType.DEXTERITY, 0.03f, MULT_BASE)
        );

        // RECOIL_REDUCTION: +3% за STR
        addFormula(AttributeType.RECOIL_REDUCTION,
                new AttributeFormula(StatType.STRENGTH, 0.03f, ADD)
        );

        // ============ ЗАЩИТНЫЕ АТРИБУТЫ ============

        // BLEED_RESISTANCE: +5% CON + 3% STR
        addFormula(AttributeType.BLEED_RESISTANCE,
                new AttributeFormula(StatType.CONSTITUTION, 0.05f, ADD),
                new AttributeFormula(StatType.STRENGTH, 0.03f, ADD)
        );

        // FRACTURE_RESISTANCE: +3% STR + 2% CON
        addFormula(AttributeType.FRACTURE_RESISTANCE,
                new AttributeFormula(StatType.STRENGTH, 0.03f, ADD),
                new AttributeFormula(StatType.CONSTITUTION, 0.02f, ADD)
        );

        // PAIN_RESISTANCE: +4% за CON
        addFormula(AttributeType.PAIN_RESISTANCE,
                new AttributeFormula(StatType.CONSTITUTION, 0.04f, ADD)
        );

        // EVASION: +2% DEX + 1.5% LUCK
        addFormula(AttributeType.EVASION,
                new AttributeFormula(StatType.DEXTERITY, 0.02f, ADD),
                new AttributeFormula(StatType.LUCK, 0.015f, ADD)
        );

        // DEBUFF_AVOIDANCE: +4% за LUCK
        addFormula(AttributeType.DEBUFF_AVOIDANCE,
                new AttributeFormula(StatType.LUCK, 0.04f, ADD)
        );

        // ============ ЗДОРОВЬЕ ============

        // MAX_HEALTH_MULTIPLIER: +2% за CON
        addFormula(AttributeType.MAX_HEALTH_MULTIPLIER,
                new AttributeFormula(StatType.CONSTITUTION, 0.02f, MULT_BASE)
        );

        // HEALTH_REGEN: +3% за CON
        addFormula(AttributeType.HEALTH_REGEN,
                new AttributeFormula(StatType.CONSTITUTION, 0.03f, MULT_BASE)
        );

        // ============ ВИТАЛЫ ============

        // MAX_STAMINA: +10 за CON (ВАЖНО: ADD, не процент!)
        addFormula(AttributeType.MAX_STAMINA,
                new AttributeFormula(StatType.CONSTITUTION, 10.0f, ADD)
        );

        // MAX_OXYGEN: +20 за INT (ВАЖНО: ADD!)
        addFormula(AttributeType.MAX_OXYGEN,
                new AttributeFormula(StatType.INTELLIGENCE, 20.0f, ADD)
        );

        // STAMINA_REGEN: +3% за CON
        addFormula(AttributeType.STAMINA_REGEN,
                new AttributeFormula(StatType.CONSTITUTION, 0.03f, MULT_BASE)
        );

        // TEMPERATURE_RESISTANCE: +2% за CON
        addFormula(AttributeType.TEMPERATURE_RESISTANCE,
                new AttributeFormula(StatType.CONSTITUTION, 0.02f, ADD)
        );

        // ============ ПСИХИКА ============

        // MENTAL_RESISTANCE: +1% за INT
        addFormula(AttributeType.MENTAL_RESISTANCE,
                new AttributeFormula(StatType.INTELLIGENCE, 0.01f, ADD)
        );

        // MENTAL_RECOVERY: +2% за INT
        addFormula(AttributeType.MENTAL_RECOVERY,
                new AttributeFormula(StatType.INTELLIGENCE, 0.02f, MULT_BASE)
        );

        // STRESS_RESISTANCE: можно добавить если нужно

        // ============ УТИЛИТАРНЫЕ ============

        // CARRY_WEIGHT: +5 кг за STR
        addFormula(AttributeType.CARRY_WEIGHT,
                new AttributeFormula(StatType.STRENGTH, 5.0f, ADD)
        );

        // HEALING_EFFICIENCY: +6% за INT
        addFormula(AttributeType.HEALING_EFFICIENCY,
                new AttributeFormula(StatType.INTELLIGENCE, 0.06f, MULT_BASE)
        );

        // MINING_SPEED: +2% за STR
        addFormula(AttributeType.MINING_SPEED,
                new AttributeFormula(StatType.STRENGTH, 0.02f, MULT_BASE)
        );

        // CRAFTING_SPEED: +5% за INT
        addFormula(AttributeType.CRAFTING_SPEED,
                new AttributeFormula(StatType.INTELLIGENCE, 0.05f, MULT_BASE)
        );

        // CRAFTING_QUALITY: +2% за INT
        addFormula(AttributeType.CRAFTING_QUALITY,
                new AttributeFormula(StatType.INTELLIGENCE, 0.02f, ADD)
        );

        // RESOURCE_SAVING: +2% за INT
        addFormula(AttributeType.RESOURCE_SAVING,
                new AttributeFormula(StatType.INTELLIGENCE, 0.02f, ADD)
        );

        // EXPERIENCE_GAIN: +3% за INT
        addFormula(AttributeType.EXPERIENCE_GAIN,
                new AttributeFormula(StatType.INTELLIGENCE, 0.03f, MULT_BASE)
        );

        // VISIBILITY: -2% за DEX (меньше = лучше)
        addFormula(AttributeType.VISIBILITY,
                new AttributeFormula(StatType.DEXTERITY, -0.02f, MULT_BASE)
        );

        // DETECTION_RANGE: +10% за PER
        addFormula(AttributeType.DETECTION_RANGE,
                new AttributeFormula(StatType.PERCEPTION, 0.10f, MULT_BASE)
        );

        // BUFF_DURATION: +4% за INT
        addFormula(AttributeType.BUFF_DURATION,
                new AttributeFormula(StatType.INTELLIGENCE, 0.04f, MULT_BASE)
        );

        // DURABILITY_BONUS: +3% за LUCK
        addFormula(AttributeType.DURABILITY_BONUS,
                new AttributeFormula(StatType.LUCK, 0.03f, MULT_BASE)
        );

        // ============ ЛУТ ============

        // LOOT_QUALITY: +3% PER + 5% LUCK
        addFormula(AttributeType.LOOT_QUALITY,
                new AttributeFormula(StatType.PERCEPTION, 0.03f, ADD),
                new AttributeFormula(StatType.LUCK, 0.05f, ADD)
        );

        // SECRET_DETECTION: +8% за PER
        addFormula(AttributeType.SECRET_DETECTION,
                new AttributeFormula(StatType.PERCEPTION, 0.08f, ADD)
        );

        // LAST_STAND_CHANCE: +2% за LUCK
        addFormula(AttributeType.LAST_STAND_CHANCE,
                new AttributeFormula(StatType.LUCK, 0.02f, ADD)
        );

        // ============ СОЦИАЛЬНОЕ ============

        // TRADE_DISCOUNT: +4% за CHA
        addFormula(AttributeType.TRADE_DISCOUNT,
                new AttributeFormula(StatType.CHARISMA, 0.04f, ADD)
        );

        // QUEST_REWARD_BONUS: +5% за CHA
        addFormula(AttributeType.QUEST_REWARD_BONUS,
                new AttributeFormula(StatType.CHARISMA, 0.05f, ADD)
        );

        // DIALOGUE_SUCCESS: +6% за CHA
        addFormula(AttributeType.DIALOGUE_SUCCESS,
                new AttributeFormula(StatType.CHARISMA, 0.06f, ADD)
        );

        // REPUTATION_GAIN: +3% за CHA
        addFormula(AttributeType.REPUTATION_GAIN,
                new AttributeFormula(StatType.CHARISMA, 0.03f, MULT_BASE)
        );

        // COMBAT_AVOIDANCE: +5% за CHA
        addFormula(AttributeType.COMBAT_AVOIDANCE,
                new AttributeFormula(StatType.CHARISMA, 0.05f, ADD)
        );
    }

    /**
     * Вспомогательный метод для добавления формул
     */
    private static void addFormula(AttributeType attribute, AttributeFormula... formulas) {
        ATTRIBUTE_FORMULAS.put(attribute, Arrays.asList(formulas));
    }

    // ==================== ИНИЦИАЛИЗАЦИЯ ====================

    /**
     * Получить или создать все атрибуты для игрока
     *
     * КОГДА ВЫЗЫВАЕТСЯ:
     * - При первом обращении к атрибутам игрока
     * - При входе игрока на сервер
     *
     * ЧТО ДЕЛАЕТ:
     * 1. Проверяет есть ли уже атрибуты для этого игрока
     * 2. Если нет → создаёт AttributeInstance для ВСЕХ 45 атрибутов
     * 3. Возвращает Map атрибутов
     *
     * @param player Игрок
     * @return Map всех атрибутов этого игрока
     */
    private static Map<AttributeType, AttributeInstance> getOrCreateAttributes(Player player) {
        UUID uuidPlayer = player.getUUID();

        if (!playerAttributes.containsKey(uuidPlayer)) {
            HashMap<AttributeType, AttributeInstance> newData = new HashMap<>();

            for (AttributeType type : AttributeType.values()) {
                AttributeInstance newAttribute = new AttributeInstance(type);
                newData.put(type, newAttribute);
            }

            playerAttributes.put(uuidPlayer, newData);
        }

        return playerAttributes.get(uuidPlayer);
    }

    /**
     * Удалить все атрибуты игрока
     * Вызывается при выходе игрока с сервера
     */
    public static void removePlayer(Player player) {
       playerAttributes.remove(player.getUUID());
    }

    /**
     * Пересчитать ВСЕ атрибуты игрока на основе его статов
     *
     * КОГДА ВЫЗЫВАЕТСЯ:
     * - Игрок повысил стат (STR, DEX, CON и т.д.)
     * - Игрок получил уровень
     * - При загрузке игрока (вход на сервер)
     *
     * ЧТО ДЕЛАЕТ:
     * 1. Удаляет ВСЕ старые модификаторы от статов
     * 2. Пересчитывает новые модификаторы на основе текущих статов
     * 3. Применяет их к атрибутам
     *
     * ФОРМУЛЫ (из твоей документации):
     * - MAX_STAMINA = 100 + (CON × 10)
     * - MELEE_DAMAGE = 1.0 + (STR × 0.05)
     * - BLEED_RESISTANCE = (CON × 0.05) + (STR × 0.03)
     * - и т.д. для всех 45 атрибутов
     */
    public static void recalculate(Player player) {
        Map<AttributeType, AttributeInstance> attributes = getOrCreateAttributes(player);

        // ШАГ 1: Удалить все старые модификаторы от статов
        for (AttributeInstance instance : attributes.values()) {
            instance.removeModifiersBySource("stats");
        }

        // ШАГ 2: Получить статы игрока
        PlayerStatsData stats = PlayerStatsManager.getComponent(player).getData();

        // ШАГ 3: Применить ВСЕ формулы из таблицы
        for (Map.Entry<AttributeType, List<AttributeFormula>> entry : ATTRIBUTE_FORMULAS.entrySet()) {
            AttributeType attributeType = entry.getKey();
            List<AttributeFormula> formulas = entry.getValue();

            // Для каждой формулы этого атрибута
            for (AttributeFormula formula : formulas) {
                // Получаем значение стата
                int statValue = stats.getStat(formula.getStat());

                // Создаём модификатор
                if (statValue > 0) {
                    AttributeModifier modifier = formula.createModifier(
                            statValue,
                            attributeType.getId()
                    );

                    // Применяем
                    addModifier(player, attributeType, modifier);
                }
            }
        }
    }

    /**
     * Получить финальное значение атрибута игрока
     *
     * ПРИМЕР ИСПОЛЬЗОВАНИЯ:
     * float maxStamina = AttributeManager.getValue(player, AttributeType.MAX_STAMINA);
     * // Вернёт 200 (150 базовое + 50 от статов)
     *
     * @param player Игрок
     * @param type Тип атрибута
     * @return Финальное значение с учётом всех модификаторов
     */
    public static float getValue(Player player, AttributeType type) {
        Map<AttributeType, AttributeInstance> attributePlayer = getOrCreateAttributes(player);
        AttributeInstance attribute = attributePlayer.get(type);

        if (attribute == null) {
            return type.getBaseValue();
        }

        return attribute.getValue();
    }

    /**
     * Добавить модификатор к атрибуту игрока
     *
     * ПРИМЕР ИСПОЛЬЗОВАНИЯ:
     * // Игрок надел броню
     * AttributeModifier armorBonus = new AttributeModifier(
     *     "armor_stamina", 20, Operation.ADD, "armor_chest"
     * );
     * AttributeManager.addModifier(player, AttributeType.MAX_STAMINA, armorBonus);
     *
     * @param player Игрок
     * @param type Тип атрибута
     * @param modifier Модификатор для добавления
     */
    public static void addModifier(Player player, AttributeType type, AttributeModifier modifier) {
        Map<AttributeType, AttributeInstance> attributePlayer = getOrCreateAttributes(player);
        AttributeInstance attribute = attributePlayer.get(type);
        attribute.addModifier(modifier);
    }
    /**
     * Удалить модификатор по UUID
     *
     * ПРИМЕР:
     * AttributeManager.removeModifier(player, AttributeType.MAX_STAMINA, modifierUUID);
     */
    public static void removeModifier(Player player, AttributeType type, UUID modifierId) {
        Map<AttributeType, AttributeInstance> attributePlayer = getOrCreateAttributes(player);
        AttributeInstance attribute = attributePlayer.get(type);
        attribute.removeModifier(modifierId);
    }

    /**
     * Удалить ВСЕ модификаторы от источника
     *
     * ПРИМЕР:
     * // Игрок снял броню - удаляем все бонусы от неё
     * AttributeManager.removeModifiersBySource(player, AttributeType.MAX_STAMINA, "armor_chest");
     *
     * @return Количество удалённых модификаторов
     */
    public static int removeModifiersBySource(Player player, AttributeType type, String source) {
        Map<AttributeType, AttributeInstance> attributePlayer = getOrCreateAttributes(player);
        AttributeInstance attribute = attributePlayer.get(type);
        return attribute.removeModifiersBySource(source);
    }

    // 5. Проверить наличие
    public static boolean hasModifier(Player player, AttributeType type, UUID modifierId) {
        Map<AttributeType, AttributeInstance> attributePlayer = getOrCreateAttributes(player);
        AttributeInstance attribute = attributePlayer.get(type);
        return attribute.hasModifier(modifierId);
    }
}