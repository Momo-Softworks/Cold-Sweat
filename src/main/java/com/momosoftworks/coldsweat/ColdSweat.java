package com.momosoftworks.coldsweat;

import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.capability.ModCapabilities;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.insulation.ItemInsulationCap;
import com.momosoftworks.coldsweat.common.capability.shearing.ShearableFurCap;
import com.momosoftworks.coldsweat.common.capability.temperature.EntityTempCap;
import com.momosoftworks.coldsweat.common.capability.temperature.PlayerTempCap;
import com.momosoftworks.coldsweat.common.entity.Chameleon;
import com.momosoftworks.coldsweat.config.*;
import com.momosoftworks.coldsweat.config.spec.*;
import com.momosoftworks.coldsweat.core.advancement.trigger.ModAdvancementTriggers;
import com.momosoftworks.coldsweat.core.init.*;
import com.momosoftworks.coldsweat.core.network.ModPacketHandlers;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.configuration.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.SpawnPlacementRegisterEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod(ColdSweat.MOD_ID)
public class ColdSweat
{
    public static final Logger LOGGER = LogManager.getLogger("Cold Sweat");

    public static final String MOD_ID = "cold_sweat";

    public ColdSweat(IEventBus bus, ModContainer modContainer)
    {
        //NeoForge.EVENT_BUS.register(this);

        bus.addListener(this::commonSetup);
        bus.addListener(this::spawnPlacements);
        bus.addListener(this::registerCaps);

        // Register stuff
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModEntities.ENTITY_TYPES.register(bus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(bus);
        MenuInit.MENU_TYPES.register(bus);
        ModEffects.EFFECTS.register(bus);
        ModParticleTypes.PARTICLES.register(bus);
        ModPotions.POTIONS.register(bus);
        ModSounds.SOUNDS.register(bus);
        ModFeatures.FEATURES.register(bus);
        BiomeModifierInit.BIOME_MODIFIER_SERIALIZERS.register(bus);
        CreativeTabInit.ITEM_GROUPS.register(bus);
        ModAttributes.ATTRIBUTES.register(bus);
        CommandInit.ARGUMENTS.register(bus);
        ModArmorMaterials.ARMOR_MATERIALS.register(bus);

        // Setup configs
        WorldSettingsConfig.setup(modContainer);
        ItemSettingsConfig.setup(modContainer);
        MainSettingsConfig.setup(modContainer);
        ClientSettingsConfig.setup(modContainer);
        EntitySettingsConfig.setup(modContainer);

        // Setup JSON data-driven handlers
        bus.addListener((DataPackRegistryEvent.NewRegistry event) ->
        {   event.dataPackRegistry(ModRegistries.FUEL_DATA, FuelData.CODEC);
            event.dataPackRegistry(ModRegistries.FOOD_DATA, FoodData.CODEC);
            event.dataPackRegistry(ModRegistries.INSULATOR_DATA, InsulatorData.CODEC);
            event.dataPackRegistry(ModRegistries.BLOCK_TEMP_DATA, BlockTempData.CODEC);
            event.dataPackRegistry(ModRegistries.BIOME_TEMP_DATA, BiomeTempData.CODEC);
            event.dataPackRegistry(ModRegistries.DIMENSION_TEMP_DATA, DimensionTempData.CODEC);
            event.dataPackRegistry(ModRegistries.STRUCTURE_TEMP_DATA, StructureTempData.CODEC);
            event.dataPackRegistry(ModRegistries.MOUNT_DATA, MountData.CODEC);
            event.dataPackRegistry(ModRegistries.ENTITY_SPAWN_BIOME_DATA, SpawnBiomeData.CODEC);
        });
    }

    public void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() ->
        {   // Register advancement triggers
            CriteriaTriggers.register("temperature_changed", ModAdvancementTriggers.TEMPERATURE_CHANGED);
            CriteriaTriggers.register("soulspring_lamp_fueled", ModAdvancementTriggers.SOUL_LAMP_FUELED);
            CriteriaTriggers.register("block_affects_temperature", ModAdvancementTriggers.BLOCK_AFFECTS_TEMP);
            CriteriaTriggers.register("armor_insulated", ModAdvancementTriggers.ARMOR_INSULATED);
        });

        // Load configs to memory
        ConfigSettings.load(null);
    }

    public void spawnPlacements(SpawnPlacementRegisterEvent event)
    {
        event.register(ModEntities.CHAMELEON.value(), SpawnPlacementTypes.ON_GROUND,
                       Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Chameleon::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    public void registerCaps(RegisterCapabilitiesEvent event)
    {
        // Register fluid handlers for hearth-like blocks
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.HEARTH.value(), (hearth, facing) ->
        {
            return facing == Direction.DOWN
                   ? new HearthBlockEntity.BottomFluidHandler(hearth)
                   : facing != Direction.UP
                     ? new HearthBlockEntity.SidesFluidHandler(hearth)
                     : null;
        });
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.BOILER.value(), (boiler, facing) -> new HearthBlockEntity.BottomFluidHandler(boiler));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.ICEBOX.value(), (icebox, facing) -> new HearthBlockEntity.SidesFluidHandler(icebox));
    }
}
