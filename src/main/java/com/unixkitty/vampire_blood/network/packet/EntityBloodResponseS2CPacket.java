package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.client.gui.MouseOverHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityBloodResponseS2CPacket
{
    private final int bloodType;
    private final int maxBloodPoints;
    private final int bloodPoints;

    public EntityBloodResponseS2CPacket(int bloodTypeId, int bloodPoints, int maxBloodPoints)
    {
        this.bloodType = bloodTypeId;
        this.bloodPoints = bloodPoints;
        this.maxBloodPoints = maxBloodPoints;
    }

    public EntityBloodResponseS2CPacket(FriendlyByteBuf buffer)
    {
        this.bloodType = buffer.readInt();
        this.bloodPoints = buffer.readInt();
        this.maxBloodPoints = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.bloodType);
        buffer.writeInt(this.bloodPoints);
        buffer.writeInt(this.maxBloodPoints);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            MouseOverHandler.bloodType = BloodType.fromId(this.bloodType);
            MouseOverHandler.bloodPoints = this.bloodPoints;
            MouseOverHandler.maxBloodPoints = this.maxBloodPoints;
        });

        context.setPacketHandled(true);

        return true;
    }
}
