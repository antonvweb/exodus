package com.exodus.core.api.player.health;

import com.exodus.core.api.attributes.AttributeType;
import com.exodus.core.player.attributes.AttributeManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Данные здоровья игрока - Tarkov-style система
 *
 * КЛЮЧЕВЫЕ МЕХАНИКИ:
 * 1. Head/Torso = 0 HP → instant death
 * 2. Конечности = 0 HP → "blacked" (дебафы + overflow damage)
 * 3. Overflow damage: урон по черной части распределяется на живые
 * 4. Кровотечение: урон идет на ТУ ЖЕ часть (может вызвать overflow)
 */
public class PlayerHealthData {

    public static final int INFINITE_DURATION = -1;

    // ============ HP ЧАСТЕЙ ТЕЛА ============
    private final Map<BodyPart, Float> bodyPartHP;

    // ============ ЭФФЕКТЫ НА ЧАСТИ ============
    private final Map<BodyPart, BleedingEffect> bleedingEffects;
    private final Map<BodyPart, FractureEffect> fractureEffects;

    // Боль: глобальный эффект
    private int painDuration;
    private float painIntensity;

    public PlayerHealthData() {
        this.bodyPartHP = new HashMap<>();
        this.bleedingEffects = new HashMap<>();
        this.fractureEffects = new HashMap<>();

        // Инициализируем HP
        for (BodyPart part : BodyPart.values()) {
            bodyPartHP.put(part, part.getMaxHP());
        }

        this.painDuration = 0;
        this.painIntensity = 0f;
    }

    // ============ ИНИЦИАЛИЗАЦИЯ С УЧЕТОМ АТРИБУТОВ ============

    /**
     * Инициализировать HP с учётом атрибутов (CON влияет на макс HP)
     * Вызывается при входе в мир
     */
    public void initializeHP(Player player) {
        for (BodyPart part : BodyPart.values()) {
            float maxHP = getMaxBodyPartHP(part, player);
            bodyPartHP.put(part, maxHP);
        }
    }

    // ============ HP ЧАСТЕЙ ТЕЛА ============

    public float getBodyPartHP(BodyPart part) {
        return bodyPartHP.getOrDefault(part, 0f);
    }

    /**
     * Получить максимальный HP части с учетом атрибутов
     */
    public float getMaxBodyPartHP(BodyPart part, Player player) {
        float baseMax = part.getMaxHP();
        float multiplier = AttributeManager.getValue(player, AttributeType.MAX_HEALTH_MULTIPLIER);
        return baseMax * multiplier;
    }

    /**
     * Установить HP части тела
     */
    public void setBodyPartHP(BodyPart part, float hp, Player player) {
        float max = getMaxBodyPartHP(part, player);
        bodyPartHP.put(part, Math.max(0, Math.min(max, hp)));
    }

    /**
     * ОСНОВНОЙ МЕТОД УРОНА - Tarkov-style
     *
     * КАК РАБОТАЕТ:
     * 1. Если часть жива (HP > 0) → обычный урон
     * 2. Если часть мертва (HP = 0) → overflow damage!
     * 3. Overflow: урон * multiplier → на другие части
     *
     * @param part Часть тела куда пришел урон
     * @param damage Количество урона
     * @param player Игрок
     */
    public void damageBodyPart(BodyPart part, float damage, Player player) {
        float currentHP = getBodyPartHP(part);

        // ========== СЛУЧАЙ 1: ЧАСТЬ ЖИВА ==========
        if (currentHP > 0) {
            float newHP = currentHP - damage;
            setBodyPartHP(part, newHP, player);

            // Проверяем: стала ли часть черной?
            if (newHP <= 0) {
                onLimbBlacked(part, player);
            }

            return;
        }

        // ========== СЛУЧАЙ 2: ЧАСТЬ УЖЕ ЧЕРНАЯ (BLACKED) ==========
        // Это overflow damage!

        // Проверка безопасности: критические части не должны быть живы с 0 HP
        if (part == BodyPart.HEAD || part == BodyPart.TORSO) {
            // Это означает что игрок должен быть мертв
            // Но на всякий случай просто игнорируем
            return;
        }

        // Применяем overflow damage
        applyOverflowDamage(part, damage, player);
    }

    /**
     * Overflow Damage - TARKOV МЕХАНИКА
     *
     * Урон по черной конечности распределяется на живые части
     *
     * ПРАВИЛА:
     * 1. Урон умножается на multiplier (1.5x по умолчанию)
     * 2. Распределяется ТОЛЬКО на живые конечности (не голову/торс!)
     * 3. Если ВСЕ конечности черные → урон идет на торс
     *
     * @param blackedPart Черная часть по которой пришел урон
     * @param damage Изначальный урон
     * @param player Игрок
     */
    private void applyOverflowDamage(BodyPart blackedPart, float damage, Player player) {
        // === ШАГ 1: Применяем множитель ===
        // В Tarkov это 0.7x - 2.0x в зависимости от части
        // Мы используем универсальный 1.5x (можно балансировать)
        float multiplier = 1.5f;
        float totalOverflowDamage = damage * multiplier;

        // === ШАГ 2: Находим живые конечности ===
        List<BodyPart> aliveLimbs = new ArrayList<>();

        for (BodyPart part : BodyPart.values()) {
            // Пропускаем критические части (голова/торс)
            if (part == BodyPart.HEAD || part == BodyPart.TORSO) {
                continue;
            }

            // Пропускаем черные конечности
            if (getBodyPartHP(part) <= 0) {
                continue;
            }

            aliveLimbs.add(part);
        }

        // === ШАГ 3: Распределяем урон ===

        // Если НЕТ живых конечностей → урон идет на торс!
        if (aliveLimbs.isEmpty()) {
            // Это критическая ситуация: все конечности черные
            // Overflow бьет по торсу → вероятно приведет к смерти
            damageBodyPart(BodyPart.TORSO, totalOverflowDamage, player);
            return;
        }

        // Есть живые конечности → делим урон поровну
        float damagePerLimb = totalOverflowDamage / aliveLimbs.size();

        for (BodyPart limb : aliveLimbs) {
            // Рекурсивно вызываем damageBodyPart
            // Это позволяет цепочке overflow работать корректно
            damageBodyPart(limb, damagePerLimb, player);
        }
    }

    /**
     * Callback когда конечность становится черной (HP = 0)
     *
     * АВТОМАТИЧЕСКИЕ ЭФФЕКТЫ:
     * - Конечность → автоматический перелом (максимальная интенсивность)
     * - Можно добавить звук, визуальный эффект
     */
    private void onLimbBlacked(BodyPart part, Player player) {
        // Только для конечностей (не голова/торс)
        if (part == BodyPart.LEFT_ARM || part == BodyPart.RIGHT_ARM ||
                part == BodyPart.LEFT_LEG || part == BodyPart.RIGHT_LEG) {

            // Автоматический перелом черной конечности
            addFracture(part, 1.0f); // Максимальная интенсивность
        }
    }

    /**
     * Восстановить HP части тела
     */
    public void healBodyPart(BodyPart part, float amount, Player player) {
        float currentHP = getBodyPartHP(part);
        setBodyPartHP(part, currentHP + amount, player);
    }

    /**
     * Получить процент HP части
     */
    public float getBodyPartHPPercentage(BodyPart part, Player player) {
        float current = getBodyPartHP(part);
        float max = getMaxBodyPartHP(part, player);
        return max > 0 ? Math.max(0f, Math.min(1f, current / max)) : 0f;
    }

    /**
     * Получить визуальное состояние части (для текстур)
     */
    public BodyPart.BodyPartState getBodyPartState(BodyPart part, Player player) {
        return part.getState(getBodyPartHPPercentage(part, player));
    }

    /**
     * Получить общий процент HP игрока
     * (среднее по всем частям)
     */
    public float getFullHp(Player player) {
        float totalPercentage = 0f;

        for (BodyPart part : BodyPart.values()) {
            totalPercentage += getBodyPartHPPercentage(part, player);
        }

        return totalPercentage / BodyPart.values().length;
    }

    // ============ ПРОВЕРКА ЖИЗНИ (TARKOV LOGIC) ============

    /**
     * Жив ли игрок?
     *
     * ПРАВИЛА СМЕРТИ (КАК В TARKOV):
     * 1. HEAD = 0 HP → instant death
     * 2. TORSO = 0 HP → instant death
     * 3. Все конечности = 0 HP → жив (но парализован)
     *
     * @return true если жив, false если мертв
     */
    public boolean isAlive() {
        // 1. Голова = 0 → мгновенная смерть
        if (getBodyPartHP(BodyPart.HEAD) <= 0) {
            return false;
        }

        // 2. Торс = 0 → мгновенная смерть (как в Tarkov!)
        if (getBodyPartHP(BodyPart.TORSO) <= 0) {
            return false;
        }

        // 3. Конечности = 0 → жив, но с дебафами
        // (не приводит к автоматической смерти)

        return true;
    }

    /**
     * Получить причину смерти
     */
    public DeathCause getDeathCause() {
        if (getBodyPartHP(BodyPart.HEAD) <= 0) {
            return DeathCause.HEAD_DESTROYED;
        }

        if (getBodyPartHP(BodyPart.TORSO) <= 0) {
            return DeathCause.TORSO_DESTROYED; // Instant death!
        }

        return DeathCause.UNKNOWN;
    }

    public enum DeathCause {
        HEAD_DESTROYED,        // Голова уничтожена
        TORSO_DESTROYED,       // Торс уничтожен (instant death!)
        BLEEDING,              // Истёк кровью (через overflow на торс)
        EXPLOSION,             // Сильный взрыв
        FALL,                  // Падение с высоты
        UNKNOWN
    }

    // ============ КРОВОТЕЧЕНИЕ ============

    /**
     * Добавить кровотечение на часть тела
     */
    public void addBleeding(BodyPart part, BleedingType type) {
        if (part == BodyPart.HEAD) {
            return; // Голова не кровоточит
        }

        // Проверяем есть ли уже кровотечение
        if (hasBleeding(part)) {
            BleedingEffect existing = bleedingEffects.get(part);

            // Усиливаем: берем более сильный тип
            BleedingType strongerType = getStrongerType(existing.type, type);
            float newDamage = Math.max(existing.damagePerSecond, type.getRandomDamage());

            // Обновляем длительность
            int newDuration = (existing.duration == INFINITE_DURATION || type.isInfinite())
                    ? INFINITE_DURATION
                    : Math.max(existing.duration, type.getRandomDuration() * 20);

            bleedingEffects.put(part, new BleedingEffect(newDuration, strongerType, newDamage));
        } else {
            // Новое кровотечение
            int duration = type.isInfinite() ? INFINITE_DURATION : type.getRandomDuration() * 20;
            bleedingEffects.put(part, new BleedingEffect(duration, type, type.getRandomDamage()));
        }
    }

    /**
     * Убрать кровотечение
     */
    public void removeBleeding(BodyPart part, Player player) {
        BleedingEffect effect = bleedingEffects.remove(part);

        // Если кровотечение вызывало боль → оставляем боль на некоторое время
        if (effect != null && effect.type.causesPain()) {
            int painAfter = effect.type.getPainDurationAfter() * 20;

            float basePainIntensity = 0.4f;
            float painResist = AttributeManager.getValue(player, AttributeType.PAIN_RESISTANCE);
            float finalPainIntensity = basePainIntensity * (1.0f - painResist);

            addPain(painAfter, finalPainIntensity);
        }
    }

    public boolean hasBleeding(BodyPart part) {
        BleedingEffect effect = bleedingEffects.get(part);
        if (effect == null) return false;
        return effect.duration == INFINITE_DURATION || effect.duration > 0;
    }

    public BleedingType getBleedingType(BodyPart part) {
        BleedingEffect effect = bleedingEffects.get(part);
        return effect != null ? effect.type : null;
    }

    public float getBleedingDamage(BodyPart part) {
        BleedingEffect effect = bleedingEffects.get(part);
        return effect != null ? effect.damagePerSecond : 0f;
    }

    private BleedingType getStrongerType(BleedingType a, BleedingType b) {
        if (a == BleedingType.STRONG || b == BleedingType.STRONG) return BleedingType.STRONG;
        if (a == BleedingType.MEDIUM || b == BleedingType.MEDIUM) return BleedingType.MEDIUM;
        return BleedingType.WEAK;
    }

    // ============ ПЕРЕЛОМ ============

    public void addFracture(BodyPart part, float intensity) {
        if (hasFracture(part)) {
            FractureEffect existing = fractureEffects.get(part);
            float newIntensity = Math.min(1.0f, existing.intensity + intensity * 0.5f);
            fractureEffects.put(part, new FractureEffect(INFINITE_DURATION, newIntensity));
        } else {
            fractureEffects.put(part, new FractureEffect(INFINITE_DURATION, intensity));
        }
    }

    public void removeFracture(BodyPart part, Player player) {
        FractureEffect effect = fractureEffects.remove(part);

        if (effect != null) {
            int painAfter = (120 + (int)(Math.random() * 60)) * 20;

            float basePainIntensity = 0.5f;
            float painResist = AttributeManager.getValue(player, AttributeType.PAIN_RESISTANCE);
            float finalPainIntensity = basePainIntensity * (1.0f - painResist);

            addPain(painAfter, finalPainIntensity);
        }
    }

    public boolean hasFracture(BodyPart part) {
        FractureEffect effect = fractureEffects.get(part);
        return effect != null;
    }

    public float getFractureIntensity(BodyPart part) {
        FractureEffect effect = fractureEffects.get(part);
        return effect != null ? effect.intensity : 0f;
    }

    // ============ БОЛЬ ============

    public void addPain(int duration, float intensity) {
        if (intensity > this.painIntensity) {
            this.painIntensity = intensity;
        }

        if (duration == INFINITE_DURATION) {
            this.painDuration = INFINITE_DURATION;
        } else if (this.painDuration != INFINITE_DURATION) {
            this.painDuration = Math.max(this.painDuration, duration);
        }
    }

    public void removePain() {
        this.painDuration = 0;
        this.painIntensity = 0f;
    }

    public boolean hasPain() {
        return painDuration > 0 || painDuration == INFINITE_DURATION;
    }

    public float getPainIntensity() {
        return painIntensity;
    }

    // ============ ОБНОВЛЕНИЕ ЭФФЕКТОВ ============

    /**
     * Тикать эффекты (вызывается каждый тик)
     */
    public void tickEffects() {
        // Тикаем кровотечения
        bleedingEffects.entrySet().removeIf(entry -> {
            BleedingEffect effect = entry.getValue();

            if (effect.duration == INFINITE_DURATION) {
                return false;
            }

            effect.duration--;

            if (effect.duration <= 0) {
                return true;
            }
            return false;
        });

        // Тикаем боль
        if (painDuration > 0 && painDuration != INFINITE_DURATION) {
            painDuration--;
            if (painDuration <= 0) {
                painIntensity = 0f;
            }
        }
    }

    // ============ ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ ============

    private static class BleedingEffect {
        int duration;
        BleedingType type;
        float damagePerSecond;

        BleedingEffect(int duration, BleedingType type, float damagePerSecond) {
            this.duration = duration;
            this.type = type;
            this.damagePerSecond = damagePerSecond;
        }
    }

    private static class FractureEffect {
        int duration;
        float intensity;

        FractureEffect(int duration, float intensity) {
            this.duration = duration;
            this.intensity = intensity;
        }
    }

    // ============ NBT (БЕЗ ИЗМЕНЕНИЙ) ============

    public CompoundTag writeNbt(CompoundTag nbt) {
        // HP частей тела
        CompoundTag bodyPartsNbt = new CompoundTag();
        for (BodyPart part : BodyPart.values()) {
            bodyPartsNbt.putFloat(part.getId(), getBodyPartHP(part));
        }
        nbt.put("bodyParts", bodyPartsNbt);

        // Кровотечения
        CompoundTag bleedingsNbt = new CompoundTag();
        for (Map.Entry<BodyPart, BleedingEffect> entry : bleedingEffects.entrySet()) {
            CompoundTag effectNbt = new CompoundTag();
            effectNbt.putInt("duration", entry.getValue().duration);
            effectNbt.putString("type", entry.getValue().type.getId());
            effectNbt.putFloat("damage", entry.getValue().damagePerSecond);
            bleedingsNbt.put(entry.getKey().getId(), effectNbt);
        }
        nbt.put("bleedings", bleedingsNbt);

        // Переломы
        CompoundTag fracturesNbt = new CompoundTag();
        for (Map.Entry<BodyPart, FractureEffect> entry : fractureEffects.entrySet()) {
            CompoundTag effectNbt = new CompoundTag();
            effectNbt.putInt("duration", entry.getValue().duration);
            effectNbt.putFloat("intensity", entry.getValue().intensity);
            fracturesNbt.put(entry.getKey().getId(), effectNbt);
        }
        nbt.put("fractures", fracturesNbt);

        // Боль
        nbt.putInt("painDuration", painDuration);
        nbt.putFloat("painIntensity", painIntensity);

        return nbt;
    }

    public void readNbt(CompoundTag nbt) {
        // HP частей тела
        if (nbt.contains("bodyParts")) {
            CompoundTag bodyPartsNbt = nbt.getCompound("bodyParts");
            for (BodyPart part : BodyPart.values()) {
                if (bodyPartsNbt.contains(part.getId())) {
                    bodyPartHP.put(part, bodyPartsNbt.getFloat(part.getId()));
                }
            }
        }

        // Кровотечения
        if (nbt.contains("bleedings")) {
            CompoundTag bleedingsNbt = nbt.getCompound("bleedings");
            bleedingEffects.clear();

            for (BodyPart part : BodyPart.values()) {
                if (bleedingsNbt.contains(part.getId())) {
                    CompoundTag effectNbt = bleedingsNbt.getCompound(part.getId());
                    int duration = effectNbt.getInt("duration");
                    String typeId = effectNbt.getString("type");
                    float damage = effectNbt.getFloat("damage");

                    BleedingType type = BleedingType.valueOf(typeId.toUpperCase());
                    bleedingEffects.put(part, new BleedingEffect(duration, type, damage));
                }
            }
        }

        // Переломы
        if (nbt.contains("fractures")) {
            CompoundTag fracturesNbt = nbt.getCompound("fractures");
            fractureEffects.clear();

            for (BodyPart part : BodyPart.values()) {
                if (fracturesNbt.contains(part.getId())) {
                    CompoundTag effectNbt = fracturesNbt.getCompound(part.getId());
                    int duration = effectNbt.getInt("duration");
                    float intensity = effectNbt.getFloat("intensity");

                    fractureEffects.put(part, new FractureEffect(duration, intensity));
                }
            }
        }

        // Боль
        if (nbt.contains("painDuration")) {
            painDuration = nbt.getInt("painDuration");
        }
        if (nbt.contains("painIntensity")) {
            painIntensity = nbt.getFloat("painIntensity");
        }
    }
}