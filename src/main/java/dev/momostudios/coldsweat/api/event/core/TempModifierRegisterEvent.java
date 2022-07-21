package dev.momostudios.coldsweat.api.event.core;

import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when the {@link TempModifier} registry is being built ({@link TempModifierRegistry}). <br>
 * The event is fired during {@link net.minecraftforge.event.world.WorldEvent.Load}. <br>
 * <br>
 * Use {@code TempModifierRegistry.flush()} if calling manually to prevent duplicates. <br>
 * (You probably shouldn't ever do that anyway) <br>
 * <br>
 * This event is not {@link net.minecraftforge.eventbus.api.Cancelable}. <br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public class TempModifierRegisterEvent extends Event
{
    /**
     * Adds a new {@link TempModifier} to the registry.
     *
     * @param modifier the {@link TempModifier} to add.
     */
    public void register(TempModifier modifier)
    {
        TempModifierRegistry.register(modifier);
    }
}
