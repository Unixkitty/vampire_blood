package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.player.VampirePlayerData;
import com.unixkitty.vampire_blood.client.ClientVampirePlayerDataCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DebugDataSyncS2CPacket
{
    private final int ticksInSun;
    private final int ticksFeeding;
    private final int noRegenTicks;

    private final int thirstExhaustion;
    private final int thirstExhaustionIncrement;
    private final int thirstTickTimer;

    public DebugDataSyncS2CPacket(VampirePlayerData data)
    {
        this.ticksInSun = data.getSunTicks();
        this.ticksFeeding = data.getFeedingTicks();
        this.noRegenTicks = data.getNoRegenTicks();

        this.thirstExhaustion = data.getThirstExhaustion();
        this.thirstExhaustionIncrement = data.getThirstExhaustionIncrement();
        this.thirstTickTimer = data.getThirstTickTimer();
    }

    public DebugDataSyncS2CPacket(FriendlyByteBuf buffer)
    {
        this.ticksInSun = buffer.readInt();
        this.ticksFeeding = buffer.readInt();
        this.noRegenTicks = buffer.readInt();

        this.thirstExhaustion = buffer.readInt();
        this.thirstExhaustionIncrement = buffer.readInt();
        this.thirstTickTimer = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.ticksInSun);
        buffer.writeInt(this.ticksFeeding);
        buffer.writeInt(this.noRegenTicks);

        buffer.writeInt(this.thirstExhaustion);
        buffer.writeInt(this.thirstExhaustionIncrement);
        buffer.writeInt(this.thirstTickTimer);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            ClientVampirePlayerDataCache.ticksFeeding = this.ticksFeeding;
            ClientVampirePlayerDataCache.Debug.ticksInSun = this.ticksInSun;
            ClientVampirePlayerDataCache.Debug.noRegenTicks = this.noRegenTicks;

            ClientVampirePlayerDataCache.Debug.thirstExhaustion = this.thirstExhaustion;
            ClientVampirePlayerDataCache.Debug.thirstExhaustionIncrement = this.thirstExhaustionIncrement;
            ClientVampirePlayerDataCache.Debug.thirstTickTimer = this.thirstTickTimer;
        });

        context.setPacketHandled(true);

        return true;
    }
}
