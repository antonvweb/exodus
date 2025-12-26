package com.exodus.health.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Пакет для отправки информации об уроне с сервера на клиент
 */
public class DamagePacket implements FabricPacket {

    public static final PacketType<DamagePacket> TYPE =
            PacketType.create(new ResourceLocation("exodus-health", "damage"), DamagePacket::new);

    private final float damage;

    public DamagePacket(float damage) {
        this.damage = damage;
    }

    public DamagePacket(FriendlyByteBuf buf) {
        this.damage = buf.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeFloat(damage);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public float getDamage() {
        return damage;
    }
}