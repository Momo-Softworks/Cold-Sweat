package net.momostudios.coldsweat.core.event;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.temperature.modifier.*;
import net.momostudios.coldsweat.common.temperature.modifier.block.*;
import net.momostudios.coldsweat.common.temperature.modifier.sereneseasons.SereneSeasonsDummyModifier;
import net.momostudios.coldsweat.common.temperature.modifier.sereneseasons.SereneSeasonsTempModifier;
import net.momostudios.coldsweat.common.world.BlockEffectEntries;
import net.momostudios.coldsweat.common.world.TempModifierEntries;
import net.momostudios.coldsweat.core.event.csevents.TempModifierEvent;

@Mod.EventBusSubscriber
public class InitTempModifiers
{
    // Trigger TempModifierEvent.Init
    @SubscribeEvent
    public static void registerTempModifiers(WorldEvent.Load event) {
        TempModifierEntries.getEntries().flush();
        BlockEffectEntries.getEntries().flush();
        MinecraftForge.EVENT_BUS.post(new TempModifierEvent.Init.Modifier());
        MinecraftForge.EVENT_BUS.post(new TempModifierEvent.Init.Block());
    }

    // Register BlockEffects
    @SubscribeEvent
    public static void registerBlockEffects(TempModifierEvent.Init.Block event)
    {
        try
        {
            event.addBlockEffect(LavaBlockEffect.class);
            event.addBlockEffect(FurnaceBlockEffect.class);
            event.addBlockEffect(CampfireBlockEffect.class);
            event.addBlockEffect(FireBlockEffect.class);
            event.addBlockEffect(IceBlockEffect.class);
            event.addBlockEffect(IceboxBlockEffect.class);
            event.addBlockEffect(BoilerBlockEffect.class);
            event.addBlockEffect(SoulFireBlockEffect.class);
            event.addBlockEffect(SoulCampfireBlockEffect.class);
        }
        catch (Exception e) {
            ColdSweat.LOGGER.error("Registering BlockEffects failed!");
            e.printStackTrace();
        }
    }

    // Register TempModifiers
    @SubscribeEvent
    public static void registerTempModifiers(TempModifierEvent.Init.Modifier event)
    {
        try
        {
            event.addModifier(BlockTempModifier.class);
            event.addModifier(BiomeTempModifier.class);
            event.addModifier(DepthTempModifier.class);
            event.addModifier(LeatherTempModifier.class);
            event.addModifier(MinecartTempModifier.class);
            event.addModifier(TimeTempModifier.class);
            event.addModifier(WaterskinTempModifier.class);
            event.addModifier(WeatherTempModifier.class);
            event.addModifier(SoulLampTempModifier.class);
            event.addModifier(ModList.get().isLoaded("sereneseasons") ?
                    (Class<? extends TempModifier>) Class.forName("net.momostudios.coldsweat.common.temperature.modifier.sereneseasons.SereneSeasonsTempModifier") :
                    SereneSeasonsDummyModifier.class);
            event.addModifier(HearthTempModifier.class);
        }
        catch (Exception e) {
            ColdSweat.LOGGER.error("Registering TempModifiers failed!");
            e.printStackTrace();
        }
    }
}
