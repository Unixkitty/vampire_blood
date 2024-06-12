package com.unixkitty.vampire_blood.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.Collection;

public class EntitySummonArgument implements ArgumentType<ResourceLocation>
{
    private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:pig", "cow");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_ENTITY = new DynamicCommandExceptionType((o) -> Component.translatable("commands.vampire_blood.entity.notFound", String.valueOf(o)));

    public static EntitySummonArgument id()
    {
        return new EntitySummonArgument();
    }

    public static ResourceLocation getSummonableEntity(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException
    {
        return verifyCanSummon(pContext.getArgument(pName, ResourceLocation.class));
    }

    private static ResourceLocation verifyCanSummon(ResourceLocation resourceLocation) throws CommandSyntaxException
    {
        ForgeRegistries.ENTITY_TYPES.getDelegate(resourceLocation).filter(entityTypeReference -> entityTypeReference.get().canSummon()).orElseThrow(() -> ERROR_UNKNOWN_ENTITY.create(resourceLocation));

        return resourceLocation;
    }

    public ResourceLocation parse(StringReader reader) throws CommandSyntaxException
    {
        return verifyCanSummon(ResourceLocation.read(reader));
    }

    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }
}
