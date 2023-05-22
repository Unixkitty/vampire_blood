package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.client.ClientPacketHandler;
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

        context.enqueueWork(() -> ClientPacketHandler.handleAvoidHurtAnim(this.health));

        context.setPacketHandled(true);

        return true;
    }
}
