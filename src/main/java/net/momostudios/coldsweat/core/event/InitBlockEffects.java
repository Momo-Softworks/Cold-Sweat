package net.momostudios.coldsweat.core.event;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.common.temperature.modifier.block.BlockEffectEntries;
import net.momostudios.coldsweat.common.temperature.modifier.block.FurnaceBlockEffect;
import net.momostudios.coldsweat.common.temperature.modifier.block.LavaBlockEffect;

@Mod.EventBusSubscriber
public class InitBlockEffects
{
    @SubscribeEvent
    public static void registerBlockEffects(WorldEvent.Load event)
    {
        BlockEffectEntries.add(new LavaBlockEffect());
        BlockEffectEntries.add(new FurnaceBlockEffect());
    }

    @SubscribeEvent
    public static void registerBlockEffects(WorldEvent.Unload event)
    {
        BlockEffectEntries.flush();
    }
}
