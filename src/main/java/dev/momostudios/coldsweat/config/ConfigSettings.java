package dev.momostudios.coldsweat.config;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.util.compat.CompatManager;
import dev.momostudios.coldsweat.config.util.ConfigHelper;
import dev.momostudios.coldsweat.config.util.ValueSupplier;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import oshi.util.tuples.Triplet;

import java.util.*;
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
    public static final Map<String, ValueSupplier<?>> CONFIG_SETTINGS = new HashMap<>();

    public static Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;

    // Settings visible in the config screen
    public static final ValueSupplier<Integer> DIFFICULTY;
    public static final ValueSupplier<Double> MAX_TEMP;
    public static final ValueSupplier<Double> MIN_TEMP;
    public static final ValueSupplier<Double> TEMP_RATE;
    public static final ValueSupplier<Boolean> FIRE_RESISTANCE_ENABLED;
    public static final ValueSupplier<Boolean> ICE_RESISTANCE_ENABLED;
    public static final ValueSupplier<Boolean> DAMAGE_SCALING;
    public static final ValueSupplier<Boolean> REQUIRE_THERMOMETER;
    public static final ValueSupplier<Integer> GRACE_LENGTH;
    public static final ValueSupplier<Boolean> GRACE_ENABLED;

    // World Settings
    public static final ValueSupplier<Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>>> BIOME_TEMPS;
    public static final ValueSupplier<Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>>> BIOME_OFFSETS;
    public static final ValueSupplier<Map<ResourceLocation, Double>> DIMENSION_TEMPS;
    public static final ValueSupplier<Map<ResourceLocation, Double>> DIMENSION_OFFSETS;
    public static final ValueSupplier<Double[]> SUMMER_TEMPS;
    public static final ValueSupplier<Double[]> AUTUMN_TEMPS;
    public static final ValueSupplier<Double[]> WINTER_TEMPS;
    public static final ValueSupplier<Double[]> SPRING_TEMPS;

    // Block settings
    public static final ValueSupplier<Boolean> COLD_SOUL_FIRE;
    public static final ValueSupplier<List<Block>> HEARTH_SPREAD_WHITELIST;
    public static final ValueSupplier<List<Block>> HEARTH_SPREAD_BLACKLIST;

    // Item settings
    public static final ValueSupplier<Map<Item, Pair<Double, Double>>> INSULATION_ITEMS;
    public static final ValueSupplier<Map<Item, Pair<Double, Double>>> ADAPTIVE_INSULATION_ITEMS;
    public static final ValueSupplier<Map<Item, Pair<Double, Double>>> INSULATING_ARMORS;
    public static final ValueSupplier<Integer[]> INSULATION_SLOTS;
    public static final ValueSupplier<List<ResourceLocation>> INSULATION_BLACKLIST;

    public static final ValueSupplier<Boolean> CHECK_SLEEP_CONDITIONS;

    public static final ValueSupplier<Map<Item, Double>> FOOD_TEMPERATURES;

    public static final ValueSupplier<Integer> WATERSKIN_STRENGTH;

    public static final ValueSupplier<Map<Item, Integer>> LAMP_FUEL_ITEMS;

    public static final ValueSupplier<List<ResourceLocation>> LAMP_DIMENSIONS;

    public static final ValueSupplier<Map<Item, Double>> BOILER_FUEL;
    public static final ValueSupplier<Map<Item, Double>> ICEBOX_FUEL;
    public static final ValueSupplier<Map<Item, Double>> HEARTH_FUEL;
    public static final ValueSupplier<Boolean> HEARTH_POTIONS_ENABLED;
    public static final ValueSupplier<List<ResourceLocation>> BLACKLISTED_POTIONS;

    // Entity Settings
    public static final ValueSupplier<Triplet<Integer, Integer, Double>> GOAT_FUR_TIMINGS;
    public static final ValueSupplier<Map<ResourceLocation, Integer>> CHAMELEON_BIOMES;
    public static final ValueSupplier<Map<ResourceLocation, Integer>> GOAT_BIOMES;


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
        encoder -> ConfigHelper.writeNBTTripletMap(encoder, "BiomeTemps"),
        decoder -> ConfigHelper.readNBTTripletMap(decoder, "BiomeTemps"),
        saver -> WorldSettingsConfig.getInstance().setBiomeTemperatures(saver.entrySet().stream().map(entry -> Arrays.asList(entry.getKey(), entry.getValue().getA(), entry.getValue().getB(), entry.getValue().getC())).collect(Collectors.toList())));

        BIOME_OFFSETS = addSyncedSetting("biome_offsets", () -> ConfigHelper.getBiomesWithValues(WorldSettingsConfig.getInstance().getBiomeTempOffsets(), false),
        encoder -> ConfigHelper.writeNBTTripletMap(encoder, "BiomeOffsets"),
        decoder -> ConfigHelper.readNBTTripletMap(decoder, "BiomeOffsets"),
        saver -> WorldSettingsConfig.getInstance().setBiomeTempOffsets(saver.entrySet().stream().map(entry -> Arrays.asList(entry.getKey(), entry.getValue().getA(), entry.getValue().getB(), entry.getValue().getC())).collect(Collectors.toList())));

        DIMENSION_TEMPS = addSyncedSetting("dimension_temps", () -> WorldSettingsConfig.getInstance().getDimensionTemperatures().stream()
                                                   .map(entry -> Map.entry(new ResourceLocation((String) entry.get(0)), ((Number) entry.get(1)).doubleValue()))
                                                   .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
        encoder -> ConfigHelper.writeNBTDoubleMap(encoder, "DimensionTemps"),
        decoder -> ConfigHelper.readNBTDoubleMap(decoder, "DimensionTemps"),
        saver -> WorldSettingsConfig.getInstance().setDimensionTemperatures(saver.entrySet().stream().map(entry -> Arrays.asList(entry.getKey().toString(), entry.getValue())).collect(Collectors.toList())));

        DIMENSION_OFFSETS = addSyncedSetting("dimension_offsets", () -> WorldSettingsConfig.getInstance().getDimensionTempOffsets().stream()
                                                     .map(entry -> Map.entry(new ResourceLocation((String) entry.get(0)), ((Number) entry.get(1)).doubleValue()))
                                                     .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
        encoder -> ConfigHelper.writeNBTDoubleMap(encoder, "DimensionOffsets"),
        decoder -> ConfigHelper.readNBTDoubleMap(decoder, "DimensionOffsets"),
        saver -> WorldSettingsConfig.getInstance().setDimensionTempOffsets(saver.entrySet().stream().map(entry -> Arrays.asList(entry.getKey().toString(), entry.getValue())).collect(Collectors.toList())));

        BOILER_FUEL = addSetting("boiler_fuel_items", () -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().getBoilerFuelItems()));
        ICEBOX_FUEL = addSetting("icebox_fuel_items", () -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().getIceboxFuelItems()));
        HEARTH_FUEL = addSetting("hearth_fuel_items", () -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().getHearthFuelItems()));

        HEARTH_POTIONS_ENABLED = addSetting("hearth_potions_enabled", () -> ItemSettingsConfig.getInstance().arePotionsEnabled());
        BLACKLISTED_POTIONS = addSetting("hearth_potion_blacklist", () -> ItemSettingsConfig.getInstance().getPotionBlacklist().stream().map(ResourceLocation::new).toList());

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
        {   CompoundTag tag = new CompoundTag();
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

        INSULATION_BLACKLIST = addSetting("insulation_blacklist", () -> ItemSettingsConfig.getInstance().getInsulationBlacklist().stream().map(ResourceLocation::new).toList());

        CHECK_SLEEP_CONDITIONS = addSetting("check_sleep_conditions", () -> ColdSweatConfig.getInstance().isSleepChecked());

        FOOD_TEMPERATURES = addSetting("food_temperatures", () -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().getFoodTemperatures()));

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
            CompoundTag tag = new CompoundTag();
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

        LAMP_DIMENSIONS = addSetting("valid_lamp_dimensions", () -> ItemSettingsConfig.getInstance().getValidSoulLampDimensions().stream().map(ResourceLocation::new).toList());

        GOAT_FUR_TIMINGS = addSyncedSetting("goat_fur_timings", () ->
        {
            List<?> entry = EntitySettingsConfig.getInstance().getGoatFurStats();
            return new Triplet<>(((Number) entry.get(0)).intValue(), ((Number) entry.get(1)).intValue(), ((Number) entry.get(2)).doubleValue());
        },
        triplet ->
        {
            CompoundTag tag = new CompoundTag();
            tag.put("Interval", IntTag.valueOf(triplet.getA()));
            tag.put("Cooldown", IntTag.valueOf(triplet.getB()));
            tag.put("Chance", DoubleTag.valueOf(triplet.getC()));
            return tag;
        },
        tag ->
        {
            int interval = tag.getInt("Interval");
            int cooldown = tag.getInt("Cooldown");
            double chance = tag.getDouble("Chance");
            return new Triplet<>(interval, cooldown, chance);
        },
        triplet ->
        {
            List<Number> list = new ArrayList<>();
            list.add(triplet.getA());
            list.add(triplet.getB());
            list.add(triplet.getC());
            EntitySettingsConfig.getInstance().setGoatFurStats(list);
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

        GOAT_BIOMES = addSetting("goat_spawn_biomes", () ->
        {
            Map<ResourceLocation, Integer> map = new HashMap<>();
            for (List<?> entry : EntitySettingsConfig.getInstance().getGoatSpawnBiomes())
            {
                map.put(new ResourceLocation((String) entry.get(0)), ((Number) entry.get(1)).intValue());
            }
            return map;
        });

        COLD_SOUL_FIRE = addSetting("cold_soul_fire", () -> ColdSweatConfig.getInstance().isSoulFireCold());

        HEARTH_SPREAD_WHITELIST = addSyncedSetting("hearth_spread_whitelist", () -> ConfigHelper.getBlocks(ColdSweatConfig.getInstance().getHearthSpreadWhitelist().toArray(new String[0])),
        encoder ->
        {
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            for (Block entry : encoder)
            {   list.add(StringTag.valueOf(ForgeRegistries.BLOCKS.getKey(entry).toString()));
            }
            tag.put("HearthWhitelist", list);
            return tag;
        },
        decoder ->
        {
            List<Block> list = new ArrayList<>();
            for (Tag entry : decoder.getList("HearthWhitelist", 8))
            {   list.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry.getAsString())));
            }
            return list;
        },
        saver -> ColdSweatConfig.getInstance().setHearthSpreadWhitelist(saver.stream().map(ForgeRegistries.BLOCKS::getKey).toList()));

        HEARTH_SPREAD_BLACKLIST = addSyncedSetting("hearth_spread_blacklist", () -> ConfigHelper.getBlocks(ColdSweatConfig.getInstance().getHearthSpreadBlacklist().toArray(new String[0])),
        encoder ->
        {
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            for (Block entry : encoder)
            {   list.add(StringTag.valueOf(ForgeRegistries.BLOCKS.getKey(entry).toString()));
            }
            tag.put("HearthBlacklist", list);
            return tag;
        },
        decoder ->
        {
            List<Block> list = new ArrayList<>();
            for (Tag entry : decoder.getList("HearthBlacklist", 8))
            {   list.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry.getAsString())));
            }
            return list;
        },
        saver -> ColdSweatConfig.getInstance().setHearthSpreadBlacklist(saver.stream().map(ForgeRegistries.BLOCKS::getKey).toList()));

        boolean ssLoaded = CompatManager.isSereneSeasonsLoaded();
        SUMMER_TEMPS = addSetting("summer_temps", ssLoaded ? () -> WorldSettingsConfig.getInstance().getSummerTemps() : () -> new Double[3]);
        AUTUMN_TEMPS = addSetting("autumn_temps", ssLoaded ? () -> WorldSettingsConfig.getInstance().getAutumnTemps() : () -> new Double[3]);
        WINTER_TEMPS = addSetting("winter_temps", ssLoaded ? () -> WorldSettingsConfig.getInstance().getWinterTemps() : () -> new Double[3]);
        SPRING_TEMPS = addSetting("spring_temps", ssLoaded ? () -> WorldSettingsConfig.getInstance().getSpringTemps() : () -> new Double[3]);
    }

    public enum Difficulty
    {
        SUPER_EASY(Map.of(
            "min_temp", () -> CSMath.convertTemp(40, Temperature.Units.F, Temperature.Units.MC, true),
            "max_temp", () -> CSMath.convertTemp(120, Temperature.Units.F, Temperature.Units.MC, true),
            "temp_rate", () -> 0.5,
            "require_thermometer", () -> false,
            "fire_resistance_enabled", () -> true,
            "ice_resistance_enabled", () -> true,
            "damage_scaling", () -> false
        )),

        EASY(Map.of(
            "min_temp", () -> CSMath.convertTemp(45, Temperature.Units.F, Temperature.Units.MC, true),
            "max_temp", () -> CSMath.convertTemp(110, Temperature.Units.F, Temperature.Units.MC, true),
            "temp_rate", () -> 0.75,
            "require_thermometer", () -> false,
            "fire_resistance_enabled", () -> true,
            "ice_resistance_enabled", () -> true,
            "damage_scaling", () -> false
        )),

        NORMAL(Map.of(
            "min_temp", () -> CSMath.convertTemp(50, Temperature.Units.F, Temperature.Units.MC, true),
            "max_temp", () -> CSMath.convertTemp(100, Temperature.Units.F, Temperature.Units.MC, true),
            "temp_rate", () -> 1.0,
            "require_thermometer", () -> true,
            "fire_resistance_enabled", () -> true,
            "ice_resistance_enabled", () -> true,
            "damage_scaling", () -> true
        )),

        HARD(Map.of(
            "min_temp", () -> CSMath.convertTemp(60, Temperature.Units.F, Temperature.Units.MC, true),
            "max_temp", () -> CSMath.convertTemp(90, Temperature.Units.F, Temperature.Units.MC, true),
            "temp_rate", () -> 1.5,
            "require_thermometer", () -> true,
            "fire_resistance_enabled", () -> false,
            "ice_resistance_enabled", () -> false,
            "damage_scaling", () -> true
        )),

        CUSTOM(Map.of());

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

    public static <T> ValueSupplier<T> addSyncedSetting(String id, Supplier<T> supplier, Function<T, CompoundTag> writer, Function<CompoundTag, T> reader, Consumer<T> saver)
    {   ValueSupplier<T> loader = ValueSupplier.synced(supplier, writer, reader, saver);
        CONFIG_SETTINGS.put(id, loader);
        return loader;
    }

    public static <T> ValueSupplier<T> addSetting(String id, Supplier<T> supplier)
    {   ValueSupplier<T> loader = ValueSupplier.of(supplier);
        CONFIG_SETTINGS.put(id, loader);
        return loader;
    }

    public static Map<String, CompoundTag> encode()
    {
        Map<String, CompoundTag> map = new HashMap<>();
        CONFIG_SETTINGS.forEach((key, value) ->
        {   if (value.isSynced())
            {   map.put(key, value.encode());
            }
        });
        return map;
    }

    public static void decode(String key, CompoundTag tag)
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
    {   CONFIG_SETTINGS.values().forEach(ValueSupplier::load);
    }
}
