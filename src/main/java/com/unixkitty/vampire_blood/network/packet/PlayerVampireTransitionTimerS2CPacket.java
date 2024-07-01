package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerVampireTransitionTimerS2CPacket extends BasePacket
{
    public final int transitionTimer;

    public PlayerVampireTransitionTimerS2CPacket(int transitionTimer)
    {
        this.transitionTimer = transitionTimer;
    }

    public PlayerVampireTransitionTimerS2CPacket(FriendlyByteBuf buffer)
    {
        this.transitionTimer = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.transitionTimer);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> ClientPacketHandler.handleTransitionTimerPacket(this.transitionTimer));

        context.setPacketHandled(true);

        return true;
    }
}
