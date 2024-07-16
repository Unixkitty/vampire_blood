package com.unixkitty.vampire_blood.advancement.trigger;

import com.google.gson.JsonObject;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.advancement.predicate.SelfPredicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;

public class BloodlettingTrigger extends SimpleCriterionTrigger<BloodlettingTrigger.TriggerInstance>
{
    private static BloodlettingTrigger instance;

    static final ResourceLocation ID = new ResourceLocation(VampireBlood.MODID, "bloodletting");

    public static void register()
    {
        if (instance == null)
        {
            instance = CriteriaTriggers.register(new BloodlettingTrigger());
        }
    }

    public static void trigger(ServerPlayer player, ItemStack itemStack, boolean fromSelf)
    {
        if (instance == null)
        {
            throw new IllegalStateException(BloodlettingTrigger.class.getSimpleName() + " called before it was registered!");
        }
        else
        {
            instance._trigger(player, itemStack, fromSelf);
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
    public BloodlettingTrigger.TriggerInstance createInstance(JsonObject json, @Nonnull ContextAwarePredicate predicate, @Nonnull DeserializationContext deserializationContext)
    {
        ItemPredicate itemPredicate = ItemPredicate.fromJson(json.get("item"));
        SelfPredicate selfPredicate = SelfPredicate.fromJson(json.getAsJsonObject("self"));

        return new BloodlettingTrigger.TriggerInstance(predicate, itemPredicate, selfPredicate);
    }

    public void _trigger(ServerPlayer player, ItemStack itemStack, boolean fromSelf)
    {
        this.trigger(player, (p_38777_) -> p_38777_.matches(itemStack, fromSelf));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final ItemPredicate item;
        private final SelfPredicate self;

        public TriggerInstance(ContextAwarePredicate player, ItemPredicate itemPredicate, SelfPredicate self)
        {
            super(BloodlettingTrigger.ID, player);

            this.item = itemPredicate;
            this.self = self;
        }

        public static BloodlettingTrigger.TriggerInstance bloodExtracted(ItemLike itemLike, boolean fromSelf)
        {
            return new BloodlettingTrigger.TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.Builder.item().of(itemLike).build(), SelfPredicate.self(fromSelf));
        }

        public boolean matches(ItemStack itemStack, boolean fromSelf)
        {
            return this.item.matches(itemStack) && this.self.matches(fromSelf);
        }

        @Nonnull
        @Override
        public JsonObject serializeToJson(@Nonnull SerializationContext serializationContext)
        {
            JsonObject json = super.serializeToJson(serializationContext);

            json.add("item", this.item.serializeToJson());
            json.add("self", this.self.serializeToJson());

            return json;
        }
    }
}
