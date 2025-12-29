package com.exodus.core.api.player;

import net.minecraft.nbt.CompoundTag;

/**
 * Данные жизненных показателей игрока
 * Динамические параметры, которые постоянно меняются
 */
public class PlayerVitalsData {

    // ============ ЖИЗНЕННЫЕ ПОКАЗАТЕЛИ ============
    
    // Сытость (0-100%)
    private float hunger;
    
    // Жажда (0-100%)
    private float thirst;
    
    // Энергия/Стамина (зависит от CON)
    private float energy;
    private float maxEnergy;
    
    // Кислород (зависит от INT, в секундах)
    private float oxygen;
    private float maxOxygen;
    
    // Температура тела (35.0-38.5°C, оптимум 36.5-37.5)
    private float temperature;
    
    // Психика/рассудок (0-100%, 100% = здоров)
    private float mental;

    public PlayerVitalsData() {
        // Инициализация с полными значениями
        this.hunger = 100.0f;
        this.thirst = 100.0f;
        this.energy = 150.0f; // Базовое, обновится при первом тике
        this.maxEnergy = 150.0f;
        this.oxygen = 400.0f; // Базовое
        this.maxOxygen = 400.0f;
        this.temperature = 37.0f; // Нормальная температура
        this.mental = 100.0f;
    }

    // ============ СЫТОСТЬ ============

    public float getHunger() {
        return hunger;
    }

    public void setHunger(float value) {
        this.hunger = Math.max(0, Math.min(100.0f, value));
    }

    public void addHunger(float amount) {
        setHunger(hunger + amount);
    }

    public float getHungerPercentage() {
        return hunger / 100.0f;
    }

    /**
     * Расход сытости в минуту
     */
    public float getHungerDrainRate(boolean running) {
        if (running) {
            return -1.5f; // При беге
        }
        return -0.5f; // Базовый
    }

    // ============ ЖАЖДА ============

    public float getThirst() {
        return thirst;
    }

    public void setThirst(float value) {
        this.thirst = Math.max(0, Math.min(100.0f, value));
    }

    public void addThirst(float amount) {
        setThirst(thirst + amount);
    }

    public float getThirstPercentage() {
        return thirst / 100.0f;
    }

    /**
     * Расход жажды в минуту
     */
    public float getThirstDrainRate(boolean running, boolean hot, boolean bleeding) {
        float rate = -0.8f; // Базовый
        
        if (running) {
            rate -= 1.2f; // -2% при беге
        }
        if (hot) {
            rate -= 0.7f; // -1.5% в жаре
        }
        if (bleeding) {
            rate -= 0.2f; // -1% при кровотечении
        }
        
        return rate;
    }

    // ============ ЭНЕРГИЯ/СТАМИНА ============

    public float getEnergy() {
        return energy;
    }

    public void setEnergy(float value) {
        this.energy = Math.max(0, Math.min(maxEnergy, value));
    }

    public void addEnergy(float amount) {
        setEnergy(energy + amount);
    }

    public float getMaxEnergy() {
        return maxEnergy;
    }

    public void setMaxEnergy(float max) {
        this.maxEnergy = Math.max(100, max);
        // Если текущая энергия больше нового максимума - обрезаем
        if (energy > maxEnergy) {
            energy = maxEnergy;
        }
    }

    public float getEnergyPercentage() {
        return maxEnergy > 0 ? energy / maxEnergy : 0;
    }

    /**
     * Можно ли выполнить действие
     */
    public boolean canPerformAction(float cost) {
        return energy >= cost;
    }

    /**
     * Потратить энергию
     */
    public boolean consumeEnergy(float cost) {
        if (canPerformAction(cost)) {
            addEnergy(-cost);
            return true;
        }
        return false;
    }

    /**
     * Скорость восстановления энергии
     */
    public float getEnergyRegenRate(boolean standing, boolean crouching) {
        if (crouching) {
            return 15.0f; // +15/сек при приседании
        } else if (standing) {
            return 10.0f; // +10/сек стоя/при ходьбе
        }
        return 0f; // Не восстанавливается при беге
    }

    // ============ КИСЛОРОД ============

    public float getOxygen() {
        return oxygen;
    }

    public void setOxygen(float value) {
        this.oxygen = Math.max(0, Math.min(maxOxygen, value));
    }

    public void addOxygen(float amount) {
        setOxygen(oxygen + amount);
    }

    public float getMaxOxygen() {
        return maxOxygen;
    }

    public void setMaxOxygen(float max) {
        this.maxOxygen = Math.max(300, max);
        if (oxygen > maxOxygen) {
            oxygen = maxOxygen;
        }
    }

    public float getOxygenPercentage() {
        return maxOxygen > 0 ? oxygen / maxOxygen : 0;
    }

    /**
     * Расход кислорода в секунду
     */
    public float getOxygenDrainRate(boolean inVacuum, boolean running, boolean inWater) {
        if (!inVacuum && !inWater) {
            return 0f; // В атмосфере не расходуется
        }
        
        float rate = 0f;
        
        if (inVacuum) {
            rate = -1.0f; // -1%/сек в вакууме
            if (running) {
                rate = -1.5f; // -1.5%/сек при беге
            }
        } else if (inWater) {
            rate = -2.0f; // -2%/сек в воде
        }
        
        return rate;
    }

    // ============ ТЕМПЕРАТУРА ============

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float value) {
        this.temperature = Math.max(34.0f, Math.min(40.0f, value));
    }

    public void addTemperature(float amount) {
        setTemperature(temperature + amount);
    }

    /**
     * Проверка температуры
     */
    public boolean isNormalTemperature() {
        return temperature >= 36.5f && temperature <= 37.5f;
    }

    public boolean isHypothermia() {
        return temperature < 36.0f;
    }

    public boolean isSevereHypothermia() {
        return temperature < 35.0f;
    }

    public boolean isHyperthermia() {
        return temperature > 37.6f;
    }

    public boolean isSevereHyperthermia() {
        return temperature > 38.5f;
    }

    /**
     * Изменение температуры в минуту
     */
    public float getTemperatureChangeRate(boolean cold, boolean hot) {
        if (cold) {
            return -0.1f; // -0.1°C/мин в холоде
        } else if (hot) {
            return 0.1f; // +0.1°C/мин в жаре
        }
        
        // Естественная регуляция к 37°C
        if (temperature < 37.0f) {
            return 0.05f;
        } else if (temperature > 37.0f) {
            return -0.05f;
        }
        
        return 0f;
    }

    // ============ ПСИХИКА/РАССУДОК ============

    public float getMental() {
        return mental;
    }

    public void setMental(float value) {
        this.mental = Math.max(0, Math.min(100.0f, value));
    }

    public void addMental(float amount) {
        setMental(mental + amount);
    }

    public float getMentalPercentage() {
        return mental / 100.0f;
    }

    /**
     * Изменение психики
     */
    public float getMentalChangeRate(
            boolean lowHP,
            boolean alone,
            boolean hungry,
            boolean safe
    ) {
        float rate = 0f;
        
        // Негативные факторы
        if (lowHP) {
            rate -= 1.0f; // -1%/мин при < 20% HP
        }
        if (alone) {
            rate -= 0.5f / 60.0f; // -0.5%/час одиночества
        }
        if (hungry) {
            rate -= 1.0f; // -1%/мин при голоде/жажде < 25%
        }
        
        // Позитивные факторы
        if (safe) {
            rate += 2.0f; // +2%/мин в безопасной зоне
        }
        
        return rate;
    }

    /**
     * Получить состояние психики
     */
    public MentalState getMentalState() {
        if (mental >= 75) {
            return MentalState.NORMAL;
        } else if (mental >= 50) {
            return MentalState.STRESSED;
        } else if (mental >= 25) {
            return MentalState.ANXIOUS;
        } else if (mental >= 10) {
            return MentalState.UNSTABLE;
        } else {
            return MentalState.BREAKING;
        }
    }

    public enum MentalState {
        NORMAL,      // 100-75%: норма
        STRESSED,    // 74-50%: легкий стресс
        ANXIOUS,     // 49-25%: тревога
        UNSTABLE,    // 24-10%: нестабильное состояние
        BREAKING     // 9-0%: на грани безумия
    }

    // ============ ОБЩИЕ МЕТОДЫ ============

    /**
     * Получить значение витала
     */
    public float getVital(VitalType type) {
        switch (type) {
            case HUNGER: return hunger;
            case THIRST: return thirst;
            case ENERGY: return energy;
            case OXYGEN: return oxygen;
            case TEMPERATURE: return temperature;
            case MENTAL: return mental;
            default: return 0f;
        }
    }

    /**
     * Установить значение витала
     */
    public void setVital(VitalType type, float value) {
        switch (type) {
            case HUNGER: setHunger(value); break;
            case THIRST: setThirst(value); break;
            case ENERGY: setEnergy(value); break;
            case OXYGEN: setOxygen(value); break;
            case TEMPERATURE: setTemperature(value); break;
            case MENTAL: setMental(value); break;
        }
    }

    /**
     * Получить максимальное значение витала
     */
    public float getMaxVital(VitalType type) {
        switch (type) {
            case HUNGER: return 100.0f;
            case THIRST: return 100.0f;
            case ENERGY: return maxEnergy;
            case OXYGEN: return maxOxygen;
            case TEMPERATURE: return 38.5f; // Максимум безопасный
            case MENTAL: return 100.0f;
            default: return 0f;
        }
    }

    /**
     * Получить процент витала
     */
    public float getVitalPercentage(VitalType type) {
        float current = getVital(type);
        float max = getMaxVital(type);
        return max > 0 ? current / max : 0f;
    }

    /**
     * Обновить максимумы на основе статов
     */
    public void updateMaxValues(PlayerStatsData stats) {
        // Энергия: 100 + (CON × 10)
        setMaxEnergy(stats.getMaxStamina());
        
        // Кислород: 300 + (INT × 20) секунд
        setMaxOxygen(stats.getMaxOxygen());
    }

    // ============ NBT ============

    public CompoundTag writeNbt(CompoundTag nbt) {
        nbt.putFloat("hunger", hunger);
        nbt.putFloat("thirst", thirst);
        nbt.putFloat("energy", energy);
        nbt.putFloat("maxEnergy", maxEnergy);
        nbt.putFloat("oxygen", oxygen);
        nbt.putFloat("maxOxygen", maxOxygen);
        nbt.putFloat("temperature", temperature);
        nbt.putFloat("mental", mental);
        return nbt;
    }

    public void readNbt(CompoundTag nbt) {
        if (nbt.contains("hunger")) hunger = nbt.getFloat("hunger");
        if (nbt.contains("thirst")) thirst = nbt.getFloat("thirst");
        if (nbt.contains("energy")) energy = nbt.getFloat("energy");
        if (nbt.contains("maxEnergy")) maxEnergy = nbt.getFloat("maxEnergy");
        if (nbt.contains("oxygen")) oxygen = nbt.getFloat("oxygen");
        if (nbt.contains("maxOxygen")) maxOxygen = nbt.getFloat("maxOxygen");
        if (nbt.contains("temperature")) temperature = nbt.getFloat("temperature");
        if (nbt.contains("mental")) mental = nbt.getFloat("mental");
    }
}
