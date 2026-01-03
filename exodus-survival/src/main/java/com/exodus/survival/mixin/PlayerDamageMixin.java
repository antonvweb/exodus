package com.exodus.survival.mixin;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.attributes.AttributeType;
import com.exodus.core.api.player.BleedingType;
import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.player.PlayerHealthData;
import com.exodus.core.player.attributes.AttributeManager;
import com.exodus.survival.health.damage.DeathHandler;
import com.exodus.survival.health.damage.BodyPartHitboxes;
import com.exodus.survival.health.damage.HitboxDetection;
import com.exodus.survival.health.network.DamagePacket;
import com.exodus.survival.health.network.FracturePacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin для перехвата урона игрока
 *
 * ОСНОВНЫЕ ИЗМЕНЕНИЯ:
 * 1. Точные хитбоксы для projectiles
 * 2. Weighted zones для melee
 * 3. Убран таймер торса (Tarkov-style: торс = 0 → instant death)
 * 4. Overflow damage встроен в PlayerHealthData
 */
@Mixin(Player.class)
public abstract class PlayerDamageMixin {

    /**
     * Флаг предотвращения рекурсии при смерти
     */
    @Unique
    private boolean exodus$isDying = false;

    /**
     * ГЛАВНЫЙ HOOK: перехват всего урона игрока
     *
     * КАК РАБОТАЕТ:
     * 1. Проверяем абсолютные источники смерти (/kill)
     * 2. Проверяем мгновенную смерть (взрыв >20 урона)
     * 3. Определяем часть тела через хитбоксы
     * 4. Применяем урон на эту часть
     * 5. Добавляем статусные эффекты
     * 6. Проверяем смерть
     * 7. Отменяем ванильный урон
     */
    @Inject(
            method = "hurt",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onPlayerHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;

        // ========== ПРОПУСКАЕМ НЕКОТОРЫЕ СЛУЧАИ ==========

        // 1. Абсолютные источники смерти (игнорируем систему частей тела)
        if (exodus$isAbsoluteDeath(source)) {
            return; // Пропускаем в ванильную обработку
        }

        // 2. Период неуязвимости
        if (player.invulnerableTime > 0) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        // 3. Creative или invulnerable
        if (player.isCreative() || player.isInvulnerable()) {
            return; // Пропускаем
        }

        // 4. Предотвращаем рекурсию при смерти
        if (exodus$isDying) {
            return;
        }

        // ========== МГНОВЕННАЯ СМЕРТЬ ОТ ЭКСТРЕМАЛЬНОГО УРОНА ==========

        // Сильный взрыв в упор (урон > 20) → instant death
        if ((source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION))
                && amount > 20.0f) {

            player.sendSystemMessage(Component.literal("§c§l💥 Вы были разорваны взрывом!"));
            exodus$isDying = true;
            player.hurt(player.damageSources().generic(), 1000.0f);
            exodus$isDying = false;
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }

        // Падение с огромной высоты (урон > 20) → instant death
        if (source.is(DamageTypes.FALL) && amount > 20.0f) {
            player.sendSystemMessage(Component.literal("§c§l💀 Вы разбились при падении!"));
            exodus$isDying = true;
            player.hurt(player.damageSources().generic(), 1000.0f);
            exodus$isDying = false;
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }

        // ========== ОПРЕДЕЛЯЕМ ЧАСТЬ ТЕЛА ==========

        BodyPart hitPart = exodus$determineHitBodyPart(source, player, amount);

        // ========== ПРИМЕНЯЕМ УРОН ==========

        ExodusCoreAPI.damageBodyPart(player, hitPart, amount);

        // ========== ПРОВЕРЯЕМ СМЕРТЬ ==========

        if (!ExodusCoreAPI.isAlive(player)) {
            player.setHealth(0); // Убиваем игрока
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }

        // ========== ОТПРАВЛЯЕМ ПАКЕТ НА КЛИЕНТ ==========

        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new DamagePacket(amount));
        }

        // ========== СООБЩЕНИЕ В ЧАТ ==========

        exodus$sendDamageMessage(player, source, amount, hitPart);

        // ========== ДОБАВЛЯЕМ СТАТУСНЫЕ ЭФФЕКТЫ ==========

        exodus$applyStatusEffects(player, source, amount, hitPart);

        // ========== УСТАНАВЛИВАЕМ HURT TIME ==========

        player.hurtTime = 10;
        player.hurtDuration = 10;

        // ========== KNOCKBACK ==========

        Entity attacker = source.getEntity();
        if (attacker != null) {
            double dx = player.getX() - attacker.getX();
            double dz = player.getZ() - attacker.getZ();

            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > 0) {
                dx /= distance;
                dz /= distance;
            }

            double strength = 0.4;

            player.setDeltaMovement(
                    player.getDeltaMovement().add(
                            dx * strength,
                            0.1,
                            dz * strength
                    )
            );

            player.hurtMarked = true;
        }

        // ========== ПРОВЕРКА СМЕРТИ (ПОВТОРНАЯ) ==========

        exodus$isDying = true;
        DeathHandler.checkDeath(player, source);
        exodus$isDying = false;

        // ========== ПЕРИОД НЕУЯЗВИМОСТИ ========== ⭐ УНИВЕРСАЛЬНАЯ ЛОГИКА

// === ИСКЛЮЧЕНИЯ: Дискретные события БЕЗ атакующего ===
// Эти типы урона НЕ имеют attacker, но наносятся дискретно (не каждый тик)
        boolean isDiscreteEvent =
                source.is(DamageTypes.FALL) ||              // Падение (1 раз)
                        source.is(DamageTypes.FLY_INTO_WALL) ||     // Элитры в стену (1 раз)
                        source.is(DamageTypes.EXPLOSION) ||         // Взрыв без источника (1 раз)
                        source.is(DamageTypes.PLAYER_EXPLOSION) ||  // Взрыв игрока (1 раз)
                        source.is(DamageTypes.LIGHTNING_BOLT) ||    // Молния (1 раз)
                        source.is(DamageTypes.STALAGMITE) ||        // Падение на сталагмит (1 раз)
                        source.is(DamageTypes.FALLING_BLOCK) ||     // Падающий блок (1 раз)
                        source.is(DamageTypes.FALLING_ANVIL) ||     // Падающая наковальня (1 раз)
                        source.is(DamageTypes.FALLING_STALACTITE);  // Падающий сталактит (1 раз)

        if (attacker != null || isDiscreteEvent) {
            // === ДИСКРЕТНЫЙ УРОН ===
            // - Есть атакующий (моб, игрок, снаряд)
            // - ИЛИ это разовое событие (падение, взрыв)
            // → Короткий iframes
            player.invulnerableTime = 3;

        } else {
            // === ПОСТОЯННЫЙ УРОН ===
            // - Нет атакующего И не разовое событие
            // - Скорее всего: кактус, огонь, куст, лава, кислота (из модов)
            // → Длинный iframes
            player.invulnerableTime = 20;
        }

// ========== ОТМЕНЯЕМ ВАНИЛЬНЫЙ УРОН ==========

        cir.setReturnValue(true);
        cir.cancel();
    }

    // ============ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ============

    /**
     * Проверить является ли источник урона "абсолютным"
     * (игнорирует систему частей тела)
     *
     * АБСОЛЮТНЫЕ ИСТОЧНИКИ:
     * - /kill
     * - Void (падение в пустоту)
     * - generic_kill
     */
    @Unique
    private boolean exodus$isAbsoluteDeath(DamageSource source) {
        return source.is(DamageTypes.GENERIC_KILL)
                || source.is(DamageTypes.FELL_OUT_OF_WORLD);
    }

    /**
     * КЛЮЧЕВОЙ МЕТОД: определение части тела куда пришел урон
     *
     * СТРАТЕГИЯ:
     * 1. FALL → ноги
     * 2. EXPLOSION → множественные части (первая = основной урон)
     * 3. PROJECTILES → точные хитбоксы (AABB)
     * 4. MELEE → weighted zones
     * 5. FALLBACK → торс
     *
     * @param source Источник урона
     * @param player Игрок
     * @param amount Количество урона
     * @return Часть тела
     *
     * Определение части тела куда пришел урон
     */
    @Unique
    private BodyPart exodus$determineHitBodyPart(DamageSource source, Player player, float amount) {

        // ========== ПАДЕНИЕ ==========
        if (source.is(DamageTypes.FALL)) {
            return HitboxDetection.getFallBodyPart();
        }

        // ========== ВЗРЫВ ==========
        if (source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION)) {
            BodyPart[] explosionParts = HitboxDetection.getExplosionBodyParts(amount);

            for (int i = 1; i < explosionParts.length; i++) {
                float additionalDamage = amount * 0.3f;
                ExodusCoreAPI.damageBodyPart(player, explosionParts[i], additionalDamage);
            }

            return explosionParts[0];
        }

        // ========== PROJECTILES: ВЫЧИСЛЯЕМ ТОЧКУ ПОПАДАНИЯ ========== ⭐ ИЗМЕНЕНО
        Entity directEntity = source.getDirectEntity();

        if (directEntity instanceof Projectile projectile) {
            // Используем специальный метод для снарядов
            Vec3 projectilePos = projectile.position();
            return BodyPartHitboxes.detectProjectileHit(player, projectilePos); // ← ИЗМЕНЕНО!
        }

        // ========== MELEE: WEIGHTED ZONES ==========
        Entity attacker = source.getEntity();

        if (attacker != null) {
            return HitboxDetection.detectHitBodyPart(player, attacker, null);
        }

        // ========== FALLBACK ==========
        return BodyPart.TORSO;
    }

    /**
     * Отправить сообщение о получении урона в чат игрока
     *
     * ФОРМАТ:
     * [Урон] Зомби нанёс 8.5 урона по Левой ноге
     */
    @Unique
    private void exodus$sendDamageMessage(Player player, DamageSource source, float damage, BodyPart hitPart) {
        String sourceName = exodus$getDamageSourceName(source);
        String damageText = String.format("%.1f", damage);

        String message = "§c[Урон] §7" + sourceName + " нанёс §c" + damageText
                + " §7урона по §e" + hitPart.getDisplayName().toLowerCase();

        player.sendSystemMessage(Component.literal(message));
    }

    /**
     * Получить название источника урона
     */
    @Unique
    private String exodus$getDamageSourceName(DamageSource source) {
        Entity attacker = source.getEntity();

        if (attacker != null) {
            return attacker.getDisplayName().getString();
        }

        if (source.is(DamageTypes.FALL)) return "Падение";
        if (source.is(DamageTypes.DROWN)) return "Утопление";
        if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)) return "Огонь";
        if (source.is(DamageTypes.LAVA)) return "Лава";
        if (source.is(DamageTypes.STARVE)) return "Голод";
        if (source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION)) return "Взрыв";

        return "Неизвестный источник";
    }

    /**
     * Применить статусные эффекты на часть тела
     *
     * ЛИМИТЫ (как в Tarkov):
     * - Максимум 3 кровотечения одновременно
     * - Максимум 2 перелома одновременно
     *
     * ПРАВИЛА:
     * - Голова НЕ получает эффекты
     * - Кровотечение зависит от урона
     * - Перелом зависит от урона
     */
    @Unique
    private void exodus$applyStatusEffects(Player player, DamageSource source, float damage, BodyPart hitPart) {

        // Голова НЕ получает эффекты
        if (hitPart == BodyPart.HEAD) {
            return;
        }

        // Подсчитываем текущие эффекты
        int currentBleedings = exodus$countActiveEffects(player, "bleeding");
        int currentFractures = exodus$countActiveEffects(player, "fracture");

        // ========== ПАДЕНИЕ ==========
        if (source.is(DamageTypes.FALL)) {
            // Сильное падение (>5 урона) → перелом ног
            if (damage > 5.0f && currentFractures < 2) {
                float fractureChance = damage > 10.0f ? 1.0f : 0.5f;
                float resistance = AttributeManager.getValue(player, AttributeType.FRACTURE_RESISTANCE);
                float finalChance = fractureChance * (1.0f - resistance);

                if (Math.random() < finalChance) {
                    float intensity = Math.min(1.0f, damage / 20.0f);

                    // Перелом той ноги куда попал урон
                    exodus$addFractureWithPain(player, hitPart, intensity);
                    currentFractures++;

                    // Шанс перелома второй ноги при очень сильном падении
                    if (damage > 15.0f && currentFractures < 2) {
                        float secondChance = (damage > 20.0f ? 0.3f : 0.2f) * (1.0f - resistance);

                        if (Math.random() < secondChance) {
                            BodyPart otherLeg = hitPart == BodyPart.LEFT_LEG
                                    ? BodyPart.RIGHT_LEG
                                    : BodyPart.LEFT_LEG;
                            exodus$addFractureWithPain(player, otherLeg, intensity);
                        }
                    }
                }
            }
            return; // Падение не вызывает кровотечение
        }

        // ========== АТАКА МОБА/ИГРОКА ==========
        Entity attacker = source.getEntity();

        if (attacker != null && !source.is(DamageTypes.EXPLOSION)) {

            // === КРОВОТЕЧЕНИЕ ===
            if (currentBleedings < 3) {
                float bleedingChance;

                if (damage < 4.0f) {
                    bleedingChance = 0.2f;      // 20% слабый урон
                } else if (damage < 8.0f) {
                    bleedingChance = 0.4f;      // 40% средний
                } else {
                    bleedingChance = 0.6f;      // 60% сильный
                }

                float resistance = AttributeManager.getValue(player, AttributeType.BLEED_RESISTANCE);
                float finalChance = bleedingChance * (1.0f - resistance);

                if (Math.random() < finalChance) {
                    BleedingType type;

                    if (damage < 5.0f) {
                        type = BleedingType.WEAK;
                    } else if (damage < 10.0f) {
                        type = BleedingType.MEDIUM;
                    } else {
                        type = BleedingType.STRONG; // Бесконечное!
                    }

                    exodus$addBleedingWithPain(player, hitPart, type);
                }
            }

            // === ПЕРЕЛОМ ===
            if (currentFractures < 2) {
                float fractureChance;

                if (damage < 8.0f) {
                    fractureChance = 0.1f;      // 10%
                } else if (damage < 12.0f) {
                    fractureChance = 0.2f;      // 20%
                } else {
                    fractureChance = 0.4f;      // 40%
                }

                float resistance = AttributeManager.getValue(player, AttributeType.FRACTURE_RESISTANCE);
                float finalChance = fractureChance * (1.0f - resistance);

                if (Math.random() < finalChance) {
                    float intensity = Math.min(1.0f, damage / 20.0f);
                    exodus$addFractureWithPain(player, hitPart, intensity);
                }
            }
        }

        // ========== ВЗРЫВ ==========
        if (source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION)) {

            float intensity = Math.min(1.0f, damage / 15.0f);

            // Взрыв бьет по нескольким частям → эффекты более строгие
            BodyPart[] explosionParts = HitboxDetection.getExplosionBodyParts(damage);

            int bleedingsAdded = 0;
            int fracturesAdded = 0;

            for (BodyPart part : explosionParts) {
                if (part == BodyPart.HEAD) continue;

                // Кровотечение
                if (currentBleedings + bleedingsAdded < 3) {
                    float bleedChance = (damage < 10.0f ? 0.4f : 0.6f);
                    float resistance = AttributeManager.getValue(player, AttributeType.BLEED_RESISTANCE);
                    float finalChance = bleedChance * (1.0f - resistance);

                    if (Math.random() < finalChance) {
                        BleedingType type = damage > 15.0f ? BleedingType.STRONG : BleedingType.MEDIUM;
                        exodus$addBleedingWithPain(player, part, type);
                        bleedingsAdded++;
                    }
                }

                // Перелом
                if (currentFractures + fracturesAdded < 2) {
                    float fracChance = (damage < 10.0f ? 0.3f : 0.5f);
                    float resistance = AttributeManager.getValue(player, AttributeType.FRACTURE_RESISTANCE);
                    float finalChance = fracChance * (1.0f - resistance);

                    if (Math.random() < finalChance) {
                        exodus$addFractureWithPain(player, part, intensity);
                        fracturesAdded++;
                    }
                }
            }
        }
    }

    /**
     * Добавить кровотечение С учётом боли и резистов
     */
    @Unique
    private void exodus$addBleedingWithPain(Player player, BodyPart part, BleedingType type) {
        // 1. Добавляем кровотечение
        ExodusCoreAPI.addBleeding(player, part, type);

        // 2. Если вызывает боль → добавляем боль
        if (type.causesPain()) {
            float basePainIntensity = 0.6f;
            float painResist = AttributeManager.getValue(player, AttributeType.PAIN_RESISTANCE);
            float finalPainIntensity = basePainIntensity * (1.0f - painResist);

            int duration = type.isInfinite()
                    ? PlayerHealthData.INFINITE_DURATION
                    : type.getRandomDuration() * 20;

            ExodusCoreAPI.addPain(player, duration, finalPainIntensity);
        }
    }

    /**
     * Добавить перелом С учётом боли и резистов
     */
    @Unique
    private void exodus$addFractureWithPain(Player player, BodyPart part, float intensity) {
        // 1. Добавляем перелом
        ExodusCoreAPI.addFracture(player, part, intensity);

        // 2. Отправляем пакет на клиент для звука
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(
                    serverPlayer,
                    new FracturePacket(part.name(), intensity)
            );
        }

        // 3. Добавляем боль от перелома
        float basePainIntensity = intensity * 0.8f;
        float painResist = AttributeManager.getValue(player, AttributeType.PAIN_RESISTANCE);
        float finalPainIntensity = basePainIntensity * (1.0f - painResist);

        ExodusCoreAPI.addPain(player, PlayerHealthData.INFINITE_DURATION, finalPainIntensity);
    }

    /**
     * Подсчитать активные эффекты (для лимитов)
     */
    @Unique
    private int exodus$countActiveEffects(Player player, String effectType) {
        PlayerHealthData data = ExodusCoreAPI.getHealthData(player);
        int count = 0;

        for (BodyPart part : BodyPart.values()) {
            if (effectType.equals("bleeding") && data.hasBleeding(part)) {
                count++;
            } else if (effectType.equals("fracture") && data.hasFracture(part)) {
                count++;
            }
        }

        return count;
    }
}