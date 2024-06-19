package com.unixkitty.vampire_blood.datagen;

import com.unixkitty.vampire_blood.init.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

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
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BLOODLETTING_KNIFE.get())
                .define('i', Tags.Items.INGOTS_IRON)
                .define('s', Items.STICK)
                .pattern(" i")
                .pattern("s ")
                .unlockedBy("has_item", has(Tags.Items.INGOTS_IRON))
                .save(consumer);
    }
}
