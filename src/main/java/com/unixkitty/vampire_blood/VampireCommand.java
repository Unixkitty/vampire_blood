package com.unixkitty.vampire_blood;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.unixkitty.vampire_blood.capability.VampireBloodType;
import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.VampirePlayerProvider;
import com.unixkitty.vampire_blood.capability.VampirismStage;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class VampireCommand
{
    private static final DynamicCommandExceptionType error_no_such_vampire_level = new DynamicCommandExceptionType(o -> Component.translatable("commands.vampire_blood.no_such_level", o));
    private static final DynamicCommandExceptionType error_no_such_blood_type = new DynamicCommandExceptionType(o -> Component.translatable("commands.vampire_blood.no_such_blood", o));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> vampireCommand = Commands.literal("vampire")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(4));

        registerLevelCommand(vampireCommand);
        registerBloodTypeCommand(vampireCommand);
        registerBloodCommand(vampireCommand);
        registerHealCommand(vampireCommand);
        registerSetHealthCommand(vampireCommand);

        dispatcher.register(vampireCommand);
    }

    private static void registerLevelCommand(LiteralArgumentBuilder<CommandSourceStack> vampireCommand)
    {
        vampireCommand.then(Commands.literal("level")
                .then(playerArg()
                        .then(Commands.argument("level", IntegerArgumentType.integer())
                                .executes(context -> setPlayerVampirismLevel(context, getPlayer(context), IntegerArgumentType.getInteger(context, "level"))))
                        .executes(context -> getPlayerVampirismLevel(context, getPlayer(context))))
                .then(Commands.literal("list")
                        .executes(VampireCommand::listLevels)));
    }

    private static void registerBloodTypeCommand(LiteralArgumentBuilder<CommandSourceStack> vampireCommand)
    {
        vampireCommand.then(Commands.literal("blood_type")
                .then(playerArg()
                        .then(Commands.argument("type", IntegerArgumentType.integer())
                                .executes(context -> setPlayerBloodType(context, getPlayer(context), IntegerArgumentType.getInteger(context, "type"))))
                        .executes(context -> getPlayerBloodType(context, getPlayer(context))))
                .then(Commands.literal("list")
                        .executes(VampireCommand::listBloodTypes)));
    }

    private static void registerBloodCommand(LiteralArgumentBuilder<CommandSourceStack> vampireCommand)
    {
        vampireCommand.then(Commands.literal("blood")
                .then(Commands.literal("get")
                        .then(playerArg()
                                .executes(context -> getBloodLevel(context, getPlayer(context)))))
                .then(Commands.literal("set")
                        .then(playerArg()
                                .then(Commands.argument("bloodPoints", IntegerArgumentType.integer(VampirePlayerData.Blood.MIN_THIRST, VampirePlayerData.Blood.MAX_THIRST))
                                        .executes(context -> setBloodLevel(context, getPlayer(context), IntegerArgumentType.getInteger(context, "bloodPoints")))))));
    }

    private static void registerHealCommand(LiteralArgumentBuilder<CommandSourceStack> vampireCommand)
    {
        vampireCommand.then(Commands.literal("heal")
                .then(playerArg()
                        .executes(context -> setPlayerHealth(getPlayer(context), -1))));
    }

    private static void registerSetHealthCommand(LiteralArgumentBuilder<CommandSourceStack> vampireCommand)
    {
        vampireCommand.then(Commands.literal("set_health")
                .then(playerArg()
                        .then(Commands.argument("health", FloatArgumentType.floatArg(0))
                                .executes(context -> setPlayerHealth(getPlayer(context), FloatArgumentType.getFloat(context, "health"))))));
    }

    //===========================================================================

    private static int setPlayerHealth(ServerPlayer player, float health)
    {
        if (!player.isCreative() && !player.isSpectator())
        {
            player.setHealth(health < 0 ? player.getMaxHealth() : health);
        }

        return 0;
    }

    private static int setBloodLevel(CommandContext<CommandSourceStack> context, ServerPlayer player, int bloodPoints)
    {
        if (player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() > VampirismStage.IN_TRANSITION.getId())
                {
                    vampirePlayerData.setBlood(bloodPoints);
                    vampirePlayerData.syncBlood();
                    context.getSource().sendSystemMessage(Component.literal(vampirePlayerData.getThirstLevel() + "/" + VampirePlayerData.Blood.MAX_THIRST));
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

    private static int getBloodLevel(CommandContext<CommandSourceStack> context, ServerPlayer player)
    {
        if (player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() > VampirismStage.IN_TRANSITION.getId())
                {
                    context.getSource().sendSystemMessage(Component.literal(vampirePlayerData.getThirstLevel() + "/" + VampirePlayerData.Blood.MAX_THIRST));
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

    private static int listBloodTypes(CommandContext<CommandSourceStack> context)
    {
        for (VampireBloodType type : VampireBloodType.values())
        {
            context.getSource().sendSystemMessage(
                    Component.literal(type.ordinal() + "     ").append(Component.translatable("vampire_blood.blood_type." + type.toString().toLowerCase()))
            );
        }

        return 0;
    }

    private static int setPlayerBloodType(CommandContext<CommandSourceStack> context, ServerPlayer player, int type) throws CommandSyntaxException
    {
        if (player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
        {
            if (VampireBloodType.fromId(type) == null) throw error_no_such_blood_type.create(type);

            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getBloodType().ordinal() != type)
                {
                    vampirePlayerData.setBloodType(player, type);
                }
            });

            context.getSource().sendSuccess(
                    Component.translatable(
                            "commands.vampire_blood.level_change",
                            Component.literal(String.valueOf(type)).withStyle(ChatFormatting.BOLD),
                            player.getDisplayName()
                    ),
                    true
            );
        }
        else
        {
            capabilityFail(context, player);
        }

        return 0;
    }

    private static int listLevels(CommandContext<CommandSourceStack> context)
    {
        for (VampirismStage level : VampirismStage.values())
        {
            context.getSource().sendSystemMessage(
                    Component.literal(level.getId() + "     ").append(Component.translatable("vampire_blood.vampire_level." + level.toString().toLowerCase()))
            );
        }

        return 0;
    }

    private static int setPlayerVampirismLevel(CommandContext<CommandSourceStack> context, ServerPlayer player, int level) throws CommandSyntaxException
    {
        if (player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
        {
            if (VampirismStage.fromId(level) == null) throw error_no_such_vampire_level.create(level);

            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() != level)
                {
                    vampirePlayerData.setVampireLevel(player, level);
                }
            });

            context.getSource().sendSuccess(
                    Component.translatable(
                            "commands.vampire_blood.level_change",
                            Component.literal(String.valueOf(level)).withStyle(ChatFormatting.BOLD),
                            player.getDisplayName()
                    ),
                    true
            );
        }
        else
        {
            capabilityFail(context, player);
        }

        return 0;
    }

    private static int getPlayerBloodType(CommandContext<CommandSourceStack> context, ServerPlayer player)
    {
        if (player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData -> context.getSource().sendSystemMessage(Component.translatable("vampire_blood.blood_type." + vampirePlayerData.getBloodType().toString().toLowerCase())));
        }
        else
        {
            capabilityFail(context, player);
        }

        return 0;
    }

    private static int getPlayerVampirismLevel(CommandContext<CommandSourceStack> context, ServerPlayer player)
    {
        if (player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData -> context.getSource().sendSystemMessage(Component.translatable("vampire_blood.vampire_level." + vampirePlayerData.getVampireLevel().toString().toLowerCase())));
        }
        else
        {
            capabilityFail(context, player);
        }

        return 0;
    }

    private static void playerNotVampire(CommandContext<CommandSourceStack> context, ServerPlayer player)
    {
        context.getSource().sendSystemMessage(Component.translatable("commands.vampire_blood.player_not_vampire", player.getDisplayName()));
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
}
