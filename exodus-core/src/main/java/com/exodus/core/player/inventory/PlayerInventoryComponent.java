package com.exodus.core.player.inventory;

import com.exodus.core.api.player.inventory.PlayerInventoryData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class PlayerInventoryComponent {
    private final Player player;
    private final PlayerInventoryData data;

    public PlayerInventoryComponent(Player player) {
        this.player = player;
        this.data = new PlayerInventoryData();
    }

    public PlayerInventoryData getData() {
        return data;
    }

    public void writeNbt(CompoundTag nbt) {
        data.writeNbt(nbt);
    }

    public void readNbt(CompoundTag nbt) {
        data.readNbt(nbt);
    }
}