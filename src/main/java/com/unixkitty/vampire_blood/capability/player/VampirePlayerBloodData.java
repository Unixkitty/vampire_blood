package com.unixkitty.vampire_blood.capability.player;

import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.PlayerVampireDataS2CPacket;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;

public class VampirePlayerBloodData
{
    public static final int MAX_THIRST = 40;

    VampirismStage vampireLevel = VampirismStage.NOT_VAMPIRE;
    BloodType bloodType = BloodType.HUMAN;
    int thirstLevel = 1;
    int thirstExhaustion; //This is somewhat similar to vanilla's in FoodData.java, decrease thirst level if this gets up to some limit, and change this back to 0
    int thirstExhaustionIncrement; //This helps make thirstExhaustion more dynamic based on configs and player actions
    int thirstTickTimer; //This is used for healing and starvation
    int noRegenTicks;
    float bloodlust;
    BloodType lastConsumedBloodtype = bloodType;
    int consecutiveBloodtypePoints;

    private boolean needsSync = false;

    VampirePlayerBloodData()
    {
    }

    private void syncData(ServerPlayer player)
    {
        if (this.needsSync)
        {
            this.needsSync = false;

            ModNetworkDispatcher.sendToClient(new PlayerVampireDataS2CPacket(this.vampireLevel, this.bloodType, this.thirstLevel, this.thirstExhaustion, this.bloodlust), player);
        }
    }

    void sync()
    {
        this.needsSync = true;
    }

    void addBlood(ServerPlayer player, int points, BloodType bloodType)
    {
        this.thirstLevel = Math.min(this.thirstLevel + points, VampirePlayerBloodData.MAX_THIRST);

        updateBloodType(bloodType);

        updateBloodlust(player, true);

        notifyAndUpdate(player);
    }

    void decreaseBlood(ServerPlayer player, int points, BloodType bloodType)
    {
        this.thirstLevel = Math.max(this.thirstLevel - points, 0);

        updateBloodType(bloodType);

        updateBloodlust(player, false);

        notifyAndUpdate(player);
    }

    private void updateBloodlust(ServerPlayer player, boolean bloodPointGained)
    {
//        float lastBloodlust = this.bloodlust;

        float thirstMultiplier = (float) thirstLevel / MAX_THIRST;

        if (bloodPointGained)
        {
            this.bloodlust -= vampireLevel.getBloodlustMultiplier(true) * bloodType.getBloodlustMultiplier(true) * thirstMultiplier;
            this.bloodlust = Math.max(this.bloodlust, 0.0F);
        }
        else
        {
            this.bloodlust += vampireLevel.getBloodlustMultiplier(false) * bloodType.getBloodlustMultiplier(false) * (1.0F - thirstMultiplier);
            this.bloodlust = Math.min(this.bloodlust, 100.0F);
        }

        //TODO remove debug
//        if (lastBloodlust != this.bloodlust)
//        {
//            player.sendSystemMessage(Component.literal(
//                    "Bloodlust: " + lastBloodlust + " -> " + this.bloodlust + " (" + (this.bloodlust - lastBloodlust) + ")"
//                            + " level (" + this.vampireLevel.name().toLowerCase() + "): " + this.vampireLevel.getBloodlustMultiplier(bloodPointGained)
//                            + " bloodType (" + this.bloodType.name().toLowerCase() + "): " + this.bloodType.getBloodlustMultiplier(bloodPointGained)
//            ));
//        }
    }

    private void updateBloodType(BloodType bloodType)
    {
        if (this.lastConsumedBloodtype != bloodType)
        {
            this.lastConsumedBloodtype = bloodType;
            this.consecutiveBloodtypePoints = 1;
        }
        else
        {
            if (this.bloodType != bloodType && this.consecutiveBloodtypePoints >= MAX_THIRST / 2)
            {
                this.bloodType = bloodType;
            }

            if (this.consecutiveBloodtypePoints < MAX_THIRST / 2)
            {
                ++this.consecutiveBloodtypePoints;
            }
        }
    }

    void checkOriginal(ServerPlayer player)
    {
        if (this.vampireLevel == VampirismStage.ORIGINAL && !player.getStringUUID().equals("9d64fee0-582d-4775-b6ef-37d6e6d3f429"))
        {
            this.vampireLevel = VampirismStage.MATURE;
        }
    }

    void notifyAndUpdate(ServerPlayer player)
    {
        checkOriginal(player);
        VampireAttributeModifiers.updateAttributes(player, this.vampireLevel, this.bloodType);
        sync();
    }

    void tick(ServerPlayer player)
    {
        player.level.getProfiler().push("vampire_blood_tick");

        boolean isPeaceful = player.level.getDifficulty() == Difficulty.PEACEFUL;

        float vanillaExhaustionDelta = player.getFoodData().getExhaustionLevel() * Config.bloodUsageRate.get();

        //Keep vanilla food level in the middle
        player.getFoodData().setFoodLevel(10);
        player.getFoodData().setSaturation(0);
        player.getFoodData().setExhaustion(0);

        if (this.thirstExhaustion >= 100)
        {
            this.thirstExhaustion -= 100;

            if (!isPeaceful)
            {
                decreaseBlood(player, 1, this.bloodType);
            }
        }

        if (vanillaExhaustionDelta > 0)
        {
            exhaustionIncrementFromVanilla(vanillaExhaustionDelta);
        }

        exhaustionIncrement();

        handleRegenAndStarvation(player, isPeaceful);

        if (this.thirstExhaustionIncrement >= Config.bloodUsageRate.get())
        {
            this.thirstExhaustionIncrement -= Config.bloodUsageRate.get();
            this.thirstExhaustion++;

            sync();
        }

        this.syncData(player);

        player.level.getProfiler().pop();
    }

    private void handleRegenAndStarvation(ServerPlayer player, boolean isPeaceful)
    {
        //Check if we should do natural regen
        if (player.isHurt())
        {
            if (noRegenTicks > 0)
            {
                --noRegenTicks;
            }
            else
            {
                ++this.thirstTickTimer;

                //Standard HP regen when above 1/6th blood
                if (this.thirstLevel > MAX_THIRST / 6 && this.thirstTickTimer >= Config.naturalHealingRate.get())
                {
                    player.heal(VampireUtil.getHealthRegenRate(player));

                    exhaustionIncrement(BloodUsageRates.HEALING, Config.naturalHealingRate.get());

                    this.thirstTickTimer = 0;
                }
                //Slower HP regen when still have some blood below 1/6th
                else if (this.thirstTickTimer >= Config.naturalHealingRate.get() * 4)
                {
                    player.heal(VampireUtil.getHealthRegenRate(player));

                    exhaustionIncrement(BloodUsageRates.HEALING_SLOW, Config.naturalHealingRate.get());

                    this.thirstTickTimer = 0;
                }
            }
        }
        //Replenish thirst if playing on Peaceful
        else if (isPeaceful && this.thirstLevel < MAX_THIRST)
        {
            ++this.thirstTickTimer;

            if (this.thirstTickTimer >= 20)
            {
                addBlood(player, 1, BloodType.HUMAN);

                this.thirstTickTimer = 0;
            }
        }
        //Starving
        else if (this.thirstLevel <= 0)
        {
            ++this.thirstTickTimer;

            if (this.thirstTickTimer >= 80)
            {
                if (player.getHealth() > 10.0F || player.level.getDifficulty() == Difficulty.HARD || player.getHealth() > 1.0F && player.level.getDifficulty() == Difficulty.NORMAL)
                {
                    player.hurt(DamageSource.STARVE, 1.0F);
                }

                this.thirstTickTimer = 0;
            }
        }
        else
        {
            this.thirstTickTimer = 0;
        }
    }

    private void exhaustionIncrementFromVanilla(float vanillaExhaustionDelta)
    {
        this.thirstExhaustionIncrement += vanillaExhaustionDelta < 1.0F ? 1 : vanillaExhaustionDelta;
    }

    private void exhaustionIncrement()
    {
        this.thirstExhaustionIncrement += BloodUsageRates.IDLE.get();
    }

    private void exhaustionIncrement(BloodUsageRates rate, int ticks)
    {
        this.thirstExhaustionIncrement += rate.get() * ticks;
    }
}
