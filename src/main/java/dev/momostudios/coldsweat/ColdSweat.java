package dev.momostudios.coldsweat;

import dev.momostudios.coldsweat.config.*;
import dev.momostudios.coldsweat.core.capabilities.*;
import dev.momostudios.coldsweat.core.init.*;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
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
    public static final boolean remapMixins = false;

    public ColdSweat()
    {
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);
        BlockInit.BLOCKS.register(bus);
        BlockEntityInit.TILE_ENTITY_TYPE.register(bus);
        ContainerInit.CONTAINER_TYPES.register(bus);
        ItemInit.ITEMS.register(bus);
        EffectInit.EFFECTS.register(bus);
        ParticleTypesInit.PARTICLES.register(bus);
        PotionInit.POTIONS.register(bus);
        SoundInit.SOUNDS.register(bus);

        // Setup configs
        WorldTemperatureConfig.setup();
        ItemSettingsConfig.setup();
        ColdSweatConfig.setup();
        ClientSettingsConfig.setup();
        EntitySettingsConfig.setup();
    }

    // Register Commands
    @SubscribeEvent
    public void onCommandRegister(final RegisterCommandsEvent event)
    {
        CommandInit.registerCommands(event);
    }

    // Register Packet
    @SubscribeEvent
    public void commonSetup(final FMLCommonSetupEvent event)
    {
        CapabilityManager.INSTANCE.register(IBlockStorageCap.class, new HearthRadiusCapStorage(), HearthRadiusCapability::new);
        CapabilityManager.INSTANCE.register(PlayerTempCapability.class, new DummyStorage(), PlayerTempCapability::new);
        ColdSweatPacketHandler.init();
    }

    // Fix Hearth transparency
    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event)
    {
        RenderTypeLookup.setRenderLayer(BlockInit.HEARTH.get(), RenderType.getCutoutMipped());
    }
}
