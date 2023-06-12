package com.unixkitty.vampire_blood.network.packet;

import com.unixkitty.vampire_blood.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BloodParticlesS2CPacket extends BasePacket
{
    public final double x;
    public final double y;
    public final double z;

    public BloodParticlesS2CPacket(Vec3 position)
    {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
    }

    public BloodParticlesS2CPacket(FriendlyByteBuf buffer)
    {
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> ClientPacketHandler.handleBloodParticles(new Vec3(this.x, this.y, this.z)));

        context.setPacketHandled(true);

        return true;
    }
}
