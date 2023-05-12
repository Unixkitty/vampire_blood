package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import com.unixkitty.vampire_blood.client.ClientVampirePlayerDataCache;
import com.unixkitty.vampire_blood.util.VampirismTier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerRespawnS2CPacket
{
    private final int vampireLevel;
    private final int bloodType;
    private final int thirstLevel;

    public PlayerRespawnS2CPacket(int vampireLevel, int bloodType, int thirstLevel)
    {
        this.vampireLevel = vampireLevel;
        this.bloodType = bloodType;
        this.thirstLevel = thirstLevel;
    }

    public PlayerRespawnS2CPacket(FriendlyByteBuf buffer)
    {
        this.vampireLevel = buffer.readInt();
        this.bloodType = buffer.readInt();
        this.thirstLevel = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.vampireLevel);
        buffer.writeInt(this.bloodType);
        buffer.writeInt(this.thirstLevel);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            ClientVampirePlayerDataCache.vampireLevel = VampirismTier.fromId(VampirismStage.class, this.vampireLevel);
            ClientVampirePlayerDataCache.bloodType = VampirismTier.fromId(BloodType.class, this.bloodType);
            ClientVampirePlayerDataCache.thirstLevel = this.thirstLevel;
            ClientVampirePlayerDataCache.playerJustRespawned = true;
        });

        context.setPacketHandled(true);

        return true;
    }
}
