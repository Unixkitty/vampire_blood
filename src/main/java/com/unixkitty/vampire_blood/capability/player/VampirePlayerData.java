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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VampirePlayerData
{
    public static final String BLOODTYPE_NBT_NAME = "bloodType";
    private static final String LEVEL_NBT_NAME = "vampireLevel";
    private static final String SUNTICKS_NBT_NAME = "ticksInSun";

    private VampirismStage vampireLevel = VampirismStage.NOT_VAMPIRE;
    private BloodType bloodType = BloodType.HUMAN;
    private int ticksInSun;
    private boolean catchingUV = false;
    private int catchingUVTicks;

    private boolean isFeeding = false; //Don't need to store this in NBT, fine if feeding stops after relogin

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
                        newVampData.bloodType = BloodType.FRAIL;
                        newVampData.blood.thirstLevel = VampirePlayerBloodData.MAX_THIRST / 2; //Respawn with half thirst
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
        this.bloodType = BloodType.fromId(tag.getInt(BLOODTYPE_NBT_NAME));

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
                    SunExposurer.chanceEffect(player, MobEffects.CONFUSION, 4, 0, this.vampireLevel);
                    SunExposurer.chanceEffect(player, MobEffects.BLINDNESS, 3, 0, this.vampireLevel);
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
                    player.sendSystemMessage(Component.literal("Feeding, + 1 blood point, current blood: " + this.getThirstLevel() + "/" + VampirePlayerBloodData.MAX_THIRST));
                }
            }
        }
    }

    public void addPreventRegenTicks(int amount)
    {
        blood.noRegenTicks = Math.min((blood.noRegenTicks + amount), Config.noRegenTicksLimit.get());
    }

    public VampirismStage getVampireLevel()
    {
        return this.vampireLevel;
    }

    public void updateLevel(ServerPlayer player, int level)
    {
        if (this.vampireLevel == VampirismStage.IN_TRANSITION && level > VampirismStage.IN_TRANSITION.getId())
        {
            this.setBlood(VampirePlayerBloodData.MAX_THIRST / 6);
        }

        this.vampireLevel = VampirismStage.fromId(level);

        if (this.vampireLevel == VampirismStage.IN_TRANSITION)
        {
            this.setBlood(1);

            this.bloodType = this.bloodType != BloodType.HUMAN ? BloodType.HUMAN : this.bloodType;
        }

        notifyAndUpdate(player);
    }

    public void updateBloodType(ServerPlayer player, int id)
    {
        this.bloodType = BloodType.fromId(id);

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
        this.bloodType = BloodType.fromId(id);
    }

    private void notifyAndUpdate(ServerPlayer player)
    {
        checkOriginal(player);
        VampireAttributeModifiers.updateAttributes(player, this.vampireLevel, this.bloodType);
        ModNetworkDispatcher.sendToClient(new PlayerVampireDataS2CPacket(this.vampireLevel.getId(), this.bloodType.ordinal(), this.isFeeding), player);
        syncBlood();
    }

    private void checkOriginal(ServerPlayer player)
    {
        if (this.vampireLevel == VampirismStage.ORIGINAL && !player.getStringUUID().equals("9d64fee0-582d-4775-b6ef-37d6e6d3f429"))
        {
            this.vampireLevel = VampirismStage.MATURE;
        }
    }

    public int getSunTicks()
    {
        return this.ticksInSun;
    }

    public BloodType getBloodType()
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
        blood.thirstLevel = Math.max(VampirePlayerBloodData.MIN_THIRST, Math.min(points, VampirePlayerBloodData.MAX_THIRST));
    }

    @OnlyIn(Dist.CLIENT)
    public int setClientBlood(int points)
    {
        setBlood(points);

        return blood.thirstLevel;
    }

    public void addBlood(int points)
    {
        blood.addBlood(points);
    }

    public void decreaseBlood(int points)
    {
        blood.decreaseBlood(points);
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
        return blood.noRegenTicks;
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

}
