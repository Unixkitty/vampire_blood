package com.unixkitty.vampire_blood.advancement.trigger;

import com.google.gson.JsonObject;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.advancement.predicate.VampiricAbilityPredicate;
import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

public class VampireAbilityUseTrigger extends SimpleCriterionTrigger<VampireAbilityUseTrigger.TriggerInstance>
{
    private static VampireAbilityUseTrigger instance;

    static final ResourceLocation ID = new ResourceLocation(VampireBlood.MODID, "vampire_ability_use");

    public static void register()
    {
        if (instance == null)
        {
            instance = CriteriaTriggers.register(new VampireAbilityUseTrigger());
        }
    }

    public static void trigger(ServerPlayer player, VampireActiveAbility ability)
    {
        if (instance == null)
        {
            throw new IllegalStateException(VampireAbilityUseTrigger.class.getSimpleName() + " called before it was registered!");
        }
        else
        {
            instance._trigger(player, ability);
        }
    }

    @Override
    @Nonnull
    public ResourceLocation getId()
    {
        return ID;
    }

    @Nonnull
    @Override
    public VampireAbilityUseTrigger.TriggerInstance createInstance(JsonObject json, @Nonnull ContextAwarePredicate predicate, @Nonnull DeserializationContext deserializationContext)
    {
        VampiricAbilityPredicate vampiricAbilityPredicate = VampiricAbilityPredicate.fromJson(json.getAsJsonObject(VampiricAbilityPredicate.ABILITY_KEY));

        return new VampireAbilityUseTrigger.TriggerInstance(predicate, vampiricAbilityPredicate);
    }

    public void _trigger(ServerPlayer player, VampireActiveAbility ability)
    {
        this.trigger(player, (p_38777_) -> p_38777_.matches(ability));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final VampiricAbilityPredicate vampiricAbilityPredicate;

        public TriggerInstance(ContextAwarePredicate player, VampiricAbilityPredicate vampiricAbilityPredicate)
        {
            super(VampireAbilityUseTrigger.ID, player);

            this.vampiricAbilityPredicate = vampiricAbilityPredicate;
        }

        public static VampireAbilityUseTrigger.TriggerInstance abilityUsed(VampireActiveAbility ability)
        {
            return new VampireAbilityUseTrigger.TriggerInstance(ContextAwarePredicate.ANY, VampiricAbilityPredicate.ability(ability));
        }

        public boolean matches(VampireActiveAbility ability)
        {
            return this.vampiricAbilityPredicate.matches(ability);
        }

        @Nonnull
        @Override
        public JsonObject serializeToJson(@Nonnull SerializationContext serializationContext)
        {
            JsonObject json = super.serializeToJson(serializationContext);

            json.add(VampiricAbilityPredicate.ABILITY_KEY, this.vampiricAbilityPredicate.serializeToJson());

            return json;
        }
    }
}
