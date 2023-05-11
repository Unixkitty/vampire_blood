package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.client.ClientVampirePlayerDataCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DebugDataSyncS2CPacket
{
    private final int ticksInSun;
    private final int ticksFeeding;
    private final int noRegenTicks;

    private final int thirstExhaustionIncrement;
    private final int thirstTickTimer;
    private final int consecutiveBloodtypePoints;

    public DebugDataSyncS2CPacket(int ticksInSun, int ticksFeeding, int noRegenTicks, int thirstExhaustionIncrement, int thirstTickTimer, int consecutiveBloodtypePoints)
    {
        this.ticksInSun = ticksInSun;
        this.ticksFeeding = ticksFeeding;
        this.noRegenTicks = noRegenTicks;

        this.thirstExhaustionIncrement = thirstExhaustionIncrement;
        this.thirstTickTimer = thirstTickTimer;
        this.consecutiveBloodtypePoints = consecutiveBloodtypePoints;
    }

    public DebugDataSyncS2CPacket(FriendlyByteBuf buffer)
    {
        this.ticksInSun = buffer.readInt();
        this.ticksFeeding = buffer.readInt();
        this.noRegenTicks = buffer.readInt();

        this.thirstExhaustionIncrement = buffer.readInt();
        this.thirstTickTimer = buffer.readInt();
        this.consecutiveBloodtypePoints = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.ticksInSun);
        buffer.writeInt(this.ticksFeeding);
        buffer.writeInt(this.noRegenTicks);

        buffer.writeInt(this.thirstExhaustionIncrement);
        buffer.writeInt(this.thirstTickTimer);
        buffer.writeInt(this.consecutiveBloodtypePoints);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            ClientVampirePlayerDataCache.ticksFeeding = this.ticksFeeding;
            ClientVampirePlayerDataCache.Debug.ticksInSun = this.ticksInSun;
            ClientVampirePlayerDataCache.Debug.noRegenTicks = this.noRegenTicks;

            ClientVampirePlayerDataCache.Debug.thirstExhaustionIncrement = this.thirstExhaustionIncrement;
            ClientVampirePlayerDataCache.Debug.thirstTickTimer = this.thirstTickTimer;
            ClientVampirePlayerDataCache.Debug.consecutiveBloodtypePoints = this.consecutiveBloodtypePoints;
        });

        context.setPacketHandled(true);

        return true;
    }
}
