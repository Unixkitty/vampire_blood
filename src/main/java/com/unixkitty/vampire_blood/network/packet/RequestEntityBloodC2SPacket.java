package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.provider.BloodProvider;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestEntityBloodC2SPacket extends BasePacket
{
    private final int id;

    public RequestEntityBloodC2SPacket(int id)
    {
        this.id = id;
    }

    public RequestEntityBloodC2SPacket(FriendlyByteBuf buffer)
    {
        this.id = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.id);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            ServerPlayer player = context.getSender();

            if (player != null)
            {
                if (player.level.getEntity(this.id) instanceof LivingEntity livingEntity)
                {
                    if (livingEntity instanceof Player otherPlayer)
                    {
                        livingEntity.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData -> ModNetworkDispatcher.sendPlayerEntityBlood(player, vampirePlayerData.getBloodType(), vampirePlayerData.getBloodPoints(), vampirePlayerData.getMaxBloodPoints()));
                    }
                    else
                    {
                        livingEntity.getCapability(BloodProvider.BLOOD_STORAGE).ifPresent(bloodStorage -> ModNetworkDispatcher.sendPlayerEntityBlood(player, bloodStorage.getBloodType(), bloodStorage.getBloodPoints(), bloodStorage.getMaxBloodPoints()));
                    }
                }
            }
        });

        context.setPacketHandled(true);

        return true;
    }
}
