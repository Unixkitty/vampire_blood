package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.client.ClientVampirePlayerDataCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerVampireDataS2CPacket
{
    private final int vampireLevel;
    private final int bloodType;
    private final int thirstLevel;
    private final int thirstExhaustion;
    private final float bloodlust;

    public PlayerVampireDataS2CPacket(VampirismStage vampirismStage, BloodType bloodType, int thirstLevel, int thirstExhaustion, float bloodlust)
    {
        this.vampireLevel = vampirismStage.getId();
        this.bloodType = bloodType.getId();
        this.thirstLevel = thirstLevel;
        this.thirstExhaustion = thirstExhaustion;
        this.bloodlust = bloodlust;
    }

    public PlayerVampireDataS2CPacket(FriendlyByteBuf buffer)
    {
        this.vampireLevel = buffer.readInt();
        this.bloodType = buffer.readInt();
        this.thirstLevel = buffer.readInt();
        this.thirstExhaustion = buffer.readInt();
        this.bloodlust = buffer.readFloat();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.vampireLevel);
        buffer.writeInt(this.bloodType);
        buffer.writeInt(this.thirstLevel);
        buffer.writeInt(this.thirstExhaustion);
        buffer.writeFloat(this.bloodlust);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            if (Minecraft.getInstance().player != null)
            {
                Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
                {
                    ClientVampirePlayerDataCache.vampireLevel = vampirePlayerData.setVampireLevel(this.vampireLevel);
                    ClientVampirePlayerDataCache.bloodType = vampirePlayerData.setBloodType(this.bloodType);

                    ClientVampirePlayerDataCache.thirstLevel = vampirePlayerData.setClientBlood(this.thirstLevel);
                    ClientVampirePlayerDataCache.thirstExhaustion = vampirePlayerData.setClientExhaustion(this.thirstExhaustion);
                    ClientVampirePlayerDataCache.bloodlust = vampirePlayerData.setClientBloodlust(this.bloodlust);
                });
            }
        });

        context.setPacketHandled(true);

        return true;
    }
}
