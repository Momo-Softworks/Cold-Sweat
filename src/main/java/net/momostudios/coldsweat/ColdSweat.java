package net.momostudios.coldsweat;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.momostudios.coldsweat.core.init.*;
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

        ModBlocks.BLOCKS.register(bus);
        TileEntityInit.TILE_ENTITY_TYPE.register(bus);
        ContainerInit.CONTAINER_TYPES.register(bus);
        ItemInit.ITEMS.register(bus);

        /*ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FuelItemsConfig.SPEC, "cold-sweat_fuel-items.toml");
        ModLoadingContext context = ModLoadingContext.get();
        context.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,
            () -> (mc, screen) -> new ConfigScreen.PageOne());*/
    }

    @SubscribeEvent
    public void onCommandRegister(final RegisterCommandsEvent event)
    {
        CommandInit.registerCommands(event);
    }
}
