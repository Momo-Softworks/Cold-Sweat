package dev.momostudios.coldsweat.common.command.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.command.BaseCommand;
import dev.momostudios.coldsweat.util.entity.TempHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.Comparator;

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

    private int executeSetPlayerTemp(CommandSourceStack source, Collection<ServerPlayer> players, int amount)
    {
        // Set the temperature for all affected targets
        for (ServerPlayer player : players)
        {
            player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
            {
                cap.set(Temperature.Types.CORE, amount);
                TempHelper.updateTemperature(player, cap, true);
            });
        }

        //Compose & send message
        if (players.size() == 1)
        {
            Player target = players.iterator().next();
            source.sendSuccess(new TranslatableComponent("commands.cold_sweat.temperature.set.single.result", target.getName().getString(), amount), true);
        }
        else
        {
            source.sendSuccess(new TranslatableComponent("commands.cold_sweat.temperature.set.many.result", players.size(), amount), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executeGetPlayerTemp(CommandSourceStack source, Collection<ServerPlayer> players)
    {
        for (ServerPlayer target : players.stream().sorted(Comparator.comparing(player -> player.getName().getString())).toList())
        {
            //Compose & send message
            int bodyTemp = (int) TempHelper.getTemperature(target, Temperature.Types.BODY).get();
            source.sendSuccess(new TranslatableComponent("commands.cold_sweat.temperature.get.result", target.getName().getString(), bodyTemp), false);
        }
        return Command.SINGLE_SUCCESS;
    }
}
