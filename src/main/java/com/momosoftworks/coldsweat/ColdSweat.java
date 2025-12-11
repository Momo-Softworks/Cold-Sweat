package com.momosoftworks.coldsweat;

import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.entity.Chameleon;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.config.ModUpdater;
import com.momosoftworks.coldsweat.config.spec.*;
import com.momosoftworks.coldsweat.core.init.*;
import com.momosoftworks.coldsweat.util.registries.ModGameRules;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod(ColdSweat.MOD_ID)
public class ColdSweat
{
    public static final Logger LOGGER = LogManager.getLogger("Cold Sweat");

    public static final String MOD_ID = "cold_sweat";
    public static IEventBus MOD_BUS = null;

    public ColdSweat(IEventBus bus, ModContainer modContainer)
    {
        MOD_BUS = bus;

        MOD_BUS.addListener(this::clientSetup);
        MOD_BUS.addListener(this::spawnPlacements);
        MOD_BUS.addListener(this::registerCaps);
        MOD_BUS.addListener(this::updateConfigs);

        // Register stuff
        ModBlocks.BLOCKS.register(MOD_BUS);
        ModFluids.FLUID_TYPES.register(MOD_BUS);
        ModFluids.FLUIDS.register(MOD_BUS);
        ModItems.ITEMS.register(MOD_BUS);
        ModEntities.ENTITY_TYPES.register(MOD_BUS);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(MOD_BUS);
        ModMenus.MENU_TYPES.register(MOD_BUS);
        ModEffects.EFFECTS.register(MOD_BUS);
        ModParticleTypes.PARTICLES.register(MOD_BUS);
        ModPotions.POTIONS.register(MOD_BUS);
        ModSounds.SOUNDS.register(MOD_BUS);
        ModFeatures.FEATURES.register(MOD_BUS);
        ModBiomeModifiers.BIOME_MODIFIER_SERIALIZERS.register(MOD_BUS);
        ModCreativeTabs.ITEM_GROUPS.register(MOD_BUS);
        ModAttributes.ATTRIBUTES.register(MOD_BUS);
        ModCommands.ARGUMENTS.register(MOD_BUS);
        ModArmorMaterials.ARMOR_MATERIALS.register(MOD_BUS);
        ModAdvancementTriggers.TRIGGERS.register(MOD_BUS);
        ModItemComponents.DATA_COMPONENTS.register(MOD_BUS);
        ModTempEffects.TEMP_EFFECTS.register(MOD_BUS);
        ModDataAttachments.DATA_ATTACHMENTS.register(MOD_BUS);

        // Setup game rules
        ModGameRules.registerGameRules();

        // Handle config updates
        ModUpdater.updateFileNames();

        // Setup configs
        MainSettingsConfig.setup(modContainer);
        ClientSettingsConfig.setup(modContainer);
        WorldSettingsConfig.setup(modContainer);
        ItemSettingsConfig.setup(modContainer);
        EntitySettingsConfig.setup(modContainer);

        // Setup compat
        CompatManager.registerEventHandlers();
    }

    public static ResourceLocation createKey(String path)
    {   return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String getVersion()
    {   return FMLLoader.getLoadingModList().getModFileById(ColdSweat.MOD_ID).versionString();
    }

    public void clientSetup(final FMLClientSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            ItemBlockRenderTypes.setRenderLayer(ModFluids.SLUSH.value(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_SLUSH.value(), RenderType.translucent());
        });
    }

    public void spawnPlacements(RegisterSpawnPlacementsEvent event)
    {
        event.register(ModEntities.CHAMELEON.value(), SpawnPlacementTypes.ON_GROUND,
                       Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Chameleon::canSpawn, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    public void registerCaps(RegisterCapabilitiesEvent event)
    {
        for (BlockEntityType<? extends HearthBlockEntity> blockEntityType : List.of(ModBlockEntities.HEARTH.value(), ModBlockEntities.BOILER.value(), ModBlockEntities.ICEBOX.value()))
        {
            // Register fluid handlers for hearth-like blocks
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, blockEntityType, (hearthLike, facing) -> hearthLike.getFuelHandler());
        }
    }

    public void updateConfigs(FMLLoadCompleteEvent event)
    {   ModUpdater.updateConfigs();
    }
}
