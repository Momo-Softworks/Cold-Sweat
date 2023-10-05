package com.momosoftworks.coldsweat.config;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.util.ValueHolder;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.Pair;
import com.momosoftworks.coldsweat.util.math.Triplet;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConfigSettings
{
    public static final Map<String, ValueHolder<?>> CONFIG_SETTINGS = new HashMap<>();

    public static Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;

    // Settings visible in the config screen
    public static final ValueHolder<Integer> DIFFICULTY;
    public static final ValueHolder<Double> MAX_TEMP;
    public static final ValueHolder<Double> MIN_TEMP;
    public static final ValueHolder<Double> TEMP_RATE;
    public static final ValueHolder<Boolean> FIRE_RESISTANCE_ENABLED;
    public static final ValueHolder<Boolean> ICE_RESISTANCE_ENABLED;
    public static final ValueHolder<Boolean> DAMAGE_SCALING;
    public static final ValueHolder<Boolean> REQUIRE_THERMOMETER;
    public static final ValueHolder<Integer> GRACE_LENGTH;
    public static final ValueHolder<Boolean> GRACE_ENABLED;

    // World Settings
    public static final ValueHolder<Map<Integer, Triplet<Double, Double, Temperature.Units>>> BIOME_TEMPS;
    public static final ValueHolder<Map<Integer, Triplet<Double, Double, Temperature.Units>>> BIOME_OFFSETS;
    public static final ValueHolder<Map<Integer, Pair<Double, Temperature.Units>>> DIMENSION_TEMPS;
    public static final ValueHolder<Map<Integer, Pair<Double, Temperature.Units>>> DIMENSION_OFFSETS;
    public static final ValueHolder<Double> CAVE_INSULATION;
    public static final ValueHolder<Double[]> SUMMER_TEMPS;
    public static final ValueHolder<Double[]> AUTUMN_TEMPS;
    public static final ValueHolder<Double[]> WINTER_TEMPS;
    public static final ValueHolder<Double[]> SPRING_TEMPS;

    // Block settings
    public static final ValueHolder<Integer> BLOCK_RANGE;
    public static final ValueHolder<Boolean> COLD_SOUL_FIRE;
    public static final ValueHolder<List<Block>> HEARTH_SPREAD_WHITELIST;
    public static final ValueHolder<List<Block>> HEARTH_SPREAD_BLACKLIST;
    public static final ValueHolder<Double> HEARTH_EFFECT;

    // Item settings
    public static final ValueHolder<Map<Item, Pair<Double, Double>>> INSULATION_ITEMS = ValueHolder.simple(() -> new HashMap<>());
    public static final ValueHolder<Map<Item, Pair<Double, Double>>> ADAPTIVE_INSULATION_ITEMS = ValueHolder.simple(() -> new HashMap<>());
    public static final ValueHolder<Map<Item, Pair<Double, Double>>> INSULATING_ARMORS = ValueHolder.simple(() -> new HashMap<>());
    public static final ValueHolder<Integer[]> INSULATION_SLOTS = ValueHolder.simple(() -> new Integer[]{4, 5, 6, 4});
    public static final ValueHolder<List<ResourceLocation>> INSULATION_BLACKLIST = ValueHolder.simple(() -> new ArrayList<>());

    public static final ValueHolder<Boolean> CHECK_SLEEP_CONDITIONS = ValueHolder.simple(() -> true);

    public static final ValueHolder<Map<Item, Double>> FOOD_TEMPERATURES = ValueHolder.simple(() -> new HashMap<>());

    public static final ValueHolder<Integer> WATERSKIN_STRENGTH = ValueHolder.simple(() -> 50);

    public static final ValueHolder<Map<Item, Integer>> LAMP_FUEL_ITEMS = ValueHolder.simple(() -> new HashMap<>());
    public static final ValueHolder<List<ResourceLocation>> LAMP_DIMENSIONS = ValueHolder.simple(() -> new ArrayList<>());

    public static final ValueHolder<Map<Item, Double>> BOILER_FUEL = ValueHolder.simple(() -> new HashMap<>());
    public static final ValueHolder<Map<Item, Double>> ICEBOX_FUEL = ValueHolder.simple(() -> new HashMap<>());
    public static final ValueHolder<Map<Item, Double>> HEARTH_FUEL = ValueHolder.simple(() -> new HashMap<>());
    public static final ValueHolder<Boolean> HEARTH_POTIONS_ENABLED = ValueHolder.simple(() -> true);
    public static final ValueHolder<List<ResourceLocation>> BLACKLISTED_POTIONS = ValueHolder.simple(() -> new ArrayList<>());

    // Entity Settings
    public static final ValueHolder<Triplet<Integer, Integer, Double>> LLAMA_FUR_TIMINGS = ValueHolder.simple(() -> new Triplet<>(24000, 24000, 0.5));
    public static final ValueHolder<Map<ResourceLocation, Integer>> CHAMELEON_BIOMES = ValueHolder.simple(() -> new HashMap<>());
    public static final ValueHolder<Map<ResourceLocation, Integer>> LLAMA_BIOMES = ValueHolder.simple(() -> new HashMap<>());
    
    static
    {
        DIFFICULTY = addSyncedSetting("difficulty", () -> ColdSweatConfig.difficulty,
                                      encoder -> ConfigHelper.writeNBTInt(encoder, "Difficulty"),
                                      decoder -> decoder.getInteger("Difficulty"),
                                      saver -> ColdSweatConfig.difficulty = saver);

        MAX_TEMP = addSyncedSetting("max_temp", () -> ColdSweatConfig.maxHabitable,
                                    encoder -> ConfigHelper.writeNBTDouble(encoder, "MaxTemp"),
                                    decoder -> decoder.getDouble("MaxTemp"),
                                    saver -> ColdSweatConfig.maxHabitable = saver);

        MIN_TEMP = addSyncedSetting("min_temp", () -> ColdSweatConfig.minHabitable,
                                    encoder -> ConfigHelper.writeNBTDouble(encoder, "MinTemp"),
                                    decoder -> decoder.getDouble("MinTemp"),
                                    saver -> ColdSweatConfig.minHabitable = saver);

        TEMP_RATE = addSyncedSetting("temp_rate", () -> ColdSweatConfig.rateMultiplier,
                                     encoder -> ConfigHelper.writeNBTDouble(encoder, "TempRate"),
                                     decoder -> decoder.getDouble("TempRate"),
                                     saver -> ColdSweatConfig.rateMultiplier = saver);

        FIRE_RESISTANCE_ENABLED = addSyncedSetting("fire_resistance_enabled", () -> ColdSweatConfig.fireResistanceEffect,
                                                   encoder -> ConfigHelper.writeNBTBoolean(encoder, "FireResistanceEnabled"),
                                                   decoder -> decoder.getBoolean("FireResistanceEnabled"),
                                                   saver -> ColdSweatConfig.fireResistanceEffect = saver);

        ICE_RESISTANCE_ENABLED = addSyncedSetting("ice_resistance_enabled", () -> ColdSweatConfig.iceResistanceEffect,
                                                  encoder -> ConfigHelper.writeNBTBoolean(encoder, "IceResistanceEnabled"),
                                                  decoder -> decoder.getBoolean("IceResistanceEnabled"),
                                                  saver -> ColdSweatConfig.iceResistanceEffect = saver);

        DAMAGE_SCALING = addSyncedSetting("damage_scaling", () -> ColdSweatConfig.damageScaling,
                                          encoder -> ConfigHelper.writeNBTBoolean( encoder, "DamageScaling"),
                                          decoder -> decoder.getBoolean("DamageScaling"),
                                          saver -> ColdSweatConfig.damageScaling = saver);

        REQUIRE_THERMOMETER = addSyncedSetting("require_thermometer", () -> ColdSweatConfig.requireThermometer,
                                               encoder -> ConfigHelper.writeNBTBoolean(encoder, "RequireThermometer"),
                                               decoder -> decoder.getBoolean("RequireThermometer"),
                                               saver -> ColdSweatConfig.requireThermometer = saver);

        GRACE_LENGTH = addSyncedSetting("grace_length", () -> ColdSweatConfig.gracePeriodLength,
                                        encoder -> ConfigHelper.writeNBTInt(encoder, "GraceLength"),
                                        decoder -> decoder.getInteger("GraceLength"),
                                        saver -> ColdSweatConfig.gracePeriodLength = saver);

        GRACE_ENABLED = addSyncedSetting("grace_enabled", () -> ColdSweatConfig.gracePeriodEnabled,
                                         encoder -> ConfigHelper.writeNBTBoolean(encoder, "GraceEnabled"),
                                         decoder -> decoder.getBoolean("GraceEnabled"),
                                         saver -> ColdSweatConfig.gracePeriodEnabled = saver);

        BLOCK_RANGE = addSyncedSetting("block_range", () -> WorldSettingsConfig.blockRange,
                                       encoder -> ConfigHelper.writeNBTInt(encoder, "BlockRange"),
                                       decoder -> decoder.getInteger("BlockRange"),
                                       saver -> WorldSettingsConfig.blockRange = saver);

        COLD_SOUL_FIRE = addSetting("cold_soul_fire", () -> ColdSweatConfig.coldSoulFire);

        HEARTH_EFFECT = addSetting("hearth_effect", () -> ColdSweatConfig.hearthEffect);

        HEARTH_SPREAD_WHITELIST = addSyncedSetting("hearth_spread_whitelist", () -> ConfigHelper.getBlocks(ColdSweatConfig.hearthSpreadWhitelist),
        encoder ->
        {
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagList list = new NBTTagList();
            for (Block entry : encoder)
            {   ConfigHelper.getBlockID(entry).ifPresent(id -> list.appendTag(new NBTTagString(id.toString())));
            }
            tag.setTag("HearthWhitelist", list);
            return tag;
        },
        decoder ->
        {
            List<Block> list = new ArrayList<>();
            NBTTagList tagList = decoder.getTagList("HearthWhitelist", 8);
            for (int i = 0; i < tagList.tagCount(); i++)
            {   ConfigHelper.getBlock(tagList.getStringTagAt(i)).ifPresent(list::add);
            }
            return list;
        },
        saver -> ColdSweatConfig.hearthSpreadWhitelist = ConfigHelper.serializeList(saver.stream().map(ConfigHelper::getBlockID).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList())));

        HEARTH_SPREAD_BLACKLIST = addSyncedSetting("hearth_spread_blacklist", () -> ConfigHelper.getBlocks(ColdSweatConfig.hearthSpreadBlacklist),
        encoder ->
        {
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagList list = new NBTTagList();
            for (Block entry : encoder)
            {   ConfigHelper.getBlockID(entry).ifPresent(id -> list.appendTag(new NBTTagString(id.toString())));
            }
            tag.setTag("HearthBlacklist", list);
            return tag;
        },
        decoder ->
        {
            List<Block> list = new ArrayList<>();
            NBTTagList tagList = decoder.getTagList("HearthBlacklist", 8);
            for (int i = 0; i < tagList.tagCount(); i++)
            {   ConfigHelper.getBlock(tagList.getStringTagAt(i)).ifPresent(list::add);
            }
            return list;
        },
        saver -> ColdSweatConfig.hearthSpreadBlacklist = ConfigHelper.serializeList(saver.stream().map(ConfigHelper::getBlockID).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList())));

        CAVE_INSULATION = addSyncedSetting("cave_insulation", () -> WorldSettingsConfig.caveInsulation,
        encoder -> ConfigHelper.writeNBTDouble(encoder, "CaveInsulation"),
        decoder -> decoder.getDouble("CaveInsulation"),
        saver -> WorldSettingsConfig.caveInsulation = saver);

        BIOME_TEMPS = addSyncedSetting("biome_temps", () -> ConfigHelper.getBiomesWithValues(WorldSettingsConfig.biomeTemps, true),
        encoder -> ConfigHelper.writeBiomeTemps(encoder, "BiomeTemps"),
        decoder -> ConfigHelper.readBiomeTemps(decoder, "BiomeTemps"),
        saver -> WorldSettingsConfig.biomeTemps = ConfigHelper.serializeNestedList(saver.entrySet().stream()
                                        .map(entry ->
                                             {
                                                 Temperature.Units units = entry.getValue().getThird();
                                                 double min = Temperature.convertUnits(entry.getValue().getFirst(), Temperature.Units.MC, units, false);
                                                 double max = Temperature.convertUnits(entry.getValue().getSecond(), Temperature.Units.MC, units, false);
                                                 return Arrays.asList(entry.getKey().toString(), min, max, units);
                                             })
                                        .collect(Collectors.toList())));

        BIOME_OFFSETS = addSyncedSetting("biome_offsets", () -> ConfigHelper.getBiomesWithValues(WorldSettingsConfig.biomeOffsets, false),
        encoder -> ConfigHelper.writeBiomeTemps(encoder, "BiomeOffsets"),
        decoder -> ConfigHelper.readBiomeTemps(decoder, "BiomeOffsets"),
        saver -> WorldSettingsConfig.biomeOffsets = ConfigHelper.serializeNestedList(saver.entrySet().stream()
                                        .map(entry ->
                                             {
                                                 Temperature.Units units = entry.getValue().getThird();
                                                 double min = Temperature.convertUnits(entry.getValue().getFirst(), Temperature.Units.MC, units, false);
                                                 double max = Temperature.convertUnits(entry.getValue().getSecond(), Temperature.Units.MC, units, false);
                                                 return Arrays.asList(entry.getKey().toString(), min, max, units);
                                             })
                                        .collect(Collectors.toList())));

        DIMENSION_TEMPS = addSyncedSetting("dimension_temps", () -> ConfigHelper.getDimensionsWithValues(WorldSettingsConfig.dimensionTemps),
        encoder -> ConfigHelper.writeDimensionTemps(encoder, "DimensionTemps"),
        decoder -> ConfigHelper.readDimensionTemps(decoder, "DimensionTemps"),
        saver -> WorldSettingsConfig.dimensionTemps = ConfigHelper.serializeNestedList(saver.entrySet().stream()
                                           .map(entry -> Arrays.asList(entry.getKey().toString(), entry.getValue().getFirst(), entry.getValue().getSecond().toString()))
                                           .collect(Collectors.toList())));

        DIMENSION_OFFSETS = addSyncedSetting("dimension_offsets", () -> ConfigHelper.getDimensionsWithValues(WorldSettingsConfig.dimensionOffsets),
        encoder -> ConfigHelper.writeDimensionTemps(encoder, "DimensionOffsets"),
        decoder -> ConfigHelper.readDimensionTemps(decoder, "DimensionOffsets"),
        saver -> WorldSettingsConfig.dimensionOffsets = ConfigHelper.serializeNestedList(saver.entrySet().stream()
                                           .map(entry -> Arrays.asList(entry.getKey().toString(), entry.getValue().getFirst(), entry.getValue().getSecond().toString()))
                                           .collect(Collectors.toList())));


        boolean ssLoaded = CompatManager.isSereneSeasonsLoaded();
        SUMMER_TEMPS = addSetting("summer_temps", () -> ssLoaded ? Arrays.stream(ConfigHelper.deserializeArray(WorldSettingsConfig.summerTemps)).map(Double::valueOf).toArray(Double[]::new) : new Double[]{0d, 0d, 0d});
        AUTUMN_TEMPS = addSetting("autumn_temps", () -> ssLoaded ? Arrays.stream(ConfigHelper.deserializeArray(WorldSettingsConfig.autumnTemps)).map(Double::valueOf).toArray(Double[]::new) : new Double[]{0d, 0d, 0d});
        WINTER_TEMPS = addSetting("winter_temps", () -> ssLoaded ? Arrays.stream(ConfigHelper.deserializeArray(WorldSettingsConfig.winterTemps)).map(Double::valueOf).toArray(Double[]::new) : new Double[]{0d, 0d, 0d});
        SPRING_TEMPS = addSetting("spring_temps", () -> ssLoaded ? Arrays.stream(ConfigHelper.deserializeArray(WorldSettingsConfig.springTemps)).map(Double::valueOf).toArray(Double[]::new) : new Double[]{0d, 0d, 0d});
    }

    public enum Difficulty
    {
        SUPER_EASY(CSMath.mapOf(
                "min_temp", () -> Temperature.convertUnits(40, Temperature.Units.F, Temperature.Units.MC, true),
                "max_temp", () -> Temperature.convertUnits(120, Temperature.Units.F, Temperature.Units.MC, true),
                "temp_rate", () -> 0.5,
                "require_thermometer", () -> false,
                "fire_resistance_enabled", () -> true,
                "ice_resistance_enabled", () -> true,
                "damage_scaling", () -> false
        )),

        EASY(CSMath.mapOf(
                "min_temp", () -> Temperature.convertUnits(45, Temperature.Units.F, Temperature.Units.MC, true),
                "max_temp", () -> Temperature.convertUnits(110, Temperature.Units.F, Temperature.Units.MC, true),
                "temp_rate", () -> 0.75,
                "require_thermometer", () -> false,
                "fire_resistance_enabled", () -> true,
                "ice_resistance_enabled", () -> true,
                "damage_scaling", () -> false
        )),

        NORMAL(CSMath.mapOf(
                "min_temp", () -> Temperature.convertUnits(50, Temperature.Units.F, Temperature.Units.MC, true),
                "max_temp", () -> Temperature.convertUnits(100, Temperature.Units.F, Temperature.Units.MC, true),
                "temp_rate", () -> 1.0,
                "require_thermometer", () -> true,
                "fire_resistance_enabled", () -> true,
                "ice_resistance_enabled", () -> true,
                "damage_scaling", () -> true
        )),

        HARD(CSMath.mapOf(
                "min_temp", () -> Temperature.convertUnits(60, Temperature.Units.F, Temperature.Units.MC, true),
                "max_temp", () -> Temperature.convertUnits(90, Temperature.Units.F, Temperature.Units.MC, true),
                "temp_rate", () -> 1.5,
                "require_thermometer", () -> true,
                "fire_resistance_enabled", () -> false,
                "ice_resistance_enabled", () -> false,
                "damage_scaling", () -> true
        )),

        CUSTOM(CSMath.mapOf());

        private final Map<String, Supplier<?>> settings;
        Difficulty(Map<String, Supplier<?>> settings)
        {   this.settings = settings;
        }

        public <T> T getSetting(String id)
        {   return (T) settings.get(id).get();
        }

        public <T> T getOrDefault(String id, T defaultValue)
        {   return (T) settings.getOrDefault(id, () -> defaultValue).get();
        }

        public void load()
        {   settings.forEach((id, loader) -> CONFIG_SETTINGS.get(id).set(loader.get()));
        }
    }

    public static <T> ValueHolder<T> addSyncedSetting(String id, Supplier<T> supplier, Function<T, NBTTagCompound> writer, Function<NBTTagCompound, T> reader, Consumer<T> saver)
    {   ValueHolder<T> loader = ValueHolder.synced(supplier, writer, reader, saver);
        CONFIG_SETTINGS.put(id, loader);
        return loader;
    }

    public static <T> ValueHolder<T> addSetting(String id, Supplier<T> supplier)
    {   ValueHolder<T> loader = ValueHolder.simple(supplier);
        CONFIG_SETTINGS.put(id, loader);
        return loader;
    }

    public static Map<String, NBTTagCompound> encode()
    {
        Map<String, NBTTagCompound> map = new HashMap<>();
        CONFIG_SETTINGS.forEach((key, value) ->
        {   if (value.isSynced())
            {   map.put(key, value.encode());
            }
        });
        return map;
    }

    public static void decode(String key, NBTTagCompound tag)
    {
        CONFIG_SETTINGS.computeIfPresent(key, (k, value) ->
        {   value.decode(tag);
            return value;
        });
    }

    public static void saveValues()
    {
        CONFIG_SETTINGS.values().forEach(value ->
        {   if (value.isSynced())
            {   value.save();
            }
        });
    }

    public static void load()
    {   CONFIG_SETTINGS.values().forEach(ValueHolder::load);
    }
}
