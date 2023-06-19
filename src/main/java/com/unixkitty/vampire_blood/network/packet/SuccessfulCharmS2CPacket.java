package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SuccessfulCharmS2CPacket extends BasePacket
{
    public final int entityId;

    public SuccessfulCharmS2CPacket(int entityId)
    {
        this.entityId = entityId;
    }

    public SuccessfulCharmS2CPacket(FriendlyByteBuf buffer)
    {
        this.entityId = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> ClientPacketHandler.handleSuccessfulCharm(this.entityId));

        context.setPacketHandled(true);

        return true;
    }
}
