package com.momosoftworks.coldsweat.config;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.config.util.ValueHolder;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.Triplet;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Holds almost all configs for Cold Sweat in memory for easy access.
 * Handles syncing configs between the client/server.
 */
public class ConfigSettings
{
    public static final Map<String, ValueHolder<?>> CONFIG_SETTINGS = new ConcurrentHashMap<>();

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
    public static final ValueHolder<Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>>> BIOME_TEMPS;
    public static final ValueHolder<Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>>> BIOME_OFFSETS;
    public static final ValueHolder<Map<ResourceLocation, Pair<Double, Temperature.Units>>> DIMENSION_TEMPS;
    public static final ValueHolder<Map<ResourceLocation, Pair<Double, Temperature.Units>>> DIMENSION_OFFSETS;
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
    public static final ValueHolder<Map<Item, Pair<Double, Double>>> INSULATION_ITEMS;
    public static final ValueHolder<Map<Item, Pair<Double, Double>>> ADAPTIVE_INSULATION_ITEMS;
    public static final ValueHolder<Map<Item, Pair<Double, Double>>> INSULATING_ARMORS;
    public static final ValueHolder<Integer[]> INSULATION_SLOTS;
    public static final ValueHolder<List<ResourceLocation>> INSULATION_BLACKLIST;

    public static final ValueHolder<Boolean> CHECK_SLEEP_CONDITIONS;
    
    public static final ValueHolder<Map<Item, Double>> FOOD_TEMPERATURES;

    public static final ValueHolder<Integer> WATERSKIN_STRENGTH;

    public static final ValueHolder<Map<Item, Integer>> LAMP_FUEL_ITEMS;
    public static final ValueHolder<List<ResourceLocation>> LAMP_DIMENSIONS;

    public static final ValueHolder<Map<Item, Double>> BOILER_FUEL;
    public static final ValueHolder<Map<Item, Double>> ICEBOX_FUEL;
    public static final ValueHolder<Map<Item, Double>> HEARTH_FUEL;
    public static final ValueHolder<Boolean> HEARTH_POTIONS_ENABLED;
    public static final ValueHolder<List<ResourceLocation>> BLACKLISTED_POTIONS;

    // Entity Settings
    public static final ValueHolder<Triplet<Integer, Integer, Double>> LLAMA_FUR_TIMINGS;
    public static final ValueHolder<Map<ResourceLocation, Integer>> CHAMELEON_BIOMES;
    public static final ValueHolder<Map<ResourceLocation, Integer>> LLAMA_BIOMES;


    // Makes the settings instantiation collapsible & easier to read
    static
    {
        DIFFICULTY = addSyncedSetting("difficulty", () -> ColdSweatConfig.getInstance().getDifficulty(),
        encoder -> ConfigHelper.writeNBTInt(encoder, "Difficulty"),
        decoder -> decoder.getInt("Difficulty"),
        saver -> ColdSweatConfig.getInstance().setDifficulty(saver));

        MAX_TEMP = addSyncedSetting("max_temp", () -> ColdSweatConfig.getInstance().getMaxTempHabitable(),
        encoder -> ConfigHelper.writeNBTDouble(encoder, "MaxTemp"),
        decoder -> decoder.getDouble("MaxTemp"),
        saver -> ColdSweatConfig.getInstance().setMaxHabitable(saver));

        MIN_TEMP = addSyncedSetting("min_temp", () -> ColdSweatConfig.getInstance().getMinTempHabitable(),
        encoder -> ConfigHelper.writeNBTDouble(encoder, "MinTemp"),
        decoder -> decoder.getDouble("MinTemp"),
        saver -> ColdSweatConfig.getInstance().setMinHabitable(saver));

        TEMP_RATE = addSyncedSetting("temp_rate", () -> ColdSweatConfig.getInstance().getRateMultiplier(),
        encoder -> ConfigHelper.writeNBTDouble(encoder, "TempRate"),
        decoder -> decoder.getDouble("TempRate"),
        saver -> ColdSweatConfig.getInstance().setRateMultiplier(saver));

        FIRE_RESISTANCE_ENABLED = addSyncedSetting("fire_resistance_enabled", () -> ColdSweatConfig.getInstance().isFireResistanceEnabled(),
        encoder -> ConfigHelper.writeNBTBoolean(encoder, "FireResistanceEnabled"),
        decoder -> decoder.getBoolean("FireResistanceEnabled"),
        saver -> ColdSweatConfig.getInstance().setFireResistanceEnabled(saver));

        ICE_RESISTANCE_ENABLED = addSyncedSetting("ice_resistance_enabled", () -> ColdSweatConfig.getInstance().isIceResistanceEnabled(),
        encoder -> ConfigHelper.writeNBTBoolean(encoder, "IceResistanceEnabled"),
        decoder -> decoder.getBoolean("IceResistanceEnabled"),
        saver -> ColdSweatConfig.getInstance().setIceResistanceEnabled(saver));

        DAMAGE_SCALING = addSyncedSetting("damage_scaling", () -> ColdSweatConfig.getInstance().doDamageScaling(),
        encoder -> ConfigHelper.writeNBTBoolean( encoder, "DamageScaling"),
        decoder -> decoder.getBoolean("DamageScaling"),
        saver -> ColdSweatConfig.getInstance().setDamageScaling(saver));

        REQUIRE_THERMOMETER = addSyncedSetting("require_thermometer", () -> ColdSweatConfig.getInstance().thermometerRequired(),
        encoder -> ConfigHelper.writeNBTBoolean(encoder, "RequireThermometer"),
        decoder -> decoder.getBoolean("RequireThermometer"),
        saver -> ColdSweatConfig.getInstance().setRequireThermometer(saver));

        GRACE_LENGTH = addSyncedSetting("grace_length", () -> ColdSweatConfig.getInstance().getGracePeriodLength(),
        encoder -> ConfigHelper.writeNBTInt(encoder, "GraceLength"),
        decoder -> decoder.getInt("GraceLength"),
        saver -> ColdSweatConfig.getInstance().setGracePeriodLength(saver));

        GRACE_ENABLED = addSyncedSetting("grace_enabled", () -> ColdSweatConfig.getInstance().isGracePeriodEnabled(),
        encoder -> ConfigHelper.writeNBTBoolean(encoder, "GraceEnabled"),
        decoder -> decoder.getBoolean("GraceEnabled"),
        saver -> ColdSweatConfig.getInstance().setGracePeriodEnabled(saver));

        BIOME_TEMPS = addSyncedSetting("biome_temps", () -> ConfigHelper.getBiomesWithValues(WorldSettingsConfig.getInstance().getBiomeTemperatures(), true),
        encoder -> ConfigHelper.writeBiomeTemps(encoder, "BiomeTemps"),
        decoder -> ConfigHelper.readBiomeTemps(decoder, "BiomeTemps"),
        saver -> WorldSettingsConfig.getInstance().setBiomeTemperatures(saver.entrySet().stream()
                                                            .map(entry ->
                                                            {
                                                                Temperature.Units units = entry.getValue().getThird();
                                                                double min = Temperature.convertUnits(entry.getValue().getFirst(), Temperature.Units.MC, units, true);
                                                                double max = Temperature.convertUnits(entry.getValue().getSecond(), Temperature.Units.MC, units, true);
                                                                return Arrays.asList(entry.getKey().toString(), min, max, units);
                                                            })
                                                            .collect(Collectors.toList())));

        BIOME_OFFSETS = addSyncedSetting("biome_offsets", () -> ConfigHelper.getBiomesWithValues(WorldSettingsConfig.getInstance().getBiomeTempOffsets(), false),
        encoder -> ConfigHelper.writeBiomeTemps(encoder, "BiomeOffsets"),
        decoder -> ConfigHelper.readBiomeTemps(decoder, "BiomeOffsets"),
        saver -> WorldSettingsConfig.getInstance().setBiomeTempOffsets(saver.entrySet().stream()
                                                            .map(entry ->
                                                            {
                                                                Temperature.Units units = entry.getValue().getThird();
                                                                double min = Temperature.convertUnits(entry.getValue().getFirst(), Temperature.Units.MC, units, false);
                                                                double max = Temperature.convertUnits(entry.getValue().getSecond(), Temperature.Units.MC, units, false);
                                                                return Arrays.asList(entry.getKey().toString(), min, max, units);
                                                            })
                                                            .collect(Collectors.toList())));

        DIMENSION_TEMPS = addSyncedSetting("dimension_temps", () -> ConfigHelper.getDimensionsWithValues(WorldSettingsConfig.getInstance().getDimensionTemperatures()),
        encoder -> ConfigHelper.writeDimensionTemps(encoder, "DimensionTemps"),
        decoder -> ConfigHelper.readDimensionTemps(decoder, "DimensionTemps"),
        saver -> WorldSettingsConfig.getInstance().setDimensionTemperatures(saver.entrySet().stream()
                                                     .map(entry -> Arrays.asList(entry.getKey().toString(), entry.getValue().getFirst(), entry.getValue().getSecond().toString()))
                                                     .collect(Collectors.toList())));

        DIMENSION_OFFSETS = addSyncedSetting("dimension_offsets", () -> ConfigHelper.getDimensionsWithValues(WorldSettingsConfig.getInstance().getDimensionTempOffsets()),
        encoder -> ConfigHelper.writeDimensionTemps(encoder, "DimensionOffsets"),
        decoder -> ConfigHelper.readDimensionTemps(decoder, "DimensionOffsets"),
        saver -> WorldSettingsConfig.getInstance().setDimensionTempOffsets(saver.entrySet().stream()
                                                     .map(entry -> Arrays.asList(entry.getKey().toString(), entry.getValue().getFirst(), entry.getValue().getSecond().toString()))
                                                     .collect(Collectors.toList())));

        CAVE_INSULATION = addSyncedSetting("cave_insulation", () -> WorldSettingsConfig.getInstance().getCaveInsulation(),
                                           encoder -> ConfigHelper.writeNBTDouble(encoder, "CaveInsulation"),
                                           decoder -> decoder.getDouble("CaveInsulation"),
                                           saver -> WorldSettingsConfig.getInstance().setCaveInsulation(saver));

        BOILER_FUEL = addSetting("boiler_fuel_items", () -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().getBoilerFuelItems()));
        ICEBOX_FUEL = addSetting("icebox_fuel_items", () -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().getIceboxFuelItems()));
        HEARTH_FUEL = addSetting("hearth_fuel_items", () -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().getHearthFuelItems()));

        HEARTH_POTIONS_ENABLED = addSetting("hearth_potions_enabled", () -> ItemSettingsConfig.getInstance().arePotionsEnabled());
        BLACKLISTED_POTIONS = addSetting("hearth_potion_blacklist", () -> ItemSettingsConfig.getInstance().getPotionBlacklist().stream().map(ResourceLocation::new).collect(Collectors.toList()));

        INSULATION_ITEMS = addSyncedSetting("insulation_items", () ->
        {
            Map<Item, Pair<Double, Double>> map = new HashMap<>();
            for (List<?> entry : ItemSettingsConfig.getInstance().getInsulationItems())
            {
                String itemID = (String) entry.get(0);
                for (Item item : ConfigHelper.getItems(itemID))
                {   map.put(item, Pair.of(((Number) entry.get(1)).doubleValue(), ((Number) entry.get(2)).doubleValue()));
                }
            }
            return map;
        },
        encoder -> ConfigHelper.writeNBTItemMap(encoder, "InsulationItems"),
        decoder -> ConfigHelper.readNBTItemMap(decoder, "InsulationItems"),
        saver ->
        {
            List<List<?>> list = new ArrayList<>();
            for (Map.Entry<Item, Pair<Double, Double>> entry : saver.entrySet())
            {   ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(entry.getKey());
                if (itemID != null)
                {   list.add(Arrays.asList(itemID.toString(), entry.getValue().getFirst(), entry.getValue().getSecond()));
                }
            }
            ItemSettingsConfig.getInstance().setInsulationItems(list);
        });

        ADAPTIVE_INSULATION_ITEMS = addSyncedSetting("adaptive_insulation_items", () ->
        {
            Map<Item, Pair<Double, Double>> map = new HashMap<>();
            for (List<?> entry : ItemSettingsConfig.getInstance().getAdaptiveInsulationItems())
            {
                String itemID = (String) entry.get(0);
                for (Item item : ConfigHelper.getItems(itemID))
                {   map.put(item, Pair.of(((Number) entry.get(1)).doubleValue(), ((Number) entry.get(2)).doubleValue()));
                }
            }
            return map;
        },
        encoder -> ConfigHelper.writeNBTItemMap(encoder, "AdaptiveInsulationItems"),
        decoder -> ConfigHelper.readNBTItemMap(decoder, "AdaptiveInsulationItems"),
        saver ->
        {
            List<List<?>> list = new ArrayList<>();
            for (Map.Entry<Item, Pair<Double, Double>> entry : saver.entrySet())
            {
                ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(entry.getKey());
                if (itemID != null)
                {   list.add(Arrays.asList(itemID.toString(), entry.getValue().getFirst(), entry.getValue().getSecond()));
                }
            }
            ItemSettingsConfig.getInstance().setAdaptiveInsulationItems(list);
        });

        INSULATING_ARMORS = addSyncedSetting("insulating_armors", () ->
        {
            Map<Item, Pair<Double, Double>> map = new HashMap<>();
            for (List<?> entry : ItemSettingsConfig.getInstance().getInsulatingArmorItems())
            {
                String itemID = (String) entry.get(0);
                for (Item item : ConfigHelper.getItems(itemID))
                {   map.put(item, Pair.of(((Number) entry.get(1)).doubleValue(), ((Number) entry.get(2)).doubleValue()));
                }
            }
            return map;
        },
        encoder -> ConfigHelper.writeNBTItemMap(encoder, "InsulatingArmors"),
        decoder -> ConfigHelper.readNBTItemMap(decoder, "InsulatingArmors"),
        saver ->
        {   List<List<?>> list = new ArrayList<>();
            for (Map.Entry<Item, Pair<Double, Double>> entry : saver.entrySet())
            {   ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(entry.getKey());
                if (itemID != null)
                {   list.add(Arrays.asList(itemID.toString(), entry.getValue().getFirst(), entry.getValue().getSecond()));
                }
            }
            ItemSettingsConfig.getInstance().setInsulatingArmorItems(list);
        });

        INSULATION_SLOTS = addSyncedSetting("insulation_slots", () ->
        {
            List<? extends Number> list = ItemSettingsConfig.getInstance().getArmorInsulationSlots();
            return new Integer[] { list.get(0).intValue(), list.get(1).intValue(), list.get(2).intValue(), list.get(3).intValue() };
        },
        encoder ->
        {   CompoundNBT tag = new CompoundNBT();
            tag.putInt("Head", encoder[0]);
            tag.putInt("Chest", encoder[1]);
            tag.putInt("Legs", encoder[2]);
            tag.putInt("Feet", encoder[3]);
            return tag;
        },
        decoder ->
        {   return new Integer[] { decoder.getInt("Head"), decoder.getInt("Chest"), decoder.getInt("Legs"), decoder.getInt("Feet") };
        },
        saver ->
        {   ItemSettingsConfig.getInstance().setArmorInsulationSlots(Arrays.asList(saver[0], saver[1], saver[2], saver[3]));
        });

        INSULATION_BLACKLIST = addSetting("insulation_blacklist", () -> ItemSettingsConfig.getInstance().getInsulationBlacklist().stream().map(ResourceLocation::new).collect(Collectors.toList()));

        CHECK_SLEEP_CONDITIONS = addSetting("check_sleep_conditions", () -> ColdSweatConfig.getInstance().isSleepChecked());

        FOOD_TEMPERATURES = addSyncedSetting("food_temperatures", () ->
        {
            Map<Item, Double> map = new HashMap<>();
            for (List<?> entry : ItemSettingsConfig.getInstance().getFoodTemperatures())
            {
                String itemID = (String) entry.get(0);
                for (Item item : ConfigHelper.getItems(itemID))
                {   map.put(item, ((Number) entry.get(1)).doubleValue());
                }
            }
            return map;
        },
        encoder ->
        {
            CompoundNBT tag = new CompoundNBT();
            CompoundNBT mapTag = new CompoundNBT();
            for (Map.Entry<Item, Double> entry : encoder.entrySet())
            {
                CompoundNBT itemTag = new CompoundNBT();
                itemTag.putDouble("Value", entry.getValue());

                ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(entry.getKey());
                if (itemID != null)
                {   mapTag.put(itemID.toString(), itemTag);
                }
            }
            tag.put("FoodTemperatures", mapTag);
            return tag;
        },
        decoder ->
        {
            Map<Item, Double> map = new HashMap<>();
            CompoundNBT mapTag = decoder.getCompound("FoodTemperatures");
            for (String key : mapTag.getAllKeys())
            {
                CompoundNBT itemTag = mapTag.getCompound(key);
                map.put(ForgeRegistries.ITEMS.getValue(new ResourceLocation(key)), itemTag.getDouble("Value"));
            }
            return map;
        },
        saver ->
        {
            List<List<?>> list = new ArrayList<>();
            for (Map.Entry<Item, Double> entry : saver.entrySet())
            {   ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(entry.getKey());
                if (itemID != null)
                {   list.add(Arrays.asList(itemID.toString(), entry.getValue()));
                }
            }
            ItemSettingsConfig.getInstance().setFoodTemperatures(list);
        });

        WATERSKIN_STRENGTH = addSetting("waterskin_strength", () -> ItemSettingsConfig.getInstance().getWaterskinStrength());

        LAMP_FUEL_ITEMS = addSyncedSetting("lamp_fuel_items", () ->
        {
            Map<Item, Integer> list = new HashMap<>();
            for (List<?> item : ItemSettingsConfig.getInstance().getSoulLampFuelItems())
            {
                ConfigHelper.getItems((String) item.get(0)).forEach(i -> list.put(i, (Integer) item.get(1)));
            }
            return list;
        },
        encoder ->
        {
            CompoundNBT tag = new CompoundNBT();
            for (Map.Entry<Item, Integer> entry : encoder.entrySet())
            {
                ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(entry.getKey());
                if (itemID != null)
                {   tag.putInt(itemID.toString(), entry.getValue());
                }
            }
            return tag;
        },
        decoder ->
        {
            Map<Item, Integer> map = new HashMap<>();
            for (String key : decoder.getAllKeys())
            {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key));
                if (item != null)
                {   map.put(item, decoder.getInt(key));
                }
            }
            return map;
        },
        saver ->
        {
            List<List<?>> list = new ArrayList<>();
            for (Map.Entry<Item, Integer> entry : saver.entrySet())
            {   ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(entry.getKey());
                if (itemID != null)
                {   list.add(Arrays.asList(itemID.toString(), entry.getValue()));
                }
            }
            ItemSettingsConfig.getInstance().setSoulLampFuelItems(list);
        });

        LAMP_DIMENSIONS = addSetting("valid_lamp_dimensions", () -> ItemSettingsConfig.getInstance().getValidSoulLampDimensions().stream().map(ResourceLocation::new).collect(Collectors.toList()));

        LLAMA_FUR_TIMINGS = addSyncedSetting("fur_timings", () ->
        {
            List<?> entry = EntitySettingsConfig.getInstance().getLlamaFurStats();
            return new Triplet<>(((Number) entry.get(0)).intValue(), ((Number) entry.get(1)).intValue(), ((Number) entry.get(2)).doubleValue());
        },
        encoder ->
        {
            CompoundNBT tag = new CompoundNBT();
            tag.put("Interval", IntNBT.valueOf(encoder.getFirst()));
            tag.put("Cooldown", IntNBT.valueOf(encoder.getSecond()));
            tag.put("Chance", DoubleNBT.valueOf(encoder.getThird()));
            return tag;
        },
        decoder ->
        {
            int interval = decoder.getInt("Interval");
            int cooldown = decoder.getInt("Cooldown");
            double chance = decoder.getDouble("Chance");
            return new Triplet<>(interval, cooldown, chance);
        },
        saver ->
        {
            List<Number> list = new ArrayList<>();
            list.add(saver.getFirst());
            list.add(saver.getSecond());
            list.add(saver.getThird());
            EntitySettingsConfig.getInstance().setLlamaFurStats(list);
        });

        CHAMELEON_BIOMES = addSetting("chameleon_spawn_biomes", () ->
        {
            Map<ResourceLocation, Integer> map = new HashMap<>();
            for (List<?> entry : EntitySettingsConfig.getInstance().getChameleonSpawnBiomes())
            {
                map.put(new ResourceLocation((String) entry.get(0)), ((Number) entry.get(1)).intValue());
            }
            return map;
        });

        LLAMA_BIOMES = addSetting("llama_spawn_biomes", () ->
        {
            Map<ResourceLocation, Integer> map = new HashMap<>();
            for (List<?> entry : EntitySettingsConfig.getInstance().getLlamaSpawnBiomes())
            {
                map.put(new ResourceLocation((String) entry.get(0)), ((Number) entry.get(1)).intValue());
            }
            return map;
        });

        BLOCK_RANGE = addSyncedSetting("block_range", () -> WorldSettingsConfig.getInstance().getBlockRange(),
        encoder -> ConfigHelper.writeNBTInt(encoder, "BlockRange"),
        decoder -> decoder.getInt("BlockRange"),
        saver -> WorldSettingsConfig.getInstance().setBlockRange(saver));

        COLD_SOUL_FIRE = addSetting("cold_soul_fire", () -> ColdSweatConfig.getInstance().isSoulFireCold());

        HEARTH_SPREAD_WHITELIST = addSyncedSetting("hearth_spread_whitelist", () -> ConfigHelper.getBlocks(ColdSweatConfig.getInstance().getHearthSpreadWhitelist().toArray(new String[0])),
        encoder ->
        {
            CompoundNBT tag = new CompoundNBT();
            ListNBT list = new ListNBT();
            for (Block entry : encoder)
            {   list.add(StringNBT.valueOf(ForgeRegistries.BLOCKS.getKey(entry).toString()));
            }
            tag.put("HearthWhitelist", list);
            return tag;
        },
        decoder ->
        {
            List<Block> list = new ArrayList<>();
            for (INBT entry : decoder.getList("HearthWhitelist", 8))
            {   list.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry.getAsString())));
            }
            return list;
        },
        saver -> ColdSweatConfig.getInstance().setHearthSpreadWhitelist(saver.stream().map(ForgeRegistries.BLOCKS::getKey).collect(Collectors.toList())));

        HEARTH_SPREAD_BLACKLIST = addSyncedSetting("hearth_spread_blacklist", () -> ConfigHelper.getBlocks(ColdSweatConfig.getInstance().getHearthSpreadBlacklist().toArray(new String[0])),
        encoder ->
        {
            CompoundNBT tag = new CompoundNBT();
            ListNBT list = new ListNBT();
            for (Block entry : encoder)
            {   list.add(StringNBT.valueOf(ForgeRegistries.BLOCKS.getKey(entry).toString()));
            }
            tag.put("HearthBlacklist", list);
            return tag;
        },
        decoder ->
        {
            List<Block> list = new ArrayList<>();
            for (INBT entry : decoder.getList("HearthBlacklist", 8))
            {   list.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry.getAsString())));
            }
            return list;
        },
        saver -> ColdSweatConfig.getInstance().setHearthSpreadBlacklist(saver.stream().map(ForgeRegistries.BLOCKS::getKey).collect(Collectors.toList())));

        HEARTH_EFFECT = addSetting("hearth_effect", () -> ColdSweatConfig.getInstance().getHearthEffect());

        HEARTH_EFFECT = addSetting("hearth_effect", () -> ColdSweatConfig.getInstance().getHearthEffect());

        boolean ssLoaded = CompatManager.isSereneSeasonsLoaded();
        SUMMER_TEMPS = addSetting("summer_temps", ssLoaded ? () -> WorldSettingsConfig.getInstance().getSummerTemps() : () -> new Double[3]);
        AUTUMN_TEMPS = addSetting("autumn_temps", ssLoaded ? () -> WorldSettingsConfig.getInstance().getAutumnTemps() : () -> new Double[3]);
        WINTER_TEMPS = addSetting("winter_temps", ssLoaded ? () -> WorldSettingsConfig.getInstance().getWinterTemps() : () -> new Double[3]);
        SPRING_TEMPS = addSetting("spring_temps", ssLoaded ? () -> WorldSettingsConfig.getInstance().getSpringTemps() : () -> new Double[3]);
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

    public static <T> ValueHolder<T> addSyncedSetting(String id, Supplier<T> supplier, Function<T, CompoundNBT> writer, Function<CompoundNBT, T> reader, Consumer<T> saver)
    {   ValueHolder<T> loader = ValueHolder.synced(supplier, writer, reader, saver);
        CONFIG_SETTINGS.put(id, loader);
        return loader;
    }

    public static <T> ValueHolder<T> addSetting(String id, Supplier<T> supplier)
    {   ValueHolder<T> loader = ValueHolder.simple(supplier);
        CONFIG_SETTINGS.put(id, loader);
        return loader;
    }

    public static Map<String, CompoundNBT> encode()
    {
        Map<String, CompoundNBT> map = new HashMap<>();
        CONFIG_SETTINGS.forEach((key, value) ->
        {   if (value.isSynced())
            {   map.put(key, value.encode());
            }
        });
        return map;
    }

    public static void decode(String key, CompoundNBT tag)
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
