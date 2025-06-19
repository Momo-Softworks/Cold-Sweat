package com.momosoftworks.coldsweat.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.slot.ScalingFormula;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.entity.data.Preference;
import com.momosoftworks.coldsweat.config.spec.*;
import com.momosoftworks.coldsweat.core.init.ModEntities;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.configuration.*;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.FastMap;
import com.momosoftworks.coldsweat.util.math.RegistryMultiMap;
import com.momosoftworks.coldsweat.util.serialization.*;
import com.momosoftworks.coldsweat.compat.CompatManager;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.*;

import static com.momosoftworks.coldsweat.util.serialization.DynamicHolder.SyncType;

/**
 * Holds almost all configs for Cold Sweat in memory for easy access.
 * Handles syncing configs between the client/server.
 */
public class ConfigSettings
{
    public static final BiMap<ResourceLocation, DynamicHolder<?>> CONFIG_SETTINGS = HashBiMap.create();
    public static final BiMap<ResourceLocation, DynamicHolder<?>> CLIENT_SETTINGS = HashBiMap.create();

    public static Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;

    // Settings visible in the config screen
    public static final DynamicHolder<Difficulty> DIFFICULTY;
    public static final DynamicHolder<Double> MAX_TEMP;
    public static final DynamicHolder<Double> MIN_TEMP;
    public static final DynamicHolder<Double> TEMP_RATE;
    public static final DynamicHolder<Double> TEMP_DAMAGE;
    public static final DynamicHolder<Boolean> FIRE_RESISTANCE_ENABLED;
    public static final DynamicHolder<Boolean> ICE_RESISTANCE_ENABLED;
    public static final DynamicHolder<Boolean> USE_PEACEFUL_MODE;
    public static final DynamicHolder<Boolean> REQUIRE_THERMOMETER;
    public static final DynamicHolder<Integer> GRACE_LENGTH;
    public static final DynamicHolder<Boolean> GRACE_ENABLED;

    // Other Difficulty Settings
    public static final DynamicHolder<Double> HEARTS_FREEZING_PERCENTAGE;
    public static final DynamicHolder<Double> COLD_MINING_IMPAIRMENT;
    public static final DynamicHolder<Double> COLD_MOVEMENT_SLOWDOWN;
    public static final DynamicHolder<Double> COLD_KNOCKBACK_REDUCTION;
    public static final DynamicHolder<Double> HEATSTROKE_FOG_DISTANCE;

    // World Settings
    public static final DynamicHolder<Map<Holder<Biome>, BiomeTempData>> BIOME_TEMPS;
    public static final DynamicHolder<Map<Holder<Biome>, BiomeTempData>> BIOME_OFFSETS;
    public static final DynamicHolder<Map<Holder<DimensionType>, DimensionTempData>> DIMENSION_TEMPS;
    public static final DynamicHolder<Map<Holder<DimensionType>, DimensionTempData>> DIMENSION_OFFSETS;
    public static final DynamicHolder<Map<Holder<Structure>, StructureTempData>> STRUCTURE_TEMPS;
    public static final DynamicHolder<Map<Holder<Structure>, StructureTempData>> STRUCTURE_OFFSETS;
    public static final DynamicHolder<List<DepthTempData>> DEPTH_REGIONS;
    public static final DynamicHolder<Boolean> CHECK_SLEEP_CONDITIONS;
    public static final DynamicHolder<SeasonalTempData> SUMMER_TEMPS;
    public static final DynamicHolder<SeasonalTempData> AUTUMN_TEMPS;
    public static final DynamicHolder<SeasonalTempData> WINTER_TEMPS;
    public static final DynamicHolder<SeasonalTempData> SPRING_TEMPS;
    public static final DynamicHolder<Double> OVERCAST_TEMP_OFFSET;

    // Block settings
    public static final DynamicHolder<Integer> BLOCK_RANGE;
    public static final DynamicHolder<Boolean> COLD_SOUL_FIRE;
    public static final DynamicHolder<List<Block>> THERMAL_SOURCE_SPREAD_WHITELIST;
    public static final DynamicHolder<List<Block>> THERMAL_SOURCE_SPREAD_BLACKLIST;
    public static final DynamicHolder<Double> THERMAL_SOURCE_STRENGTH;

    public static final DynamicHolder<Boolean> SMART_HEARTH;
    public static final DynamicHolder<Integer> HEARTH_MAX_RANGE;
    public static final DynamicHolder<Integer> HEARTH_RANGE;
    public static final DynamicHolder<Integer> HEARTH_MAX_VOLUME;
    public static final DynamicHolder<Integer> HEARTH_WARM_UP_TIME;
    public static final DynamicHolder<Integer> HEARTH_MAX_INSULATION;
    public static final DynamicHolder<Integer> HEARTH_FUEL_INTERVAL;

    public static final DynamicHolder<Boolean> SMART_BOILER;
    public static final DynamicHolder<Integer> BOILER_MAX_RANGE;
    public static final DynamicHolder<Integer> BOILER_RANGE;
    public static final DynamicHolder<Integer> BOILER_MAX_VOLUME;
    public static final DynamicHolder<Integer> BOILER_WARM_UP_TIME;
    public static final DynamicHolder<Integer> BOILER_MAX_INSULATION;
    public static final DynamicHolder<Integer> BOILER_FUEL_INTERVAL;

    public static final DynamicHolder<Boolean> SMART_ICEBOX;
    public static final DynamicHolder<Integer> ICEBOX_MAX_RANGE;
    public static final DynamicHolder<Integer> ICEBOX_RANGE;
    public static final DynamicHolder<Integer> ICEBOX_MAX_VOLUME;
    public static final DynamicHolder<Integer> ICEBOX_WARM_UP_TIME;
    public static final DynamicHolder<Integer> ICEBOX_MAX_INSULATION;
    public static final DynamicHolder<Integer> ICEBOX_FUEL_INTERVAL;

    public static final DynamicHolder<Integer> THERMOREGULATOR_INSULATION;

    public static final DynamicHolder<List<Block>> SLEEP_CHECK_IGNORE_BLOCKS;
    public static final DynamicHolder<Boolean> USE_CUSTOM_WATER_FREEZE_BEHAVIOR;
    public static final DynamicHolder<Boolean> USE_CUSTOM_ICE_DROPS;

    // Item settings
    public static final DynamicHolder<Multimap<Item, InsulatorData>> INSULATION_ITEMS;
    public static final DynamicHolder<Multimap<Item, InsulatorData>> INSULATING_ARMORS;
    public static final DynamicHolder<Multimap<Item, InsulatorData>> INSULATING_CURIOS;
    public static final DynamicHolder<ScalingFormula> INSULATION_SLOTS;
    public static final DynamicHolder<Multimap<Item, ItemInsulationSlotsData>> INSULATION_SLOT_OVERRIDES;
    public static final DynamicHolder<List<Item>> INSULATION_BLACKLIST;
    public static final DynamicHolder<Multimap<Item, DryingItemData>> DRYING_ITEMS;

    public static final DynamicHolder<Multimap<Item, FoodData>> FOOD_TEMPERATURES;

    public static final DynamicHolder<Multimap<Item, ItemCarryTempData>> CARRIED_ITEM_TEMPERATURES;

    public static final DynamicHolder<Integer> WATERSKIN_CONSUME_STRENGTH;
    public static final DynamicHolder<Double> WATERSKIN_HOTBAR_STRENGTH;
    public static final DynamicHolder<Double> WATERSKIN_NEUTRALIZE_SPEED;
    public static final DynamicHolder<Double> SOULSPRING_LAMP_STRENGTH;

    public static final DynamicHolder<List<DimensionType>> LAMP_DIMENSIONS;

    public static final DynamicHolder<Multimap<Item, FuelData>> BOILER_FUEL;
    public static final DynamicHolder<Multimap<Item, FuelData>> ICEBOX_FUEL;
    public static final DynamicHolder<Multimap<Item, FuelData>> HEARTH_FUEL;
    public static final DynamicHolder<Multimap<Item, FuelData>> SOULSPRING_LAMP_FUEL;

    public static final DynamicHolder<Boolean> HEARTH_POTIONS_ENABLED;
    public static final DynamicHolder<List<MobEffect>> HEARTH_POTION_BLACKLIST;

    public static final DynamicHolder<Boolean> HEAT_DRAINS_BACKTANK;
    public static final DynamicHolder<Boolean> COLD_DRAINS_BACKTANK;

    // Entity Settings
    public static final DynamicHolder<EntityDropData> FUR_TIMINGS;
    public static final DynamicHolder<EntityDropData> SHED_TIMINGS;
    public static final DynamicHolder<Multimap<Holder<Biome>, SpawnBiomeData>> ENTITY_SPAWN_BIOMES;
    public static final DynamicHolder<Multimap<EntityType<?>, MountData>> INSULATED_MOUNTS;
    public static final DynamicHolder<Multimap<EntityType<?>, EntityTempData>> ENTITY_TEMPERATURES;
    public static final DynamicHolder<Multimap<EntityType<?>, EntityClimateData>> ENTITY_CLIMATES;
    public static final DynamicHolder<Multimap<EntityType<?>, TempEffectsData>> ENTITY_TEMP_EFFECTS;
    public static final DynamicHolder<Boolean> ENABLE_ENTITY_CLIMATES;
    public static final DynamicHolder<Boolean> ADVANCED_ENTITY_TEMPERATURE;

    // Misc Settings
    public static final DynamicHolder<Double> INSULATION_STRENGTH;
    public static final DynamicHolder<List<ResourceLocation>> DISABLED_MODIFIERS;
    public static final DynamicHolder<Double> MODIFIER_TICK_RATE;
    public static final DynamicHolder<Double> DRYOFF_SPEED;
    public static final DynamicHolder<Double> ACCLIMATION_SPEED;
    public static final DynamicHolder<Pair<Double, Double>> MIN_ACCLIMATION_RANGE;
    public static final DynamicHolder<Pair<Double, Double>> MAX_ACCLIMATION_RANGE;
    public static final DynamicHolder<List<String>> DISABLED_MOD_COMPAT;

    // Client Settings
    /* NULL ON THE SERVER */
    public static final DynamicHolder<Boolean> CELSIUS;
    public static final DynamicHolder<Integer> TEMP_OFFSET;
    public static final DynamicHolder<Double> TEMP_SMOOTHING;

    public static final DynamicHolder<Vector2i> BODY_ICON_POS;
    public static final DynamicHolder<Boolean> BODY_ICON_ENABLED;
    public static final DynamicHolder<Boolean> MOVE_BODY_ICON_WHEN_ADVANCED;

    public static final DynamicHolder<Vector2i> BODY_READOUT_POS;
    public static final DynamicHolder<Boolean> BODY_READOUT_ENABLED;

    public static final DynamicHolder<Vector2i> WORLD_GAUGE_POS;
    public static final DynamicHolder<Boolean> WORLD_GAUGE_ENABLED;

    public static final DynamicHolder<Boolean> CUSTOM_HOTBAR_LAYOUT;
    public static final DynamicHolder<Boolean> ICON_BOBBING;

    public static final DynamicHolder<Boolean> HEARTH_DEBUG;

    public static final DynamicHolder<Boolean> SHOW_CONFIG_BUTTON;
    public static final DynamicHolder<Vector2i> CONFIG_BUTTON_POS;

    public static final DynamicHolder<Boolean> DISTORTION_EFFECTS;
    public static final DynamicHolder<Boolean> HIGH_CONTRAST;

    public static final DynamicHolder<Boolean> SHOW_CREATIVE_WARNING;
    public static final DynamicHolder<Boolean> HIDE_TOOLTIPS;
    public static final DynamicHolder<Boolean> EXPAND_TOOLTIPS;

    public static final DynamicHolder<WaterEffectSetting> WATER_EFFECT_SETTING;
    public static final DynamicHolder<IntegerBounds> WATER_DROPLET_SCALE;

    public static final DynamicHolder<Boolean> ANIMATED_SOULSPRING_LAMP_MODEL;

    public static final DynamicHolder<Preference.WaterskinAction> WATERSKIN_USE_PRIMARY;
    public static final DynamicHolder<Preference.WaterskinAction> WATERSKIN_USE_SECONDARY;

    public static final DynamicHolder<Boolean> SHOW_FROZEN_HEALTH;
    public static final DynamicHolder<Double> FREEZING_OVERLAY_OPACITY;
    public static final DynamicHolder<Double> SHIVER_INTENSITY;
    public static final DynamicHolder<Double> HEATSTROKE_BORDER_OPACITY;
    public static final DynamicHolder<Double> HEATSTROKE_BLUR_AMOUNT;
    public static final DynamicHolder<Double> HEATSTROKE_SWAY_AMOUNT;
    public static final DynamicHolder<Double> HEATSTROKE_SWAY_SPEED;


    // Makes the settings instantiation collapsible & easier to read
    static
    {
        DIFFICULTY = addSyncedSetting(ColdSweat.createKey("difficulty"), () -> Difficulty.NORMAL, holder -> holder.set(Difficulty.byId(MainSettingsConfig.DIFFICULTY.get())),
        Difficulty.CODEC,
        (difficulty) -> MainSettingsConfig.DIFFICULTY.set(difficulty.getId()),
        SyncType.BOTH_WAYS);

        MAX_TEMP = addSyncedSetting(ColdSweat.createKey("max_temp"), () -> 1.7, holder -> holder.set(MainSettingsConfig.MAX_HABITABLE_TEMPERATURE.get()),
        Codec.DOUBLE,
        (temp) -> MainSettingsConfig.MAX_HABITABLE_TEMPERATURE.set(temp),
        SyncType.BOTH_WAYS);

        MIN_TEMP = addSyncedSetting(ColdSweat.createKey("min_temp"), () -> 0.5, holder -> holder.set(MainSettingsConfig.MIN_HABITABLE_TEMPERATURE.get()),
        Codec.DOUBLE,
        (temp) -> MainSettingsConfig.MIN_HABITABLE_TEMPERATURE.set(temp),
        SyncType.BOTH_WAYS);

        TEMP_RATE = addSyncedSetting(ColdSweat.createKey("temp_rate"), () -> 1d, holder -> holder.set(MainSettingsConfig.TEMP_RATE_MULTIPLIER.get()),
        Codec.DOUBLE,
        (rate) -> MainSettingsConfig.TEMP_RATE_MULTIPLIER.set(rate),
        SyncType.BOTH_WAYS);

        TEMP_DAMAGE = addSyncedSetting(ColdSweat.createKey("temp_damage"), () -> 2d, holder -> holder.set(MainSettingsConfig.TEMP_DAMAGE.get()),
        Codec.DOUBLE,
        (damage) -> MainSettingsConfig.TEMP_DAMAGE.set(damage),
        SyncType.BOTH_WAYS);

        FIRE_RESISTANCE_ENABLED = addSyncedSetting(ColdSweat.createKey("fire_resistance_enabled"), () -> true, holder -> holder.set(ItemSettingsConfig.FIRE_RESISTANCE_BLOCKS_OVERHEATING.get()),
        Codec.BOOL,
        (enabled) -> ItemSettingsConfig.FIRE_RESISTANCE_BLOCKS_OVERHEATING.set(enabled),
        SyncType.BOTH_WAYS);

        ICE_RESISTANCE_ENABLED = addSyncedSetting(ColdSweat.createKey("ice_resistance_enabled"), () -> true, holder -> holder.set(ItemSettingsConfig.ICE_RESISTANCE_BLOCKS_FREEZING.get()),
        Codec.BOOL,
        (enabled) -> ItemSettingsConfig.ICE_RESISTANCE_BLOCKS_FREEZING.set(enabled),
        SyncType.BOTH_WAYS);

        USE_PEACEFUL_MODE = addSyncedSetting(ColdSweat.createKey("use_peaceful"), () -> true, holder -> holder.set(MainSettingsConfig.NULLIFY_IN_PEACEFUL.get()),
        Codec.BOOL,
        (usePeaceful) -> MainSettingsConfig.NULLIFY_IN_PEACEFUL.set(usePeaceful),
        SyncType.BOTH_WAYS);

        REQUIRE_THERMOMETER = addSyncedSetting(ColdSweat.createKey("require_thermometer"), () -> true, holder -> holder.set(ItemSettingsConfig.REQUIRE_THERMOMETER.get()),
        Codec.BOOL,
        (requireThermometer) -> ItemSettingsConfig.REQUIRE_THERMOMETER.set(requireThermometer),
        SyncType.BOTH_WAYS);

        GRACE_LENGTH = addSyncedSetting(ColdSweat.createKey("grace_length"), () -> 6000, holder -> holder.set(MainSettingsConfig.GRACE_PERIOD_LENGTH.get()),
        Codec.INT,
        (length) -> MainSettingsConfig.GRACE_PERIOD_LENGTH.set(length),
        SyncType.BOTH_WAYS);

        GRACE_ENABLED = addSyncedSetting(ColdSweat.createKey("grace_enabled"), () -> true, holder -> holder.set(MainSettingsConfig.ENABLE_GRACE_PERIOD.get()),
        Codec.BOOL,
        (enabled) -> MainSettingsConfig.ENABLE_GRACE_PERIOD.set(enabled),
        SyncType.BOTH_WAYS);


        HEARTS_FREEZING_PERCENTAGE = addSyncedSetting(ColdSweat.createKey("hearts_freezing_percentage"), () -> 0.5, holder -> holder.set(MainSettingsConfig.FREEZING_HEARTS.get()),
        Codec.DOUBLE,
        (percentage) -> MainSettingsConfig.FREEZING_HEARTS.set(percentage),
        SyncType.BOTH_WAYS);

        COLD_MINING_IMPAIRMENT = addSyncedSetting(ColdSweat.createKey("cold_mining_slowdown"), () -> 0.5, holder -> holder.set(MainSettingsConfig.COLD_MINING.get()),
        Codec.DOUBLE,
        (slowdown) -> MainSettingsConfig.COLD_MINING.set(slowdown),
        SyncType.BOTH_WAYS);

        COLD_MOVEMENT_SLOWDOWN = addSyncedSetting(ColdSweat.createKey("cold_movement_slowdown"), () -> 0.5, holder -> holder.set(MainSettingsConfig.COLD_MOVEMENT.get()),
        Codec.DOUBLE,
        (slowdown) -> MainSettingsConfig.COLD_MOVEMENT.set(slowdown),
        SyncType.BOTH_WAYS);

        COLD_KNOCKBACK_REDUCTION = addSyncedSetting(ColdSweat.createKey("cold_knockback_reduction"), () -> 0.5, holder -> holder.set(MainSettingsConfig.COLD_KNOCKBACK.get()),
        Codec.DOUBLE,
        (reduction) -> MainSettingsConfig.COLD_KNOCKBACK.set(reduction),
        SyncType.BOTH_WAYS);

        HEATSTROKE_FOG_DISTANCE = addSyncedSetting(ColdSweat.createKey("heatstroke_fog_distance"), () -> 6d, holder -> holder.set(MainSettingsConfig.HEATSTROKE_FOG.get()),
        Codec.DOUBLE,
        (distance) -> MainSettingsConfig.HEATSTROKE_FOG.set(distance),
        SyncType.BOTH_WAYS);

        BIOME_TEMPS = addSyncedSettingWithRegistries(ColdSweat.createKey("biome_temps"), HashMap::new, (holder, registryAccess) ->
        {
            Map<Holder<Biome>, BiomeTempData> dataMap = ConfigHelper.getRegistryMap(WorldSettingsConfig.BIOME_TEMPERATURES.get(), registryAccess, Registries.BIOME,
                                                                                    toml -> BiomeTempData.fromToml(toml, false, registryAccess),
                                                                                    data -> data.biomes().flatten());
            ConfigLoadingHandler.removeEntries(dataMap.values(), ModRegistries.BIOME_TEMP_DATA);

            holder.get(registryAccess).putAll(dataMap);
        },
                                                     ExtraCodecs.registryMapCodec(Registries.BIOME, BiomeTempData.CODEC),
                                                     (saver, registryAccess) -> {},
                                                     SyncType.ONE_WAY);

        BIOME_OFFSETS = addSyncedSettingWithRegistries(ColdSweat.createKey("biome_offsets"), FastMap::new, (holder, registryAccess) ->
        {
            Map<Holder<Biome>, BiomeTempData> dataMap = ConfigHelper.getRegistryMap(WorldSettingsConfig.BIOME_TEMP_OFFSETS.get(), registryAccess, Registries.BIOME,
                                                                                    toml -> BiomeTempData.fromToml(toml, true, registryAccess),
                                                                                    data -> data.biomes().flatten());
            ConfigLoadingHandler.removeEntries(dataMap.values(), ModRegistries.BIOME_TEMP_DATA);

            holder.get(registryAccess).putAll(dataMap);
        },
                                                       ExtraCodecs.registryMapCodec(Registries.BIOME, BiomeTempData.CODEC),
                                                       (saver, registryAccess) -> {},
                                                       SyncType.ONE_WAY);

        DIMENSION_TEMPS = addSyncedSettingWithRegistries(ColdSweat.createKey("dimension_temps"), FastMap::new, (holder, registryAccess) ->
        {
            Map<Holder<DimensionType>, DimensionTempData> dataMap = ConfigHelper.getRegistryMap(WorldSettingsConfig.DIMENSION_TEMPERATURES.get(), registryAccess, Registries.DIMENSION_TYPE,
                                                                                                toml -> DimensionTempData.fromToml(toml, false, registryAccess),
                                                                                                data -> data.dimensions().flatten());
            ConfigLoadingHandler.removeEntries(dataMap.values(), ModRegistries.DIMENSION_TEMP_DATA);

            holder.get(registryAccess).putAll(dataMap);
        },
                                                         ExtraCodecs.registryMapCodec(Registries.DIMENSION_TYPE, DimensionTempData.CODEC),
                                                         (saver, registryAccess) -> {},
                                                         SyncType.ONE_WAY);

        DIMENSION_OFFSETS = addSyncedSettingWithRegistries(ColdSweat.createKey("dimension_offsets"), FastMap::new, (holder, registryAccess) ->
        {
            Map<Holder<DimensionType>, DimensionTempData> dataMap = ConfigHelper.getRegistryMap(WorldSettingsConfig.DIMENSION_TEMP_OFFSETS.get(), registryAccess, Registries.DIMENSION_TYPE,
                                                                                                toml -> DimensionTempData.fromToml(toml, true, registryAccess),
                                                                                                data -> data.dimensions().flatten());
            ConfigLoadingHandler.removeEntries(dataMap.values(), ModRegistries.DIMENSION_TEMP_DATA);

            holder.get(registryAccess).putAll(dataMap);
        },
                                                           ExtraCodecs.registryMapCodec(Registries.DIMENSION_TYPE, DimensionTempData.CODEC),
                                                           (saver, registryAccess) -> {},
                                                           SyncType.ONE_WAY);

        STRUCTURE_TEMPS = addSettingWithRegistries(ColdSweat.createKey("structure_temperatures"), FastMap::new, (holder, registryAccess) ->
        {
            Map<Holder<Structure>, StructureTempData> dataMap = ConfigHelper.getRegistryMap(WorldSettingsConfig.STRUCTURE_TEMPERATURES.get(), registryAccess, Registries.STRUCTURE,
                                                                                            toml -> StructureTempData.fromToml(toml, false, registryAccess),
                                                                                            data -> data.structures().flatten());
            ConfigLoadingHandler.removeEntries(dataMap.values(), ModRegistries.STRUCTURE_TEMP_DATA);

            holder.get(registryAccess).putAll(dataMap);
        });

        STRUCTURE_OFFSETS = addSettingWithRegistries(ColdSweat.createKey("structure_offsets"), FastMap::new, (holder, registryAccess) ->
        {
            Map<Holder<Structure>, StructureTempData> dataMap = ConfigHelper.getRegistryMap(WorldSettingsConfig.STRUCTURE_TEMP_OFFSETS.get(), registryAccess, Registries.STRUCTURE,
                                                                                            toml -> StructureTempData.fromToml(toml, true, registryAccess),
                                                                                            data -> data.structures().flatten());
            ConfigLoadingHandler.removeEntries(dataMap.values(), ModRegistries.STRUCTURE_TEMP_DATA);

            holder.get(registryAccess).putAll(dataMap);
        });

        OVERCAST_TEMP_OFFSET = addSetting(ColdSweat.createKey("overcast_temp_offset"), () -> 0.35, holder ->
        {
            List<?> setting = WorldSettingsConfig.OVERCAST_TEMP_OFFSET.get();
            double temperature = ((Number) setting.get(0)).doubleValue();
            Temperature.Units units = setting.size() > 1
                                      ? Temperature.Units.fromID((String) setting.get(1))
                                      : Temperature.Units.MC;
            holder.set(Temperature.convert(temperature, units, Temperature.Units.MC, false));
        });

        DEPTH_REGIONS = addSetting(ColdSweat.createKey("depth_regions"), ArrayList::new, holder -> {});

        TriConsumer<FuelData.FuelType, ModConfigSpec.ConfigValue<List<? extends List<?>>>, DynamicHolder<Multimap<Item, FuelData>>> fuelAdder =
        (fuelType, config, holder) ->
        {
            Multimap<Item, FuelData> dataMap = ConfigHelper.parseTomlRegistry(config, list -> FuelData.fromToml(list, fuelType),
                                                                              data -> data.item().nestedFlatMap(ItemRequirement::items),
                                                                              BuiltInRegistries.ITEM, ModRegistries.FUEL_DATA);
            holder.get().putAll(dataMap);
        };
        BOILER_FUEL = addSetting(ColdSweat.createKey("boiler_fuel_items"), RegistryMultiMap::new, holder -> fuelAdder.accept(FuelData.FuelType.BOILER, ItemSettingsConfig.BOILER_FUELS, holder));
        ICEBOX_FUEL = addSetting(ColdSweat.createKey("icebox_fuel_items"), RegistryMultiMap::new, holder -> fuelAdder.accept(FuelData.FuelType.ICEBOX, ItemSettingsConfig.ICEBOX_FUELS, holder));
        HEARTH_FUEL = addSetting(ColdSweat.createKey("hearth_fuel_items"), RegistryMultiMap::new, holder -> fuelAdder.accept(FuelData.FuelType.HEARTH, ItemSettingsConfig.HEARTH_FUELS, holder));

        SOULSPRING_LAMP_FUEL = addSyncedSetting(ColdSweat.createKey("lamp_fuel_items"), RegistryMultiMap::new, holder -> fuelAdder.accept(FuelData.FuelType.SOUL_LAMP, ItemSettingsConfig.SOULSPRING_LAMP_FUELS, holder),
        ExtraCodecs.builtinMultimapCodec(BuiltInRegistries.ITEM, FuelData.CODEC),
        (saver) -> {},
        SyncType.ONE_WAY);

        HEARTH_POTIONS_ENABLED = addSetting(ColdSweat.createKey("hearth_potions_enabled"), () -> true, holder -> holder.set(ItemSettingsConfig.ALLOW_POTIONS_IN_HEARTH.get()));
        HEARTH_POTION_BLACKLIST = addSetting(ColdSweat.createKey("hearth_potion_blacklist"), ArrayList::new,
                                             holder -> holder.get().addAll(ItemSettingsConfig.HEARTH_POTION_BLACKLIST.get()
                                                       .stream()
                                                       .map(entry -> BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse(entry)))
                                                       .collect(ArrayList::new, List::add, List::addAll)));

        TriConsumer<ModConfigSpec.ConfigValue<List<? extends List<?>>>, DynamicHolder<Multimap<Item, InsulatorData>>, Insulation.Slot> insulatorAdder =
        (config, holder, slot) ->
        {
            Multimap<Item, InsulatorData> dataMap = ConfigHelper.parseTomlRegistry(config, list -> InsulatorData.fromToml(list, slot),
                                                                                   data -> data.item().nestedFlatMap(ItemRequirement::items),
                                                                                   BuiltInRegistries.ITEM, ModRegistries.INSULATOR_DATA);
            holder.get().putAll(dataMap);
        };
        INSULATION_ITEMS = addSyncedSetting(ColdSweat.createKey("insulation_items"), RegistryMultiMap::new, holder -> insulatorAdder.accept(ItemSettingsConfig.INSULATION_ITEMS, holder, Insulation.Slot.ITEM),
        ExtraCodecs.builtinMultimapCodec(BuiltInRegistries.ITEM, InsulatorData.CODEC),
        (saver) -> {},
        SyncType.ONE_WAY);

        INSULATING_ARMORS = addSyncedSetting(ColdSweat.createKey("insulating_armors"), RegistryMultiMap::new, holder -> insulatorAdder.accept(ItemSettingsConfig.INSULATING_ARMOR, holder, Insulation.Slot.ARMOR),
        ExtraCodecs.builtinMultimapCodec(BuiltInRegistries.ITEM, InsulatorData.CODEC),
        (saver) -> {},
        SyncType.ONE_WAY);

        INSULATING_CURIOS = addSyncedSetting(ColdSweat.createKey("insulating_curios"), RegistryMultiMap::new, holder ->
        {
            if (CompatManager.isCuriosLoaded())
            {   insulatorAdder.accept(ItemSettingsConfig.INSULATING_CURIOS, holder, Insulation.Slot.CURIO);
            }
        },
        ExtraCodecs.builtinMultimapCodec(BuiltInRegistries.ITEM, InsulatorData.CODEC),
        (saver) -> {},
        SyncType.ONE_WAY);

        INSULATION_SLOTS = addSyncedSetting(ColdSweat.createKey("insulation_slots"), () -> new ScalingFormula.Static(0, 0, 0, 0), holder ->
        {
            List<?> list = ItemSettingsConfig.INSULATION_SLOTS.get();
            // Handle legacy insulation notation
            if (list.size() == 4 && list.stream().allMatch(el -> el instanceof Integer))
            {   list = List.of("static", list.get(0), list.get(1), list.get(2), list.get(3));
            }
            String mode = ((String) list.get(0));

            ScalingFormula.Type scalingType = ScalingFormula.Type.byName(mode);
            List<? extends Number> values = list.subList(1, list.size()).stream().map(o -> (Number) o).toList();

            holder.set(scalingType == ScalingFormula.Type.STATIC
                                      ? new ScalingFormula.Static(values.get(0).intValue(),
                                                                  values.get(1).intValue(),
                                                                  values.get(2).intValue(),
                                                                  values.get(3).intValue())
                                      : new ScalingFormula.Dynamic(scalingType,
                                                                   values.get(0).doubleValue(),
                                                                   values.size() > 2 ? values.get(2).doubleValue() : Double.POSITIVE_INFINITY));
        },
        ScalingFormula.getCodec(),
        (saver) ->
        {
            List<?> list = ListBuilder.begin(saver.getType().getSerializedName())
                                      .addAll(saver.getValues())
                                      .build();
            ItemSettingsConfig.INSULATION_SLOTS.set(list);
        },
        SyncType.BOTH_WAYS);

        INSULATION_SLOT_OVERRIDES = addSyncedSetting(ColdSweat.createKey("insulation_slot_overrides"), RegistryMultiMap::new, holder ->
        {
            Multimap<Item, ItemInsulationSlotsData> dataMap = ConfigHelper.parseTomlRegistry(ItemSettingsConfig.INSULATION_SLOT_OVERRIDES,
                                                                                             ItemInsulationSlotsData::fromToml,
                                                                                             data -> data.item().nestedFlatMap(ItemRequirement::items),
                                                                                             BuiltInRegistries.ITEM, ModRegistries.INSULATION_SLOTS_DATA);
            holder.get().putAll(dataMap);
        },
        ExtraCodecs.builtinMultimapCodec(BuiltInRegistries.ITEM, ItemInsulationSlotsData.CODEC),
        (saver) -> {},
        SyncType.ONE_WAY);

        INSULATION_BLACKLIST = addSetting(ColdSweat.createKey("insulation_blacklist"), ArrayList::new,
                                          holder -> holder.get().addAll(ItemSettingsConfig.INSULATION_BLACKLIST.get()
                                                    .stream()
                                                    .map(entry -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry)))
                                                    .collect(ArrayList::new, List::add, List::addAll)));

        DRYING_ITEMS = addSyncedSetting(ColdSweat.createKey("drying_items"), RegistryMultiMap::new, holder ->
        {
            Multimap<Item, DryingItemData> dataMap = ConfigHelper.parseTomlRegistry(ItemSettingsConfig.DRYING_ITEMS,
                                                                                    DryingItemData::fromToml,
                                                                                    data -> data.item().nestedFlatMap(ItemRequirement::items),
                                                                                    BuiltInRegistries.ITEM, ModRegistries.DRYING_ITEM_DATA);
            holder.get().putAll(dataMap);
        },
        ExtraCodecs.builtinMultimapCodec(BuiltInRegistries.ITEM, DryingItemData.CODEC),
        (saver) -> {},
        SyncType.ONE_WAY);

        CHECK_SLEEP_CONDITIONS = addSetting(ColdSweat.createKey("check_sleep_conditions"), () -> true, holder -> holder.set(WorldSettingsConfig.SHOULD_CHECK_SLEEP.get()));

        SLEEP_CHECK_IGNORE_BLOCKS = addSyncedSetting(ColdSweat.createKey("sleep_check_override_blocks"), ArrayList::new, holder ->
        {
            var blocks = ConfigHelper.getBlocks(WorldSettingsConfig.SLEEPING_OVERRIDE_BLOCKS.get().toArray(new String[0]));
            holder.get().addAll(RegistryHelper.mapBuiltinRegistryTagList(BuiltInRegistries.BLOCK, blocks));
        },
        BuiltInRegistries.BLOCK.byNameCodec().listOf(),
        (saver) -> {},
        SyncType.ONE_WAY);

        USE_CUSTOM_WATER_FREEZE_BEHAVIOR = addSetting(ColdSweat.createKey("custom_freeze_check"), () -> true, holder -> holder.set(WorldSettingsConfig.CUSTOM_WATER_FREEZE_BEHAVIOR.get()));

        USE_CUSTOM_ICE_DROPS = addSetting(ColdSweat.createKey("custom_ice_drops"), () -> true, holder -> holder.set(WorldSettingsConfig.CUSTOM_ICE_DROPS.get()));

        FOOD_TEMPERATURES = addSyncedSetting(ColdSweat.createKey("food_temperatures"), RegistryMultiMap::new, holder ->
        {
            Multimap<Item, FoodData> dataMap = ConfigHelper.parseTomlRegistry(ItemSettingsConfig.FOOD_TEMPERATURES,
                                                                              FoodData::fromToml,
                                                                              data -> data.item().nestedFlatMap(ItemRequirement::items),
                                                                              BuiltInRegistries.ITEM, ModRegistries.FOOD_DATA);
            holder.get().putAll(dataMap);
        },
        ExtraCodecs.builtinMultimapCodec(BuiltInRegistries.ITEM, FoodData.CODEC),
        (saver) -> {},
        SyncType.ONE_WAY);

        CARRIED_ITEM_TEMPERATURES = addSyncedSetting(ColdSweat.createKey("carried_item_temps"), RegistryMultiMap::new, holder ->
        {
            Multimap<Item, ItemCarryTempData> dataMap = ConfigHelper.parseTomlRegistry(ItemSettingsConfig.CARRIED_ITEM_TEMPERATURES,
                                                                                       ItemCarryTempData::fromToml,
                                                                                       data -> data.item().nestedFlatMap(ItemRequirement::items),
                                                                                       BuiltInRegistries.ITEM, ModRegistries.CARRY_TEMP_DATA);
            holder.get().putAll(dataMap);
        },
        ExtraCodecs.builtinMultimapCodec(BuiltInRegistries.ITEM, ItemCarryTempData.CODEC),
        (saver) -> {},
        SyncType.ONE_WAY);

        WATERSKIN_CONSUME_STRENGTH = addSyncedSetting(ColdSweat.createKey("waterskin_consume_strength"), () -> 50, holder -> holder.set(ItemSettingsConfig.WATERSKIN_CONSUME_STRENGTH.get()),
        Codec.INT,
        (saver) -> ItemSettingsConfig.WATERSKIN_CONSUME_STRENGTH.set(saver),
        SyncType.BOTH_WAYS);

        WATERSKIN_HOTBAR_STRENGTH = addSyncedSetting(ColdSweat.createKey("waterskin_hotbar_strength"), () -> 1.0, holder -> holder.set(ItemSettingsConfig.WATERSKIN_HOTBAR_STRENGTH.get()),
        Codec.DOUBLE,
        (saver) -> ItemSettingsConfig.WATERSKIN_HOTBAR_STRENGTH.set(saver),
        SyncType.BOTH_WAYS);

        WATERSKIN_NEUTRALIZE_SPEED = addSyncedSetting(ColdSweat.createKey("waterskin_neutralize_speed"), () -> 1.0, holder -> holder.set(ItemSettingsConfig.WATERSKIN_NEUTRALIZE_SPEED.get()),
        Codec.DOUBLE,
        (saver) -> ItemSettingsConfig.WATERSKIN_NEUTRALIZE_SPEED.set(saver),
        SyncType.BOTH_WAYS);

        SOULSPRING_LAMP_STRENGTH = addSetting(ColdSweat.createKey("soulspring_lamp_strength"), () -> 0.6d, holder -> holder.set(ItemSettingsConfig.SOULSPRING_LAMP_STRENGTH.get()));

        LAMP_DIMENSIONS = addSettingWithRegistries(ColdSweat.createKey("valid_lamp_dimensions"), ArrayList::new,
                                                   (holder, registryAccess) -> holder.get(registryAccess).addAll(new ArrayList<>(ItemSettingsConfig.SOULSPRING_LAMP_DIMENSIONS.get()
                                                                           .stream()
                                                                           .map(entry -> registryAccess.registryOrThrow(Registries.DIMENSION_TYPE).get(ResourceLocation.parse(entry)))
                                                                           .collect(ArrayList::new, List::add, List::addAll))));

        HEAT_DRAINS_BACKTANK = addSetting(ColdSweat.createKey("heat_drains_backtank"), () -> true, holder ->
        {   if (CompatManager.isCreateLoaded()) holder.set(ItemSettingsConfig.HEAT_DRAINS_BACKTANK.get());
        });
        COLD_DRAINS_BACKTANK = addSetting(ColdSweat.createKey("cold_drains_backtank"), () -> false, holder ->
        {   if (CompatManager.isCreateLoaded()) holder.set(ItemSettingsConfig.COLD_DRAINS_BACKTANK.get());
        });

        FUR_TIMINGS = addSyncedSetting(ColdSweat.createKey("fur_timings"), () -> new EntityDropData(0, 0, 0d), holder ->
        {   List<?> entry = EntitySettingsConfig.GOAT_FUR_GROWTH_STATS.get();
            holder.set(new EntityDropData(((Number) entry.get(0)).intValue(), ((Number) entry.get(1)).intValue(), ((Number) entry.get(2)).doubleValue()));
        },
        EntityDropData.CODEC,
        (saver) ->
        {   List<Number> list = new ArrayList<>();
            list.add(saver.interval());
            list.add(saver.cooldown());
            list.add(saver.chance());
            EntitySettingsConfig.GOAT_FUR_GROWTH_STATS.set(list);
        },
        SyncType.BOTH_WAYS);

        SHED_TIMINGS = addSyncedSetting(ColdSweat.createKey("shed_timings"), () -> new EntityDropData(0, 0, 0d), holder ->
        {
            List<?> entry = EntitySettingsConfig.CHAMELEON_SHED_STATS.get();
            holder.set(new EntityDropData(((Number) entry.get(0)).intValue(), ((Number) entry.get(1)).intValue(), ((Number) entry.get(2)).doubleValue()));
        },
        EntityDropData.CODEC,
        (saver) ->
        {   List<Number> list = new ArrayList<>();
            list.add(saver.interval());
            list.add(saver.cooldown());
            list.add(saver.chance());
            EntitySettingsConfig.CHAMELEON_SHED_STATS.set(list);
        },
        SyncType.BOTH_WAYS);

        ENTITY_SPAWN_BIOMES = addSettingWithRegistries(ColdSweat.createKey("entity_spawn_biomes"), RegistryMultiMap::new, (holder, registryAccess) ->
        {
            // Function to read biomes from configs and put them in the config settings
            BiConsumer<List<? extends List<?>>, EntityType<?>> configReader = (configBiomes, entityType) ->
            {
                Multimap<Holder<Biome>, SpawnBiomeData> dataMap = ConfigHelper.getRegistryMultimap(configBiomes, registryAccess, Registries.BIOME,
                                                                                                   toml -> SpawnBiomeData.fromToml(toml, entityType, registryAccess), SpawnBiomeData::biomes);
                ConfigLoadingHandler.removeEntries(dataMap.values(), ModRegistries.ENTITY_SPAWN_BIOME_DATA);

                holder.get(registryAccess).putAll(dataMap);
            };

            // Parse goat and chameleon biomes
            configReader.accept(EntitySettingsConfig.CHAMELEON_SPAWN_BIOMES.get(), ModEntities.CHAMELEON.get());
            configReader.accept(EntitySettingsConfig.GOAT_SPAWN_BIOMES.get(), EntityType.GOAT);
        });

        INSULATED_MOUNTS = addSetting(ColdSweat.createKey("insulated_entities"), RegistryMultiMap::new, holder ->
        {
            Multimap<EntityType<?>, MountData> dataMap = ConfigHelper.parseTomlRegistry(EntitySettingsConfig.INSULATED_MOUNTS,
                                                                                        MountData::fromToml,
                                                                                        data -> data.entity().nestedFlatMap(EntityRequirement::entities),
                                                                                        BuiltInRegistries.ENTITY_TYPE, ModRegistries.MOUNT_DATA);
            holder.get().putAll(dataMap);
        });

        ENTITY_TEMPERATURES = addSetting(ColdSweat.createKey("entity_temperatures"), RegistryMultiMap::new, holder ->
        {
            Multimap<EntityType<?>, EntityTempData> dataMap = ConfigHelper.parseTomlRegistry(EntitySettingsConfig.ENTITY_TEMPERATURES,
                                                                                             EntityTempData::fromToml,
                                                                                             data -> data.entity().nestedFlatMap(EntityRequirement::entities),
                                                                                             BuiltInRegistries.ENTITY_TYPE, ModRegistries.ENTITY_TEMP_DATA);
            holder.get().putAll(dataMap);
        });

        ENTITY_CLIMATES = addSyncedSetting(ColdSweat.createKey("entity_climates"), RegistryMultiMap::new, holder ->
        {
            Multimap<EntityType<?>, EntityClimateData> dataMap = ConfigHelper.parseTomlRegistry(EntitySettingsConfig.ENTITY_CLIMATES,
                                                                                                 EntityClimateData::fromToml,
                                                                                                 data -> data.entity().nestedFlatMap(EntityRequirement::entities),
                                                                                                 BuiltInRegistries.ENTITY_TYPE, ModRegistries.ENTITY_CLIMATE_DATA);
            holder.get().putAll(dataMap);
        },
        ExtraCodecs.builtinMultimapCodec(BuiltInRegistries.ENTITY_TYPE, EntityClimateData.CODEC),
        (saver) -> {},
        SyncType.ONE_WAY);

        ENTITY_TEMP_EFFECTS = addSyncedSetting(ColdSweat.createKey("temp_effects"), RegistryMultiMap::new, holder -> {},
        ExtraCodecs.builtinMultimapCodec(BuiltInRegistries.ENTITY_TYPE, TempEffectsData.CODEC),
        (saver) -> {},
        SyncType.ONE_WAY);

        ENABLE_ENTITY_CLIMATES = addSyncedSetting(ColdSweat.createKey("enable_entity_climates"), () -> true, holder -> holder.set(EntitySettingsConfig.ENABLE_ENTITY_CLIMATES.get()),
        Codec.BOOL,
        (enabled) -> EntitySettingsConfig.ENABLE_ENTITY_CLIMATES.set(enabled),
        SyncType.BOTH_WAYS);

        ADVANCED_ENTITY_TEMPERATURE = addSyncedSetting(ColdSweat.createKey("advanced_entity_temperature"), () -> true, holder -> holder.set(EntitySettingsConfig.ADVANCED_ENTITY_TEMPERATURE.get()),
        Codec.BOOL,
        (enabled) -> EntitySettingsConfig.ADVANCED_ENTITY_TEMPERATURE.set(enabled),
        SyncType.BOTH_WAYS);

        BLOCK_RANGE = addSyncedSetting(ColdSweat.createKey("block_range"), () -> 7, holder -> holder.set(WorldSettingsConfig.MAX_BLOCK_TEMP_RANGE.get()),
        Codec.INT,
        (range) -> WorldSettingsConfig.MAX_BLOCK_TEMP_RANGE.set(range),
        SyncType.BOTH_WAYS);

        COLD_SOUL_FIRE = addSetting(ColdSweat.createKey("cold_soul_fire"), () -> true, holder -> holder.set(WorldSettingsConfig.IS_SOUL_FIRE_COLD.get()));

        THERMAL_SOURCE_SPREAD_WHITELIST = addSyncedSetting(ColdSweat.createKey("hearth_spread_whitelist"), ArrayList::new, holder ->
        {
            var blocks = ConfigHelper.getBlocks(WorldSettingsConfig.SOURCE_SPREAD_WHITELIST.get().toArray(new String[0]));
            holder.get().addAll(RegistryHelper.mapBuiltinRegistryTagList(BuiltInRegistries.BLOCK, blocks));
        },
        BuiltInRegistries.BLOCK.byNameCodec().listOf(),
        saver -> {},
        SyncType.ONE_WAY);

        THERMAL_SOURCE_SPREAD_BLACKLIST = addSyncedSetting(ColdSweat.createKey("hearth_spread_blacklist"), ArrayList::new, holder ->
        {
            var blocks = ConfigHelper.getBlocks(WorldSettingsConfig.SOURCE_SPREAD_BLACKLIST.get().toArray(new String[0]));
            holder.get().addAll(RegistryHelper.mapBuiltinRegistryTagList(BuiltInRegistries.BLOCK, blocks));
        },
        BuiltInRegistries.BLOCK.byNameCodec().listOf(),
        saver -> {},
        SyncType.ONE_WAY);

        THERMAL_SOURCE_STRENGTH = addSetting(ColdSweat.createKey("hearth_effect"), () -> 0.75, holder -> holder.set(WorldSettingsConfig.SOURCE_EFFECT_STRENGTH.get()));

        SMART_HEARTH = addSyncedSetting(ColdSweat.createKey("smart_hearth"), () -> false, holder -> holder.set(WorldSettingsConfig.ENABLE_SMART_HEARTH.get()),
        Codec.BOOL,
        (saver) -> WorldSettingsConfig.ENABLE_SMART_HEARTH.set(saver),
        SyncType.BOTH_WAYS);

        SMART_BOILER = addSyncedSetting(ColdSweat.createKey("smart_boiler"), () -> false, holder -> holder.set(WorldSettingsConfig.ENABLE_SMART_BOILER.get()),
        Codec.BOOL,
        (saver) -> WorldSettingsConfig.ENABLE_SMART_BOILER.set(saver),
        SyncType.BOTH_WAYS);

        SMART_ICEBOX = addSyncedSetting(ColdSweat.createKey("smart_icebox"), () -> false, holder -> holder.set(WorldSettingsConfig.ENABLE_SMART_ICEBOX.get()),
        Codec.BOOL,
        (saver) -> WorldSettingsConfig.ENABLE_SMART_ICEBOX.set(saver),
        SyncType.BOTH_WAYS);

        HEARTH_MAX_RANGE = addSyncedSetting(ColdSweat.createKey("hearth_max_range"), () -> 16, holder -> holder.set(WorldSettingsConfig.HEARTH_MAX_RANGE.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.HEARTH_MAX_RANGE.set(saver),
        SyncType.BOTH_WAYS);

        HEARTH_RANGE = addSyncedSetting(ColdSweat.createKey("hearth_range"), () -> 8, holder -> holder.set(WorldSettingsConfig.HEARTH_RANGE.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.HEARTH_RANGE.set(saver),
        SyncType.BOTH_WAYS);

        HEARTH_MAX_VOLUME = addSyncedSetting(ColdSweat.createKey("hearth_max_volume"), () -> 1000, holder -> holder.set(WorldSettingsConfig.HEARTH_MAX_VOLUME.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.HEARTH_MAX_VOLUME.set(saver),
        SyncType.BOTH_WAYS);

        HEARTH_WARM_UP_TIME = addSyncedSetting(ColdSweat.createKey("hearth_warm_up_time"), () -> 20, holder -> holder.set(WorldSettingsConfig.HEARTH_WARM_UP_TIME.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.HEARTH_WARM_UP_TIME.set(saver),
        SyncType.BOTH_WAYS);

        HEARTH_MAX_INSULATION = addSyncedSetting(ColdSweat.createKey("hearth_max_insulation"), () -> 1, holder -> holder.set(WorldSettingsConfig.HEARTH_MAX_INSULATION.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.HEARTH_MAX_INSULATION.set(saver),
        SyncType.BOTH_WAYS);

        HEARTH_FUEL_INTERVAL = addSyncedSetting(ColdSweat.createKey("hearth_fuel_rate"), () -> 1, holder -> holder.set(WorldSettingsConfig.HEARTH_FUEL_INTERVAL.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.HEARTH_FUEL_INTERVAL.set(saver),
        SyncType.BOTH_WAYS);

        BOILER_MAX_RANGE = addSyncedSetting(ColdSweat.createKey("boiler_max_range"), () -> 16, holder -> holder.set(WorldSettingsConfig.BOILER_MAX_RANGE.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.BOILER_MAX_RANGE.set(saver),
        SyncType.BOTH_WAYS);

        BOILER_RANGE = addSyncedSetting(ColdSweat.createKey("boiler_range"), () -> 8, holder -> holder.set(WorldSettingsConfig.BOILER_RANGE.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.BOILER_RANGE.set(saver),
        SyncType.BOTH_WAYS);

        BOILER_MAX_VOLUME = addSyncedSetting(ColdSweat.createKey("boiler_max_volume"), () -> 1000, holder -> holder.set(WorldSettingsConfig.BOILER_MAX_VOLUME.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.BOILER_MAX_VOLUME.set(saver),
        SyncType.BOTH_WAYS);

        BOILER_WARM_UP_TIME = addSyncedSetting(ColdSweat.createKey("boiler_warm_up_time"), () -> 20, holder -> holder.set(WorldSettingsConfig.BOILER_WARM_UP_TIME.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.BOILER_WARM_UP_TIME.set(saver),
        SyncType.BOTH_WAYS);

        BOILER_MAX_INSULATION = addSyncedSetting(ColdSweat.createKey("boiler_max_insulation"), () -> 1, holder -> holder.set(WorldSettingsConfig.BOILER_MAX_INSULATION.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.BOILER_MAX_INSULATION.set(saver),
        SyncType.BOTH_WAYS);

        BOILER_FUEL_INTERVAL = addSyncedSetting(ColdSweat.createKey("boiler_fuel_rate"), () -> 1, holder -> holder.set(WorldSettingsConfig.BOILER_FUEL_INTERVAL.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.BOILER_FUEL_INTERVAL.set(saver),
        SyncType.BOTH_WAYS);

        ICEBOX_MAX_RANGE = addSyncedSetting(ColdSweat.createKey("icebox_max_range"), () -> 16, holder -> holder.set(WorldSettingsConfig.ICEBOX_MAX_RANGE.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.ICEBOX_MAX_RANGE.set(saver),
        SyncType.BOTH_WAYS);

        ICEBOX_RANGE = addSyncedSetting(ColdSweat.createKey("icebox_range"), () -> 8, holder -> holder.set(WorldSettingsConfig.ICEBOX_RANGE.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.ICEBOX_RANGE.set(saver),
        SyncType.BOTH_WAYS);

        ICEBOX_MAX_VOLUME = addSyncedSetting(ColdSweat.createKey("icebox_max_volume"), () -> 1000, holder -> holder.set(WorldSettingsConfig.ICEBOX_MAX_VOLUME.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.ICEBOX_MAX_VOLUME.set(saver),
        SyncType.BOTH_WAYS);

        ICEBOX_WARM_UP_TIME = addSyncedSetting(ColdSweat.createKey("icebox_warm_up_time"), () -> 20, holder -> holder.set(WorldSettingsConfig.ICEBOX_WARM_UP_TIME.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.ICEBOX_WARM_UP_TIME.set(saver),
        SyncType.BOTH_WAYS);

        ICEBOX_MAX_INSULATION = addSyncedSetting(ColdSweat.createKey("icebox_max_insulation"), () -> 1, holder -> holder.set(WorldSettingsConfig.ICEBOX_MAX_INSULATION.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.ICEBOX_MAX_INSULATION.set(saver),
        SyncType.BOTH_WAYS);

        ICEBOX_FUEL_INTERVAL = addSyncedSetting(ColdSweat.createKey("icebox_fuel_rate"), () -> 1, holder -> holder.set(WorldSettingsConfig.ICEBOX_FUEL_INTERVAL.get()),
        Codec.INT,
        (saver) -> WorldSettingsConfig.ICEBOX_FUEL_INTERVAL.set(saver),
        SyncType.BOTH_WAYS);

        THERMOREGULATOR_INSULATION = addSyncedSetting(ColdSweat.createKey("thermoregulator_insulation"), () -> 1, holder ->
        {   if (CompatManager.isToughAsNailsLoaded())
            {   holder.set(WorldSettingsConfig.THERMOREGULATOR_INSULATION.get());
            }
        },
        Codec.INT,
        (saver) ->
        {   if (CompatManager.isToughAsNailsLoaded())
            {   WorldSettingsConfig.THERMOREGULATOR_INSULATION.set(saver);
            }
        },
        SyncType.BOTH_WAYS);

        INSULATION_STRENGTH = addSyncedSetting(ColdSweat.createKey("insulation_strength"), () -> 1d, holder -> holder.set(ItemSettingsConfig.INSULATION_STRENGTH.get()),
        Codec.DOUBLE,
        (saver) -> ItemSettingsConfig.INSULATION_STRENGTH.set(saver),
        SyncType.BOTH_WAYS);

        DISABLED_MODIFIERS = addSetting(ColdSweat.createKey("disabled_modifiers"), ArrayList::new, holder -> holder.get().addAll(MainSettingsConfig.DISABLED_TEMP_MODIFIERS.get().stream().map(ResourceLocation::parse).toList()));

        MODIFIER_TICK_RATE = addSyncedSetting(ColdSweat.createKey("modifier_tick_rate"), () -> 1.0, holder -> holder.set(MainSettingsConfig.MODIFIER_TICK_RATE.get()),
        Codec.DOUBLE,
        (saver) -> MainSettingsConfig.MODIFIER_TICK_RATE.set(saver),
        SyncType.BOTH_WAYS);

        DRYOFF_SPEED = addSyncedSetting(ColdSweat.createKey("dryoff_speed"), () -> 1.0, holder -> holder.set(MainSettingsConfig.DRYOFF_SPEED.get()),
        Codec.DOUBLE,
        (saver) -> MainSettingsConfig.DRYOFF_SPEED.set(saver),
        SyncType.BOTH_WAYS);

        ACCLIMATION_SPEED = addSyncedSetting(ColdSweat.createKey("acclimation_speed"), () -> 1.0, holder -> holder.set(MainSettingsConfig.ACCLIMATION_SPEED.get()),
        Codec.DOUBLE,
        (saver) -> MainSettingsConfig.ACCLIMATION_SPEED.set(saver),
        SyncType.BOTH_WAYS);

        MIN_ACCLIMATION_RANGE = addSyncedSetting(ColdSweat.createKey("min_acclimation_range"), () -> Pair.of(0.0, 0.0), holder ->
        {
            List<? extends Number> range = MainSettingsConfig.MIN_ACCLIMATION_RANGE.get();
            holder.set(Pair.of(range.get(0).doubleValue(), range.get(1).doubleValue()));
        },
                                                 ExtraCodecs.pair(Codec.DOUBLE, Codec.DOUBLE),
                                                 (saver) -> MainSettingsConfig.MIN_ACCLIMATION_RANGE.set(List.of(saver.getFirst(), saver.getSecond())),
                                                 SyncType.BOTH_WAYS);

        MAX_ACCLIMATION_RANGE = addSyncedSetting(ColdSweat.createKey("max_acclimation_range"), () -> Pair.of(0.0, 0.0), holder ->
        {
            List<? extends Number> range = MainSettingsConfig.MAX_ACCLIMATION_RANGE.get();
            holder.set(Pair.of(range.get(0).doubleValue(), range.get(1).doubleValue()));
        },
                                                 ExtraCodecs.pair(Codec.DOUBLE, Codec.DOUBLE),
                                                 (saver) -> MainSettingsConfig.MAX_ACCLIMATION_RANGE.set(List.of(saver.getFirst(), saver.getSecond())),
                                                 SyncType.BOTH_WAYS);

        DISABLED_MOD_COMPAT = addSetting(ColdSweat.createKey("disabled_mod_compat"), ArrayList::new, holder ->
        {
            File disabledModsFile = FMLPaths.CONFIGDIR.get().resolve("coldsweat").resolve("disabled_mods.txt").toFile();
            // Create file if it doesn't exist
            if (!disabledModsFile.exists())
            {
                try
                {   Files.createDirectories(disabledModsFile.getParentFile().toPath());
                    Files.createFile(disabledModsFile.toPath());

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(disabledModsFile)))
                    {   writer.write("""
                                            # Cold Sweat will not enable extra compatibility features for mods listed here.
                                            # List one mod ID per line, no spaces or punctuation (including quotes).""");
                    }
                }
                catch (IOException e)
                {   ColdSweat.LOGGER.error("Failed to create disabled mods file", e);
                }
            }
            // Read disabled mods from file
            try (BufferedReader reader = new BufferedReader(new FileReader(disabledModsFile)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {   if (!line.isBlank() && !line.startsWith("#"))
                    {   holder.get().add(line.trim());
                    }
                }
            }
            catch (IOException e)
            {   ColdSweat.LOGGER.error("Failed to read disabled mods file", e);
            }
        });


        // Client

        CELSIUS = addClientSetting(ColdSweat.createKey("celsius"), () -> false, holder -> holder.set(ClientSettingsConfig.USE_CELSIUS.get()));

        TEMP_OFFSET = addClientSetting(ColdSweat.createKey("temp_offset"), () -> 0, holder -> holder.set(ClientSettingsConfig.TEMPERATURE_OFFSET.get()));

        TEMP_SMOOTHING = addClientSetting(ColdSweat.createKey("temp_smoothing"), () -> 10d, holder -> holder.set(ClientSettingsConfig.TEMPERATURE_SMOOTHING.get()));

        BODY_ICON_POS = addClientSetting(ColdSweat.createKey("body_icon_pos"), Vector2i::new, holder -> holder.set(ClientSettingsConfig.getBodyIconPos()));
        BODY_ICON_ENABLED = addClientSetting(ColdSweat.createKey("body_icon_enabled"), () -> true, holder -> holder.set(ClientSettingsConfig.SHOW_BODY_TEMP_ICON.get()));

        MOVE_BODY_ICON_WHEN_ADVANCED = addClientSetting(ColdSweat.createKey("move_body_icon_for_advanced"), () -> true, holder -> holder.set(ClientSettingsConfig.MOVE_BODY_TEMP_ICON_ADVANCED.get()));

        BODY_READOUT_POS = addClientSetting(ColdSweat.createKey("body_readout_pos"), Vector2i::new, holder -> holder.set(ClientSettingsConfig.getBodyReadoutPos()));
        BODY_READOUT_ENABLED = addClientSetting(ColdSweat.createKey("body_readout_enabled"), () -> true, holder -> holder.set(ClientSettingsConfig.SHOW_BODY_TEMP_READOUT.get()));

        WORLD_GAUGE_POS = addClientSetting(ColdSweat.createKey("world_gauge_pos"), Vector2i::new, holder -> holder.set(ClientSettingsConfig.getWorldGaugePos()));
        WORLD_GAUGE_ENABLED = addClientSetting(ColdSweat.createKey("world_gauge_enabled"), () -> true, holder -> holder.set(ClientSettingsConfig.SHOW_WORLD_TEMP_GAUGE.get()));

        CUSTOM_HOTBAR_LAYOUT = addClientSetting(ColdSweat.createKey("custom_hotbar_layout"), () -> true, holder -> holder.set(ClientSettingsConfig.USE_CUSTOM_HOTBAR_LAYOUT.get()));
        ICON_BOBBING = addClientSetting(ColdSweat.createKey("icon_bobbing"), () -> true, holder -> holder.set(ClientSettingsConfig.ENABLE_ICON_BOBBING.get()));

        HEARTH_DEBUG = addClientSetting(ColdSweat.createKey("hearth_debug"), () -> true, holder -> holder.set(ClientSettingsConfig.SHOW_HEARTH_DEBUG_VISUALS.get()));

        SHOW_CONFIG_BUTTON = addClientSetting(ColdSweat.createKey("show_config_button"), () -> true, holder -> holder.set(ClientSettingsConfig.SHOW_CONFIG_BUTTON.get()));
        CONFIG_BUTTON_POS = addClientSetting(ColdSweat.createKey("config_button_pos"), Vector2i::new, holder -> holder.set(ClientSettingsConfig.getConfigButtonPos()));

        DISTORTION_EFFECTS = addClientSetting(ColdSweat.createKey("distortion_effects"), () -> true, holder -> holder.set(ClientSettingsConfig.SHOW_SCREEN_DISTORTIONS.get()));

        HIGH_CONTRAST = addClientSetting(ColdSweat.createKey("high_contrast"), () -> false, holder -> holder.set(ClientSettingsConfig.HIGH_CONTRAST_MODE.get()));

        SHOW_CREATIVE_WARNING = addClientSetting(ColdSweat.createKey("show_creative_warning"), () -> true, holder -> holder.set(ClientSettingsConfig.ENABLE_CREATIVE_WARNING.get()));

        HIDE_TOOLTIPS = addClientSetting(ColdSweat.createKey("show_creative_warning"), () -> false, holder -> holder.set(ClientSettingsConfig.HIDE_INSULATION_TOOLTIPS.get()));
        EXPAND_TOOLTIPS = addClientSetting(ColdSweat.createKey("expand_tooltips"), () -> true, holder -> holder.set(ClientSettingsConfig.EXPAND_TOOLTIPS.get()));

        WATER_EFFECT_SETTING = addClientSetting(ColdSweat.createKey("show_water_effect"), () -> WaterEffectSetting.ALL, holder -> holder.set(WaterEffectSetting.values()[ClientSettingsConfig.WATER_EFFECT_SETTING.get()]));
        WATER_DROPLET_SCALE = addClientSetting(ColdSweat.createKey("water_droplet_scale"), () -> new IntegerBounds(40, 48), holder -> holder.set(new IntegerBounds(ClientSettingsConfig.WATER_DROPLET_SCALE.get().toArray(Integer[]::new))));

        SHOW_FROZEN_HEALTH = addClientSetting(ColdSweat.createKey("show_frozen_health"), () -> true, holder -> holder.set(ClientSettingsConfig.SHOW_FROZEN_HEALTH.get()));
        FREEZING_OVERLAY_OPACITY = addClientSetting(ColdSweat.createKey("freezing_overlay_opacity"), () -> 0.5, holder -> holder.set(ClientSettingsConfig.FREEZING_OVERLAY_OPACITY.get()));
        SHIVER_INTENSITY = addClientSetting(ColdSweat.createKey("shiver_intensity"), () -> 1.0, holder -> holder.set(ClientSettingsConfig.SHIVER_INTENSITY.get()));
        HEATSTROKE_BORDER_OPACITY = addClientSetting(ColdSweat.createKey("heatstroke_border_opacity"), () -> 0.5, holder -> holder.set(ClientSettingsConfig.HEATSTROKE_BORDER_OPACITY.get()));
        HEATSTROKE_BLUR_AMOUNT = addClientSetting(ColdSweat.createKey("heatstroke_blur_amount"), () -> 1.0, holder -> holder.set(ClientSettingsConfig.HEATSTROKE_BLUR.get()));
        HEATSTROKE_SWAY_AMOUNT = addClientSetting(ColdSweat.createKey("heatstroke_sway_amount"), () -> 1.0, holder -> holder.set(ClientSettingsConfig.HEATSTROKE_SWAY_AMOUNT.get()));
        HEATSTROKE_SWAY_SPEED = addClientSetting(ColdSweat.createKey("heatstroke_sway_speed"), () -> 1.0, holder -> holder.set(ClientSettingsConfig.HEATSTROKE_SWAY_SPEED.get()));

        ANIMATED_SOULSPRING_LAMP_MODEL = addClientSetting(ColdSweat.createKey("animated_soulspring_lamp_model"), () -> true, holder -> holder.set(ClientSettingsConfig.ANIMATED_SOUL_SPRING_LAMP.get()));

        WATERSKIN_USE_PRIMARY = addClientSetting(ColdSweat.createKey("waterskin_primary_action"), () -> Preference.WaterskinAction.POUR,
            holder -> holder.set(Preference.WaterskinAction.byName(ClientSettingsConfig.WATERSKIN_DRINK_PRIMARY.get())));
        WATERSKIN_USE_SECONDARY = addClientSetting(ColdSweat.createKey("waterskin_secondary_action"), () -> Preference.WaterskinAction.DRINK,
            holder -> holder.set(Preference.WaterskinAction.byName(ClientSettingsConfig.WATERSKIN_DRINK_SECONDARY.get())));

        SUMMER_TEMPS = addSetting(ColdSweat.createKey("summer_temps"), SeasonalTempData::new, holder -> holder.set(!CompatManager.getSeasonsMods().isEmpty() ? SeasonalTempData.fromToml(WorldSettingsConfig.getSummerTemps()) : new SeasonalTempData()));
        AUTUMN_TEMPS = addSetting(ColdSweat.createKey("autumn_temps"), SeasonalTempData::new, holder -> holder.set(!CompatManager.getSeasonsMods().isEmpty() ? SeasonalTempData.fromToml(WorldSettingsConfig.getAutumnTemps()) : new SeasonalTempData()));
        WINTER_TEMPS = addSetting(ColdSweat.createKey("winter_temps"), SeasonalTempData::new, holder -> holder.set(!CompatManager.getSeasonsMods().isEmpty() ? SeasonalTempData.fromToml(WorldSettingsConfig.getWinterTemps()) : new SeasonalTempData()));
        SPRING_TEMPS = addSetting(ColdSweat.createKey("spring_temps"), SeasonalTempData::new, holder -> holder.set(!CompatManager.getSeasonsMods().isEmpty() ? SeasonalTempData.fromToml(WorldSettingsConfig.getSpringTemps()) : new SeasonalTempData()));
    }

    public static ResourceLocation getKey(DynamicHolder<?> setting)
    {   return CONFIG_SETTINGS.inverse().get(setting);
    }

    public static DynamicHolder<?> getSetting(ResourceLocation key)
    {   return CONFIG_SETTINGS.get(key);
    }

    public enum Difficulty implements StringRepresentable
    {
        SUPER_EASY("super_easy", () -> Map.of(
            getKey(MIN_TEMP), () -> Temperature.convert(40, Temperature.Units.F, Temperature.Units.MC, true),
            getKey(MAX_TEMP), () -> Temperature.convert(120, Temperature.Units.F, Temperature.Units.MC, true),
            getKey(TEMP_RATE), () -> 0.5,
            getKey(REQUIRE_THERMOMETER), () -> false,
            getKey(FIRE_RESISTANCE_ENABLED), () -> true,
            getKey(ICE_RESISTANCE_ENABLED), () -> true
        )),

        EASY("easy", () -> Map.of(
            getKey(MIN_TEMP), () -> Temperature.convert(45, Temperature.Units.F, Temperature.Units.MC, true),
            getKey(MAX_TEMP), () -> Temperature.convert(110, Temperature.Units.F, Temperature.Units.MC, true),
            getKey(TEMP_RATE), () -> 0.75,
            getKey(REQUIRE_THERMOMETER), () -> false,
            getKey(FIRE_RESISTANCE_ENABLED), () -> true,
            getKey(ICE_RESISTANCE_ENABLED), () -> true
        )),

        NORMAL("normal", () -> Map.of(
            getKey(MIN_TEMP), () -> Temperature.convert(50, Temperature.Units.F, Temperature.Units.MC, true),
            getKey(MAX_TEMP), () -> Temperature.convert(100, Temperature.Units.F, Temperature.Units.MC, true),
            getKey(TEMP_RATE), () -> 1.0,
            getKey(REQUIRE_THERMOMETER), () -> true,
            getKey(FIRE_RESISTANCE_ENABLED), () -> true,
            getKey(ICE_RESISTANCE_ENABLED), () -> true
        )),

        HARD("hard", () -> Map.of(
            getKey(MIN_TEMP), () -> Temperature.convert(55, Temperature.Units.F, Temperature.Units.MC, true),
            getKey(MAX_TEMP), () -> Temperature.convert(90, Temperature.Units.F, Temperature.Units.MC, true),
            getKey(TEMP_RATE), () -> 1.25,
            getKey(REQUIRE_THERMOMETER), () -> true,
            getKey(FIRE_RESISTANCE_ENABLED), () -> false,
            getKey(ICE_RESISTANCE_ENABLED), () -> false
        )),

        CUSTOM("custom", () -> Map.of());

        public static final Codec<Difficulty> CODEC = StringRepresentable.fromEnum(Difficulty::values);

        private final Supplier<Map<ResourceLocation, Supplier<?>>> settingsSupplier;
        private Map<ResourceLocation, Supplier<?>> settings;
        String name;

        Difficulty(String name, Supplier<Map<ResourceLocation, Supplier<?>>> settings)
        {   this.settingsSupplier = settings;
            this.name = name;
        }

        private void ensureSettingsGenerated()
        {   if (settings == null) settings = settingsSupplier.get();
        }

        public <T> T getSetting(ResourceLocation id)
        {
            this.ensureSettingsGenerated();
            return (T) settings.get(id).get();
        }

        public <T> T getSetting(DynamicHolder<T> config)
        {
            this.ensureSettingsGenerated();
            return (T) settings.get(getKey(config)).get();
        }

        public <T> T getOrDefault(ResourceLocation id, T defaultValue)
        {
            this.ensureSettingsGenerated();
            return (T) settings.getOrDefault(id, () -> defaultValue).get();
        }

        public <T> T getOrDefault(DynamicHolder<T> config, T defaultValue)
        {
            this.ensureSettingsGenerated();
            return (T) settings.getOrDefault(getKey(config), () -> defaultValue).get();
        }

        public void load()
        {
            this.ensureSettingsGenerated();
            settings.forEach((id, loader) -> ConfigSettings.getSetting(id).setUnsafe(loader.get()));
        }

        public String getId()
        {   return this.name;
        }

        @Nullable
        public static Difficulty byId(String id)
        {
            for (Difficulty difficulty : values())
            {   if (difficulty.name.equals(id))
                {   return difficulty;
                }
            }
            return null;
        }

        @Override
        public String getSerializedName()
        {   return name;
        }

        public static Component getFormattedName(Difficulty difficulty)
        {
            return switch (difficulty)
            {   case SUPER_EASY  -> Component.translatable("cold_sweat.config.difficulty.super_easy.name");
                case EASY  -> Component.translatable("cold_sweat.config.difficulty.easy.name");
                case NORMAL  -> Component.translatable("cold_sweat.config.difficulty.normal.name");
                case HARD  -> Component.translatable("cold_sweat.config.difficulty.hard.name");
                default -> Component.translatable("cold_sweat.config.difficulty.custom.name");
            };
        }
    }

    public static <T> DynamicHolder<T> addSetting(ResourceLocation id, Supplier<T> defaultVal, Consumer<DynamicHolder<T>> loader)
    {   DynamicHolder<T> holder = DynamicHolder.create(id, defaultVal, loader);
        CONFIG_SETTINGS.put(id, holder);
        return holder;
    }

    public static <T> DynamicHolder<T> addSettingWithRegistries(ResourceLocation id, Supplier<T> defaultVal, DynamicHolder.Loader<T> loader)
    {   DynamicHolder<T> holder = DynamicHolder.createWithRegistries(id, defaultVal, loader);
        CONFIG_SETTINGS.put(id, holder);
        return holder;
    }

    public static <T> DynamicHolder<T> addSyncedSetting(ResourceLocation id, Supplier<T> defaultVal, Consumer<DynamicHolder<T>> loader, Codec<T> codec, Consumer<T> saver, SyncType syncType)
    {   DynamicHolder<T> holder = DynamicHolder.createSynced(id, defaultVal, loader, codec, saver, syncType);
        CONFIG_SETTINGS.put(id, holder);
        return holder;
    }

    public static <T> DynamicHolder<T> addSyncedSettingWithRegistries(ResourceLocation id, Supplier<T> defaultVal, DynamicHolder.Loader<T> loader, Codec<T> codec,
                                                                      DynamicHolder.Saver<T> saver, SyncType syncType)
    {   DynamicHolder<T> holder = DynamicHolder.createSyncedWithRegistries(id, defaultVal, loader, codec, saver, syncType);
        CONFIG_SETTINGS.put(id, holder);
        return holder;
    }

    public static <T> DynamicHolder<T> addClientSetting(ResourceLocation id, Supplier<T> defaultVal, Consumer<DynamicHolder<T>> loader)
    {
        if (EffectiveSide.get().isClient())
        {
            DynamicHolder<T> holder = DynamicHolder.create(id, defaultVal, loader);
            CLIENT_SETTINGS.put(id, holder);
            return holder;
        }
        else return DynamicHolder.create(id, () -> null, (value) -> {});
    }

    public static CompoundTag encode(RegistryAccess registryAccess)
    {
        CompoundTag map = new CompoundTag();
        CONFIG_SETTINGS.forEach((key, value) ->
        {
            if (value.getSyncType().canSend())
            {   CompoundTag encoded = value.encode(registryAccess);
                map.merge(encoded);
            }
        });
        return map;
    }

    public static void decode(CompoundTag tag, RegistryAccess registryAccess)
    {
        for (DynamicHolder<?> config : CONFIG_SETTINGS.values())
        {
            if (config.getSyncType().canReceive())
            {   config.decode(tag, registryAccess);
            }
        }
    }

    public static void saveValues(RegistryAccess registryAccess)
    {
        CONFIG_SETTINGS.values().forEach(value ->
        {   if (value.isSynced())
            {   value.save(registryAccess);
            }
        });
    }

    public static void load(RegistryAccess registryAccess, boolean replace)
    {
        if (registryAccess != null)
        {   CONFIG_SETTINGS.values().forEach(dynamicHolder -> dynamicHolder.load(registryAccess, replace));
        }
        else
        {
            ColdSweat.LOGGER.warn("Loading Cold Sweat config settings without registry access. This is normal during startup.");
            CONFIG_SETTINGS.values().forEach(dynamicHolder ->
            {
                if (!dynamicHolder.requiresRegistries())
                {   dynamicHolder.load(replace);
                }
            });
        }
    }

    public static void clear()
    {
        for (Map.Entry<ResourceLocation, DynamicHolder<?>> entry : CONFIG_SETTINGS.entrySet())
        {   entry.getValue().reset();
        }
    }

    private static <K, V> void putRegistryEntries(Multimap<K, V> map, Registry<K> registry, List<Either<TagKey<K>, K>> list, V data)
    {
        RegistryHelper.mapBuiltinRegistryTagList(registry, list).forEach(entry -> map.put(entry, data));
    }

    public enum WaterEffectSetting implements StringRepresentable
    {
        OFF("options.off", false, false),
        PARTICLES("cold_sweat.config.show_water_effect.particles", true, false),
        SCREEN("cold_sweat.config.show_water_effect.screen", false, true),
        ALL("cold_sweat.config.show_water_effect.all", true, true);

        private final String translationKey;
        private final boolean showParticles;
        private final boolean showGui;

        WaterEffectSetting(String translationKey, boolean showParticles, boolean showGui)
        {   this.translationKey = translationKey;
            this.showParticles = showParticles;
            this.showGui = showGui;
        }

        @Override
        public String getSerializedName()
        {   return this.translationKey;
        }

        public boolean showParticles()
        {   return this.showParticles;
        }

        public boolean showGui()
        {   return this.showGui;
        }
    }
}
