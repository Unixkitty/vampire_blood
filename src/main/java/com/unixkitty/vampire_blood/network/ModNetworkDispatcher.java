package com.unixkitty.vampire_blood.network;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.network.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(VampireBlood.MODID, "messages"), () -> PROTOCOL_VERSION, s -> true, s -> true);

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
    }

    private static <T extends BasePacket> void registerPacket(Class<T> packetClass, boolean toServer)
    {
        INSTANCE.messageBuilder(packetClass, packetId++, toServer ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT)
                .decoder(buf -> {
                    try
                    {
                        return packetClass.getDeclaredConstructor(buf.getClass()).newInstance(buf);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("Failed to decode packet", e);
                    }
                })
                .encoder(BasePacket::toBytes)
                .consumerMainThread(BasePacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message)
    {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToClient(MSG message, ServerPlayer player)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    //========================================

    public static void sendPlayerEntityBlood(ServerPlayer player, int entityId, BloodType bloodType, int bloodPoints, int maxBloodPoints, boolean lookingDirectly)
    {
        sendToClient(new EntityBloodInfoS2CPacket(entityId, bloodType, bloodPoints, maxBloodPoints, lookingDirectly), player);
    }

    public static void notifyPlayerFeeding(ServerPlayer player, boolean value)
    {
        sendToClient(new PlayerFeedingStatusS2CPacket(value), player);
    }
}
