package net.momostudios.coldsweat.core.event;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.temperature.modifier.*;
import net.momostudios.coldsweat.common.temperature.modifier.block.*;
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
            event.addBlockEffect(NetherPortalBlockEffect.class);
            event.addBlockEffect(MagmaBlockEffect.class);
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
        String sereneseasons = "net.momostudios.coldsweat.common.temperature.modifier.sereneseasons.SereneSeasonsTempModifier";
        try
        {
            event.addModifier(BlockTempModifier.class);
            event.addModifier(BiomeTempModifier.class);
            event.addModifier(DepthTempModifier.class);
            event.addModifier(InsulationTempModifier.class);
            event.addModifier(MinecartTempModifier.class);
            event.addModifier(TimeTempModifier.class);
            event.addModifier(WaterskinTempModifier.class);
            event.addModifier(SoulLampTempModifier.class);
            if (ModList.get().isLoaded("sereneseasons")) event.addModifier((Class<TempModifier>) Class.forName(sereneseasons));
            event.addModifier(WaterTempModifier.class);
            event.addModifier(HearthTempModifier.class);
        }
        catch (Exception e) {
            ColdSweat.LOGGER.error("Registering TempModifiers failed!");
            e.printStackTrace();
        }
    }
}
