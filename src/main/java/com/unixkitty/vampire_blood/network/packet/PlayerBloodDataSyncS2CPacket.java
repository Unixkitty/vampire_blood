package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.client.ClientVampirePlayerDataCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerBloodDataSyncS2CPacket
{
    private final int thirstLevel;

    public PlayerBloodDataSyncS2CPacket(int thirstLevel)
    {
        this.thirstLevel = thirstLevel;
    }

    public PlayerBloodDataSyncS2CPacket(FriendlyByteBuf buffer)
    {
        this.thirstLevel = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.thirstLevel);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
                Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
                        ClientVampirePlayerDataCache.thirstLevel = vampirePlayerData.setClientBlood(this.thirstLevel)));

        context.setPacketHandled(true);

        return true;
    }
}
