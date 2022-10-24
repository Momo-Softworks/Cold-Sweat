package dev.momostudios.coldsweat;

import dev.momostudios.coldsweat.common.capability.ITemperatureCap;
import dev.momostudios.coldsweat.config.*;
import dev.momostudios.coldsweat.core.init.*;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ColdSweat.MOD_ID)
public class ColdSweat
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "cold_sweat";
    public static final boolean REMAP_MIXINS = true;

    public ColdSweat()
    {
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);
        bus.addListener(this::registerCaps);

        BlockInit.BLOCKS.register(bus);
        ItemInit.ITEMS.register(bus);
        BlockEntityInit.BLOCK_ENTITY_TYPES.register(bus);
        MenuInit.MENU_TYPES.register(bus);
        EffectInit.EFFECTS.register(bus);
        ParticleTypesInit.PARTICLES.register(bus);
        PotionInit.POTIONS.register(bus);
        SoundInit.SOUNDS.register(bus);

        // Setup configs
        WorldSettingsConfig.setup();
        ItemSettingsConfig.setup();
        ColdSweatConfig.setup();
        ClientSettingsConfig.setup();
        EntitySettingsConfig.setup();
    }

    @SubscribeEvent
    public void registerCommands(final RegisterCommandsEvent event)
    {
        CommandInit.registerCommands(event);
    }

    @SubscribeEvent
    public void commonSetup(final FMLCommonSetupEvent event)
    {
        ColdSweatPacketHandler.init();
    }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event)
    {
        // Fix hearth transparency
        ItemBlockRenderTypes.setRenderLayer(BlockInit.HEARTH_BOTTOM.get(), RenderType.cutoutMipped());
    }

    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event)
    {
        event.register(ITemperatureCap.class);
    }
}
