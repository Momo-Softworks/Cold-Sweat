package com.momosoftworks.coldsweat.api.event.vanilla;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.Event;

/**
 * Must be present because biome modifiers are loaded before {@link net.minecraftforge.event.server.ServerAboutToStartEvent} is fired, and configs must load before that point.<br>
 * <br>
 * Fires directly after Forge's server configs are loaded
 */
public class ServerConfigsLoadedEvent extends Event
{
    private MinecraftServer server;

    public ServerConfigsLoadedEvent(MinecraftServer server)
    {   this.server = server;
    }

    public MinecraftServer getServer()
    {   return server;
    }
}
