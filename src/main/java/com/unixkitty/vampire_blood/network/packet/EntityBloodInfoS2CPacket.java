package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityBloodInfoS2CPacket extends BasePacket
{
    private final BloodType bloodType;
    private final int maxBloodPoints;
    private final int bloodPoints;

    public EntityBloodInfoS2CPacket(BloodType bloodType, int bloodPoints, int maxBloodPoints)
    {
        this.bloodType = bloodType;
        this.bloodPoints = bloodPoints;
        this.maxBloodPoints = maxBloodPoints;
    }

    public EntityBloodInfoS2CPacket(FriendlyByteBuf buffer)
    {
        this.bloodType = buffer.readEnum(BloodType.class);
        this.bloodPoints = buffer.readInt();
        this.maxBloodPoints = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeEnum(this.bloodType);
        buffer.writeInt(this.bloodPoints);
        buffer.writeInt(this.maxBloodPoints);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> ClientPacketHandler.handleEntityBloodInfo(this.bloodType, this.bloodPoints, this.maxBloodPoints));

        context.setPacketHandled(true);

        return true;
    }
}
