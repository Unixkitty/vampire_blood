package com.unixkitty.vampire_blood.capability.attribute;

import com.unixkitty.vampire_blood.capability.VampireBloodType;
import com.unixkitty.vampire_blood.capability.VampirismStage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class VampireAttributeModifiers
{
    public static void updateAttributes(ServerPlayer player, VampirismStage vampirismStage, VampireBloodType bloodType)
    {
        for (Modifier modifier : Modifier.values())
        {
            float lastHealth = modifier == Modifier.HEALTH ? player.getHealth() : -1;

            //1. Remove existing modifier
            AttributeInstance attribute = player.getAttribute(modifier.getBaseAttribute());

            if (attribute != null)
            {
                AttributeModifier existingModifier = attribute.getModifier(modifier.getUuid());

                if (existingModifier != null)
                {
                    attribute.removeModifier(existingModifier);
                }

                //2. Calculate actual value to use
                final double modifierValue = modifier.getValue(attribute.getBaseValue(), vampirismStage, bloodType);

                //3. Add modifier to player
                if (modifierValue != -1)
                {
                    attribute.addPermanentModifier(new AttributeModifier(modifier.getUuid(), modifier.getName(), modifierValue, modifier.getModifierOperation()));
                }
                
                if (lastHealth != -1)
                {
                    player.setHealth(Math.min(lastHealth, player.getMaxHealth()));
                }
            }
        }
    }

    public enum Modifier
    {
        HEALTH(Attributes.MAX_HEALTH, "VampireHealthModifier", "43f72fe4-af73-4412-a5fc-a60f3a250aed", AttributeModifier.Operation.ADDITION),
        STRENGTH(Attributes.ATTACK_DAMAGE, "VampireStrengthModifier", "0a0caf30-6479-4e32-8ca9-42a84f1bd4ff", AttributeModifier.Operation.MULTIPLY_BASE),
        BASE_SPEED(Attributes.MOVEMENT_SPEED, "VampireBaseSpeedModifier", "036ae219-2165-410b-a8f1-c961ca7fc0c9", AttributeModifier.Operation.MULTIPLY_BASE);

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

        public UUID getUuid()
        {
            return uuid;
        }

        public double getValue(double baseValue, VampirismStage vampirismStage, VampireBloodType bloodType)
        {
            if (isApplicableStage(vampirismStage))
            {
                return switch (this.modifierOperation)
                {
                    case MULTIPLY_BASE -> (vampirismStage.getAttributeMultiplier(this) * bloodType.getAttributeMultiplier(this)) - 1.0D;
                    case ADDITION -> Math.round(((baseValue * vampirismStage.getAttributeMultiplier(this) * bloodType.getAttributeMultiplier(this)) - baseValue) / 2) * 2;
                    case MULTIPLY_TOTAL -> -1;
                };
            }

            return -1;
        }

        public boolean isApplicableStage(VampirismStage stage)
        {
            return switch (this)
            {
                case HEALTH -> stage.getId() > VampirismStage.IN_TRANSITION.getId();
                case STRENGTH -> stage.getId() > VampirismStage.NOT_VAMPIRE.getId();
                case BASE_SPEED -> stage == VampirismStage.IN_TRANSITION;
            };
        }

        public AttributeModifier.Operation getModifierOperation()
        {
            return modifierOperation;
        }
    }
}
