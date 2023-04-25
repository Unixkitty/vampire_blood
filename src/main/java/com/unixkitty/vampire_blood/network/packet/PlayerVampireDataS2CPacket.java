package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.VampireBloodType;
import com.unixkitty.vampire_blood.capability.VampirePlayerProvider;
import com.unixkitty.vampire_blood.capability.VampirismStage;
import com.unixkitty.vampire_blood.client.ClientVampirePlayerDataCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerVampireDataS2CPacket
{
    private final int vampireLevel;
    private final int bloodType;
    private final boolean isFeeding;

    public PlayerVampireDataS2CPacket(int vampireLevel, int bloodType, boolean isFeeding)
    {
        this.vampireLevel = vampireLevel;
        this.bloodType = bloodType;
        this.isFeeding = isFeeding;
    }

    public PlayerVampireDataS2CPacket(FriendlyByteBuf buffer)
    {
        this.vampireLevel = buffer.readInt();
        this.bloodType = buffer.readInt();
        this.isFeeding = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.vampireLevel);
        buffer.writeInt(this.bloodType);
        buffer.writeBoolean(this.isFeeding);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                vampirePlayerData.setVampireLevel(this.vampireLevel);
                vampirePlayerData.setBloodType(this.bloodType);
                vampirePlayerData.setFeeding(this.isFeeding);
            });

            ClientVampirePlayerDataCache.vampireLevel = VampirismStage.fromId(this.vampireLevel);
            ClientVampirePlayerDataCache.bloodType = VampireBloodType.fromId(this.bloodType);
            ClientVampirePlayerDataCache.isFeeding = this.isFeeding;
        });

        context.setPacketHandled(true);

        return true;
    }
}
