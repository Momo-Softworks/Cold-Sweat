package com.momosoftworks.coldsweat.data;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.codec.configuration.*;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.util.math.FastMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public class ModRegistries
{
    private static final Map<ResourceLocation, RegistryHolder<? extends ConfigData>> REGISTRIES = new FastMap<>();

    // Item Registries
    public static final RegistryHolder<InsulatorData> INSULATOR_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "item/insulator"), InsulatorData.CODEC, InsulatorData.class);
    public static final RegistryHolder<FuelData> FUEL_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "item/fuel"), FuelData.CODEC, FuelData.class);
    public static final RegistryHolder<FoodData> FOOD_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "item/food"), FoodData.CODEC, FoodData.class);
    public static final RegistryHolder<ItemTempData> ITEM_TEMP_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "item/item_temp"), ItemTempData.CODEC, ItemTempData.class);
    public static final RegistryHolder<DryingItemData> DRYING_ITEM_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "item/drying_item"), DryingItemData.CODEC, DryingItemData.class);
    public static final RegistryHolder<ItemInsulationSlotsData> INSULATION_SLOTS_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "item/insulation_slots"), ItemInsulationSlotsData.CODEC, ItemInsulationSlotsData.class);

    // World Registries
    public static final RegistryHolder<BlockTempData> BLOCK_TEMP_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "block/block_temp"), BlockTempData.CODEC, BlockTempData.class);
    public static final RegistryHolder<BiomeTempData> BIOME_TEMP_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "world/biome_temp"), BiomeTempData.CODEC, BiomeTempData.class);
    public static final RegistryHolder<DimensionTempData> DIMENSION_TEMP_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "world/dimension_temp"), DimensionTempData.CODEC, DimensionTempData.class);
    public static final RegistryHolder<StructureTempData> STRUCTURE_TEMP_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "world/structure_temp"), StructureTempData.CODEC, StructureTempData.class);
    public static final RegistryHolder<DepthTempData> DEPTH_TEMP_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "world/temp_region"), DepthTempData.CODEC, DepthTempData.class);

    // Entity Registries
    public static final RegistryHolder<MountData> MOUNT_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "entity/mount"), MountData.CODEC, MountData.class);
    public static final RegistryHolder<SpawnBiomeData> ENTITY_SPAWN_BIOME_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "entity/spawn_biome"), SpawnBiomeData.CODEC, SpawnBiomeData.class);
    public static final RegistryHolder<EntityTempData> ENTITY_TEMP_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "entity/entity_temp"), EntityTempData.CODEC, EntityTempData.class);
    public static final RegistryHolder<EntityClimateData> ENTITY_CLIMATE_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "entity/climate"), EntityClimateData.CODEC, EntityClimateData.class);
    public static final RegistryHolder<TempEffectsData> TEMP_EFFECTS_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "entity/temp_effects"), TempEffectsData.CODEC, TempEffectsData.class);

    // Special registries
    public static final RegistryHolder<RegistryModifierData<?>> REGISTRY_MODIFIER_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "modifier"), (Codec) RegistryModifierData.CODEC, RegistryModifierData.class);

    public static <V extends ConfigData> RegistryHolder<V> createRegistry(ResourceLocation registry, Codec<V> codec, Class<V> type)
    {
        RegistryHolder<V> registryHolder = new RegistryHolder<>(ResourceKey.createRegistryKey(registry), codec, type);
        REGISTRIES.put(registry, registryHolder);
        return registryHolder;
    }

    public static Map<ResourceLocation, RegistryHolder<?>> getRegistries()
    {   return ImmutableMap.copyOf(REGISTRIES);
    }

    public static ResourceKey<? extends Registry<? extends ConfigData>> getRegistry(ResourceLocation name)
    {
        return Optional.ofNullable(REGISTRIES.get(name)).map(RegistryHolder::key)
               .orElseThrow(() -> ColdSweat.LOGGER.throwing(new IllegalArgumentException("Unknown Cold Sweat registry: " + name)));
    }

    public static <T extends ConfigData> Codec<T> getCodec(ResourceKey<Registry<T>> registry)
    {
        return (Codec<T>) Optional.of(REGISTRIES.get(registry.location())).map(RegistryHolder::codec)
               .orElseThrow(() -> ColdSweat.LOGGER.throwing(new IllegalArgumentException("Unknown Cold Sweat registry: " + registry.location().getPath())));
    }
}
