package net.momostudios.coldsweat;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.momostudios.coldsweat.config.FuelItemsConfig;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.util.init.ModBlocks;
import net.momostudios.coldsweat.util.init.ContainerInit;
import net.momostudios.coldsweat.util.init.ModItems;
import net.momostudios.coldsweat.util.init.TileEntityInit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ColdSweat.MOD_ID)
public class ColdSweat
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "cold_sweat";

    public ColdSweat()
    {
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);

        ModBlocks.BLOCKS.register(bus);
        TileEntityInit.TILE_ENTITY_TYPE.register(bus);
        ContainerInit.CONTAINER_TYPES.register(bus);
        ModItems.ITEMS.register(bus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ColdSweatConfig.SPEC, "cold-sweat_common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FuelItemsConfig.SPEC, "cold-sweat_fuel-items.toml");
    }

    private void setup(final FMLCommonSetupEvent event)
    {
    }
}
