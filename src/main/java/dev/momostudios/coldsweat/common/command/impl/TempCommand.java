package dev.momostudios.coldsweat.common.command.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.momostudios.coldsweat.common.command.BaseCommand;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.util.PlayerHelper;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;

public class TempCommand extends BaseCommand
{
    public TempCommand(String name, int permissionLevel, boolean enabled) {
        super(name, permissionLevel, enabled);
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> setExecution()
    {
        return builder
                .then(Commands.literal("set")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(-150, 150))
                                        .executes(source -> executeSetPlayerTemp(
                                        source.getSource(), EntityArgument.getPlayers(source, "players"), IntegerArgumentType.getInteger(source, "amount")))
                                )
                        )
                )
                .then(Commands.literal("get")
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(source -> executeGetPlayerTemp(
                                source.getSource(), EntityArgument.getPlayers(source, "players")))
                        )
                );
    }

    private int executeSetPlayerTemp(CommandSource source, Collection<ServerPlayerEntity> players, int amount) throws CommandSyntaxException
    {
        if (players.size() == 1)
        {
            if (players.contains(source.asPlayer()))
            {
                //Set the sender's body temperature
                PlayerHelper.setTemperature(players.iterator().next(), new Temperature(amount), PlayerHelper.Types.BODY);

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
                    " \u00a7f" + PlayerHelper.getTemperature(source.asPlayer(), PlayerHelper.Types.BODY).get() + "\u00a7r"), false);
                }
            }
            else
            {
                //Set the target player's temperature
                PlayerHelper.setTemperature(players.iterator().next(), new Temperature(amount), PlayerHelper.Types.BODY);

                //Print success message to all players
                for (PlayerEntity player : source.asPlayer().world.getPlayers())
                {
                    //Compose the message
                    player.sendStatusMessage(new StringTextComponent(
                    "\u00a77\u00a7o[" + source.asPlayer().getScoreboardName() + "]: " +
                    new TranslationTextComponent("commands.cold_sweat.temperature.set.other.result", players.iterator().next().getScoreboardName()).getString()  +
                    " \u00a7f" + PlayerHelper.getTemperature(players.iterator().next(), PlayerHelper.Types.BODY).get() + "\u00a7r"), false);
                }
            }
        }
        else
        {
            int playerCount = 0;
            for (ServerPlayerEntity player : players)
            {
                PlayerHelper.setTemperature(player, new Temperature(amount), PlayerHelper.Types.BODY);
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

    private int executeGetPlayerTemp(CommandSource source, Collection<ServerPlayerEntity> players) throws CommandSyntaxException
    {
        for (ServerPlayerEntity player : players)
        {
            //Compose the message
            source.asPlayer().sendStatusMessage(new StringTextComponent(
            "\u00a77" +
            new TranslationTextComponent("commands.cold_sweat.temperature.get.result", player.getScoreboardName()).getString()  +
            " \u00a7f" + (int) PlayerHelper.getTemperature(player, PlayerHelper.Types.COMPOSITE).get() + "\u00a7r"), false);
        }
        return Command.SINGLE_SUCCESS;
    }
}
