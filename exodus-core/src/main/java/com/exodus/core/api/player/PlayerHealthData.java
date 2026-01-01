package com.exodus.core.api.player;

import com.exodus.core.api.attributes.AttributeType;
import com.exodus.core.player.attributes.AttributeManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Данные здоровья игрока - система 6 частей тела
 * Упрощённая механика смерти + переливание урона от кровотечения
 */
public class PlayerHealthData {

    // Специальное значение для бесконечной длительности
    public static final int INFINITE_DURATION = -1;

    // ============ HP ДЛЯ КАЖДОЙ ЧАСТИ ТЕЛА ============
    private final Map<BodyPart, Float> bodyPartHP;

    // ============ ЭФФЕКТЫ НА КОНКРЕТНЫЕ ЧАСТИ ТЕЛА ============
    private final Map<BodyPart, BleedingEffect> bleedingEffects;
    private final Map<BodyPart, FractureEffect> fractureEffects;

    // Боль: ГЛОБАЛЬНЫЙ эффект
    private int painDuration;
    private float painIntensity;

    public PlayerHealthData() {
        this.bodyPartHP = new HashMap<>();
        this.bleedingEffects = new HashMap<>();
        this.fractureEffects = new HashMap<>();

        // Инициализируем HP для всех частей тела
        for (BodyPart part : BodyPart.values()) {
            bodyPartHP.put(part, part.getMaxHP());
        }

        this.painDuration = 0;
        this.painIntensity = 0f;
    }

    /**
     * Инициализировать HP с учётом атрибутов игрока
     * Вызывается ОДИН РАЗ при входе игрока в мир
     */
    public void initializeHP(Player player) {
        for (BodyPart part : BodyPart.values()) {
            float maxHP = getMaxBodyPartHP(part, player);
            bodyPartHP.put(part, maxHP); // Полное HP
        }
    }

    // ============ HP ЧАСТЕЙ ТЕЛА ============

    public float getBodyPartHP(BodyPart part) {
        return bodyPartHP.getOrDefault(part, 0f);
    }

    public float getMaxBodyPartHP(BodyPart part, Player player){
        float baseMax = part.getMaxHP(); // Базовое (35, 85, etc)
        float multiplier = AttributeManager.getValue(player, AttributeType.MAX_HEALTH_MULTIPLIER);
        return baseMax * multiplier;
    }

    public void setBodyPartHP(BodyPart part, float hp, Player player) {
        bodyPartHP.put(part, Math.max(0, Math.min(getMaxBodyPartHP(part, player), hp)));
    }

    public void damageBodyPart(BodyPart part, float damage, Player player) {
        float currentHP = getBodyPartHP(part);
        setBodyPartHP(part, currentHP - damage, player);
    }

    public void healBodyPart(BodyPart part, float amount, Player player) {
        float currentHP = getBodyPartHP(part);
        setBodyPartHP(part, currentHP + amount, player);

        // ✅ Если вылечили торс (HP > 0) - отменяем таймер смерти
        if (part == BodyPart.TORSO && currentHP <= 0 && getBodyPartHP(part) > 0) {
            cancelTorsoDeathTimer();
        }
    }

    public float getBodyPartHPPercentage(BodyPart part, Player player) {
        float current = getBodyPartHP(part);
        float max = getMaxBodyPartHP(part, player);
        return max > 0 ? Math.max(0f, Math.min(1f, current / max)) : 0f;
    }

    public BodyPart.BodyPartState getBodyPartState(BodyPart part, Player player) {
        return part.getState(getBodyPartHPPercentage(part, player));
    }

    // ============ ТАЙМЕР ТОРСА (ВНУТРЕННИЙ) ============

    private boolean torsoDestroyed = false;
    private int torsoDeathTimer = 0;        // Оставшееся время (в тиках)
    private int torsoDeathDuration = 0;     // Общая длительность таймера

    /**
     * Запустить таймер смерти торса
     * 20-30 минут случайно
     */
    public void startTorsoDeathTimer() {
        if (!torsoDestroyed) {
            torsoDestroyed = true;
            // 20-30 минут = 24000-36000 тиков
            torsoDeathDuration = 24000 + (int)(Math.random() * 12000);
            torsoDeathTimer = torsoDeathDuration;
        }
    }

    /**
     * Отменить таймер торса (если вылечили)
     */
    public void cancelTorsoDeathTimer() {
        if (torsoDestroyed) {
            torsoDestroyed = false;
            torsoDeathTimer = 0;
            torsoDeathDuration = 0;
        }
    }

    /**
     * Обновить таймер торса
     */
    public void tickTorsoDeathTimer() {
        if (torsoDestroyed && torsoDeathTimer > 0) {
            torsoDeathTimer--;
        }
    }

    /**
     * Получить шанс смерти от торса (0.0 - 1.0)
     * Прогрессирующий шанс в последние 5 минут
     */
    public float getTorsoDeathChance() {
        if (!torsoDestroyed || torsoDeathTimer > 6000) { // > 5 минут
            return 0f;
        }

        if (torsoDeathTimer <= 0) {
            return 1.0f; // 100% смерть
        }

        // Последние 5 минут: прогрессирующий шанс
        // 5:00 → 1%, 4:00 → 2%, 3:00 → 5%, 2:00 → 10%, 1:00 → 20%, 0:30 → 50%
        int secondsLeft = torsoDeathTimer / 20;

        if (secondsLeft > 240) {      // 5:00-4:01
            return 0.01f;
        } else if (secondsLeft > 180) { // 4:00-3:01
            return 0.02f;
        } else if (secondsLeft > 120) { // 3:00-2:01
            return 0.05f;
        } else if (secondsLeft > 60) {  // 2:00-1:01
            return 0.10f;
        } else if (secondsLeft > 30) {  // 1:00-0:31
            return 0.20f;
        } else {                        // 0:30-0:00
            return 0.50f;
        }
    }

    public boolean isTorsoDestroyed() {
        return torsoDestroyed;
    }

    public int getTorsoTimeLeft() {
        return torsoDeathTimer / 20; // В секундах
    }

    // ============ ПРОВЕРКА ЖИЗНИ ============

    /**
     * Проверить жив ли игрок
     *
     * ПРАВИЛА СМЕРТИ:
     * 1. Голова = 0 HP → мгновенная смерть
     * 2. Торс = 0 HP → таймер 20-30 мин, потом прогрессирующий шанс смерти
     * 3. ВСЕ части = 0 HP → смерть
     */
    public boolean isAlive() {
        // 1. Голова = 0 → смерть
        if (getBodyPartHP(BodyPart.HEAD) <= 0) {
            return false;
        }

        // 2. Торс = 0 → проверяем таймер
        if (torsoDestroyed && torsoDeathTimer <= 0) {
            return false; // Таймер истёк
        }

        // 3. Все части тела = 0 → смерть
        boolean allDestroyed = true;
        for (BodyPart part : BodyPart.values()) {
            if (getBodyPartHP(part) > 0) {
                allDestroyed = false;
                break;
            }
        }
        if (allDestroyed) {
            return false;
        }

        return true;
    }

    /**
     * Получить причину смерти
     */
    public DeathCause getDeathCause() {
        if (getBodyPartHP(BodyPart.HEAD) <= 0) {
            return DeathCause.HEAD_DESTROYED;
        }

        if (torsoDestroyed && torsoDeathTimer <= 0) {
            return DeathCause.TORSO_FAILURE;
        }

        boolean allDestroyed = true;
        for (BodyPart part : BodyPart.values()) {
            if (getBodyPartHP(part) > 0) {
                allDestroyed = false;
                break;
            }
        }
        if (allDestroyed) {
            return DeathCause.ALL_BODY_DESTROYED;
        }

        return DeathCause.UNKNOWN;
    }

    public enum DeathCause {
        HEAD_DESTROYED,        // Голова уничтожена
        TORSO_FAILURE,         // Отказ внутренних органов (таймер торса)
        ALL_BODY_DESTROYED,    // Все части уничтожены
        BLEEDING,              // Истёк кровью
        EXPLOSION,             // Сильный взрыв в упор
        FALL,                  // Падение с большой высоты
        UNKNOWN                // Неизвестная причина
    }

    // ============ КРОВОТЕЧЕНИЕ ============

    public void addBleeding(BodyPart part, BleedingType type) {
        // ✅ Голова НЕ МОЖЕТ кровоточить
        if (part == BodyPart.HEAD) {
            return;
        }

        int duration;
        if (type.isInfinite()) {
            duration = INFINITE_DURATION; // Бесконечное
        } else {
            duration = type.getRandomDuration() * 20; // Секунды → тики
        }

        float damage = type.getRandomDamage();

        bleedingEffects.put(part, new BleedingEffect(duration, type, damage));
    }

    public void removeBleeding(BodyPart part, Player player) {
        BleedingEffect effect = bleedingEffects.remove(part);

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
        if (effect == null) {
            return false;
        }
        // Бесконечное или ещё не кончилось
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

    // ============ ПЕРЕЛОМ ============

    public void addFracture(BodyPart part, float intensity) {
        // ✅ Голова НЕ МОЖЕТ иметь перелом
        if (part == BodyPart.HEAD) {
            return;
        }

        fractureEffects.put(part, new FractureEffect(INFINITE_DURATION, intensity));
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
        return effect != null && (effect.duration == INFINITE_DURATION || effect.duration > 0);
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

    public void tickEffects() {
        // Обновляем кровотечения
        bleedingEffects.entrySet().removeIf(entry -> {
            BleedingEffect effect = entry.getValue();

            // Бесконечное кровотечение не тикается
            if (effect.duration == INFINITE_DURATION) {
                return false;
            }

            effect.duration--;

            if (effect.duration <= 0) {
                BodyPart part = entry.getKey();
                if (effect.type.causesPain()) {
                    int painAfter = effect.type.getPainDurationAfter() * 20;
                    addPain(painAfter, 0.4f);
                }
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

        // ✅ Тикаем таймер торса
        tickTorsoDeathTimer();
    }

    // ============ ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ ============

    private static class BleedingEffect {
        int duration;           // В тиках, INFINITE_DURATION = бесконечное
        BleedingType type;
        float damagePerSecond;

        BleedingEffect(int duration, BleedingType type, float damagePerSecond) {
            this.duration = duration;
            this.type = type;
            this.damagePerSecond = damagePerSecond;
        }
    }

    private static class FractureEffect {
        int duration;      // INFINITE_DURATION
        float intensity;   // 0.0 - 1.0

        FractureEffect(int duration, float intensity) {
            this.duration = duration;
            this.intensity = intensity;
        }
    }

    // ============ NBT ============

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

        // ✅ Таймер торса
        nbt.putBoolean("torsoDestroyed", torsoDestroyed);
        nbt.putInt("torsoDeathTimer", torsoDeathTimer);
        nbt.putInt("torsoDeathDuration", torsoDeathDuration);

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

        // ✅ Таймер торса
        if (nbt.contains("torsoDestroyed")) {
            torsoDestroyed = nbt.getBoolean("torsoDestroyed");
        }
        if (nbt.contains("torsoDeathTimer")) {
            torsoDeathTimer = nbt.getInt("torsoDeathTimer");
        }
        if (nbt.contains("torsoDeathDuration")) {
            torsoDeathDuration = nbt.getInt("torsoDeathDuration");
        }
    }
}