package com.unixkitty.vampire_blood.event;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.VampirePlayerProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VampireBlood.MODID)
public class ModEvents
{
    @SubscribeEvent
    public static void onAttachCapabilityPlayer(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            if (!event.getObject().getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
            {
                event.addCapability(new ResourceLocation(VampireBlood.MODID, "vampirism"), new VampirePlayerProvider());
            }
        }
    }

    //TODO test
    /*
        If player dies, some capability data needs to be copied over to their newly created entity

        @SubscribeEvent(priority = EventPriority.HIGH)
        public void onPlayerClone(PlayerEvent.@NotNull Clone event) {
        if (!event.getEntity().getCommandSenderWorld().isClientSide) {
            event.getOriginal().reviveCaps();
            FactionPlayerHandler.get(event.getEntity()).copyFrom(event.getOriginal());
            event.getOriginal().invalidateCaps();
        }
    }
     */
    @SubscribeEvent
    public static void onPlayerCloned(final PlayerEvent.Clone event)
    {
        if (event.isWasDeath())
        {
            event.getOriginal().getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(
                    oldStore -> event.getOriginal().getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(
                            newStore -> newStore.copyOnDeath(oldStore)
                    )
            );
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(final EntityJoinLevelEvent event)
    {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player)
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                vampirePlayerData.sync();
                vampirePlayerData.syncBlood();
            });
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(final RegisterCapabilitiesEvent event)
    {
        event.register(VampirePlayerData.class);
    }

    @SubscribeEvent
    public static void onPlayerTick(final TickEvent.PlayerTickEvent event)
    {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END)
        {
            event.player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (!event.player.isCreative() && !event.player.isSpectator())
                {
                    vampirePlayerData.tick(event.player);
                }
            });
        }
    }
}
