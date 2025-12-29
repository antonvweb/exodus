package com.exodus.health.damage;

import com.exodus.core.api.player.BodyPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Определяет в какую часть тела попал урон
 * На основе позиции игрока и источника урона
 */
public class HitboxDetection {

    /**
     * Определить часть тела куда попал урон
     * @param player Игрок
     * @param attacker Атакующий (null если нет)
     * @return Часть тела куда попал урон
     */
    public static BodyPart detectHitBodyPart(Player player, Entity attacker) {
        if (attacker == null) {
            // Нет атакующего - случайная часть тела
            return getRandomBodyPart();
        }
        
        // Получаем позиции
        Vec3 playerPos = player.position();
        Vec3 attackerPos = attacker.position();
        
        // Вычисляем относительную высоту атаки
        double relativeHeight = attackerPos.y - playerPos.y;
        
        // Определяем часть тела по высоте атаки
        return detectByHeight(relativeHeight, player);
    }

    /**
     * Определить часть тела по высоте атаки
     */
    private static BodyPart detectByHeight(double relativeHeight, Player player) {
        // Высота игрока ~1.8 блока
        
        // ГОЛОВА: 1.5-1.8 блока (верхние 12.5%)
        if (relativeHeight > 1.5) {
            return BodyPart.HEAD;
        }
        
        // ТОРС: 0.8-1.5 блока (центральные 37.5%)
        if (relativeHeight > 0.8) {
            return BodyPart.TORSO;
        }
        
        // НОГИ/РУКИ: 0.0-0.8 блока (нижние 50%)
        // 50% руки, 50% ноги
        if (Math.random() < 0.5) {
            // Руки
            return Math.random() < 0.5 ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM;
        } else {
            // Ноги
            return Math.random() < 0.5 ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG;
        }
    }

    /**
     * Получить случайную часть тела с весами
     */
    private static BodyPart getRandomBodyPart() {
        double rand = Math.random();
        
        // Веса:
        // Голова: 10% (самая маленькая цель)
        // Торс: 40% (самая большая цель)
        // Руки: 25% (средние цели)
        // Ноги: 25% (средние цели)
        
        if (rand < 0.10) {
            return BodyPart.HEAD;
        } else if (rand < 0.50) {
            return BodyPart.TORSO;
        } else if (rand < 0.625) {
            return BodyPart.LEFT_ARM;
        } else if (rand < 0.75) {
            return BodyPart.RIGHT_ARM;
        } else if (rand < 0.875) {
            return BodyPart.LEFT_LEG;
        } else {
            return BodyPart.RIGHT_LEG;
        }
    }

    /**
     * Определить часть тела для падения (всегда ноги)
     */
    public static BodyPart getFallBodyPart() {
        // При падении урон всегда по ногам
        // 50/50 какая нога пострадает больше
        return Math.random() < 0.5 ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG;
    }

    /**
     * Определить части тела для взрыва (множественные)
     */
    public static BodyPart[] getExplosionBodyParts() {
        // Взрыв бьёт по нескольким частям тела
        // 2-4 части в зависимости от силы
        
        int count = 2 + (int)(Math.random() * 3); // 2-4 части
        BodyPart[] parts = new BodyPart[count];
        
        for (int i = 0; i < count; i++) {
            parts[i] = getRandomBodyPart();
        }
        
        return parts;
    }

    /**
     * Шанс попадания в голову (для стрел/снарядов)
     */
    public static boolean isHeadshot(double distance) {
        // Шанс попадания в голову зависит от расстояния
        
        if (distance < 10) {
            // Близко: 25% шанс
            return Math.random() < 0.25;
        } else if (distance < 20) {
            // Средне: 15% шанс
            return Math.random() < 0.15;
        } else {
            // Далеко: 5% шанс
            return Math.random() < 0.05;
        }
    }
}
