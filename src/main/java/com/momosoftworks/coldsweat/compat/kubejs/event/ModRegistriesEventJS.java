package com.momosoftworks.coldsweat.compat.kubejs.event;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;
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
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.DynamicHolder;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import dev.latvian.mods.kubejs.event.StartupEventJS;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModRegistriesEventJS extends StartupEventJS
{
    RegistryAccess registryAccess;

    public ModRegistriesEventJS(RegistryAccess registryAccess)
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

        Multimap<Item, InsulatorData> map = switch (insulatorJS.slot)
        {
            case ITEM -> ConfigSettings.INSULATION_ITEMS.get();
            case ARMOR -> ConfigSettings.INSULATING_ARMORS.get();
            case CURIO -> ConfigSettings.INSULATING_CURIOS.get();
        };
        if (insulatorJS.items.isEmpty())
        {   insulatorJS.items.add(null);
        }
        for (Item item : insulatorJS.items)
        {   map.put(item, insulator);
        }
        ColdSweat.LOGGER.info("Registered KubeJS insulator for items: {}", insulatorJS.items);
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

        if (foodJS.items.isEmpty())
        {   foodJS.items.add(null);
        }
        for (Item item : foodJS.items)
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

        if (fuelJS.items.isEmpty())
        {   fuelJS.items.add(null);
        }
        for (Item item : fuelJS.items)
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

        if (carriedItemJS.items.isEmpty())
        {   carriedItemJS.items.add(null);
        }
        for (Item item : carriedItemJS.items)
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

        if (dryingItemJS.items.isEmpty())
        {   dryingItemJS.items.add(null);
        }
        for (Item item : dryingItemJS.items)
        {   ConfigSettings.DRYING_ITEMS.get().put(item, dryingData);
        }
    }

    private <K, V extends ConfigData> void addRegistryConfig(ResourceKey<Registry<K>> keyRegistry,
                                                             DynamicHolder<? extends Map<Holder<K>, V>> config,
                                                             String[] rawKeys,
                                                             Function<List<Either<TagKey<K>, Holder<K>>>, V> constructor)
    {
        List<Either<TagKey<K>, Holder<K>>> parsed = ConfigHelper.parseRegistryItems(keyRegistry, registryAccess, rawKeys);
        if (parsed.isEmpty())
        {   ColdSweat.LOGGER.error("Failed to find any {} in: {}", keyRegistry.location().getPath(), Arrays.toString(rawKeys));
            return;
        }
        V configData = constructor.apply(parsed);
        configData.setType(ConfigData.Type.KUBEJS);
        if (!configData.areRequiredModsLoaded()) return;

        for (Holder<K> holder : RegistryHelper.mapVanillaRegistryTagList(keyRegistry, parsed, registryAccess))
        {   config.get(registryAccess).put(holder, configData);
        }
    }

    /*
     Biome Temperature
     */

    public void addBiomeTemperature(double minTemp, double maxTemp, String units, String... biomes)
    {
        this.addRegistryConfig(Registry.BIOME_REGISTRY, ConfigSettings.BIOME_TEMPS, biomes,
                parsedBiomes -> new BiomeTempData(parsedBiomes, minTemp, maxTemp, Temperature.Units.fromID(units), false));
    }
    public void addBiomeTemperature(double minTemp, double maxTemp, String... biomes)
    {   addBiomeTemperature(minTemp, maxTemp, "mc", biomes);
    }

    public void addBiomeOffset(double minTemp, double maxTemp, String units, String... biomes)
    {
        this.addRegistryConfig(Registry.BIOME_REGISTRY, ConfigSettings.BIOME_OFFSETS, biomes,
                parsedBiomes -> new BiomeTempData(parsedBiomes, minTemp, maxTemp, Temperature.Units.fromID(units), true));
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
                parsedDimensions -> new DimensionTempData(parsedDimensions, temperature, Temperature.Units.fromID(units), false));
    }
    public void addDimensionTemperature(double temperature, String... dimensions)
    {   addDimensionTemperature(temperature, "mc", dimensions);
    }

    public void addDimensionOffset(double temperature, String units, String... dimensions)
    {
        this.addRegistryConfig(Registry.DIMENSION_TYPE_REGISTRY, ConfigSettings.DIMENSION_OFFSETS, dimensions,
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
        this.addRegistryConfig(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ConfigSettings.STRUCTURE_TEMPS, structures,
                parsedStructures -> new StructureTempData(parsedStructures, temperature, Temperature.Units.fromID(units), false));
    }
    public void addStructureTemperature(double temperature, String... structures)
    {   addStructureTemperature(temperature, "mc", structures);
    }

    public void addStructureOffset(double temperature, String units, String... structures)
    {
        this.addRegistryConfig(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ConfigSettings.STRUCTURE_OFFSETS, structures,
                parsedStructures -> new StructureTempData(parsedStructures, temperature, Temperature.Units.fromID(units), true));
    }
    public void addStructureOffset(double temperature, String... structures)
    {   addStructureOffset(temperature, "mc", structures);
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
