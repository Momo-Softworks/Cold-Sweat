package net.momostudios.coldsweat.core.event;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.common.temperature.modifier.*;
import net.momostudios.coldsweat.common.temperature.modifier.block.*;
import net.momostudios.coldsweat.common.world.BlockEffectEntries;
import net.momostudios.coldsweat.common.world.TempModifierEntries;

@Mod.EventBusSubscriber
public class InitTempModifiers
{
    // Register blocks that emit temperature
    @SubscribeEvent
    public static void registerBlockEffects(WorldEvent.Load event)
    {
        BlockEffectEntries BLE = BlockEffectEntries.getEntries();
        BLE.flush();
        BLE.add(new LavaBlockEffect());
        BLE.add(new FurnaceBlockEffect());
        BLE.add(new CampfireBlockEffect());
        BLE.add(new FireBlockEffect());
        BLE.add(new IceBlockEffect());
        BLE.add(new IceboxBlockEffect());
        BLE.add(new BoilerBlockEffect());
    }

    // Register TempModifiers
    @SubscribeEvent
    public static void registerTempModifiers(WorldEvent.Load event)
    {
        TempModifierEntries TME = TempModifierEntries.getEntries();
        TME.flush();
        TME.add(new BlockTempModifier());
        TME.add(new BiomeTempModifier());
        TME.add(new DepthTempModifier());
        TME.add(new LeatherTempModifier());
        TME.add(new MinecartTempModifier());
        TME.add(new TimeTempModifier());
        TME.add(new WaterskinTempModifier());
        TME.add(new WeatherTempModifier());
        TME.add(new HearthTempModifier());
    }
}
