package com.momosoftworks.coldsweat.common.command.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.command.BaseCommand;
import com.momosoftworks.coldsweat.common.capability.EntityTempManager;
import com.momosoftworks.coldsweat.common.command.argument.AbilityOrTempTypeArgument;
import com.momosoftworks.coldsweat.common.command.argument.TempModifierTypeArgument;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
                /* Set temperature for entity */
                .then(Commands.literal("set")
                        .then(Commands.argument("entities", EntityArgument.entities())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(-150, 150))
                                        .executes(source -> executeSetEntityTemp(
                                        source.getSource(), EntityArgument.getEntities(source, "entities"), IntegerArgumentType.getInteger(source, "amount")))
                                )
                        )
                )
                /* Set temperature */
                .then(Commands.literal("get")
                        /* Get from entity */
                        .then(Commands.argument("entities", EntityArgument.entities())
                                .executes(source -> executeGetEntityTemp(
                                source.getSource(), EntityArgument.getEntities(source, "entities")))
                        )
                        /* Get from world */
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(source ->
                                          {   BlockPos pos = BlockPosArgument.getLoadedBlockPos(source, "pos");
                                              return executeGetWorldTemp(source.getSource(), pos.getX(), pos.getY(), pos.getZ(), source.getSource().getLevel());
                                          }
                                )
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                              .executes(source -> executeGetWorldTemp(
                                                      source.getSource(), IntegerArgumentType.getInteger(source, "x"),
                                                      IntegerArgumentType.getInteger(source, "y"), IntegerArgumentType.getInteger(source, "z"),
                                                      DimensionArgument.getDimension(source, "dimension"))
                                              )
                                )
                        )
                )
                /* Get TempModifiers from entity */
                .then(Commands.literal("debug")
                        .then(Commands.argument("entity", EntityArgument.entity())
                                  .then(Commands.argument("type", TempModifierTypeArgument.temperature())
                                          .executes(source -> executeShowModifiers(
                                                  source.getSource(), EntityArgument.getEntity(source, "entity"), TempModifierTypeArgument.getTemperature(source, "type"))
                                          )
                                  )
                        )
                )
                /* Modify attributes */
                .then(Commands.literal("modify")
                        .then(Commands.argument("entities", EntityArgument.entities())
                                /* Clear all attributes */
                                .then(Commands.literal("clear")
                                              /* Modify attribute of this type */
                                              .then(Commands.argument("type", AbilityOrTempTypeArgument.type())
                                                            .executes(source -> executeClearModifier(
                                                                    source.getSource(), EntityArgument.getEntities(source, "entities"),
                                                                    AbilityOrTempTypeArgument.getAttribute(source, "type"))
                                                            )
                                              )
                                              .executes(source -> executeClearAllModifiers(
                                                      source.getSource(), EntityArgument.getEntities(source, "entities"))
                                              )
                                )
                                /* Add to base value */
                                .then(Commands.literal("add")
                                              .then(Commands.argument("type", AbilityOrTempTypeArgument.type())
                                                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                                                          .then(Commands.argument("permanent", BoolArgumentType.bool())
                                                                                        .executes(source -> executeModifyEntityTemp(
                                                                                                source.getSource(), EntityArgument.getEntities(source, "entities"),
                                                                                                AbilityOrTempTypeArgument.getAttribute(source, "type"),
                                                                                                DoubleArgumentType.getDouble(source, "amount"),
                                                                                                AttributeModifier.Operation.ADDITION, BoolArgumentType.getBool(source, "permanent"))
                                                                                        )
                                                                          )
                                                                          /* Default to non-permanent if not specified */
                                                                          .executes(source -> executeModifyEntityTemp(
                                                                                  source.getSource(), EntityArgument.getEntities(source, "entities"),
                                                                                  AbilityOrTempTypeArgument.getAttribute(source, "type"),
                                                                                  DoubleArgumentType.getDouble(source, "amount"),
                                                                                  AttributeModifier.Operation.ADDITION, false)
                                                                          )
                                                            )
                                              )
                                )
                                /* Multiply base */
                                .then(Commands.literal("multiply_base")
                                              .then(Commands.argument("type", AbilityOrTempTypeArgument.type())
                                                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                                                          .then(Commands.argument("permanent", BoolArgumentType.bool())
                                                                                        .executes(source -> executeModifyEntityTemp(
                                                                                                source.getSource(), EntityArgument.getEntities(source, "entities"),
                                                                                                AbilityOrTempTypeArgument.getAttribute(source, "type"),
                                                                                                DoubleArgumentType.getDouble(source, "amount"),
                                                                                                AttributeModifier.Operation.MULTIPLY_BASE, BoolArgumentType.getBool(source, "permanent"))
                                                                                        )
                                                                          )
                                                                          .executes(source -> executeModifyEntityTemp(
                                                                                  source.getSource(), EntityArgument.getEntities(source, "entities"),
                                                                                  AbilityOrTempTypeArgument.getAttribute(source, "type"),
                                                                                  DoubleArgumentType.getDouble(source, "amount"),
                                                                                  AttributeModifier.Operation.MULTIPLY_BASE, false)
                                                                          )
                                                            )
                                              )
                                )
                                /* Multiply base */
                                .then(Commands.literal("multiply_total")
                                              .then(Commands.argument("type", AbilityOrTempTypeArgument.type())
                                                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                                                          .then(Commands.argument("permanent", BoolArgumentType.bool())
                                                                                        .executes(source -> executeModifyEntityTemp(
                                                                                                source.getSource(), EntityArgument.getEntities(source, "entities"),
                                                                                                AbilityOrTempTypeArgument.getAttribute(source, "type"),
                                                                                                DoubleArgumentType.getDouble(source, "amount"),
                                                                                                AttributeModifier.Operation.MULTIPLY_TOTAL, BoolArgumentType.getBool(source, "permanent"))
                                                                                        )
                                                                          )
                                                                          .executes(source -> executeModifyEntityTemp(
                                                                                  source.getSource(), EntityArgument.getEntities(source, "entities"),
                                                                                  AbilityOrTempTypeArgument.getAttribute(source, "type"),
                                                                                  DoubleArgumentType.getDouble(source, "amount"),
                                                                                  AttributeModifier.Operation.MULTIPLY_TOTAL, false)
                                                                          )
                                                            )
                                              )
                                )
                                .then(Commands.literal("set")
                                              .then(Commands.argument("type", AbilityOrTempTypeArgument.type())
                                                              .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                                                          .then(Commands.argument("permanent", BoolArgumentType.bool())
                                                                                          .executes(source -> executeModifyEntityTemp(
                                                                                                  source.getSource(), EntityArgument.getEntities(source, "entities"),
                                                                                                  AbilityOrTempTypeArgument.getAttribute(source, "type"),
                                                                                                  DoubleArgumentType.getDouble(source, "amount"),
                                                                                                  null, BoolArgumentType.getBool(source, "permanent"))
                                                                                          )
                                                                          )
                                                                          .executes(source -> executeModifyEntityTemp(
                                                                                  source.getSource(), EntityArgument.getEntities(source, "entities"),
                                                                                  AbilityOrTempTypeArgument.getAttribute(source, "type"),
                                                                                  DoubleArgumentType.getDouble(source, "amount"),
                                                                                  null, false)
                                                                          )
                                                              )
                                              )
                                )
                        )
                );
    }

    private int executeSetEntityTemp(CommandSourceStack source, Collection<? extends Entity> entities, int temp)
    {
        if (entities.stream().anyMatch(entity -> !(entity instanceof Player || EntityTempManager.getEntitiesWithTemperature().contains(entity.getType()))))
        {   source.sendFailure(Component.translatable("commands.cold_sweat.temperature.invalid"));
            return 0;
        }
        // Set the temperature for all affected targets
        for (Entity entity : entities)
        {
            if (!(entity instanceof LivingEntity)) continue;
            EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
            {   cap.setTemp(Temperature.Type.CORE, temp);
                Temperature.updateTemperature((LivingEntity) entity, cap, true);
            });
        }

        //Compose & send message
        if (entities.size() == 1)
        {   Entity target = entities.iterator().next();
            source.sendSuccess(() -> Component.translatable("commands.cold_sweat.temperature.set.single.result", target.getName().getString(), temp), true);
        }
        else
        {   source.sendSuccess(() -> Component.translatable("commands.cold_sweat.temperature.set.many.result", entities.size(), temp), true);
        }
        return entities.size();
    }

    private int executeGetEntityTemp(CommandSourceStack source, Collection<? extends Entity> entities)
    {
        if (entities.stream().anyMatch(entity -> !(entity instanceof Player || EntityTempManager.getEntitiesWithTemperature().contains(entity.getType()))))
        {   source.sendFailure(Component.translatable("commands.cold_sweat.temperature.invalid"));
            return 0;
        }
        for (Entity target : entities.stream().sorted(Comparator.comparing(player -> player.getName().getString())).toList())
        {   //Compose & send message
            int bodyTemp = (int) Temperature.get((LivingEntity) target, Temperature.Type.BODY);
            source.sendSuccess(() -> Component.translatable("commands.cold_sweat.temperature.get.result", target.getName().getString(), bodyTemp), false);
        }
        return entities.size();
    }

    private int executeGetWorldTemp(CommandSourceStack source, int x , int y, int z, ServerLevel level)
    {   //Compose & send message
        EntityTempManager.getTemperatureCap(source.getPlayer()).ifPresent(cap ->
        {
            int worldTemp = (int) Temperature.convertUnits(Temperature.getTemperatureAt(new BlockPos(x, y, z), level != null
                                                                                                               ? level
                                                                                                               : source.getLevel()),
                                                           Temperature.Units.MC, cap.getPreferredUnits(), true);
            source.sendSuccess(() -> Component.translatable("commands.cold_sweat.temperature.get.world.result", x, y, z, worldTemp, cap.getPreferredUnits().getSerializedName()), true);
        });
        return Command.SINGLE_SUCCESS;
    }

    private int executeShowModifiers(CommandSourceStack source, Entity entity, Temperature.Type type)
    {
        if (!(entity instanceof Player || EntityTempManager.getEntitiesWithTemperature().contains(entity.getType())))
        {   source.sendFailure(Component.translatable("commands.cold_sweat.temperature.invalid"));
            return 0;
        }
        for (TempModifier modifier : Temperature.getModifiers((LivingEntity) entity, type))
        {
            source.sendSuccess(() ->
                            Component.literal(CSMath.sigFigs(modifier.getLastInput(), 2)+"").withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(" → ").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(modifier.toString()).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" → ").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(CSMath.sigFigs(modifier.getLastOutput(), 2)+"").withStyle(ChatFormatting.AQUA)), false);
        }
        return Command.SINGLE_SUCCESS;
    }
    
    private int executeModifyEntityTemp(CommandSourceStack source, Collection<? extends Entity> entities, Either<Temperature.Type, Temperature.Ability> attribute,
                                        double amount, AttributeModifier.Operation operation, boolean permanent)
    {
        for (Entity entity : entities)
        {
            if (EntityTempManager.getEntitiesWithTemperature().contains(entity.getType()) && entity instanceof LivingEntity living)
            {
                EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
                {   AttributeInstance instance = EntityTempManager.getAttribute(attribute, living);
                    if (instance == null) return;
                    if (operation != null)
                    {   AttributeModifier modifier = EntityTempManager.makeAttributeModifier(attribute, amount, operation);
                        instance.addPermanentModifier(modifier);
                    }
                    else
                    {   EntityTempManager.getAttribute(attribute, living).setBaseValue(amount);
                    }
                    if (permanent)
                    {   cap.markPersistentAttribute(instance.getAttribute());
                    }
                    else cap.clearPersistentAttribute(instance.getAttribute());
                });
            }
            else
            {   source.sendFailure(Component.translatable("commands.cold_sweat.temperature.invalid"));
                return 0;
            }
        }
        if (entities.size() == 1)
        {
            if (operation == null)
            source.sendSuccess(() -> Component.translatable("commands.cold_sweat.temperature.modify.set.single.result",
                                                            attribute.left().map(StringRepresentable::getSerializedName)
                                                                    .orElse(attribute.right().map(StringRepresentable::getSerializedName)
                                                                    .orElse("")),
                                                            entities.iterator().next().getName().getString(), amount), true);
            else source.sendSuccess(() -> Component.translatable("commands.cold_sweat.temperature.modify.add_modifier.single.result",
                                                                 attribute.left().map(StringRepresentable::getSerializedName)
                                                                         .orElse(attribute.right().map(StringRepresentable::getSerializedName)
                                                                         .orElse("")),
                                                                 entities.iterator().next().getName().getString()), true);
        }
        else
        {
            if (operation == null)
            source.sendSuccess(() -> Component.translatable("commands.cold_sweat.temperature.modify.set.many.result",
                                                            attribute.left().map(StringRepresentable::getSerializedName)
                                                                    .orElse(attribute.right().map(StringRepresentable::getSerializedName)
                                                                    .orElse("")),
                                                            entities.size(), amount), true);
            else source.sendSuccess(() -> Component.translatable("commands.cold_sweat.temperature.modify.add_modifier.many.result",
                                                                 attribute.left().map(StringRepresentable::getSerializedName)
                                                                         .orElse(attribute.right().map(StringRepresentable::getSerializedName)
                                                                         .orElse("")),
                                                                 entities.size()), true);
        }
        return entities.size();
    }

    private int executeClearModifier(CommandSourceStack source, Collection<? extends Entity> entities, Either<Temperature.Type, Temperature.Ability> attribute)
    {
        for (Entity entity : entities)
        {
            if (EntityTempManager.getEntitiesWithTemperature().contains(entity.getType()) && entity instanceof LivingEntity living)
            {
                EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
                {   AttributeInstance instance = EntityTempManager.getAttribute(attribute, living);
                    if (instance == null) return;
                    instance.removeModifiers();
                    EntityTempManager.getAttribute(attribute, living).setBaseValue(Double.NaN);
                    cap.clearPersistentAttribute(instance.getAttribute());
                });
            }
            else
            {   source.sendFailure(Component.translatable("commands.cold_sweat.temperature.invalid"));
                return 0;
            }
        }
        if (entities.size() == 1)
        {   source.sendSuccess(() -> Component.translatable("commands.cold_sweat.temperature.clear.single.result",
                                                            attribute.left().map(StringRepresentable::getSerializedName)
                                                                    .orElse(attribute.right().map(StringRepresentable::getSerializedName)
                                                                                    .orElse("")),
                                                            entities.iterator().next().getName().getString()), true);
        }
        else
        {   source.sendSuccess(() -> Component.translatable("commands.cold_sweat.temperature.clear.many.result",
                                                            attribute.left().map(StringRepresentable::getSerializedName)
                                                                    .orElse(attribute.right().map(StringRepresentable::getSerializedName)
                                                                                    .orElse("")),
                                                            entities.size()), true);
        }
        return entities.size();
    }

    private int executeClearAllModifiers(CommandSourceStack source, Collection<? extends Entity> entities)
    {
        for (Entity entity : entities)
        {
            if (EntityTempManager.getEntitiesWithTemperature().contains(entity.getType()) && entity instanceof LivingEntity living)
            {
                EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
                {
                    for (Either<Temperature.Type, Temperature.Ability> attribute : EntityTempManager.VALID_ATTRIBUTES)
                    {
                        AttributeInstance instance = EntityTempManager.getAttribute(attribute, living);
                        if (instance == null) continue;
                        instance.removeModifiers();
                        EntityTempManager.getAttribute(attribute, living).setBaseValue(Double.NaN);
                        cap.clearPersistentAttribute(instance.getAttribute());
                    }
                });
            }
            else
            {   source.sendFailure(Component.translatable("commands.cold_sweat.temperature.invalid"));
                return 0;
            }
        }
        if (entities.size() == 1)
        {   source.sendSuccess(() -> Component.translatable("commands.cold_sweat.temperature.clear.all.single.result",
                                                            entities.iterator().next().getName().getString()), true);
        }
        else
        {   source.sendSuccess(() -> Component.translatable("commands.cold_sweat.temperature.clear.all.many.result",
                                                            entities.size()), true);
        }
        return entities.size();
    }
}
