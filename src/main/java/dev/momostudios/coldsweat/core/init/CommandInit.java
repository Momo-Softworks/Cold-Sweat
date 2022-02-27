package dev.momostudios.coldsweat.core.init;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import dev.momostudios.coldsweat.common.command.BaseCommand;
import dev.momostudios.coldsweat.common.command.impl.TempCommand;

import java.util.ArrayList;

public class CommandInit
{
    private static final ArrayList<BaseCommand> commands = new ArrayList();

    public static void registerCommands(final RegisterCommandsEvent event)
    {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        commands.add(new TempCommand("temperature", 2, true));
        commands.add(new TempCommand("temp", 2, true));

        commands.forEach(command -> {
            if (command.isEnabled() && command.setExecution() != null)
            {
                dispatcher.register(command.getBuilder());
            }
        });
    }
}
