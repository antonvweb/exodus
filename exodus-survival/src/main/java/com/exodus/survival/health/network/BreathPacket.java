package com.exodus.survival.health.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class BreathPacket implements FabricPacket {
    public static final PacketType<BreathPacket> TYPE =
            PacketType.create(new ResourceLocation("exodus-survival", "low_hp"), BreathPacket::new);

    private final boolean activate;

    public BreathPacket(boolean activate) {
        this.activate = activate;
    }

    public BreathPacket(FriendlyByteBuf buf) {
        this.activate = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(activate);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public boolean shouldActivate() {
        return activate;
    }
}
