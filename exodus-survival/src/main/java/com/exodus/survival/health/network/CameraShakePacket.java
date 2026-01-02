package com.exodus.survival.health.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Пакет для тряски камеры (дрожь от холода/урона)
 */
public class CameraShakePacket implements FabricPacket {

    public static final PacketType<CameraShakePacket> TYPE =
            PacketType.create(
                    new ResourceLocation("exodus-survival", "camera_shake"),
                    CameraShakePacket::new
            );

    private final boolean activate;  // true = включить, false = выключить
    private final float intensity;   // сила кружения (градусы)

    public CameraShakePacket(boolean activate, float intensity) {
        this.activate = activate;
        this.intensity = intensity;
    }

    public CameraShakePacket(FriendlyByteBuf buf) {
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