package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DebugDataSyncS2CPacket extends BasePacket
{
    private final boolean catchingUV;
    private final float armourUVCoverage;
    private final int ticksInSun;
    private final long age;
    private final int noRegenTicks;

    private final int thirstExhaustionIncrement;
    private final int thirstTickTimer;
    private final int[] diet;

    public DebugDataSyncS2CPacket(boolean catchingUV, float armourUVCoverage, int ticksInSun, long age, int noRegenTicks, int thirstExhaustionIncrement, int thirstTickTimer, int[] diet)
    {
        this.catchingUV = catchingUV;
        this.armourUVCoverage = armourUVCoverage;
        this.ticksInSun = ticksInSun;
        this.age = age;
        this.noRegenTicks = noRegenTicks;

        this.thirstExhaustionIncrement = thirstExhaustionIncrement;
        this.thirstTickTimer = thirstTickTimer;
        this.diet = diet;
    }

    public DebugDataSyncS2CPacket(FriendlyByteBuf buffer)
    {
        this.catchingUV = buffer.readBoolean();
        this.armourUVCoverage = buffer.readFloat();
        this.ticksInSun = buffer.readInt();
        this.age = buffer.readLong();
        this.noRegenTicks = buffer.readVarInt();

        this.thirstExhaustionIncrement = buffer.readInt();
        this.thirstTickTimer = buffer.readVarInt();
        this.diet = buffer.readVarIntArray(20);
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(this.catchingUV);
        buffer.writeFloat(this.armourUVCoverage);
        buffer.writeInt(this.ticksInSun);
        buffer.writeLong(this.age);
        buffer.writeVarInt(this.noRegenTicks);

        buffer.writeInt(this.thirstExhaustionIncrement);
        buffer.writeVarInt(this.thirstTickTimer);
        buffer.writeVarIntArray(this.diet);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> ClientPacketHandler.handleDebugData(this.catchingUV, this.armourUVCoverage, this.ticksInSun, this.age, this.noRegenTicks, this.thirstExhaustionIncrement, this.thirstTickTimer, this.diet));

        context.setPacketHandled(true);

        return true;
    }
}
