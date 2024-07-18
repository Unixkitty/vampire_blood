package com.unixkitty.vampire_blood.capability.player;

import com.unixkitty.vampire_blood.advancement.trigger.VampireAbilityUseTrigger;
import com.unixkitty.vampire_blood.advancement.trigger.VampireLevelChangeTrigger;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.blood.BloodVessel;
import com.unixkitty.vampire_blood.capability.blood.IBloodVessel;
import com.unixkitty.vampire_blood.capability.provider.BloodProvider;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.config.ArmourUVCoverageManager;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.init.ModDamageTypes;
import com.unixkitty.vampire_blood.init.ModRegistry;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.DebugDataSyncS2CPacket;
import com.unixkitty.vampire_blood.util.SunExposurer;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class VampirePlayerData extends BloodVessel
{
    private static final String LEVEL_NBT_NAME = "vampireLevel";
    private static final String SUNTICKS_NBT_NAME = "ticksInSun";
    public static final String BLOOD_PURITY_NBT_NAME = "bloodPurity";
    private static final String THIRST_NBT_NAME = "thirstLevel";
    private static final String THIRST_EXHAUSTION_NBT_NAME = "thirstExhaustion";
    private static final String THIRST_TIMER_NBT_NAME = "thirstTickTimer";
    private static final String THIRST_EXHAUSTION_INCREMENT_NBT_NAME = "thirstExhaustionIncrement";
    private static final String NO_REGEN_TICKS = "noRegenTicks";
    private static final String BLOODLUST_NBT_NAME = "bloodlust";
    private static final String TRANSITION_START_TIME_NBT_NAME = "transitionStartTime";
    private static final String AGE_NBT_NAME = "age";

    private boolean shouldTransition = false;

    private int ticksInSun;
    private boolean catchingUV = false;
    private int catchingUVTicks;
    private float armourUVCoverage = 0F;

    private boolean feeding = false;

    private final VampirePlayerBloodData blood = new VampirePlayerBloodData();

    private LivingEntity feedingEntity = null;
    private IBloodVessel feedingEntityBlood = null;
    private int ticksFeeding;
    private int totalTicksFeeding = 0;

    private long age;

    //This is for being fed on
    private int maxBloodPoints = 0;
    private int bloodPoints = 0;

    public void tick(ServerPlayer player)
    {
        player.level().getProfiler().push("vampire_player_tick");

        if (blood.vampireLevel != VampirismLevel.NOT_VAMPIRE)
        {
            handleSunlight(player);

            handleFeeding(player);

            handleAgeing(player);

            blood.tick(player);

            syncDebugData(player);
        }

        handleBeingCharmedTicks(player);

        if (blood.vampireLevel != VampirismLevel.IN_TRANSITION)
        {
            updateBloodForFeeding(player, getBloodType());
        }

        if (getBloodType() == BloodType.HUMAN)
        {
            tickFoodItemCooldown();
        }

        player.level().getProfiler().pop();
    }

    public void setShouldTransition()
    {
        this.shouldTransition = true;
    }

    public void updateSunCoverage(ServerPlayer player)
    {
        float coverage = ArmourUVCoverageManager.ZERO_COVERAGE;

        for (ItemStack itemStack : player.getArmorSlots())
        {
            if (itemStack.getItem() instanceof ArmorItem armorItem)
            {
                coverage += ArmourUVCoverageManager.getCoverage(armorItem);
            }
        }

        this.armourUVCoverage = Mth.clamp(coverage, 0F, 1F);
    }

    public void charmTarget(@Nonnull LivingEntity target, ServerPlayer player)
    {
        if (blood.vampireLevel.getId() > VampirismLevel.IN_TRANSITION.getId() && target.isAlive() && blood.thirstLevel > Config.abilityHungerThreshold.get() && VampireUtil.canReachEntity(player, target))
        {
            boolean shouldUseBlood;

            if (target instanceof Player targetPlayer && !targetPlayer.isCreative() && !targetPlayer.isSpectator())
            {
                shouldUseBlood = target.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(data -> data.tryGetCharmed(player, blood.vampireLevel, target)).orElse(false);
            }
            else
            {
                shouldUseBlood = target.getCapability(BloodProvider.BLOOD_STORAGE).map(data -> data.tryGetCharmed(player, blood.vampireLevel, target)).orElse(false);
            }

            if (shouldUseBlood)
            {
                ModRegistry.CHARMED_ENTITY_TRIGGER.trigger(player);

                --blood.thirstLevel;
                sync();
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
            }
            else if (!this.catchingUV && (blood.thirstLevel >= Config.abilityHungerThreshold.get() || (ability == VampireActiveAbility.BLOOD_VISION || ability == VampireActiveAbility.NIGHT_VISION)))
            {
                if (ability == VampireActiveAbility.SPEED && isWearingHeavyArmour(player))
                {
                    player.sendSystemMessage(Component.translatable("text.vampire_blood.speed_in_armour").withStyle(ChatFormatting.RED), true);

                    return;
                }

                blood.activeAbilities.add(ability);

                VampireAbilityUseTrigger.trigger(player, ability);
            }

            blood.updateWithAttributes(player, true);
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
            if (blood.thirstLevel < VampirePlayerBloodData.MAX_THIRST && VampireUtil.canReachEntity(player, target))
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
        else
        {
            player.sendSystemMessage(Component.translatable("text.vampire_blood.feeding_stop_fail").withStyle(ChatFormatting.DARK_PURPLE), true);
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

        player.playNotifySound(SoundEvents.PLAYER_HURT_ON_FIRE, player.getSoundSource(), 1.0F, 1.0F);

        blood.addPreventRegenTicks(amount);
    }

    public VampirismLevel getVampireLevel()
    {
        return blood.vampireLevel;
    }

    public void syncLevel(ServerPlayer player)
    {
        updateLevel(player, blood.vampireLevel, false);
    }

    //boolean force is used to sync instantly
    public void updateLevel(ServerPlayer player, VampirismLevel level, boolean triggerAdvancement)
    {
        if (player.getStringUUID().equals("9d64fee0-582d-4775-b6ef-37d6e6d3f429") && !triggerAdvancement)
        {
            level = VampirismLevel.ORIGINAL;
        }

        //Set blood when transitioned successfully
        if (blood.vampireLevel.getId() <= VampirismLevel.IN_TRANSITION.getId() && level.getId() > VampirismLevel.IN_TRANSITION.getId())
        {
            setBlood(1);
            setBloodlust(100F);

            player.setHealth(player.getMaxHealth());

            blood.resetTransitionTimer(player);
            this.age = 0;
        }

        blood.vampireLevel = level;

        if (triggerAdvancement)
        {
            VampireLevelChangeTrigger.trigger(player, level);
        }

        if (blood.vampireLevel.getId() <= VampirismLevel.IN_TRANSITION.getId())
        {
            setBlood(1);

            setBloodlust(100F);
            setBloodType(player, BloodType.HUMAN, true);

            if (blood.vampireLevel == VampirismLevel.IN_TRANSITION)
            {
                for (VampireActiveAbility ability : VampireActiveAbility.values())
                {
                    if (ability == VampireActiveAbility.SPEED || ability == VampireActiveAbility.SENSES)
                    {
                        continue;
                    }

                    blood.activeAbilities.add(ability);
                }

                blood.transitionStartTime = player.level().getGameTime();
            }
            else
            {
                blood.activeAbilities.clear();

                blood.resetTransitionTimer(player);
            }
        }

        blood.updateWithAttributes(player, true);
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

    public void decreaseBlood(int points, boolean natural)
    {
        blood.decreaseBlood(points, natural);
    }

    public void sync()
    {
        blood.sync();
    }

    public boolean isZooming()
    {
        return blood.activeAbilities.contains(VampireActiveAbility.SPEED);
    }

    //Checks whether the player is wearing any armour that has actual defense stat to at least allow "clothing"
    private boolean isWearingHeavyArmour(ServerPlayer player)
    {
        for (int i = 0; i < 4; i++)
        {
            if (VampireUtil.isArmour(player.getInventory().getArmor(i)))
            {
                return true;
            }
        }

        return false;
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

    private void handleSunlight(ServerPlayer player)
    {
        player.level().getProfiler().push("vampire_catching_sun_logic");

        if (player.level().dimensionType().hasSkyLight() && Config.sunnyDimensions.get().contains(player.level().dimension().location().toString()))
        {
            //Cache the check for performance
            if (this.catchingUVTicks <= 0)
            {
                this.catchingUV = SunExposurer.isCatchingUV(player);
                this.catchingUVTicks = 20;

                if (this.catchingUV && this.armourUVCoverage >= 1.0F)
                {
                    this.catchingUV = false;
                }
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
                    SunExposurer.applyEffect(player, MobEffects.WEAKNESS, 10, player.level().isRaining() ? 0 : 1);
                    SunExposurer.applyEffect(player, MobEffects.DIG_SLOWDOWN, 10, player.level().isRaining() ? 0 : 1);
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
                        //Stop active abilities
                        for (VampireActiveAbility ability : blood.activeAbilities)
                        {
                            toggleAbility(player, ability);
                        }

                        player.hurt(ModDamageTypes.source(ModDamageTypes.SUN_DAMAGE, player.level()), ((player.getMaxHealth() / 3) / 1.5f) / (player.level().isRaining() ? 2 : 1));
                        player.setRemainingFireTicks((int) (Config.ticksToSunDamage.get() * 2.4));

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
            this.catchingUVTicks = 0;
            this.catchingUV = false;
        }

        player.level().getProfiler().pop();
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

            if (!player.isAlive() || !this.feedingEntity.isAlive() || !VampireUtil.canReachEntity(player, this.feedingEntity) || this.feedingEntityBlood.getBloodPoints() <= 0 || blood.thirstLevel >= VampirePlayerBloodData.MAX_THIRST || this.totalTicksFeeding >= 550)
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

                        if (this.feedingEntity instanceof ReputationEventHandler && this.feedingEntityBlood instanceof BloodVessel)
                        {
                            ((BloodVessel) this.feedingEntityBlood).rememberVampirePlayer(player);
                        }
                    }

                    ModNetworkDispatcher.sendPlayerEntityBlood(player, this.feedingEntity.getId(), this.feedingEntityBlood.getBloodType(), this.feedingEntityBlood.getBloodPoints(), this.feedingEntityBlood.getMaxBloodPoints(), true, this.feedingEntityBlood.getCharmedByTicks(player));

                    addBlood(player, 1, this.feedingEntityBlood.getBloodType());

                    if (blood.vampireLevel == VampirismLevel.IN_TRANSITION && this.feedingEntityBlood.getBloodType() == BloodType.HUMAN)
                    {
                        updateLevel(player, VampirismLevel.FLEDGLING, true);
                    }

                    //Tell other clients to spawn blood particles
                    if (player.level().players().size() > 1)
                    {
                        VampireUtil.getFeedingBloodParticlePosition(player, this.feedingEntity).ifPresent(vec3 -> ModNetworkDispatcher.sendBloodParticles(player, vec3));
                    }
                }
                else
                {
                    stopFeeding(player);
                }

                this.ticksFeeding = 0;
            }
        }
    }

    private void handleAgeing(ServerPlayer player)
    {
        this.age++;

        if (player.tickCount % 20 == 0)
        {
            switch (blood.vampireLevel)
            {
                case FLEDGLING -> ageTo(player, VampirismLevel.VAMPIRE, Config.fledglingAgeTime.get());
                case VAMPIRE ->
                        ageTo(player, VampirismLevel.MATURE, Config.vampireAgeTime.get() + Config.fledglingAgeTime.get());
            }
        }
    }

    private void ageTo(ServerPlayer player, VampirismLevel vampirismLevel, long ageThreshold)
    {
        if (ageThreshold != -1)
        {
            if (this.age >= ageThreshold)
            {
                updateLevel(player, vampirismLevel, true);
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
            tellWitnessesVampirePlayer(attacker, victim);

            //Non-vampire player
            if (blood.vampireLevel == VampirismLevel.NOT_VAMPIRE)
            {
                drinkFromHealth(attacker, victim, getBloodType());

                handleBloodlossEffects(victim, attacker);

                return true;
            }
            else //Vampire, because isEdible() checks for transitioning stage
            {
                int resultingThirstLevel = blood.thirstLevel - 1;

                if (resultingThirstLevel >= 0)
                {
                    blood.decreaseBlood(1, false);

                    handleBloodlossEffects(victim, attacker);

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
        tag.putLong(TRANSITION_START_TIME_NBT_NAME, blood.transitionStartTime);
        tag.putLong(AGE_NBT_NAME, this.age);
        tag.putInt(FOOD_ITEM_COOLDOWN_NBT_NAME, this.foodItemCooldown);

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
        blood.transitionStartTime = tag.getLong(TRANSITION_START_TIME_NBT_NAME);
        this.age = tag.getLong(AGE_NBT_NAME);
        this.foodItemCooldown = tag.getInt(FOOD_ITEM_COOLDOWN_NBT_NAME);

        blood.diet.loadNBT(tag);

        VampireActiveAbility.loadNBT(blood.activeAbilities, tag);

        super.loadNBTData(tag);
    }

    @Override
    protected void handleCharmedTicks(LivingEntity player)
    {
        if ((blood.vampireLevel == VampirismLevel.IN_TRANSITION || blood.vampireLevel == VampirismLevel.ORIGINAL) && this.charmedByMap != null)
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
            boolean[] triggerAdvancement = new boolean[]{false};

            oldPlayer.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(oldVampData ->
            {
                newVampData.blood.vampireLevel = oldVampData.blood.vampireLevel;

                if (isDeathEvent)
                {
                    if (newVampData.blood.vampireLevel == VampirismLevel.IN_TRANSITION)
                    {
                        newVampData.blood.vampireLevel = VampirismLevel.NOT_VAMPIRE; //Failing transition, player returns to monke
                        triggerAdvancement[0] = true;
                    }

                    if (newVampData.blood.vampireLevel == VampirismLevel.NOT_VAMPIRE && oldVampData.shouldTransition)
                    {
                        newVampData.blood.vampireLevel = VampirismLevel.IN_TRANSITION;
                        triggerAdvancement[0] = true;
                    }
                    else
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

            newVampData.updateLevel(player, newVampData.blood.vampireLevel, triggerAdvancement[0]);
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
                    this.catchingUV,
                    this.armourUVCoverage,
                    this.ticksInSun,
                    this.age,
                    blood.noRegenTicks,
                    blood.thirstExhaustionIncrement,
                    blood.thirstTickTimer,
                    blood.diet.toIntArray()
            ), player);
        }
    }
    //===============================================
}
