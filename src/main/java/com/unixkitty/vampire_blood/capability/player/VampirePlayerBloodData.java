package com.unixkitty.vampire_blood.capability.player;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.PlayerVampireDataS2CPacket;
import com.unixkitty.vampire_blood.network.packet.SyncAbilitiesS2CPacket;
import com.unixkitty.vampire_blood.util.VampireUtil;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;

public class VampirePlayerBloodData
{
    public static final int MAX_THIRST = 40;

    VampirismLevel vampireLevel = VampirismLevel.NOT_VAMPIRE;
    BloodType bloodType = BloodType.HUMAN;
    float bloodPurity = 1.0F;
    int thirstLevel = 1;
    int thirstExhaustion; //This is somewhat similar to vanilla's in FoodData.java, decrease thirst level if this gets up to some limit, and change this back to 0
    int thirstExhaustionIncrement; //This helps make thirstExhaustion more dynamic based on configs and player actions
    int thirstTickTimer; //This is used for healing and starvation
    int noRegenTicks;
    float bloodlust;

    final ObjectArraySet<VampireActiveAbility> activeAbilities = new ObjectArraySet<>();

    final VampirePlayerDiet diet = new VampirePlayerDiet(this.bloodType);

    private boolean needsSync = false;

    VampirePlayerBloodData()
    {
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
                decreaseBlood(true);
            }
        }

        if (vanillaExhaustionDelta > 0)
        {
            exhaustionIncrementFromVanilla(vanillaExhaustionDelta);
        }

        exhaustionIncrement(BloodUsageRates.IDLE);

        handleRegenAndStarvation(player, isPeaceful);

        handleAbilityExhaustion();

        if (this.thirstExhaustionIncrement >= Config.bloodUsageRate.get())
        {
            this.thirstExhaustionIncrement -= Config.bloodUsageRate.get();
            this.thirstExhaustion++;

            sync();
        }

        this.syncData(player);

        player.level.getProfiler().pop();
    }

    void sync()
    {
        this.needsSync = true;
    }

    void addBlood(ServerPlayer player, int points, BloodType bloodType)
    {
        this.thirstLevel = Math.min(this.thirstLevel + points, VampirePlayerBloodData.MAX_THIRST);

        updateDiet(bloodType);

        updateBloodlust(true);

        updateWithAttributes(player, false);
    }

    void decreaseBlood(boolean natural)
    {
        this.thirstLevel = Math.max(this.thirstLevel - 1, 0);

        if (natural)
        {
            updateBloodlust(false);
        }

        sync();
    }

    void updateDiet(BloodType bloodType)
    {
        var result = diet.updateWith(bloodType);

        this.bloodType = result.left();

        this.bloodPurity = result.rightFloat();
    }

    void checkOriginal(ServerPlayer player)
    {
        if (this.vampireLevel == VampirismLevel.ORIGINAL && !player.getStringUUID().equals("9d64fee0-582d-4775-b6ef-37d6e6d3f429"))
        {
            this.vampireLevel = VampirismLevel.MATURE;
        }
    }

    void updateWithAttributes(ServerPlayer player, boolean force)
    {
        checkOriginal(player);

        float lastHealthFactor = player.getHealth() / player.getMaxHealth();

        VampireUtil.updateAttributes(player, this.vampireLevel, this.bloodType, this.bloodPurity, this.activeAbilities);

        if (player.getHealth() / player.getMaxHealth() < lastHealthFactor)
        {
            player.setHealth(player.getMaxHealth() * lastHealthFactor);
        }

        sync();

        if (force)
        {
            this.syncData(player);
        }
    }

    void addPreventRegenTicks(int amount)
    {
        this.noRegenTicks = Math.min((this.noRegenTicks + amount), Config.noRegenTicksLimit.get());
    }

    private void syncData(ServerPlayer player)
    {
        if (this.needsSync)
        {
            this.needsSync = false;

            ModNetworkDispatcher.sendToClient(new PlayerVampireDataS2CPacket(this.vampireLevel, this.bloodType, this.thirstLevel, this.thirstExhaustion, this.bloodlust, this.bloodPurity), player);
            ModNetworkDispatcher.sendToClient(new SyncAbilitiesS2CPacket(this.activeAbilities), player);
        }
    }

    private void updateBloodlust(boolean bloodPointGained)
    {
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
    }

    private void handleRegenAndStarvation(ServerPlayer player, boolean isPeaceful)
    {
        //Check if we should do natural regen
        if (player.isHurt() && this.thirstLevel > 0)
        {
            if (this.noRegenTicks > 0)
            {
                --this.noRegenTicks;
            }
            else
            {
                ++this.thirstTickTimer;

                //Standard HP regen when above 1/6th blood
                if (this.thirstLevel > MAX_THIRST / 6 && this.thirstTickTimer >= Config.naturalHealingRate.get())
                {
                    player.heal(VampireUtil.getHealthRegenRate(player));

                    exhaustionIncrement(BloodUsageRates.HEALING);

                    this.thirstTickTimer = 0;
                }
                //Slower HP regen when still have some blood below 1/6th
                else if (this.thirstTickTimer >= Config.naturalHealingRate.get() * 4)
                {
                    player.heal(VampireUtil.getHealthRegenRate(player));

                    exhaustionIncrement(BloodUsageRates.HEALING);

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

    private void handleAbilityExhaustion()
    {
        for (VampireActiveAbility ability : this.activeAbilities)
        {
            this.thirstExhaustionIncrement += BloodUsageRates.getForAbility(ability);
        }
    }

    private void exhaustionIncrementFromVanilla(float vanillaExhaustionDelta)
    {
        this.thirstExhaustionIncrement += (vanillaExhaustionDelta < 1.0F ? 1 : vanillaExhaustionDelta) * this.vampireLevel.getBloodUsageMultiplier();
    }

    private void exhaustionIncrement(BloodUsageRates rate)
    {
        this.thirstExhaustionIncrement += rate.get() * this.vampireLevel.getBloodUsageMultiplier();
    }
}
