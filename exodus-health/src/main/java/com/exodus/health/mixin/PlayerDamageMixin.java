package com.exodus.health.mixin;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.PlayerHealthData;
import com.exodus.core.api.player.StatusEffect;
import com.exodus.health.damage.DeathHandler;
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

        // Пропускаем если это креативный режим или игрок неуязвим
        if (player.isCreative() || player.isInvulnerable()) {
            return;
        }

        // Пропускаем если уже умираем
        if (exodus$isDying) {
            return;
        }

        System.out.println("=== PLAYER HURT! Source: " + source.getMsgId() + ", Amount: " + amount + " ===");

        // Наносим урон в нашу систему
        ExodusCoreAPI.damage(player, amount);

        // ✅ ОТПРАВЛЯЕМ ПАКЕТ НА КЛИЕНТ ДЛЯ ВИЗУАЛЬНЫХ ЭФФЕКТОВ
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            System.out.println("=== SENDING DAMAGE PACKET TO CLIENT... ===");
            ServerPlayNetworking.send(serverPlayer, new DamagePacket(amount));
        }

        // Отправляем сообщение в чат
        sendDamageMessage(player, source, amount);

        // Добавляем статусные эффекты
        applyStatusEffects(player, source, amount);

        // ✅ Устанавливаем hurtTime (для ванильной анимации и красной модели)
        player.hurtTime = 10;
        player.hurtDuration = 10;

        // ✅ Применяем откидывание (knockback) если есть атакующий
        Entity attacker = source.getEntity();
        if (attacker != null) {
            // Вычисляем направление от атакующего к игроку
            double dx = player.getX() - attacker.getX();
            double dz = player.getZ() - attacker.getZ();

            // Нормализуем вектор
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > 0) {
                dx /= distance;
                dz /= distance;
            }

            // Сила отталкивания (0.4 = стандартная ванильная)
            double strength = 0.4;

            // Применяем velocity напрямую
            player.setDeltaMovement(
                    player.getDeltaMovement().add(
                            dx * strength,
                            0.1, // Небольшой подъём вверх
                            dz * strength
                    )
            );

            // ✅ Важно! Обновляем движение на клиенте
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
     * Наложение статусных эффектов в зависимости от типа урона
     */
    private void applyStatusEffects(Player player, DamageSource source, float damage) {

        // ========== ПАДЕНИЕ ==========
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
            // При сильном падении (>5 урона) - перелом
            if (damage > 5.0f) {
                // 50% шанс перелома при урон 5-10 HP
                // 100% шанс при уроне >10 HP
                double fractureChance = damage > 10.0f ? 1.0 : 0.5;

                if (Math.random() < fractureChance) {
                    float intensity = Math.min(1.0f, damage / 20.0f);

                    // ✅ ПЕРЕЛОМ - БЕСКОНЕЧНАЯ ДЛИТЕЛЬНОСТЬ
                    ExodusCoreAPI.getHealthComponent(player).addEffect(
                            StatusEffect.FRACTURE,
                            PlayerHealthData.INFINITE_DURATION, // Бесконечный эффект
                            intensity
                    );

                    player.sendSystemMessage(Component.literal("§c⚠ У вас перелом! Требуется лечение."));
                }
            }
        }

        // ========== АТАКА МОБА ==========
        Entity attacker = source.getEntity();
        if (attacker != null && !source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION)) {

            // 40% шанс кровотечения от обычной атаки
            if (Math.random() < 0.4f) {
                // ✅ Длительность 60-120 секунд (увеличено с 20-40)
                int duration = 60 + (int) (Math.random() * 60);
                float intensity = Math.min(0.8f, damage / 20.0f);

                ExodusCoreAPI.addEffect(player, StatusEffect.BLEEDING, duration, intensity);
            }
        }

        // ========== ВЗРЫВ ==========
        if (source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION) ||
                source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_EXPLOSION)) {

            float intensity = Math.min(1.0f, damage / 15.0f);

            // 80% шанс кровотечения от взрыва (увеличено с 70%)
            if (Math.random() < 0.8f) {
                // ✅ Длительность 90-150 секунд (серьёзное кровотечение)
                int duration = 90 + (int) (Math.random() * 60);
                ExodusCoreAPI.addEffect(player, StatusEffect.BLEEDING, duration, intensity);
            }

            // 60% шанс перелома от взрыва (увеличено с 50%)
            if (Math.random() < 0.6f) {
                // ✅ ПЕРЕЛОМ - БЕСКОНЕЧНАЯ ДЛИТЕЛЬНОСТЬ
                ExodusCoreAPI.getHealthComponent(player).addEffect(
                        StatusEffect.FRACTURE,
                        PlayerHealthData.INFINITE_DURATION,
                        intensity * 0.9f
                );

                player.sendSystemMessage(Component.literal("§c⚠ У вас перелом! Требуется лечение."));
            }
        }

        // ✅ БОЛЬ НАКЛАДЫВАЕТСЯ АВТОМАТИЧЕСКИ в StatusEffectManager
        // Если есть кровотечение или перелом - боль появится сама
    }

    private void sendDamageMessage(Player player, DamageSource source, float damage) {
        String sourceName = getDamageSourceName(source);
        String damageText = String.format("%.1f", damage);
        String message = "§c[Урон] §7" + sourceName + " нанёс §c" + damageText + " §7урона";
        player.sendSystemMessage(Component.literal(message));
    }

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