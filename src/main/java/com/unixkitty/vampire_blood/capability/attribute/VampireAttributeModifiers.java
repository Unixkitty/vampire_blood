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
    //AttributeModifier healthModifier = new AttributeModifier(healthModifierUUID, healthModifierName, VampirismStage.VAMPIRE.getHealthMultiplier(), healthOperation);

    public static void updateAttributes(ServerPlayer player, VampirismStage vampirismStage, VampireBloodType bloodType)
    {
        for (Modifier modifier : Modifier.values())
        {
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
                double modifierValue = -1;

                if (vampirismStage.getId() > VampirismStage.IN_TRANSITION.getId())
                {
                    if (modifier.getModifierOperation() == AttributeModifier.Operation.MULTIPLY_BASE)
                    {
                        modifierValue = (vampirismStage.getAttributeMultiplier(modifier) * bloodType.getAttributeMultiplier(modifier)) - 1.0D;
                    }
                    else if (modifier.getModifierOperation() == AttributeModifier.Operation.ADDITION)
                    {
                        modifierValue = Math.round(((attribute.getBaseValue() * vampirismStage.getAttributeMultiplier(modifier) * bloodType.getAttributeMultiplier(modifier)) - attribute.getBaseValue()) / 2) * 2;
                    }
                }

                //3. Add modifier to player
                if (modifierValue != -1)
                {
                    attribute.addPermanentModifier(new AttributeModifier(modifier.getUuid(), modifier.getName(), modifierValue, modifier.getModifierOperation()));
                }
            }
        }
    }

    public enum Modifier
    {
        HEALTH(Attributes.MAX_HEALTH, "VampireHealthModifier", "43f72fe4-af73-4412-a5fc-a60f3a250aed", AttributeModifier.Operation.ADDITION),
        STRENGTH(Attributes.ATTACK_DAMAGE, "VampireStrengthModifier", "0a0caf30-6479-4e32-8ca9-42a84f1bd4ff", AttributeModifier.Operation.MULTIPLY_BASE);

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

        public AttributeModifier.Operation getModifierOperation()
        {
            return modifierOperation;
        }
    }
}
