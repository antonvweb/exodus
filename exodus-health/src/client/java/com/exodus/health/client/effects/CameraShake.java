package com.exodus.health.client.effects;

import net.minecraft.util.Mth;

/**
 * Система тряски камеры при получении урона
 */
public class CameraShake {

    private static float shakeIntensity = 0.0f;
    private static long shakeStartTime = 0;
    private static final long SHAKE_DURATION = 500; // 0.5 секунды

    private static float baseYaw = 0.0f;
    private static float basePitch = 0.0f;

    /**
     * Добавить тряску камеры
     */
    public static void addShake(float damage) {
        shakeStartTime = System.currentTimeMillis();

        // Интенсивность зависит от урона (1.0 - 5.0) - УВЕЛИЧЕНО!
        shakeIntensity = Mth.clamp(damage * 0.5f, 1.0f, 5.0f);

        // Debug
        System.out.println("=== CAMERA SHAKE ADDED! Damage: " + damage + ", Intensity: " + shakeIntensity + " ===");
    }

    /**
     * Получить текущее смещение Yaw (горизонталь)
     */
    public static float getShakeYaw() {
        if (!isShaking()) {
            return 0.0f;
        }

        float progress = getShakeProgress();
        float decay = 1.0f - progress; // Затухание

        // Синусоидальная вибрация для более естественной тряски
        float time = (float) (System.currentTimeMillis() - shakeStartTime);
        float vibration = (float) Math.sin(time * 0.05f) * 2.0f; // УВЕЛИЧЕНО!

        return shakeIntensity * vibration * decay;
    }

    /**
     * Получить текущее смещение Pitch (вертикаль)
     */
    public static float getShakePitch() {
        if (!isShaking()) {
            return 0.0f;
        }

        float progress = getShakeProgress();
        float decay = 1.0f - progress;

        // Косинусоидальная вибрация (сдвиг фазы относительно Yaw)
        float time = (float) (System.currentTimeMillis() - shakeStartTime);
        float vibration = (float) Math.cos(time * 0.05f) * 2.0f; // УВЕЛИЧЕНО!

        return shakeIntensity * vibration * decay;
    }

    /**
     * Проверить идёт ли тряска
     */
    private static boolean isShaking() {
        return System.currentTimeMillis() - shakeStartTime < SHAKE_DURATION;
    }

    /**
     * Получить прогресс тряски (0.0 - 1.0)
     */
    private static float getShakeProgress() {
        long elapsed = System.currentTimeMillis() - shakeStartTime;
        return Mth.clamp((float) elapsed / SHAKE_DURATION, 0.0f, 1.0f);
    }

    /**
     * Debug: проверка активности тряски
     */
    public static boolean isActive() {
        return isShaking();
    }
}