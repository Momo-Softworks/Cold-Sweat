package com.momosoftworks.coldsweat.config;

import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.registry.AddRegistriesEvent;
import com.momosoftworks.coldsweat.api.event.core.registry.LoadRegistriesEvent;
import com.momosoftworks.coldsweat.api.event.vanilla.ProbeEventBusEvent;
import com.momosoftworks.coldsweat.api.event.vanilla.ServerConfigsLoadedEvent;
import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.api.temperature.block_temp.ConfiguredBlockTemp;
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
import com.momosoftworks.coldsweat.util.math.RegistryMultiMap;
import com.momosoftworks.coldsweat.util.serialization.OptionalHolder;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
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
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class ConfigLoadingHandler
{
    public static final Multimap<RegistryHolder<?>, Holder<RegistryModifierData<?>>> REGISTRY_MODIFIERS = new RegistryMultiMap<>();
    private static final List<OptionalHolder<?>> OPTIONAL_HOLDERS = new ArrayList<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void loadConfigs(ServerConfigsLoadedEvent event)
    {
        ConfigSettings.clear();
        BlockTempRegistry.flush();

        RegistryAccess registryAccess = event.getServer().registryAccess();
        Multimap<RegistryHolder<?>, Holder<? extends ConfigData>> registries = new RegistryMultiMap<>();

        // User JSON configs (config folder)
        ColdSweat.LOGGER.info("Loading registries from configs...");
        registries.putAll(collectUserRegistries(registryAccess));

        // JSON configs (data resources)
        ColdSweat.LOGGER.info("Loading registries from data resources...");
        registries.putAll(collectDataRegistries(registryAccess));

        fillOptionalHolders(registryAccess);

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

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class CreateRegistries
    {
        private static boolean REGISTRIES_INITIALIZED = false;

        @SubscribeEvent
        public static void initRegistries(FMLConstructModEvent event)
        {
            event.enqueueWork(() ->
            {
                if (REGISTRIES_INITIALIZED) return;

                ColdSweat.LOGGER.info("Gathering Cold Sweat registries");
                // Gather modded registries
                AddRegistriesEvent addRegistriesEvent = new AddRegistriesEvent();

                // Check if the event bus is closed
                boolean busWasClosed = !MinecraftForge.EVENT_BUS.post(new ProbeEventBusEvent());
                // Start the event bus temporarily
                MinecraftForge.EVENT_BUS.start();
                // Post AddRegistriesEvent to the event bus
                MinecraftForge.EVENT_BUS.post(addRegistriesEvent);
                // Re-close the event bus if it was closed before
                if (busWasClosed)
                {   ColdSweat.LOGGER.info("The event bus was started early to gather Cold Sweat registries; it will now be stopped again until the correct loading phase.");
                    MinecraftForge.EVENT_BUS.shutdown();
                }

                // Add registries via dummy NewRegistry event
                NewRegistryEvent dummyEvent = new NewRegistryEvent();
                for (RegistryHolder holder : ModRegistries.getRegistries().values())
                {   dummyEvent.create(new RegistryBuilder<>().setName(holder.key().location()).dataPackRegistry(holder.codec(), holder.codec()));
                }
                try
                {
                    Method process = NewRegistryEvent.class.getDeclaredMethod("fill");
                    process.setAccessible(true);
                    process.invoke(dummyEvent);
                }
                catch (Exception ignored) {}

                REGISTRIES_INITIALIZED = true;
            });
        }
    }

    /**
     * Loads JSON-based configs from data resources
     */
    public static Multimap<RegistryHolder<?>, Holder<? extends ConfigData>> collectDataRegistries(RegistryAccess registryAccess)
    {
        if (registryAccess == null)
        {   ColdSweat.LOGGER.error("Failed to load registries from null RegistryAccess");
            return new RegistryMultiMap<>();
        }
        /*
         Read mod-related tags for config settings
         */
        ConfigSettings.THERMAL_SOURCE_SPREAD_WHITELIST.get()
                .addAll(registryAccess.registryOrThrow(Registry.BLOCK_REGISTRY)
                        .getTag(ModBlockTags.HEARTH_SPREAD_WHITELIST).orElseThrow()
                        .stream().map(holder ->
                        {   ColdSweat.LOGGER.info("Adding block {} to hearth spread whitelist", holder.get());
                            return holder.get();
                        }).toList());

        ConfigSettings.THERMAL_SOURCE_SPREAD_BLACKLIST.get().
                addAll(registryAccess.registryOrThrow(Registry.BLOCK_REGISTRY)
                       .getTag(ModBlockTags.HEARTH_SPREAD_BLACKLIST).orElseThrow()
                       .stream().map(holder ->
                       {
                           ColdSweat.LOGGER.info("Adding block {} to hearth spread blacklist", holder.get());
                           return holder.get();
                       }).toList());

        ConfigSettings.SLEEP_CHECK_IGNORE_BLOCKS.get()
                .addAll(registryAccess.registryOrThrow(Registry.BLOCK_REGISTRY)
                        .getTag(ModBlockTags.IGNORE_SLEEP_CHECK).orElseThrow()
                        .stream().map(holder ->
                        {   ColdSweat.LOGGER.info("Disabling sleeping conditions check for block {}", holder.get());
                            return holder.get();
                        }).toList());

        ConfigSettings.LAMP_DIMENSIONS.get(registryAccess)
                .addAll(registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
                        .getTag(ModDimensionTags.SOUL_LAMP_VALID).orElseThrow()
                        .stream().map(holder ->
                        {   ColdSweat.LOGGER.info("Enabling dimension {} for soulspring lamp", holder.value());
                            return holder.value();
                        }).toList());

        ConfigSettings.INSULATION_BLACKLIST.get()
                .addAll(registryAccess.registryOrThrow(Registry.ITEM_REGISTRY)
                        .getTag(ModItemTags.NOT_INSULATABLE).orElseThrow()
                        .stream().map(holder ->
                        {   ColdSweat.LOGGER.info("Adding item {} to insulation blacklist", holder.get());
                            return holder.get();
                        }).toList());

        ConfigSettings.HEARTH_POTION_BLACKLIST.get()
                .addAll(registryAccess.registryOrThrow(Registry.MOB_EFFECT_REGISTRY)
                        .getTag(ModEffectTags.HEARTH_BLACKLISTED).orElseThrow()
                        .stream().map(holder ->
                        {   ColdSweat.LOGGER.info("Adding effect {} to hearth potion blacklist", holder.get());
                            return holder.get();
                        }).toList());

        /*
         Fetch JSON registries
        */
        Multimap<RegistryHolder<?>, Holder<? extends ConfigData>> registries = new RegistryMultiMap<>();
        for (Map.Entry<ResourceLocation, RegistryHolder<?>> entry : ModRegistries.getRegistries().entrySet())
        {
            RegistryHolder<?> registry = entry.getValue();
            registryAccess.registryOrThrow(registry.key()).holders().forEach(holder ->
            {   holder.unwrapKey().ifPresent(holderKey -> holder.value().setRegistryKey(holderKey));
                registries.put(registry, holder);
            });
        }
        return registries;
    }

    /**
     * Loads JSON-based configs from the configs folder
     */
    public static Multimap<RegistryHolder<?>, Holder<? extends ConfigData>> collectUserRegistries(RegistryAccess registryAccess)
    {
        if (registryAccess == null)
        {   ColdSweat.LOGGER.error("Failed to load registries from null RegistryAccess");
            return new RegistryMultiMap<>();
        }

        /*
         Parse user-defined JSON data from the configs folder
        */
        Multimap<RegistryHolder<?>, Holder<? extends ConfigData>> registries = new RegistryMultiMap<>();
        for (Map.Entry<ResourceLocation, RegistryHolder<?>> entry : ModRegistries.getRegistries().entrySet())
        {
            RegistryHolder<?> registry = entry.getValue();
            registries.putAll(registry, parseConfigData(registry, (Codec) registry.codec(), registryAccess));
        }
        return registries;
    }

    private static void logAndAddRegistries(RegistryAccess registryAccess, Multimap<RegistryHolder<?>, Holder<? extends ConfigData>> registries)
    {
        // Ensure default registry entries load last
        setDefaultRegistryPriority(registries);

        // Load registry removals
        loadRegistryModifiers(registryAccess);

        // Mark holders as "JSON"
        for (Holder<? extends ConfigData> holder : registries.values())
        {   holder.value().setConfigType(ConfigData.Type.JSON);
        }

        // Fire pre-registry-loading event
        LoadRegistriesEvent.Pre event = new LoadRegistriesEvent.Pre(registryAccess, registries, REGISTRY_MODIFIERS);
        MinecraftForge.EVENT_BUS.post(event);

        // Remove registries that don't have required loaded mods
        registries.values().removeIf(holder -> !holder.get().areRequiredModsLoaded());

        // Remove registry entries that match removal criteria
        modifyRegistries(event.getRegistries());

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

        // Fire post-registry-loading event
        LoadRegistriesEvent.Post postEvent = new LoadRegistriesEvent.Post(registryAccess, event.getRegistries());
        MinecraftForge.EVENT_BUS.post(postEvent);
    }

    private static void logRegistryLoaded(String message, Collection<? extends Holder<? extends ConfigData>> registry)
    {
        if (registry.isEmpty())
        {   message += ".";
        }
        else message += ": [";
        // Print comma-separated registry entries
        StringBuilder messageBuilder = new StringBuilder(message);
        Iterator<? extends Holder<? extends ConfigData>> iterator = registry.iterator();
        while (iterator.hasNext())
        {
            Holder<? extends ConfigData> entry = iterator.next();
            if (entry.unwrapKey().isPresent())
            {   messageBuilder.append(entry.unwrapKey().get().location());
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
        for (Holder<? extends ConfigData> holder : registry)
        {
            if (holder.unwrapKey().isEmpty())
            {   messageBuilder.append("\n- ").append(holder.value());
                hasNameless = true;
            }
        }
        if (hasNameless)
        {   ColdSweat.LOGGER.debug(messageBuilder.toString());
        }
    }

    private static void setDefaultRegistryPriority(Multimap<RegistryHolder<?>, ? extends Holder<? extends ConfigData>> registries)
    {
        for (RegistryHolder<?> key : registries.keySet())
        {
            List<? extends Holder<? extends ConfigData>> sortedHolders = new ArrayList<>(registries.get(key));
            sortedHolders.sort(Comparator.comparing(holder ->
            {   return holder.unwrapKey().map(k -> k.location().getPath().startsWith("default") ? 1 : 0).orElse(0);
            }));
            registries.replaceValues(key, (List) sortedHolders);
        }
    }

    private static void loadRegistryModifiers(RegistryAccess registryAccess)
    {
        // Clear the static map
        REGISTRY_MODIFIERS.clear();
        // Gather registry removals & add them to the static map
        Set<Holder<RegistryModifierData<?>>> removals = registryAccess.registryOrThrow(ModRegistries.REGISTRY_MODIFIER_DATA.key()).holders().collect(Collectors.toSet());
        removals.addAll(parseConfigData(ModRegistries.REGISTRY_MODIFIER_DATA, RegistryModifierData.CODEC, registryAccess));
        removals.forEach(holder ->
        {
            RegistryHolder<?> key = ModRegistries.getRegistry(holder.get().registry());
            REGISTRY_MODIFIERS.put(key, holder);
        });
        setDefaultRegistryPriority(REGISTRY_MODIFIERS);
    }

    private static void modifyRegistries(Multimap<RegistryHolder<?>, Holder<? extends ConfigData>> registries)
    {
        ColdSweat.LOGGER.info("Handling registry modifiers...");
        for (var entry : REGISTRY_MODIFIERS.asMap().entrySet())
        {
            modifyEntries((Collection) entry.getValue(), (Collection) registries.get(entry.getKey()));
        }
    }

    private static <T extends ConfigData> void modifyEntries(Collection<Holder<RegistryModifierData<T>>> modifiers, Collection<Holder<T>> registries)
    {
        List<Holder<T>> newRegistries = new ArrayList<>(registries);
        for (Holder<RegistryModifierData<T>> holder : modifiers)
        {
            RegistryModifierData<T> modifier = holder.value();
            for (int i = 0; i < newRegistries.size(); i++)
            {
                Holder<T> elementHolder = newRegistries.get(i);
                if (modifier.matches(elementHolder))
                {
                    T modified = modifier.applyModifications(elementHolder.value());
                    if (modified == null)
                    {
                        newRegistries.remove(i);
                        i--;
                        continue;
                    }
                    if (elementHolder instanceof Holder.Reference<T> reference)
                    {   newRegistries.set(i, RegistryHelper.modifyHolder(reference, modified));
                    }
                    else {
                        newRegistries.set(i, Holder.direct(modified));
                    }
                }
            }
        }
        registries.clear();
        registries.addAll(newRegistries);
    }

    public static <C, K, V extends ConfigData> void modifyEntries(C registries, RegistryHolder<V> registry, Function<C, Collection<Map.Entry<K, V>>> entryGetter,
                                                                  Consumer<Map.Entry<K, V>> entrySetter, Consumer<Map.Entry<K, V>> entryRemover)
    {
        getRegistryModifiers(registry).forEach(holder ->
        {
            RegistryModifierData<V> modifier = holder.value();
            if (modifier.registry() == registry.key())
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
                        entrySetter.accept(Map.entry(entry.getKey(), modified));
                    }
                }
            }
        });
    }

    public static <T extends ConfigData> void modifyEntries(List<T> registries, RegistryHolder<T> registry)
    {
        AtomicInteger index = new AtomicInteger(0);
        modifyEntries(registries, registry, list -> list.stream().map(e -> Map.entry(index.getAndIncrement(), e)).toList(),
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
    {   return getRegistryModifiers(registry).stream().anyMatch(data -> data.value().matches(entry));
    }

    public static <T extends ConfigData> Collection<Holder<RegistryModifierData<T>>> getRegistryModifiers(RegistryHolder<T> registry)
    {   return (Collection<Holder<RegistryModifierData<T>>>) (Collection) REGISTRY_MODIFIERS.get(registry);
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
            {   return;
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
            {   return;
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
            {   return;
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
            {   return;
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
            {   return;
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
            {   return;
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

            BlockTemp blockTemp = new ConfiguredBlockTemp(blockTempData);
            BlockTempRegistry.register(blockTemp);
        });
    }

    private static void addBiomeTempConfigs(Collection<Holder<BiomeTempData>> biomeTemps, RegistryAccess registryAccess)
    {
        biomeTemps.forEach(holder ->
        {
            BiomeTempData biomeTempData = holder.get();

            for (OptionalHolder<Biome> biome : RegistryHelper.mapVanillaRegistryTagList(Registry.BIOME_REGISTRY, biomeTempData.biomes(), registryAccess))
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

            for (OptionalHolder<DimensionType> dimension : RegistryHelper.mapVanillaRegistryTagList(Registry.DIMENSION_TYPE_REGISTRY, dimensionTempData.dimensions(), registryAccess))
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

            for (OptionalHolder<Structure> structure : RegistryHelper.mapVanillaRegistryTagList(Registry.STRUCTURE_REGISTRY, structureTempData.structures(), registryAccess))
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
            for (OptionalHolder<DimensionType> dimension : RegistryHelper.mapVanillaRegistryTagList(Registry.DIMENSION_TYPE_REGISTRY, depthTempData.dimensions(), registryAccess))
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
            {   return;
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

            for (OptionalHolder<Biome> biome : RegistryHelper.mapVanillaRegistryTagList(Registry.BIOME_REGISTRY, spawnBiomeData.biomes(), registryAccess))
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
            {   return;
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
            {   return;
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
            {   return;
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
