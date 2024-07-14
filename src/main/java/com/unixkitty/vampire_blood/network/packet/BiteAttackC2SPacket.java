package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.init.ModDamageTypes;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BiteAttackC2SPacket extends BasePacket
{
    private final int entityId;

    public BiteAttackC2SPacket(int entityId)
    {
        this.entityId = entityId;
    }

    public BiteAttackC2SPacket(FriendlyByteBuf buffer)
    {
        this.entityId = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            ServerPlayer player = context.getSender();

            if (player != null && VampireUtil.isVampire(player))
            {
                Entity entity = player.level().getEntity(this.entityId);

                if (entity instanceof LivingEntity victim)
                {
                    //TODO damage calculation instead of hardcoded 6F ?
                    victim.hurt(ModDamageTypes.source(ModDamageTypes.BITE_ATTACK, victim.level(), player), 6F);
                }
            }
        });

        context.setPacketHandled(true);

        return true;
    }
}
