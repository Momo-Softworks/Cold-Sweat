package com.momosoftworks.coldsweat.core.init;

import com.mojang.brigadier.CommandDispatcher;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.command.BaseCommand;
import com.momosoftworks.coldsweat.common.command.argument.*;
import com.momosoftworks.coldsweat.common.command.impl.TempCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;

@EventBusSubscriber
public class ModCommands
{
    private static final ArrayList<BaseCommand> COMMANDS = new ArrayList<>();

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event)
    {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        COMMANDS.add(new TempCommand("temperature", 2, true));
        COMMANDS.add(new TempCommand("temp", 2, true));

        COMMANDS.forEach(command ->
        {
            if (command.isEnabled() && command.setExecution() != null)
            {   dispatcher.register(command.getBuilder());
            }
        });
    }

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENTS = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, ColdSweat.MOD_ID);

    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<?, ?>> TEMP_MODIFIER_TRAIT = ARGUMENTS.register("temp_modifier_type",
                                                                                       () -> ArgumentTypeInfos.registerByClass(TempModifierTraitArgument.class, new TempModifierTraitArgument.Info()));
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<?, ?>> TEMPERATURE_TRAIT = ARGUMENTS.register("temperature_type",
                                                                                       () -> ArgumentTypeInfos.registerByClass(TemperatureTraitArgument.class, new TemperatureTraitArgument.Info()));
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<?, ?>> ATTRIBUTE_TRAIT_TRAIT = ARGUMENTS.register("attribute_trait_type",
                                                                                       () -> ArgumentTypeInfos.registerByClass(TempAttributeTraitArgument.class, new TempAttributeTraitArgument.Info()));
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<?, ?>> ENUM = ARGUMENTS.register("attribute_operation_type",
                                                                                       () -> ArgumentTypeInfos.registerByClass(NicerEnumArgument.class, new NicerEnumArgument.Info()));
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<?, ?>> TEMP_MODIFIER = ARGUMENTS.register("temp_modifier",
                                                                                       () -> ArgumentTypeInfos.registerByClass(TempModifierArgument.class, new TempModifierArgument.Info()));
}
