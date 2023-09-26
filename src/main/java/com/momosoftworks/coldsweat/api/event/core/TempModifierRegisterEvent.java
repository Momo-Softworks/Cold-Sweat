package com.momosoftworks.coldsweat.api.event.core;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.function.Supplier;

/**
 * Builds the {@link TempModifierRegistry}. <br>
 * The event is fired during {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}. <br>
 * <br>
 * This event is NOT {@link net.minecraftforge.eventbus.api.Cancelable}. <br>
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
    public void register(Supplier<TempModifier> modifier)
    {   TempModifierRegistry.register(modifier);
    }

    public void registerByClassName(String className)
    {
        try
        {
            this.register(() ->
            {   try
                {   return (TempModifier) Class.forName(className).getConstructor().newInstance();
                } catch (Exception e) { throw new RuntimeException(e); }
            });
        }
        catch (Exception e)
        {   ColdSweat.LOGGER.error("Failed to register TempModifier by class name: \"" + className + "\"!", e);
        }
    }
}
