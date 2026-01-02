package com.exodus.survival.health.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Пакет для кружения головы (вращение камеры)
 */
public class HeadSpinPacket implements FabricPacket {

    public static final PacketType<HeadSpinPacket> TYPE =
            PacketType.create(
                    new ResourceLocation("exodus-survival", "head_spin"),
                    HeadSpinPacket::new
            );

    private final boolean activate;    // Включить/выключить
    private final float intensity;     // Градусов в секунду

    public HeadSpinPacket(boolean activate, float intensity) {
        this.activate = activate;
        this.intensity = intensity;
    }

    public HeadSpinPacket(FriendlyByteBuf buf) {
        this.activate = buf.readBoolean();
        this.intensity = buf.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(activate);
        buf.writeFloat(intensity);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public boolean shouldActivate() {
        return activate;
    }

    public float getIntensity() {
        return intensity;
    }
}