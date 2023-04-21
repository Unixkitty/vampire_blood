package com.unixkitty.vampire_blood;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.VampirePlayerProvider;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.PlayerVampireDataS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
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
                                Commands.argument("player", EntityArgument.player()).then(
                                        Commands.argument("level", IntegerArgumentType.integer()).executes(
                                                context -> setPlayerVampirismLevel(context, EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "level"))
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
                                        Commands.argument("player", EntityArgument.player()).executes(
                                                context -> getBloodLevel(context, EntityArgument.getPlayer(context, "player"))
                                        )
                                )
                        ).then(
                                Commands.literal("set").then(
                                        Commands.argument("player", EntityArgument.player()).then(
                                                Commands.argument("bloodPoints", IntegerArgumentType.integer(VampirePlayerData.Blood.MIN_THIRST, VampirePlayerData.Blood.MAX_THIRST)).executes(
                                                        context -> setBloodLevel(context, EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "bloodPoints"))
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int setBloodLevel(CommandContext<CommandSourceStack> context, ServerPlayer player, int bloodPoints)
    {
        AtomicBoolean isVampire = new AtomicBoolean(false);

        if (player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() > VampirePlayerData.Stage.IN_TRANSITION.getId())
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
                if (vampirePlayerData.getVampireLevel().getId() > VampirePlayerData.Stage.IN_TRANSITION.getId())
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
        for (VampirePlayerData.Stage level : VampirePlayerData.Stage.values())
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
            if (VampirePlayerData.Stage.fromId(level) == null) throw error_no_such_vampire_level.create(level);

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
}
