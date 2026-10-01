package com.momosoftworks.coldsweat.common.command;

import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.Permission;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class BaseCommand
{
    protected LiteralArgumentBuilder<CommandSourceStack> builder;
    boolean enabled;

    public BaseCommand(String name, int permissionLevel, boolean enabled)
    {   Permission permission = new Permission.HasCommandLevel(PermissionLevel.byId(permissionLevel));
        this.builder = Commands.literal(name).requires(source -> source.permissions().hasPermission(permission));
        this.enabled = enabled;
    }

    public LiteralArgumentBuilder<CommandSourceStack> getBuilder()
    {   return builder;
    }

    public boolean isEnabled()
    {   return enabled;
    }

    public LiteralArgumentBuilder<CommandSourceStack> setExecution()
    {   return null;
    }
}
