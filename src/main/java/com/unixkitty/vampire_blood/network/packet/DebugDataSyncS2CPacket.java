package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DebugDataSyncS2CPacket extends BasePacket
{
    private final int ticksInSun;
    private final int ticksFeeding;
    private final int noRegenTicks;

    private final int thirstExhaustionIncrement;
    private final int thirstTickTimer;
    private final int[] diet;

    public DebugDataSyncS2CPacket(int ticksInSun, int ticksFeeding, int noRegenTicks, int thirstExhaustionIncrement, int thirstTickTimer, int[] diet)
    {
        this.ticksInSun = ticksInSun;
        this.ticksFeeding = ticksFeeding;
        this.noRegenTicks = noRegenTicks;

        this.thirstExhaustionIncrement = thirstExhaustionIncrement;
        this.thirstTickTimer = thirstTickTimer;
        this.diet = diet;
    }

    public DebugDataSyncS2CPacket(FriendlyByteBuf buffer)
    {
        this.ticksInSun = buffer.readInt();
        this.ticksFeeding = buffer.readInt();
        this.noRegenTicks = buffer.readVarInt();

        this.thirstExhaustionIncrement = buffer.readInt();
        this.thirstTickTimer = buffer.readVarInt();
        this.diet = buffer.readVarIntArray(20);
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.ticksInSun);
        buffer.writeInt(this.ticksFeeding);
        buffer.writeVarInt(this.noRegenTicks);

        buffer.writeInt(this.thirstExhaustionIncrement);
        buffer.writeVarInt(this.thirstTickTimer);
        buffer.writeVarIntArray(this.diet);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> ClientPacketHandler.handleDebugData(this.ticksFeeding, this.ticksInSun, this.noRegenTicks, this.thirstExhaustionIncrement, this.thirstTickTimer, this.diet));

        context.setPacketHandled(true);

        return true;
    }
}
