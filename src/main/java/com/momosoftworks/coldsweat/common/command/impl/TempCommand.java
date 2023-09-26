package com.momosoftworks.coldsweat.common.command.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.command.BaseCommand;
import com.momosoftworks.coldsweat.common.capability.EntityTempManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
                )
                .then(Commands.literal("showmodifiers")
                        .then(Commands.argument("entity", EntityArgument.entity())
                                  .then(Commands.literal(Temperature.Type.WORLD.getID())
                                          .executes(source -> executeShowModifiers(
                                                  source.getSource(), EntityArgument.getEntity(source, "entity"), Temperature.Type.WORLD))
                                  )
                                  .then(Commands.literal(Temperature.Type.CORE.getID())
                                          .executes(source -> executeShowModifiers(
                                                  source.getSource(), EntityArgument.getEntity(source, "entity"), Temperature.Type.CORE))
                                  )
                                  .then(Commands.literal(Temperature.Type.RATE.getID())
                                          .executes(source -> executeShowModifiers(
                                                  source.getSource(), EntityArgument.getEntity(source, "entity"), Temperature.Type.RATE))
                                  )
                                  .then(Commands.literal(Temperature.Type.BASE.getID())
                                          .executes(source -> executeShowModifiers(
                                                  source.getSource(), EntityArgument.getEntity(source, "entity"), Temperature.Type.BASE))
                                  )
                                  .then(Commands.literal(Temperature.Type.BURNING_POINT.getID())
                                          .executes(source -> executeShowModifiers(
                                                  source.getSource(), EntityArgument.getEntity(source, "entity"), Temperature.Type.BURNING_POINT))
                                  )
                                  .then(Commands.literal(Temperature.Type.FREEZING_POINT.getID())
                                          .executes(source -> executeShowModifiers(
                                                  source.getSource(), EntityArgument.getEntity(source, "entity"), Temperature.Type.FREEZING_POINT))
                                  )
                        )
                );
    }

    private int executeSetPlayerTemp(CommandSourceStack source, Collection<? extends Entity> entities, int temp)
    {
        if (entities.stream().anyMatch(entity -> !(entity instanceof Player || EntityTempManager.getEntitiesWithTemperature().contains(entity.getType()))))
        {
            source.sendFailure(new TranslatableComponent("commands.cold_sweat.temperature.invalid"));
            return Command.SINGLE_SUCCESS;
        }
        // Set the temperature for all affected targets
        for (Entity entity : entities)
        {
            if (!(entity instanceof LivingEntity)) continue;
            EntityTempManager.getTemperatureCap(((LivingEntity) entity)).ifPresent(cap ->
            {
                cap.setTemp(Temperature.Type.CORE, temp);
                Temperature.updateTemperature((LivingEntity) entity, cap, true);
            });
        }

        //Compose & send message
        if (entities.size() == 1)
        {
            Entity target = entities.iterator().next();
            source.sendSuccess(new TranslatableComponent("commands.cold_sweat.temperature.set.single.result", target.getName().getString(), temp), true);
        }
        else
        {
            source.sendSuccess(new TranslatableComponent("commands.cold_sweat.temperature.set.many.result", entities.size(), temp), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executeGetPlayerTemp(CommandSourceStack source, Collection<? extends Entity> entities)
    {
        if (entities.stream().anyMatch(entity -> !(entity instanceof Player || EntityTempManager.getEntitiesWithTemperature().contains(entity.getType()))))
        {
            source.sendFailure(new TranslatableComponent("commands.cold_sweat.temperature.invalid"));
            return Command.SINGLE_SUCCESS;
        }
        for (Entity target : entities.stream().sorted(Comparator.comparing(player -> player.getName().getString())).toList())
        {
            //Compose & send message
            int bodyTemp = (int) Temperature.get((LivingEntity) target, Temperature.Type.BODY);
            source.sendSuccess(new TranslatableComponent("commands.cold_sweat.temperature.get.result", target.getName().getString(), bodyTemp), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executeShowModifiers(CommandSourceStack source, Entity entity, Temperature.Type type)
    {
        if (!(entity instanceof Player || EntityTempManager.getEntitiesWithTemperature().contains(entity.getType())))
        {
            source.sendFailure(new TranslatableComponent("commands.cold_sweat.temperature.invalid"));
            return Command.SINGLE_SUCCESS;
        }
        for (TempModifier modifier : Temperature.getModifiers((LivingEntity) entity, type))
        {
            source.sendSuccess(new TextComponent("§f" + CSMath.sigFigs(modifier.getLastInput(), 2) + "§f -> §6" + modifier + "§f -> §b" + CSMath.sigFigs(modifier.getLastOutput(), 2)), false);
        }
        return Command.SINGLE_SUCCESS;
    }
}
