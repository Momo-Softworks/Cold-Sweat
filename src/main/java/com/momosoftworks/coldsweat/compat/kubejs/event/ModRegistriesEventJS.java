package com.momosoftworks.coldsweat.compat.kubejs.event;

import com.google.common.collect.Multimap;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.compat.kubejs.event.builder.*;
import com.momosoftworks.coldsweat.compat.kubejs.util.TempModifierDataJS;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.*;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.DynamicHolder;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import dev.latvian.kubejs.event.StartupEventJS;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModRegistriesEventJS extends StartupEventJS
{
    DynamicRegistries registryAccess;

    public ModRegistriesEventJS(DynamicRegistries registryAccess)
    {   this.registryAccess = registryAccess;
    }

    /*
     Block Temperature
     */

    public void addBlockTemperature(Consumer<BlockTempBuilderJS> builder, BlockTempBuilderJS.Function function)
    {
        BlockTempBuilderJS blockTempJS = new BlockTempBuilderJS();
        builder.accept(blockTempJS);
        BlockTemp blockTemp = blockTempJS.build(function);
        if (blockTemp.getAffectedBlocks().isEmpty()) return;

        BlockTempRegistry.register(blockTemp);
        ColdSweat.LOGGER.info("Registered KubeJS block temperature for blocks: {}", blockTempJS.blocks);
    }

    public void addBlockTemperature(double temperature, String units, Consumer<BlockTempBuilderJS> builder)
    {
        builder = ((Consumer<BlockTempBuilderJS>) blockTemp -> blockTemp.units(Temperature.Units.fromID(units))).andThen(builder);
        addBlockTemperature(builder, (level, entity, state, pos, distance) -> temperature);
    }

    /*
     Insulator
     */

    public void addInsulator(Consumer<InsulatorBuilderJS> builder)
    {
        InsulatorBuilderJS insulatorJS = new InsulatorBuilderJS();
        builder.accept(insulatorJS);
        InsulatorData insulator = insulatorJS.build();

        Multimap<Item, InsulatorData> map;
        switch (insulatorJS.slot)
        {
            case ITEM : map = ConfigSettings.INSULATION_ITEMS.get(); break;
            case ARMOR : map = ConfigSettings.INSULATING_ARMORS.get(); break;
            case CURIO : map = ConfigSettings.INSULATING_CURIOS.get(); break;
            default : throw new IllegalArgumentException();
        }
        if (insulatorJS.itemPredicate.isEmpty())
        {   insulatorJS.itemPredicate.add(new ItemRequirement(Collections.singleton(null), null), false);
        }
        for (Item item : RegistryHelper.mapTaggableList(insulatorJS.itemPredicate.flatListMap(ItemRequirement::items)))
        {   map.put(item, insulator);
        }
    }

    /*
     Food Temperature
     */

    public void addFoodTemperature(Consumer<FoodBuilderJS> builder)
    {
        FoodBuilderJS foodJS = new FoodBuilderJS();
        builder.accept(foodJS);
        FoodData foodData = foodJS.build();
        if (!foodData.areRequiredModsLoaded()) return;

        if (foodJS.itemPredicate.isEmpty())
        {   foodJS.itemPredicate.add(new ItemRequirement(Collections.singleton(null), null), false);
        }
        for (Item item : RegistryHelper.mapTaggableList(foodJS.itemPredicate.flatListMap(ItemRequirement::items)))
        {   ConfigSettings.FOOD_TEMPERATURES.get().put(item, foodData);
        }
    }

    /*
     Fuel
     */

    private void addFuel(Consumer<FuelBuilderJS> builder, DynamicHolder<Multimap<Item, FuelData>> config, FuelData.FuelType fuelType)
    {
        FuelBuilderJS fuelJS = new FuelBuilderJS();
        builder.accept(fuelJS);
        FuelData fuelData = fuelJS.build(fuelType);
        if (!fuelData.areRequiredModsLoaded()) return;

        if (fuelJS.itemPredicate.isEmpty())
        {   fuelJS.itemPredicate.add(new ItemRequirement(Collections.singleton(null), null), false);
        }
        for (Item item : RegistryHelper.mapTaggableList(fuelJS.itemPredicate.flatListMap(ItemRequirement::items)))
        {   config.get().put(item, fuelData);
        }
    }

    public void addHearthFuel(Consumer<FuelBuilderJS> builder)
    {   addFuel(builder, ConfigSettings.HEARTH_FUEL, FuelData.FuelType.HEARTH);
    }

    public void addBoilerFuel(Consumer<FuelBuilderJS> builder)
    {   addFuel(builder, ConfigSettings.BOILER_FUEL, FuelData.FuelType.BOILER);
    }

    public void addIceboxFuel(Consumer<FuelBuilderJS> builder)
    {   addFuel(builder, ConfigSettings.ICEBOX_FUEL, FuelData.FuelType.ICEBOX);
    }

    public void addSoulspringLampFuel(Consumer<FuelBuilderJS> builder)
    {   addFuel(builder, ConfigSettings.SOULSPRING_LAMP_FUEL, FuelData.FuelType.SOUL_LAMP);
    }

    /*
     Carried Item Temperature
     */

    public void addCarriedItemTemperature(Consumer<CarriedItemBuilderJS> builder)
    {
        CarriedItemBuilderJS carriedItemJS = new CarriedItemBuilderJS();
        builder.accept(carriedItemJS);
        ItemCarryTempData carryData = carriedItemJS.build();
        if (!carryData.areRequiredModsLoaded()) return;

        if (carriedItemJS.itemPredicate.isEmpty())
        {   carriedItemJS.itemPredicate.add(new ItemRequirement(Collections.singleton(null), null), false);
        }
        for (Item item : RegistryHelper.mapTaggableList(carriedItemJS.itemPredicate.flatListMap(ItemRequirement::items)))
        {   ConfigSettings.CARRIED_ITEM_TEMPERATURES.get().put(item, carryData);
        }
    }

    /*
     Drying Item
     */

    public void addDryingItem(Consumer<DryingItemBuilderJS> builder)
    {
        DryingItemBuilderJS dryingItemJS = new DryingItemBuilderJS();
        builder.accept(dryingItemJS);
        DryingItemData dryingData = dryingItemJS.build();
        if (!dryingData.areRequiredModsLoaded()) return;

        if (dryingItemJS.itemPredicate.isEmpty())
        {   dryingItemJS.itemPredicate.add(new ItemRequirement(Collections.singleton(null), null), false);
        }
        for (Item item : RegistryHelper.mapTaggableList(dryingItemJS.itemPredicate.flatListMap(ItemRequirement::items)))
        {   ConfigSettings.DRYING_ITEMS.get().put(item, dryingData);
        }
    }

    private <K, V extends ConfigData> void addRegistryConfig(RegistryKey<Registry<K>> keyRegistry,
                                                             DynamicHolder<? extends Map<K, V>> config,
                                                             String[] rawKeys,
                                                             Function<List<K>, V> constructor)
    {
        List<K> parsed = ConfigHelper.parseRegistryItems(keyRegistry, registryAccess, rawKeys);
        if (parsed.isEmpty())
        {   ColdSweat.LOGGER.error("Failed to find any {} in: {}", keyRegistry.location().getPath(), Arrays.toString(rawKeys));
            return;
        }
        V configData = constructor.apply(parsed);
        configData.setRegistryType(ConfigData.Type.KUBEJS);
        if (!configData.areRequiredModsLoaded()) return;

        for (K holder : parsed)
        {   config.get(registryAccess).put(holder, configData);
        }
    }

    /*
     Biome Temperature
     */

    public void addBiomeTemperature(double minTemp, double maxTemp, String units, String... biomes)
    {
        this.addRegistryConfig(Registry.BIOME_REGISTRY, ConfigSettings.BIOME_TEMPS, biomes,
                parsedBiomes -> new BiomeTempData(new NegatableList<>(parsedBiomes), minTemp, maxTemp, Temperature.Units.fromID(units), false));
    }
    public void addBiomeTemperature(double minTemp, double maxTemp, String... biomes)
    {   addBiomeTemperature(minTemp, maxTemp, "mc", biomes);
    }

    public void addBiomeOffset(double minTemp, double maxTemp, String units, String... biomes)
    {
        this.addRegistryConfig(Registry.BIOME_REGISTRY, ConfigSettings.BIOME_OFFSETS, biomes,
                parsedBiomes -> new BiomeTempData(new NegatableList<>(parsedBiomes), minTemp, maxTemp, Temperature.Units.fromID(units), true));
    }
    public void addBiomeOffset(double minTemp, double maxTemp, String... biomes)
    {   addBiomeOffset(minTemp, maxTemp, "mc", biomes);
    }

    /*
     Dimension Temperature
     */

    public void addDimensionTemperature(double temperature, String units, String... dimensions)
    {
        this.addRegistryConfig(Registry.DIMENSION_TYPE_REGISTRY, ConfigSettings.DIMENSION_TEMPS, dimensions,
                parsedDimensions -> new DimensionTempData(new NegatableList<>(parsedDimensions), temperature, Temperature.Units.fromID(units), false));
    }
    public void addDimensionTemperature(double temperature, String... dimensions)
    {   addDimensionTemperature(temperature, "mc", dimensions);
    }

    public void addDimensionOffset(double temperature, String units, String... dimensions)
    {
        this.addRegistryConfig(Registry.DIMENSION_TYPE_REGISTRY, ConfigSettings.DIMENSION_OFFSETS, dimensions,
                parsedDimensions -> new DimensionTempData(new NegatableList<>(parsedDimensions), temperature, Temperature.Units.fromID(units), true));
    }
    public void addDimensionOffset(double temperature, String... dimensions)
    {   addDimensionOffset(temperature, "mc", dimensions);
    }

    /*
     Structure Temperature
     */

    public void addStructureTemperature(double temperature, String units, String... structures)
    {
        this.addRegistryConfig(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ConfigSettings.STRUCTURE_TEMPS, structures,
                parsedStructures -> new StructureTempData(new NegatableList<>(parsedStructures), temperature, Temperature.Units.fromID(units), false));
    }
    public void addStructureTemperature(double temperature, String... structures)
    {   addStructureTemperature(temperature, "mc", structures);
    }

    public void addStructureOffset(double temperature, String units, String... structures)
    {
        this.addRegistryConfig(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ConfigSettings.STRUCTURE_OFFSETS, structures,
                parsedStructures -> new StructureTempData(new NegatableList<>(parsedStructures), temperature, Temperature.Units.fromID(units), true));
    }
    public void addStructureOffset(double temperature, String... structures)
    {   addStructureOffset(temperature, "mc", structures);
    }

    /*
     Entity Temperature
     */

    public void addEntityTemperature(Consumer<EntityTempBuilderJS> builder)
    {
        EntityTempBuilderJS entityTempJS = new EntityTempBuilderJS();
        builder.accept(entityTempJS);
        EntityTempData entityTempData = entityTempJS.build();
        if (!entityTempData.areRequiredModsLoaded()) return;

        if (entityTempJS.entityPredicate.isEmpty())
        {   entityTempJS.entityPredicate.add(new EntityRequirement(Collections.singleton(null), null), false);
        }
        for (EntityType<?> item : RegistryHelper.mapTaggableList(entityTempJS.entityPredicate.flatListMap(EntityRequirement::entities)))
        {   ConfigSettings.ENTITY_TEMPERATURES.get().put(item, entityTempData);
        }
    }

    /*
     Insulating Mounts
     */

    public void addInsulatingMount(Consumer<InsulatingMountBuilderJS> builder)
    {
        InsulatingMountBuilderJS insulatingMountJS = new InsulatingMountBuilderJS();
        builder.accept(insulatingMountJS);
        MountData mountData = insulatingMountJS.build();
        if (!mountData.areRequiredModsLoaded()) return;

        if (insulatingMountJS.entityPredicate.isEmpty())
        {   insulatingMountJS.entityPredicate.add(new EntityRequirement(Collections.singleton(null), null), false);
        }
        for (EntityType<?> item : RegistryHelper.mapTaggableList(insulatingMountJS.entityPredicate.flatListMap(EntityRequirement::entities)))
        {   ConfigSettings.INSULATED_MOUNTS.get().put(item, mountData);
        }
    }

    /*
     Spawn Biomes
     */

    public void addSpawnBiomes(Consumer<SpawnBiomeBuilderJS> builder)
    {
        SpawnBiomeBuilderJS spawnBiomeJS = new SpawnBiomeBuilderJS();
        builder.accept(spawnBiomeJS);
        SpawnBiomeData spawnBiomeData = spawnBiomeJS.build();
        if (!spawnBiomeData.areRequiredModsLoaded()) return;

        if (spawnBiomeJS.biomes.isEmpty())
        {   spawnBiomeJS.biomes.add(null);
        }
        for (Biome biome : spawnBiomeJS.biomes)
        {   ConfigSettings.ENTITY_SPAWN_BIOMES.get().put(biome, spawnBiomeData);
        }
    }

    /*
     TempModifier
     */

    public void addTempModifier(String id, Function<TempModifierDataJS, Function<Double, Double>> constructor)
    {
        ResourceLocation key = new ResourceLocation(id);
        if (key.getNamespace().equals("minecraft"))
        {   ColdSweat.LOGGER.error("KubeJS: Non-Minecraft namespace required for TempModifier IDs (i.e. mymod:my_modifier)");
            return;
        }

        TempModifierRegistry.register(key, () -> new TempModifierJS(constructor));
    }

    static class TempModifierJS extends TempModifier
    {
        Function<TempModifierDataJS, Function<Double, Double>> constructor;

        public TempModifierJS(Function<TempModifierDataJS, Function<Double, Double>> constructor)
        {   this.constructor = constructor;
        }

        @Override
        protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
        {
            return constructor.apply(new TempModifierDataJS(entity, trait));
        }
    }
}
