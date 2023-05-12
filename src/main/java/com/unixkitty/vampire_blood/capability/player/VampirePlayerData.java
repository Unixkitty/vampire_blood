package com.unixkitty.vampire_blood.capability.player;

import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.init.ModRegistry;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.DebugDataSyncS2CPacket;
import com.unixkitty.vampire_blood.network.packet.PlayerRespawnS2CPacket;
import com.unixkitty.vampire_blood.network.packet.PlayerVampireDataS2CPacket;
import com.unixkitty.vampire_blood.util.SunExposurer;
import com.unixkitty.vampire_blood.util.VampirismTier;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VampirePlayerData
{
    private static final String LEVEL_NBT_NAME = "vampireLevel";
    private static final String SUNTICKS_NBT_NAME = "ticksInSun";
    private static final String THIRST_NBT_NAME = "thirstLevel";
    private static final String THIRST_EXHAUSTION_NBT_NAME = "thirstExhaustion";
    private static final String THIRST_TIMER_NBT_NAME = "thirstTickTimer";
    private static final String THIRST_EXHAUSTION_INCREMENT_NBT_NAME = "thirstExhaustionIncrement";
    private static final String NO_REGEN_TICKS = "noRegenTicks";
    private static final String BLOODLUST_NBT_NAME = "bloodlust";
    private static final String LAST_CONSUMED_BLOOD_TYPE_NBT_NAME = "lastConsumedBloodtype";
    private static final String CONSECUTIVE_BLOOD_POINTS_NBT_NAME = "consecutiveBloodtypePoints";

    private int ticksInSun;
    private boolean catchingUV = false;
    private int catchingUVTicks;

    private boolean isFeeding = false;

    private final VampirePlayerBloodData blood = new VampirePlayerBloodData();

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
        if (blood.vampireLevel != VampirismStage.NOT_VAMPIRE)
        {
            if (blood.vampireLevel == VampirismStage.IN_TRANSITION)
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

            ModNetworkDispatcher.sendToClient(new PlayerRespawnS2CPacket(newVampData.blood.vampireLevel.getId(), newVampData.blood.bloodType.getId(), newVampData.blood.thirstLevel), (ServerPlayer) newPlayer);
        });
    }

    public void tick(Player player)
    {
        player.level.getProfiler().push("vampire_tick");

        if (blood.vampireLevel != VampirismStage.NOT_VAMPIRE)
        {
            handleSunlight(player);

            handleFeeding(player);

            syncDebugData(player); //TODO remove debug

            syncData(player);

            blood.tick(player);
        }

        player.level.getProfiler().pop();
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

    private void handleFeeding(Player player)
    {
        if (this.isFeeding)
        {
            ++this.ticksFeeding;

            if (this.ticksFeeding >= 10)
            {
                //TODO actually drain some entity
                addBlood(player, 1, BloodType.FRAIL);

                this.ticksFeeding = 0;

//                player.sendSystemMessage(Component.literal("Feeding, + 1 blood point, current blood: " + this.getThirstLevel() + "/" + VampirePlayerBloodData.MAX_THIRST));
            }
        }
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
            this.setBlood(VampirePlayerBloodData.MAX_THIRST / 6);
        }

        blood.vampireLevel = level;

        if (blood.vampireLevel == VampirismStage.IN_TRANSITION)
        {
            this.setBlood(1);

            this.blood.bloodType = BloodType.HUMAN;
        }

        notifyAndUpdate(player);
    }

    public void setBloodType(ServerPlayer player, BloodType type)
    {
        this.blood.bloodType = type;

        notifyAndUpdate(player);
    }

    @OnlyIn(Dist.CLIENT)
    public void setVampireLevel(int level)
    {
        blood.vampireLevel = VampirismTier.fromId(VampirismStage.class, level);
    }

    @OnlyIn(Dist.CLIENT)
    public void setBloodType(int id)
    {
        this.blood.bloodType = VampirismTier.fromId(BloodType.class, id);
    }

    private void notifyAndUpdate(ServerPlayer player)
    {
        checkOriginal(player);
        VampireAttributeModifiers.updateAttributes(player, blood.vampireLevel, blood.bloodType);
        ModNetworkDispatcher.sendToClient(new PlayerVampireDataS2CPacket(blood.vampireLevel.getId(), blood.bloodType.getId(), isFeeding), player);
        syncBlood();
    }

    private void checkOriginal(ServerPlayer player)
    {
        if (blood.vampireLevel == VampirismStage.ORIGINAL && !player.getStringUUID().equals("9d64fee0-582d-4775-b6ef-37d6e6d3f429"))
        {
            blood.vampireLevel = VampirismStage.MATURE;
        }
    }

    public BloodType getBloodType()
    {
        return this.blood.bloodType;
    }

    public BloodType getBloodTypeIdForFeeding()
    {
        return blood.vampireLevel == VampirismStage.NOT_VAMPIRE ? BloodType.HUMAN : blood.vampireLevel.getId() > VampirismStage.IN_TRANSITION.getId() ? BloodType.VAMPIRE : BloodType.NONE;
    }

    public void setFeeding(boolean feeding)
    {
        this.isFeeding = feeding;
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

    public void addBlood(Player player, int points, BloodType bloodType)
    {
        blood.addBlood(player, points, bloodType);
    }

    public void decreaseBlood(Player player, int points, BloodType bloodType)
    {
        blood.decreaseBlood(player, points, bloodType);
    }

    public void sync()
    {
        this.needsSync = true;
    }

    public void syncBlood()
    {
        blood.sync();
    }

    public void saveNBTData(CompoundTag tag)
    {
        tag.putInt(LEVEL_NBT_NAME, blood.vampireLevel.getId());
        tag.putInt(SUNTICKS_NBT_NAME, this.ticksInSun);
        tag.putInt(BloodType.BLOODTYPE_NBT_NAME, blood.bloodType.getId());
        tag.putInt(THIRST_NBT_NAME, blood.thirstLevel);
        tag.putInt(THIRST_EXHAUSTION_NBT_NAME, blood.thirstExhaustion);
        tag.putInt(THIRST_TIMER_NBT_NAME, blood.thirstTickTimer);
        tag.putInt(THIRST_EXHAUSTION_INCREMENT_NBT_NAME, blood.thirstExhaustionIncrement);
        tag.putInt(NO_REGEN_TICKS, blood.noRegenTicks);
        tag.putFloat(BLOODLUST_NBT_NAME, blood.bloodlust);
        tag.putInt(LAST_CONSUMED_BLOOD_TYPE_NBT_NAME, blood.lastConsumedBloodtype.getId());
        tag.putInt(CONSECUTIVE_BLOOD_POINTS_NBT_NAME, blood.consecutiveBloodtypePoints);
    }

    public void loadNBTData(CompoundTag tag)
    {
        blood.vampireLevel = VampirismTier.fromId(VampirismStage.class, tag.getInt(LEVEL_NBT_NAME));
        this.ticksInSun = tag.getInt(SUNTICKS_NBT_NAME);
        blood.bloodType = VampirismTier.fromId(BloodType.class, tag.getInt(BloodType.BLOODTYPE_NBT_NAME));
        blood.thirstLevel = tag.getInt(THIRST_NBT_NAME);
        blood.thirstExhaustion = tag.getInt(THIRST_EXHAUSTION_NBT_NAME);
        blood.thirstTickTimer = tag.getInt(THIRST_TIMER_NBT_NAME);
        blood.thirstExhaustionIncrement = tag.getInt(THIRST_EXHAUSTION_INCREMENT_NBT_NAME);
        blood.noRegenTicks = tag.getInt(NO_REGEN_TICKS);
        blood.bloodlust = tag.getFloat(BLOODLUST_NBT_NAME);
        blood.lastConsumedBloodtype = VampirismTier.fromId(BloodType.class, tag.getInt(LAST_CONSUMED_BLOOD_TYPE_NBT_NAME));
        blood.consecutiveBloodtypePoints = tag.getInt(CONSECUTIVE_BLOOD_POINTS_NBT_NAME);
    }

    private void syncData(Player player)
    {
        if (this.needsSync)
        {
            this.needsSync = false;

            checkOriginal((ServerPlayer) player);

            ModNetworkDispatcher.sendToClient(new PlayerVampireDataS2CPacket(blood.vampireLevel.getId(), this.blood.bloodType.getId(), this.isFeeding), (ServerPlayer) player);
        }
    }

    //TODO remove debug
    //===============================================
    private void syncDebugData(Player player)
    {
        ModNetworkDispatcher.sendToClient(new DebugDataSyncS2CPacket(this.ticksInSun, this.ticksFeeding, blood.noRegenTicks, blood.thirstExhaustionIncrement, blood.thirstTickTimer, blood.consecutiveBloodtypePoints), (ServerPlayer) player);
    }
    //===============================================

}
