package com.momosoftworks.coldsweat.config;

import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.registry.LoadRegistriesEvent;
import com.momosoftworks.coldsweat.api.event.vanilla.ServerConfigsLoadedEvent;
import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTempConfig;
import com.momosoftworks.coldsweat.core.init.TempModifierInit;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.RegistryHolder;
import com.momosoftworks.coldsweat.data.codec.configuration.*;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.data.tag.ModDimensionTags;
import com.momosoftworks.coldsweat.data.tag.ModEffectTags;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.RegistryMultiMap;
import com.momosoftworks.coldsweat.util.serialization.OptionalHolder;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class ConfigLoadingHandler
{
    public static final Multimap<ResourceKey<Registry<? extends ConfigData>>, RemoveRegistryData<?>> REMOVED_REGISTRIES = new RegistryMultiMap<>();
    private static final List<OptionalHolder<?>> OPTIONAL_HOLDERS = new ArrayList<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void loadConfigs(ServerConfigsLoadedEvent event)
    {
        ConfigSettings.clear();
        BlockTempRegistry.flush();

        RegistryAccess registryAccess = event.getServer().registryAccess();
        Multimap<ResourceKey<? extends Registry<? extends ConfigData>>, Holder<? extends ConfigData>> registries = new RegistryMultiMap<>();

        fillOptionalHolders(registryAccess);

        // User JSON configs (config folder)
        ColdSweat.LOGGER.info("Loading registries from configs...");
        registries.putAll(collectUserRegistries(registryAccess));

        // JSON configs (data resources)
        ColdSweat.LOGGER.info("Loading registries from data resources...");
        registries.putAll(collectDataRegistries(registryAccess));

        // Load JSON data into the config settings
        logAndAddRegistries(registryAccess, registries);

        // User configs (TOML)
        ColdSweat.LOGGER.info("Loading TOML configs...");
        ConfigSettings.load(registryAccess, false);
        TempModifierInit.buildBlockConfigs();

        // Java BlockTemps
        ColdSweat.LOGGER.info("Loading BlockTemps...");
        TempModifierInit.buildBlockRegistries();
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
    public static Multimap<ResourceKey<Registry<? extends ConfigData>>, Holder<? extends ConfigData>> collectDataRegistries(RegistryAccess registryAccess)
    {
        if (registryAccess == null)
        {   ColdSweat.LOGGER.error("Failed to load registries from null RegistryAccess");
            return new RegistryMultiMap<>();
        }
        /*
         Read mod-related tags for config settings
         */
        ConfigSettings.THERMAL_SOURCE_SPREAD_WHITELIST.get()
                .addAll(registryAccess.registryOrThrow(Registries.BLOCK)
                        .getTag(ModBlockTags.HEARTH_SPREAD_WHITELIST).orElseThrow()
                        .stream().map(holder ->
                        {   ColdSweat.LOGGER.info("Adding block {} to hearth spread whitelist", holder.get());
                            return holder.get();
                        }).toList());

        ConfigSettings.THERMAL_SOURCE_SPREAD_BLACKLIST.get().
                addAll(registryAccess.registryOrThrow(Registries.BLOCK)
                       .getTag(ModBlockTags.HEARTH_SPREAD_BLACKLIST).orElseThrow()
                       .stream().map(holder ->
                       {
                           ColdSweat.LOGGER.info("Adding block {} to hearth spread blacklist", holder.get());
                           return holder.get();
                       }).toList());

        ConfigSettings.SLEEP_CHECK_IGNORE_BLOCKS.get()
                .addAll(registryAccess.registryOrThrow(Registries.BLOCK)
                        .getTag(ModBlockTags.IGNORE_SLEEP_CHECK).orElseThrow()
                        .stream().map(holder ->
                        {   ColdSweat.LOGGER.info("Disabling sleeping conditions check for block {}", holder.get());
                            return holder.get();
                        }).toList());

        ConfigSettings.LAMP_DIMENSIONS.get(registryAccess)
                .addAll(registryAccess.registryOrThrow(Registries.DIMENSION_TYPE)
                        .getTag(ModDimensionTags.SOUL_LAMP_VALID).orElseThrow()
                        .stream().map(holder ->
                        {   ColdSweat.LOGGER.info("Enabling dimension {} for soulspring lamp", holder.value());
                            return holder.value();
                        }).toList());

        ConfigSettings.INSULATION_BLACKLIST.get()
                .addAll(registryAccess.registryOrThrow(Registries.ITEM)
                        .getTag(ModItemTags.NOT_INSULATABLE).orElseThrow()
                        .stream().map(holder ->
                        {   ColdSweat.LOGGER.info("Adding item {} to insulation blacklist", holder.get());
                            return holder.get();
                        }).toList());

        ConfigSettings.HEARTH_POTION_BLACKLIST.get()
                .addAll(registryAccess.registryOrThrow(Registries.MOB_EFFECT)
                        .getTag(ModEffectTags.HEARTH_BLACKLISTED).orElseThrow()
                        .stream().map(holder ->
                        {   ColdSweat.LOGGER.info("Adding effect {} to hearth potion blacklist", holder.get());
                            return holder.get();
                        }).toList());

        /*
         Fetch JSON registries
        */
        Multimap<ResourceKey<Registry<? extends ConfigData>>, Holder<? extends ConfigData>> registries = new RegistryMultiMap<>();
        for (Map.Entry<ResourceLocation, RegistryHolder<?>> entry : ModRegistries.getRegistries().entrySet())
        {
            ResourceKey<Registry<? extends ConfigData>> key = (ResourceKey) entry.getValue().key();
            registries.putAll(key, registryAccess.registryOrThrow(key).holders().toList());
        }
        return registries;
    }

    /**
     * Loads JSON-based configs from the configs folder
     */
    public static Multimap<ResourceKey<Registry<? extends ConfigData>>, Holder<? extends ConfigData>> collectUserRegistries(RegistryAccess registryAccess)
    {
        if (registryAccess == null)
        {   ColdSweat.LOGGER.error("Failed to load registries from null RegistryAccess");
            return new RegistryMultiMap<>();
        }

        /*
         Parse user-defined JSON data from the configs folder
        */
        Multimap<ResourceKey<Registry<? extends ConfigData>>, Holder<? extends ConfigData>> registries = new RegistryMultiMap<>();
        for (Map.Entry<ResourceLocation, RegistryHolder<?>> entry : ModRegistries.getRegistries().entrySet())
        {
            RegistryHolder<?> registry = entry.getValue();
            ResourceKey<Registry<? extends ConfigData>> key = (ResourceKey) registry.key();
            Codec<?> codec = registry.codec();
            registries.putAll(key, parseConfigData(registry, (Codec) codec, registryAccess));
        }
        return registries;
    }

    private static void logAndAddRegistries(RegistryAccess registryAccess, Multimap<ResourceKey<? extends Registry<? extends ConfigData>>, Holder<? extends ConfigData>> registries)
    {
        // Ensure default registry entries load last
        setDefaultRegistryPriority(registries);

        // Load registry removals
        loadRegistryRemovals(registryAccess);

        // Mark holders as "JSON"
        for (Holder<? extends ConfigData> holder : registries.values())
        {   holder.value().setRegistryType(ConfigData.Type.JSON);
        }

        // Fire registry creation event
        LoadRegistriesEvent.Pre event = new LoadRegistriesEvent.Pre(registryAccess, registries, REMOVED_REGISTRIES);
        MinecraftForge.EVENT_BUS.post(event);

        // Remove registries that don't have required loaded mods
        registries.values().removeIf(holder -> !holder.get().areRequiredModsLoaded());

        // Remove registry entries that match removal criteria
        removeRegistries(event.getRegistries());

        /*
         Add JSON data to the config settings
         */
        // insulators
        Collection<Holder<InsulatorData>> insulators = event.getRegistry(ModRegistries.INSULATOR_DATA);
        addInsulatorConfigs(insulators);
        logRegistryLoaded(String.format("Loaded %s insulators", insulators.size()), insulators);
        // fuels
        Collection<Holder<FuelData>> fuels = event.getRegistry(ModRegistries.FUEL_DATA);
        addFuelConfigs(fuels);
        logRegistryLoaded(String.format("Loaded %s fuels", fuels.size()), fuels);
        // foods
        Collection<Holder<FoodData>> foods = event.getRegistry(ModRegistries.FOOD_DATA);
        addFoodConfigs(foods);
        logRegistryLoaded(String.format("Loaded %s foods", foods.size()), foods);
        // item temperatures
        Collection<Holder<ItemTempData>> itemTemps = event.getRegistry(ModRegistries.ITEM_TEMP_DATA);
        addItemTempConfigs(itemTemps);
        logRegistryLoaded(String.format("Loaded %s item temperatures", itemTemps.size()), itemTemps);
        // drying items
        Collection<Holder<DryingItemData>> dryingItems = event.getRegistry(ModRegistries.DRYING_ITEM_DATA);
        addDryingItemConfigs(dryingItems);
        logRegistryLoaded(String.format("Loaded %s drying items", dryingItems.size()), dryingItems);
        // insulation slots
        Collection<Holder<ItemInsulationSlotsData>> insulationSlots = event.getRegistry(ModRegistries.INSULATION_SLOTS_DATA);
        addInsulationSlotConfigs(insulationSlots);
        logRegistryLoaded(String.format("Loaded %s insulation slots configs", insulationSlots.size()), insulationSlots);

        // block temperatures
        Collection<Holder<BlockTempData>> blockTemps = event.getRegistry(ModRegistries.BLOCK_TEMP_DATA);
        addBlockTempConfigs(blockTemps);
        logRegistryLoaded(String.format("Loaded %s block temperatures", blockTemps.size()), blockTemps);
        // biome temperatures
        Collection<Holder<BiomeTempData>> biomeTemps = event.getRegistry(ModRegistries.BIOME_TEMP_DATA);
        addBiomeTempConfigs(biomeTemps, registryAccess);
        logRegistryLoaded(String.format("Loaded %s biome temperatures", biomeTemps.size()), biomeTemps);
        // dimension temperatures
        Collection<Holder<DimensionTempData>> dimensionTemps = event.getRegistry(ModRegistries.DIMENSION_TEMP_DATA);
        addDimensionTempConfigs(dimensionTemps, registryAccess);
        logRegistryLoaded(String.format("Loaded %s dimension temperatures", dimensionTemps.size()), dimensionTemps);
        // structure temperatures
        Collection<Holder<StructureTempData>> structureTemps = event.getRegistry(ModRegistries.STRUCTURE_TEMP_DATA);
        addStructureTempConfigs(structureTemps, registryAccess);
        logRegistryLoaded(String.format("Loaded %s structure temperatures", structureTemps.size()), structureTemps);
        // depth temperatures
        Collection<Holder<DepthTempData>> depthTemps = event.getRegistry(ModRegistries.DEPTH_TEMP_DATA);
        addDepthTempConfigs(depthTemps, registryAccess);
        logRegistryLoaded(String.format("Loaded %s depth temperatures", depthTemps.size()), depthTemps);

        // mounts
        Collection<Holder<MountData>> mounts = event.getRegistry(ModRegistries.MOUNT_DATA);
        addMountConfigs(mounts);
        logRegistryLoaded(String.format("Loaded %s insulated mounts", mounts.size()), mounts);
        // spawn biomes
        Collection<Holder<SpawnBiomeData>> spawnBiomes = event.getRegistry(ModRegistries.ENTITY_SPAWN_BIOME_DATA);
        addSpawnBiomeConfigs(spawnBiomes, registryAccess);
        logRegistryLoaded(String.format("Loaded %s entity spawn biomes", spawnBiomes.size()), spawnBiomes);
        // entity temperatures
        Collection<Holder<EntityTempData>> entityTemps = event.getRegistry(ModRegistries.ENTITY_TEMP_DATA);
        addEntityTempConfigs(entityTemps);
        logRegistryLoaded(String.format("Loaded %s entity temperatures", entityTemps.size()), entityTemps);
        // entity climates
        Collection<Holder<EntityClimateData>> entityClimates = event.getRegistry(ModRegistries.ENTITY_CLIMATE_DATA);
        addEntityClimateConfigs(entityClimates);
        logRegistryLoaded(String.format("Loaded %s entity climates", entityClimates.size()), entityClimates);
        // temp effects
        Collection<Holder<TempEffectsData>> tempEffects = event.getRegistry(ModRegistries.TEMP_EFFECTS_DATA);
        addTempEffectsConfigs(tempEffects);
        logRegistryLoaded(String.format("Loaded %s temp effects", tempEffects.size()), tempEffects);

        LoadRegistriesEvent.Post postEvent = new LoadRegistriesEvent.Post(registryAccess, event.getRegistries());
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
        {
            if (entry instanceof Holder<?> holder)
            {   ColdSweat.LOGGER.info("{}", holder.get());
            }
            else
            {   ColdSweat.LOGGER.info("{}", entry);
            }
        }
    }

    private static void setDefaultRegistryPriority(Multimap<ResourceKey<? extends Registry<? extends ConfigData>>, Holder<? extends ConfigData>> registries)
    {
        for (ResourceKey<? extends Registry<? extends ConfigData>> key : registries.keySet())
        {
            List<Holder<? extends ConfigData>> sortedHolders = new ArrayList<>(registries.get(key));
            sortedHolders.sort(Comparator.comparing(holder ->
            {   return holder.unwrapKey().map(k -> k.location().getPath().startsWith("default") ? 1 : 0).orElse(0);
            }));
            registries.replaceValues(key, sortedHolders);
        }
    }

    private static void loadRegistryRemovals(RegistryAccess registryAccess)
    {
        // Clear the static map
        REMOVED_REGISTRIES.clear();
        // Gather registry removals & add them to the static map
        Set<Holder<RemoveRegistryData<?>>> removals = registryAccess.registryOrThrow(ModRegistries.REMOVE_REGISTRY_DATA.key()).holders().collect(Collectors.toSet());
        removals.addAll(parseConfigData(ModRegistries.REMOVE_REGISTRY_DATA, RemoveRegistryData.CODEC, registryAccess));
        removals.forEach(holder ->
        {
            RemoveRegistryData<?> data = holder.get();
            ResourceKey<Registry<? extends ConfigData>> key = (ResourceKey) data.registry();
            REMOVED_REGISTRIES.put(key, data);
        });
    }

    private static void removeRegistries(Multimap<ResourceKey<? extends Registry<? extends ConfigData>>, Holder<? extends ConfigData>> registries)
    {
        ColdSweat.LOGGER.info("Handling registry removals...");
        for (var entry : REMOVED_REGISTRIES.asMap().entrySet())
        {
            removeEntries((Collection) entry.getValue(), (Collection) registries.get(entry.getKey()));
        }
    }

    private static <T extends ConfigData> void removeEntries(Collection<RemoveRegistryData<T>> removals, Collection<Holder<T>> registry)
    {
        for (RemoveRegistryData<T> data : removals)
        {   registry.removeIf(data::matches);
        }
    }

    public static <T extends ConfigData> Collection<T> removeEntries(Collection<T> registries, RegistryHolder<T> registry)
    {
        REMOVED_REGISTRIES.get((ResourceKey) registry.key()).forEach(data ->
        {
            RemoveRegistryData<T> removeData = ((RemoveRegistryData<T>) data);
            if (removeData.registry() == registry.key())
            {   registries.removeIf(removeData::matches);
            }
        });
        return registries;
    }

    public static <T extends ConfigData> boolean isRemoved(T entry, RegistryHolder<T> registry)
    {
        return REMOVED_REGISTRIES.get((ResourceKey) registry.key()).stream().anyMatch(data -> ((RemoveRegistryData<T>) data).matches(entry));
    }

    public static void addOptionalHolder(OptionalHolder<?> holder)
    {   OPTIONAL_HOLDERS.add(holder);
    }

    public static void fillOptionalHolders(RegistryAccess registryAccess)
    {
        Map<ResourceKey<?>, Registry<?>> registries = new HashMap<>();
        for (int i = 0; i < OPTIONAL_HOLDERS.size(); i++)
        {
            OptionalHolder<?> holder = OPTIONAL_HOLDERS.get(i);
            Registry<?> registry = registries.computeIfAbsent(holder.key(), key -> registryAccess.registry(ResourceKey.createRegistryKey(holder.key().registry())).orElse(null));
            if (registry == null) continue;
            registry.getHolder((ResourceKey) holder.key()).ifPresent(h -> holder.setValue((Holder) h));
        }
    }

    public static void destroyOptionalHolders()
    {   OPTIONAL_HOLDERS.clear();
    }

    private static void addInsulatorConfigs(Collection<Holder<InsulatorData>> insulators)
    {
        insulators.forEach(holder ->
        {
            InsulatorData insulator = holder.get();

            // Add listed items as insulators
            List<Item> items = new ArrayList<>(RegistryHelper.mapForgeRegistryTagList(ForgeRegistries.ITEMS, insulator.item().flatten(ItemRequirement::items)));
            if (items.isEmpty())
            {   items.add(null);
            }

            for (Item item : items)
            {
                switch (insulator.slot())
                {
                    case ITEM -> ConfigSettings.INSULATION_ITEMS.get().put(item, insulator);
                    case ARMOR -> ConfigSettings.INSULATING_ARMORS.get().put(item, insulator);
                    case CURIO ->
                    {
                        if (CompatManager.isCuriosLoaded())
                        {   ConfigSettings.INSULATING_CURIOS.get().put(item, insulator);
                        }
                    }
                }
            }
        });
    }

    private static void addFuelConfigs(Collection<Holder<FuelData>> fuels)
    {
        fuels.forEach(holder ->
        {
            FuelData fuelData = holder.get();

            List<Item> items = new ArrayList<>(RegistryHelper.mapForgeRegistryTagList(ForgeRegistries.ITEMS, fuelData.item().flatten(ItemRequirement::items)));
            if (items.isEmpty())
            {   items.add(null);
            }

            for (Item item : items)
            {
                switch (fuelData.fuelType())
                {
                    case BOILER -> ConfigSettings.BOILER_FUEL.get().put(item, fuelData);
                    case ICEBOX -> ConfigSettings.ICEBOX_FUEL.get().put(item, fuelData);
                    case HEARTH -> ConfigSettings.HEARTH_FUEL.get().put(item, fuelData);
                    case SOUL_LAMP -> ConfigSettings.SOULSPRING_LAMP_FUEL.get().put(item, fuelData);
                }
            }
        });
    }

    private static void addFoodConfigs(Collection<Holder<FoodData>> foods)
    {
        foods.forEach(holder ->
        {
            FoodData foodData = holder.get();

            List<Item> items = new ArrayList<>(RegistryHelper.mapForgeRegistryTagList(ForgeRegistries.ITEMS, foodData.item().flatten(ItemRequirement::items)));
            if (items.isEmpty())
            {   items.add(null);
            }

            for (Item item : items)
            {   ConfigSettings.FOOD_TEMPERATURES.get().put(item, foodData);
            }
        });
    }

    private static void addItemTempConfigs(Collection<Holder<ItemTempData>> itemTemps)
    {
        itemTemps.forEach(holder ->
        {
            ItemTempData itemTempData = holder.get();

            List<Item> items = new ArrayList<>(RegistryHelper.mapForgeRegistryTagList(ForgeRegistries.ITEMS, itemTempData.item().flatten(ItemRequirement::items)));
            if (items.isEmpty())
            {   items.add(null);
            }

            for (Item item : items)
            {   ConfigSettings.ITEM_TEMPERATURES.get().put(item, itemTempData);
            }
        });
    }

    private static void addDryingItemConfigs(Collection<Holder<DryingItemData>> dryingItems)
    {
        dryingItems.forEach(holder ->
        {
            DryingItemData dryingItemData = holder.get();

            List<Item> items = new ArrayList<>(RegistryHelper.mapForgeRegistryTagList(ForgeRegistries.ITEMS, dryingItemData.item().flatten(ItemRequirement::items)));
            if (items.isEmpty())
            {   items.add(null);
            }

            for (Item item : items)
            {   ConfigSettings.DRYING_ITEMS.get().put(item, dryingItemData);
            }
        });
    }

    private static void addInsulationSlotConfigs(Collection<Holder<ItemInsulationSlotsData>> insulationSlots)
    {
        insulationSlots.forEach(holder ->
        {
            ItemInsulationSlotsData insulationSlotData = holder.get();

            List<Item> items = new ArrayList<>(RegistryHelper.mapForgeRegistryTagList(ForgeRegistries.ITEMS, insulationSlotData.item().flatten(ItemRequirement::items)));
            if (items.isEmpty())
            {   items.add(null);
            }

            for (Item item : items)
            {   ConfigSettings.INSULATION_SLOT_OVERRIDES.get().put(item, insulationSlotData);
            }
        });
    }

    private static void addBlockTempConfigs(Collection<Holder<BlockTempData>> blockTemps)
    {
        blockTemps.forEach(holder ->
        {
            BlockTempData blockTempData = holder.get();

            BlockTemp blockTemp = new BlockTempConfig(blockTempData);
            BlockTempRegistry.register(blockTemp);
        });
    }

    private static void addBiomeTempConfigs(Collection<Holder<BiomeTempData>> biomeTemps, RegistryAccess registryAccess)
    {
        biomeTemps.forEach(holder ->
        {
            BiomeTempData biomeTempData = holder.get();

            for (OptionalHolder<Biome> biome : RegistryHelper.mapVanillaRegistryTagList(Registries.BIOME, biomeTempData.biomes(), registryAccess))
            {
                if (biomeTempData.isOffset())
                {   ConfigSettings.BIOME_OFFSETS.get(registryAccess).put(biome.get(), biomeTempData);
                }
                else
                {   ConfigSettings.BIOME_TEMPS.get(registryAccess).put(biome.get(), biomeTempData);
                }
            }
        });
    }

    private static void addDimensionTempConfigs(Collection<Holder<DimensionTempData>> dimensionTemps, RegistryAccess registryAccess)
    {
        dimensionTemps.forEach(holder ->
        {
            DimensionTempData dimensionTempData = holder.get();

            for (OptionalHolder<DimensionType> dimension : RegistryHelper.mapVanillaRegistryTagList(Registries.DIMENSION_TYPE, dimensionTempData.dimensions(), registryAccess))
            {
                if (dimensionTempData.isOffset())
                {   ConfigSettings.DIMENSION_OFFSETS.get(registryAccess).put(dimension.get(), dimensionTempData);
                }
                else
                {   ConfigSettings.DIMENSION_TEMPS.get(registryAccess).put(dimension.get(), dimensionTempData);
                }
            }
        });
    }

    private static void addStructureTempConfigs(Collection<Holder<StructureTempData>> structureTemps, RegistryAccess registryAccess)
    {
        structureTemps.forEach(holder ->
        {
            StructureTempData structureTempData = holder.get();

            for (OptionalHolder<Structure> structure : RegistryHelper.mapVanillaRegistryTagList(Registries.STRUCTURE, structureTempData.structures(), registryAccess))
            {
                if (structureTempData.isOffset())
                {   ConfigSettings.STRUCTURE_OFFSETS.get(registryAccess).put(structure.get(), structureTempData);
                }
                else
                {   ConfigSettings.STRUCTURE_TEMPS.get(registryAccess).put(structure.get(), structureTempData);
                }
            }
        });
    }

    private static void addDepthTempConfigs(Collection<Holder<DepthTempData>> depthTemps, RegistryAccess registryAccess)
    {
        // Add the depth temps to the config
        for (Holder<DepthTempData> holder : depthTemps)
        {
            DepthTempData depthTempData = holder.get();
            for (OptionalHolder<DimensionType> dimension : RegistryHelper.mapVanillaRegistryTagList(Registries.DIMENSION_TYPE, depthTempData.dimensions(), registryAccess))
            {   ConfigSettings.DEPTH_REGIONS.get().put(dimension.get().value(), depthTempData);
            }
        }
    }

    private static void addMountConfigs(Collection<Holder<MountData>> mounts)
    {
        mounts.forEach(holder ->
        {
            MountData mountData = holder.get();

            List<EntityType<?>> entities = new ArrayList<>(RegistryHelper.mapForgeRegistryTagList(ForgeRegistries.ENTITY_TYPES, mountData.entity().flatten(EntityRequirement::entities)));
            if (entities.isEmpty())
            {   entities.add(null);
            }
            for (EntityType<?> entity : entities)
            {   ConfigSettings.INSULATED_MOUNTS.get().put(entity, mountData);
            }
        });
    }

    private static void addSpawnBiomeConfigs(Collection<Holder<SpawnBiomeData>> spawnBiomes, RegistryAccess registryAccess)
    {
        spawnBiomes.forEach(holder ->
        {
            SpawnBiomeData spawnBiomeData = holder.get();

            for (OptionalHolder<Biome> biome : RegistryHelper.mapVanillaRegistryTagList(Registries.BIOME, spawnBiomeData.biomes(), registryAccess))
            {   ConfigSettings.ENTITY_SPAWN_BIOMES.get(registryAccess).put(biome.get(), spawnBiomeData);
            }
        });
    }

    private static void addEntityTempConfigs(Collection<Holder<EntityTempData>> entityTemps)
    {
        entityTemps.forEach(holder ->
        {
            EntityTempData entityTempData = holder.get();

            List<EntityType<?>> entities = new ArrayList<>(RegistryHelper.mapForgeRegistryTagList(ForgeRegistries.ENTITY_TYPES, entityTempData.entity().flatten(EntityRequirement::entities)));
            if (entities.isEmpty())
            {   entities.add(null);
            }
            for (EntityType<?> entity : entities)
            {   ConfigSettings.ENTITY_TEMPERATURES.get().put(entity, entityTempData);
            }
        });
    }

    private static void addEntityClimateConfigs(Collection<Holder<EntityClimateData>> entityTemps)
    {
        entityTemps.forEach(holder ->
        {
            EntityClimateData entityTempData = holder.get();

            List<EntityType<?>> entities = new ArrayList<>(RegistryHelper.mapForgeRegistryTagList(ForgeRegistries.ENTITY_TYPES, entityTempData.entity().flatten(EntityRequirement::entities)));
            if (entities.isEmpty())
            {   entities.add(null);
            }
            for (EntityType<?> entity : entities)
            {   ConfigSettings.ENTITY_CLIMATES.get().put(entity, entityTempData);
            }
        });
    }

    private static void addTempEffectsConfigs(Collection<Holder<TempEffectsData>> tempEffects)
    {
        tempEffects.forEach(holder ->
        {
            TempEffectsData tempEffectsData = holder.get();

            List<EntityType<?>> entities = new ArrayList<>(RegistryHelper.mapForgeRegistryTagList(ForgeRegistries.ENTITY_TYPES, tempEffectsData.entity().flatten(EntityRequirement::entities)));
            if (entities.isEmpty())
            {   entities.add(null);
            }
            for (EntityType<?> entity : entities)
            {   ConfigSettings.ENTITY_TEMP_EFFECTS.get().put(entity, tempEffectsData);
            }
        });
    }

    private static <T extends ConfigData> List<Holder<T>> parseConfigData(RegistryHolder<T> registry, Codec<T> codec, RegistryAccess registryAccess)
    {
        ResourceKey<Registry<T>> registryKey = registry.key();
        List<Holder<T>> output = new ArrayList<>();
        DynamicOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

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
                    codec.decode(registryOps, GsonHelper.parse(reader))
                            .resultOrPartial(ColdSweat.LOGGER::error)
                            .map(Pair::getFirst)
                            .ifPresent(configData -> output.add(Holder.direct(configData)));
                }
                catch (Exception e)
                {   ColdSweat.LOGGER.error("Failed to parse JSON config setting in {}: {}", registryKey.location(), file.getName(), e);
                }
            }
        }
        return output;
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
