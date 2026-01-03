package com.exodus.survival.health.damage;

import com.exodus.core.api.player.BodyPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Определение части тела куда попал урон
 *
 * СТРАТЕГИЯ:
 * - Projectiles (стрелы) → точные хитбоксы (AABB)
 * - Melee (мобы) → weighted zones по высоте
 * - Fall → ноги
 * - Explosion → множественные части
 */
public class HitboxDetection {

    /**
     * ГЛАВНЫЙ МЕТОД: определить часть тела куда попал урон
     *
     * @param player Игрок
     * @param attacker Атакующий (может быть null)
     * @param hitPosition Точка попадания (если известна)
     * @return Часть тела
     */
    public static BodyPart detectHitBodyPart(Player player, Entity attacker, Vec3 hitPosition) {
        // === 1. Projectiles: точные хитбоксы ===
        if (attacker instanceof Projectile) {
            // У снарядов есть точная позиция попадания
            Vec3 projectilePos = attacker.position();
            return BodyPartHitboxes.detectHitBodyPart(player, projectilePos);
        }

        // === 2. Если передана точка попадания: используем её ===
        if (hitPosition != null) {
            return BodyPartHitboxes.detectHitBodyPart(player, hitPosition);
        }

        // === 3. Melee: определяем по высоте атакующего ===
        if (attacker != null) {
            return detectByHeightWeighted(player, attacker);
        }

        // === 4. Fallback: торс ===
        return BodyPart.TORSO;
    }

    /**
     * Определить часть тела для падения
     * При падении урон ВСЕГДА по ногам
     *
     * @return Случайная нога (50/50)
     */
    public static BodyPart getFallBodyPart() {
        return Math.random() < 0.5 ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG;
    }

    /**
     * Определить части тела для взрыва
     * Взрыв бьёт по МНОЖЕСТВЕННЫМ частям
     *
     * @param damage Сила взрыва (влияет на количество частей)
     * @return Массив пораженных частей (2-4 части)
     */
    public static BodyPart[] getExplosionBodyParts(float damage) {
        // Чем сильнее взрыв → больше частей поражено
        int count;
        if (damage > 20.0f) {
            count = 4; // Мощный взрыв → 4 части
        } else if (damage > 10.0f) {
            count = 3; // Средний → 3 части
        } else {
            count = 2; // Слабый → 2 части
        }

        BodyPart[] parts = new BodyPart[count];

        // Используем взвешенную случайность
        for (int i = 0; i < count; i++) {
            parts[i] = getRandomBodyPartWeighted();
        }

        return parts;
    }

    /**
     * Определение по высоте атакующего (для ближнего боя)
     *
     * WEIGHTED ZONES: не чистый RNG, а зоны с весами
     *
     * @param player Игрок
     * @param attacker Атакующий моб
     * @return Часть тела с учетом весов
     */
    private static BodyPart detectByHeightWeighted(Player player, Entity attacker) {
        // Вычисляем относительную высоту атаки
        // Используем eyeY (уровень глаз) атакующего
        double attackerEyeY = attacker.getEyeY();
        double playerY = player.getY();
        double relativeHeight = attackerEyeY - playerY;

        // === ЗОНА 1: Высокая атака (выше 1.5 блока) ===
        // Например: Enderman бьет сверху
        if (relativeHeight > 1.5) {
            // 60% голова, 40% торс
            return Math.random() < 0.6 ? BodyPart.HEAD : BodyPart.TORSO;
        }

        // === ЗОНА 2: Средняя атака (0.8 - 1.5 блока) ===
        // Например: Зомби, Скелет (обычная высота)
        if (relativeHeight > 0.8) {
            double roll = Math.random();

            if (roll < 0.50) {
                // 50% торс (основная цель)
                return BodyPart.TORSO;
            } else if (roll < 0.70) {
                // 20% левая рука
                return BodyPart.LEFT_ARM;
            } else if (roll < 0.90) {
                // 20% правая рука
                return BodyPart.RIGHT_ARM;
            } else {
                // 10% голова (случайный хедшот)
                return BodyPart.HEAD;
            }
        }

        // === ЗОНА 3: Низкая атака (ниже 0.8 блока) ===
        // Например: Spider, Baby Zombie
        if (relativeHeight > 0.3) {
            double roll = Math.random();

            if (roll < 0.40) {
                // 40% ноги
                return Math.random() < 0.5 ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG;
            } else if (roll < 0.70) {
                // 30% торс (нижняя часть)
                return BodyPart.TORSO;
            } else {
                // 30% руки
                return Math.random() < 0.5 ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM;
            }
        }

        // === ЗОНА 4: Очень низкая атака (у земли) ===
        // Например: Silverfish
        // 80% ноги, 20% торс
        return Math.random() < 0.8
                ? (Math.random() < 0.5 ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG)
                : BodyPart.TORSO;
    }

    /**
     * Получить случайную часть тела с реалистичными весами
     * Основано на размере хитбоксов
     *
     * ВЕСА (примерно соответствуют размерам):
     * - Голова: 12% (маленькая)
     * - Торс: 35% (большой)
     * - Руки: 26% (средние)
     * - Ноги: 27% (средние)
     */
    private static BodyPart getRandomBodyPartWeighted() {
        double roll = Math.random();

        // Веса основаны на объеме хитбоксов
        if (roll < 0.12) {
            return BodyPart.HEAD;           // 12%
        } else if (roll < 0.47) {
            return BodyPart.TORSO;          // 35%
        } else if (roll < 0.60) {
            return BodyPart.LEFT_ARM;       // 13%
        } else if (roll < 0.73) {
            return BodyPart.RIGHT_ARM;      // 13%
        } else if (roll < 0.865) {
            return BodyPart.LEFT_LEG;       // 13.5%
        } else {
            return BodyPart.RIGHT_LEG;      // 13.5%
        }
    }

    /**
     * Проверить headshot для снайперских попаданий
     * (опциональная механика для будущего)
     *
     * @param hitPart Часть куда попали
     * @param distance Расстояние выстрела
     * @return true если это headshot
     */
    public static boolean isHeadshot(BodyPart hitPart, double distance) {
        if (hitPart != BodyPart.HEAD) {
            return false;
        }

        // Headshot с большого расстояния = бонус
        // Можно добавить модификатор урона в будущем
        return true;
    }
}