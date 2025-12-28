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
        String playerName = player.getDisplayName().getString();
        String deathMessage = getDeathMessage(playerName, source);

        // Отправляем сообщение о смерти
        player.sendSystemMessage(Component.literal(deathMessage));

        // Используем огромный урон чтобы гарантированно убить
        player.hurt(player.damageSources().generic(), 1000.0f);
    }

    /**
     * Получить сообщение о смерти в зависимости от источника
     */
    private static String getDeathMessage(String playerName, DamageSource source) {
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
            return "§c§l" + playerName + " разбился при падении";
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.DROWN)) {
            return "§c§l" + playerName + " утонул";
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.STARVE)) {
            // Используется для смерти от кровотечения
            return "§c§l" + playerName + " истёк кровью";
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE) ||
                source.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE)) {
            return "§c§l" + playerName + " сгорел заживо";
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)) {
            return "§c§l" + playerName + " искупался в лаве";
        } else if (source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION) ||
                source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_EXPLOSION)) {
            return "§c§l" + playerName + " взорвался";
        } else if (source.getEntity() != null) {
            String attackerName = source.getEntity().getDisplayName().getString();
            return "§c§l" + playerName + " был убит " + attackerName;
        }

        return "§c§l" + playerName + " умер";
    }
}