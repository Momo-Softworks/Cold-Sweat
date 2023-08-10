package dev.momostudios.coldsweat.common.command.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.momostudios.coldsweat.api.event.common.EnableTemperatureEvent;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.common.capability.ITemperatureCap;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.command.BaseCommand;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

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

    private int executeSetPlayerTemp(CommandSource source, Collection<? extends Entity> entities, int temp)
    {
        if (entities.stream().anyMatch(entity -> !(entity instanceof PlayerEntity || EnableTemperatureEvent.ENABLED_ENTITIES.contains(entity.getType()))))
        {
            source.sendFailure(new TranslationTextComponent("commands.cold_sweat.temperature.invalid"));
            return 0;
        }
        // Set the temperature for all affected targets
        for (Entity entity : entities)
        {   Temperature.set((LivingEntity) entity, Temperature.Type.CORE, temp);
        }

        //Compose & send message
        if (entities.size() == 1)
        {   Entity target = entities.iterator().next();
            source.sendSuccess(new TranslationTextComponent("commands.cold_sweat.temperature.set.single.result", target.getName().getString(), temp), true);
        }
        else
        {   source.sendSuccess(new TranslationTextComponent("commands.cold_sweat.temperature.set.many.result", entities.size(), temp), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executeGetPlayerTemp(CommandSource source, Collection<? extends Entity> entities)
    {
        if (entities.stream().anyMatch(entity -> !(entity instanceof PlayerEntity || EnableTemperatureEvent.ENABLED_ENTITIES.contains(entity.getType()))))
        {
            source.sendFailure(new TranslationTextComponent("commands.cold_sweat.temperature.invalid"));
            return 0;
        }
        for (Entity target : entities.stream().sorted(Comparator.comparing(player -> player.getName().getString())).collect(Collectors.toList()))
        {
            //Compose & send message
            int bodyTemp = (int) Temperature.get((LivingEntity) target, Temperature.Type.BODY);
            source.sendSuccess(new TranslationTextComponent("commands.cold_sweat.temperature.get.result", target.getName().getString(), bodyTemp), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executeShowModifiers(CommandSource source, Entity entity, Temperature.Type type)
    {
        if (!(entity instanceof PlayerEntity || EnableTemperatureEvent.ENABLED_ENTITIES.contains(entity.getType())))
        {
            source.sendFailure(new TranslationTextComponent("commands.cold_sweat.temperature.invalid"));
            return Command.SINGLE_SUCCESS;
        }
        for (TempModifier modifier : Temperature.getModifiers((LivingEntity) entity, type))
        {
            source.sendSuccess(new StringTextComponent("§f" + CSMath.sigFigs(modifier.getLastInput(), 2) + "§f -> §6" + modifier + "§f -> §b" + CSMath.sigFigs(modifier.getLastOutput(), 2)), false);
        }
        return Command.SINGLE_SUCCESS;
    }
}
