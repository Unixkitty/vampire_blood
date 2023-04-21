package com.unixkitty.vampire_blood.capability;

import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.DebugDataSyncS2CPacket;
import com.unixkitty.vampire_blood.network.packet.PlayerBloodDataSyncS2CPacket;
import com.unixkitty.vampire_blood.network.packet.PlayerVampireDataS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VampirePlayerData
{
    private static final String LEVEL_NBT_NAME = "vampireLevel";
    private static final String SUNTICKS_NBT_NAME = "ticksInSun";
    private static final String BLOODTYPE_NBT_NAME = "bloodType";

    private Stage vampireLevel = Stage.NOT_VAMPIRE;
    private int ticksInSun;
    private VampireBloodType bloodType = VampireBloodType.NONE;

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
        if (this.vampireLevel != Stage.NOT_VAMPIRE)
        {
            if (this.vampireLevel == Stage.IN_TRANSITION)
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

    public void copyOnDeath(VampirePlayerData source)
    {
        this.vampireLevel = source.vampireLevel;
        this.bloodType = VampireBloodType.FRAIL;
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
        this.vampireLevel = Stage.fromId(tag.getInt(LEVEL_NBT_NAME));
        this.ticksInSun = tag.getInt(SUNTICKS_NBT_NAME);
        this.bloodType = VampireBloodType.fromId(tag.getInt(BLOODTYPE_NBT_NAME));

        blood.loadNBTData(tag);
    }

    public void tick(Player player)
    {
        if (this.vampireLevel != Stage.NOT_VAMPIRE)
        {
            handleFeeding(player);

            if (Config.isDebug) syncDebugData(player);

            syncData(player);

            blood.tick(player);
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

                if (Config.isDebug) player.sendSystemMessage(Component.literal("Feeding, + 1 blood point, current blood: " + this.getThirstLevel() + "/" + Blood.MAX_THIRST));
            }
        }
    }

    public Stage getVampireLevel()
    {
        return this.vampireLevel;
    }

    public void setVampireLevel(int level)
    {
        this.vampireLevel = Stage.fromId(level);
    }

    public void setBloodType(int id)
    {
        this.bloodType = VampireBloodType.fromId(id);
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

            ModNetworkDispatcher.sendToClient(new PlayerVampireDataS2CPacket(this.vampireLevel.getId(), this.bloodType.ordinal(), this.isFeeding), (ServerPlayer) player);
        }
    }

    public static boolean isVampire(Player player)
    {
        return player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.getVampireLevel() != Stage.NOT_VAMPIRE && vampirePlayerData.getVampireLevel() != Stage.IN_TRANSITION).orElse(false);
    }

    public static boolean isTransitioning(Player player)
    {
        return player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.getVampireLevel() == Stage.IN_TRANSITION).orElse(false);
    }

    //TODO remove debug
    //===============================================
    private void syncDebugData(Player player)
    {
        ModNetworkDispatcher.sendToClient(new DebugDataSyncS2CPacket(this), (ServerPlayer) player);
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

    @OnlyIn(Dist.CLIENT)
    public void setClientDebugData(int ticksInSun, int ticksFeeding, int thirstExhaustion, int thirstExhaustionIncrement, int thirstTickTimer)
    {
        this.ticksInSun = ticksInSun;
        this.ticksFeeding = ticksFeeding;
        this.blood.thirstExhaustion = thirstExhaustion;
        this.blood.thirstExhaustionIncrement = thirstExhaustionIncrement;
        this.blood.thirstTickTimer = thirstTickTimer;
    }

    /*@OnlyIn(Dist.CLIENT)
    public void tickClient(Player player)
    {
        if (isVampire(player))
        {

        }
    }*/
    //===============================================

    public enum Stage
    {
        NOT_VAMPIRE(-1, 1, 1, 1),
        IN_TRANSITION(0, 1, 1, 1),
        FLEDGLING(1, 3, 2, 2),
        VAMPIRE(2, 4, 3, 3),
        MATURE(3, 5, 4, 4),
        ORIGINAL(999, 10, 5, 4);

        final int id;
        final double healthMultiplier;
        final double attackMultiplier;
        final double speedBoostMultiplier;

        Stage(int id, double healthMultiplier, double attackMultiplier, double speedBoostMultiplier)
        {
            this.id = id;
            this.healthMultiplier = healthMultiplier;
            this.attackMultiplier = attackMultiplier;
            this.speedBoostMultiplier = speedBoostMultiplier;
        }

        public int getId()
        {
            return id;
        }

        public static Stage fromId(int id)
        {
            for (Stage stage : values())
            {
                if (stage.id == id) return stage;
            }

            return NOT_VAMPIRE;
        }
    }

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

        private float vanillaExhaustionDelta;

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

        private void tick(Player player)
        {
            boolean isPeaceful = player.level.getDifficulty() == Difficulty.PEACEFUL;

            //TODO special handling when Stage == IN_TRANSITION

            //TODO deal with Hunger status effect
            this.vanillaExhaustionDelta = player.getFoodData().getExhaustionLevel() * Config.bloodUsageRate.get();

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

                    if (Config.isDebug) player.sendSystemMessage(Component.literal("Using, - 1 blood point, current blood: " + this.thirstLevel + "/" + MAX_THIRST));
                }
            }

            handleExhaustion(player, isPeaceful);

            this.syncData(player);
        }

        private void handleExhaustion(Player player, boolean isPeaceful)
        {
            if (this.vanillaExhaustionDelta > 0)
            {
                exhaustionIncrement();
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
            if ((Config.naturalHealthRegenWithGamerule.get() ? (Config.naturalHealthRegen.get() && player.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) : Config.naturalHealthRegen.get()) && this.thirstLevel > 0 && player.isHurt())
            {
                ++this.thirstTickTimer;

                if (this.thirstTickTimer >= Config.naturalHealingRate.get())
                {
                    player.heal(1.0F);

                    exhaustionIncrement(BloodRates.HEALING, Config.naturalHealingRate.get());
                    this.thirstTickTimer = 0;
                }
            }
            else if (this.thirstLevel <= 0)
            {
                ++this.thirstTickTimer;

                if (this.thirstTickTimer >= 80)
                {
                    //Similar to vanilla
                    if (isPeaceful)
                    {
                        addBlood(1);
                    }
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

        private void exhaustionIncrement()
        {
            VampireBlood.log().debug("Vanilla exhaustion above 0: " + this.vanillaExhaustionDelta);

            this.thirstExhaustionIncrement += this.vanillaExhaustionDelta < 1.0F ? 1 : this.vanillaExhaustionDelta;
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
