package com.exodus.survival.client.health.effects;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;

import java.lang.reflect.Method;

/**
 * Простая тряска камеры при получении урона - КАК В ВАНИЛЬНОМ МАЙНКРАФТЕ
 * Камера просто немного качается вверх-вниз на короткое время
 */
public class CameraShake {

    // Параметры тряски
    private static float intensity = 0f;          // Сила тряски (0-1)
    private static long startTime = 0;            // Время начала
    private static final long DURATION = 300;     // Длительность 300мс (как в ванилле)

    // Текущее смещение камеры
    private static float currentOffset = 0f;

    // Reflection для доступа к Camera.setRotation()
    private static Method setRotationMethod = null;
    private static boolean reflectionFailed = false;

    /**
     * Добавить тряску камеры
     * @param damage количество урона (влияет на силу тряски)
     */
    public static void addShake(float damage) {
        // Сила тряски зависит от урона: 0.3-1.5 градуса
        // Даже маленький урон даёт небольшую тряску
        intensity = Mth.clamp(damage / 10.0f, 0.3f, 1.5f);
        startTime = System.currentTimeMillis();

        System.out.println("=== CAMERA SHAKE! Damage: " + damage + ", Intensity: " + intensity + " ===");
    }

    /**
     * Обновить тряску каждый кадр
     * Вызывается из CameraShakeMixin
     */
    public static void tick() {
        if (!isActive()) {
            currentOffset = 0f;
            return;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        float progress = (float) elapsed / DURATION; // 0.0 -> 1.0

        // Затухание (сначала сильно, потом слабее)
        float fade = 1.0f - progress;

        // Колебание (синусоида для плавности)
        // Частота: 3 колебания за время тряски
        float wave = (float) Math.sin(progress * Math.PI * 6);

        // Итоговое смещение pitch (в градусах)
        currentOffset = wave * intensity * fade;
    }

    /**
     * Применить смещение к камере
     * Использует reflection для доступа к Camera.setRotation()
     */
    public static void applyToCamera(Camera camera) {
        if (!isActive() || Math.abs(currentOffset) < 0.01f) {
            return;
        }

        if (reflectionFailed) {
            return;
        }

        try {
            // Инициализируем reflection один раз
            if (setRotationMethod == null) {
                setRotationMethod = Camera.class.getDeclaredMethod("setRotation", float.class, float.class);
                setRotationMethod.setAccessible(true);
                System.out.println("=== CAMERA SHAKE: Reflection initialized successfully ===");
            }

            // Применяем смещение к pitch (вверх-вниз)
            setRotationMethod.invoke(camera, camera.getYRot(), camera.getXRot() + currentOffset);

        } catch (Exception e) {
            System.err.println("=== CAMERA SHAKE: Reflection failed! ===");
            e.printStackTrace();
            reflectionFailed = true;
        }
    }

    /**
     * Проверить активна ли тряска
     */
    public static boolean isActive() {
        long elapsed = System.currentTimeMillis() - startTime;
        return elapsed < DURATION && intensity > 0;
    }

    /**
     * Получить текущее смещение (для отладки)
     */
    public static float getCurrentOffset() {
        return currentOffset;
    }
}