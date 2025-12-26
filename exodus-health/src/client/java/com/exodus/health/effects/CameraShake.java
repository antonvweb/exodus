package com.exodus.health.effects;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Система тряски камеры при получении урона - ПОЛНОСТЬЮ ПЕРЕПИСАННАЯ
 */
public class CameraShake {

    private static float shakeIntensity = 0.0f;
    private static long shakeStartTime = 0;
    private static final long SHAKE_DURATION = 300; // 0.3 секунды - быстрая и резкая

    private static final RandomSource RANDOM = RandomSource.create();
    private static float targetYaw = 0.0f;
    private static float targetPitch = 0.0f;
    private static float currentYaw = 0.0f;
    private static float currentPitch = 0.0f;

    /**
     * Добавить тряску камеры
     */
    public static void addShake(float damage) {
        shakeStartTime = System.currentTimeMillis();

        // Интенсивность от урона: 2.0 - 10.0
        shakeIntensity = Mth.clamp(damage * 1.2f, 2.0f, 10.0f);

        // Случайное направление тряски
        targetYaw = (RANDOM.nextFloat() - 0.5f) * 2.0f * shakeIntensity;
        targetPitch = (RANDOM.nextFloat() - 0.5f) * 1.5f * shakeIntensity;

        System.out.println("=== CAMERA SHAKE! Damage: " + damage + ", Intensity: " + shakeIntensity + " ===");
    }

    /**
     * Применить тряску к камере игрока (вызывается каждый кадр)
     */
    public static void applyToCamera() {
        if (!isShaking()) {
            currentYaw = 0.0f;
            currentPitch = 0.0f;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        long elapsed = System.currentTimeMillis() - shakeStartTime;
        float progress = (float) elapsed / SHAKE_DURATION;

        // Быстрое затухание (exponential decay)
        float decay = (float) Math.pow(1.0f - progress, 3.0);

        // Интерполяция к целевой позиции с отскоком
        float bounce = (float) Math.sin(progress * Math.PI * 3.0) * decay;

        currentYaw = targetYaw * bounce;
        currentPitch = targetPitch * bounce;

        // Применяем тряску
        if (currentYaw != 0.0f || currentPitch != 0.0f) {
            mc.player.turn(currentYaw * 0.1f, currentPitch * 0.1f);
        }
    }

    /**
     * Проверить идёт ли тряска
     */
    private static boolean isShaking() {
        return System.currentTimeMillis() - shakeStartTime < SHAKE_DURATION;
    }

    /**
     * Debug: проверка активности тряски
     */
    public static boolean isActive() {
        return isShaking();
    }
}