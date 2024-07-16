package com.unixkitty.vampire_blood.advancement.trigger;

import com.google.gson.JsonObject;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.advancement.predicate.VampirismLevelPredicate;
import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

public class VampireLevelChangeTrigger extends SimpleCriterionTrigger<VampireLevelChangeTrigger.TriggerInstance>
{
    private static VampireLevelChangeTrigger instance;

    static final ResourceLocation ID = new ResourceLocation(VampireBlood.MODID, "vampire_level_change");

    public static void register()
    {
        if (instance == null)
        {
            instance = CriteriaTriggers.register(new VampireLevelChangeTrigger());
        }
    }

    public static void trigger(ServerPlayer player, VampirismLevel vampirismLevel)
    {
        if (instance == null)
        {
            throw new IllegalStateException(VampireLevelChangeTrigger.class.getSimpleName() + " called before it was registered!");
        }
        else
        {
            instance._trigger(player, vampirismLevel);
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
    public VampireLevelChangeTrigger.TriggerInstance createInstance(JsonObject json, @Nonnull ContextAwarePredicate predicate, @Nonnull DeserializationContext deserializationContext)
    {
        VampirismLevelPredicate vampireLevelPredicate = VampirismLevelPredicate.fromJson(json.getAsJsonObject(VampirismLevelPredicate.TIER_KEY));

        return new VampireLevelChangeTrigger.TriggerInstance(predicate, vampireLevelPredicate);
    }

    public void _trigger(ServerPlayer player, VampirismLevel vampirismLevel)
    {
        this.trigger(player, (p_38777_) -> p_38777_.matches(vampirismLevel));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final VampirismLevelPredicate vampirismLevelPredicate;

        public TriggerInstance(ContextAwarePredicate player, VampirismLevelPredicate vampirismLevelPredicate)
        {
            super(VampireLevelChangeTrigger.ID, player);

            this.vampirismLevelPredicate = vampirismLevelPredicate;
        }

        public static VampireLevelChangeTrigger.TriggerInstance levelChanged(VampirismLevel vampirismLevel)
        {
            return new VampireLevelChangeTrigger.TriggerInstance(ContextAwarePredicate.ANY, VampirismLevelPredicate.tier(vampirismLevel));
        }

        public boolean matches(VampirismLevel vampirismLevel)
        {
            return this.vampirismLevelPredicate.matches(vampirismLevel);
        }

        @Nonnull
        @Override
        public JsonObject serializeToJson(@Nonnull SerializationContext serializationContext)
        {
            JsonObject json = super.serializeToJson(serializationContext);

            json.add(VampirismLevelPredicate.TIER_KEY, this.vampirismLevelPredicate.serializeToJson());

            return json;
        }
    }
}
