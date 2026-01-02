package com.exodus.survival.client.health.effects;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;

import java.lang.reflect.Method;

/**
 * Эффект кружения головы (вращение камеры)
 * Камера медленно крутится по кругу
 *
 * Используется для:
 * - Жара (перегрев)
 * - Головокружение от боли
 * - Низкое HP
 * - Наркотики/яды
 */
public class HeadSpinEffect {
    // Параметры эффекта
    private static boolean isActive = false;
    private static boolean isFadingOut = false;    // Флаг плавного отключения
    private static float intensity = 0f;           // Сила кружения (градусов отклонения)
    private static long startTime = 0;
    private static long fadeStartTime = 0;         // Время начала затухания

    // Текущий угол вращения
    private static float currentYawOffset = 0f;
    private static float currentPitchOffset = 0f;

    // Целевые смещения (для плавных переходов)
    private static float targetYawOffset = 0f;
    private static float targetPitchOffset = 0f;

    // Reflection для доступа к Camera.setRotation()
    private static Method setRotationMethod = null;
    private static boolean reflectionFailed = false;

    /**
     * Активировать эффект кружения
     * @param spinIntensity сила кружения (рекомендуется 5-15 градусов)
     */
    public static void activate(float spinIntensity) {
        isActive = true;
        isFadingOut = false; // Сбрасываем флаг затухания
        intensity = Mth.clamp(spinIntensity, 5.0f, 30.0f); // 5-30 градусов
        startTime = System.currentTimeMillis();
    }

    /**
     * Деактивировать эффект (температура вернулась к норме)
     */
    public static void deactivate() {
        if (!isActive) return;

        isFadingOut = true; // Включаем режим затухания
        fadeStartTime = System.currentTimeMillis();
        // Не сбрасываем isActive сразу - ждём завершения затухания
    }

    /**
     * Обновить кружение каждый кадр
     * Вызывается из HeadSpinMixin
     */
    public static void tick() {
        if (!isActive && !isFadingOut) {
            currentYawOffset = 0f;
            currentPitchOffset = 0f;
            targetYawOffset = 0f;
            targetPitchOffset = 0f;
            return;
        }

        // Время с начала эффекта (в секундах)
        float elapsed = (System.currentTimeMillis() - startTime) / 1000.0f;

        // Плавное круговое вращение
        // Полный оборот за 6 секунд (2 * Math.PI / 6 = ~1.047 рад/сек)
        float angle = elapsed * 1.047f;

        // Вычисляем целевые смещения по синусу (влево-вправо)
        float rawYawOffset = (float) Math.sin(angle) * intensity;
        float rawPitchOffset = (float) Math.cos(angle * 0.5f) * intensity * 0.3f;

        // Если в режиме затухания, плавно уменьшаем силу эффекта
        if (isFadingOut) {
            float fadeTime = (System.currentTimeMillis() - fadeStartTime) / 1000.0f;
            float fadeDuration = 1.5f; // Длительность затухания в секундах

            if (fadeTime >= fadeDuration) {
                // Затухание завершено - полностью отключаем эффект
                isActive = false;
                isFadingOut = false;
                intensity = 0f;
                targetYawOffset = 0f;
                targetPitchOffset = 0f;
            } else {
                // Плавно уменьшаем интенсивность
                float fadeProgress = fadeTime / fadeDuration;
                float fadeFactor = 1.0f - fadeProgress;

                targetYawOffset = rawYawOffset * fadeFactor;
                targetPitchOffset = rawPitchOffset * fadeFactor;
            }
        } else {
            // Обычный режим - полная интенсивность
            targetYawOffset = rawYawOffset;
            targetPitchOffset = rawPitchOffset;
        }

        // Плавная интерполяция текущих значений к целевым (для избежания резких скачков)
        float interpolationSpeed = 0.2f; // Скорость сглаживания (0-1)
        currentYawOffset = Mth.lerp(interpolationSpeed, currentYawOffset, targetYawOffset);
        currentPitchOffset = Mth.lerp(interpolationSpeed, currentPitchOffset, targetPitchOffset);

        // Если значения стали очень маленькими - обнуляем
        if (Math.abs(currentYawOffset) < 0.1f && Math.abs(currentPitchOffset) < 0.1f && !isActive) {
            currentYawOffset = 0f;
            currentPitchOffset = 0f;
        }
    }

    /**
     * Применить смещение к камере
     * Использует reflection для доступа к Camera.setRotation()
     */
    public static void applyToCamera(Camera camera) {
        // Если смещение очень маленькое - не применяем
        if (Math.abs(currentYawOffset) < 0.01f && Math.abs(currentPitchOffset) < 0.01f) {
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
            }

            // Применяем смещение к yaw (влево-вправо)
            setRotationMethod.invoke(camera,
                    camera.getYRot() + currentYawOffset,
                    camera.getXRot() + currentPitchOffset
            );

        } catch (Exception e) {
            e.printStackTrace();
            reflectionFailed = true;
        }
    }

    /**
     * Проверить активен ли эффект
     */
    public static boolean isActive() {
        return isActive || isFadingOut;
    }

    /**
     * Получить текущее смещение (для отладки)
     */
    public static float getCurrentOffset() {
        return currentYawOffset;
    }

    /**
     * Принудительно остановить эффект без плавности (например, при смерти)
     */
    public static void forceStop() {
        isActive = false;
        isFadingOut = false;
        intensity = 0f;
        currentYawOffset = 0f;
        currentPitchOffset = 0f;
        targetYawOffset = 0f;
        targetPitchOffset = 0f;
    }
}