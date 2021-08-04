package net.momostudios.coldsweat.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.temperature.capabilities.TempModifiersCapability;
import net.momostudios.coldsweat.temperature.capabilities.TemperatureCapability;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID)
public class AttachCapabilities
{
    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        Entity entity = event.getObject();
        if (entity instanceof LivingEntity)
        {
            event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "temperature"), new TemperatureCapability.Provider());
            event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "temperature_modifiers"), new TempModifiersCapability.Provider());
        }
    }
}
