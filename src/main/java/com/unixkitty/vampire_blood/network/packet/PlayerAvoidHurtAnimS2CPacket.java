package com.unixkitty.vampire_blood.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerAvoidHurtAnimS2CPacket extends BasePacket
{
    private final float health;

    public PlayerAvoidHurtAnimS2CPacket(float health)
    {
        this.health = health;
    }

    public PlayerAvoidHurtAnimS2CPacket(FriendlyByteBuf buffer)
    {
        this.health = buffer.readFloat();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeFloat(this.health);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            if (Minecraft.getInstance().player != null)
            {
                Minecraft.getInstance().player.setHealth(this.health);
            }
        });

        context.setPacketHandled(true);

        return true;
    }
}
