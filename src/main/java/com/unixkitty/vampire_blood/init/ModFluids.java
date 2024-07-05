package com.unixkitty.vampire_blood.init;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.fluid.BloodFluidType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModFluids
{
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, VampireBlood.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, VampireBlood.MODID);

    public static final BloodFluidType.RegistryContainer FRAIL_BLOOD = new BloodFluidType.RegistryContainer(BloodType.FRAIL);
    public static final BloodFluidType.RegistryContainer CREATURE_BLOOD = new BloodFluidType.RegistryContainer(BloodType.CREATURE);
    public static final BloodFluidType.RegistryContainer HUMAN_BLOOD = new BloodFluidType.RegistryContainer(BloodType.HUMAN);
    public static final BloodFluidType.RegistryContainer VAMPIRE_BLOOD = new BloodFluidType.RegistryContainer(BloodType.VAMPIRE);
    public static final BloodFluidType.RegistryContainer PIGLIN_BLOOD = new BloodFluidType.RegistryContainer(BloodType.PIGLIN);

    public static void postInit()
    {
        for (BloodType bloodType : BloodType.values())
        {
            if (bloodType != BloodType.NONE)
            {
                FluidInteractionRegistry.addInteraction(switch (bloodType)
                {
                    case FRAIL -> FRAIL_BLOOD.type.get();
                    case CREATURE -> CREATURE_BLOOD.type.get();
                    case HUMAN -> HUMAN_BLOOD.type.get();
                    case VAMPIRE -> VAMPIRE_BLOOD.type.get();
                    case PIGLIN -> PIGLIN_BLOOD.type.get();
                    default -> throw new IllegalStateException("Unexpected value: " + bloodType);
                }, new FluidInteractionRegistry.InteractionInformation(
                        ForgeMod.LAVA_TYPE.get(),
                        fluidState -> Config.bloodAndLavaCreateNetherrack.get() ? Blocks.NETHERRACK.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState()
                ));
            }
        }
    }
}
