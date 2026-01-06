package com.exodus.survival.inventory.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

// Используйте CamelCase для имен классов
public record TakeItemEntityPacket(int itemId, int amount) implements FabricPacket {
    public static final PacketType<TakeItemEntityPacket> TYPE =
            PacketType.create(
                    new ResourceLocation("exodus-survival", "take_item_entity"),
                    TakeItemEntityPacket::new
            );

    public TakeItemEntityPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(itemId);
        buf.writeVarInt(amount);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}