package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerVampireVarsResponseS2CPacket extends BasePacket
{
    private final int[] playerEntityIds;
    private final int[] playerVampireLevels;

    public PlayerVampireVarsResponseS2CPacket(int[] playerEntityIds, int[] playerVampireLevels)
    {
        this.playerEntityIds = playerEntityIds;
        this.playerVampireLevels = playerVampireLevels;
    }

    public PlayerVampireVarsResponseS2CPacket(FriendlyByteBuf buffer)
    {
        this.playerEntityIds = buffer.readVarIntArray();
        this.playerVampireLevels = buffer.readVarIntArray();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeVarIntArray(this.playerEntityIds);
        buffer.writeVarIntArray(this.playerVampireLevels);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> ClientPacketHandler.handleVampireVarsResponse(this.playerEntityIds, this.playerVampireLevels));

        context.setPacketHandled(true);

        return true;
    }
}
