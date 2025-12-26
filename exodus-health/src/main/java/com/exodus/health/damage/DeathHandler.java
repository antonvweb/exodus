package com.exodus.health.damage;

import com.exodus.core.ExodusCoreAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

/**
 * Обработчик смерти игрока
 */
public class DeathHandler {

    /**
     * Проверить нужно ли убить игрока
     * @return true если игрок должен умереть
     */
    public static boolean checkDeath(Player player, DamageSource source) {
        // Проверяем HP
        if (!ExodusCoreAPI.isAlive(player)) {
            killPlayer(player, source);
            return true;
        }

        return false;
    }

    /**
     * Убить игрока
     */
    private static void killPlayer(Player player, DamageSource source) {
        // Отправляем сообщение о смерти
        String playerName = player.getDisplayName().getString();
        player.sendSystemMessage(Component.literal("§c§l" + playerName + " умер"));

        // Убиваем игрока через ванильную систему
        player.hurt(player.damageSources().generic(), 1000.0f);
    }
}
