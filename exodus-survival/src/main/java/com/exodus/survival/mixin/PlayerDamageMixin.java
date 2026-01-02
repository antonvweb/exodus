package com.exodus.survival.mixin;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.attributes.AttributeType;
import com.exodus.core.api.player.BleedingType;
import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.player.PlayerHealthData;
import com.exodus.core.player.attributes.AttributeManager;
import com.exodus.survival.health.damage.DeathHandler;
import com.exodus.survival.health.damage.HitboxDetection;
import com.exodus.survival.health.network.DamagePacket;
import com.exodus.survival.health.network.FracturePacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin для перехвата получения урона игроком
 * Система 6 частей тела + мгновенная смерть от сильного урона
 */
@Mixin(Player.class)
public abstract class PlayerDamageMixin {

    @Unique
    private boolean exodus$isDying = false;

    @Inject(
            method = "hurt",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onPlayerHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;

        if (player.invulnerableTime > 0) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        if (player.isCreative() || player.isInvulnerable()) {
            return;
        }

        if (exodus$isDying) {
            return;
        }


        // ✅ МГНОВЕННАЯ СМЕРТЬ от сильного взрыва в упор (урон > 20)
        if ((source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION) ||
                source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_EXPLOSION)) &&
                amount > 20.0f) {

            player.sendSystemMessage(Component.literal("§c§l💥 Вы были разорваны взрывом!"));
            exodus$isDying = true;
            player.hurt(player.damageSources().generic(), 1000.0f);
            exodus$isDying = false;
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }

        // ✅ МГНОВЕННАЯ СМЕРТЬ от падения с огромной высоты (урон > 20)
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL) && amount > 20.0f) {
            player.sendSystemMessage(Component.literal("§c§l💀 Вы разбились при падении!"));
            exodus$isDying = true;
            player.hurt(player.damageSources().generic(), 1000.0f);
            exodus$isDying = false;
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }

        // ✅ ОПРЕДЕЛЯЕМ ЧАСТЬ ТЕЛА КУДА ПОПАЛ УРОН
        BodyPart hitPart = determineHitBodyPart(amount, source, player);

        // Наносим урон на эту часть тела
        ExodusCoreAPI.damageBodyPart(player, hitPart, amount);

        // ✅ Если торс уничтожен - запускаем таймер смерти
        if (hitPart == BodyPart.TORSO && ExodusCoreAPI.getBodyPartHP(player, BodyPart.TORSO) <= 0) {
            PlayerHealthData data = ExodusCoreAPI.getHealthData(player);
            if (!data.isTorsoDestroyed()) {
                data.startTorsoDeathTimer();
                player.sendSystemMessage(Component.literal("§c§l⚠ КРИТИЧЕСКОЕ ПОВРЕЖДЕНИЕ ТОРСА! Требуется срочное лечение!"));
            }
        }

        // ✅ ОТПРАВЛЯЕМ ПАКЕТ НА КЛИЕНТ
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new DamagePacket(amount));
        }

        // Отправляем сообщение в чат
        sendDamageMessage(player, source, amount, hitPart);

        // Добавляем статусные эффекты НА ЭТУ ЧАСТЬ ТЕЛА
        applyStatusEffects(player, source, amount, hitPart);

        // ✅ Устанавливаем hurtTime
        player.hurtTime = 10;
        player.hurtDuration = 10;

        // ✅ Применяем knockback
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

        // ПРОВЕРКА СМЕРТИ
        exodus$isDying = true;
        boolean shouldDie = DeathHandler.checkDeath(player, source);
        exodus$isDying = false;

        if (shouldDie) {
            return;
        }

        player.invulnerableTime = 20;

        // Отменяем ванильный урон
        cir.setReturnValue(true);
        cir.cancel();
    }

    /**
     * Определить часть тела куда попал урон
     */
    @Unique
    private BodyPart determineHitBodyPart(float amount, DamageSource source, Player player) {
        // ПАДЕНИЕ - всегда ноги
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
            return HitboxDetection.getFallBodyPart();
        }

        // ВЗРЫВ - множественные части (берём первую)
        if (source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION) ||
                source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_EXPLOSION)) {

            BodyPart[] parts = HitboxDetection.getExplosionBodyParts();
            // Наносим урон по всем частям
            for (int i = 1; i < parts.length; i++) {
                // Урон по дополнительным частям = % от основного урона
                float additionalDamage = amount * 0.3f; // 30% от основного
                ExodusCoreAPI.damageBodyPart(player, parts[i], additionalDamage);
            }
            return parts[0];
        }

        // ОБЫЧНАЯ АТАКА - определяем по хитбоксу
        Entity attacker = source.getEntity();
        return HitboxDetection.detectHitBodyPart(player, attacker);
    }

    /**
     * Наложение статусных эффектов на конкретную часть тела
     * С ЛИМИТАМИ: максимум 3 кровотечения, максимум 2 перелома
     */
    @Unique
    private void applyStatusEffects(Player player, DamageSource source, float damage, BodyPart hitPart) {

        // ✅ ГОЛОВА НЕ МОЖЕТ ИМЕТЬ ЭФФЕКТЫ
        if (hitPart == BodyPart.HEAD) {
            return;
        }

        // ✅ Подсчитываем текущие эффекты
        int currentBleedings = countActiveEffects(player, "bleeding");
        int currentFractures = countActiveEffects(player, "fracture");

        // ========== ПАДЕНИЕ ==========
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
            // При сильном падении (>5 урона) - перелом НОГ
            if (damage > 5.0f && currentFractures < 2) {
                float fractureChance = (float) (damage > 10.0f ? 1.0 : 0.5);
                float resistance = AttributeManager.getValue(player, AttributeType.FRACTURE_RESISTANCE);
                float finalChance = fractureChance * (1.0f - resistance);

                if (Math.random() < finalChance) {
                    float intensity = Math.min(1.0f, damage / 20.0f);

                    // Перелом той ноги куда попал урон
                    addFractureWithPain(player, hitPart, intensity);
                    currentFractures++;

                    float secondFractureChance = (float) (damage > 15.0f ? 0.3 : 0.2);
                    float finalSecondChance = secondFractureChance * (1.0f - resistance);

                    if (Math.random() < finalSecondChance && currentFractures < 2) {
                        BodyPart otherLeg = hitPart == BodyPart.LEFT_LEG ?
                                BodyPart.RIGHT_LEG : BodyPart.LEFT_LEG;
                        addFractureWithPain(player, otherLeg, intensity);
                    }
                }
            }
        }

        // ========== АТАКА МОБА ==========
        Entity attacker = source.getEntity();
        if (attacker != null && !source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION)) {

            // ✅ Шанс кровотечения зависит от урона И лимита
            if (currentBleedings < 3) {
                float bleedingChance;

                if (damage < 4.0f) {
                    bleedingChance = 0.2f;  // 20% при слабом уроне
                } else if (damage < 8.0f) {
                    bleedingChance = 0.4f;  // 40% при среднем уроне
                } else {
                    bleedingChance = 0.6f;  // 60% при сильном уроне
                }

                float resistance = AttributeManager.getValue(player, AttributeType.BLEED_RESISTANCE);
                float finalChance = bleedingChance * (1.0f - resistance);

                if (Math.random() < finalChance) {
                    // Тип кровотечения зависит от урона
                    BleedingType type;
                    if (damage < 5.0f) {
                        type = BleedingType.WEAK;      // Слабое
                    } else if (damage < 10.0f) {
                        type = BleedingType.MEDIUM;    // Среднее
                    } else {
                        type = BleedingType.STRONG;    // Сильное (БЕСКОНЕЧНОЕ!)
                    }

                    addBleedingWithPain(player, hitPart, type);
                }
            }

            if(currentFractures < 2){
              float fractureChance;

              if (damage < 8.0f) {
                fractureChance = 0.1f;  // 20% при слабом уроне
              } else if (damage < 12.0f) {
                fractureChance = 0.2f;  // 40% при среднем уроне
              } else {
                fractureChance = 0.4f;  // 60% при сильном уроне
              }

              float resistance = AttributeManager.getValue(player, AttributeType.FRACTURE_RESISTANCE);
              float finalChance = fractureChance * (1.0f - resistance);

                if (Math.random() < finalChance) {
                    float intensity = Math.min(1.0f, damage / 20.0f);

                    addFractureWithPain(player, hitPart, intensity);
                    currentFractures++;
                }
            }
        }

        // ========== ВЗРЫВ ==========
        if (source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION) ||
                source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_EXPLOSION)) {

            float intensity = Math.min(1.0f, damage / 15.0f);

            // Взрыв бьёт по нескольким частям
            BodyPart[] explosionParts = HitboxDetection.getExplosionBodyParts();

            // ✅ Более строгий баланс для взрыва
            int bleedingsAdded = 0;
            int fracturesAdded = 0;

            for (BodyPart part : explosionParts) {
                // Пропускаем голову
                if (part == BodyPart.HEAD) {
                    continue;
                }

                // ✅ Кровотечение: только если < 3 и с шансом зависящим от урона
                if (currentBleedings + bleedingsAdded < 3) {
                    float bleedingChance = damage < 10.0f ? 0.4f : 0.6f; // 40-60%
                    float resistance = AttributeManager.getValue(player, AttributeType.BLEED_RESISTANCE);
                    float finalChance = bleedingChance * (1.0f - resistance);

                    if (Math.random() < finalChance) {
                        BleedingType type = damage > 15.0f ? BleedingType.STRONG : BleedingType.MEDIUM;
                        addBleedingWithPain(player, part, type);
                        bleedingsAdded++;
                    }
                }

                // ✅ Перелом: только если < 2 и с шансом зависящим от урона
                if (currentFractures + fracturesAdded < 2) {
                    float fractureChance = damage < 10.0f ? 0.3f : 0.5f; // 30-50%
                    float resistance = AttributeManager.getValue(player, AttributeType.FRACTURE_RESISTANCE);
                    float finalChance = fractureChance * (1.0f - resistance);

                    if (Math.random() < finalChance) {
                        addFractureWithPain(player, part, intensity);
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
    private void addBleedingWithPain(Player player, BodyPart part, BleedingType type) {
        // 1. Добавляем кровотечение (БЕЗ автоматической боли)
        ExodusCoreAPI.addBleeding(player, part, type);

        // 2. Если кровотечение вызывает боль - добавляем вручную
        if (type.causesPain()) {
            // Базовая интенсивность
            float basePainIntensity = 0.6f;

            // Резист боли
            float painResist = AttributeManager.getValue(player, AttributeType.PAIN_RESISTANCE);

            // Финальная интенсивность
            float finalPainIntensity = basePainIntensity * (1.0f - painResist);

            // Длительность
            int duration = type.isInfinite() ?
                    PlayerHealthData.INFINITE_DURATION :
                    type.getRandomDuration() * 20;

            // Добавляем боль
            ExodusCoreAPI.addPain(player, duration, finalPainIntensity);
        }
    }

    /**
     * Добавить перелом С учётом боли и резистов
     */
    @Unique
    private void addFractureWithPain(Player player, BodyPart part, float intensity) {
        // 1. Добавляем перелом (БЕЗ автоматической боли)
        ExodusCoreAPI.addFracture(player, part, intensity);

        // ✅ ОТПРАВЛЯЕМ ПАКЕТ НА КЛИЕНТ ДЛЯ ЗВУКА ПЕРЕЛОМА
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(
                    serverPlayer,
                    new FracturePacket(part.name(), intensity)
            );
        }

        // 2. Вычисляем боль от перелома
        float basePainIntensity = intensity * 0.8f;
        float painResist = AttributeManager.getValue(player, AttributeType.PAIN_RESISTANCE);
        float finalPainIntensity = basePainIntensity * (1.0f - painResist);

        ExodusCoreAPI.addPain(player, PlayerHealthData.INFINITE_DURATION, finalPainIntensity);
    }

    /**
     * Подсчитать активные эффекты
     */
    @Unique
    private int countActiveEffects(Player player, String effectType) {
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

    @Unique
    private void sendDamageMessage(Player player, DamageSource source, float damage, BodyPart hitPart) {
        String sourceName = getDamageSourceName(source);
        String damageText = String.format("%.1f", damage);
        String message = "§c[Урон] §7" + sourceName + " нанёс §c" + damageText +
                " §7урона по §e" + hitPart.getDisplayName().toLowerCase();
        player.sendSystemMessage(Component.literal(message));
    }

    @Unique
    private String getDamageSourceName(DamageSource source) {
        Entity attacker = source.getEntity();

        if (attacker != null) {
            return attacker.getDisplayName().getString();
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
            return "Падение";
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.DROWN)) {
            return "Утопление";
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE) ||
                source.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE)) {
            return "Огонь";
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)) {
            return "Лава";
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.STARVE)) {
            return "Голод";
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION) ||
                source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_EXPLOSION)) {
            return "Взрыв";
        }

        return "Неизвестный источник";
    }
}