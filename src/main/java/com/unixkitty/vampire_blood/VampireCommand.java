package com.unixkitty.vampire_blood;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerBloodData;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.util.VampirismTier;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.command.EnumArgument;

import java.util.function.BiFunction;

public class VampireCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> vampireCommand = Commands.literal("vampire")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(4));

        registerTierCommand("level", vampireCommand, VampirismStage.class, VampireCommand::vampireTier);
        registerTierCommand("blood_type", vampireCommand, BloodType.class, VampireCommand::vampireTier);

        registerCommand("blood", vampireCommand, IntegerArgumentType.integer(0, VampirePlayerBloodData.MAX_THIRST), IntegerArgumentType::getInteger, VampireCommand::setBloodLevel);
        registerCommand("bloodlust", vampireCommand, FloatArgumentType.floatArg(0F, 100F), FloatArgumentType::getFloat, VampireCommand::setBloodlust);
        registerCommand("heal", vampireCommand, FloatArgumentType.floatArg(0), FloatArgumentType::getFloat, VampireCommand::setPlayerHealth);

        dispatcher.register(vampireCommand);
    }

    @SuppressWarnings("unchecked")
    private static <T, E extends Enum<E>> void registerTierCommand(String name, LiteralArgumentBuilder<CommandSourceStack> vampireCommand, Class<E> clazz, TierCommandExecutor<T, E> commandExecutor)
    {
        vampireCommand.then(Commands.literal(name)
                .then(playerArg()
                        .then(Commands.argument("value", EnumArgument.enumArgument(clazz))
                                .executes(context -> commandExecutor.execute(context, getPlayer(context), (T) context.getArgument("value", clazz), clazz)))
                        .executes(context -> commandExecutor.execute(context, getPlayer(context), null, clazz)))
                .then(Commands.literal("list")
                        .executes(context -> VampireCommand.list(context, (Class<? extends VampirismTier>) clazz))));
    }

    private static <T> void registerCommand(String name, LiteralArgumentBuilder<CommandSourceStack> vampireCommand, ArgumentType<T> argumentType, BiFunction<CommandContext<CommandSourceStack>, String, T> argumentGetter, CommandExecutor<T> commandExecutor)
    {
        vampireCommand.then(Commands.literal(name)
                .then(playerArg()
                        .then(Commands.argument("value", argumentType)
                                .executes(context -> commandExecutor.execute(context, getPlayer(context), argumentGetter.apply(context, "value"))))
                        .executes(context -> commandExecutor.execute(context, getPlayer(context), null))));
    }


    //===========================================================================

    private static int setPlayerHealth(CommandContext<CommandSourceStack> context, ServerPlayer player, Float value)
    {
        if (!player.isCreative() && !player.isSpectator())
        {
            player.setHealth(value == null ? player.getMaxHealth() : value);
        }

        return 0;
    }

    private static int setBloodLevel(CommandContext<CommandSourceStack> context, ServerPlayer player, Integer value)
    {
        if (player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() > VampirismStage.IN_TRANSITION.getId())
                {
                    if (value != null)
                    {
                        vampirePlayerData.setBlood(value);
                        vampirePlayerData.syncBlood();
                    }

                    context.getSource().sendSystemMessage(Component.literal(vampirePlayerData.getThirstLevel() + "/" + VampirePlayerBloodData.MAX_THIRST));
                }
                else
                {
                    playerNotVampire(context, player);
                }
            });
        }
        else
        {
            capabilityFail(context, player);
        }

        return 0;
    }

    private static int setBloodlust(CommandContext<CommandSourceStack> context, ServerPlayer player, Float value)
    {
        if (player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() > VampirismStage.IN_TRANSITION.getId())
                {
                    if (value != null)
                    {
                        vampirePlayerData.setBloodlust(value);
                        vampirePlayerData.syncBlood();
                    }

                    context.getSource().sendSystemMessage(Component.literal(vampirePlayerData.getBloodlust() + "/100"));
                }
                else
                {
                    playerNotVampire(context, player);
                }
            });
        }
        else
        {
            capabilityFail(context, player);
        }

        return 0;
    }

    private static int list(CommandContext<CommandSourceStack> context, Class<? extends VampirismTier> clazz)
    {
        for (var type : clazz.getEnumConstants())
        {
            context.getSource().sendSystemMessage(
                    Component.literal(type.getId() + "     ").append(type.getTranslation())
            );
        }

        return 0;
    }

    private static <T> int vampireTier(CommandContext<CommandSourceStack> context, ServerPlayer player, T value, Class<? extends VampirismTier> clazz)
    {
        if (player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (value == null)
                {
                    Component message = null;

                    if (clazz.equals(BloodType.class))
                    {
                        message = vampirePlayerData.getBloodType().getTranslation();
                    }
                    else if (clazz.equals(VampirismStage.class))
                    {
                        message = vampirePlayerData.getVampireLevel().getTranslation();
                    }

                    if (message == null)
                    {
                        context.getSource().sendFailure(Component.literal("Unknown super class type as parameter passed to " + VampireCommand.class.getSimpleName() + ".playerTier()"));
                    }
                    else
                    {
                        context.getSource().sendSystemMessage(message);
                    }
                }
                else
                {
                    if (value instanceof BloodType)
                    {
                        if (vampirePlayerData.getVampireLevel().getId() > VampirismStage.IN_TRANSITION.getId())
                        {
                            vampirePlayerData.setBloodType(player, (BloodType) value);

                            context.getSource().sendSuccess(
                                    Component.translatable(
                                            "commands.vampire_blood.blood_change",
                                            Component.literal(String.valueOf(value)).withStyle(ChatFormatting.BOLD),
                                            player.getDisplayName()
                                    ),
                                    true
                            );
                        }
                        else
                        {
                            playerNotVampire(context, player);
                        }
                    }
                    else if (value instanceof VampirismStage)
                    {
                        if (vampirePlayerData.getVampireLevel() != value)
                        {
                            vampirePlayerData.updateLevel(player, (VampirismStage) value);
                        }

                        context.getSource().sendSuccess(
                                Component.translatable(
                                        "commands.vampire_blood.level_change",
                                        Component.literal(String.valueOf(value)).withStyle(ChatFormatting.BOLD),
                                        player.getDisplayName()
                                ),
                                true
                        );
                    }
                }
            });
        }
        else
        {
            capabilityFail(context, player);
        }

        return 0;
    }

    private static void playerNotVampire(CommandContext<CommandSourceStack> context, ServerPlayer player)
    {
        context.getSource().sendFailure(Component.translatable("commands.vampire_blood.player_not_vampire", player.getDisplayName()));
    }

    private static void capabilityFail(CommandContext<CommandSourceStack> context, ServerPlayer player)
    {
        context.getSource().sendFailure(Component.translatable("commands.vampire_blood.player_has_no_cap", player.getDisplayName()));
    }

    private static ServerPlayer getPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        return EntityArgument.getPlayer(context, "player");
    }

    private static RequiredArgumentBuilder<CommandSourceStack, EntitySelector> playerArg()
    {
        return Commands.argument("player", EntityArgument.player());
    }

    @FunctionalInterface
    private interface CommandExecutor<T>
    {
        int execute(CommandContext<CommandSourceStack> context, ServerPlayer player, T value) throws CommandSyntaxException;
    }

    @FunctionalInterface
    private interface TierCommandExecutor<T, E>
    {
        int execute(CommandContext<CommandSourceStack> context, ServerPlayer player, T value, Class<E> clazz);
    }
}
