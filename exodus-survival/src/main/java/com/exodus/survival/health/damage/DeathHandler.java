package com.exodus.survival.health.damage;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.PlayerHealthData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

/**
 * Обработчик смерти игрока
 * Упрощённая система
 */
public class DeathHandler {

    /**
     * Проверить нужно ли убить игрока
     */
    public static boolean checkDeath(Player player, DamageSource source) {
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
        // ✅ Проверяем что игрок еще не мертв
        if (player.isDeadOrDying()) {
            return;
        }
        // Убиваем игрока
        player.setHealth(0);
    }

    /**
     * Получить сообщение о смерти
     */
    private static String getDeathMessage(String playerName, DamageSource source, PlayerHealthData.DeathCause deathCause) {

        // ========== СПЕЦИФИЧНЫЕ ПРИЧИНЫ ОТ СИСТЕМЫ ЧАСТЕЙ ТЕЛА ==========

        // 💀 Голова уничтожена
        if (deathCause == PlayerHealthData.DeathCause.HEAD_DESTROYED) {
            return "§c§l💀 " + playerName + " §4§lполучил смертельное ранение головы";
        }

        // 🩸 Истёк кровью
        if (source.is(net.minecraft.world.damagesource.DamageTypes.STARVE)) {
            return "§c§l🩸 " + playerName + " §4§lистёк кровью";
        }

        // ========== ОБЫЧНЫЕ ПРИЧИНЫ СМЕРТИ ==========

        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
            return "§c§l" + playerName + " разбился при падении";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.FLY_INTO_WALL)) {
            return "§c§l" + playerName + " испытал кинетическую перегрузку";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)) {
            return "§c§l" + playerName + " упал в бездну";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)) {
            return "§c§l" + playerName + " задохнулся в стене";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.CRAMMING)) {
            return "§c§l" + playerName + " был раздавлен";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.OUTSIDE_BORDER)) {
            return "§c§l" + playerName + " вышел за пределы мира";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.DROWN)) {
            return "§c§l" + playerName + " утонул";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.DRY_OUT)) {
            return "§c§l" + playerName + " высох";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE)) {
            return "§c§l" + playerName + " сгорел в огне";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE)) {
            return "§c§l" + playerName + " сгорел заживо";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)) {
            return "§c§l" + playerName + " искупался в лаве";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)) {
            return "§c§l" + playerName + " обжёгся о горячую поверхность";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.FIREBALL) ||
                source.is(net.minecraft.world.damagesource.DamageTypes.UNATTRIBUTED_FIREBALL)) {

            if (source.getEntity() != null) {
                String attackerName = source.getEntity().getDisplayName().getString();
                return "§c§l" + playerName + " был сожжён огненным шаром от " + attackerName;
            }
            return "§c§l" + playerName + " был сожжён огненным шаром";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.FREEZE)) {
            return "§c§l" + playerName + " замёрз насмерть";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION)) {
            if (source.getEntity() != null) {
                String attackerName = source.getEntity().getDisplayName().getString();
                return "§c§l" + playerName + " был взорван " + attackerName;
            }
            return "§c§l" + playerName + " взорвался";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_EXPLOSION)) {
            return "§c§l" + playerName + " был взорван игроком";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.BAD_RESPAWN_POINT)) {
            return "§c§l" + playerName + " был убит [Intentional Game Design]";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.FIREWORKS)) {
            return "§c§l" + playerName + " взорвался от фейерверка";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC) ||
                source.is(net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC)) {

            if (source.getEntity() != null) {
                String attackerName = source.getEntity().getDisplayName().getString();
                return "§c§l" + playerName + " был убит магией " + attackerName;
            }
            return "§c§l" + playerName + " был убит магией";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
            return "§c§l" + playerName + " иссох";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.DRAGON_BREATH)) {
            return "§c§l" + playerName + " был испепелён драконом";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.SONIC_BOOM)) {
            return "§c§l" + playerName + " был уничтожен звуковой волной";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.CACTUS)) {
            return "§c§l" + playerName + " был уколот до смерти";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.SWEET_BERRY_BUSH)) {
            return "§c§l" + playerName + " был заколот ягодным кустом";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.THORNS)) {
            return "§c§l" + playerName + " был убит шипами";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.STALAGMITE)) {
            return "§c§l" + playerName + " был проткнут сталагмитом";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALLING_BLOCK)) {
            return "§c§l" + playerName + " был раздавлен падающим блоком";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALLING_ANVIL)) {
            return "§c§l" + playerName + " был расплющен падающей наковальней";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALLING_STALACTITE)) {
            return "§c§l" + playerName + " был проткнут падающим сталактитом";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK) ||
                source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK_NO_AGGRO)) {

            if (source.getEntity() != null) {
                String mobName = source.getEntity().getDisplayName().getString();
                return "§c§l" + playerName + " был убит " + mobName;
            }
            return "§c§l" + playerName + " был убит мобом";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)) {
            if (source.getEntity() != null) {
                String killerName = source.getEntity().getDisplayName().getString();
                return "§c§l" + playerName + " был убит игроком " + killerName;
            }
            return "§c§l" + playerName + " был убит игроком";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.STING)) {
            return "§c§l" + playerName + " был ужален до смерти";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.ARROW)) {
            if (source.getEntity() != null) {
                String shooterName = source.getEntity().getDisplayName().getString();
                return "§c§l" + playerName + " был застрелен " + shooterName;
            }
            return "§c§l" + playerName + " был застрелен";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.TRIDENT)) {
            if (source.getEntity() != null) {
                String throwerName = source.getEntity().getDisplayName().getString();
                return "§c§l" + playerName + " был пронзён трезубцем " + throwerName;
            }
            return "§c§l" + playerName + " был пронзён трезубцем";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.MOB_PROJECTILE)) {
            if (source.getEntity() != null) {
                String mobName = source.getEntity().getDisplayName().getString();
                return "§c§l" + playerName + " был убит снарядом " + mobName;
            }
            return "§c§l" + playerName + " был убит снарядом";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.WITHER_SKULL)) {
            if (source.getEntity() != null) {
                String witherName = source.getEntity().getDisplayName().getString();
                return "§c§l" + playerName + " был убит черепом " + witherName;
            }
            return "§c§l" + playerName + " был убит черепом иссушителя";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.THROWN)) {
            if (source.getEntity() != null) {
                String throwerName = source.getEntity().getDisplayName().getString();
                return "§c§l" + playerName + " был забит предметами " + throwerName;
            }
            return "§c§l" + playerName + " был забит брошенными предметами";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT)) {
            return "§c§l" + playerName + " был поражён молнией";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)) {
            return "§c§l" + playerName + " был убит";
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC)) {
            return "§c§l" + playerName + " умер";
        }

        if (source.getEntity() != null) {
            String attackerName = source.getEntity().getDisplayName().getString();
            return "§c§l" + playerName + " был убит " + attackerName;
        }

        return "§c§l" + playerName + " умер при загадочных обстоятельствах";
    }
}