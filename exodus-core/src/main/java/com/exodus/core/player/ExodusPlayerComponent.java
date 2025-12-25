package com.exodus.core.player;

import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.player.ExodusPlayerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Компонент расширенных данных игрока
 */
public class ExodusPlayerComponent {

    private final Player player;
    private final ExodusPlayerData data;

    public ExodusPlayerComponent(Player player) {
        this.player = player;
        this.data = new ExodusPlayerData();
        initialize();
    }

    private void initialize() {
        // Устанавливаем имя персонажа
        data.setCharacterName(player.getName().getString());

        // Базовые значения уже установлены в ExodusPlayerData конструкторе
    }

    /**
     * Получить данные игрока
     */
    public ExodusPlayerData getData() {
        return data;
    }

    /**
     * Получить данные части тела
     */
    public ExodusPlayerData.BodyPartData getBodyPart(BodyPart part) {
        return data.getBodyPart(part);
    }

    /**
     * Нанести урон части тела
     */
    public void damageBodyPart(BodyPart part, float damage) {
        ExodusPlayerData.BodyPartData partData = data.getBodyPart(part);
        partData.currentHP = Math.max(0, partData.currentHP - damage);

        // Если HP части достигло 0 - она сломана
        if (partData.currentHP <= 0) {
            partData.isBroken = true;
        }
    }

    /**
     * Вылечить часть тела
     */
    public void healBodyPart(BodyPart part, float amount) {
        ExodusPlayerData.BodyPartData partData = data.getBodyPart(part);
        partData.currentHP = Math.min(partData.maxHP, partData.currentHP + amount);

        // Если HP восстановлено выше 0 - больше не сломана
        if (partData.currentHP > 0) {
            partData.isBroken = false;
        }
    }

    /**
     * Сохранить в NBT
     */
    public CompoundTag writeNbt(CompoundTag nbt) {
        return data.writeNbt(nbt);
    }

    /**
     * Загрузить из NBT
     */
    public void readNbt(CompoundTag nbt) {
        data.readNbt(nbt);
    }
}