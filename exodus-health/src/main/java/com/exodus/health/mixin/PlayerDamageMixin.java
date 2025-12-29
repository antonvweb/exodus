package com.exodus.health.mixin;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.BleedingType;
import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.player.PlayerHealthData;
import com.exodus.health.damage.DeathHandler;
import com.exodus.health.damage.HitboxDetection;
import com.exodus.health.network.DamagePacket;
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
        BodyPart hitPart = determineHitBodyPart(source, player);

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

        // Отменяем ванильный урон
        cir.setReturnValue(true);
        cir.cancel();
    }

    /**
     * Определить часть тела куда попал урон
     */
    @Unique
    private BodyPart determineHitBodyPart(DamageSource source, Player player) {
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
                ExodusCoreAPI.damageBodyPart(player, parts[i], 5.0f);
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
                double fractureChance = damage > 10.0f ? 1.0 : 0.5;

                if (Math.random() < fractureChance) {
                    float intensity = Math.min(1.0f, damage / 20.0f);

                    // Перелом той ноги куда попал урон
                    ExodusCoreAPI.addFracture(player, hitPart, intensity);
                    currentFractures++;

                    // Возможно сломаем и вторую ногу
                    if (damage > 15.0f && Math.random() < 0.5f && currentFractures < 2) {
                        BodyPart otherLeg = hitPart == BodyPart.LEFT_LEG ?
                                BodyPart.RIGHT_LEG : BodyPart.LEFT_LEG;
                        ExodusCoreAPI.addFracture(player, otherLeg, intensity * 0.7f);
                    }

                    player.sendSystemMessage(Component.literal("§c⚠ У вас перелом! Требуется лечение."));
                }
            }
        }

        // ========== АТАКА МОБА ==========
        Entity attacker = source.getEntity();
        if (attacker != null && !source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION)) {

            // ✅ Шанс кровотечения зависит от урона И лимита
            if (currentBleedings < 3) {
                double bleedingChance;

                if (damage < 4.0f) {
                    bleedingChance = 0.2f;  // 20% при слабом уроне
                } else if (damage < 8.0f) {
                    bleedingChance = 0.4f;  // 40% при среднем уроне
                } else {
                    bleedingChance = 0.6f;  // 60% при сильном уроне
                }

                if (Math.random() < bleedingChance) {
                    // Тип кровотечения зависит от урона
                    BleedingType type;
                    if (damage < 5.0f) {
                        type = BleedingType.WEAK;      // Слабое
                    } else if (damage < 10.0f) {
                        type = BleedingType.MEDIUM;    // Среднее
                    } else {
                        type = BleedingType.STRONG;    // Сильное (БЕСКОНЕЧНОЕ!)
                    }

                    ExodusCoreAPI.addBleeding(player, hitPart, type);

                    if (type == BleedingType.STRONG) {
                        player.sendSystemMessage(Component.literal("§c§l⚠ СИЛЬНОЕ КРОВОТЕЧЕНИЕ! Требуется лечение!"));
                    }
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
                    double bleedingChance = damage < 10.0f ? 0.4f : 0.6f; // 40-60%

                    if (Math.random() < bleedingChance) {
                        // Сильное кровотечение только при большом уроне
                        BleedingType type = damage > 15.0f ? BleedingType.STRONG : BleedingType.MEDIUM;
                        ExodusCoreAPI.addBleeding(player, part, type);
                        bleedingsAdded++;
                    }
                }

                // ✅ Перелом: только если < 2 и с шансом зависящим от урона
                if (currentFractures + fracturesAdded < 2) {
                    double fractureChance = damage < 10.0f ? 0.3f : 0.5f; // 30-50%

                    if (Math.random() < fractureChance) {
                        ExodusCoreAPI.addFracture(player, part, intensity * 0.9f);
                        fracturesAdded++;
                    }
                }
            }

            if (bleedingsAdded > 0 || fracturesAdded > 0) {
                player.sendSystemMessage(Component.literal("§c§l⚠ Повреждения от взрыва!"));
            }
        }
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