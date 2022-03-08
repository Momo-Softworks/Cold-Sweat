package dev.momostudios.coldsweat.common.command.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.momostudios.coldsweat.common.command.BaseCommand;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.util.entity.PlayerHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public class TempCommand extends BaseCommand
{
    public TempCommand(String name, int permissionLevel, boolean enabled) {
        super(name, permissionLevel, enabled);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> setExecution()
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

    private int executeSetPlayerTemp(CommandSourceStack source, Collection<ServerPlayer> players, int amount) throws CommandSyntaxException
    {
        if (players.size() == 1)
        {
            if (players.contains(source.getPlayerOrException()))
            {
                //Set the sender's body temperature
                PlayerHelper.setTemperature(players.iterator().next(), new dev.momostudios.coldsweat.common.temperature.Temperature(amount), Temperature.Types.BODY);

                //Print success message to all players
                for (Player player : source.getPlayerOrException().level.players())
                {
                    //Determine if the message is being sent to the sender or another player
                    TranslatableComponent message = player == source.getPlayerOrException() ?
                    new TranslatableComponent("commands.cold_sweat.temperature.set.self.result") :
                    new TranslatableComponent("commands.cold_sweat.temperature.set.other.result", source.getPlayerOrException().getName().getString());

                    //Compose the message
                    player.sendMessage(new TextComponent(
                    "\u00a77\u00a7o[" + source.getPlayerOrException().getScoreboardName() + "]: " +
                    message.getString()  +
                    " \u00a7f" + PlayerHelper.getTemperature(source.getPlayerOrException(), Temperature.Types.BODY).get() + "\u00a7r"),
                    source.getPlayerOrException().getUUID());
                }
            }
            else
            {
                //Set the target player's temperature
                PlayerHelper.setTemperature(players.iterator().next(), new dev.momostudios.coldsweat.common.temperature.Temperature(amount), Temperature.Types.BODY);

                //Print success message to all players
                for (Player player : source.getPlayerOrException().level.players())
                {
                    //Compose the message
                    player.sendMessage(new TextComponent(
                    "\u00a77\u00a7o[" + source.getPlayerOrException().getScoreboardName() + "]: " +
                    new TranslatableComponent("commands.cold_sweat.temperature.set.other.result", players.iterator().next().getScoreboardName()).getString()  +
                    " \u00a7f" + PlayerHelper.getTemperature(players.iterator().next(), Temperature.Types.BODY).get() + "\u00a7r"),
                    source.getPlayerOrException().getUUID());
                }
            }
        }
        else
        {
            int playerCount = 0;
            for (ServerPlayer player : players)
            {
                PlayerHelper.setTemperature(player, new dev.momostudios.coldsweat.common.temperature.Temperature(amount), Temperature.Types.BODY);
                playerCount++;
            }

            //Print success message to all players
            for (Player player : source.getPlayerOrException().level.players())
            {
                //Compose the message
                player.sendMessage(new TextComponent(
                "\u00a77\u00a7o[" + source.getPlayerOrException().getScoreboardName() + "]: " +
                new TranslatableComponent("commands.cold_sweat.temperature.set.all.result", playerCount).getString()  +
                " \u00a7f" + amount + "\u00a7r"), player.getUUID());
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executeGetPlayerTemp(CommandSourceStack source, Collection<ServerPlayer> players) throws CommandSyntaxException
    {
        for (ServerPlayer player : players)
        {
            //Compose the message
            source.getPlayerOrException().sendMessage(new TextComponent(
            "\u00a77" +
            new TranslatableComponent("commands.cold_sweat.temperature.get.result", player.getScoreboardName()).getString()  +
            " \u00a7f" + (int) PlayerHelper.getTemperature(player, Temperature.Types.TOTAL).get() + "\u00a7r"),
            source.getPlayerOrException().getUUID());
        }
        return Command.SINGLE_SUCCESS;
    }
}
