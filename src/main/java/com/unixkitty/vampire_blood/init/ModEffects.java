package com.unixkitty.vampire_blood.init;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.effect.BasicStatusEffect;
import com.unixkitty.vampire_blood.effect.CharmEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects
{
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, VampireBlood.MODID);

    public static final RegistryObject<MobEffect> BLOOD_VISION = EFFECTS.register("blood_vision", () -> new BasicStatusEffect(ChatFormatting.DARK_RED.getColor()));
    public static final RegistryObject<MobEffect> ENHANCED_SENSES = EFFECTS.register("enhanced_senses", BasicStatusEffect::new);
    public static final RegistryObject<MobEffect> ENHANCED_SPEED = EFFECTS.register("enhanced_speed", BasicStatusEffect::new);
    public static final RegistryObject<MobEffect> CHARMED = EFFECTS.register("charmed", CharmEffect::new);
}
