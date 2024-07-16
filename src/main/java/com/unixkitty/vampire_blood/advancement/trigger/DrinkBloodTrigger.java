package com.unixkitty.vampire_blood.advancement.trigger;

import com.google.gson.JsonObject;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.advancement.predicate.BloodPredicate;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

public class DrinkBloodTrigger extends SimpleCriterionTrigger<DrinkBloodTrigger.TriggerInstance>
{
    private static DrinkBloodTrigger instance;

    static final ResourceLocation ID = new ResourceLocation(VampireBlood.MODID, "drink_blood");

    public static void register()
    {
        if (instance == null)
        {
            instance = CriteriaTriggers.register(new DrinkBloodTrigger());
        }
    }

    public static void trigger(ServerPlayer player, BloodType bloodType, float purity)
    {
        if (instance == null)
        {
            throw new IllegalStateException(DrinkBloodTrigger.class.getSimpleName() + " called before it was registered!");
        }
        else
        {
            instance._trigger(player, bloodType, purity);
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
    public DrinkBloodTrigger.TriggerInstance createInstance(JsonObject json, @Nonnull ContextAwarePredicate predicate, @Nonnull DeserializationContext deserializationContext)
    {
        BloodPredicate bloodPredicate = BloodPredicate.fromJson(json.getAsJsonObject(BloodPredicate.TIER_KEY));

        return new DrinkBloodTrigger.TriggerInstance(predicate, bloodPredicate);
    }

    public void _trigger(ServerPlayer player, BloodType bloodType, float purity)
    {
        this.trigger(player, (p_38777_) -> p_38777_.matches(bloodType, purity));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final BloodPredicate bloodPredicate;

        public TriggerInstance(ContextAwarePredicate player, BloodPredicate bloodPredicate)
        {
            super(DrinkBloodTrigger.ID, player);

            this.bloodPredicate = bloodPredicate;
        }

        public static DrinkBloodTrigger.TriggerInstance bloodConsumed(BloodType bloodType)
        {
            return new DrinkBloodTrigger.TriggerInstance(ContextAwarePredicate.ANY, BloodPredicate.blood(bloodType));
        }

        public static DrinkBloodTrigger.TriggerInstance bloodConsumed(BloodType bloodType, float currentPurity)
        {
            return new DrinkBloodTrigger.TriggerInstance(ContextAwarePredicate.ANY, BloodPredicate.blood(bloodType, currentPurity));
        }

        public boolean matches(BloodType bloodType, float purity)
        {
            return this.bloodPredicate.matches(bloodType, purity);
        }

        @Nonnull
        @Override
        public JsonObject serializeToJson(@Nonnull SerializationContext serializationContext)
        {
            JsonObject json = super.serializeToJson(serializationContext);

            json.add(BloodPredicate.TIER_KEY, this.bloodPredicate.serializeToJson());

            return json;
        }
    }
}
