package com.exodus.health.effects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

/**
 * Звуковые эффекты при получении урона
 */
public class DamageSounds {

    /**
     * Воспроизвести звук получения урона
     */
    public static void playHurtSound(float damage) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        // Громкость зависит от урона: 0.3 - 1.0
        float volume = Mth.clamp(damage / 10.0f, 0.3f, 1.0f);

        // Pitch (высота звука) зависит от урона: 0.8 - 1.2
        // Больше урон = ниже звук
        float pitch = Mth.clamp(1.2f - (damage / 20.0f), 0.8f, 1.2f);

        // Используем ванильный звук удара игрока
        mc.getSoundManager().play(
                SimpleSoundInstance.forLocalAmbience(
                        SoundEvents.PLAYER_HURT,
                        pitch,
                        volume
                )
        );

        System.out.println("=== PLAYED HURT SOUND! Volume: " + volume + ", Pitch: " + pitch + " ===");
    }

    /**
     * Воспроизвести звук критического урона (тяжёлый удар)
     */
    public static void playCriticalHurtSound(float damage) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        // Для критического урона используем более тяжёлый звук
        float volume = Mth.clamp(damage / 10.0f, 0.5f, 1.0f);
        float pitch = 0.7f; // Низкий звук для тяжёлого удара

        mc.getSoundManager().play(
                SimpleSoundInstance.forLocalAmbience(
                        SoundEvents.PLAYER_HURT,
                        pitch,
                        volume
                )
        );

        System.out.println("=== PLAYED CRITICAL HURT SOUND! ===");
    }
}