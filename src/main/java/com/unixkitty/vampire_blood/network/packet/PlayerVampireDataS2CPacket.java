package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.client.cache.ClientVampirePlayerDataCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerVampireDataS2CPacket extends BasePacket
{
    private final VampirismStage vampireLevel;
    private final BloodType bloodType;
    private final int thirstLevel;
    private final int thirstExhaustion;
    private final float bloodlust;
    private final float bloodPurity;

    public PlayerVampireDataS2CPacket(VampirismStage vampirismStage, BloodType bloodType, int thirstLevel, int thirstExhaustion, float bloodlust, float bloodPurity)
    {
        this.vampireLevel = vampirismStage;
        this.bloodType = bloodType;
        this.thirstLevel = thirstLevel;
        this.thirstExhaustion = thirstExhaustion;
        this.bloodlust = bloodlust;
        this.bloodPurity = bloodPurity;
    }

    public PlayerVampireDataS2CPacket(FriendlyByteBuf buffer)
    {
        this.vampireLevel = buffer.readEnum(VampirismStage.class);
        this.bloodType = buffer.readEnum(BloodType.class);
        this.thirstLevel = buffer.readVarInt();
        this.thirstExhaustion = buffer.readVarInt();
        this.bloodlust = buffer.readFloat();
        this.bloodPurity = buffer.readFloat();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeEnum(this.vampireLevel);
        buffer.writeEnum(this.bloodType);
        buffer.writeVarInt(this.thirstLevel);
        buffer.writeVarInt(this.thirstExhaustion);
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
                    ClientVampirePlayerDataCache.vampireLevel = vampirePlayerData.setClientVampireLevel(this.vampireLevel);
                    ClientVampirePlayerDataCache.bloodType = vampirePlayerData.setClientBloodType(this.bloodType);

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
