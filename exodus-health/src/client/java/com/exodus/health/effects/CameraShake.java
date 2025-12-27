package com.exodus.health.effects;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

/**
 * Откат камеры при получении урона - ИСПОЛЬЗУЕТ ВАНИЛЬНУЮ МЕХАНИКУ
 * Просто вызываем стандартную функцию поворота камеры при ударе
 */
public class CameraShake {

    private static final RandomSource RANDOM = RandomSource.create();

    /**
     * Сила отклонения камеры
     * Чем больше - тем сильнее отбрасывает камеру
     * РЕКОМЕНДУЕТСЯ: 0.1-0.5
     */
    private static final float KNOCKBACK_STRENGTH = 10f;

    /**
     * Добавить откат камеры - ВАНИЛЬНЫЙ СПОСОБ
     */
    public static void addShake(float damage) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        Player player = mc.player;

        // Вычисляем силу на основе урона
        float strength = damage * KNOCKBACK_STRENGTH;

        // Случайное направление (как в ванилле при ударе)
        float yaw = (RANDOM.nextFloat() - 0.5f) * strength;
        float pitch = (RANDOM.nextFloat() * 0.5f) * strength; // Вверх

        // ✅ ИСПОЛЬЗУЕМ ВАНИЛЬНУЮ ФУНКЦИЮ turn()
        // Это та же функция которую использует Minecraft при ударе
        player.turn(yaw, -pitch); // Минус чтобы камера шла вверх

        System.out.println("=== CAMERA KNOCKBACK! Damage: " + damage + ", Strength: " + strength + " ===");
    }

    /**
     * Применить к камере - НЕ НУЖНО, всё делает turn() сразу
     */
    public static void applyToCamera() {
        // Пусто - эффект применяется сразу в addShake()
    }

    public static boolean isActive() {
        return false; // Нет анимации - эффект мгновенный
    }
}