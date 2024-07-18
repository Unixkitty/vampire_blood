package com.unixkitty.vampire_blood.compat;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.init.ModItems;
import com.unixkitty.vampire_blood.item.BloodBottleItem;
import favouriteless.enchanted.api.rites.AbstractRite;
import favouriteless.enchanted.common.init.registry.EnchantedBlocks;
import favouriteless.enchanted.common.init.registry.EnchantedItems;
import favouriteless.enchanted.common.rites.CirclePart;
import favouriteless.enchanted.common.rites.RiteType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class SummonOriginalRite extends AbstractRite
{
    public SummonOriginalRite(RiteType<?> type, ServerLevel level, BlockPos pos, UUID caster)
    {
        super(type, level, pos, caster, 3000, 0);
        CIRCLES_REQUIRED.put(CirclePart.LARGE, EnchantedBlocks.OTHERWHERE_CHALK.get());
        ITEMS_REQUIRED.put(EnchantedItems.WAYSTONE.get(), 1);
        ITEMS_REQUIRED.put(ModItems.HUMAN_BLOOD_BOTTLE.get(), 1);
        ITEMS_REQUIRED.put(EnchantedItems.ENDER_DEW.get(), 1);
        ITEMS_REQUIRED.put(EnchantedItems.BLOOD_POPPY.get(), 1);
        ITEMS_REQUIRED.put(EnchantedItems.DEMONIC_BLOOD.get(), 1);
        ITEMS_REQUIRED.put(EnchantedItems.HINT_OF_REBIRTH.get(), 1);
    }

    @Override
    public void execute()
    {
        setTargetUUID(UUID.fromString("9d64fee0-582d-4775-b6ef-37d6e6d3f429"));

        if (getTargetEntity() == null)
        {
            tryFindTargetEntity();
        }

        ServerLevel level = getLevel();
        BlockPos pos = getPos();
        Entity targetEntity = getTargetEntity();

        if (level != null && pos != null)
        {
            if (targetEntity == null)
            {
                targetEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), BloodBottleItem.getItem(BloodType.VAMPIRE).getDefaultInstance());
            }

            spawnParticles(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            spawnParticles((ServerLevel) targetEntity.level(), targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
            level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.ENDERMAN_TELEPORT, SoundSource.MASTER, 1.0F, 1.0F);
            targetEntity.level().playSound(null, targetEntity.getX(), targetEntity.getX(), targetEntity.getY(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.MASTER, 1.0F, 1.0F);

            Vec3 destination = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            if (level != targetEntity.level())
            {
                targetEntity.changeDimension(level);
            }
            else
            {
                targetEntity.teleportTo(destination.x, destination.y, destination.z);
            }
        }
        else
        {
            cancel();
        }
        stopExecuting();
    }

    protected void spawnParticles(ServerLevel world, double x, double y, double z)
    {
        for (int i = 0; i < 25; i++)
        {
            double dx = x - 0.5D + (Math.random() * 1.5D);
            double dy = y + (Math.random() * 2.0D);
            double dz = z - 0.5D + (Math.random() * 1.5D);
            world.sendParticles(ParticleTypes.PORTAL, dx, dy, dz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }
}
