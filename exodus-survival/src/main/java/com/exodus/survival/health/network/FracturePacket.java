// FracturePacket.java
package com.exodus.survival.health.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class FracturePacket implements FabricPacket {

    public static final PacketType<FracturePacket> TYPE =
            PacketType.create(new ResourceLocation("exodus-survival", "fracture"), FracturePacket::new);

    private final String bodyPart;
    private final float intensity;

    public FracturePacket(String bodyPart, float intensity) {
        this.bodyPart = bodyPart;
        this.intensity = intensity;
    }

    public FracturePacket(FriendlyByteBuf buf) {
        this.bodyPart = buf.readUtf();
        this.intensity = buf.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(bodyPart);
        buf.writeFloat(intensity);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public String getBodyPart() {
        return bodyPart;
    }

    public float getIntensity() {
        return intensity;
    }
}