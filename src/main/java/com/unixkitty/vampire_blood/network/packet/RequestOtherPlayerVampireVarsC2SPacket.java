package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestOtherPlayerVampireVarsC2SPacket extends BasePacket
{
    private final int[] playerEntityIds;

    public RequestOtherPlayerVampireVarsC2SPacket(int[] playerEntityIds)
    {
        this.playerEntityIds = playerEntityIds;
    }

    public RequestOtherPlayerVampireVarsC2SPacket(FriendlyByteBuf buffer)
    {
        this.playerEntityIds = buffer.readVarIntArray();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeVarIntArray(this.playerEntityIds);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            ServerPlayer senderPlayer = context.getSender();

            if (senderPlayer != null)
            {
                final Int2IntOpenHashMap map = new Int2IntOpenHashMap();

                for (int id : this.playerEntityIds)
                {
                    if (id != senderPlayer.getId() && senderPlayer.level.getEntity(id) instanceof ServerPlayer player && !player.isSpectator())
                    {
                        int playerVampireLevelId = -737;

                        playerVampireLevelId = player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.getVampireLevel().getId()).orElse(playerVampireLevelId);

                        if (playerVampireLevelId != -737)
                        {
                            map.put(id, playerVampireLevelId);
                        }
                    }
                }

                if (!map.isEmpty())
                {
                    ModNetworkDispatcher.sendToClient(new PlayerVampireVarsResponseS2CPacket(map.keySet().toIntArray(), map.values().toIntArray()), senderPlayer);
                }
            }
        });

        context.setPacketHandled(true);

        return true;
    }
}
