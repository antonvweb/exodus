package com.exodus.health.mixin;

import com.exodus.core.ExodusCoreAPI;
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

        // Устанавливаем hurtTime (для ванильной анимации)
        player.hurtTime = 10;
        player.hurtDuration = 10;

        // Применяем откидывание (knockback) если есть атакующий
        Entity attacker = source.getEntity();
        if (attacker != null) {
            player.knockback(
                    0.4,
                    attacker.getX() - player.getX(),
                    attacker.getZ() - player.getZ()
            );
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

    private void applyStatusEffects(Player player, DamageSource source, float damage) {
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
            if (damage > 5.0f && Math.random() < 0.5f) {
                int duration = 30 + (int) (Math.random() * 30);
                float intensity = Math.min(1.0f, damage / 20.0f);
                ExodusCoreAPI.addEffect(player, StatusEffect.FRACTURE, duration, intensity);
            }
        }

        Entity attacker = source.getEntity();
        if (attacker != null && !source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION)) {
            int duration = 10 + (int) (Math.random() * 10);
            float intensity = Math.min(1.0f, damage / 15.0f);
            ExodusCoreAPI.addEffect(player, StatusEffect.PAIN, duration, intensity);

            if (Math.random() < 0.3f) {
                duration = 20 + (int) (Math.random() * 20);
                intensity = Math.min(0.8f, damage / 20.0f);
                ExodusCoreAPI.addEffect(player, StatusEffect.BLEEDING, duration, intensity);
            }
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION) ||
                source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_EXPLOSION)) {

            float intensity = Math.min(1.0f, damage / 15.0f);
            ExodusCoreAPI.addEffect(player, StatusEffect.PAIN, 30, intensity);

            if (Math.random() < 0.7f) {
                ExodusCoreAPI.addEffect(player, StatusEffect.BLEEDING, 40, intensity);
            }

            if (Math.random() < 0.5f) {
                ExodusCoreAPI.addEffect(player, StatusEffect.FRACTURE, 60, intensity * 0.8f);
            }
        }
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