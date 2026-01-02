package com.exodus.survival.client.health.effects;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;

import java.lang.reflect.Method;

/**
 * Непрерывная тряска камеры (дрожь от холода)
 * Камера быстро дёргается вверх-вниз со случайной амплитудой
 *
 * ОТЛИЧИЯ ОТ КРУЖЕНИЯ:
 * - Быстрые резкие движения (не плавные)
 * - Случайность (не предсказуемая синусоида)
 * - Pitch (вверх-вниз), не Yaw
 */
public class CameraShakeEffect {

    // Параметры эффекта
    private static boolean isActive = false;
    private static float intensity = 0f;           // Сила дрожи (в градусах, 0-2)

    // Параметры дрожи
    private static float currentXOffset = 0f;       // Текущее смещение
    private static float targetOffset = 0f;        // Целевое смещение
    private static long lastChangeTime = 0;        // Время последнего изменения
    private static final long CHANGE_INTERVAL = 50; // Менять направление каждые 50мс

    // Reflection для доступа к Camera.setRotation()
    private static Method setRotationMethod = null;
    private static boolean reflectionFailed = false;

    /**
     * Активировать дрожь
     * @param shakeIntensity сила дрожи (рекомендуется 0.5-2.0 градуса)
     */
    public static void activate(float shakeIntensity) {
        isActive = true;
        intensity = Mth.clamp(shakeIntensity, 0.5f, 2.0f);
        lastChangeTime = System.currentTimeMillis();
    }

    /**
     * Деактивировать дрожь
     */
    public static void deactivate() {
        if (isActive) {
            isActive = false;
            intensity = 0f;
            currentXOffset = 0f;
            targetOffset = 0f;
        }
    }

    /**
     * Обновить дрожь каждый кадр
     */
    public static void tick() {
        if (!isActive) {
            currentXOffset = 0f;
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastChangeTime >= CHANGE_INTERVAL) {
            targetOffset = (float) ((Math.random() - 0.5) * 2.0 * intensity);
            lastChangeTime = currentTime;
        }

        float lerpSpeed = 0.2f;
        currentXOffset = Mth.lerp(lerpSpeed, currentXOffset, targetOffset);
    }

    /**
     * Применить дрожь к камере
     */
    public static void applyToCamera(Camera camera) {
        if (!isActive || Math.abs(currentXOffset) < 0.01f) {
            return;
        }

        if (reflectionFailed) {
            return;
        }

        try {
            if (setRotationMethod == null) {
                setRotationMethod = Camera.class.getDeclaredMethod("setRotation", float.class, float.class);
                setRotationMethod.setAccessible(true);
            }

            setRotationMethod.invoke(camera,
                    camera.getYRot(),
                    camera.getXRot() + currentXOffset
            );

        } catch (Exception e) {
            e.printStackTrace();
            reflectionFailed = true;
        }
    }

    /**
     * Проверить активна ли дрожь
     */
    public static boolean isActive() {
        return isActive;
    }

    /**
     * Получить текущее смещение (для отладки)
     */
    public static float getcurrentXOffset() {
        return currentXOffset;
    }
}