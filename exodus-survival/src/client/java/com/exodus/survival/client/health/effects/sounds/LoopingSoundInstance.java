package com.exodus.survival.client.health.effects.sounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Зацикленный звук который играет пока не остановят
 * Используется для ambient эффектов (холод, жара)
 */
public class LoopingSoundInstance extends AbstractTickableSoundInstance {

    private boolean shouldStop = false;

    public LoopingSoundInstance(SoundEvent soundEvent, float volume, float pitch) {
        super(soundEvent, SoundSource.AMBIENT, RandomSource.create());

        this.looping = true;  // ✅ ЗАЦИКЛИВАНИЕ!
        this.delay = 0;
        this.volume = volume;
        this.pitch = pitch;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.relative = true; // Звук следует за игроком
    }

    @Override
    public void tick() {
        // Если получен сигнал остановки - плавно затухаем
        if (shouldStop) {
            this.volume *= 0.9f; // Затухание за ~10 тиков

            if (this.volume < 0.01f) {
                this.stop(); // Останавливаем
            }
        }
    }

    /**
     * Остановить звук (плавно)
     */
    public void fadeOut() {
        this.shouldStop = true;
    }

    /**
     * Обновить громкость
     */
    public void setVolume(float volume) {
        this.volume = volume;
    }
}
