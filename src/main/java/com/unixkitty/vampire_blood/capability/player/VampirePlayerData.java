package com.unixkitty.vampire_blood.capability.player;

import com.unixkitty.vampire_blood.capability.blood.AbstractBloodVessel;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.blood.IBloodVessel;
import com.unixkitty.vampire_blood.capability.provider.BloodProvider;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.init.ModDamageSources;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.DebugDataSyncS2CPacket;
import com.unixkitty.vampire_blood.util.SunExposurer;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class VampirePlayerData extends AbstractBloodVessel
{
    private static final String LEVEL_NBT_NAME = "vampireLevel";
    private static final String SUNTICKS_NBT_NAME = "ticksInSun";
    private static final String BLOOD_PURITY_NBT_NAME = "bloodPurity";
    private static final String THIRST_NBT_NAME = "thirstLevel";
    private static final String THIRST_EXHAUSTION_NBT_NAME = "thirstExhaustion";
    private static final String THIRST_TIMER_NBT_NAME = "thirstTickTimer";
    private static final String THIRST_EXHAUSTION_INCREMENT_NBT_NAME = "thirstExhaustionIncrement";
    private static final String NO_REGEN_TICKS = "noRegenTicks";
    private static final String BLOODLUST_NBT_NAME = "bloodlust";

    private int ticksInSun;
    private boolean catchingUV = false;
    private int catchingUVTicks;

    private boolean feeding = false;

    private final VampirePlayerBloodData blood = new VampirePlayerBloodData();

    private LivingEntity feedingEntity = null;
    private IBloodVessel feedingEntityBlood = null;
    private int ticksFeeding;
    private int totalTicksFeeding = 0;

    //This is for being fed on
    private int maxBloodPoints = 0;
    private int bloodPoints = 0;

    public void tick(ServerPlayer player)
    {
        player.level.getProfiler().push("vampire_player_tick");

        if (blood.vampireLevel != VampirismLevel.NOT_VAMPIRE)
        {
            handleSunlight(player);

            handleFeeding(player);

            blood.tick(player);

            handleAbilities(player);

            syncDebugData(player);
        }

        handleBeingCharmedTicks(player);

        if (blood.vampireLevel != VampirismLevel.IN_TRANSITION)
        {
            updateBloodForFeeding(player, getBloodType());
        }

        player.level.getProfiler().pop();
    }

    public void charmTarget(@Nonnull LivingEntity target, ServerPlayer player)
    {
        if (blood.vampireLevel.getId() > VampirismLevel.IN_TRANSITION.getId() && target.isAlive() && blood.thirstLevel > Config.abilityHungerThreshold.get() && VampireUtil.isLookingAtEntity(player, target))
        {
            if (target instanceof Player targetPlayer && !targetPlayer.isCreative() && !targetPlayer.isSpectator())
            {
                target.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData -> vampirePlayerData.tryGetCharmed(player, blood.vampireLevel));
            }
            else
            {
                target.getCapability(BloodProvider.BLOOD_STORAGE).ifPresent(bloodEntityStorage -> bloodEntityStorage.tryGetCharmed(player, blood.vampireLevel));
            }
        }
    }

    public void toggleAbility(ServerPlayer player, VampireActiveAbility ability)
    {
        if (ability != null && blood.vampireLevel.getId() > VampirismLevel.IN_TRANSITION.getId())
        {
            if (blood.activeAbilities.contains(ability))
            {
                blood.activeAbilities.remove(ability);

                blood.updateWithAttributes(player, true);
            }
            else
            {
                if (!this.catchingUV && (ability == VampireActiveAbility.BLOOD_VISION || ability == VampireActiveAbility.NIGHT_VISION) || blood.thirstLevel > Config.abilityHungerThreshold.get())
                {
                    blood.activeAbilities.add(ability);

                    blood.updateWithAttributes(player, true);
                }
            }
        }
    }

    public boolean isFeeding()
    {
        return this.feeding && this.feedingEntity != null;
    }

    public void beginFeeding(@Nonnull LivingEntity target, ServerPlayer player)
    {
        if (blood.vampireLevel != VampirismLevel.NOT_VAMPIRE && !feeding && target.isAlive())
        {
            if (blood.thirstLevel < VampirePlayerBloodData.MAX_THIRST && VampireUtil.isLookingAtEntity(player, target))
            {
                if (target instanceof Player targetPlayer && !targetPlayer.isSpectator() && !targetPlayer.isCreative())
                {
                    target.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
                    {
                        if (vampirePlayerData.isEdible())
                        {
                            this.feedingEntityBlood = vampirePlayerData;
                        }
                    });
                }
                else
                {
                    target.getCapability(BloodProvider.BLOOD_STORAGE).ifPresent(bloodEntityStorage ->
                    {
                        if (bloodEntityStorage.isEdible())
                        {
                            this.feedingEntityBlood = bloodEntityStorage;
                        }
                    });
                }
            }

            if (this.feedingEntityBlood != null)
            {
                VampireUtil.preventMovement(this.feedingEntity = target);

                ModNetworkDispatcher.notifyPlayerFeeding(player, this.feeding = true);

                sync();
            }
        }
    }

    public void tryStopFeeding(ServerPlayer player, float damage)
    {
        //Factors from 0 to 1F are part of the whole stop chance
        float healthFactor = Math.max(player.getHealth() / player.getMaxHealth(), 1.0F); //Factor from 0 to 1, 1 being maximum health and thus highest chance to break off feeding
        float damageFactor = Math.max(damage / player.getMaxHealth(), 1.0F); //Damage factor from 0 to 1, 1 being damage equal to player's health

        if (player.getRandom().nextFloat() <= (healthFactor * 0.3f) + (damageFactor * 0.4f) + (getBloodlustFactor() * 0.3f))
        {
            stopFeeding(player);
        }
    }

    public void tryStopFeeding(ServerPlayer player)
    {
        //When player is attempting to stop feeding of their own will, only factor in bloodlust
        if (player.getRandom().nextFloat() < getBloodlustFactor())
        {
            stopFeeding(player);
        }
    }

    public void stopFeeding(ServerPlayer player)
    {
        this.feeding = false;
        this.feedingEntity = null;
        this.feedingEntityBlood = null;
        this.totalTicksFeeding = 0;

        ModNetworkDispatcher.notifyPlayerFeeding(player, false);

        sync();
    }

    public int getNoRegenTicks()
    {
        return blood.noRegenTicks;
    }

    public void addPreventRegenTicks(ServerPlayer player, int amount)
    {
        if (this.feeding)
        {
            stopFeeding(player);
        }

        blood.addPreventRegenTicks(amount);
    }

    public VampirismLevel getVampireLevel()
    {
        return blood.vampireLevel;
    }

    public void updateLevel(ServerPlayer player, VampirismLevel level, boolean force)
    {
        if (blood.vampireLevel.getId() <= VampirismLevel.IN_TRANSITION.getId() && level.getId() > VampirismLevel.IN_TRANSITION.getId())
        {
            setBlood(VampirePlayerBloodData.MAX_THIRST / 6);

            player.setHealth(player.getMaxHealth());
        }

        blood.vampireLevel = level;

        if (blood.vampireLevel.getId() <= VampirismLevel.IN_TRANSITION.getId())
        {
            setBlood(1);

            blood.bloodType = BloodType.HUMAN;
        }

        blood.updateWithAttributes(player, force);
    }

    public void setBloodType(ServerPlayer player, BloodType type, boolean force)
    {
        blood.diet.reset(type);

        blood.updateDiet(type);

        blood.updateWithAttributes(player, force);
    }

    public BloodType getDietBloodType()
    {
        return blood.bloodType;
    }

    public int getThirstLevel()
    {
        return blood.thirstLevel;
    }

    public float getBloodlust()
    {
        return blood.bloodlust;
    }

    public void setBlood(int value)
    {
        blood.thirstLevel = VampireUtil.clampInt(value, VampirePlayerBloodData.MAX_THIRST);
    }

    public void setBloodlust(float value)
    {
        blood.bloodlust = VampireUtil.clampFloat(value, 100F);
    }

    public void addBlood(ServerPlayer player, int points, BloodType bloodType)
    {
        blood.addBlood(player, points, bloodType);
    }

    public void sync()
    {
        blood.sync();
    }

    private void updateBloodForFeeding(ServerPlayer player, BloodType bloodType)
    {
        if (blood.vampireLevel == VampirismLevel.NOT_VAMPIRE)
        {
            this.maxBloodPoints = VampireUtil.healthToBlood(player.getMaxHealth(), bloodType);
            this.bloodPoints = VampireUtil.healthToBlood(player.getHealth(), bloodType);
        }
        else
        {
            this.maxBloodPoints = VampirePlayerBloodData.MAX_THIRST;
            this.bloodPoints = blood.thirstLevel;
        }
    }

    private void handleAbilities(ServerPlayer player)
    {
        boolean veryHungry = blood.thirstLevel <= Config.abilityHungerThreshold.get();

        //Turn off abilities when exposed to sunlight or at low hunger
        if (blood.vampireLevel.getId() > VampirismLevel.IN_TRANSITION.getId() && player.tickCount % 20 == 0 && !blood.activeAbilities.isEmpty() && (this.catchingUV || veryHungry))
        {
            for (VampireActiveAbility ability : blood.activeAbilities)
            {
                if (veryHungry && (ability == VampireActiveAbility.BLOOD_VISION || ability == VampireActiveAbility.NIGHT_VISION))
                {
                    continue;
                }

                toggleAbility(player, ability);
            }
        }
    }

    private void handleSunlight(ServerPlayer player)
    {
        player.level.getProfiler().push("vampire_catching_sun_logic");

        if (player.level.skyDarken < 4 && Config.sunnyDimensions.get().contains(player.level.dimension().location().toString()))
        {
            //Cache the check for performance
            if (this.catchingUVTicks <= 0)
            {
                this.catchingUV = SunExposurer.isCatchingUV(player);
                this.catchingUVTicks = 20;
            }
            else
            {
                --this.catchingUVTicks;
            }

            if (this.catchingUV)
            {
                ++this.ticksInSun;

                //We do basic effects sooner
                if (this.ticksInSun == Config.ticksToSunDamage.get() / 6)
                {
                    //Do common effects
                    SunExposurer.chanceEffect(player, MobEffects.WEAKNESS, 10, player.level.isRaining() ? 0 : 1, 100);
                    SunExposurer.chanceEffect(player, MobEffects.DIG_SLOWDOWN, 10, player.level.isRaining() ? 0 : 1, 100);
                }

                if (this.ticksInSun == Config.ticksToSunDamage.get() / 2)
                {
                    SunExposurer.chanceEffect(player, MobEffects.CONFUSION, 4, 0, blood.vampireLevel);
                    SunExposurer.chanceEffect(player, MobEffects.BLINDNESS, 3, 0, blood.vampireLevel);
                }

                if (this.ticksInSun >= Config.ticksToSunDamage.get())
                {
                    if (blood.vampireLevel != VampirismLevel.IN_TRANSITION)
                    {
                        player.hurt(ModDamageSources.SUN_DAMAGE, ((player.getMaxHealth() / 3) / 1.5f) / (player.level.isRaining() ? 2 : 1));
                        player.setRemainingFireTicks((int) (Config.ticksToSunDamage.get() * 1.2));

                        addPreventRegenTicks(player, Config.ticksToSunDamage.get());
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

    private float getBloodlustFactor()
    {
        return (1 - (blood.bloodlust / 100));
    }

    private void handleFeeding(ServerPlayer player)
    {
        if (this.feeding && this.feedingEntity != null && this.feedingEntityBlood != null)
        {
            ++this.ticksFeeding;
            ++this.totalTicksFeeding;

            if (!player.isAlive() || !this.feedingEntity.isAlive() || !VampireUtil.isLookingAtEntity(player, this.feedingEntity) || this.feedingEntityBlood.getBloodPoints() <= 0 || blood.thirstLevel >= VampirePlayerBloodData.MAX_THIRST || this.totalTicksFeeding >= 550)
            {
                stopFeeding(player);
            }
            else if (this.ticksFeeding >= 10)
            {
                if (this.feedingEntityBlood.decreaseBlood(player, this.feedingEntity))
                {
                    VampireUtil.preventMovement(this.feedingEntity);

                    if (!this.feedingEntity.isSleeping() && !this.feedingEntityBlood.isCharmedBy(player) && this.feedingEntity.getLastHurtByMob() != player)
                    {
                        this.feedingEntity.setLastHurtByMob(player);
                    }

                    ModNetworkDispatcher.sendPlayerEntityBlood(player, this.feedingEntity.getId(), this.feedingEntityBlood.getBloodType(), this.feedingEntityBlood.getBloodPoints(), this.feedingEntityBlood.getMaxBloodPoints(), true);

                    addBlood(player, 1, this.feedingEntityBlood.getBloodType());
                }
                else
                {
                    stopFeeding(player);
                }

                this.ticksFeeding = 0;
            }
        }
    }

    @Override
    public boolean isEdible()
    {
        return getBloodType() != BloodType.NONE && blood.vampireLevel != VampirismLevel.IN_TRANSITION;
    }

    @Override
    public int getBloodPoints()
    {
        return this.bloodPoints;
    }

    @Override
    public int getMaxBloodPoints()
    {
        return this.maxBloodPoints;
    }

    @Override
    public boolean decreaseBlood(@NotNull LivingEntity attacker, @NotNull LivingEntity victim)
    {
        if (isEdible())
        {
            //Non-vampire player
            if (blood.vampireLevel == VampirismLevel.NOT_VAMPIRE)
            {
                drinkFromHealth(attacker, victim, getBloodType());

                return true;
            }
            else //Vampire, because isEdible() checks for transitioning stage
            {
                int resultingThirstLevel = blood.thirstLevel - 1;

                if (resultingThirstLevel >= 0)
                {
                    blood.decreaseBlood(false);

                    return true;
                }
                else
                {
                    return false;
                }
            }
        }

        return false;
    }

    @Override
    public BloodType getBloodType()
    {
        return blood.vampireLevel == VampirismLevel.NOT_VAMPIRE ? BloodType.HUMAN : blood.vampireLevel.getId() > VampirismLevel.IN_TRANSITION.getId() ? BloodType.VAMPIRE : BloodType.NONE;
    }

    @Override
    public void saveNBTData(CompoundTag tag)
    {
        tag.putInt(LEVEL_NBT_NAME, blood.vampireLevel.getId());
        tag.putInt(SUNTICKS_NBT_NAME, this.ticksInSun);
        tag.putInt(BloodType.BLOODTYPE_NBT_NAME, blood.bloodType.getId());
        tag.putFloat(BLOOD_PURITY_NBT_NAME, blood.bloodPurity);
        tag.putInt(THIRST_NBT_NAME, blood.thirstLevel);
        tag.putInt(THIRST_EXHAUSTION_NBT_NAME, blood.thirstExhaustion);
        tag.putInt(THIRST_TIMER_NBT_NAME, blood.thirstTickTimer);
        tag.putInt(THIRST_EXHAUSTION_INCREMENT_NBT_NAME, blood.thirstExhaustionIncrement);
        tag.putInt(NO_REGEN_TICKS, blood.noRegenTicks);
        tag.putFloat(BLOODLUST_NBT_NAME, blood.bloodlust);

        blood.diet.saveNBT(tag);

        VampireActiveAbility.saveNBT(blood.activeAbilities, tag);

        super.saveNBTData(tag);
    }

    @Override
    public void loadNBTData(CompoundTag tag)
    {
        blood.vampireLevel = VampirismTier.fromId(VampirismLevel.class, tag.getInt(LEVEL_NBT_NAME));
        this.ticksInSun = tag.getInt(SUNTICKS_NBT_NAME);
        blood.bloodType = VampirismTier.fromId(BloodType.class, tag.getInt(BloodType.BLOODTYPE_NBT_NAME));
        blood.bloodPurity = tag.getFloat(BLOOD_PURITY_NBT_NAME);
        blood.thirstLevel = tag.getInt(THIRST_NBT_NAME);
        blood.thirstExhaustion = tag.getInt(THIRST_EXHAUSTION_NBT_NAME);
        blood.thirstTickTimer = tag.getInt(THIRST_TIMER_NBT_NAME);
        blood.thirstExhaustionIncrement = tag.getInt(THIRST_EXHAUSTION_INCREMENT_NBT_NAME);
        blood.noRegenTicks = tag.getInt(NO_REGEN_TICKS);
        blood.bloodlust = tag.getFloat(BLOODLUST_NBT_NAME);

        blood.diet.loadNBT(tag);

        VampireActiveAbility.loadNBT(blood.activeAbilities, tag);

        super.loadNBTData(tag);
    }

    @Override
    protected void handleCharmedTicks(LivingEntity player)
    {
        if (blood.vampireLevel == VampirismLevel.IN_TRANSITION || blood.vampireLevel == VampirismLevel.ORIGINAL)
        {
            this.charmedByMap.clear();
        }
        else
        {
            super.handleCharmedTicks(player);
        }
    }

    public static void copyData(Player oldPlayer, ServerPlayer player, boolean isDeathEvent)
    {
        player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(newVampData ->
        {
            oldPlayer.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(oldVampData ->
            {
                newVampData.blood.vampireLevel = oldVampData.blood.vampireLevel;

                if (isDeathEvent)
                {
                    if (newVampData.blood.vampireLevel == VampirismLevel.IN_TRANSITION)
                    {
                        newVampData.blood.vampireLevel = VampirismLevel.NOT_VAMPIRE; //Failing transition, player returns to monke
                    }

                    if (newVampData.blood.vampireLevel != VampirismLevel.NOT_VAMPIRE)
                    {
                        newVampData.blood.bloodType = BloodType.FRAIL;
                        newVampData.blood.diet.reset(newVampData.blood.bloodType);
                        newVampData.blood.bloodlust = 50F;
                        newVampData.blood.thirstLevel = VampirePlayerBloodData.MAX_THIRST / 4; //Don't respawn with full thirst
                    }
                }
                else if (newVampData.blood.vampireLevel != VampirismLevel.IN_TRANSITION && newVampData.blood.vampireLevel != VampirismLevel.NOT_VAMPIRE)
                {
                    newVampData.blood.bloodType = oldVampData.blood.bloodType;
                    newVampData.blood.bloodPurity = oldVampData.blood.bloodPurity;

                    newVampData.blood.thirstLevel = oldVampData.blood.thirstLevel;
                    newVampData.blood.thirstExhaustion = oldVampData.blood.thirstExhaustion;
                    newVampData.blood.thirstExhaustionIncrement = oldVampData.blood.thirstExhaustionIncrement;
                    newVampData.blood.thirstTickTimer = oldVampData.blood.thirstTickTimer;
                    newVampData.blood.noRegenTicks = oldVampData.blood.noRegenTicks;
                    newVampData.blood.bloodlust = oldVampData.blood.bloodlust;
                }
            });

//            float lastHealthFactor = player.getHealth() / player.getMaxHealth();

            newVampData.blood.updateWithAttributes(player, true);

//            float healthFactor = player.getHealth() / player.getMaxHealth();
//
//            //If player was of a higher level they may end up lacking HP out of max after updating attributes
//            if (healthFactor < lastHealthFactor)
//            {
//                player.setHealth(Math.max(player.getMaxHealth() * healthFactor, player.getMaxHealth()));
//            }
        });
    }

    //===============================================
    // Client methods
    //===============================================
    @OnlyIn(Dist.CLIENT)
    public boolean setFeeding(boolean feeding)
    {
        this.feeding = feeding;

        return feeding;
    }

    @OnlyIn(Dist.CLIENT)
    public VampirismLevel setClientVampireLevel(VampirismLevel vampireLevel)
    {
        blood.vampireLevel = vampireLevel;

        return blood.vampireLevel;
    }

    @OnlyIn(Dist.CLIENT)
    public BloodType setClientBloodType(BloodType bloodType)
    {
        blood.bloodType = bloodType;

        return blood.bloodType;
    }

    @OnlyIn(Dist.CLIENT)
    public int setClientBlood(int value)
    {
        setBlood(value);

        return blood.thirstLevel;
    }

    @OnlyIn(Dist.CLIENT)
    public int setClientExhaustion(int value)
    {
        blood.thirstExhaustion = VampireUtil.clampInt(value, 100);

        return blood.thirstExhaustion;
    }

    @OnlyIn(Dist.CLIENT)
    public float setClientBloodlust(float value)
    {
        setBloodlust(value);

        return blood.bloodlust;
    }
    //===============================================

    //===============================================
    //Debug
    //===============================================
    private void syncDebugData(ServerPlayer player)
    {
        if (Config.debug.get())
        {
            ModNetworkDispatcher.sendToClient(new DebugDataSyncS2CPacket(
                    this.ticksInSun,
                    blood.noRegenTicks,
                    blood.thirstExhaustionIncrement,
                    blood.thirstTickTimer,
                    blood.diet.toIntArray()
            ), player);
        }
    }
    //===============================================
}
