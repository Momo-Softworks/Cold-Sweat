package com.momosoftworks.coldsweat.compat.kubejs.event;

import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.registry.LoadRegistriesEvent;
import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.compat.kubejs.event.builder.*;
import com.momosoftworks.coldsweat.compat.kubejs.util.TempModifierDataJS;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.RegistryHolder;
import com.momosoftworks.coldsweat.data.codec.configuration.*;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.OptionalHolder;
import dev.latvian.mods.kubejs.event.StartupEventJS;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModRegistriesEventJS extends StartupEventJS
{
    private final LoadRegistriesEvent.Pre event;

    public ModRegistriesEventJS(LoadRegistriesEvent.Pre event)
    {
        this.event = event;
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
        if (insulatorJS.itemPredicate.isEmpty())
        {   insulatorJS.itemPredicate.add(new ItemRequirement(Collections.singleton(null), null), false);
        }

        InsulatorData insulatorDate = insulatorJS.build();
        if (!insulatorDate.areRequiredModsLoaded()) return;

        this.event.addRegistryEntry(ModRegistries.INSULATOR_DATA, insulatorDate);
    }

    /*
     Food Temperature
     */

    public void addFoodTemperature(Consumer<FoodBuilderJS> builder)
    {
        FoodBuilderJS foodJS = new FoodBuilderJS();
        builder.accept(foodJS);
        if (foodJS.itemPredicate.isEmpty())
        {   foodJS.itemPredicate.add(new ItemRequirement(Collections.singleton(null), null), false);
        }

        FoodData foodData = foodJS.build();
        if (!foodData.areRequiredModsLoaded()) return;

        this.event.addRegistryEntry(ModRegistries.FOOD_DATA, foodData);
    }

    /*
     Fuel
     */

    private void addFuel(Consumer<FuelBuilderJS> builder, FuelData.FuelType fuelType)
    {
        FuelBuilderJS fuelJS = new FuelBuilderJS();
        builder.accept(fuelJS);
        if (fuelJS.itemPredicate.isEmpty())
        {   fuelJS.itemPredicate.add(new ItemRequirement(Collections.singleton(null), null), false);
        }

        FuelData fuelData = fuelJS.build(fuelType);
        if (!fuelData.areRequiredModsLoaded()) return;

        this.event.addRegistryEntry(ModRegistries.FUEL_DATA, fuelData);
    }

    public void addHearthFuel(Consumer<FuelBuilderJS> builder)
    {   addFuel(builder, FuelData.FuelType.HEARTH);
    }

    public void addBoilerFuel(Consumer<FuelBuilderJS> builder)
    {   addFuel(builder, FuelData.FuelType.BOILER);
    }

    public void addIceboxFuel(Consumer<FuelBuilderJS> builder)
    {   addFuel(builder, FuelData.FuelType.ICEBOX);
    }

    public void addSoulspringLampFuel(Consumer<FuelBuilderJS> builder)
    {   addFuel(builder, FuelData.FuelType.SOUL_LAMP);
    }

    /*
     Item Temperature
     */

    public void addItemTemperature(Consumer<ItemTempBuilderJS> builder)
    {
        ItemTempBuilderJS itemTempJS = new ItemTempBuilderJS();
        builder.accept(itemTempJS);
        if (itemTempJS.itemPredicate.isEmpty())
        {   itemTempJS.itemPredicate.add(new ItemRequirement(Collections.singleton(null), null), false);
        }

        ItemTempData itemData = itemTempJS.build();
        if (!itemData.areRequiredModsLoaded()) return;

        this.event.addRegistryEntry(ModRegistries.ITEM_TEMP_DATA, itemData);
    }

    /*
     Drying Item
     */

    public void addDryingItem(Consumer<DryingItemBuilderJS> builder)
    {
        DryingItemBuilderJS dryingItemJS = new DryingItemBuilderJS();
        builder.accept(dryingItemJS);
        if (dryingItemJS.itemPredicate.isEmpty())
        {   dryingItemJS.itemPredicate.add(new ItemRequirement(Collections.singleton(null), null), false);
        }

        DryingItemData dryingData = dryingItemJS.build();
        if (!dryingData.areRequiredModsLoaded()) return;

        this.event.addRegistryEntry(ModRegistries.DRYING_ITEM_DATA, dryingData);
    }

    private <K, V extends ConfigData> void addRegistryConfig(ResourceKey<Registry<K>> keyRegistry,
                                                             RegistryHolder<V> modRegistry,
                                                             String[] rawKeys,
                                                             Function<NegatableList<Either<TagKey<K>, OptionalHolder<K>>>, V> constructor)
    {
        NegatableList<Either<TagKey<K>, OptionalHolder<K>>> parsed = ConfigHelper.parseRegistryItems(keyRegistry, this.event.getRegistryAccess(), rawKeys);
        if (parsed.isEmpty())
        {   ColdSweat.LOGGER.error("Failed to find any {} in: {}", keyRegistry.location().getPath(), Arrays.toString(rawKeys));
            return;
        }
        V configData = constructor.apply(parsed);
        configData.setConfigType(ConfigData.Type.KUBEJS);
        if (!configData.areRequiredModsLoaded()) return;

        this.event.addRegistryEntry(modRegistry, configData);
    }

    /*
     Biome Temperature
     */

    public void addBiomeTemperature(double minTemp, double maxTemp, String units, String... biomes)
    {
        this.addRegistryConfig(Registry.BIOME_REGISTRY, ModRegistries.BIOME_TEMP_DATA, biomes,
                parsedBiomes -> new BiomeTempData(parsedBiomes, minTemp, maxTemp, Temperature.Units.fromID(units), false, false));
    }
    public void addBiomeTemperature(double minTemp, double maxTemp, String... biomes)
    {   addBiomeTemperature(minTemp, maxTemp, "mc", biomes);
    }

    public void addBiomeOffset(double minTemp, double maxTemp, String units, String... biomes)
    {
        this.addRegistryConfig(Registry.BIOME_REGISTRY, ModRegistries.BIOME_TEMP_DATA, biomes,
                parsedBiomes -> new BiomeTempData(parsedBiomes, minTemp, maxTemp, Temperature.Units.fromID(units), true, false));
    }
    public void addBiomeOffset(double minTemp, double maxTemp, String... biomes)
    {   addBiomeOffset(minTemp, maxTemp, "mc", biomes);
    }

    /*
     Dimension Temperature
     */

    public void addDimensionTemperature(double temperature, String units, String... dimensions)
    {
        this.addRegistryConfig(Registry.DIMENSION_TYPE_REGISTRY, ModRegistries.DIMENSION_TEMP_DATA, dimensions,
                parsedDimensions -> new DimensionTempData(parsedDimensions, temperature, Temperature.Units.fromID(units), false));
    }
    public void addDimensionTemperature(double temperature, String... dimensions)
    {   addDimensionTemperature(temperature, "mc", dimensions);
    }

    public void addDimensionOffset(double temperature, String units, String... dimensions)
    {
        this.addRegistryConfig(Registry.DIMENSION_TYPE_REGISTRY, ModRegistries.DIMENSION_TEMP_DATA, dimensions,
                parsedDimensions -> new DimensionTempData(parsedDimensions, temperature, Temperature.Units.fromID(units), true));
    }
    public void addDimensionOffset(double temperature, String... dimensions)
    {   addDimensionOffset(temperature, "mc", dimensions);
    }

    /*
     Structure Temperature
     */

    public void addStructureTemperature(double temperature, String units, String... structures)
    {
        this.addRegistryConfig(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ModRegistries.STRUCTURE_TEMP_DATA, structures,
                parsedStructures -> new StructureTempData(parsedStructures, temperature, Temperature.Units.fromID(units), false));
    }
    public void addStructureTemperature(double temperature, String... structures)
    {   addStructureTemperature(temperature, "mc", structures);
    }

    public void addStructureOffset(double temperature, String units, String... structures)
    {
        this.addRegistryConfig(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ModRegistries.STRUCTURE_TEMP_DATA, structures,
                parsedStructures -> new StructureTempData(parsedStructures, temperature, Temperature.Units.fromID(units), true));
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
        if (entityTempJS.entityPredicate.isEmpty())
        {   entityTempJS.entityPredicate.add(new EntityRequirement(Collections.singleton(null), null), false);
        }

        EntityTempData entityTempData = entityTempJS.build();
        if (!entityTempData.areRequiredModsLoaded()) return;

        this.event.addRegistryEntry(ModRegistries.ENTITY_TEMP_DATA, entityTempData);
    }

    /*
     Entity Climate
     */

    public void addEntityClimate(Consumer<EntityClimateBuilderJS> builder)
    {
        EntityClimateBuilderJS entityClimateJS = new EntityClimateBuilderJS();
        builder.accept(entityClimateJS);
        if (entityClimateJS.entityPredicate.isEmpty())
        {   entityClimateJS.entityPredicate.add(new EntityRequirement(Collections.singleton(null), null), false);
        }

        EntityClimateData entityClimateData = entityClimateJS.build();
        if (!entityClimateData.areRequiredModsLoaded()) return;

        this.event.addRegistryEntry(ModRegistries.ENTITY_CLIMATE_DATA, entityClimateData);
    }

    /*
     Insulating Mounts
     */

    public void addInsulatingMount(Consumer<InsulatingMountBuilderJS> builder)
    {
        InsulatingMountBuilderJS insulatingMountJS = new InsulatingMountBuilderJS();
        builder.accept(insulatingMountJS);
        if (insulatingMountJS.entityPredicate.isEmpty())
        {   insulatingMountJS.entityPredicate.add(new EntityRequirement(Collections.singleton(null), null), false);
        }

        MountData mountData = insulatingMountJS.build();
        if (!mountData.areRequiredModsLoaded()) return;

        this.event.addRegistryEntry(ModRegistries.MOUNT_DATA, mountData);
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
        this.event.addRegistryEntry(ModRegistries.ENTITY_SPAWN_BIOME_DATA, spawnBiomeData);
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
        class TempModifierJS extends TempModifier
        {
            public TempModifierJS()
            {}

            @Override
            protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
            {
                return constructor.apply(new TempModifierDataJS(entity, trait));
            }
        }

        TempModifierRegistry.register(key, TempModifierJS::new);
    }
}
