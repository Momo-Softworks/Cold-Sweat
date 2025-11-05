package com.momosoftworks.coldsweat;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.api.event.core.registry.AddRegistriesEvent;
import com.momosoftworks.coldsweat.api.event.vanilla.ServerConfigsLoadedEvent;
import com.momosoftworks.coldsweat.common.capability.insulation.ItemInsulationCap;
import com.momosoftworks.coldsweat.common.capability.shearing.ShearableFurCap;
import com.momosoftworks.coldsweat.common.capability.temperature.EntityTempCap;
import com.momosoftworks.coldsweat.common.capability.temperature.PlayerTempCap;
import com.momosoftworks.coldsweat.common.entity.Chameleon;
import com.momosoftworks.coldsweat.config.*;
import com.momosoftworks.coldsweat.config.spec.*;
import com.momosoftworks.coldsweat.core.advancement.trigger.ModAdvancementTriggers;
import com.momosoftworks.coldsweat.core.init.*;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.util.registries.ModEntities;
import com.momosoftworks.coldsweat.util.registries.ModFluids;
import com.momosoftworks.coldsweat.util.registries.ModGameRules;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ColdSweat.MOD_ID)
@Mod.EventBusSubscriber
public class ColdSweat
{
    public static final Logger LOGGER = LogManager.getLogger("Cold Sweat");

    public static final String MOD_ID = "cold_sweat";
    public static final IEventBus MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();

    public ColdSweat()
    {
        MOD_BUS.addListener(this::commonSetup);
        MOD_BUS.addListener(this::clientSetup);
        MOD_BUS.addListener(this::spawnPlacements);
        MOD_BUS.addListener(this::registerCaps);
        MOD_BUS.addListener(this::updateConfigs);

        // Register stuff
        BlockInit.BLOCKS.register(MOD_BUS);
        FluidInit.FLUID_TYPES.register(MOD_BUS);
        FluidInit.FLUIDS.register(MOD_BUS);
        ItemInit.ITEMS.register(MOD_BUS);
        EntityInit.ENTITY_TYPES.register(MOD_BUS);
        BlockEntityInit.BLOCK_ENTITY_TYPES.register(MOD_BUS);
        MenuInit.MENU_TYPES.register(MOD_BUS);
        EffectInit.EFFECTS.register(MOD_BUS);
        ParticleTypesInit.PARTICLES.register(MOD_BUS);
        PotionInit.POTIONS.register(MOD_BUS);
        SoundInit.SOUNDS.register(MOD_BUS);
        FeatureInit.FEATURES.register(MOD_BUS);
        BiomeCodecInit.BIOME_MODIFIER_SERIALIZERS.register(MOD_BUS);
        CreativeTabInit.ITEM_GROUPS.register(MOD_BUS);
        AttributeInit.ATTRIBUTES.register(MOD_BUS);
        CommandInit.ARGUMENTS.register(MOD_BUS);
        TempEffectInit.TEMP_EFFECTS.register(MOD_BUS);

        // Setup game rules
        ModGameRules.registerGameRules();

        // Handle config updates
        ModUpdater.updateFileNames();

        // Setup configs
        MainSettingsConfig.setup();
        ClientSettingsConfig.setup();
        WorldSettingsConfig.setup();
        ItemSettingsConfig.setup();
        EntitySettingsConfig.setup();

        // Setup compat
        CompatManager.registerEventHandlers();
        CompatManager.invokeRegistries(MOD_BUS);
    }

    public static ResourceLocation createKey(String path)
    {   return new ResourceLocation(MOD_ID, path);
    }

    public static String getVersion()
    {   return FMLLoader.getLoadingModList().getModFileById(ColdSweat.MOD_ID).versionString();
    }

    public void clientSetup(final FMLClientSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            ItemBlockRenderTypes.setRenderLayer(ModFluids.SLUSH, RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_SLUSH, RenderType.translucent());
        });
    }

    public void commonSetup(final FMLCommonSetupEvent event)
    {
        // Setup packets
        ColdSweatPacketHandler.init();
        event.enqueueWork(() ->
        {   // Register advancement triggers
            CriteriaTriggers.register(ModAdvancementTriggers.TEMPERATURE_CHANGED);
            CriteriaTriggers.register(ModAdvancementTriggers.SOUL_LAMP_FUELLED);
            CriteriaTriggers.register(ModAdvancementTriggers.BLOCK_AFFECTS_TEMP);
            CriteriaTriggers.register(ModAdvancementTriggers.ARMOR_INSULATED);
        });
    }

    public void spawnPlacements(SpawnPlacementRegisterEvent event)
    {
        event.register(ModEntities.CHAMELEON, SpawnPlacements.Type.ON_GROUND,
                       Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Chameleon::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    public void registerCaps(RegisterCapabilitiesEvent event)
    {   event.register(PlayerTempCap.class);
        event.register(EntityTempCap.class);
        event.register(ItemInsulationCap.class);
        event.register(ShearableFurCap.class);
    }

    public void updateConfigs(FMLLoadCompleteEvent event)
    {   ModUpdater.updateConfigs();
    }
}
