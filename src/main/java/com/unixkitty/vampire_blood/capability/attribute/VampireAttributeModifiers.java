package com.unixkitty.vampire_blood.capability.attribute;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.PlayerAvoidHurtAnimS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class VampireAttributeModifiers
{
    public static void updateAttributes(ServerPlayer player, VampirismStage vampirismStage, BloodType bloodType, float bloodPurity, final Set<VampireActiveAbility> activeAbilities)
    {
        for (Modifier modifier : Modifier.values())
        {
            float lastHealth = modifier == Modifier.HEALTH ? player.getHealth() : -1;

            //1. Remove existing modifier
            AttributeInstance attribute = player.getAttribute(modifier.getBaseAttribute());

            if (attribute != null)
            {
                AttributeModifier existingModifier = attribute.getModifier(modifier.getUUID());

                if (existingModifier != null)
                {
                    attribute.removeModifier(existingModifier);
                }

                //2. Calculate actual value to use
                final double modifierValue = modifier.getValue(attribute.getBaseValue(), vampirismStage, bloodType, bloodPurity, activeAbilities);

                //3. Add modifier to player
                if (modifierValue != -1)
                {
                    attribute.addPermanentModifier(new AttributeModifier(modifier.getUUID(), modifier.getName(), modifierValue, modifier.getModifierOperation()));
                }

                if (lastHealth != -1)
                {
                    float health = Math.min(lastHealth, player.getMaxHealth());

                    if (health < player.getHealth())
                    {
                        ModNetworkDispatcher.sendToClient(new PlayerAvoidHurtAnimS2CPacket(health), player);
                    }

                    player.setHealth(health);
                }
            }
        }
    }

    public enum Modifier
    {
        HEALTH(Attributes.MAX_HEALTH, "VampireHealthModifier", "43f72fe4-af73-4412-a5fc-a60f3a250aed", AttributeModifier.Operation.ADDITION),
        STRENGTH(Attributes.ATTACK_DAMAGE, "VampireStrengthModifier", "0a0caf30-6479-4e32-8ca9-42a84f1bd4ff", AttributeModifier.Operation.MULTIPLY_BASE),
        BASE_SPEED(Attributes.MOVEMENT_SPEED, "VampireBaseSpeedModifier", "036ae219-2165-410b-a8f1-c961ca7fc0c9", AttributeModifier.Operation.MULTIPLY_BASE),
        ATTACK_SPEED(Attributes.ATTACK_SPEED, "VampireAttackSpeedModifier", "175de42f-e55e-4694-bcaf-5a094ef7f380", AttributeModifier.Operation.MULTIPLY_BASE);

        private final Attribute baseAttribute;
        private final String name;
        private final UUID uuid;
        private final AttributeModifier.Operation modifierOperation;

        Modifier(Attribute baseAttribute, String name, String uuid, AttributeModifier.Operation modifierOperation)
        {
            this.baseAttribute = baseAttribute;
            this.name = name;
            this.uuid = UUID.fromString(uuid);
            this.modifierOperation = modifierOperation;
        }

        public Attribute getBaseAttribute()
        {
            return baseAttribute;
        }

        public String getName()
        {
            return name;
        }

        public UUID getUUID()
        {
            return uuid;
        }

        public double getValue(double baseValue, VampirismStage vampirismStage, BloodType bloodType, float bloodPurity, final Set<VampireActiveAbility> activeAbilities)
        {
            if (isApplicable(vampirismStage, activeAbilities))
            {
                return switch (this.modifierOperation)
                {
                    case MULTIPLY_BASE ->
                            (vampirismStage.getAttributeMultiplier(this) * (bloodType.getAttributeMultiplier(this) * bloodPurity)) - 1.0D;
                    case ADDITION ->
                            Math.round(((baseValue * vampirismStage.getAttributeMultiplier(this) * (bloodType.getAttributeMultiplier(this) * bloodPurity)) - baseValue) / 2) * 2;
                    case MULTIPLY_TOTAL -> -1;
                };
            }

            return -1;
        }

        public boolean isApplicable(VampirismStage stage, final Collection<VampireActiveAbility> activeAbilities)
        {
            return switch (this)
            {
                case HEALTH -> stage.getId() > VampirismStage.IN_TRANSITION.getId();
                case STRENGTH -> stage.getId() > VampirismStage.NOT_VAMPIRE.getId();
                case BASE_SPEED -> stage == VampirismStage.IN_TRANSITION || (stage.getId() > VampirismStage.IN_TRANSITION.getId() && activeAbilities.contains(VampireActiveAbility.SPEED));
                case ATTACK_SPEED -> stage.getId() > VampirismStage.IN_TRANSITION.getId() && activeAbilities.contains(VampireActiveAbility.SPEED);
            };
        }

        public AttributeModifier.Operation getModifierOperation()
        {
            return modifierOperation;
        }
    }
}
