package com.unixkitty.vampire_blood;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.VampirePlayerProvider;
import com.unixkitty.vampire_blood.capability.VampirismStage;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.PlayerVampireDataS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.atomic.AtomicBoolean;

public class VampireCommand
{
    private static final DynamicCommandExceptionType error_no_such_vampire_level = new DynamicCommandExceptionType(o -> Component.translatable("commands.vampire_blood.no_such_level", o));

    /*
        /vampire <level> <player> < true | false >
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("vampire").requires(commandSourceStack -> commandSourceStack.hasPermission(4)).then(
                        Commands.literal("level").then(
                                playerArg().then(
                                        Commands.argument("level", IntegerArgumentType.integer()).executes(
                                                context -> setPlayerVampirismLevel(context, getPlayer(context), IntegerArgumentType.getInteger(context, "level"))
                                        )
                                )
                        ).then(
                                Commands.literal("list").executes(
                                        VampireCommand::listLevels
                                )
                        )
                ).then(
                        Commands.literal("blood").then(
                                Commands.literal("get").then(
                                        playerArg().executes(
                                                context -> getBloodLevel(context, getPlayer(context))
                                        )
                                )
                        ).then(
                                Commands.literal("set").then(
                                        playerArg().then(
                                                Commands.argument("bloodPoints", IntegerArgumentType.integer(VampirePlayerData.Blood.MIN_THIRST, VampirePlayerData.Blood.MAX_THIRST)).executes(
                                                        context -> setBloodLevel(context, getPlayer(context), IntegerArgumentType.getInteger(context, "bloodPoints"))
                                                )
                                        )
                                )
                        )
                ).then(
                        Commands.literal("heal").then(
                                playerArg().executes(
                                        context -> setPlayerHealth(getPlayer(context), -1)
                                )
                        )
                ).then(
                        Commands.literal("set_health").then(
                                playerArg().then(
                                        Commands.argument("health", FloatArgumentType.floatArg(0)).executes(
                                                context -> setPlayerHealth(getPlayer(context), FloatArgumentType.getFloat(context, "health"))
                                        )
                                )
                        )
                )
        );
    }

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
        AtomicBoolean isVampire = new AtomicBoolean(false);

        if (player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() > VampirismStage.IN_TRANSITION.getId())
                {
                    isVampire.set(true);

                    vampirePlayerData.setBlood(bloodPoints);
                    vampirePlayerData.syncBlood();
                    context.getSource().sendSystemMessage(Component.literal(vampirePlayerData.getThirstLevel() + "/" + VampirePlayerData.Blood.MAX_THIRST));
                }
            });
        }

        if (!isVampire.get())
        {
            context.getSource().sendSystemMessage(Component.translatable("commands.vampire_blood.player_not_vampire", player.getDisplayName()));
        }

        return 0;
    }

    private static int getBloodLevel(CommandContext<CommandSourceStack> context, ServerPlayer player)
    {
        AtomicBoolean isVampire = new AtomicBoolean(false);

        if (player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() > VampirismStage.IN_TRANSITION.getId())
                {
                    isVampire.set(true);

                    context.getSource().sendSystemMessage(Component.literal(vampirePlayerData.getThirstLevel() + "/" + VampirePlayerData.Blood.MAX_THIRST));
                }
            });
        }

        if (!isVampire.get())
        {
            context.getSource().sendSystemMessage(Component.translatable("commands.vampire_blood.player_not_vampire", player.getDisplayName()));
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
                    vampirePlayerData.setVampireLevel(level);

                    ModNetworkDispatcher.sendToClient(new PlayerVampireDataS2CPacket(vampirePlayerData.getVampireLevel().getId(), vampirePlayerData.getBloodType().ordinal(), vampirePlayerData.isFeeding()), player);
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

        return 0;
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
