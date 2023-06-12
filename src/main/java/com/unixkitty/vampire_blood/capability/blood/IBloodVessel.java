package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public interface IBloodVessel
{
    String CHARMED_BY_NBT_NAME = "charmedBy";

    boolean isEdible();

    int getBloodPoints();

    int getMaxBloodPoints();

    BloodType getBloodType();

    void dieFromBloodLoss(@Nonnull LivingEntity victim, @Nonnull LivingEntity attacker);

    void drinkFromHealth(@Nonnull LivingEntity attacker, @Nonnull LivingEntity victim, @Nonnull BloodType bloodType);

    boolean decreaseBlood(@Nonnull LivingEntity attacker, @Nonnull LivingEntity victim);

    boolean tryGetCharmed(ServerPlayer player, VampirismLevel attackerLevel);

    boolean isCharmedBy(ServerPlayer player);

    int getCharmedByTicks(ServerPlayer player);

    boolean setCharmedBy(ServerPlayer player);

    void handleBeingCharmedTicks(LivingEntity entity);
}
