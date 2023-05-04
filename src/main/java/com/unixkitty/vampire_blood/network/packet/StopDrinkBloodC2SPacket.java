package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.player.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StopDrinkBloodC2SPacket
{
    public StopDrinkBloodC2SPacket()
    {

    }

    public StopDrinkBloodC2SPacket(FriendlyByteBuf buffer)
    {

    }

    public void toBytes(FriendlyByteBuf buffer)
    {

    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            ServerPlayer player = context.getSender();

            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(VampirePlayerData::stopFeeding);
        });

        context.setPacketHandled(true);

        return true;
    }
}
