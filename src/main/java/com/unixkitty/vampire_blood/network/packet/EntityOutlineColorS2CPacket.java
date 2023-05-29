package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class EntityOutlineColorS2CPacket extends BasePacket
{
    private final int entityId;
    private final int color;

    public EntityOutlineColorS2CPacket(@Nonnull LivingEntity entity, int color)
    {
        this.entityId = entity.getId();
        this.color = color;
    }

    public EntityOutlineColorS2CPacket(FriendlyByteBuf buffer)
    {
        this.entityId = buffer.readInt();
        this.color = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
        buffer.writeInt(this.color);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> ClientPacketHandler.handleEntityOutlineColor(this.entityId, this.color));

        context.setPacketHandled(true);

        return true;
    }
}
