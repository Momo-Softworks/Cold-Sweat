package net.momostudios.coldsweat.command.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ComponentArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.IRangeArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.momostudios.coldsweat.command.BaseCommand;
import net.momostudios.coldsweat.temperature.PlayerTempHandler;
import net.momostudios.coldsweat.temperature.Temperature;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TempCommand extends BaseCommand
{
    public TempCommand(String name, int permissionLevel, boolean enabled) {
        super(name, permissionLevel, enabled);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> setExecution()
    {
        return builder
                .then(Commands.literal("rate")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100))
                                .executes(source -> executeSetRate(source.getSource(), IntegerArgumentType.getInteger(source, "amount")))
                        )
                )
                .then(Commands.literal("range")
                        .then(Commands.literal("min")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100))
                                        .executes(source -> executeSetMinRange(source.getSource(), IntegerArgumentType.getInteger(source, "amount")))
                                )
                        )
                        .then(Commands.literal("max")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100))
                                        .executes(source -> executeSetMaxRange(source.getSource(), IntegerArgumentType.getInteger(source, "amount")))
                                )
                        )
                )
                .then(Commands.literal("set")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 150))
                                        .executes(source -> executeSetPlayerTemp(
                                        source.getSource(), EntityArgument.getPlayers(source, "players"), IntegerArgumentType.getInteger(source, "amount")))
                                )
                        )
                );
    }

    private int executeSetRate(CommandSource source, int multiplier) throws CommandSyntaxException
    {


        //Print success message to all players
        for (PlayerEntity player : source.asPlayer().world.getPlayers()) {
            player.sendStatusMessage(new StringTextComponent(
                    "\u00a77\u00a7o[" + source.asPlayer().getScoreboardName() + "]: " +
                    new TranslationTextComponent("commands.cold_sweat.temperature.rate.result").getString() +
                    " \u00a7f" + (multiplier * 100) + "%\u00a7r"), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executeSetMinRange(CommandSource source, int amount) throws CommandSyntaxException
    {
        

        //Print success message to all players
        for (PlayerEntity player : source.asPlayer().world.getPlayers())
        {
            player.sendStatusMessage(new StringTextComponent(
            "\u00a77\u00a7o[" + source.asPlayer().getScoreboardName() + "]: " +
            new TranslationTextComponent("commands.cold_sweat.temperature.range.min.result").getString() +
            " \u00a7f" + amount + "\u00a7r"), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executeSetMaxRange(CommandSource source, int amount) throws CommandSyntaxException
    {


        //Print success message to all players
        for (PlayerEntity player : source.asPlayer().world.getPlayers())
        {
            player.sendStatusMessage(new StringTextComponent(
            "\u00a77\u00a7o[" + source.asPlayer().getScoreboardName() + "]: " +
            new TranslationTextComponent("commands.cold_sweat.temperature.range.max.result").getString() +
            " \u00a7f" + amount + "\u00a7r"), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executeSetPlayerTemp(CommandSource source, Collection<ServerPlayerEntity> players, int amount) throws CommandSyntaxException
    {
        if (players.size() == 1)
        {
            if (players.contains(source.asPlayer()))
            {
                //Set the sender's body temperature
                PlayerTempHandler.setBody(source.asPlayer(), new Temperature(amount));

                //Print success message to all players
                for (PlayerEntity player : source.asPlayer().world.getPlayers())
                {
                    //Determine if the message is being sent to the sender or another player
                    TranslationTextComponent message = player == source.asPlayer() ?
                    new TranslationTextComponent("commands.cold_sweat.temperature.set.self.result") :
                    new TranslationTextComponent("commands.cold_sweat.temperature.set.other.result", source.asPlayer().getScoreboardName());

                    //Compose the message
                    player.sendStatusMessage(new StringTextComponent(
                    "\u00a77\u00a7o[" + source.asPlayer().getScoreboardName() + "]: " +
                    message.getString()  +
                    " \u00a7f" + amount + "\u00a7r"), false);
                }
            }
            else
            {
                //Set the target player's temperature
                PlayerTempHandler.setBody((PlayerEntity) Arrays.asList(players).get(0), new Temperature(amount));

                //Print success message to all players
                for (PlayerEntity player : source.asPlayer().world.getPlayers())
                {
                    //Compose the message
                    player.sendStatusMessage(new StringTextComponent(
                    "\u00a77\u00a7o[" + source.asPlayer().getScoreboardName() + "]: " +
                    new TranslationTextComponent("commands.cold_sweat.temperature.set.other.result", source.asPlayer().getScoreboardName()).getString()  +
                    " \u00a7f" + amount + "\u00a7r"), false);
                }
            }
        }
        else
        {
            int playerCount = 0;
            for (ServerPlayerEntity player : players)
            {
                PlayerTempHandler.setBody(player, new Temperature(amount));
                playerCount++;
            }

            //Print success message to all players
            for (PlayerEntity player : source.asPlayer().world.getPlayers())
            {
                //Compose the message
                player.sendStatusMessage(new StringTextComponent(
                "\u00a77\u00a7o[" + source.asPlayer().getScoreboardName() + "]: " +
                new TranslationTextComponent("commands.cold_sweat.temperature.set.all.result", playerCount).getString()  +
                " \u00a7f" + amount + "\u00a7r"), false);
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
