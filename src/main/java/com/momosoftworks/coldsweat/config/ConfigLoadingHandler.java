package com.momosoftworks.coldsweat.config;

import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.registry.CreateRegistriesEvent;
import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTempConfig;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.core.init.TempModifierInit;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.configuration.*;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.LocationRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.RegistryMultiMap;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.resources.IResource;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import javax.xml.ws.Holder;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class ConfigLoadingHandler
{
    public static final Multimap<RegistryKey<Registry<? extends ConfigData>>, RemoveRegistryData<?>> REMOVED_REGISTRIES = new RegistryMultiMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void loadConfigs(FMLServerAboutToStartEvent event)
    {
        long startTime = System.nanoTime();
        ConfigSettings.clear();
        BlockTempRegistry.flush();
        ModRegistries.getRegistries().forEach((registryKey, registry) ->
        {   registry.flush();
        });

        DynamicRegistries registryAccess = event.getServer().registryAccess();
        Multimap<RegistryKey<? extends Registry<? extends ConfigData>>, ? extends ConfigData> registries = new RegistryMultiMap<>();

        // User JSON configs (config folder)
        ColdSweat.LOGGER.info("Loading registries from configs...");
        registries.putAll((Multimap) collectUserRegistries());

        // JSON configs (data resources)
        ColdSweat.LOGGER.info("Loading registries from data resources...");
        registries.putAll((Multimap) collectDataRegistries());

        // Load JSON data into the config settings
        logAndAddRegistries(registryAccess, registries);

        // User configs (TOML)
        ColdSweat.LOGGER.info("Loading TOML configs...");
        ConfigSettings.load(registryAccess, false);
        TempModifierInit.buildBlockConfigs();

        // Java BlockTemps
        ColdSweat.LOGGER.info("Loading BlockTemps...");
        TempModifierInit.buildBlockRegistries();

        long endTime = System.nanoTime();
        ColdSweat.LOGGER.info("Loaded mod registries in {}ms", CSMath.truncate((endTime - startTime) / 1_000_000.0, 1));
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ClientConfigs
    {
        @SubscribeEvent
        public static void loadClientConfigs(FMLLoadCompleteEvent event)
        {   ConfigSettings.CLIENT_SETTINGS.forEach((id, holder) -> holder.load(true));
        }
    }

    /**
     * Loads JSON-based configs from data resources
     */
    public static Multimap<RegistryKey<Registry<? extends ConfigData>>, ? extends ConfigData> collectDataRegistries()
    {
        /*
         Add blocks from tags to configs
         */
        ConfigSettings.THERMAL_SOURCE_SPREAD_WHITELIST.get().addAll(ModBlockTags.HEARTH_SPREAD_WHITELIST.getValues().stream().peek(holder ->
                                                           {   ColdSweat.LOGGER.debug("Adding block {} to hearth spread whitelist", holder);
                                                           }).collect(Collectors.toSet()));
        ConfigSettings.THERMAL_SOURCE_SPREAD_BLACKLIST.get().addAll(ModBlockTags.HEARTH_SPREAD_BLACKLIST.getValues().stream().peek(holder ->
                                                           {   ColdSweat.LOGGER.debug("Adding block {} to hearth spread blacklist", holder);
                                                           }).collect(Collectors.toSet()));
        ConfigSettings.SLEEP_CHECK_IGNORE_BLOCKS.get().addAll(ModBlockTags.IGNORE_SLEEP_CHECK.getValues().stream().peek(holder ->
                                                           {   ColdSweat.LOGGER.debug("Disabling sleeping conditions check for block {}", holder);
                                                           }).collect(Collectors.toSet()));
        ConfigSettings.INSULATION_BLACKLIST.get().addAll(ModItemTags.NOT_INSULATABLE.getValues().stream().peek(holder ->
                                                           {   ColdSweat.LOGGER.debug("Adding item {} to insulation blacklist", holder);
                                                           }).collect(Collectors.toSet()));

        /*
         Fetch JSON registries
        */
        Multimap<RegistryKey<Registry<? extends ConfigData>>, ? extends ConfigData> registries = new RegistryMultiMap<>();
        for (ModRegistries.ConfigRegistry<?> registry : ModRegistries.getRegistries().values())
        {
            try
            {
                String registryPath = "config/" + registry.key().location().getPath();
                for (ResourceLocation resourceLocation : ModRegistries.getResourceManager().listResources(registryPath, file -> file.endsWith(".json")))
                {
                    IResource resource = ModRegistries.getResourceManager().getResource(resourceLocation);
                    try (InputStream inputStream = resource.getInputStream())
                    {
                        JsonObject json = JSONUtils.parse(new InputStreamReader(inputStream));
                        String relativePath = resourceLocation.getPath().replace(registryPath, "");
                        relativePath = relativePath.substring(1, relativePath.length() - 5);
                        ResourceLocation registryId = new ResourceLocation(resourceLocation.getNamespace(), relativePath);
                        // Create a reader from the input stream
                        registry.codec().parse(JsonOps.INSTANCE, json)
                                .resultOrPartial(ColdSweat.LOGGER::error)
                                .ifPresent(data ->
                                {
                                    data.setRegistryType(ConfigData.Type.JSON);
                                    data.setRegistryId(registryId);
                                    ((ModRegistries.ConfigRegistry) registry).register(registryId, data);
                                    ((RegistryMultiMap) registries).put(registry.key(), data);
                                });
                    }
                    catch (Exception e)
                    {   ColdSweat.LOGGER.error("Failed to load JSON registry: {}", registry.key(), e);
                    }
                }
            }
            catch (IOException ignored) {}
        }
        return registries;
    }

    /**
     * Loads JSON-based configs from the configs folder
     */
    public static Multimap<RegistryKey<Registry<? extends ConfigData>>, ? extends ConfigData> collectUserRegistries()
    {
        /*
         Parse user-defined JSON data from the configs folder
        */
        Multimap<RegistryKey<Registry<? extends ConfigData>>, ? extends ConfigData> registries = new RegistryMultiMap<>();
        for (Map.Entry<String, ModRegistries.ConfigRegistry<?>> entry : ModRegistries.getRegistries().entrySet())
        {
            ModRegistries.ConfigRegistry<? extends ConfigData> registry = entry.getValue();
            RegistryKey key = registry.key();
            registries.putAll(key, (Collection) parseConfigData(registry));
        }
        return registries;
    }

    private static void logAndAddRegistries(DynamicRegistries registryAccess, Multimap<RegistryKey<? extends Registry<? extends ConfigData>>, ? extends ConfigData> registries)
    {
        // Ensure default registry entries load last
        setDefaultRegistryPriority(registries, registryAccess);

        // Load registry removals
        loadRegistryRemovals();

        // Mark holders as "JSON"
        for (ConfigData data : registries.values())
        {   data.setRegistryType(ConfigData.Type.JSON);
        }

        // Fire registry creation event
        CreateRegistriesEvent.Pre event = new CreateRegistriesEvent.Pre(registryAccess, registries, REMOVED_REGISTRIES);
        MinecraftForge.EVENT_BUS.post(event);

        // Remove registries that don't have required loaded mods
        registries.values().removeIf(data -> !data.areRequiredModsLoaded());

        // Remove registry entries that match removal criteria
        removeRegistries(event.getRegistries());

        /*
         Add JSON data to the config settings
         */
        // insulators
        Collection<InsulatorData> insulators = event.getRegistry(ModRegistries.INSULATOR_DATA);
        addInsulatorConfigs(insulators);
        logRegistryLoaded(String.format("Loaded %s insulators", insulators.size()), insulators);
        // fuels
        Collection<FuelData> fuels = event.getRegistry(ModRegistries.FUEL_DATA);
        addFuelConfigs(fuels);
        logRegistryLoaded(String.format("Loaded %s fuels", fuels.size()), fuels);
        // foods
        Collection<FoodData> foods = event.getRegistry(ModRegistries.FOOD_DATA);
        addFoodConfigs(foods);
        logRegistryLoaded(String.format("Loaded %s foods", foods.size()), foods);
        // carry temperatures
        Collection<ItemCarryTempData> carryTemps = event.getRegistry(ModRegistries.CARRY_TEMP_DATA);
        addCarryTempConfigs(carryTemps);
        logRegistryLoaded(String.format("Loaded %s carried item temperatures", carryTemps.size()), carryTemps);
        // drying items
        Collection<DryingItemData> dryingItems = event.getRegistry(ModRegistries.DRYING_ITEM_DATA);
        addDryingItemConfigs(dryingItems);
        logRegistryLoaded(String.format("Loaded %s drying items", dryingItems.size()), dryingItems);
        // insulation slots
        Collection<ItemInsulationSlotsData> insulationSlots = event.getRegistry(ModRegistries.INSULATION_SLOTS_DATA);
        addInsulationSlotConfigs(insulationSlots);
        logRegistryLoaded(String.format("Loaded %s insulation slots configs", insulationSlots.size()), insulationSlots);

        // block temperatures
        Collection<BlockTempData> blockTemps = event.getRegistry(ModRegistries.BLOCK_TEMP_DATA);
        addBlockTempConfigs(blockTemps);
        logRegistryLoaded(String.format("Loaded %s block temperatures", blockTemps.size()), blockTemps);
        // biome temperatures
        Collection<BiomeTempData> biomeTemps = event.getRegistry(ModRegistries.BIOME_TEMP_DATA);
        addBiomeTempConfigs(biomeTemps, registryAccess);
        logRegistryLoaded(String.format("Loaded %s biome temperatures", biomeTemps.size()), biomeTemps);
        // dimension temperatures
        Collection<DimensionTempData> dimensionTemps = event.getRegistry(ModRegistries.DIMENSION_TEMP_DATA);
        addDimensionTempConfigs(dimensionTemps, registryAccess);
        logRegistryLoaded(String.format("Loaded %s dimension temperatures", dimensionTemps.size()), dimensionTemps);
        // structure temperatures
        Collection<StructureTempData> structureTemps = event.getRegistry(ModRegistries.STRUCTURE_TEMP_DATA);
        addStructureTempConfigs(structureTemps, registryAccess);
        logRegistryLoaded(String.format("Loaded %s structure temperatures", structureTemps.size()), structureTemps);
        // depth temperatures
        Collection<DepthTempData> depthTemps = event.getRegistry(ModRegistries.DEPTH_TEMP_DATA);
        addDepthTempConfigs(depthTemps);
        logRegistryLoaded(String.format("Loaded %s depth temperatures", depthTemps.size()), depthTemps);

        // mounts
        Collection<MountData> mounts = event.getRegistry(ModRegistries.MOUNT_DATA);
        addMountConfigs(mounts);
        logRegistryLoaded(String.format("Loaded %s insulated mounts", mounts.size()), mounts);
        // spawn biomes
        Collection<SpawnBiomeData> spawnBiomes = event.getRegistry(ModRegistries.ENTITY_SPAWN_BIOME_DATA);
        addSpawnBiomeConfigs(spawnBiomes, registryAccess);
        logRegistryLoaded(String.format("Loaded %s entity spawn biomes", spawnBiomes.size()), spawnBiomes);
        // entity temperatures
        Collection<EntityTempData> entityTemps = event.getRegistry(ModRegistries.ENTITY_TEMP_DATA);
        addEntityTempConfigs(entityTemps);
        logRegistryLoaded(String.format("Loaded %s entity temperatures", entityTemps.size()), entityTemps);
        // entity climates
        Collection<EntityClimateData> entityClimates = event.getRegistry(ModRegistries.ENTITY_CLIMATE_DATA);
        addEntityClimateConfigs(entityClimates);
        logRegistryLoaded(String.format("Loaded %s entity climates", entityClimates.size()), entityClimates);
        // temp effects
        Collection<TempEffectsData> tempEffects = event.getRegistry(ModRegistries.TEMP_EFFECTS_DATA);
        addTempEffectsConfigs(tempEffects);
        logRegistryLoaded(String.format("Loaded %s temp effects", tempEffects.size()), tempEffects);

        CreateRegistriesEvent.Post postEvent = new CreateRegistriesEvent.Post(registryAccess, event.getRegistries());
        MinecraftForge.EVENT_BUS.post(postEvent);
    }

    private static void logRegistryLoaded(String message, Collection<?> registry)
    {
        if (registry.isEmpty())
        {   message += ".";
        }
        else message += ":";
        ColdSweat.LOGGER.info(message, registry.size());
        if (registry.isEmpty())
        {   return;
        }
        for (Object entry : registry)
        {   ColdSweat.LOGGER.info("{}", entry);
        }
    }

    private static void setDefaultRegistryPriority(Multimap<RegistryKey<? extends Registry<? extends ConfigData>>, ? extends ConfigData> registries, DynamicRegistries dynamicRegistries)
    {
        for (RegistryKey<? extends Registry<? extends ConfigData>> key : registries.keySet())
        {
            List<? extends ConfigData> sortedHolders = new ArrayList<>(registries.get(key));
            sortedHolders.sort(Comparator.comparing(holder ->
            {   return RegistryHelper.getKey(holder).getPath().startsWith("default") ? 1 : 0;
            }));
            registries.replaceValues(key, (Iterable) sortedHolders);
        }
    }

    private static void loadRegistryRemovals()
    {
        // Clear the static map
        REMOVED_REGISTRIES.clear();
        // Gather registry removals & add them to the static map
        Collection<RemoveRegistryData<?>> removals = ModRegistries.REMOVE_REGISTRY_DATA.data().values();
        removals.addAll(parseConfigData(ModRegistries.REMOVE_REGISTRY_DATA));
        removals.forEach(data ->
        {
            RegistryKey<Registry<? extends ConfigData>> key = (RegistryKey) data.registry();
            REMOVED_REGISTRIES.put(key, data);
        });
    }

    private static void removeRegistries(Multimap<RegistryKey<? extends Registry<? extends ConfigData>>, ? extends ConfigData> registries)
    {
        ColdSweat.LOGGER.info("Handling registry removals...");
        for (Map.Entry entry : REMOVED_REGISTRIES.asMap().entrySet())
        {
            removeEntries((Collection) entry.getValue(), registries.get((RegistryKey) entry.getKey()));
        }
    }

    private static <T extends ConfigData> void removeEntries(Collection<RemoveRegistryData<T>> removals, Collection<T> registry)
    {
        for (RemoveRegistryData<T> data : removals)
        {   registry.removeIf(data::matches);
        }
    }

    public static <T extends ConfigData> Collection<T> removeEntries(Collection<T> registries, ModRegistries.ConfigRegistry<T> registry)
    {
        REMOVED_REGISTRIES.get((RegistryKey) registry.key()).forEach(data ->
        {
            RemoveRegistryData<T> removeData = ((RemoveRegistryData<T>) data);
            if (removeData.registry().equals(registry.key()))
            {   registries.removeIf(removeData::matches);
            }
        });
        return registries;
    }

    public static <T extends ConfigData> boolean isRemoved(T entry, ModRegistries.ConfigRegistry<T> registry)
    {
        return REMOVED_REGISTRIES.get((RegistryKey) registry.key()).stream().anyMatch(data -> ((RemoveRegistryData<T>) data).matches(entry));
    }

    private static void addInsulatorConfigs(Collection<InsulatorData> insulators)
    {
        insulators.forEach(insulator ->
        {
            // Add listed items as insulators
            List<Item> items = new ArrayList<>(RegistryHelper.mapTaggableList(insulator.item().nestedFlatMap(ItemRequirement::items)));
            if (items.isEmpty())
            {   items.add(null);
            }

            for (Item item : items)
            {
                switch (insulator.slot())
                {
                    case ITEM  : ConfigSettings.INSULATION_ITEMS.get().put(item, insulator); break;
                    case ARMOR : ConfigSettings.INSULATING_ARMORS.get().put(item, insulator); break;
                    case CURIO :
                    {
                        if (CompatManager.isCuriosLoaded())
                        {   ConfigSettings.INSULATING_CURIOS.get().put(item, insulator);
                        }
                        break;
                    }
                }
            }
        });
    }

    private static void addFuelConfigs(Collection<FuelData> fuels)
    {
        fuels.forEach(fuelData ->
        {
            List<Item> items = new ArrayList<>(RegistryHelper.mapTaggableList(fuelData.item().nestedFlatMap(ItemRequirement::items)));
            if (items.isEmpty())
            {   items.add(null);
            }

            for (Item item : items)
            {
                switch (fuelData.fuelType())
                {
                    case BOILER : ConfigSettings.BOILER_FUEL.get().put(item, fuelData); break;
                    case ICEBOX : ConfigSettings.ICEBOX_FUEL.get().put(item, fuelData); break;
                    case HEARTH : ConfigSettings.HEARTH_FUEL.get().put(item, fuelData); break;
                    case SOUL_LAMP : ConfigSettings.SOULSPRING_LAMP_FUEL.get().put(item, fuelData); break;
                }
            }
        });
    }

    private static void addFoodConfigs(Collection<FoodData> foods)
    {
        foods.forEach(foodData ->
        {
            List<Item> items = new ArrayList<>(RegistryHelper.mapTaggableList(foodData.item().nestedFlatMap(ItemRequirement::items)));
            if (items.isEmpty())
            {   items.add(null);
            }

            for (Item item : items)
            {   ConfigSettings.FOOD_TEMPERATURES.get().put(item, foodData);
            }
        });
    }

    private static void addCarryTempConfigs(Collection<ItemCarryTempData> carryTemps)
    {
        carryTemps.forEach(carryTempData ->
        {
            List<Item> items = new ArrayList<>(RegistryHelper.mapTaggableList(carryTempData.item().nestedFlatMap(ItemRequirement::items)));
            if (items.isEmpty())
            {   items.add(null);
            }

            for (Item item : items)
            {   ConfigSettings.CARRIED_ITEM_TEMPERATURES.get().put(item, carryTempData);
            }
        });
    }

    private static void addDryingItemConfigs(Collection<DryingItemData> dryingItems)
    {
        dryingItems.forEach(dryingItemData ->
        {
            List<Item> items = new ArrayList<>(RegistryHelper.mapTaggableList(dryingItemData.item().nestedFlatMap(ItemRequirement::items)));
            if (items.isEmpty())
            {   items.add(null);
            }

            for (Item item : items)
            {   ConfigSettings.DRYING_ITEMS.get().put(item, dryingItemData);
            }
        });
    }

    private static void addInsulationSlotConfigs(Collection<ItemInsulationSlotsData> insulationSlots)
    {
        insulationSlots.forEach(insulationSlotData ->
        {
            List<Item> items = new ArrayList<>(RegistryHelper.mapTaggableList(insulationSlotData.item().nestedFlatMap(ItemRequirement::items)));
            if (items.isEmpty())
            {   items.add(null);
            }

            for (Item item : items)
            {   ConfigSettings.INSULATION_SLOT_OVERRIDES.get().put(item, insulationSlotData);
            }
        });
    }

    private static void addBlockTempConfigs(Collection<BlockTempData> blockTemps)
    {
        blockTemps.forEach(blockTempData ->
        {
            BlockTemp blockTemp = new BlockTempConfig(blockTempData)
            {
                final double temperature = blockTempData.getTemperature();
                final NegatableList<LocationRequirement> locationRequirement = blockTempData.location();
                final NegatableList<EntityRequirement> entityRequirement = blockTempData.entity();

                @Override
                public double getTemperature(World level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
                {
                    if (locationRequirement.test(req -> req.test(level, pos))
                    && entityRequirement.test(req -> req.test(entity)))
                    {   return temperature;
                    }
                    return 0;
                }
            };

            BlockTempRegistry.register(blockTemp);
        });
    }

    private static void addBiomeTempConfigs(Collection<BiomeTempData> biomeTemps, DynamicRegistries registryAccess)
    {
        biomeTemps.forEach(biomeTempData ->
        {
            for (Biome biome : biomeTempData.biomes().flatten())
            {
                if (biomeTempData.isOffset())
                {   ConfigSettings.BIOME_OFFSETS.get(registryAccess).put(biome, biomeTempData);
                }
                else
                {   ConfigSettings.BIOME_TEMPS.get(registryAccess).put(biome, biomeTempData);
                }
            }
        });
    }

    private static void addDimensionTempConfigs(Collection<DimensionTempData> dimensionTemps, DynamicRegistries registryAccess)
    {
        dimensionTemps.forEach(dimensionTempData ->
        {
            for (DimensionType dimension : dimensionTempData.dimensions().flatten())
            {
                if (dimensionTempData.isOffset())
                {   ConfigSettings.DIMENSION_OFFSETS.get(registryAccess).put(dimension, dimensionTempData);
                }
                else
                {   ConfigSettings.DIMENSION_TEMPS.get(registryAccess).put(dimension, dimensionTempData);
                }
            }
        });
    }

    private static void addStructureTempConfigs(Collection<StructureTempData> structureTemps, DynamicRegistries registryAccess)
    {
        structureTemps.forEach(structureTempData ->
        {
            for (StructureFeature<?, ?> structure : structureTempData.structures().flatten())
            {
                if (structureTempData.isOffset())
                {   ConfigSettings.STRUCTURE_OFFSETS.get(registryAccess).put(structure, structureTempData);
                }
                else
                {   ConfigSettings.STRUCTURE_TEMPS.get(registryAccess).put(structure, structureTempData);
                }
            }
        });
    }

    private static void addDepthTempConfigs(Collection<DepthTempData> depthTemps)
    {
        // Add the depth temps to the config
        for (DepthTempData depthData : depthTemps)
        {   ConfigSettings.DEPTH_REGIONS.get().add(depthData);
        }
    }

    private static void addMountConfigs(Collection<MountData> mounts)
    {
        mounts.forEach(mountData ->
        {
            List<EntityType<?>> entities = new ArrayList<>(RegistryHelper.mapTaggableList(mountData.entity().nestedFlatMap(EntityRequirement::entities)));
            if (entities.isEmpty())
            {   entities.add(null);
            }
            for (EntityType<?> entity : entities)
            {   ConfigSettings.INSULATED_MOUNTS.get().put(entity, mountData);
            }
        });
    }

    private static void addSpawnBiomeConfigs(Collection<SpawnBiomeData> spawnBiomes, DynamicRegistries registryAccess)
    {
        spawnBiomes.forEach(spawnBiomeData ->
        {
            for (Biome biome : spawnBiomeData.biomes().flatten())
            {   ConfigSettings.ENTITY_SPAWN_BIOMES.get(registryAccess).put(biome, spawnBiomeData);
            }
        });
    }

    private static void addEntityTempConfigs(Collection<EntityTempData> entityTemps)
    {
        entityTemps.forEach(entityTempData ->
        {
            List<EntityType<?>> entities = new ArrayList<>(RegistryHelper.mapTaggableList(entityTempData.entity().nestedFlatMap(EntityRequirement::entities)));
            if (entities.isEmpty())
            {   entities.add(null);
            }
            for (EntityType<?> entity : entities)
            {   ConfigSettings.ENTITY_TEMPERATURES.get().put(entity, entityTempData);
            }
        });
    }

    private static void addEntityClimateConfigs(Collection<EntityClimateData> entityTemps)
    {
        entityTemps.forEach(entityTempData ->
        {
            List<EntityType<?>> entities = new ArrayList<>(RegistryHelper.mapTaggableList(entityTempData.entity().nestedFlatMap(EntityRequirement::entities)));
            if (entities.isEmpty())
            {   entities.add(null);
            }
            for (EntityType<?> entity : entities)
            {   ConfigSettings.ENTITY_CLIMATES.get().put(entity, entityTempData);
            }
        });
    }

    private static void addTempEffectsConfigs(Collection<TempEffectsData> tempEffects)
    {
        tempEffects.forEach(tempEffectsData ->
        {
            List<EntityType<?>> entities = new ArrayList<>(RegistryHelper.mapTaggableList(tempEffectsData.entity().nestedFlatMap(EntityRequirement::entities)));
            if (entities.isEmpty())
            {   entities.add(null);
            }
            for (EntityType<?> entity : entities)
            {   ConfigSettings.ENTITY_TEMP_EFFECTS.get().put(entity, tempEffectsData);
            }
        });
    }

    private static <T extends ConfigData> List<T> parseConfigData(ModRegistries.ConfigRegistry<T> registry)
    {
        RegistryKey<Registry<T>> registryKey = registry.key();
        List<T> output = new ArrayList<>();
        DynamicOps<JsonElement> registryOps = JsonOps.INSTANCE;

        String configFolder = registryKey.location().getNamespace().replace("_", "");
        Path coldSweatDataPath = FMLPaths.CONFIGDIR.get().resolve(configFolder + "/data").resolve(registryKey.location().getPath());
        File jsonDirectory = coldSweatDataPath.toFile();

        if (!jsonDirectory.exists())
        {   return output;
        }
        else for (File file : findFilesRecursive(jsonDirectory))
        {
            if (file.getName().endsWith(".json"))
            {
                try (FileReader reader = new FileReader(file))
                {
                    JsonObject json = JSONUtils.parse(reader);
                    if (!shouldLoadJSON(registryKey, file.getPath(), json))
                    {   continue;
                    }
                    registry.codec().decode(registryOps, JSONUtils.parse(reader))
                            .resultOrPartial(ColdSweat.LOGGER::error)
                            .map(Pair::getFirst)
                            .ifPresent(configData ->
                            {
                                configData.setRegistryType(ConfigData.Type.JSON);
                                configData.setRegistryId(new ResourceLocation(ColdSweat.MOD_ID, file.getPath()));
                                output.add(configData);
                            });
                }
                catch (Exception e)
                {   ColdSweat.LOGGER.error("Failed to parse JSON config setting in {}: {}", registryKey.location(), file.getName(), e);
                }
            }
        }
        return output;
    }

    private static boolean shouldLoadJSON(RegistryKey registryKey, String elementName, JsonObject json)
    {
        if (json.has("required_mods"))
        {
            JsonArray requiredMods = new JsonArray();
            JsonArray excludedMods = new JsonArray();
            JsonElement requiredModField = json.get("required_mods");
            if (requiredModField.isJsonArray())
            {
                requiredMods = requiredModField.getAsJsonArray();
            }
            else
            {
                JsonObject requiredModCompound = requiredModField.getAsJsonObject();
                if (requiredModCompound.has("require"))
                {   requiredMods = requiredModCompound.getAsJsonArray("require");
                }
                if (requiredModCompound.has("exclude"))
                {   excludedMods = requiredModCompound.getAsJsonArray("exclude");
                }
            }
            for (JsonElement requiredMod : requiredMods)
            {
                if (!CompatManager.modLoaded(requiredMod.getAsString()))
                {   ColdSweat.LOGGER.warn("Skipping registration of {} {}: missing mod \"{}\"", registryKey.location(), elementName, requiredMod.getAsString());
                    return true;
                }
            }
            for (JsonElement excludedMod : excludedMods)
            {
                if (CompatManager.modLoaded(excludedMods.getAsString()))
                {   ColdSweat.LOGGER.warn("Skipping registration of {} {}: disallowed mod \"{}\" is loaded", registryKey.location(), elementName, excludedMod.getAsString());
                    return true;
                }
            }
        }
        return true;
    }

    public static List<File> findFilesRecursive(File directory)
    {
        List<File> files = new ArrayList<>();
        File[] filesInDirectory = directory.listFiles();
        if (filesInDirectory == null)
        {   return files;
        }
        for (File file : filesInDirectory)
        {
            if (file.isDirectory())
            {   files.addAll(findFilesRecursive(file));
            }
            else
            {   files.add(file);
            }
        }
        return files;
    }
}
