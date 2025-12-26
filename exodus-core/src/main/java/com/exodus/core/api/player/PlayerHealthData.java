package com.exodus.core.api.player;

import net.minecraft.nbt.CompoundTag;
import java.util.HashMap;
import java.util.Map;

/**
 * Данные здоровья игрока (упрощённая версия)
 * Одна полоска HP + статусные эффекты
 */
public class PlayerHealthData {

    // ============ ПОКАЗАТЕЛИ ============
    private float currentHP;
    private float maxHP;
    private float speed;
    private float damage;
    private float jump;

    // ============ СТАТУСНЫЕ ЭФФЕКТЫ ============
    // Ключ - эффект, значение - время окончания (в тиках)
    private final Map<StatusEffect, Integer> activeEffects;
    
    // Интенсивность эффектов (0.0 - 1.0)
    private final Map<StatusEffect, Float> effectIntensity;

    public PlayerHealthData() {
        this.maxHP = 100.0f;
        this.currentHP = maxHP;
        this.activeEffects = new HashMap<>();
        this.effectIntensity = new HashMap<>();
    }

    // ============ HP ============

    public float getCurrentHP() {
        return currentHP;
    }

    public void setCurrentHP(float currentHP) {
        this.currentHP = Math.max(0, Math.min(maxHP, currentHP));
    }

    public float getMaxHP() {
        return maxHP;
    }

    public void setMaxHP(float maxHP) {
        this.maxHP = maxHP;
        this.currentHP = Math.min(currentHP, maxHP);
    }

    /**
     * Получить процент HP (0.0 - 1.0)
     */
    public float getHPPercentage() {
        if (maxHP <= 0) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, currentHP / maxHP));
    }

    /**
     * Нанести урон
     */
    public void damage(float amount) {
        currentHP = Math.max(0, currentHP - amount);
    }

    /**
     * Восстановить здоровье
     */
    public void heal(float amount) {
        currentHP = Math.min(maxHP, currentHP + amount);
    }

    /**
     * Жив ли игрок
     */
    public boolean isAlive() {
        return currentHP > 0;
    }

    // ============ СТАТУСНЫЕ ЭФФЕКТЫ ============

    /**
     * Добавить статусный эффект
     * @param effect эффект
     * @param duration длительность в тиках (20 тиков = 1 секунда)
     * @param intensity интенсивность (0.0 - 1.0)
     */
    public void addEffect(StatusEffect effect, int duration, float intensity) {
        activeEffects.put(effect, duration);
        effectIntensity.put(effect, Math.max(0f, Math.min(1f, intensity)));
    }

    /**
     * Убрать статусный эффект
     */
    public void removeEffect(StatusEffect effect) {
        activeEffects.remove(effect);
        effectIntensity.remove(effect);
    }

    /**
     * Проверить есть ли эффект
     */
    public boolean hasEffect(StatusEffect effect) {
        return activeEffects.containsKey(effect) && activeEffects.get(effect) > 0;
    }

    /**
     * Получить оставшееся время эффекта (в тиках)
     */
    public int getEffectDuration(StatusEffect effect) {
        return activeEffects.getOrDefault(effect, 0);
    }

    /**
     * Получить интенсивность эффекта (0.0 - 1.0)
     */
    public float getEffectIntensity(StatusEffect effect) {
        return effectIntensity.getOrDefault(effect, 0f);
    }

    /**
     * Получить все активные эффекты
     */
    public Map<StatusEffect, Integer> getActiveEffects() {
        return new HashMap<>(activeEffects);
    }

    /**
     * Обновить эффекты (вызывается каждый тик)
     */
    public void tickEffects() {
        // Уменьшаем таймеры
        activeEffects.entrySet().removeIf(entry -> {
            int newDuration = entry.getValue() - 1;
            if (newDuration <= 0) {
                effectIntensity.remove(entry.getKey());
                return true;
            }
            entry.setValue(newDuration);
            return false;
        });
    }

    // ============ NBT СОХРАНЕНИЕ/ЗАГРУЗКА ============

    public CompoundTag writeNbt(CompoundTag nbt) {
        nbt.putFloat("currentHP", currentHP);
        nbt.putFloat("maxHP", maxHP);

        // Сохраняем эффекты
        CompoundTag effectsNbt = new CompoundTag();
        for (Map.Entry<StatusEffect, Integer> entry : activeEffects.entrySet()) {
            CompoundTag effectNbt = new CompoundTag();
            effectNbt.putInt("duration", entry.getValue());
            effectNbt.putFloat("intensity", effectIntensity.getOrDefault(entry.getKey(), 0f));
            effectsNbt.put(entry.getKey().getId(), effectNbt);
        }
        nbt.put("effects", effectsNbt);

        return nbt;
    }

    public void readNbt(CompoundTag nbt) {
        if (nbt.contains("currentHP")) {
            this.currentHP = nbt.getFloat("currentHP");
        }
        if (nbt.contains("maxHP")) {
            this.maxHP = nbt.getFloat("maxHP");
        }

        // Загружаем эффекты
        if (nbt.contains("effects")) {
            CompoundTag effectsNbt = nbt.getCompound("effects");
            activeEffects.clear();
            effectIntensity.clear();

            for (StatusEffect effect : StatusEffect.values()) {
                if (effectsNbt.contains(effect.getId())) {
                    CompoundTag effectNbt = effectsNbt.getCompound(effect.getId());
                    int duration = effectNbt.getInt("duration");
                    float intensity = effectNbt.getFloat("intensity");
                    
                    activeEffects.put(effect, duration);
                    effectIntensity.put(effect, intensity);
                }
            }
        }
    }
}
