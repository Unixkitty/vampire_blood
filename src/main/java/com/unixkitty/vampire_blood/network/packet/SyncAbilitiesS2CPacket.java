package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.player.VampireActiveAbilities;
import com.unixkitty.vampire_blood.client.cache.ClientVampirePlayerDataCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class SyncAbilitiesS2CPacket extends BasePacket
{
    private final int[] abilities;

    public SyncAbilitiesS2CPacket(final Set<VampireActiveAbilities> activeAbilities)
    {
        this.abilities = activeAbilities.stream().mapToInt(VampireActiveAbilities::ordinal).toArray();
    }

    public SyncAbilitiesS2CPacket(FriendlyByteBuf buffer)
    {
        this.abilities = buffer.readVarIntArray(VampireActiveAbilities.values().length);
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeVarIntArray(this.abilities);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            List<VampireActiveAbilities> previousList = new ArrayList<>(ClientVampirePlayerDataCache.activeAbilities);

            ClientVampirePlayerDataCache.activeAbilities.clear();

            for (int id : abilities)
            {
                ClientVampirePlayerDataCache.activeAbilities.add(VampireActiveAbilities.fromOrdinal(id));
            }

            if (Minecraft.getInstance().player != null)
            {
                for (VampireActiveAbilities ability : VampireActiveAbilities.values())
                {
                    if (ClientVampirePlayerDataCache.activeAbilities.contains(ability))
                    {
                        ability.refresh(Minecraft.getInstance().player);
                    }
                    else if (previousList.contains(ability))
                    {
                        ability.stop(Minecraft.getInstance().player);
                    }
                }
            }
        });

        context.setPacketHandled(true);

        return true;
    }
}
