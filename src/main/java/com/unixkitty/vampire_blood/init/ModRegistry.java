package com.unixkitty.vampire_blood.init;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.VampireCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = VampireBlood.MODID)
public final class ModRegistry
{
    public static final DamageSource SUN_DAMAGE = new DamageSource("sunlight")
    {
        @Override
        public Component getLocalizedDeathMessage(LivingEntity whoDied)
        {
            return Component.translatable("vampire_blood.death.attack.sunlight_" + whoDied.getRandom().nextIntBetweenInclusive(1, 9), whoDied.getDisplayName());
        }
    }.bypassArmor().setMagic();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VampireBlood.MODID);

    public static final RegistryObject<Item> VAMPIRE_DUST = ITEMS.register("vampire_dust", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_BREWING)));

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event)
    {
        VampireCommand.register(event.getDispatcher());
    }
}
