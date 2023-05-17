package com.unixkitty.vampire_blood.capability.player;

import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.capability.blood.BloodEntityStorage;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.provider.BloodProvider;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.init.ModRegistry;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.DebugDataSyncS2CPacket;
import com.unixkitty.vampire_blood.network.packet.PlayerRespawnS2CPacket;
import com.unixkitty.vampire_blood.util.SunExposurer;
import com.unixkitty.vampire_blood.util.VampireUtil;
import com.unixkitty.vampire_blood.util.VampirismTier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class VampirePlayerData
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
    private BloodEntityStorage feedingEntityBlood = null;
    private int ticksFeeding;

    public void tick(ServerPlayer player)
    {
        player.level.getProfiler().push("vampire_tick");

        if (blood.vampireLevel != VampirismStage.NOT_VAMPIRE)
        {
            handleSunlight(player);

            handleFeeding(player);

            syncDebugData(player); //TODO remove debug

            blood.tick(player);
        }

        player.level.getProfiler().pop();
    }

    public boolean isFeeding()
    {
        return this.feeding && this.feedingEntity != null;
    }

    public void beginFeeding(@Nonnull LivingEntity target, ServerPlayer player)
    {
        if (blood.vampireLevel != VampirismStage.NOT_VAMPIRE && !feeding && target.isAlive())
        {
            if (target instanceof PathfinderMob)
            {
                target.getCapability(BloodProvider.BLOOD_STORAGE).ifPresent(bloodEntityStorage ->
                {
                    if (blood.noRegenTicks <= 0 && isLookingAtEntity(player, target) && bloodEntityStorage.isEdible() && blood.thirstLevel < VampirePlayerBloodData.MAX_THIRST)
                    {
                        this.feedingEntity = target;
                        this.feedingEntityBlood = bloodEntityStorage;

                        bloodEntityStorage.preventMovement(target);

                        this.feeding = true;

                        ModNetworkDispatcher.notifyPlayerFeeding(player, true);

                        sync();
                    }
                });
            }
            else if (target instanceof Player)
            {
                //TODO player
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

        ModNetworkDispatcher.notifyPlayerFeeding(player, false);

        sync();
    }

    public void addPreventRegenTicks(int amount)
    {
        blood.noRegenTicks = Math.min((blood.noRegenTicks + amount), Config.noRegenTicksLimit.get());
    }

    public VampirismStage getVampireLevel()
    {
        return blood.vampireLevel;
    }

    public void updateLevel(ServerPlayer player, VampirismStage level)
    {
        if (blood.vampireLevel == VampirismStage.IN_TRANSITION && level.getId() > VampirismStage.IN_TRANSITION.getId())
        {
            setBlood(VampirePlayerBloodData.MAX_THIRST / 6);
        }

        blood.vampireLevel = level;

        if (blood.vampireLevel.getId() <= VampirismStage.IN_TRANSITION.getId())
        {
            setBlood(1);

            blood.bloodType = BloodType.HUMAN;
        }

        blood.notifyAndUpdate(player);
    }

    public void setBloodType(ServerPlayer player, BloodType type)
    {
        blood.diet.reset(type);

        blood.updateDiet(type);

        blood.notifyAndUpdate(player);
    }

    public BloodType getBloodType()
    {
        return blood.bloodType;
    }

    public BloodType getBloodTypeIdForFeeding()
    {
        return blood.vampireLevel == VampirismStage.NOT_VAMPIRE ? BloodType.HUMAN : blood.vampireLevel.getId() > VampirismStage.IN_TRANSITION.getId() ? BloodType.VAMPIRE : BloodType.NONE;
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

    public void decreaseBlood(ServerPlayer player, int points)
    {
        blood.decreaseBlood(player, points);
    }

    public void sync()
    {
        blood.sync();
    }

    private void handleSunlight(Player player)
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

            if (catchingUV)
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
                    if (blood.vampireLevel != VampirismStage.IN_TRANSITION)
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

    private float getBloodlustFactor()
    {
        return (1 - (blood.bloodlust / 100));
    }

    private boolean isLookingAtEntity(ServerPlayer player, LivingEntity target)
    {
        Vec3 eyePos2 = player.getEyePosition();

        return target.getBoundingBox().clip(eyePos2, eyePos2.add(player.getLookAngle().scale(1.1D))).isPresent();
    }

    private void handleFeeding(ServerPlayer player)
    {
        if (this.feeding && this.feedingEntity != null && this.feedingEntityBlood != null)
        {
            ++this.ticksFeeding;

            if (!player.isAlive() || !this.feedingEntity.isAlive() || !isLookingAtEntity(player, this.feedingEntity) || this.feedingEntityBlood.getBloodPoints() <= 0 || blood.thirstLevel >= VampirePlayerBloodData.MAX_THIRST)
            {
                stopFeeding(player);
            }
            else if (this.ticksFeeding >= 10)
            {
                if (this.feedingEntityBlood.decreaseBlood(player, this.feedingEntity))
                {
                    this.feedingEntityBlood.preventMovement(this.feedingEntity);

                    if (!this.feedingEntity.isSleeping())
                    {
                        this.feedingEntity.setLastHurtByMob(player);
                    }

                    ModNetworkDispatcher.sendPlayerEntityBlood(player, this.feedingEntityBlood.getBloodType(), this.feedingEntityBlood.getBloodPoints(), this.feedingEntityBlood.getMaxBloodPoints());

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
    }

    public void loadNBTData(CompoundTag tag)
    {
        blood.vampireLevel = VampirismTier.fromId(VampirismStage.class, tag.getInt(LEVEL_NBT_NAME));
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
    }

    public static void copyData(Player oldPlayer, ServerPlayer newPlayer, boolean isDeathEvent)
    {
        newPlayer.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(newVampData ->
        {
            oldPlayer.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(oldVampData ->
            {
                newVampData.blood.vampireLevel = oldVampData.blood.vampireLevel;

                if (isDeathEvent)
                {
                    if (newVampData.blood.vampireLevel == VampirismStage.IN_TRANSITION)
                    {
                        newVampData.blood.vampireLevel = VampirismStage.NOT_VAMPIRE; //Failing transition, player returns to monke
                    }

                    if (newVampData.blood.vampireLevel != VampirismStage.NOT_VAMPIRE)
                    {
                        newVampData.blood.bloodType = BloodType.FRAIL;
                        newVampData.blood.thirstLevel = VampirePlayerBloodData.MAX_THIRST / 2; //Respawn with half thirst
                    }
                }
                else if (newVampData.blood.vampireLevel != VampirismStage.IN_TRANSITION && newVampData.blood.vampireLevel != VampirismStage.NOT_VAMPIRE)
                {
                    newVampData.blood.bloodType = oldVampData.blood.bloodType;

                    newVampData.blood.thirstLevel = oldVampData.blood.thirstLevel;
                    newVampData.blood.thirstExhaustion = oldVampData.blood.thirstExhaustion;
                    newVampData.blood.thirstExhaustionIncrement = oldVampData.blood.thirstExhaustionIncrement;
                }
            });

            ModNetworkDispatcher.sendToClient(new PlayerRespawnS2CPacket(newVampData.blood.vampireLevel.getId(), newVampData.blood.bloodType.getId(), newVampData.blood.thirstLevel), newPlayer);
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
    public VampirismStage setVampireLevel(int level)
    {
        blood.vampireLevel = VampirismTier.fromId(VampirismStage.class, level);

        return blood.vampireLevel;
    }

    @OnlyIn(Dist.CLIENT)
    public BloodType setBloodType(int id)
    {
        blood.bloodType = VampirismTier.fromId(BloodType.class, id);

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
    //TODO remove debug
    //===============================================
    private void syncDebugData(ServerPlayer player)
    {
        ModNetworkDispatcher.sendToClient(new DebugDataSyncS2CPacket(
                this.ticksInSun,
                this.ticksFeeding,
                blood.noRegenTicks,
                blood.thirstExhaustionIncrement,
                blood.thirstTickTimer,
                blood.diet.toIntArray()
        ), player);
    }
    //===============================================
}
