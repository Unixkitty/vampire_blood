package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityCharmedStatusS2CPacket extends BasePacket
{
    public final int entityId;
    public final boolean charmed;

    public EntityCharmedStatusS2CPacket(int entityId, boolean charmed)
    {
        this.entityId = entityId;
        this.charmed = charmed;
    }

    public EntityCharmedStatusS2CPacket(FriendlyByteBuf buffer)
    {
        this.entityId = buffer.readInt();
        this.charmed = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
        buffer.writeBoolean(this.charmed);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> ClientPacketHandler.handleEntityCharmedStatus(this.entityId, this.charmed));

        context.setPacketHandled(true);

        return true;
    }
}
