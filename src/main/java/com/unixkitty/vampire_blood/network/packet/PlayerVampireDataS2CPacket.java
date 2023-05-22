package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.client.cache.ClientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerVampireDataS2CPacket extends BasePacket
{
    public final VampirismLevel vampireLevel;
    public final BloodType bloodType;
    public final int thirstLevel;
    public final int thirstExhaustion;
    public final float bloodlust;
    public final float bloodPurity;

    public PlayerVampireDataS2CPacket(VampirismLevel vampirismLevel, BloodType bloodType, int thirstLevel, int thirstExhaustion, float bloodlust, float bloodPurity)
    {
        this.vampireLevel = vampirismLevel;
        this.bloodType = bloodType;
        this.thirstLevel = thirstLevel;
        this.thirstExhaustion = thirstExhaustion;
        this.bloodlust = bloodlust;
        this.bloodPurity = bloodPurity;
    }

    public PlayerVampireDataS2CPacket(FriendlyByteBuf buffer)
    {
        this.vampireLevel = buffer.readEnum(VampirismLevel.class);
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

        context.enqueueWork(() -> ClientCache.getVampireVars().handleVampireDataPacket(this));

        context.setPacketHandled(true);

        return true;
    }
}
