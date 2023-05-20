package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.player.VampireActiveAbilities;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UseAbilityC2SPacket extends BasePacket
{
    private final VampireActiveAbilities ability;

    public UseAbilityC2SPacket(VampireActiveAbilities ability)
    {
        this.ability = ability;
    }

    public UseAbilityC2SPacket(FriendlyByteBuf buffer)
    {
        this.ability = buffer.readEnum(VampireActiveAbilities.class);
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeEnum(this.ability);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            ServerPlayer player = context.getSender();

            if (player != null)
            {
                player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData -> vampirePlayerData.toggleAbility(player, this.ability));
            }
        });

        context.setPacketHandled(true);

        return true;
    }
}
