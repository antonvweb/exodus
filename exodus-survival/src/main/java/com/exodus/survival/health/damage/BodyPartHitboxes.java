package com.exodus.survival.health.damage;

import com.exodus.core.api.player.BodyPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Система хитбоксов для частей тела
 *
 * ОБНОВЛЕНИЕ: Добавлен raycast для точного определения попадания снарядов
 */
public class BodyPartHitboxes {

    private static final Map<BodyPart, AABB> HITBOXES = createHitboxes();

    private static Map<BodyPart, AABB> createHitboxes() {
        Map<BodyPart, AABB> hitboxes = new HashMap<>();

        // Голова
        hitboxes.put(BodyPart.HEAD,
                new AABB(-0.25, 1.5, -0.25, 0.25, 1.8, 0.25)
        );

        // Торс
        hitboxes.put(BodyPart.TORSO,
                new AABB(-0.3, 0.6, -0.2, 0.3, 1.5, 0.2)
        );

        // Левая рука
        hitboxes.put(BodyPart.LEFT_ARM,
                new AABB(-0.5, 0.6, -0.1, -0.3, 1.4, 0.1)
        );

        // Правая рука
        hitboxes.put(BodyPart.RIGHT_ARM,
                new AABB(0.3, 0.6, -0.1, 0.5, 1.4, 0.1)
        );

        // Левая нога
        hitboxes.put(BodyPart.LEFT_LEG,
                new AABB(-0.25, 0.0, -0.1, -0.05, 0.6, 0.1)
        );

        // Правая нога
        hitboxes.put(BodyPart.RIGHT_LEG,
                new AABB(0.05, 0.0, -0.1, 0.25, 0.6, 0.1)
        );

        return hitboxes;
    }

    /**
     * ⭐ НОВЫЙ МЕТОД: Определить часть тела куда попал СНАРЯД
     *
     * СПЕЦИАЛЬНАЯ ВЕРСИЯ ДЛЯ PROJECTILES:
     * Вычисляет приблизительную точку попадания через raycast
     *
     * ПОЧЕМУ НУЖЕН:
     * - projectile.position() в момент hurt() уже внутри игрока
     * - Нужно восстановить реальную точку попадания
     * - Используем raycast от снаряда к игроку
     *
     * @param player Игрок
     * @param projectilePos Позиция снаряда в момент hurt()
     * @return Часть тела
     */
    public static BodyPart detectProjectileHit(Player player, Vec3 projectilePos) {
        // 1. Вычисляем точку попадания
        Vec3 hitPoint = calculateProjectileHitPoint(player, projectilePos);

        // 2. Определяем часть тела через хитбоксы
        return detectHitBodyPart(player, hitPoint);
    }

    /**
     * ⭐ НОВЫЙ МЕТОД: Вычислить точку попадания снаряда
     *
     * КАК РАБОТАЕТ:
     * 1. Проводим луч от снаряда к центру игрока
     * 2. Находим точку пересечения с AABB игрока
     * 3. Эта точка = приблизительная точка попадания
     *
     * ПРИМЕР:
     * Снаряд на (10, 1.2, 5) → летел в игрока на (10.5, 0.5, 5)
     * Raycast найдет точку пересечения → (10.3, 0.5, 5) ← реальная точка попадания!
     *
     * @param player Игрок
     * @param projectilePos Позиция снаряда
     * @return Точка попадания (приблизительная)
     */
    private static Vec3 calculateProjectileHitPoint(Player player, Vec3 projectilePos) {
        // Получаем AABB игрока (его полный хитбокс)
        AABB playerBox = player.getBoundingBox();

        // Вычисляем центр игрока
        Vec3 playerCenter = new Vec3(
                (playerBox.minX + playerBox.maxX) / 2.0,
                (playerBox.minY + playerBox.maxY) / 2.0,
                (playerBox.minZ + playerBox.maxZ) / 2.0
        );

        // === ВАРИАНТ 1: Raycast от снаряда к центру игрока ===

        // AABB.clip() - встроенный метод Minecraft для ray-box intersection
        // Возвращает Optional<Vec3> - точку пересечения луча с боксом
        Optional<Vec3> hitPoint = playerBox.clip(projectilePos, playerCenter);

        if (hitPoint.isPresent()) {
            // ✅ Нашли точку пересечения!
            return hitPoint.get();
        }

        // === ВАРИАНТ 2: Снаряд уже внутри игрока (fallback) ===

        // Если clip() не нашел пересечение → снаряд уже внутри AABB
        // Зажимаем позицию снаряда внутри границ игрока
        return new Vec3(
                clamp(projectilePos.x, playerBox.minX, playerBox.maxX),
                clamp(projectilePos.y, playerBox.minY, playerBox.maxY),
                clamp(projectilePos.z, playerBox.minZ, playerBox.maxZ)
        );
    }

    /**
     * Вспомогательный метод: зажать значение в диапазон
     */
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Определить часть тела по мировым координатам
     * (используется для всех типов урона)
     *
     * @param player Игрок
     * @param worldHitPosition Точка урона в мировых координатах
     * @return Часть тела
     */
    public static BodyPart detectHitBodyPart(Player player, Vec3 worldHitPosition) {
        Vec3 playerPos = player.position();
        Vec3 relativeHit = worldHitPosition.subtract(playerPos);

        // Проверяем хитбоксы в порядке приоритета
        if (isInsideHitbox(relativeHit, BodyPart.HEAD)) {
            return BodyPart.HEAD;
        }

        if (isInsideHitbox(relativeHit, BodyPart.LEFT_ARM)) {
            return BodyPart.LEFT_ARM;
        }
        if (isInsideHitbox(relativeHit, BodyPart.RIGHT_ARM)) {
            return BodyPart.RIGHT_ARM;
        }

        if (isInsideHitbox(relativeHit, BodyPart.LEFT_LEG)) {
            return BodyPart.LEFT_LEG;
        }
        if (isInsideHitbox(relativeHit, BodyPart.RIGHT_LEG)) {
            return BodyPart.RIGHT_LEG;
        }

        if (isInsideHitbox(relativeHit, BodyPart.TORSO)) {
            return BodyPart.TORSO;
        }

        return BodyPart.TORSO;
    }

    private static boolean isInsideHitbox(Vec3 relativePoint, BodyPart part) {
        AABB hitbox = HITBOXES.get(part);
        if (hitbox == null) {
            return false;
        }

        return hitbox.contains(relativePoint);
    }

    public static Map<BodyPart, AABB> getWorldHitboxes(Player player) {
        Map<BodyPart, AABB> worldHitboxes = new HashMap<>();
        Vec3 playerPos = player.position();

        for (Map.Entry<BodyPart, AABB> entry : HITBOXES.entrySet()) {
            AABB worldBox = entry.getValue().move(playerPos);
            worldHitboxes.put(entry.getKey(), worldBox);
        }

        return worldHitboxes;
    }

    public static double getHitboxVolume(BodyPart part) {
        AABB hitbox = HITBOXES.get(part);
        if (hitbox == null) return 0.0;

        double width = hitbox.maxX - hitbox.minX;
        double height = hitbox.maxY - hitbox.minY;
        double depth = hitbox.maxZ - hitbox.minZ;

        return width * height * depth;
    }

    public static double getHitboxPercentage(BodyPart part) {
        double partVolume = getHitboxVolume(part);
        double totalVolume = 0.0;

        for (BodyPart p : BodyPart.values()) {
            totalVolume += getHitboxVolume(p);
        }

        return totalVolume > 0 ? partVolume / totalVolume : 0.0;
    }
}