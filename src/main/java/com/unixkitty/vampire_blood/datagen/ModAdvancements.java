package com.unixkitty.vampire_blood.datagen;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.advancement.trigger.BloodlettingTrigger;
import com.unixkitty.vampire_blood.advancement.trigger.DrinkBloodTrigger;
import com.unixkitty.vampire_blood.advancement.trigger.VampireAbilityUseTrigger;
import com.unixkitty.vampire_blood.advancement.trigger.VampireLevelChangeTrigger;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.init.ModEffects;
import com.unixkitty.vampire_blood.init.ModFluids;
import com.unixkitty.vampire_blood.init.ModItems;
import com.unixkitty.vampire_blood.init.ModRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancements extends ForgeAdvancementProvider
{
    public ModAdvancements(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper)
    {
        super(output, registries, existingFileHelper, List.of(new ModStoryAdvancements()));
    }

    public static class ModStoryAdvancements implements AdvancementGenerator
    {
        @Override
        public void generate(@Nonnull HolderLookup.Provider provider, @Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper existingFileHelper)
        {
            Advancement root = Advancement.Builder.advancement()
                    .display(
                            ModItems.MOD_ICON.get(),
                            Component.translatable("text.vampire_blood.advancements.root"),
                            Component.translatable("text.vampire_blood.advancements.root.desc"),
                            new ResourceLocation("textures/block/black_concrete.png"),
                            FrameType.TASK,
                            false,
                            false,
                            false
                    )
                    .addCriterion("unlocked_by_default", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{}))
                    .save(consumer, saveString("root"));

            addFun(root, consumer);

            Advancement vampire_blood_bottle = Advancement.Builder.advancement()
                    .parent(root)
                    .display(
                            ModItems.VAMPIRE_BLOOD_BOTTLE.get(),
                            Component.translatable("text.vampire_blood.advancements.vampire_blood"),
                            Component.translatable("text.vampire_blood.advancements.vampire_blood.desc"),
                            null,
                            FrameType.TASK,
                            true,
                            true,
                            true
                    )
                    .addCriterion("vampire_blood_bottle", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.VAMPIRE_BLOOD_BOTTLE.get()))
                    .save(consumer, saveString("vampire_blood_bottle"));
            Advancement vampire_blood_consumed = Advancement.Builder.advancement()
                    .parent(vampire_blood_bottle)
                    .display(
                            Items.SKELETON_SKULL,
                            Component.translatable("text.vampire_blood.advancements.vampire_blood_consumed"),
                            Component.translatable("text.vampire_blood.advancements.vampire_blood_consumed.desc"),
                            null,
                            FrameType.TASK,
                            true,
                            true,
                            true
                    )
                    .addCriterion("vampire_blood_consumed", EffectsChangedTrigger.TriggerInstance.hasEffects(MobEffectsPredicate.effects().and(ModEffects.VAMPIRE_BLOOD.get())))
                    .save(consumer, saveString("vampire_blood_consumed"));
            Advancement in_transition = Advancement.Builder.advancement()
                    .parent(vampire_blood_consumed)
                    .display(
                            iconFromData(1),
                            Component.translatable("text.vampire_blood.advancements.in_transition"),
                            Component.translatable("text.vampire_blood.advancements.in_transition.desc"),
                            null,
                            FrameType.TASK,
                            true,
                            true,
                            true
                    )
                    .addCriterion("in_transition", VampireLevelChangeTrigger.TriggerInstance.levelChanged(VampirismLevel.IN_TRANSITION))
                    .save(consumer, saveString("in_transition"));
            Advancement.Builder.advancement()
                    .parent(in_transition)
                    .display(
                            Items.PLAYER_HEAD,
                            Component.translatable("text.vampire_blood.advancements.transition_fail"),
                            Component.translatable("text.vampire_blood.advancements.transition_fail.desc"),
                            null,
                            FrameType.TASK,
                            true,
                            true,
                            true
                    )
                    .addCriterion("transition_fail", VampireLevelChangeTrigger.TriggerInstance.levelChanged(VampirismLevel.NOT_VAMPIRE))
                    .save(consumer, saveString("transition_fail"));
            Advancement vampire = Advancement.Builder.advancement()
                    .parent(in_transition)
                    .display(
                            iconFromData(7),
                            Component.translatable("text.vampire_blood.advancements.vampire"),
                            Component.translatable("text.vampire_blood.advancements.vampire.desc"),
                            null,
                            FrameType.GOAL,
                            true,
                            true,
                            true
                    )
                    .addCriterion("vampire", VampireLevelChangeTrigger.TriggerInstance.levelChanged(VampirismLevel.FLEDGLING))
                    .save(consumer, saveString("vampire"));
            Advancement no_fledgling = Advancement.Builder.advancement()
                    .parent(vampire)
                    .display(
                            iconFromData(7),
                            Component.translatable("text.vampire_blood.advancements.no_fledgling"),
                            Component.translatable("text.vampire_blood.advancements.no_fledgling.desc"),
                            null,
                            FrameType.GOAL,
                            true,
                            true,
                            true
                    )
                    .addCriterion("no_fledgling", VampireLevelChangeTrigger.TriggerInstance.levelChanged(VampirismLevel.VAMPIRE))
                    .rewards(AdvancementRewards.Builder.experience(500))
                    .save(consumer, saveString("no_fledgling"));
            Advancement.Builder.advancement()
                    .parent(no_fledgling)
                    .display(
                            iconFromData(7),
                            Component.translatable("text.vampire_blood.advancements.mature"),
                            Component.translatable("text.vampire_blood.advancements.mature.desc"),
                            null,
                            FrameType.CHALLENGE,
                            true,
                            true,
                            true
                    )
                    .addCriterion("mature", VampireLevelChangeTrigger.TriggerInstance.levelChanged(VampirismLevel.MATURE))
                    .rewards(AdvancementRewards.Builder.experience(1000))
                    .save(consumer, saveString("mature"));

            addVampireFun(vampire, consumer);
            addAbilities(vampire, consumer);
        }

        private void addAbilities(Advancement root, Consumer<Advancement> consumer)
        {
            for (VampireActiveAbility ability : VampireActiveAbility.values())
            {
                String name = "ability_" + ability.getSimpleName();

                Advancement.Builder.advancement()
                        .parent(root)
                        .display(
                                iconFromData(ability.ordinal() + 2),
                                Component.translatable("text.vampire_blood.advancements." + name),
                                Component.translatable("text.vampire_blood.advancements." + name + ".desc"),
                                null,
                                FrameType.GOAL,
                                true,
                                false,
                                false
                        )
                        .addCriterion(name, VampireAbilityUseTrigger.TriggerInstance.abilityUsed(ability))
                        .rewards(AdvancementRewards.Builder.experience(50))
                        .save(consumer, saveString(name));
            }

            Advancement.Builder.advancement()
                    .parent(root)
                    .display(
                            iconFromData(6),
                            Component.translatable("text.vampire_blood.advancements.charm"),
                            Component.translatable("text.vampire_blood.advancements.charm.desc"),
                            null,
                            FrameType.GOAL,
                            true,
                            true,
                            false
                    )
                    .addCriterion("charm", new PlayerTrigger.TriggerInstance(ModRegistry.CHARMED_ENTITY_TRIGGER.getId(), ContextAwarePredicate.ANY))
                    .rewards(AdvancementRewards.Builder.experience(50))
                    .save(consumer, saveString("charm"));
        }

        private ItemStack iconFromData(float id)
        {
            ItemStack itemStack = ModItems.MOD_ICON.get().getDefaultInstance();

            itemStack.getOrCreateTag().putFloat("CustomModelData", id);

            return itemStack;
        }

        private void addVampireFun(Advancement root, @Nonnull Consumer<Advancement> consumer)
        {
            allBloodTypes(Advancement.Builder.advancement()
                    .parent(root)
                    .display(
                            ModFluids.VAMPIRE_BLOOD.bucket.get(),
                            Component.translatable("text.vampire_blood.advancements.all_blood_types"),
                            Component.translatable("text.vampire_blood.advancements.all_blood_types.desc"),
                            null,
                            FrameType.CHALLENGE,
                            true,
                            true,
                            false
                    )
                    .rewards(AdvancementRewards.Builder.experience(100)))
                    .save(consumer, saveString("all_blood_types"));
        }

        private Advancement.Builder allBloodTypes(Advancement.Builder pBuilder)
        {
            for (BloodType bloodType : BloodType.values())
            {
                if (bloodType == BloodType.NONE) continue;

                pBuilder.addCriterion(bloodType.name().toLowerCase(), DrinkBloodTrigger.TriggerInstance.bloodConsumed(bloodType, 1F));
            }

            return pBuilder;
        }

        private void addFun(Advancement root, @Nonnull Consumer<Advancement> consumer)
        {
            Advancement blood_knife = Advancement.Builder.advancement()
                    .parent(root)
                    .display(
                            ModItems.BLOODLETTING_KNIFE.get(),
                            Component.translatable("text.vampire_blood.advancements.blood_knife"),
                            Component.translatable("text.vampire_blood.advancements.blood_knife.desc"),
                            null,
                            FrameType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("blood_knife", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.BLOODLETTING_KNIFE.get()))
                    .save(consumer, saveString("blood_knife"));
            Advancement.Builder.advancement()
                    .parent(blood_knife)
                    .display(
                            ModItems.BLOODLETTING_KNIFE.get(),
                            Component.translatable("text.vampire_blood.advancements.bloodletting_other"),
                            Component.translatable("text.vampire_blood.advancements.bloodletting_other.desc"),
                            null,
                            FrameType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("bloodletting_other", BloodlettingTrigger.TriggerInstance.bloodExtracted(ModItems.BLOODLETTING_KNIFE.get(), false))
                    .save(consumer, saveString("bloodletting_other"));
            Advancement.Builder.advancement()
                    .parent(blood_knife)
                    .display(
                            ModItems.BLOODLETTING_KNIFE.get(),
                            Component.translatable("text.vampire_blood.advancements.bloodletting_self"),
                            Component.translatable("text.vampire_blood.advancements.bloodletting_self.desc"),
                            null,
                            FrameType.GOAL,
                            true,
                            true,
                            true
                    )
                    .addCriterion("bloodletting_self", BloodlettingTrigger.TriggerInstance.bloodExtracted(ModItems.BLOODLETTING_KNIFE.get(), true))
                    .rewards(AdvancementRewards.Builder.experience(100))
                    .save(consumer, saveString("bloodletting_self"));
        }

        private String saveString(String name)
        {
            return new ResourceLocation(VampireBlood.MODID, "story/" + name).toString();
        }
    }
}
