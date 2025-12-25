package com.exodus.survival.mixin;

import com.exodus.survival.systems.BodyPartDamageSystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Перехватываем весь урон игрока
 */
@Mixin(Player.class)
public class PlayerDamageMixin {

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void onPlayerHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player)(Object)this;

        // Только на сервере
        if (player.level().isClientSide) {
            return;
        }

        // ОТМЕНЯЕМ весь ванильный урон
        cir.setReturnValue(false);

        // Применяем через нашу систему
        BodyPartDamageSystem.handleDamage((ServerPlayer)player, source, amount);
    }
}