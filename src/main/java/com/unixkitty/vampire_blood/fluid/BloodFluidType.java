package com.unixkitty.vampire_blood.fluid;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.init.ModBlocks;
import com.unixkitty.vampire_blood.init.ModFluids;
import com.unixkitty.vampire_blood.init.ModItems;
import com.unixkitty.vampire_blood.init.ModParticles;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class BloodFluidType extends FluidType
{
    private final ResourceLocation still_texture;
    private final ResourceLocation flow_texture;
    private final ResourceLocation overlay_texture;
    private final int tint;

    public BloodFluidType(BloodType bloodType)
    {
        super(Properties.create()
                .fallDistanceModifier(0F)
                .canExtinguish(true)
                .supportsBoating(true)
                .canHydrate(true)
                .density(2000)
                .motionScale(0.007D)
                .rarity(ModItems.getBloodItemRarity(bloodType))
                .viscosity(3000)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
        );

        this.still_texture = new ResourceLocation(VampireBlood.MODID, "block/blood_still");
        this.flow_texture = new ResourceLocation(VampireBlood.MODID, "block/blood_flowing");
        this.overlay_texture = new ResourceLocation(VampireBlood.MODID, "textures/misc/blood_overlay.png");
        this.tint = switch (bloodType)
        {
            case NONE -> 0;
            case FRAIL -> 0xFF300000;
            case CREATURE -> 0xFF6F3700;
            case HUMAN -> 0xFF6F0000;
            case VAMPIRE -> 0xFF9A0D31;
            case PIGLIN -> 0xFF802A2A;
        };
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
    {
        consumer.accept(new IClientFluidTypeExtensions()
        {
            @Override
            public ResourceLocation getStillTexture()
            {
                return still_texture;
            }

            @Override
            public ResourceLocation getFlowingTexture()
            {
                return flow_texture;
            }

            @Override
            public @Nullable ResourceLocation getRenderOverlayTexture(Minecraft mc)
            {
                return overlay_texture;
            }

            @Override
            public int getTintColor()
            {
                return tint;
            }

            @Override
            public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor)
            {
                return new Vector3f(
                        0.188F * ((level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(camera.getBlockPosition()) / 10F) + 1F),
                        0F, 0F
                );
            }
        });
    }

    public static class RegistryContainer
    {
        public final RegistryObject<FluidType> type;
        public final RegistryObject<LiquidBlock> block;
        public final RegistryObject<BucketItem> bucket;
        private ForgeFlowingFluid.Properties properties;
        public final RegistryObject<ForgeFlowingFluid.Source> source;
        public final RegistryObject<ForgeFlowingFluid.Flowing> flowing;

        public RegistryContainer(BloodType bloodType)
        {
            String name = bloodType.name().toLowerCase() + "_blood";
            this.type = ModFluids.FLUID_TYPES.register(name, () -> new BloodFluidType(bloodType));
            this.source = ModFluids.FLUIDS.register(name, () -> new ForgeFlowingFluid.Source(this.properties)
            {
                @Override
                public int getAmount(FluidState state)
                {
                    return 6;
                }

                @Override
                protected ParticleOptions getDripParticle()
                {
                    return ModParticles.DRIPPING_BLOOD.get();
                }
            });
            this.flowing = ModFluids.FLUIDS.register(name + "_flowing", () -> new ForgeFlowingFluid.Flowing(this.properties));
            this.properties = new ForgeFlowingFluid.Properties(this.type, this.source, this.flowing).tickRate(10);

            this.properties.block(this.block = ModBlocks.BLOCKS.register(name, () -> new LiquidBlock(this.source, BlockBehaviour.Properties.copy(Blocks.WATER).mapColor(MapColor.COLOR_RED))));
            this.properties.bucket(this.bucket = ModItems.ITEMS.register(name + "_bucket", () -> new BucketItem(this.source, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).rarity(ModItems.getBloodItemRarity(bloodType)))));
        }
    }
}
