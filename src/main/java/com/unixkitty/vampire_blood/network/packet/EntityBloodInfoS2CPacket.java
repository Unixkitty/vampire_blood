package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityBloodInfoS2CPacket extends BasePacket
{
    public final int entityId;
    public final BloodType bloodType;
    public final int maxBloodPoints;
    public final int bloodPoints;
    public final boolean lookingDirectly;
    public final int charmedTicks;

    public EntityBloodInfoS2CPacket(int entityId, BloodType bloodType, int bloodPoints, int maxBloodPoints, boolean lookingDirectly, int charmedTicks)
    {
        this.entityId = entityId;
        this.bloodType = bloodType;
        this.bloodPoints = bloodPoints;
        this.maxBloodPoints = maxBloodPoints;
        this.lookingDirectly = lookingDirectly;
        this.charmedTicks = charmedTicks;
    }

    public EntityBloodInfoS2CPacket(FriendlyByteBuf buffer)
    {
        this.entityId = buffer.readInt();
        this.bloodType = buffer.readEnum(BloodType.class);
        this.bloodPoints = buffer.readInt();
        this.maxBloodPoints = buffer.readInt();
        this.lookingDirectly = buffer.readBoolean();
        this.charmedTicks = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
        buffer.writeEnum(this.bloodType);
        buffer.writeInt(this.bloodPoints);
        buffer.writeInt(this.maxBloodPoints);
        buffer.writeBoolean(this.lookingDirectly);
        buffer.writeInt(this.charmedTicks);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> ClientPacketHandler.handleEntityBloodInfo(this));

        context.setPacketHandled(true);

        return true;
    }
}
