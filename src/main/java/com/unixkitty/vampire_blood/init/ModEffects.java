package com.unixkitty.vampire_blood.init;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.effect.BasicStatusEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("DataFlowIssue")
public class ModEffects
{
    public static final float SENSES_DEFAULT_DISTANCE = 30F;

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, VampireBlood.MODID);

    public static final RegistryObject<MobEffect> BLOOD_VISION = EFFECTS.register("blood_vision", () -> new BasicStatusEffect(ChatFormatting.DARK_RED.getColor()));
    public static final RegistryObject<MobEffect> ENHANCED_SENSES = EFFECTS.register("enhanced_senses", BasicStatusEffect::new);
    public static final RegistryObject<MobEffect> ENHANCED_SPEED = EFFECTS.register("enhanced_speed", BasicStatusEffect::new);
    public static final RegistryObject<MobEffect> VAMPIRE_BLOOD = EFFECTS.register("vampire_blood", () -> new BasicStatusEffect(ChatFormatting.DARK_PURPLE.getColor()));
    public static final RegistryObject<MobEffect> VAMPIRE_IN_TRANSITION = EFFECTS.register("transitioning", () -> new BasicStatusEffect(ChatFormatting.DARK_PURPLE.getColor()));
}
