package com.momosoftworks.coldsweat.core.init;

import com.mojang.brigadier.CommandDispatcher;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.command.BaseCommand;
import com.momosoftworks.coldsweat.common.command.argument.TempAttributeTraitArgument;
import com.momosoftworks.coldsweat.common.command.argument.TempModifierTraitArgument;
import com.momosoftworks.coldsweat.common.command.argument.TemperatureTraitArgument;
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

        ArgumentTypeInfos.registerByClass(TemperatureTraitArgument.class, new TemperatureTraitArgument.Info());
        ArgumentTypeInfos.registerByClass(TempAttributeTraitArgument.class, new TempAttributeTraitArgument.Info());
        ArgumentTypeInfos.registerByClass(TempModifierTraitArgument.class, new TempModifierTraitArgument.Info());
    }

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENTS = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, ColdSweat.MOD_ID);

    DeferredHolder<ArgumentTypeInfo<?,?>, ArgumentTypeInfo<?, ?>> TEMP_MODIFIER_TYPE = ARGUMENTS.register("temp_modifier_type", () -> new TempModifierTraitArgument.Info());
    DeferredHolder<ArgumentTypeInfo<?,?>, ArgumentTypeInfo<?, ?>> TEMPERATURE_TYPE = ARGUMENTS.register("temperature_type", () -> new TemperatureTraitArgument.Info());
    DeferredHolder<ArgumentTypeInfo<?,?>, ArgumentTypeInfo<?, ?>> ABILITY_OR_TEMP_TYPE = ARGUMENTS.register("ability_or_temp_type", () -> new TempAttributeTraitArgument.Info());
}
