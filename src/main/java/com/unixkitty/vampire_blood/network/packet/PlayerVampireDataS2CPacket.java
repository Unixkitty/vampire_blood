package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.client.ClientVampirePlayerDataCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerVampireDataS2CPacket extends BasePacket
{
    private final int vampireLevel;
    private final int bloodType;
    private final int thirstLevel;
    private final int thirstExhaustion;
    private final float bloodlust;
    private final float bloodPurity;

    public PlayerVampireDataS2CPacket(VampirismStage vampirismStage, BloodType bloodType, int thirstLevel, int thirstExhaustion, float bloodlust, float bloodPurity)
    {
        this.vampireLevel = vampirismStage.getId();
        this.bloodType = bloodType.getId();
        this.thirstLevel = thirstLevel;
        this.thirstExhaustion = thirstExhaustion;
        this.bloodlust = bloodlust;
        this.bloodPurity = bloodPurity;
    }

    public PlayerVampireDataS2CPacket(FriendlyByteBuf buffer)
    {
        this.vampireLevel = buffer.readInt();
        this.bloodType = buffer.readInt();
        this.thirstLevel = buffer.readInt();
        this.thirstExhaustion = buffer.readInt();
        this.bloodlust = buffer.readFloat();
        this.bloodPurity = buffer.readFloat();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.vampireLevel);
        buffer.writeInt(this.bloodType);
        buffer.writeInt(this.thirstLevel);
        buffer.writeInt(this.thirstExhaustion);
        buffer.writeFloat(this.bloodlust);
        buffer.writeFloat(this.bloodPurity);
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
                    ClientVampirePlayerDataCache.bloodPurity = this.bloodPurity;
                });
            }
        });

        context.setPacketHandled(true);

        return true;
    }
}
