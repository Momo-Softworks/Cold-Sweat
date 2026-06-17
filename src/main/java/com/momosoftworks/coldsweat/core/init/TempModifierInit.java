package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.registry.BlockTempRegisterEvent;
import com.momosoftworks.coldsweat.api.event.core.registry.TempModifierRegisterEvent;
import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.*;
import com.momosoftworks.coldsweat.api.temperature.modifier.*;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.config.ConfigLoadingHandler;
import com.momosoftworks.coldsweat.config.spec.WorldSettingsConfig;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.configuration.BlockTempData;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber
public class TempModifierInit
{
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void fireRegisterModifiers(FMLServerAboutToStartEvent event)
    {   buildModifierRegistries();
    }

    // Trigger registry events
    public static void buildModifierRegistries()
    {
        TempModifierRegistry.flush();

        try { MinecraftForge.EVENT_BUS.post(new TempModifierRegisterEvent()); }
        catch (Exception e)
        {
            ColdSweat.LOGGER.error("Registering TempModifiers failed!");
            throw e;
        }
    }

    public static void buildBlockRegistries()
    {
        try { MinecraftForge.EVENT_BUS.post(new BlockTempRegisterEvent()); }
        catch (Exception e)
        {
            ColdSweat.LOGGER.error("Registering BlockTemps failed!");
            throw e;
        }
    }

    public static void buildBlockConfigs()
    {
        // Auto-generate BlockTemps from config
        List<BlockTempData> blockTemps = WorldSettingsConfig.BLOCK_TEMPERATURES.get().stream()
                                         .map(BlockTempData::fromToml)
                                         .filter(Objects::nonNull).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        // Handle entries removed by configs
        ConfigLoadingHandler.modifyEntries(blockTemps, ModRegistries.BLOCK_TEMP_DATA);

        for (BlockTempData blockConfig : blockTemps)
        {
            BlockTemp blockTemp = new ConfiguredBlockTemp(blockConfig);
            BlockTempRegistry.register(blockTemp);
        }
    }

    // Register BlockTemps
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void registerBlockTemps(BlockTempRegisterEvent event)
    {
        long startMS = System.currentTimeMillis();

        event.register(new FurnaceBlockTemp());
        event.register(new NetherPortalBlockTemp());
        if (CompatManager.isCreateLoaded())
        {   event.register(new com.momosoftworks.coldsweat.api.temperature.block_temp.compat.CreateFluidTankTemp());
            event.register(new com.momosoftworks.coldsweat.api.temperature.block_temp.compat.CreateFluidPipeTemp());
        }
        ColdSweat.LOGGER.debug("Registered BlockTemps in {}ms", System.currentTimeMillis() - startMS);
    }

    // Register TempModifiers
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void registerTempModifiers(TempModifierRegisterEvent event)
    {
        long startMS = System.currentTimeMillis();

        event.register(new ResourceLocation(ColdSweat.MOD_ID, "blocks"), BlockTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "biomes"), BiomeTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "shade"), ShadeTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "elevation"), ElevationTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "armor"), ArmorInsulationTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "mount"), MountTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "waterskin"), WaterskinTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "soulspring_lamp"), SoulLampTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "water"), WaterTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "warming"), WarmthTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "cooling"), FrigidnessTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "food"), FoodTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "soul_sprout"), SoulSproutTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "inventory_items"), InventoryItemsTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "entities"), EntitiesTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "acclimation"), AcclimationTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "climate"), EntityClimateTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "simple"), SimpleTempModifier::new);

        // Compat
        if (CompatManager.isSublevelCompatLoaded())
        {   event.register(new ResourceLocation(ColdSweat.MOD_ID, "sublevel_blocks"), SublevelBlockTempModifier::new);
        }
        if (CompatManager.isSereneSeasonsLoaded())
        {   event.register(new ResourceLocation("sereneseasons", "season"), () -> new com.momosoftworks.coldsweat.api.temperature.modifier.compat.SereneSeasonsTempModifier());
        }
        if (CompatManager.isWeather2Loaded())
        {   event.register(new ResourceLocation("weather2", "storm"), () -> new com.momosoftworks.coldsweat.api.temperature.modifier.compat.StormTempModifier());
        }
        if (CompatManager.isCuriosLoaded())
        {   event.register(new ResourceLocation("curios", "curios"), () -> new com.momosoftworks.coldsweat.api.temperature.modifier.compat.CuriosTempModifier());
        }
        if (CompatManager.isBetterWeatherLoaded())
        {   event.register(new ResourceLocation("betterweather", "season"), () -> new com.momosoftworks.coldsweat.api.temperature.modifier.compat.BetterWeatherTempModifier());
        }

        ColdSweat.LOGGER.debug("Registered TempModifiers in {}ms", System.currentTimeMillis() - startMS);
    }
}
