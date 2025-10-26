package com.momosoftworks.coldsweat.config;

import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.registry.AddRegistriesEvent;
import com.momosoftworks.coldsweat.api.event.core.registry.LoadRegistriesEvent;
import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.api.temperature.block_temp.ConfiguredBlockTemp;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.core.init.TempModifierInit;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.RegistryHolder;
import com.momosoftworks.coldsweat.data.codec.configuration.*;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.RegistryMultiMap;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.resources.IResource;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
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

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class ConfigLoadingHandler
{
    public static final Multimap<RegistryHolder<?>, RegistryModifierData<?>> REGISTRY_MODIFIERS = new RegistryMultiMap<>();

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
        Multimap<RegistryHolder<?>, ? extends ConfigData> registries = new RegistryMultiMap<>();

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

    private static boolean REGISTRIES_INITIALIZED = false;
    @SubscribeEvent
    public static void initRegistries(FMLServerAboutToStartEvent event)
    {
        if (REGISTRIES_INITIALIZED) return;

        ColdSweat.LOGGER.info("Gathering Cold Sweat registries");
        // Gather modded registries
        AddRegistriesEvent addRegistriesEvent = new AddRegistriesEvent();
        MinecraftForge.EVENT_BUS.start();
        MinecraftForge.EVENT_BUS.post(addRegistriesEvent);

        REGISTRIES_INITIALIZED = true;
    }

    /**
     * Loads JSON-based configs from data resources
     */
    public static Multimap<RegistryHolder<?>, ? extends ConfigData> collectDataRegistries()
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
        Multimap<RegistryHolder<?>, ? extends ConfigData> registries = new RegistryMultiMap<>();
        for (Map.Entry<ResourceLocation, RegistryHolder<?>> entry : ModRegistries.getRegistries().entrySet())
        {
            RegistryHolder<? extends ConfigData> registry = entry.getValue();
            try
            {
                String registryPath = String.format("%s/%s", ColdSweat.MOD_ID, registry.key().location().getPath());
                for (ResourceLocation resourceLocation : ModRegistries.getResourceManager().listResources(registryPath, file -> file.endsWith(".json")))
                {
                    IResource resource = ModRegistries.getResourceManager().getResource(resourceLocation);
                    try (InputStream inputStream = resource.getInputStream())
                    {
                        JsonObject json = JSONUtils.parse(new InputStreamReader(inputStream));
                        String relativePath = resourceLocation.getPath().replace(registryPath, "");
                        relativePath = relativePath.substring(1, relativePath.length() - 5);
                        ResourceLocation registryId = new ResourceLocation(resourceLocation.getNamespace(), relativePath);
                        RegistryKey<? extends ConfigData> registryKey = RegistryKey.create(registry.key(), registryId);
                        // Create a reader from the input stream
                        registry.codec().parse(JsonOps.INSTANCE, json)
                                .resultOrPartial(ColdSweat.LOGGER::error)
                                .ifPresent(data ->
                                {
                                    data.setConfigType(ConfigData.Type.JSON);
                                    data.setRegistryKey(registryKey);
                                    ((RegistryHolder) registry).register(registryId, data);
                                    ((RegistryMultiMap) registries).put(registry, data);
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
    public static Multimap<RegistryHolder<?>, ? extends ConfigData> collectUserRegistries()
    {
        /*
         Parse user-defined JSON data from the configs folder
        */
        Multimap<RegistryHolder<?>, ? extends ConfigData> registries = new RegistryMultiMap<>();
        for (Map.Entry<ResourceLocation, RegistryHolder<?>> entry : ModRegistries.getRegistries().entrySet())
        {
            RegistryHolder<?> registry = entry.getValue();
            registries.putAll(registry, (Collection) parseConfigData(registry));
        }
        return registries;
    }

    private static void logAndAddRegistries(DynamicRegistries registryAccess, Multimap<RegistryHolder<?>, ? extends ConfigData> registries)
    {
        // Ensure default registry entries load last
        setDefaultRegistryPriority(registries);

        // Load registry removals
        loadRegistryModifiers();

        // Mark holders as "JSON"
        for (ConfigData data : registries.values())
        {   data.setConfigType(ConfigData.Type.JSON);
        }

        // Fire pre-registry-loading event
        LoadRegistriesEvent.Pre event = new LoadRegistriesEvent.Pre(registryAccess, registries, REGISTRY_MODIFIERS);
        MinecraftForge.EVENT_BUS.post(event);

        // Remove registries that don't have required loaded mods
        registries.values().removeIf(data -> !data.areRequiredModsLoaded());

        // Remove registry entries that match removal criteria
        modifyRegistries(event.getRegistries());

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
        // item temperatures
        Collection<ItemTempData> itemTemps = event.getRegistry(ModRegistries.ITEM_TEMP_DATA);
        addItemTempConfigs(itemTemps);
        logRegistryLoaded(String.format("Loaded %s item temperatures", itemTemps.size()), itemTemps);
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

        // Fire post-registry-loading event
        LoadRegistriesEvent.Post postEvent = new LoadRegistriesEvent.Post(registryAccess, event.getRegistries());
        MinecraftForge.EVENT_BUS.post(postEvent);
    }

    private static void logRegistryLoaded(String message, Collection<? extends ConfigData> registry)
    {
        if (registry.isEmpty())
        {   message += ".";
        }
        else message += ": [";
        // Print comma-separated registry entries
        StringBuilder messageBuilder = new StringBuilder(message);
        Iterator<? extends ConfigData> iterator = registry.iterator();
        while (iterator.hasNext())
        {
            ConfigData data = iterator.next();
            if (data.registryKey().isPresent())
            {   messageBuilder.append(data.registryKey().get().location());
            }
            if (iterator.hasNext())
            {   messageBuilder.append(", ");
            }
            else messageBuilder.append("]");
        }
        ColdSweat.LOGGER.info(messageBuilder.toString(), registry.size());
        // Print contents of "nameless" registries
        messageBuilder = new StringBuilder("Loaded external entries: ");
        boolean hasNameless = false;
        for (ConfigData data : registry)
        {
            if (!data.registryKey().isPresent())
            {   messageBuilder.append("\n- ").append(data);
                hasNameless = true;
            }
        }
        if (hasNameless)
        {   ColdSweat.LOGGER.debug(messageBuilder.toString());
        }
    }

    private static void setDefaultRegistryPriority(Multimap<RegistryHolder<?>, ? extends ConfigData> registries)
    {
        for (RegistryHolder<?> key : registries.keySet())
        {
            List<? extends ConfigData> sortedHolders = new ArrayList<>(registries.get(key));
            sortedHolders.sort(Comparator.comparing(holder ->
            {   return RegistryHelper.getKey(holder).getPath().startsWith("default") ? 1 : 0;
            }));
            registries.replaceValues(key, (List) sortedHolders);
        }
    }

    private static void loadRegistryModifiers()
    {
        // Clear the static map
        REGISTRY_MODIFIERS.clear();
        // Gather registry removals & add them to the static map
        Collection<RegistryModifierData<?>> removals = ModRegistries.REGISTRY_MODIFIER_DATA.data().values();
        removals.addAll(parseConfigData(ModRegistries.REGISTRY_MODIFIER_DATA));
        removals.forEach(data ->
        {
            RegistryHolder<?> key = ModRegistries.getRegistry(data.registry());
            REGISTRY_MODIFIERS.put(key, data);
        });
        setDefaultRegistryPriority(REGISTRY_MODIFIERS);
    }

    private static void modifyRegistries(Multimap<RegistryHolder<?>, ? extends ConfigData> registries)
    {
        ColdSweat.LOGGER.info("Handling registry modifiers...");
        for (Map.Entry<RegistryHolder<?>, Collection<RegistryModifierData<?>>> entry : REGISTRY_MODIFIERS.asMap().entrySet())
        {
            modifyEntries((Collection) entry.getValue(), registries.get(entry.getKey()));
        }
    }

    private static <T extends ConfigData> void modifyEntries(Collection<RegistryModifierData<T>> modifiers, Collection<T> registries)
    {
        List<T> newRegistries = new ArrayList<>(registries);
        for (RegistryModifierData<T> modifier : modifiers)
        {
            for (int i = 0; i < newRegistries.size(); i++)
            {
                T element = newRegistries.get(i);
                if (modifier.matches(element))
                {
                    T modified = modifier.applyModifications(element);
                    if (modified == null)
                    {
                        newRegistries.remove(i);
                        i--;
                        continue;
                    }
                    newRegistries.set(i, modified);
                }
            }
        }
        registries.clear();
        registries.addAll(newRegistries);
    }

    public static <C, K, V extends ConfigData> void modifyEntries(C registries, RegistryHolder<V> registry, Function<C, Collection<Map.Entry<K, V>>> entryGetter,
                                                                  Consumer<Map.Entry<K, V>> entrySetter, Consumer<Map.Entry<K, V>> entryRemover)
    {
        getRegistryModifiers(registry).forEach(modifier ->
        {
            if (modifier.registry().equals(registry.key()))
            {   for (Map.Entry<K, V> entry : entryGetter.apply(registries))
                {
                    V value = entry.getValue();
                    if (modifier.matches(value))
                    {
                        V modified = modifier.applyModifications(value);
                        if (modified == null)
                        {
                            entryRemover.accept(entry);
                            continue;
                        }
                        entrySetter.accept(new AbstractMap.SimpleEntry<>(entry.getKey(), modified));
                    }
                }
            }
        });
    }

    public static <T extends ConfigData> void modifyEntries(List<T> registries, RegistryHolder<T> registry)
    {
        AtomicInteger index = new AtomicInteger(0);
        modifyEntries(registries, registry, list -> list.stream().map(e -> new AbstractMap.SimpleEntry<>(index.getAndIncrement(), e)).collect(Collectors.toList()),
                      entry -> registries.set(entry.getKey(), entry.getValue()),
                      entry -> registries.remove(entry.getKey().intValue()));
    }

    public static <K, T extends ConfigData> void modifyEntries(Map<K, T> registries, RegistryHolder<T> registry)
    {
        modifyEntries(registries, registry, Map::entrySet,
                      entry -> registries.put(entry.getKey(), entry.getValue()),
                      entry -> registries.remove(entry.getKey()));
    }

    public static <K, T extends ConfigData> void modifyEntries(Multimap<K, T> registries, RegistryHolder<T> registry)
    {
        Map<K, List<T>> tempMap = new HashMap<>();
        for (K key : registries.keySet())
        {
            List<T> modifiedList = new ArrayList<>(registries.get(key));
            modifyEntries(modifiedList, registry);
            tempMap.put(key, modifiedList);
            }
        registries.clear();
        for (Map.Entry<K, List<T>> entry : tempMap.entrySet())
        {   registries.putAll(entry.getKey(), entry.getValue());
        }
    }

    public static <T extends ConfigData> boolean isRemoved(T entry, RegistryHolder<T> registry)
    {   return getRegistryModifiers(registry).stream().anyMatch(data -> data.matches(entry));
    }

    public static <T extends ConfigData> Collection<RegistryModifierData<T>> getRegistryModifiers(RegistryHolder<T> registry)
    {   return (Collection<RegistryModifierData<T>>) (Collection) REGISTRY_MODIFIERS.get(registry);
    }

    private static void addInsulatorConfigs(Collection<InsulatorData> insulators)
    {
        insulators.forEach(insulator ->
        {
            // Add listed items as insulators
            List<Item> items = new ArrayList<>(RegistryHelper.mapTaggableList(insulator.item().flatten(ItemRequirement::items)));
            if (items.isEmpty())
            {   return;
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
            List<Item> items = new ArrayList<>(RegistryHelper.mapTaggableList(fuelData.item().flatten(ItemRequirement::items)));
            if (items.isEmpty())
            {   return;
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
            List<Item> items = new ArrayList<>(RegistryHelper.mapTaggableList(foodData.item().flatten(ItemRequirement::items)));
            if (items.isEmpty())
            {   return;
            }

            for (Item item : items)
            {   ConfigSettings.FOOD_TEMPERATURES.get().put(item, foodData);
            }
        });
    }

    private static void addItemTempConfigs(Collection<ItemTempData> itemTemps)
    {
        itemTemps.forEach(itemTempData ->
        {
            List<Item> items = new ArrayList<>(RegistryHelper.mapTaggableList(itemTempData.item().flatten(ItemRequirement::items)));
            if (items.isEmpty())
            {   return;
            }

            for (Item item : items)
            {   ConfigSettings.ITEM_TEMPERATURES.get().put(item, itemTempData);
            }
        });
    }

    private static void addDryingItemConfigs(Collection<DryingItemData> dryingItems)
    {
        dryingItems.forEach(dryingItemData ->
        {
            List<Item> items = new ArrayList<>(RegistryHelper.mapTaggableList(dryingItemData.item().flatten(ItemRequirement::items)));
            if (items.isEmpty())
            {   return;
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
            List<Item> items = new ArrayList<>(RegistryHelper.mapTaggableList(insulationSlotData.item().flatten(ItemRequirement::items)));
            if (items.isEmpty())
            {   return;
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
            BlockTemp blockTemp = new ConfiguredBlockTemp(blockTempData);
            BlockTempRegistry.register(blockTemp);
        });
    }

    private static void addBiomeTempConfigs(Collection<BiomeTempData> biomeTemps, DynamicRegistries registryAccess)
    {
        biomeTemps.forEach(biomeTempData ->
        {
            for (Biome biome : biomeTempData.biomes().flatList())
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
            for (DimensionType dimension : dimensionTempData.dimensions().flatList())
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
            for (StructureFeature<?, ?> structure : structureTempData.structures().flatList())
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
            List<EntityType<?>> entities = new ArrayList<>(RegistryHelper.mapTaggableList(mountData.entity().flatten(EntityRequirement::entities)));
            if (entities.isEmpty())
            {   return;
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
            for (Biome biome : spawnBiomeData.biomes().flatList())
            {   ConfigSettings.ENTITY_SPAWN_BIOMES.get(registryAccess).put(biome, spawnBiomeData);
            }
        });
    }

    private static void addEntityTempConfigs(Collection<EntityTempData> entityTemps)
    {
        entityTemps.forEach(entityTempData ->
        {
            List<EntityType<?>> entities = new ArrayList<>(RegistryHelper.mapTaggableList(entityTempData.entity().flatten(EntityRequirement::entities)));
            if (entities.isEmpty())
            {   return;
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
            List<EntityType<?>> entities = new ArrayList<>(RegistryHelper.mapTaggableList(entityTempData.entity().flatten(EntityRequirement::entities)));
            if (entities.isEmpty())
            {   return;
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
            List<EntityType<?>> entities = new ArrayList<>(RegistryHelper.mapTaggableList(tempEffectsData.entity().flatten(EntityRequirement::entities)));
            if (entities.isEmpty())
            {   return;
            }
            for (EntityType<?> entity : entities)
            {   ConfigSettings.ENTITY_TEMP_EFFECTS.get().put(entity, tempEffectsData);
            }
        });
    }

    private static <T extends ConfigData> List<T> parseConfigData(RegistryHolder<T> registry)
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
                                configData.setConfigType(ConfigData.Type.JSON);
                                configData.setRegistryKey(RegistryKey.create(registryKey, new ResourceLocation(ColdSweat.MOD_ID, file.getPath())));
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
