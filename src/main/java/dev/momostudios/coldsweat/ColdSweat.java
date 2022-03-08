package dev.momostudios.coldsweat;

import dev.momostudios.coldsweat.client.event.WorldTempGaugeDisplay;
import dev.momostudios.coldsweat.client.renderer.HearthBlockEntityRenderer;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.config.*;
import dev.momostudios.coldsweat.common.capability.*;
import dev.momostudios.coldsweat.core.init.*;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.event.EntityRenderersEvent;
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
    public static final boolean remapMixins = false;

    public ColdSweat()
    {
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);
        bus.addListener(this::onCapInit);
        bus.addListener(this::registerBlockRenderers);
        BlockInit.BLOCKS.register(bus);
        BlockEntityInit.BLOCK_ENTITY_TYPES.register(bus);
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
        ColdSweatPacketHandler.init();
    }

    // Fix Hearth transparency
    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event)
    {
        ItemBlockRenderTypes.setRenderLayer(BlockInit.HEARTH.get(), RenderType.cutoutMipped());

        event.enqueueWork(() ->
        {
            ItemProperties.register(ModItems.HELLSPRING_LAMP, new ResourceLocation(ColdSweat.MOD_ID, "hellspring_state"), (stack, level, entity, id) ->
            {
                if (stack.getOrCreateTag().getBoolean("isOn"))
                {
                    return stack.getOrCreateTag().getInt("fuel") > 43 ? 3 :
                            stack.getOrCreateTag().getInt("fuel") > 22 ? 2 : 1;
                }
                return 0;
            });

            ItemProperties.register(ModItems.THERMOMETER, new ResourceLocation(ColdSweat.MOD_ID, "temperature"), (stack, level, entity, id) ->
            {
                Player player = Minecraft.getInstance().player;
                if (player != null)
                {
                    ConfigCache config = ConfigCache.getInstance();
                    double minTemp = config.minTemp;
                    double maxTemp = config.maxTemp;

                    double worldTemp = (float) CSMath.convertUnits(WorldTempGaugeDisplay.clientTemp, ClientSettingsConfig.getInstance().celsius() ? Temperature.Units.C : Temperature.Units.F, Temperature.Units.MC, true);

                    double worldTempAdjusted = CSMath.blend(-1.01d, 1d, worldTemp, minTemp, maxTemp);
                    return (float) worldTempAdjusted;
                }
                return 1;
            });
        });
    }

    @SubscribeEvent
    public void onCapInit(RegisterCapabilitiesEvent event)
    {
        event.register(ITemperatureCap.class);
    }

    @SubscribeEvent
    public void registerBlockRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(BlockEntityInit.HEARTH_TILE_ENTITY_TYPE.get(), (BlockEntityRendererProvider<BlockEntity>) ctx -> new HearthBlockEntityRenderer());
    }
}
