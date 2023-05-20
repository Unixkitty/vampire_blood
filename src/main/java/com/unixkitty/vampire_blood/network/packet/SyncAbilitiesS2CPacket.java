package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
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

    public SyncAbilitiesS2CPacket(final Set<VampireActiveAbility> activeAbilities)
    {
        this.abilities = activeAbilities.stream().mapToInt(VampireActiveAbility::ordinal).toArray();
    }

    public SyncAbilitiesS2CPacket(FriendlyByteBuf buffer)
    {
        this.abilities = buffer.readVarIntArray(VampireActiveAbility.values().length);
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
            List<VampireActiveAbility> previousList = new ArrayList<>(ClientVampirePlayerDataCache.activeAbilities);

            ClientVampirePlayerDataCache.activeAbilities.clear();

            for (int id : abilities)
            {
                ClientVampirePlayerDataCache.activeAbilities.add(VampireActiveAbility.fromOrdinal(id));
            }

            if (Minecraft.getInstance().player != null)
            {
                for (VampireActiveAbility ability : VampireActiveAbility.values())
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
