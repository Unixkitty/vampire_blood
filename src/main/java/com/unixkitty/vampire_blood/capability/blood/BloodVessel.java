package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.capability.provider.BloodProvider;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.init.ModDamageTypes;
import com.unixkitty.vampire_blood.init.ModEffects;
import com.unixkitty.vampire_blood.init.ModRegistry;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.EntityCharmedStatusS2CPacket;
import com.unixkitty.vampire_blood.network.packet.SuccessfulCharmS2CPacket;
import com.unixkitty.vampire_blood.util.VampireUtil;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public abstract class BloodVessel implements IBloodVessel
{
    @Nullable
    protected LivingEntity bloodVessel = null;
    @Nullable
    protected Object2IntOpenHashMap<UUID> charmedByMap = null;
    @Nullable
    protected ObjectOpenHashSet<UUID> knownVampirePlayers = null;
    @Nullable
    private Player lastCharmedPlayer = null;

    protected int foodItemCooldown;

    public void saveNBTData(CompoundTag tag)
    {
        if (this.charmedByMap != null)
        {
            CompoundTag subTag = new CompoundTag();

            this.charmedByMap.forEach((uuid, integer) -> subTag.putInt(uuid.toString(), integer));

            tag.put(CHARMED_BY_NBT_NAME, subTag);
        }

        if (this.knownVampirePlayers != null && this.bloodVessel != null && this.bloodVessel instanceof ReputationEventHandler)
        {
            CompoundTag subTag = new CompoundTag();

            int i = 0;

            for (UUID uuid : this.knownVampirePlayers)
            {
                subTag.putUUID(String.valueOf(i++), uuid);
            }

            tag.put(KNOWN_VAMPIRE_PLAYERS_NBT_NAME, subTag);
        }
    }

    public void loadNBTData(CompoundTag tag)
    {
        CompoundTag subTagCharmedBy = tag.getCompound(CHARMED_BY_NBT_NAME);

        if (subTagCharmedBy.size() > 0)
        {
            this.charmedByMap = new Object2IntOpenHashMap<>();

            for (String key : subTagCharmedBy.getAllKeys())
            {
                this.charmedByMap.put(UUID.fromString(key), subTagCharmedBy.getInt(key));
            }
        }

        CompoundTag subTagKnownVampires = tag.getCompound(KNOWN_VAMPIRE_PLAYERS_NBT_NAME);

        if (subTagKnownVampires.size() > 0)
        {
            this.knownVampirePlayers = new ObjectOpenHashSet<>();

            for (int i = 0; i < subTagKnownVampires.size(); i++)
            {
                this.knownVampirePlayers.add(subTagKnownVampires.getUUID(String.valueOf(i)));
            }
        }
    }

    public boolean isCurrentlyCharmingPlayer(@Nonnull LivingEntity entity)
    {
        return entity instanceof ServerPlayer player && !player.isCreative() && !player.isSpectator() && player.equals(this.lastCharmedPlayer) && isCharmedBy(player) && VampireUtil.isVampire(player);
    }

    public void rememberVampirePlayer(@Nonnull ServerPlayer player)
    {
        if (this.knownVampirePlayers == null)
        {
            this.knownVampirePlayers = new ObjectOpenHashSet<>();
        }

        this.knownVampirePlayers.add(player.getUUID());
    }

    protected void handleCharmedTicks(LivingEntity entity)
    {
        if (this.charmedByMap != null)
        {
            for (UUID key : this.charmedByMap.keySet())
            {
                int value = this.charmedByMap.getInt(key);

                if (value > 0)
                {
                    this.charmedByMap.addTo(key, value >= 20 ? -20 : -value);
                }

                if (value == 0)
                {
                    this.charmedByMap.removeInt(key);
                }

                ServerPlayer player = (ServerPlayer) entity.level().getPlayerByUUID(key);

                //Check if player is actually logged on and if they're nearby before sending packet
                if (player != null
                        && VampireUtil.isVampire(player)
                        && player.equals(entity.level().getNearestPlayer(TargetingConditions.forNonCombat().range(ModEffects.SENSES_DISTANCE_LIMIT).selector(target -> target.equals(player)), entity)))
                {
                    ModNetworkDispatcher.sendToClient(new EntityCharmedStatusS2CPacket(entity.getId(), value != 0), player);
                }
            }
        }
    }

    protected void tellWitnessesVampirePlayer(@Nonnull LivingEntity attacker, @Nonnull LivingEntity victim)
    {
        if (attacker instanceof ServerPlayer player)
        {
            if (victim.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
            {
                victim.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent(entities -> entities.findAll(ReputationEventHandler.class::isInstance).forEach(witness ->
                {
                    if (shouldBeNotified(witness, victim, player))
                    {
                        notifyWitness(witness, player);
                    }
                }));
            }
            else
            {
                for (LivingEntity entity : player.level().getNearbyEntities(LivingEntity.class, TargetingConditions.forNonCombat().selector(witness -> witness instanceof ReputationEventHandler && shouldBeNotified(witness, victim, player)), player, victim.getBoundingBox().inflate(32.0D)))
                {
                    notifyWitness(entity, player);
                }
            }
        }
    }

    private boolean shouldBeNotified(LivingEntity witness, LivingEntity victim, ServerPlayer player)
    {
        return !witness.isSleeping() && witness.equals(victim) ? !isCharmedBy(player) : !VampireUtil.isEntityCharmedBy(witness, player);
    }

    private void notifyWitness(LivingEntity witness, ServerPlayer player)
    {
        ((ServerLevel) player.level()).onReputationEvent(ModRegistry.REPUTATION_VAMPIRE_PLAYER, player, (ReputationEventHandler) witness);

        witness.getCapability(BloodProvider.BLOOD_STORAGE).ifPresent(bloodEntityStorage -> bloodEntityStorage.rememberVampirePlayer(player));
    }

    @Override
    public void dieFromBloodLoss(@Nonnull LivingEntity victim, @Nonnull LivingEntity attacker)
    {
        victim.setLastHurtByMob(attacker);
        victim.hurt(ModDamageTypes.source(ModDamageTypes.BLOOD_LOSS, victim.level(), attacker), Float.MAX_VALUE);
    }

    @Override
    public void drinkFromHealth(@Nonnull LivingEntity attacker, @Nonnull LivingEntity victim, @Nonnull BloodType bloodType)
    {
        float resultingHealth = victim.getHealth() - (1F / bloodType.getBloodSaturationModifier());

        if (resultingHealth > 0)
        {
            victim.setHealth(resultingHealth);
        }
        else
        {
            dieFromBloodLoss(victim, attacker);
        }
    }

    @Override
    public void handleBeingCharmedTicks(@Nonnull LivingEntity entity)
    {
        if (entity.tickCount % 20 == 0)
        {
            handleCharmedTicks(entity);
        }
    }

    @Override
    public boolean isCharmedBy(@Nonnull ServerPlayer player)
    {
        return this.charmedByMap != null && this.charmedByMap.containsKey(player.getUUID());
    }

    @Override
    public int getCharmedByTicks(@Nonnull ServerPlayer player)
    {
        return this.charmedByMap == null ? -2 : this.charmedByMap.getOrDefault(player.getUUID(), -2);
    }

    @Override
    public boolean setCharmedBy(@Nonnull ServerPlayer player, @Nonnull LivingEntity target)
    {
        if (this.charmedByMap == null)
        {
            this.charmedByMap = new Object2IntOpenHashMap<>();
        }

        UUID uuid = player.getUUID();

        if (this.charmedByMap.containsKey(uuid))
        {
            this.charmedByMap.put(uuid, 0);

            return false;
        }
        else
        {
            this.charmedByMap.put(uuid, player.getStringUUID().equals("9d64fee0-582d-4775-b6ef-37d6e6d3f429") ? -1 : Config.charmEffectDuration.get());

            this.lastCharmedPlayer = player;

            ModNetworkDispatcher.sendToClient(new SuccessfulCharmS2CPacket(target.getId()), player);

            if (target instanceof ReputationEventHandler)
            {
                ((ServerLevel) player.level()).onReputationEvent(ModRegistry.REPUTATION_CHARMED_BY_VAMPIRE_PLAYER, player, (ReputationEventHandler) target);

                if (this.knownVampirePlayers != null)
                {
                    this.knownVampirePlayers.remove(player.getUUID());

                    this.lastCharmedPlayer = null;
                }
            }
        }

        if (this.charmedByMap.isEmpty() && this.lastCharmedPlayer != null)
        {
            this.lastCharmedPlayer = null;
        }

        return true;
    }

    @Override
    public boolean tryGetCharmed(@Nonnull ServerPlayer player, VampirismLevel attackerLevel, @Nonnull LivingEntity target)
    {
        return switch (getBloodType())
        {
            case VAMPIRE -> attackerLevel == VampirismLevel.ORIGINAL && setCharmedBy(player, target);
            case CREATURE, HUMAN, PIGLIN -> setCharmedBy(player, target) && notifyPlayerCharmed(player, target);
            default -> false;
        };
    }

    @Override
    public boolean hasNoFoodItemCooldown()
    {
        return this.foodItemCooldown <= 0;
    }

    @Override
    public void addFoodItemCooldown(LivingEntity entity, ItemStack stack)
    {
        if (stack.isEdible())
        {
            this.foodItemCooldown += stack.getUseDuration();

            this.foodItemCooldown = entity instanceof Player ? this.foodItemCooldown * 2 : this.foodItemCooldown + (int) (entity.getRandom().nextFloat() * Config.entityFoodItemMaxCooldown.get());

            VampireUtil.applyEffect(entity, MobEffects.UNLUCK, this.foodItemCooldown, 0);
        }
    }

    @Override
    public int getFoodItemCooldown()
    {
        return this.foodItemCooldown;
    }

    protected void tickFoodItemCooldown()
    {
        if (this.foodItemCooldown > 0)
        {
            this.foodItemCooldown--;
        }
    }

    private boolean notifyPlayerCharmed(Player charmingPlayer, LivingEntity target)
    {
        if (target instanceof ServerPlayer targetPlayer)
        {
            targetPlayer.sendSystemMessage(Component.translatable("text.vampire_blood.feeling_charmed", charmingPlayer.getDisplayName().getString()).withStyle(ChatFormatting.DARK_PURPLE), true);
        }

        return true;
    }
}
