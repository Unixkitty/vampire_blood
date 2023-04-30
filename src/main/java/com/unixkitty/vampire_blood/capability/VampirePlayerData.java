package com.unixkitty.vampire_blood.capability;

import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;
import com.unixkitty.vampire_blood.init.ModRegistry;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.DebugDataSyncS2CPacket;
import com.unixkitty.vampire_blood.network.packet.PlayerBloodDataSyncS2CPacket;
import com.unixkitty.vampire_blood.network.packet.PlayerRespawnS2CPacket;
import com.unixkitty.vampire_blood.network.packet.PlayerVampireDataS2CPacket;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VampirePlayerData
{
    private static final String LEVEL_NBT_NAME = "vampireLevel";
    private static final String SUNTICKS_NBT_NAME = "ticksInSun";
    private static final String BLOODTYPE_NBT_NAME = "bloodType";

    private VampirismStage vampireLevel = VampirismStage.NOT_VAMPIRE;
    private VampireBloodType bloodType = VampireBloodType.HUMAN;
    private int ticksInSun;
    private boolean catchingUV = false;
    private int catchingUVTicks;
    private int noRegenTicks;

    private boolean isFeeding = false; //Don't need to store this in NBT, fine if feeding stops after relogin

    private final Blood blood = new Blood();

    private LivingEntity feedingEntity = null;
    private int ticksFeeding;

    private boolean needsSync = false;

    //TODO stuff
    /*
        If player's vampire stage is anything other than NOT_VAMPIRE
            If player's in transition
                If biting target is a human with non-zero blood points, transition to fledgling
            set isFeeding to true
     */
    public void beginFeeding(LivingEntity target)
    {
        if (this.vampireLevel != VampirismStage.NOT_VAMPIRE)
        {
            if (this.vampireLevel == VampirismStage.IN_TRANSITION)
            {
                //TODO handle transitioning
            }

            //TODO check entity validity and stuff
            this.feedingEntity = target;
            this.isFeeding = true;

            sync();
        }
    }

    public void stopFeeding()
    {
        this.isFeeding = false;

        sync();
    }

    public static void copyData(Player oldPlayer, Player newPlayer, boolean isDeathEvent)
    {
        newPlayer.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(newVampData ->
        {
            oldPlayer.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(oldVampData ->
            {
                newVampData.vampireLevel = oldVampData.getVampireLevel();

                if (isDeathEvent)
                {
                    if (newVampData.vampireLevel == VampirismStage.IN_TRANSITION)
                    {
                        newVampData.vampireLevel = VampirismStage.NOT_VAMPIRE; //Failing transition, player returns to monke
                    }

                    if (newVampData.vampireLevel != VampirismStage.NOT_VAMPIRE)
                    {
                        newVampData.bloodType = VampireBloodType.FRAIL;
                        newVampData.blood.thirstLevel = Blood.MAX_THIRST / 2; //Respawn with half thirst
                    }
                }
                else if (newVampData.vampireLevel != VampirismStage.IN_TRANSITION && newVampData.vampireLevel != VampirismStage.NOT_VAMPIRE)
                {
                    newVampData.bloodType = oldVampData.bloodType;

                    newVampData.blood.thirstLevel = oldVampData.getThirstLevel();
                    newVampData.blood.thirstExhaustion = oldVampData.getThirstExhaustion();
                    newVampData.blood.thirstExhaustionIncrement = oldVampData.getThirstExhaustionIncrement();
                }
            });

            ModNetworkDispatcher.sendToClient(new PlayerRespawnS2CPacket(newVampData.vampireLevel.id, newVampData.bloodType.ordinal(), newVampData.blood.thirstLevel), (ServerPlayer) newPlayer);
        });
    }

    public void saveNBTData(CompoundTag tag)
    {
        tag.putInt(LEVEL_NBT_NAME, this.vampireLevel.id);
        tag.putInt(SUNTICKS_NBT_NAME, this.ticksInSun);
        tag.putInt(BLOODTYPE_NBT_NAME, this.bloodType.ordinal());

        blood.saveNBTData(tag);
    }

    public void loadNBTData(CompoundTag tag)
    {
        this.vampireLevel = VampirismStage.fromId(tag.getInt(LEVEL_NBT_NAME));
        this.ticksInSun = tag.getInt(SUNTICKS_NBT_NAME);
        this.bloodType = VampireBloodType.fromId(tag.getInt(BLOODTYPE_NBT_NAME));

        blood.loadNBTData(tag);
    }

    public void tick(Player player)
    {
        player.level.getProfiler().push("vampire_tick");

        if (this.vampireLevel != VampirismStage.NOT_VAMPIRE)
        {
            handleSunlight(player);

            handleFeeding(player);

            syncDebugData(player); //TODO remove debug

            syncData(player);

            blood.tick(player, this.vampireLevel);
        }

        player.level.getProfiler().pop();
    }

    private void handleSunlight(Player player)
    {
        player.level.getProfiler().push("vampire_catching_sun_logic");

        if (player.level.isDay())
        {
            //Cache the check for performance
            if (this.catchingUVTicks <= 0)
            {
                this.catchingUV = catchingUV(player);
                this.catchingUVTicks = 20;
            }
            else
            {
                --this.catchingUVTicks;
            }

            if (catchingUV)
            {
                ++this.ticksInSun;

                //We do basic effects sooner and more often
                if (this.ticksInSun >= Config.ticksToSunDamage.get() / 6)
                {
                    //Do common effects
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, Config.ticksToSunDamage.get() * 10, player.level.isRaining() ? 0 : 1, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, Config.ticksToSunDamage.get() * 10, player.level.isRaining() ? 0 : 1, false, false, true));
                }

                if (this.ticksInSun >= Config.ticksToSunDamage.get() / 2 && this.vampireLevel.id > VampirismStage.IN_TRANSITION.id)
                {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, Config.ticksToSunDamage.get() * 4, 0, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, Config.ticksToSunDamage.get() * 3, 0, false, false, true));
                }

                if (this.ticksInSun >= Config.ticksToSunDamage.get())
                {
                    if (this.vampireLevel != VampirismStage.IN_TRANSITION)
                    {
                        player.hurt(ModRegistry.SUN_DAMAGE, ((player.getMaxHealth() / 3) / 1.5f) / (player.level.isRaining() ? 2 : 1));
                        player.setRemainingFireTicks((int) (Config.ticksToSunDamage.get() * 1.2));

                        addPreventRegenTicks(Config.ticksToSunDamage.get());
                    }

                    this.ticksInSun = 0;
                }
            }
            else
            {
                this.ticksInSun = 0;
            }
        }
        else
        {
            this.ticksInSun = 0;
        }

        player.level.getProfiler().pop();
    }

    private boolean catchingUV(Player player)
    {
        final BlockPos playerEyePos = new BlockPos(player.getX(), player.getEyeY(), player.getZ());

        //If fully submerged, including eyes, check if deep enough (4 blocks) or if the block above the liquid can't see the sky
        if (player.isUnderWater())
        {
            BlockPos abovePos;
            BlockPos blockPosAboveLiquid = null;

            for (int i = 0; i <= 4; i++)
            {
                abovePos = playerEyePos.above(i);

                if (!player.level.getBlockState(abovePos).getMaterial().isLiquid())
                {
                    blockPosAboveLiquid = abovePos;
                    break;
                }
            }

            //If shallow enough liquid and found nonwater above, can it see sky
            return blockPosAboveLiquid != null && player.level.canSeeSky(blockPosAboveLiquid);
        }
        else //If Player not in water and can see sky
        {
            return player.level.canSeeSky(playerEyePos);
        }
    }

    private void handleFeeding(Player player)
    {
        if (this.isFeeding)
        {
            ++this.ticksFeeding;

            if (this.ticksFeeding >= 10)
            {
                //TODO actually drain some entity
                addBlood(1);

                this.ticksFeeding = 0;

                if (Config.debugOutput.get())
                {
                    player.sendSystemMessage(Component.literal("Feeding, + 1 blood point, current blood: " + this.getThirstLevel() + "/" + Blood.MAX_THIRST));
                }
            }
        }
    }

    public void addPreventRegenTicks(int amount)
    {
        this.noRegenTicks = Math.min((this.noRegenTicks + amount), Config.noRegenTicksLimit.get());
    }

    public VampirismStage getVampireLevel()
    {
        return this.vampireLevel;
    }

    public void setVampireLevel(ServerPlayer player, int level)
    {
        this.vampireLevel = VampirismStage.fromId(level);

        notifyAndUpdate(player);
    }

    public void setBloodType(ServerPlayer player, int id)
    {
        this.bloodType = VampireBloodType.fromId(id);

        notifyAndUpdate(player);
    }

    @OnlyIn(Dist.CLIENT)
    public void setVampireLevel(int level)
    {
        this.vampireLevel = VampirismStage.fromId(level);
    }

    @OnlyIn(Dist.CLIENT)
    public void setBloodType(int id)
    {
        this.bloodType = VampireBloodType.fromId(id);
    }

    private void notifyAndUpdate(ServerPlayer player)
    {
        checkOriginal(player);
        VampireAttributeModifiers.updateAttributes(player, this.vampireLevel, this.bloodType);
        ModNetworkDispatcher.sendToClient(new PlayerVampireDataS2CPacket(this.vampireLevel.getId(), this.bloodType.ordinal(), this.isFeeding), player);
    }

    private void checkOriginal(ServerPlayer player)
    {
//        if (this.vampireLevel == VampirismStage.ORIGINAL && !player.getStringUUID().equals("9d64fee0-582d-4775-b6ef-37d6e6d3f429"))
//        {
//            this.vampireLevel = VampirismStage.MATURE;
//        }
    }

    public int getSunTicks()
    {
        return this.ticksInSun;
    }

    public VampireBloodType getBloodType()
    {
        return this.bloodType;
    }

    public void setFeeding(boolean feeding)
    {
        this.isFeeding = feeding;
    }

    public boolean isFeeding()
    {
        return this.isFeeding;
    }

    public int getThirstLevel()
    {
        return blood.thirstLevel;
    }

    public int getThirstExhaustion()
    {
        return blood.thirstExhaustion;
    }

    public void setBlood(int points)
    {
        blood.thirstLevel = Math.max(Blood.MIN_THIRST, Math.min(points, Blood.MAX_THIRST));
    }

    @OnlyIn(Dist.CLIENT)
    public int setClientBlood(int points)
    {
        setBlood(points);

        return blood.thirstLevel;
    }

    public void addBlood(int points)
    {
        blood.thirstLevel = Math.min(blood.thirstLevel + points, Blood.MAX_THIRST);

        blood.sync();
    }

    public void decreaseBlood(int points)
    {
        blood.thirstLevel = Math.max(blood.thirstLevel - points, Blood.MIN_THIRST);

        blood.sync();
    }

    public void sync()
    {
        this.needsSync = true;
    }

    public void syncBlood()
    {
        blood.sync();
    }

    private void syncData(Player player)
    {
        if (this.needsSync)
        {
            this.needsSync = false;

            checkOriginal((ServerPlayer) player);

            ModNetworkDispatcher.sendToClient(new PlayerVampireDataS2CPacket(this.vampireLevel.getId(), this.bloodType.ordinal(), this.isFeeding), (ServerPlayer) player);
        }
    }

    //TODO remove debug
    //===============================================
    private void syncDebugData(Player player)
    {
        ModNetworkDispatcher.sendToClient(new DebugDataSyncS2CPacket(this), (ServerPlayer) player);
    }

    public int getNoRegenTicks()
    {
        return this.noRegenTicks;
    }

    public int getThirstExhaustionIncrement()
    {
        return blood.thirstExhaustionIncrement;
    }

    public int getThirstTickTimer()
    {
        return blood.thirstTickTimer;
    }

    public int getFeedingTicks()
    {
        return this.ticksFeeding;
    }
    //===============================================

    public class Blood
    {
        public static final int MIN_THIRST = 0;
        public static final int MAX_THIRST = 40;

        private static final String THIRST_NBT_NAME = "thirstLevel";
        private static final String THIRST_EXHAUSTION_NBT_NAME = "thirstExhaustion";
        private static final String THIRST_TIMER_NBT_NAME = "thirstTickTimer";

        private int thirstLevel = 1;
        private int thirstExhaustion; //This will be similar to vanilla's in FoodData.java, decrease thirst level if this gets up to some limit, and change this back to 0
        private int thirstExhaustionIncrement; //This should help make thirstExhaustion more dynamic based on configs and player actions
        private int thirstTickTimer; //This will be used for healing and starvation

        private boolean needsSync = false;

        private Blood()
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

        private void sync()
        {
            this.needsSync = true;
        }

        private void tick(Player player, VampirismStage vampireLevel)
        {
            player.level.getProfiler().push("vampire_blood_tick");

            boolean isPeaceful = player.level.getDifficulty() == Difficulty.PEACEFUL;

            float vanillaExhaustionDelta = player.getFoodData().getExhaustionLevel() * Config.bloodUsageRate.get();

            //Keep vanilla food level in the middle
            player.getFoodData().setFoodLevel(10);
            player.getFoodData().setSaturation(0);
            player.getFoodData().setExhaustion(0);

            //TODO special handling when Stage == IN_TRANSITION
            if (vampireLevel == VampirismStage.IN_TRANSITION)
            {

            }
            else
            {
                if (this.thirstExhaustion >= 100)
                {
                    this.thirstExhaustion -= 100;

                    if (!isPeaceful)
                    {
                        decreaseBlood(1);

                        if (Config.debugOutput.get())
                        {
                            player.sendSystemMessage(Component.literal("Using, - 1 blood point, current blood: " + this.thirstLevel + "/" + MAX_THIRST));
                        }
                    }
                }

                handleExhaustion(player, vanillaExhaustionDelta, isPeaceful);
            }

            this.syncData(player);

            player.level.getProfiler().pop();
        }

        private void handleExhaustion(Player player, float vanillaExhaustionDelta, boolean isPeaceful)
        {
            if (vanillaExhaustionDelta > 0)
            {
                exhaustionIncrementFromVanilla(vanillaExhaustionDelta);
            }

            exhaustionIncrement(BloodRates.IDLE);

            handleRegenAndStarvation(player, isPeaceful);

            if (this.thirstExhaustionIncrement >= Config.bloodUsageRate.get())
            {
                this.thirstExhaustionIncrement -= Config.bloodUsageRate.get();
                this.thirstExhaustion++;
            }
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
                    //Standard HP regen when above 1/6th blood
                    if (this.thirstLevel > MAX_THIRST / 6)
                    {
                        ++this.thirstTickTimer;

                        if (this.thirstTickTimer >= Config.naturalHealingRate.get())
                        {
                            player.heal(VampireUtil.getHealthRegenRate(player));

                            exhaustionIncrement(BloodRates.HEALING, Config.naturalHealingRate.get());

                            this.thirstTickTimer = 0;
                        }
                    }
                    //Slower HP regen when still have some blood below 1/6th
                    else
                    {
                        ++this.thirstTickTimer;

                        if (this.thirstTickTimer >= 80)
                        {
                            player.heal(VampireUtil.getHealthRegenRate(player));

                            exhaustionIncrement(BloodRates.HEALING_SLOW, Config.naturalHealingRate.get());

                            this.thirstTickTimer = 0;
                        }
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

        private void exhaustionIncrement(BloodRates rate)
        {
            this.thirstExhaustionIncrement += rate.get();
        }

        private void exhaustionIncrement(BloodRates rate, int ticks)
        {
            this.thirstExhaustionIncrement += rate.get() * ticks;
        }

        protected void saveNBTData(CompoundTag tag)
        {
            tag.putInt(THIRST_NBT_NAME, this.thirstLevel);
            tag.putInt(THIRST_EXHAUSTION_NBT_NAME, this.thirstExhaustion);
            tag.putInt(THIRST_TIMER_NBT_NAME, this.thirstTickTimer);
        }

        protected void loadNBTData(CompoundTag tag)
        {
            this.thirstLevel = tag.getInt(THIRST_NBT_NAME);
            this.thirstExhaustion = tag.getInt(THIRST_EXHAUSTION_NBT_NAME);
            this.thirstTickTimer = tag.getInt(THIRST_TIMER_NBT_NAME);
        }
    }
}
