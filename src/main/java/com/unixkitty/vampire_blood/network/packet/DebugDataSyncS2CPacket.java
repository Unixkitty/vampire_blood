package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.VampirePlayerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DebugDataSyncS2CPacket
{
    private final int ticksInSun;
    private final int ticksFeeding;

    private final int thirstExhaustion;
    private final int thirstExhaustionIncrement;
    private final int thirstTickTimer;

    public DebugDataSyncS2CPacket(VampirePlayerData data)
    {
        this.ticksInSun = data.getSunTicks();
        this.ticksFeeding = data.getFeedingTicks();

        this.thirstExhaustion = data.getThirstExhaustion();
        this.thirstExhaustionIncrement = data.getThirstExhaustionIncrement();
        this.thirstTickTimer = data.getThirstTickTimer();
    }

    public DebugDataSyncS2CPacket(FriendlyByteBuf buffer)
    {
        this.ticksInSun = buffer.readInt();
        this.ticksFeeding = buffer.readInt();

        this.thirstExhaustion = buffer.readInt();
        this.thirstExhaustionIncrement = buffer.readInt();
        this.thirstTickTimer = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.ticksInSun);
        buffer.writeInt(this.ticksFeeding);

        buffer.writeInt(this.thirstExhaustion);
        buffer.writeInt(this.thirstExhaustionIncrement);
        buffer.writeInt(this.thirstTickTimer);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
                Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
                        vampirePlayerData.setClientDebugData(this.ticksInSun, this.ticksFeeding, this.thirstExhaustion, this.thirstExhaustionIncrement, this.thirstTickTimer)));

        context.setPacketHandled(true);

        return true;
    }
}
