package com.exodus.survival.client.raycast;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class ItemRaycast {

    private static final double PICKUP_REACH = 3.0; // Дистанция подбора

    /**
     * Создать луч от глаз игрока
     *
     * @return массив [start, end] - начало и конец луча
     */
    public static Vec3[] createRay(Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(PICKUP_REACH));

        return new Vec3[] {eyePos, endPos};
    }

    /**
     * Найти ItemEntity на который смотрит игрок
     *
     * @return ItemEntity если нашли, null если нет
     */
    public static ItemEntity findLookedAtItem(Player player) {
        Vec3[] ray = createRay(player);
        Level level = player.level();
        AABB searchBox = player.getBoundingBox().inflate(PICKUP_REACH + 1);
        Predicate<Entity> filter = entity -> {
            return entity instanceof ItemEntity;
        };

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(level, player, ray[0], ray[1], searchBox, filter);

        if(hit != null){
            return (ItemEntity) hit.getEntity();
        }

        return null;
    }
}