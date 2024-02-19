package com.momosoftworks.coldsweat.core.init;

import com.mojang.brigadier.CommandDispatcher;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.command.BaseCommand;
import com.momosoftworks.coldsweat.common.command.argument.AbilityOrTempTypeArgument;
import com.momosoftworks.coldsweat.common.command.argument.TempModifierTypeArgument;
import com.momosoftworks.coldsweat.common.command.argument.TemperatureTypeArgument;
import com.momosoftworks.coldsweat.common.command.impl.TempCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;

@Mod.EventBusSubscriber
public class CommandInit
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

        ArgumentTypeInfos.registerByClass(TemperatureTypeArgument.class, new TemperatureTypeArgument.Info());
        ArgumentTypeInfos.registerByClass(AbilityOrTempTypeArgument.class, new AbilityOrTempTypeArgument.Info());
        ArgumentTypeInfos.registerByClass(TempModifierTypeArgument.class, new TempModifierTypeArgument.Info());
    }

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENTS = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, ColdSweat.MOD_ID);

    RegistryObject<ArgumentTypeInfo<?, ?>> TEMP_MODIFIER_TYPE = ARGUMENTS.register("temp_modifier_type", () -> new TempModifierTypeArgument.Info());
    RegistryObject<ArgumentTypeInfo<?, ?>> TEMPERATURE_TYPE = ARGUMENTS.register("temperature_type", () -> new TemperatureTypeArgument.Info());
    RegistryObject<ArgumentTypeInfo<?, ?>> ABILITY_OR_TEMP_TYPE = ARGUMENTS.register("ability_or_temp_type", () -> new AbilityOrTempTypeArgument.Info());
}
