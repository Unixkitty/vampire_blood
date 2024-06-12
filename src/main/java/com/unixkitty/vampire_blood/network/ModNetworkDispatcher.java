package com.unixkitty.vampire_blood.network;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.network.packet.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworkDispatcher
{
    private static final String PROTOCOL_VERSION = Integer.toString(1);

    private static SimpleChannel INSTANCE;

    private static int packetId = 0;

    public static void register()
    {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(VampireBlood.MODID, "messages"), () -> PROTOCOL_VERSION, ModNetworkDispatcher::shouldAcceptPacket, ModNetworkDispatcher::shouldAcceptPacket);

        //================================================================================================

        registerPacket(RequestFeedingC2SPacket.class, true);
        registerPacket(RequestStopFeedingC2SPacket.class, true);
        registerPacket(PlayerVampireDataS2CPacket.class, false);
        registerPacket(DebugDataSyncS2CPacket.class, false);
        registerPacket(PlayerFeedingStatusS2CPacket.class, false);
        registerPacket(RequestEntityBloodC2SPacket.class, true);
        registerPacket(EntityBloodInfoS2CPacket.class, false);
        registerPacket(PlayerAvoidHurtAnimS2CPacket.class, false);
        registerPacket(ToggleActiveAbilityC2SPacket.class, true);
        registerPacket(SyncAbilitiesS2CPacket.class, false);
        registerPacket(RequestEntityOutlineColorC2SPacket.class, true);
        registerPacket(EntityOutlineColorS2CPacket.class, false);
        registerPacket(UseCharmAbilityC2SPacket.class, true);
        registerPacket(EntityCharmedStatusS2CPacket.class, false);
        registerPacket(BloodParticlesS2CPacket.class, false);
        registerPacket(SuccessfulCharmS2CPacket.class, false);
        registerPacket(RequestOtherPlayerVampireVarsC2SPacket.class, true);
        registerPacket(PlayerVampireVarsResponseS2CPacket.class, false);
    }

    private static <T extends BasePacket> void registerPacket(Class<T> packetClass, boolean toServer)
    {
        INSTANCE.messageBuilder(packetClass, packetId++, toServer ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT)
                .decoder(buf ->
                {
                    try
                    {
                        return packetClass.getDeclaredConstructor(buf.getClass()).newInstance(buf);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("Failed to decode packet " + packetClass.getSimpleName(), e);
                    }
                })
                .encoder(BasePacket::toBytes)
                .consumerMainThread(BasePacket::handle)
                .add();
    }

    private static boolean shouldAcceptPacket(String protocolVersion)
    {
        return PROTOCOL_VERSION.equals(protocolVersion);
    }

    public static void sendToServer(BasePacket message)
    {
        INSTANCE.sendToServer(message);
    }

    public static void sendToClient(BasePacket message, ServerPlayer player)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToNearbyPlayers(BasePacket message, final double x, final double y, final double z, final double radius, final ResourceKey<Level> dimension)
    {
        INSTANCE.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(x, y, z, radius, dimension)), message);
    }

    //========================================

    public static void sendPlayerEntityBlood(ServerPlayer player, int entityId, BloodType bloodType, int bloodPoints, int maxBloodPoints, boolean lookingDirectly, int charmedTicks)
    {
        sendToClient(new EntityBloodInfoS2CPacket(entityId, bloodType, bloodPoints, maxBloodPoints, lookingDirectly, charmedTicks), player);
    }

    public static void notifyPlayerFeeding(ServerPlayer player, boolean value)
    {
        sendToClient(new PlayerFeedingStatusS2CPacket(value), player);
    }

    public static void sendBloodParticles(ServerPlayer player, Vec3 position)
    {
        INSTANCE.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(player, position.x, position.y, position.z, 16.0D, player.level().dimension())), new BloodParticlesS2CPacket(position));
    }
}
