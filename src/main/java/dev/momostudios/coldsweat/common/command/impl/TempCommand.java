package dev.momostudios.coldsweat.common.command.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.common.capability.ITemperatureCap;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.command.BaseCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;

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
                        .then(Commands.argument("entities", EntityArgument.entities())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(-150, 150))
                                        .executes(source -> executeSetPlayerTemp(
                                        source.getSource(), EntityArgument.getEntities(source, "entities"), IntegerArgumentType.getInteger(source, "amount")))
                                )
                        )
                )
                .then(Commands.literal("get")
                        .then(Commands.argument("entities", EntityArgument.entities())
                                .executes(source -> executeGetPlayerTemp(
                                source.getSource(), EntityArgument.getEntities(source, "entities")))
                        )
                );
    }

    private int executeSetPlayerTemp(CommandSourceStack source, Collection<? extends Entity> entities, int temp)
    {
        if (entities.stream().anyMatch(entity -> !(entity instanceof LivingEntity)))
        {
            source.sendFailure(Component.translatable("commands.cold_sweat.temperature.invalid"));
            return Command.SINGLE_SUCCESS;
        }
        // Set the temperature for all affected targets
        for (Entity entity : entities)
        {
            Capability<ITemperatureCap> iCap = entity instanceof Player ? ModCapabilities.PLAYER_TEMPERATURE : ModCapabilities.ENTITY_TEMPERATURE;
            if (iCap == null) continue;
            entity.getCapability(iCap).ifPresent(cap ->
            {
                cap.setTemp(Temperature.Type.CORE, temp);
                Temperature.updateTemperature((LivingEntity) entity, cap, true);
            });
        }

        //Compose & send message
        if (entities.size() == 1)
        {
            Entity target = entities.iterator().next();
            source.sendSuccess(Component.translatable("commands.cold_sweat.temperature.set.single.result", target.getName().getString(), temp), true);
        }
        else
        {
            source.sendSuccess(Component.translatable("commands.cold_sweat.temperature.set.many.result", entities.size(), temp), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executeGetPlayerTemp(CommandSourceStack source, Collection<? extends Entity> entities)
    {
        if (entities.stream().anyMatch(entity -> !(entity instanceof LivingEntity)))
        {
            source.sendFailure(Component.translatable("commands.cold_sweat.temperature.invalid"));
            return Command.SINGLE_SUCCESS;
        }
        for (Entity target : entities.stream().sorted(Comparator.comparing(player -> player.getName().getString())).toList())
        {
            //Compose & send message
            int bodyTemp = (int) Temperature.get((LivingEntity) target, Temperature.Type.BODY);
            source.sendSuccess(Component.translatable("commands.cold_sweat.temperature.get.result", target.getName().getString(), bodyTemp), false);
        }
        return Command.SINGLE_SUCCESS;
    }
}
