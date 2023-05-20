package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.client.cache.ClientVampirePlayerDataCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerFeedingStatusS2CPacket extends BasePacket
{
    private final boolean feeding;

    public PlayerFeedingStatusS2CPacket(boolean feeding)
    {
        this.feeding = feeding;
    }

    public PlayerFeedingStatusS2CPacket(FriendlyByteBuf buffer)
    {
        this.feeding = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(this.feeding);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            if (Minecraft.getInstance().player != null)
            {
                Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
                        ClientVampirePlayerDataCache.feeding = vampirePlayerData.setFeeding(this.feeding));
            }
        });

        context.setPacketHandled(true);

        return true;
    }
}
