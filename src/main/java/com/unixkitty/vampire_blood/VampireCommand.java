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
import com.unixkitty.vampire_blood.util.VampireUtil;
import com.unixkitty.vampire_blood.util.VampirismTier;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.command.EnumArgument;

public class VampireCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> vampireCommand = Commands.literal("vampire")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(4));

        registerTierCommand(VampirismStage.class, vampireCommand);
        registerTierCommand(BloodType.class, vampireCommand);

        registerCommand(Value.BLOOD, vampireCommand, IntegerArgumentType.integer(0, VampirePlayerBloodData.MAX_THIRST));
        registerCommand(Value.BLOODLUST, vampireCommand, FloatArgumentType.floatArg(0F, 100F));
        registerCommand(Value.HEAL, vampireCommand, FloatArgumentType.floatArg(0));

        dispatcher.register(vampireCommand);
    }

    private static <E extends Enum<E> & VampirismTier<E>> void registerTierCommand(Class<E> tierClass, LiteralArgumentBuilder<CommandSourceStack> vampireCommand)
    {
        vampireCommand.then(command(tierClass)
                .then(playerArg()
                        .then(Commands.argument("value", enumArgument(tierClass))
                                .executes(context -> tierOperation(context, getPlayer(context), context.getArgument("value", tierClass), tierClass)))
                        .executes(context -> tierOperation(context, getPlayer(context), null, tierClass)))
                .then(Commands.literal("list")
                        .executes(context -> VampireCommand.list(context, tierClass))));
    }

    private static <T> void registerCommand(Value valueType, LiteralArgumentBuilder<CommandSourceStack> vampireCommand, ArgumentType<T> argumentType)
    {
        vampireCommand.then(valueType.getCommand()
                .then(playerArg()
                        .then(Commands.argument("value", argumentType)
                                .executes(context -> valueOperation(context, getPlayer(context), valueType.getValue(context), valueType)))
                        .executes(context -> valueOperation(context, getPlayer(context), null, valueType))));
    }


    //===========================================================================

    private static <T extends Number> int valueOperation(CommandContext<CommandSourceStack> context, ServerPlayer player, T value, Value type)
    {
        try
        {
            if (type == Value.HEAL)
            {
                if (!player.isCreative() && !player.isSpectator())
                {
                    player.setHealth(value == null ? player.getMaxHealth() : (float) value);
                }
            }
            else if (player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
            {
                player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
                {
                    if (vampirePlayerData.getVampireLevel().getId() > VampirismStage.IN_TRANSITION.getId())
                    {
                        String message = "";

                        if (value != null)
                        {
                            if (type == Value.BLOOD)
                            {
                                vampirePlayerData.setBlood((int) value);
                                vampirePlayerData.sync();

                                message = vampirePlayerData.getThirstLevel() + "/" + VampirePlayerBloodData.MAX_THIRST;
                            }
                            else if (type == Value.BLOODLUST)
                            {
                                vampirePlayerData.setBloodlust((float) value);
                                vampirePlayerData.sync();

                                message = vampirePlayerData.getBloodlust() + "/100";
                            }

                            if (message.isEmpty())
                            {
                                context.getSource().sendFailure(Component.literal("Unknown value type in command"));
                            }
                            else
                            {
                                context.getSource().sendSystemMessage(Component.literal(message));
                            }
                        }
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
        }
        catch (Exception e)
        {
            VampireBlood.log().error(e);
        }

        return 0;
    }

    private static <T> int tierOperation(CommandContext<CommandSourceStack> context, ServerPlayer player, T value, Class<? extends VampirismTier<?>> clazz)
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
                    if (value instanceof BloodType type)
                    {
                        if (vampirePlayerData.getVampireLevel().getId() > VampirismStage.IN_TRANSITION.getId())
                        {
                            if (type == BloodType.NONE)
                            {
                                context.getSource().sendFailure(Component.literal(VampirismTier.getName(clazz) + ": " + type.name()));

                                return;
                            }

                            vampirePlayerData.setBloodType(player, type);

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
                    else if (value instanceof VampirismStage stage)
                    {
                        if (vampirePlayerData.getVampireLevel() != stage)
                        {
                            vampirePlayerData.updateLevel(player, stage);
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

    private static int list(CommandContext<CommandSourceStack> context, Class<? extends VampirismTier<?>> clazz)
    {
        for (var type : clazz.getEnumConstants())
        {
            context.getSource().sendSystemMessage(
                    Component.literal(type.getId() + "     ").append(type.getTranslation())
            );
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

    private static LiteralArgumentBuilder<CommandSourceStack> command(Class<? extends VampirismTier<?>> tierClass)
    {
        return Commands.literal(VampirismTier.getName(tierClass));
    }

    public static <E extends Enum<E> & VampirismTier<E>> EnumArgument<E> enumArgument(Class<E> clazz)
    {
        return EnumArgument.enumArgument(clazz);
    }

    private enum Value
    {
        HEAL,
        BLOOD,
        BLOODLUST;

        private Number getValue(CommandContext<CommandSourceStack> context)
        {
            if (this == BLOOD)
            {
                return IntegerArgumentType.getInteger(context, "value");
            }
            else
            {
                return FloatArgumentType.getFloat(context, "value");
            }
        }

        LiteralArgumentBuilder<CommandSourceStack> getCommand()
        {
            return Commands.literal(VampireUtil.getEnumName(this));
        }
    }
}
