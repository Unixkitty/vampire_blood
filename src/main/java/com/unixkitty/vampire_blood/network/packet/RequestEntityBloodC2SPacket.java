package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.provider.BloodProvider;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestEntityBloodC2SPacket
{
    private final int id;

    public RequestEntityBloodC2SPacket(int id)
    {
        this.id = id;
    }

    public RequestEntityBloodC2SPacket(FriendlyByteBuf buffer)
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
                entity.getCapability(BloodProvider.BLOOD_STORAGE).ifPresent(bloodStorage ->
                        ModNetworkDispatcher.sendToClient(new EntityBloodResponseS2CPacket(bloodStorage.getBloodType().getId(), bloodStorage.getBloodPoints(), bloodStorage.getMaxBloodPoints()), player));
            }
        });

        context.setPacketHandled(true);

        return true;
    }
}
