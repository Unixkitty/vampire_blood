package com.unixkitty.vampire_blood.datagen;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.init.ModDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class DamageTypeTagProvider extends DamageTypeTagsProvider
{
    public DamageTypeTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvier, @Nullable ExistingFileHelper existingFileHelper)
    {
        super(packOutput, lookupProvier, VampireBlood.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider provider)
    {
        tag(DamageTypeTags.BYPASSES_ARMOR).add(
                ModDamageTypes.SUN_DAMAGE,
                ModDamageTypes.BLOOD_LOSS
        );

        tag(DamageTypeTags.BYPASSES_ENCHANTMENTS).add(
                ModDamageTypes.SUN_DAMAGE,
                ModDamageTypes.BLOOD_LOSS
        );

        tag(DamageTypeTags.BYPASSES_RESISTANCE).add(
                ModDamageTypes.SUN_DAMAGE,
                ModDamageTypes.BLOOD_LOSS
        );
    }
}
