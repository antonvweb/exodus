package com.exodus.survival.client.health.effects.sounds;

import com.exodus.survival.ExodusSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.Mth;

/**
 * Звуковые эффекты для статусов
 * Использует кастомные звуки Exodus
 */
public class StatusSounds {

    // Текущие зацикленные звуки
    private static LoopingSoundInstance hypothermiaSound = null;
    private static LoopingSoundInstance breathSound = null;

    // Таймеры для разовых звуков
    private static long lastShiverSound = 0;
    private static final long SHIVER_INTERVAL = 8000;

    // ============ ГИПОТЕРМИЯ (ХОЛОД) ============

    /**
     * Обновить ambient звук холода (зацикленный)
     */
    public static void updateHypothermiaAmbient(boolean shouldPlay, float temperature) {
        Minecraft mc = Minecraft.getInstance();

        if (shouldPlay) {
            // ✅ Если звук не играет - запустить
            if (hypothermiaSound == null || !mc.getSoundManager().isActive(hypothermiaSound)) {

                float severity = Mth.clamp((35.0f - temperature) / 2.0f, 0.3f, 1.0f);

                hypothermiaSound = new LoopingSoundInstance(
                        ExodusSounds.SHIVER,
                        severity * 0.4f, // Громкость (тихий фон)
                        1.0f
                );

                mc.getSoundManager().play(hypothermiaSound);
            } else {
                float severity = Mth.clamp((35.0f - temperature) / 2.0f, 0.3f, 1.0f);
                hypothermiaSound.setVolume(severity * 0.4f);
            }

        } else {
            if (hypothermiaSound != null) {
                hypothermiaSound.fadeOut();
                hypothermiaSound = null;
            }
        }
    }

    // ============ ГИПЕРТЕРМИЯ (ЖАРА) ============

    /**
     * Обновить звук тяжёлого дыхания (зацикленный)
     */
    public static void updateBreath(boolean shouldPlay, float severity) {
        Minecraft mc = Minecraft.getInstance();

        if (shouldPlay) {
            // ✅ Если звук не играет - запустить
            if (breathSound == null || !mc.getSoundManager().isActive(breathSound)) {
                breathSound = new LoopingSoundInstance(
                        ExodusSounds.BREATH,
                        severity,
                        1.0f
                );

                mc.getSoundManager().play(breathSound);
            } else {
                breathSound.setVolume(severity);
            }

        } else {
            if (breathSound != null) {
                breathSound.fadeOut();
                breathSound = null;
            }
        }
    }

    // ============ ПЕРЕЛОМ ============

    /**
     * Звук перелома костей (один раз)
     */
    public static void playFractureSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        mc.getSoundManager().play(
                SimpleSoundInstance.forLocalAmbience(
                        ExodusSounds.FRACTURE_BONES,
                        1.5f,
                        1.0f
                )
        );
    }

    // ============ ОСТАНОВКА ВСЕХ ЗВУКОВ ============

    /**
     * Остановить все зацикленные звуки
     * Вызывается при смерти/респавне/выходе
     */
    public static void stopAll() {
        if (hypothermiaSound != null) {
            hypothermiaSound.fadeOut();
            hypothermiaSound = null;
        }

        if (breathSound != null) {
            breathSound.fadeOut();
            breathSound = null;
        }

        lastShiverSound = 0;
    }
}
