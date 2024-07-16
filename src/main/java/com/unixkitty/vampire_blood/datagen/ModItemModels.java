package com.unixkitty.vampire_blood.datagen;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.init.ModItems;
import com.unixkitty.vampire_blood.item.BloodBottleItem;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class ModItemModels extends ItemModelProvider
{
    public ModItemModels(PackOutput output, ExistingFileHelper existingFileHelper)
    {
        super(output, VampireBlood.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels()
    {
        basicItem(ModItems.VAMPIRE_DUST.get());

        tool(ModItems.BLOODLETTING_KNIFE.get());

        for (BloodType bloodType : BloodType.values())
        {
            if (bloodType != BloodType.NONE)
            {
                basicItem(BloodBottleItem.getItem(bloodType));
                basicItem(ModItems.getBloodBucketItem(bloodType));
            }
        }

        modIconTextures();
    }

    private void tool(Item item)
    {
        ResourceLocation resourceLocation = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item));

        getBuilder(resourceLocation.toString())
                .parent(model("minecraft", "handheld"))
                .texture("layer0", new ResourceLocation(resourceLocation.getNamespace(), "item/" + resourceLocation.getPath()));
    }

    private ModelFile.UncheckedModelFile generatedModel()
    {
        return model("minecraft", "generated");
    }

    private ModelFile.UncheckedModelFile model(String namespace, String path)
    {
        return new ModelFile.UncheckedModelFile(new ResourceLocation(namespace, "item/" + path));
    }

    //Advancement stuff
    private void modIconTextures()
    {
        ResourceLocation resourceLocation = Objects.requireNonNull(ModItems.MOD_ICON.getKey()).location();
        float[] id = new float[]{0F};

        ItemModelBuilder iconModelsBuilder = getBuilder(resourceLocation.toString())
                .parent(generatedModel());

        addModelOverride(iconModelsBuilder, id, "mod_icon", "custom/mod_icon");
        addModelOverride(iconModelsBuilder, id, "in_transition_icon", "mob_effect/transitioning");
        addModelOverride(iconModelsBuilder, id, "night_vision_icon", "mob_effect/night_vision");
        addModelOverride(iconModelsBuilder, id, "senses_icon", "mob_effect/enhanced_speed");
        addModelOverride(iconModelsBuilder, id, "blood_vision_icon", "mob_effect/enhanced_senses");
        addModelOverride(iconModelsBuilder, id, "speed_icon", "mob_effect/blood_vision");
        addModelOverride(iconModelsBuilder, id, "charm_icon", "gui/charm");
        addModelOverride(iconModelsBuilder, id, "vampire_blood_icon", "mob_effect/vampire_blood");
    }

    private void addModelOverride(ItemModelBuilder iconModelsBuilder, float[] id, String name, String texturePath)
    {
        ResourceLocation resourceLocation = new ResourceLocation(VampireBlood.MODID, name);

        getBuilder(resourceLocation.toString())
                .parent(generatedModel())
                .texture("layer0", new ResourceLocation(name.endsWith("night_vision_icon") ? "minecraft" : resourceLocation.getNamespace(), texturePath));

        iconModelsBuilder.override()
                .model(model(VampireBlood.MODID, name))
                .predicate(new ResourceLocation("custom_model_data"), id[0]++)
                .end();
    }
}
