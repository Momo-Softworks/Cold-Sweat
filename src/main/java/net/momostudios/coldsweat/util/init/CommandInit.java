package net.momostudios.coldsweat.util.init;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.momostudios.coldsweat.command.BaseCommand;
import net.momostudios.coldsweat.command.impl.TempCommand;

import java.util.ArrayList;

public class CommandInit
{
    private static final ArrayList<BaseCommand> commands = new ArrayList();

    public static void registerCommands(final RegisterCommandsEvent event)
    {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();

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
