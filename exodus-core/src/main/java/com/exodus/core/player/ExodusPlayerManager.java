package com.exodus.core.player;

import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.player.ExodusPlayerData;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.exodus.core.ExodusCoreAPI.getPlayerData;

/**
 * Менеджер компонентов игроков
 */
public class ExodusPlayerManager {

    private static final Map<UUID, ExodusPlayerComponent> components = new HashMap<>();

    /**
     * Получить компонент игрока (создаёт если не существует)
     */
    public static ExodusPlayerComponent getComponent(Player player) {
        return components.computeIfAbsent(
                player.getUUID(),
                uuid -> new ExodusPlayerComponent(player)
        );
    }

    /**
     * Удалить компонент игрока
     */
    public static void removeComponent(UUID uuid) {
        components.remove(uuid);
    }

    /**
     * Регистрация событий для автоматического сохранения
     */
    public static void registerEvents() {
        // При респавне - частично повреждённые конечности
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) { // Игрок умер и респавнился
                ExodusPlayerData data = getPlayerData(newPlayer);

                for (BodyPart part : BodyPart.values()) {
                    ExodusPlayerData.BodyPartData partData = data.getBodyPart(part);

                    // Восстанавливаем HP в зависимости от части
                    switch (part) {
                        case HEAD -> {
                            // Голова: 60-80% HP
                            partData.currentHP = partData.maxHP * (0.6f + (float)Math.random() * 0.2f);
                        }
                        case TORSO -> {
                            // Торс: 70-90% HP
                            partData.currentHP = partData.maxHP * (0.7f + (float)Math.random() * 0.2f);
                        }
                        case LEFT_ARM, RIGHT_ARM -> {
                            // Руки: 50-70% HP
                            partData.currentHP = partData.maxHP * (0.5f + (float)Math.random() * 0.2f);
                        }
                        case LEFT_LEG, RIGHT_LEG -> {
                            // Ноги: 40-60% HP
                            partData.currentHP = partData.maxHP * (0.4f + (float)Math.random() * 0.2f);
                        }
                    }

                    // Убираем критические состояния
                    partData.isBleeding = false;
                    partData.isBroken = false;
                }
                data.setBloodLevel(100f);              // Полная кровь
                data.setBodyTemperature(36.6f);        // Нормальная температура
                data.setLastCriticalHPCheck(0);
            }
        });

        // При переходе между мирами - копируем данные
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            ExodusPlayerComponent oldComp = getComponent(oldPlayer);
            ExodusPlayerComponent newComp = getComponent(newPlayer);

            CompoundTag nbt = new CompoundTag();
            oldComp.writeNbt(nbt);
            newComp.readNbt(nbt);
        });
    }
}