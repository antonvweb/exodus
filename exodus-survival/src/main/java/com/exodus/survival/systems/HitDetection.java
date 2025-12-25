package com.exodus.survival.systems;

import com.exodus.core.api.player.BodyPart;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * Система определения места попадания
 * НОВАЯ ВЕРСИЯ - учитывает угол и направление атаки
 */
public class HitDetection {

    private static final Random RANDOM = new Random();

    /**
     * Определить место попадания
     */
    public static BodyPart detectHitLocation(ServerPlayer player, DamageSource source) {
        Entity sourceEntity = source.getEntity();

        if (sourceEntity == null) {
            return getRandomBodyPart(); // Случайная часть для неизвестного источника
        }

        // Снаряды (стрелы, трезубцы)
        if (source.getDirectEntity() instanceof Projectile) {
            return detectProjectileHit(player, (Projectile) source.getDirectEntity());
        }

        // Ближний бой (мобы, игроки)
        if (sourceEntity instanceof LivingEntity attacker) {
            return detectMeleeHit(player, attacker);
        }

        return getRandomBodyPart();
    }

    /**
     * Определить попадание снаряда (ТОЧНОЕ)
     */
    private static BodyPart detectProjectileHit(ServerPlayer player, Projectile projectile) {
        Vec3 projectilePos = projectile.position();
        Vec3 playerPos = player.position();

        double playerHeight = player.getBbHeight();
        double relativeHeight = (projectilePos.y - playerPos.y) / playerHeight;

        // Снаряд летит точно - определяем по высоте
        if (relativeHeight >= 0.85) {
            return BodyPart.HEAD; // Попадание в голову
        } else if (relativeHeight >= 0.35) {
            // Торс или руки - зависит от горизонтального смещения
            double horizontalOffset = Math.abs(projectilePos.x - playerPos.x) +
                    Math.abs(projectilePos.z - playerPos.z);

            if (horizontalOffset > 0.3) {
                // Попадание сбоку - скорее всего рука
                return RANDOM.nextBoolean() ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM;
            } else {
                // Попадание в центр - торс
                return BodyPart.TORSO;
            }
        } else {
            // Низкое попадание - ноги
            return RANDOM.nextBoolean() ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG;
        }
    }

    /**
     * Определить попадание в ближнем бою (ВЕРОЯТНОСТНОЕ)
     */
    private static BodyPart detectMeleeHit(ServerPlayer player, LivingEntity attacker) {
        // 1. Определяем направление атаки
        AttackDirection direction = getAttackDirection(player, attacker);

        // 2. Определяем вертикальный угол
        AttackAngle angle = getAttackAngle(player, attacker);

        // 3. Выбираем часть тела на основе направления и угла
        return selectBodyPartByProbability(direction, angle);
    }

    /**
     * Направление атаки (спереди/сзади/сбоку)
     */
    private enum AttackDirection {
        FRONT,  // Спереди (±60°)
        BACK,   // Сзади (±60° от 180°)
        LEFT,   // Слева
        RIGHT   // Справа
    }

    /**
     * Угол атаки (сверху/нормально/снизу)
     */
    private enum AttackAngle {
        FROM_ABOVE,   // Атака сверху (атакующий выше на 1+ блок)
        NORMAL,       // Нормальная атака
        FROM_BELOW    // Атака снизу (атакующий ниже на 0.5+ блок)
    }

    /**
     * Определить направление атаки
     */
    private static AttackDirection getAttackDirection(ServerPlayer player, LivingEntity attacker) {
        Vec3 toAttacker = attacker.position().subtract(player.position()).normalize();
        Vec3 playerLook = player.getLookAngle();

        // Угол между взглядом игрока и направлением на атакующего
        double dotProduct = playerLook.x * toAttacker.x + playerLook.z * toAttacker.z;
        double angle = Math.acos(dotProduct);

        // Определяем с какой стороны атакующий
        double crossProduct = playerLook.x * toAttacker.z - playerLook.z * toAttacker.x;

        if (Math.abs(angle) < Math.PI / 3) {
            return AttackDirection.FRONT; // Спереди (±60°)
        } else if (Math.abs(angle) > 2 * Math.PI / 3) {
            return AttackDirection.BACK; // Сзади
        } else if (crossProduct > 0) {
            return AttackDirection.LEFT; // Слева
        } else {
            return AttackDirection.RIGHT; // Справа
        }
    }

    /**
     * Определить вертикальный угол атаки
     */
    private static AttackAngle getAttackAngle(ServerPlayer player, LivingEntity attacker) {
        double heightDifference = attacker.getEyeY() - player.getEyeY();

        if (heightDifference > 1.0) {
            return AttackAngle.FROM_ABOVE; // Атакующий сильно выше
        } else if (heightDifference < -0.5) {
            return AttackAngle.FROM_BELOW; // Атакующий ниже
        } else {
            return AttackAngle.NORMAL; // Примерно на одном уровне
        }
    }

    /**
     * Выбрать часть тела на основе вероятностей
     */
    private static BodyPart selectBodyPartByProbability(AttackDirection direction, AttackAngle angle) {
        // Атака сверху - приоритет голова
        if (angle == AttackAngle.FROM_ABOVE) {
            float roll = RANDOM.nextFloat() * 100;
            if (roll < 60) return BodyPart.HEAD;
            if (roll < 90) return BodyPart.TORSO;
            return RANDOM.nextBoolean() ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM;
        }

        // Атака снизу - приоритет ноги
        if (angle == AttackAngle.FROM_BELOW) {
            float roll = RANDOM.nextFloat() * 100;
            if (roll < 60) return RANDOM.nextBoolean() ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG;
            if (roll < 90) return BodyPart.TORSO;
            if (roll < 95) return RANDOM.nextBoolean() ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM;
            return BodyPart.HEAD;
        }

        // Нормальный угол - зависит от направления
        return switch (direction) {
            case FRONT -> selectFrontAttack();
            case BACK -> selectBackAttack();
            case LEFT -> selectSideAttack(BodyPart.LEFT_ARM, BodyPart.LEFT_LEG);
            case RIGHT -> selectSideAttack(BodyPart.RIGHT_ARM, BodyPart.RIGHT_LEG);
        };
    }

    /**
     * Атака спереди
     */
    private static BodyPart selectFrontAttack() {
        float roll = RANDOM.nextFloat() * 100;

        if (roll < 15) {
            return BodyPart.HEAD; // 15%
        } else if (roll < 65) {
            return BodyPart.TORSO; // 50%
        } else if (roll < 90) {
            // 25% - руки (случайная)
            return RANDOM.nextBoolean() ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM;
        } else {
            // 10% - ноги (случайная)
            return RANDOM.nextBoolean() ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG;
        }
    }

    /**
     * Атака сзади
     */
    private static BodyPart selectBackAttack() {
        float roll = RANDOM.nextFloat() * 100;

        if (roll < 5) {
            return BodyPart.HEAD; // 5%
        } else if (roll < 75) {
            return BodyPart.TORSO; // 70%
        } else if (roll < 90) {
            // 15% - руки
            return RANDOM.nextBoolean() ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM;
        } else {
            // 10% - ноги
            return RANDOM.nextBoolean() ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG;
        }
    }

    /**
     * Атака сбоку
     */
    private static BodyPart selectSideAttack(BodyPart arm, BodyPart leg) {
        float roll = RANDOM.nextFloat() * 100;

        if (roll < 10) {
            return BodyPart.HEAD; // 10%
        } else if (roll < 50) {
            return BodyPart.TORSO; // 40%
        } else if (roll < 90) {
            return arm; // 40% - рука со стороны атаки
        } else {
            return leg; // 10% - нога со стороны атаки
        }
    }

    /**
     * Случайная часть тела (для неизвестных источников)
     */
    private static BodyPart getRandomBodyPart() {
        BodyPart[] parts = BodyPart.values();
        return parts[RANDOM.nextInt(parts.length)];
    }
}