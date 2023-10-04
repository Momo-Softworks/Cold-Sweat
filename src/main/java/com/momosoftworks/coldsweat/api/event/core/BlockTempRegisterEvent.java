package com.momosoftworks.coldsweat.api.event.core;

import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;

/**
 * Fired when the {@link BlockTemp} registry is being built ({@link BlockTempRegistry}). <br>
 * The event is fired during {@link net.minecraftforge.event.world.WorldEvent.Load}. <br>
 * <br>
 * Use {@code BlockTempRegistry.flush()} if calling manually to prevent duplicates. <br>
 * (You probably shouldn't ever do that anyway) <br>
 * <br>
 * This event is not {@link cpw.mods.fml.common.eventhandler.Cancelable}. <br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public class BlockTempRegisterEvent extends Event
{
    /**
     * Adds a new {@link BlockTemp} to the registry.
     *
     * @param blockTemp The BlockTemp to add.
     */
    public void register(BlockTemp blockTemp)
    {
        BlockTempRegistry.register(blockTemp);
    }
}
