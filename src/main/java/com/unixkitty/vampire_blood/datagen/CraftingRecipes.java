package com.unixkitty.vampire_blood.datagen;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.init.ModItems;
import com.unixkitty.vampire_blood.item.BloodBottleItem;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public class CraftingRecipes extends RecipeProvider
{
    public CraftingRecipes(PackOutput output)
    {
        super(output);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer)
    {
        for (BloodType bloodType : BloodType.values())
        {
            if (bloodType != BloodType.NONE)
            {
                Item bloodBottleItem = BloodBottleItem.getItem(bloodType);
                Item bloodBucketItem = ModItems.getBloodBucketItem(bloodType);

                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, bloodBucketItem)
                        .requires(bloodBottleItem, 4)
                        .requires(Items.BUCKET)
                        .group("blood_buckets")
                        .unlockedBy("has_" + Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(bloodBottleItem)).getPath(), has(bloodBottleItem))
                        .save(consumer);

                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, bloodBottleItem, 4)
                        .requires(bloodBucketItem)
                        .requires(Items.GLASS_BOTTLE, 4)
                        .group("blood_bottles")
                        .unlockedBy("has_" + Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(bloodBottleItem)).getPath(), has(bloodBottleItem))
                        .save(consumer);
            }
        }

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BLOODLETTING_KNIFE.get())
                .define('i', Tags.Items.INGOTS_IRON)
                .define('s', Items.STICK)
                .pattern(" i")
                .pattern("s ")
                .unlockedBy("has_item", has(Tags.Items.INGOTS_IRON))
                .save(consumer);
    }
}
