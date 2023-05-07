package com.unixkitty.vampire_blood.capability.player;

import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.PlayerBloodDataSyncS2CPacket;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

public class VampirePlayerBloodData
{
    public static final int MIN_THIRST = 0;
    public static final int MAX_THIRST = 40;

    private static final String THIRST_NBT_NAME = "thirstLevel";
    private static final String THIRST_EXHAUSTION_NBT_NAME = "thirstExhaustion";
    private static final String THIRST_TIMER_NBT_NAME = "thirstTickTimer";
    private static final String THIRST_EXHAUSTION_INCREMENT_NBT_NAME = "thirstExhaustionIncrement";
    private static final String NO_REGEN_TICKS = "noRegenTicks";

    int thirstLevel = 1;
    int thirstExhaustion; //This is somewhat similar to vanilla's in FoodData.java, decrease thirst level if this gets up to some limit, and change this back to 0
    int thirstExhaustionIncrement; //This helps make thirstExhaustion more dynamic based on configs and player actions
    int thirstTickTimer; //This is used for healing and starvation
    int noRegenTicks;

    private boolean needsSync = false;

    VampirePlayerBloodData()
    {
    }

    private void syncData(Player player)
    {
        if (this.needsSync)
        {
            this.needsSync = false;

            ModNetworkDispatcher.sendToClient(new PlayerBloodDataSyncS2CPacket(this.thirstLevel), (ServerPlayer) player);
        }
    }

    void sync()
    {
        this.needsSync = true;
    }

    void addBlood(int points)
    {
        this.thirstLevel = Math.min(this.thirstLevel + points, VampirePlayerBloodData.MAX_THIRST);

        sync();
    }

    void decreaseBlood(int points)
    {
        this.thirstLevel = Math.max(this.thirstLevel - points, VampirePlayerBloodData.MIN_THIRST);

        sync();
    }

    void tick(Player player)
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
                decreaseBlood(1);
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
        }

        this.syncData(player);

        player.level.getProfiler().pop();
    }

    private void handleRegenAndStarvation(Player player, boolean isPeaceful)
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
                addBlood(1);

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

    protected void saveNBTData(CompoundTag tag)
    {
        tag.putInt(THIRST_NBT_NAME, this.thirstLevel);
        tag.putInt(THIRST_EXHAUSTION_NBT_NAME, this.thirstExhaustion);
        tag.putInt(THIRST_TIMER_NBT_NAME, this.thirstTickTimer);
        tag.putInt(THIRST_EXHAUSTION_INCREMENT_NBT_NAME, this.thirstExhaustionIncrement);
        tag.putInt(NO_REGEN_TICKS, this.noRegenTicks);
    }

    protected void loadNBTData(CompoundTag tag)
    {
        this.thirstLevel = tag.getInt(THIRST_NBT_NAME);
        this.thirstExhaustion = tag.getInt(THIRST_EXHAUSTION_NBT_NAME);
        this.thirstTickTimer = tag.getInt(THIRST_TIMER_NBT_NAME);
        this.thirstExhaustionIncrement = tag.getInt(THIRST_EXHAUSTION_INCREMENT_NBT_NAME);
        this.noRegenTicks = tag.getInt(NO_REGEN_TICKS);
    }
}
