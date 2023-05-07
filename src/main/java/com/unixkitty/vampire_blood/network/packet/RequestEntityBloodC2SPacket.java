package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.provider.BloodProvider;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestEntityBloodC2SPacket
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
                Entity entity = player.level.getEntity(this.id);

                if (entity instanceof PathfinderMob)
                {
                    entity.getCapability(BloodProvider.BLOOD_STORAGE).ifPresent(bloodStorage ->
                            ModNetworkDispatcher.sendToClient(new EntityBloodResponseS2CPacket(bloodStorage.getBloodType().getId(), bloodStorage.getBloodPoints(), bloodStorage.getMaxBloodPoints()), player));
                }
                else if (entity instanceof Player)
                {
                    entity.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
                    {
                        BloodType bloodType = vampirePlayerData.getBloodTypeIdForFeeding();

                        ModNetworkDispatcher.sendToClient(new EntityBloodResponseS2CPacket(bloodType.getId(), VampireUtil.healthToBlood(((Player) entity).getHealth(), bloodType), VampireUtil.healthToBlood(((Player) entity).getMaxHealth(), bloodType)), player);
                    });
                }
            }
        });

        context.setPacketHandled(true);

        return true;
    }
}
