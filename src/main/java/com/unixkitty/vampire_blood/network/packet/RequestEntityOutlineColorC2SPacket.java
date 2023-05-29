package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestEntityOutlineColorC2SPacket extends BasePacket
{
    private final int id;

    public RequestEntityOutlineColorC2SPacket(int id)
    {
        this.id = id;
    }

    public RequestEntityOutlineColorC2SPacket(FriendlyByteBuf buffer)
    {
        this.id = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.id);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            ServerPlayer player = context.getSender();

            if (player != null && player.level.getEntity(this.id) instanceof LivingEntity entity)
            {
                VampireUtil.computeEntityOutlineColorFor(player, entity);
            }
        });

        context.setPacketHandled(true);

        return true;
    }
}
