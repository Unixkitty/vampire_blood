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
    private final int thirstExhaustion;
    private final float bloodlust;

    public PlayerBloodDataSyncS2CPacket(int thirstLevel, int thirstExhaustion, float bloodlust)
    {
        this.thirstLevel = thirstLevel;
        this.thirstExhaustion = thirstExhaustion;
        this.bloodlust = bloodlust;
    }

    public PlayerBloodDataSyncS2CPacket(FriendlyByteBuf buffer)
    {
        this.thirstLevel = buffer.readInt();
        this.thirstExhaustion = buffer.readInt();
        this.bloodlust = buffer.readFloat();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.thirstLevel);
        buffer.writeInt(this.thirstExhaustion);
        buffer.writeFloat(this.bloodlust);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
                Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
                {
                    ClientVampirePlayerDataCache.thirstLevel = vampirePlayerData.setClientBlood(this.thirstLevel);
                    ClientVampirePlayerDataCache.thirstExhaustion = vampirePlayerData.setClientExhaustion(this.thirstExhaustion);
                    ClientVampirePlayerDataCache.bloodlust = vampirePlayerData.setClientBloodlust(this.bloodlust);
                }));

        context.setPacketHandled(true);

        return true;
    }
}
