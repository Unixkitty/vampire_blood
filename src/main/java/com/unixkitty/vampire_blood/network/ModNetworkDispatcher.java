package com.unixkitty.vampire_blood.network;

import com.unixkitty.vampire_blood.VampireBlood;
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
    private static int id()
    {
        return packetId++;
    }

    public static void register()
    {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(VampireBlood.MODID + ":messages"), () -> PROTOCOL_VERSION, s -> true, s -> true);

        INSTANCE.messageBuilder(RequestFeedingC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestFeedingC2SPacket::new)
                .encoder(RequestFeedingC2SPacket::toBytes)
                .consumerMainThread(RequestFeedingC2SPacket::handle)
                .add();

        INSTANCE.messageBuilder(RequestStopFeedingC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestStopFeedingC2SPacket::new)
                .encoder(RequestStopFeedingC2SPacket::toBytes)
                .consumerMainThread(RequestStopFeedingC2SPacket::handle)
                .add();

        INSTANCE.messageBuilder(PlayerBloodDataSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PlayerBloodDataSyncS2CPacket::new)
                .encoder(PlayerBloodDataSyncS2CPacket::toBytes)
                .consumerMainThread(PlayerBloodDataSyncS2CPacket::handle)
                .add();

        INSTANCE.messageBuilder(DebugDataSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(DebugDataSyncS2CPacket::new)
                .encoder(DebugDataSyncS2CPacket::toBytes)
                .consumerMainThread(DebugDataSyncS2CPacket::handle)
                .add();

        INSTANCE.messageBuilder(PlayerVampireDataS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PlayerVampireDataS2CPacket::new)
                .encoder(PlayerVampireDataS2CPacket::toBytes)
                .consumerMainThread(PlayerVampireDataS2CPacket::handle)
                .add();

        INSTANCE.messageBuilder(PlayerRespawnS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PlayerRespawnS2CPacket::new)
                .encoder(PlayerRespawnS2CPacket::toBytes)
                .consumerMainThread(PlayerRespawnS2CPacket::handle)
                .add();

        INSTANCE.messageBuilder(RequestEntityBloodC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestEntityBloodC2SPacket::new)
                .encoder(RequestEntityBloodC2SPacket::toBytes)
                .consumerMainThread(RequestEntityBloodC2SPacket::handle)
                .add();

        INSTANCE.messageBuilder(EntityBloodResponseS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(EntityBloodResponseS2CPacket::new)
                .encoder(EntityBloodResponseS2CPacket::toBytes)
                .consumerMainThread(EntityBloodResponseS2CPacket::handle)
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
}
