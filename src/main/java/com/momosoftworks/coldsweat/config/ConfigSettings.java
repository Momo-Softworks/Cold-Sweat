package com.momosoftworks.coldsweat.config;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.spec.*;
import com.momosoftworks.coldsweat.config.type.InsulatingMount;
import com.momosoftworks.coldsweat.config.type.Insulator;
import com.momosoftworks.coldsweat.config.type.PredicateItem;
import com.momosoftworks.coldsweat.util.serialization.*;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.configuration.SpawnBiomeData;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.Vec2i;
import com.momosoftworks.coldsweat.util.registries.ModEntities;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import com.momosoftworks.coldsweat.util.serialization.Triplet;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.nbt.*;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
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
    public static final Map<String, DynamicHolder<?>> CONFIG_SETTINGS = new ConcurrentHashMap<>();

    public static Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;

    // Settings visible in the config screen
    public static final DynamicHolder<Integer> DIFFICULTY;
    public static final DynamicHolder<Double> MAX_TEMP;
    public static final DynamicHolder<Double> MIN_TEMP;
    public static final DynamicHolder<Double> TEMP_RATE;
    public static final DynamicHolder<Double> TEMP_DAMAGE;
    public static final DynamicHolder<Boolean> FIRE_RESISTANCE_ENABLED;
    public static final DynamicHolder<Boolean> ICE_RESISTANCE_ENABLED;
    public static final DynamicHolder<Boolean> DAMAGE_SCALING;
    public static final DynamicHolder<Boolean> REQUIRE_THERMOMETER;
    public static final DynamicHolder<Integer> GRACE_LENGTH;
    public static final DynamicHolder<Boolean> GRACE_ENABLED;

    // Other Difficulty Settings


    // World Settings
    public static final DynamicHolder<Map<Biome, Triplet<Double, Double, Temperature.Units>>> BIOME_TEMPS;
    public static final DynamicHolder<Map<Biome, Triplet<Double, Double, Temperature.Units>>> BIOME_OFFSETS;
    public static final DynamicHolder<Map<DimensionType, Pair<Double, Temperature.Units>>> DIMENSION_TEMPS;
    public static final DynamicHolder<Map<DimensionType, Pair<Double, Temperature.Units>>> DIMENSION_OFFSETS;
    public static final DynamicHolder<Map<Structure<?>, Pair<Double, Temperature.Units>>> STRUCTURE_TEMPS;
    public static final DynamicHolder<Map<Structure<?>, Pair<Double, Temperature.Units>>> STRUCTURE_OFFSETS;
    public static final DynamicHolder<Double> CAVE_INSULATION;
    public static final DynamicHolder<Double[]> SUMMER_TEMPS;
    public static final DynamicHolder<Double[]> AUTUMN_TEMPS;
    public static final DynamicHolder<Double[]> WINTER_TEMPS;
    public static final DynamicHolder<Double[]> SPRING_TEMPS;

    // Block settings
    public static final DynamicHolder<Integer> BLOCK_RANGE;
    public static final DynamicHolder<Boolean> COLD_SOUL_FIRE;
    public static final DynamicHolder<List<Block>> HEARTH_SPREAD_WHITELIST;
    public static final DynamicHolder<List<Block>> HEARTH_SPREAD_BLACKLIST;
    public static final DynamicHolder<Double> HEARTH_STRENGTH;
    public static final DynamicHolder<List<Block>> SLEEP_CHECK_IGNORE_BLOCKS;

    // Item settings
    public static final DynamicHolder<Map<Item, Insulator>> INSULATION_ITEMS;
    public static final DynamicHolder<Map<Item, Insulator>> INSULATING_ARMORS;
    public static final DynamicHolder<Map<Item, Insulator>> INSULATING_CURIOS;
    public static final DynamicHolder<Integer[]> INSULATION_SLOTS;
    public static final DynamicHolder<List<Item>> INSULATION_BLACKLIST;

    public static final DynamicHolder<Boolean> CHECK_SLEEP_CONDITIONS;

    public static final DynamicHolder<Map<Item, PredicateItem>> FOOD_TEMPERATURES;

    public static final DynamicHolder<Integer> WATERSKIN_STRENGTH;

    public static final DynamicHolder<List<DimensionType>> LAMP_DIMENSIONS;

    public static final DynamicHolder<Map<Item, PredicateItem>> BOILER_FUEL;
    public static final DynamicHolder<Map<Item, PredicateItem>> ICEBOX_FUEL;
    public static final DynamicHolder<Map<Item, PredicateItem>> HEARTH_FUEL;
    public static final DynamicHolder<Map<Item, PredicateItem>> SOULSPRING_LAMP_FUEL;

    public static final DynamicHolder<Boolean> HEARTH_POTIONS_ENABLED;
    public static final DynamicHolder<List<Effect>> HEARTH_POTION_BLACKLIST;

    // Entity Settings
    public static final DynamicHolder<Triplet<Integer, Integer, Double>> FUR_TIMINGS;
    public static final DynamicHolder<Triplet<Integer, Integer, Double>> SHED_TIMINGS;
    public static final DynamicHolder<Multimap<Biome, SpawnBiomeData>> ENTITY_SPAWN_BIOMES;
    public static final DynamicHolder<Map<EntityType<?>, InsulatingMount>> INSULATED_ENTITIES;

    // Client Settings **NULL ON THE SERVER**
    public static final DynamicHolder<Boolean> CELSIUS;
    public static final DynamicHolder<Integer> TEMP_OFFSET;
    public static final DynamicHolder<Double> TEMP_SMOOTHING;

    public static final DynamicHolder<Vec2i> BODY_ICON_POS;
    public static final DynamicHolder<Boolean> BODY_ICON_ENABLED;

    public static final DynamicHolder<Vec2i> WORLD_GAUGE_POS;
    public static final DynamicHolder<Boolean> WORLD_GAUGE_ENABLED;

    public static final DynamicHolder<Boolean> CUSTOM_HOTBAR_LAYOUT;
    public static final DynamicHolder<Boolean> ICON_BOBBING;

    public static final DynamicHolder<Boolean> HEARTH_DEBUG;

    public static final DynamicHolder<Boolean> SHOW_CONFIG_BUTTON;
    public static final DynamicHolder<Vec2i> CONFIG_BUTTON_POS;

    public static final DynamicHolder<Boolean> DISTORTION_EFFECTS;

    public static final DynamicHolder<Boolean> HIGH_CONTRAST;

    public static final DynamicHolder<Boolean> SHOW_CREATIVE_WARNING;


    // Makes the settings instantiation collapsible & easier to read
    static
    {
        DIFFICULTY = addSyncedSetting("difficulty", () -> MainSettingsConfig.getInstance().getDifficulty(),
        encoder -> ConfigHelper.serializeNbtInt(encoder, "Difficulty"),
        decoder -> decoder.getInt("Difficulty"),
        saver -> MainSettingsConfig.getInstance().setDifficulty(saver));

        MAX_TEMP = addSyncedSetting("max_temp", () -> MainSettingsConfig.getInstance().getMaxTempHabitable(),
        encoder -> ConfigHelper.serializeNbtDouble(encoder, "MaxTemp"),
        decoder -> decoder.getDouble("MaxTemp"),
        saver -> MainSettingsConfig.getInstance().setMaxHabitable(saver));

        MIN_TEMP = addSyncedSetting("min_temp", () -> MainSettingsConfig.getInstance().getMinTempHabitable(),
        encoder -> ConfigHelper.serializeNbtDouble(encoder, "MinTemp"),
        decoder -> decoder.getDouble("MinTemp"),
        saver -> MainSettingsConfig.getInstance().setMinHabitable(saver));

        TEMP_RATE = addSyncedSetting("temp_rate", () -> MainSettingsConfig.getInstance().getRateMultiplier(),
        encoder -> ConfigHelper.serializeNbtDouble(encoder, "TempRate"),
        decoder -> decoder.getDouble("TempRate"),
        saver -> MainSettingsConfig.getInstance().setRateMultiplier(saver));

        TEMP_DAMAGE = addSyncedSetting("temp_damage", () -> MainSettingsConfig.getInstance().getTempDamage(),
        encoder -> ConfigHelper.serializeNbtDouble(encoder, "TempDamage"),
        decoder -> decoder.getDouble("TempDamage"),
        saver -> MainSettingsConfig.getInstance().setTempDamage(saver));

        FIRE_RESISTANCE_ENABLED = addSyncedSetting("fire_resistance_enabled", () -> MainSettingsConfig.getInstance().isFireResistanceEnabled(),
        encoder -> ConfigHelper.serializeNbtBool(encoder, "FireResistanceEnabled"),
        decoder -> decoder.getBoolean("FireResistanceEnabled"),
        saver -> MainSettingsConfig.getInstance().setFireResistanceEnabled(saver));

        ICE_RESISTANCE_ENABLED = addSyncedSetting("ice_resistance_enabled", () -> MainSettingsConfig.getInstance().isIceResistanceEnabled(),
        encoder -> ConfigHelper.serializeNbtBool(encoder, "IceResistanceEnabled"),
        decoder -> decoder.getBoolean("IceResistanceEnabled"),
        saver -> MainSettingsConfig.getInstance().setIceResistanceEnabled(saver));

        DAMAGE_SCALING = addSyncedSetting("damage_scaling", () -> MainSettingsConfig.getInstance().doDamageScaling(),
        encoder -> ConfigHelper.serializeNbtBool(encoder, "DamageScaling"),
        decoder -> decoder.getBoolean("DamageScaling"),
        saver -> MainSettingsConfig.getInstance().setDamageScaling(saver));

        REQUIRE_THERMOMETER = addSyncedSetting("require_thermometer", () -> MainSettingsConfig.getInstance().thermometerRequired(),
        encoder -> ConfigHelper.serializeNbtBool(encoder, "RequireThermometer"),
        decoder -> decoder.getBoolean("RequireThermometer"),
        saver -> MainSettingsConfig.getInstance().setRequireThermometer(saver));

        GRACE_LENGTH = addSyncedSetting("grace_length", () -> MainSettingsConfig.getInstance().getGracePeriodLength(),
        encoder -> ConfigHelper.serializeNbtInt(encoder, "GraceLength"),
        decoder -> decoder.getInt("GraceLength"),
        saver -> MainSettingsConfig.getInstance().setGracePeriodLength(saver));

        GRACE_ENABLED = addSyncedSetting("grace_enabled", () -> MainSettingsConfig.getInstance().isGracePeriodEnabled(),
        encoder -> ConfigHelper.serializeNbtBool(encoder, "GraceEnabled"),
        decoder -> decoder.getBoolean("GraceEnabled"),
        saver -> MainSettingsConfig.getInstance().setGracePeriodEnabled(saver));

        BIOME_TEMPS = addSyncedSetting("biome_temps", () -> ConfigHelper.getBiomesWithValues(WorldSettingsConfig.getInstance().getBiomeTemperatures(), true),
        encoder -> ConfigHelper.serializeBiomeTemps(encoder, "BiomeTemps"),
        decoder -> ConfigHelper.deserializeBiomeTemps(decoder, "BiomeTemps"),
        saver -> WorldSettingsConfig.getInstance().setBiomeTemperatures(saver.entrySet().stream()
                                                            .map(entry ->
                                                            {
                                                                ResourceLocation biome = RegistryHelper.getBiomeId(entry.getKey());
                                                                if (biome == null) return null;

                                                                Temperature.Units units = entry.getValue().getThird();
                                                                double min = Temperature.convert(entry.getValue().getFirst(), Temperature.Units.MC, units, true);
                                                                double max = Temperature.convert(entry.getValue().getSecond(), Temperature.Units.MC, units, true);

                                                                return Arrays.asList(biome.toString(), min, max, units.toString());
                                                            })
                                                            .filter(Objects::nonNull)
                                                            .collect(Collectors.toList())));

        BIOME_OFFSETS = addSyncedSetting("biome_offsets", () -> ConfigHelper.getBiomesWithValues(WorldSettingsConfig.getInstance().getBiomeTempOffsets(), false),
        encoder -> ConfigHelper.serializeBiomeTemps(encoder, "BiomeOffsets"),
        decoder -> ConfigHelper.deserializeBiomeTemps(decoder, "BiomeOffsets"),
        saver -> WorldSettingsConfig.getInstance().setBiomeTempOffsets(saver.entrySet().stream()
                                                            .map(entry ->
                                                            {
                                                                ResourceLocation biome = RegistryHelper.getBiomeId(entry.getKey());
                                                                if (biome == null) return null;

                                                                Temperature.Units units = entry.getValue().getThird();
                                                                double min = Temperature.convert(entry.getValue().getFirst(), Temperature.Units.MC, units, false);
                                                                double max = Temperature.convert(entry.getValue().getSecond(), Temperature.Units.MC, units, false);

                                                                return Arrays.asList(biome.toString(), min, max, units.toString());
                                                            })
                                                            .filter(Objects::nonNull)
                                                            .collect(Collectors.toList())));

        DIMENSION_TEMPS = addSyncedSetting("dimension_temps", () -> ConfigHelper.getDimensionsWithValues(WorldSettingsConfig.getInstance().getDimensionTemperatures(), true),
        encoder -> ConfigHelper.serializeDimensionTemps(encoder, "DimensionTemps"),
        decoder -> ConfigHelper.deserializeDimensionTemps(decoder, "DimensionTemps"),
        saver -> WorldSettingsConfig.getInstance().setDimensionTemperatures(saver.entrySet().stream()
                                                     .map(entry ->
                                                     {
                                                         ResourceLocation dim = RegistryHelper.getDimensionId(entry.getKey());
                                                         if (dim == null) return null;

                                                         Temperature.Units units = entry.getValue().getSecond();
                                                         double temp = Temperature.convert(entry.getValue().getFirst(), Temperature.Units.MC, units, true);

                                                         return Arrays.asList(dim.toString(), temp, units.toString());
                                                     })
                                                     .filter(Objects::nonNull)
                                                     .collect(Collectors.toList())));

        DIMENSION_OFFSETS = addSyncedSetting("dimension_offsets", () -> ConfigHelper.getDimensionsWithValues(WorldSettingsConfig.getInstance().getDimensionTempOffsets(), false),
        encoder -> ConfigHelper.serializeDimensionTemps(encoder, "DimensionOffsets"),
        decoder -> ConfigHelper.deserializeDimensionTemps(decoder, "DimensionOffsets"),
        saver -> WorldSettingsConfig.getInstance().setDimensionTempOffsets(saver.entrySet().stream()
                                                     .map(entry ->
                                                     {
                                                         ResourceLocation dim = RegistryHelper.getDimensionId(entry.getKey());
                                                         if (dim == null) return null;

                                                         Temperature.Units units = entry.getValue().getSecond();
                                                         double temp = Temperature.convert(entry.getValue().getFirst(), Temperature.Units.MC, units, false);

                                                         return Arrays.asList(dim.toString(), temp, units.toString());
                                                     })
                                                     .filter(Objects::nonNull)
                                                     .collect(Collectors.toList())));

        STRUCTURE_TEMPS = addSyncedSetting("structure_temperatures", () -> ConfigHelper.getStructuresWithValues(WorldSettingsConfig.getInstance().getStructureTemperatures(), true),
        encoder -> ConfigHelper.serializeStructureTemps(encoder, "StructureTemperatures"),
        decoder -> ConfigHelper.deserializeStructureTemps(decoder, "StructureTemperatures"),
        saver -> WorldSettingsConfig.getInstance().setStructureTemperatures(saver.entrySet().stream()
                                                     .map(entry ->
                                                     {
                                                         ResourceLocation struct = RegistryHelper.getStructureId(entry.getKey());
                                                         if (struct == null) return null;

                                                         Temperature.Units units = entry.getValue().getSecond();
                                                         double temp = Temperature.convert(entry.getValue().getFirst(), Temperature.Units.MC, units, true);

                                                         return Arrays.asList(struct.toString(), temp, units.toString());
                                                     })
                                                     .filter(Objects::nonNull)
                                                     .collect(Collectors.toList())));

        STRUCTURE_OFFSETS = addSyncedSetting("structure_offsets", () -> ConfigHelper.getStructuresWithValues(WorldSettingsConfig.getInstance().getStructureTempOffsets(), false),
        encoder -> ConfigHelper.serializeStructureTemps(encoder, "StructureOffsets"),
        decoder -> ConfigHelper.deserializeStructureTemps(decoder, "StructureOffsets"),
        saver -> WorldSettingsConfig.getInstance().setStructureTempOffsets(saver.entrySet().stream()
                                                     .map(entry ->
                                                     {
                                                         ResourceLocation struct = RegistryHelper.getStructureId(entry.getKey());
                                                         if (struct == null) return null;

                                                         Temperature.Units units = entry.getValue().getSecond();
                                                         double temp = Temperature.convert(entry.getValue().getFirst(), Temperature.Units.MC, units, false);

                                                         return Arrays.asList(struct.toString(), temp, units.toString());
                                                     })
                                                     .filter(Objects::nonNull)
                                                     .collect(Collectors.toList())));

        CAVE_INSULATION = addSyncedSetting("cave_insulation", () -> WorldSettingsConfig.getInstance().getCaveInsulation(),
                                           encoder -> ConfigHelper.serializeNbtDouble(encoder, "CaveInsulation"),
                                           decoder -> decoder.getDouble("CaveInsulation"),
                                           saver -> WorldSettingsConfig.getInstance().setCaveInsulation(saver));

        BiFunction<Item, List<?>, PredicateItem> fuelMapper = (item, args) ->
        {
            double fuel = ((Number) args.get(0)).doubleValue();
            NbtRequirement nbtRequirement;
            if (args.size() > 2)
            {   nbtRequirement = new NbtRequirement(NBTHelper.parseCompoundNbt((String) args.get(2)));
            }
            else nbtRequirement = new NbtRequirement(new CompoundNBT());
            return new PredicateItem(fuel, new ItemRequirement(Arrays.asList(), Optional.empty(),
                                                               Optional.empty(), Optional.empty(),
                                                               Optional.empty(), Optional.empty(),
                                                               nbtRequirement),
                                     EntityRequirement.NONE);
        };
        BOILER_FUEL = addSetting("boiler_fuel_items", () -> ConfigHelper.readItemMap(ItemSettingsConfig.getInstance().getBoilerFuelItems(), fuelMapper));
        ICEBOX_FUEL = addSetting("icebox_fuel_items", () -> ConfigHelper.readItemMap(ItemSettingsConfig.getInstance().getIceboxFuelItems(), fuelMapper));
        HEARTH_FUEL = addSetting("hearth_fuel_items", () -> ConfigHelper.readItemMap(ItemSettingsConfig.getInstance().getHearthFuelItems(), fuelMapper));

        SOULSPRING_LAMP_FUEL = addSyncedSetting("lamp_fuel_items", () -> ConfigHelper.readItemMap(ItemSettingsConfig.getInstance().getSoulLampFuelItems(), fuelMapper),
        encoder -> ConfigHelper.serializeItemMap(encoder, "LampFuelItems", fuel -> fuel.serialize()),
        decoder -> ConfigHelper.deserializeItemMap(decoder, "LampFuelItems", nbt -> PredicateItem.deserialize(nbt)),
        saver -> ConfigHelper.writeItemMap(saver,
                                           list -> ItemSettingsConfig.getInstance().setSoulLampFuelItems(list),
                                           fuel -> Arrays.asList(fuel.value, fuel.data.nbt.tag.toString())));

        HEARTH_POTIONS_ENABLED = addSetting("hearth_potions_enabled", () -> ItemSettingsConfig.getInstance().arePotionsEnabled());
        HEARTH_POTION_BLACKLIST = addSetting("hearth_potion_blacklist", () -> ItemSettingsConfig.getInstance().getPotionBlacklist()
                                                                          .stream()
                                                                          .map(entry -> ForgeRegistries.POTIONS.getValue(new ResourceLocation(entry)))
                                                                          .collect(ArrayList::new, List::add, List::addAll));

        INSULATION_ITEMS = addSyncedSetting("insulation_items", () -> ConfigHelper.readItemInsulations(ItemSettingsConfig.getInstance().getInsulationItems(), Insulation.Slot.ITEM),
        encoder -> ConfigHelper.serializeItemInsulations(encoder, "InsulationItems"),
        decoder -> ConfigHelper.deserializeItemInsulations(decoder, "InsulationItems"),
        saver -> ConfigHelper.writeItemInsulations(saver, list -> ItemSettingsConfig.getInstance().setInsulationItems(list)));

        INSULATING_ARMORS = addSyncedSetting("insulating_armors", () -> ConfigHelper.readItemInsulations(ItemSettingsConfig.getInstance().getInsulatingArmorItems(), Insulation.Slot.ARMOR),
        encoder -> ConfigHelper.serializeItemInsulations(encoder, "InsulatingArmors"),
        decoder -> ConfigHelper.deserializeItemInsulations(decoder, "InsulatingArmors"),
        saver -> ConfigHelper.writeItemInsulations(saver, list -> ItemSettingsConfig.getInstance().setInsulatingArmorItems(list)));

        INSULATING_CURIOS = addSyncedSetting("insulating_curios", () ->
        {   if (!CompatManager.isCuriosLoaded()) return new HashMap<>();
            return ConfigHelper.readItemInsulations(ItemSettingsConfig.getInstance().getInsulatingCurios(), Insulation.Slot.CURIO);
        },
        encoder -> ConfigHelper.serializeItemInsulations(encoder, "InsulatingCurios"),
        decoder -> ConfigHelper.deserializeItemInsulations(decoder, "InsulatingCurios"),
        saver ->
        {   if (CompatManager.isCuriosLoaded())
            {   ConfigHelper.writeItemInsulations(saver, list -> ItemSettingsConfig.getInstance().setInsulatingCurios(list));
            }
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
        decoder -> new Integer[] { decoder.getInt("Head"), decoder.getInt("Chest"), decoder.getInt("Legs"), decoder.getInt("Feet") },
        saver -> ItemSettingsConfig.getInstance().setArmorInsulationSlots(Arrays.asList(saver[0], saver[1], saver[2], saver[3])));

        INSULATION_BLACKLIST = addSetting("insulation_blacklist", () -> ItemSettingsConfig.getInstance().getInsulationBlacklist()
                                                                        .stream()
                                                                        .map(entry -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry)))
                                                                        .collect(ArrayList::new, List::add, List::addAll));

        CHECK_SLEEP_CONDITIONS = addSetting("check_sleep_conditions", () -> WorldSettingsConfig.getInstance().isSleepChecked());

        SLEEP_CHECK_IGNORE_BLOCKS = addSetting("sleep_check_override_blocks", () -> ConfigHelper.getBlocks(WorldSettingsConfig.getInstance().getSleepOverrideBlocks().toArray(new String[0])));

        FOOD_TEMPERATURES = addSyncedSetting("food_temperatures", () -> ConfigHelper.readItemMap(ItemSettingsConfig.getInstance().getFoodTemperatures(), (item, args) ->
        {
            double value = ((Number) args.get(0)).doubleValue();
            NbtRequirement nbtRequirement = args.size() > 1
                                            ? new NbtRequirement(NBTHelper.parseCompoundNbt((String) args.get(1)))
                                            : new NbtRequirement(new CompoundNBT());
            Integer duration = args.size() > 2 ? ((Number) args.get(2)).intValue() : null;
            ItemRequirement itemRequirement = new ItemRequirement(Arrays.asList(), Optional.empty(),
                                                                  Optional.empty(), Optional.empty(),
                                                                  Optional.empty(), Optional.empty(),
                                                                  nbtRequirement);
            CompoundNBT tag = new CompoundNBT();
            if (duration != null)
            {   tag.putInt("duration", duration);
            }
            return new PredicateItem(value, itemRequirement, EntityRequirement.NONE, tag);
        }),
        encoder -> ConfigHelper.serializeItemMap(encoder, "FoodTemperatures", food -> food.serialize()),
        decoder -> ConfigHelper.deserializeItemMap(decoder, "FoodTemperatures", nbt -> PredicateItem.deserialize(nbt)),
        saver -> ConfigHelper.writeItemMap(saver,
                                           list -> ItemSettingsConfig.getInstance().setFoodTemperatures(list),
                                           food -> Arrays.asList(food.value, food.data.nbt.tag.toString())));

        WATERSKIN_STRENGTH = addSetting("waterskin_strength", () -> ItemSettingsConfig.getInstance().getWaterskinStrength());

        LAMP_DIMENSIONS = addSetting("valid_lamp_dimensions", () -> new ArrayList<>(ItemSettingsConfig.getInstance().getValidSoulLampDimensions()
                                                                                    .stream()
                                                                                    .map(entry -> CSMath.getIfNotNull(WorldHelper.getServer(),
                                                                                                                      server -> server.registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).get(new ResourceLocation(entry)),
                                                                                                                      null))
                                                                                    .collect(ArrayList::new, List::add, List::addAll)));

        FUR_TIMINGS = addSyncedSetting("fur_timings", () ->
        {   List<?> entry = EntitySettingsConfig.getInstance().getLlamaFurStats();
            return new Triplet<>(((Number) entry.get(0)).intValue(), ((Number) entry.get(1)).intValue(), ((Number) entry.get(2)).doubleValue());
        },
        encoder ->
        {   CompoundNBT tag = new CompoundNBT();
            tag.put("Interval", IntNBT.valueOf(encoder.getFirst()));
            tag.put("Cooldown", IntNBT.valueOf(encoder.getSecond()));
            tag.put("Chance", DoubleNBT.valueOf(encoder.getThird()));
            return tag;
        },
        decoder ->
        {   int interval = decoder.getInt("Interval");
            int cooldown = decoder.getInt("Cooldown");
            double chance = decoder.getDouble("Chance");
            return new Triplet<>(interval, cooldown, chance);
        },
        saver ->
        {   List<Number> list = new ArrayList<>();
            list.add(saver.getFirst());
            list.add(saver.getSecond());
            list.add(saver.getThird());
            EntitySettingsConfig.getInstance().setLlamaFurStats(list);
        });

        SHED_TIMINGS = addSyncedSetting("shed_timings", () ->
        {   List<?> entry = EntitySettingsConfig.getInstance().getChameleonShedStats();
            return new Triplet<>(((Number) entry.get(0)).intValue(), ((Number) entry.get(1)).intValue(), ((Number) entry.get(2)).doubleValue());
        },
        encoder ->
        {   CompoundNBT tag = new CompoundNBT();
            tag.put("Interval", IntNBT.valueOf(encoder.getFirst()));
            tag.put("Cooldown", IntNBT.valueOf(encoder.getSecond()));
            tag.put("Chance", DoubleNBT.valueOf(encoder.getThird()));
            return tag;
        },
        decoder ->
        {   int interval = decoder.getInt("Interval");
            int cooldown = decoder.getInt("Cooldown");
            double chance = decoder.getDouble("Chance");
            return new Triplet<>(interval, cooldown, chance);
        },
        saver ->
        {   List<Number> list = new ArrayList<>();
            list.add(saver.getFirst());
            list.add(saver.getSecond());
            list.add(saver.getThird());
            EntitySettingsConfig.getInstance().setChameleonShedStats(list);
        });

        ENTITY_SPAWN_BIOMES = addSetting("entity_spawn_biomes", () ->
        {
            Multimap<Biome, SpawnBiomeData> map = HashMultimap.create();
            // Function to read biomes from configs and put them in the config settings
            Consumer<List<? extends List<?>>> configReader = configBiomes ->
            {
                for (List<?> entry : configBiomes)
                {
                    String biomeId = ((String) entry.get(0));
                    List<Biome> biomes = ConfigHelper.parseRegistryItems(Registry.BIOME_REGISTRY, biomeId);
                    for (Biome biome : biomes)
                    {
                        SpawnBiomeData spawnData = new SpawnBiomeData(biomes, EntityClassification.CREATURE, ((Number) entry.get(1)).intValue(),
                                                                      Arrays.asList(Either.right(ModEntities.CHAMELEON)),
                                                                      Optional.empty());
                        map.put(biome, spawnData);
                    }
                }
            };

            // Parse goat and chameleon biomes
            configReader.accept(EntitySettingsConfig.getInstance().getChameleonSpawnBiomes());
            configReader.accept(EntitySettingsConfig.getInstance().getLlamaSpawnBiomes());

            return map;
        });

        INSULATED_ENTITIES = addSetting("insulated_entities", () ->
        EntitySettingsConfig.getInstance().getInsulatedEntities().stream().map(entry ->
        {
            List<Map.Entry<EntityType<?>, InsulatingMount>> entries = new ArrayList<>();
            String entityID = (String) entry.get(0);
            double coldInsul = ((Number) entry.get(1)).doubleValue();
            double hotInsul = entry.size() < 3
                              ? coldInsul
                              : ((Number) entry.get(2)).doubleValue();

            for (EntityType<?> entityType : ConfigHelper.getEntityTypes(entityID))
            {   entries.add(new AbstractMap.SimpleEntry<>(entityType, new InsulatingMount(entityType, coldInsul, hotInsul, EntityRequirement.NONE)));
            }

            return entries;
        })
        .flatMap(List::stream)
        .distinct()
        .filter(entry -> entry.getKey() != null)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
            ColdSweat.LOGGER.warn("Duplicate entity entry for \"{}\" found in config. Using the first entry.", ForgeRegistries.ENTITIES.getKey(a.entityType).toString());
            return a;
        })));

        BLOCK_RANGE = addSyncedSetting("block_range", () -> WorldSettingsConfig.getInstance().getBlockRange(),
        encoder -> ConfigHelper.serializeNbtInt(encoder, "BlockRange"),
        decoder -> decoder.getInt("BlockRange"),
        saver -> WorldSettingsConfig.getInstance().setBlockRange(saver));

        COLD_SOUL_FIRE = addSetting("cold_soul_fire", () -> WorldSettingsConfig.getInstance().isSoulFireCold());

        HEARTH_SPREAD_WHITELIST = addSyncedSetting("hearth_spread_whitelist", () -> ConfigHelper.getBlocks(WorldSettingsConfig.getInstance().getHearthSpreadWhitelist().toArray(new String[0])),
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
        saver -> WorldSettingsConfig.getInstance().setHearthSpreadWhitelist(saver.stream().map(ForgeRegistries.BLOCKS::getKey).collect(Collectors.toList())));

        HEARTH_SPREAD_BLACKLIST = addSyncedSetting("hearth_spread_blacklist", () -> ConfigHelper.getBlocks(WorldSettingsConfig.getInstance().getHearthSpreadBlacklist().toArray(new String[0])),
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
        saver -> WorldSettingsConfig.getInstance().setHearthSpreadBlacklist(saver.stream().map(ForgeRegistries.BLOCKS::getKey).collect(Collectors.toList())));

        HEARTH_STRENGTH = addSetting("hearth_effect", () -> WorldSettingsConfig.getInstance().getHearthStrength());

        CELSIUS = addClientSetting("celsius", () -> ClientSettingsConfig.getInstance().isCelsius());

        TEMP_OFFSET = addClientSetting("temp_offset", () -> ClientSettingsConfig.getInstance().getTempOffset());

        TEMP_SMOOTHING = addClientSetting("temp_smoothing", () -> ClientSettingsConfig.getInstance().getTempSmoothing());

        BODY_ICON_POS = addClientSetting("body_icon_pos", () -> new Vec2i(ClientSettingsConfig.getInstance().getBodyIconX(),
                                                                  ClientSettingsConfig.getInstance().getBodyIconY()));
        BODY_ICON_ENABLED = addClientSetting("body_icon_enabled", () -> ClientSettingsConfig.getInstance().isBodyIconEnabled());

        WORLD_GAUGE_POS = addClientSetting("world_gauge_pos", () -> new Vec2i(ClientSettingsConfig.getInstance().getWorldGaugeX(),
                                                                    ClientSettingsConfig.getInstance().getWorldGaugeY()));
        WORLD_GAUGE_ENABLED = addClientSetting("world_gauge_enabled", () -> ClientSettingsConfig.getInstance().isWorldGaugeEnabled());

        CUSTOM_HOTBAR_LAYOUT = addClientSetting("custom_hotbar_layout", () -> ClientSettingsConfig.getInstance().customHotbarEnabled());
        ICON_BOBBING = addClientSetting("icon_bobbing", () -> ClientSettingsConfig.getInstance().isIconBobbingEnabled());

        HEARTH_DEBUG = addClientSetting("hearth_debug", () -> ClientSettingsConfig.getInstance().isHearthDebugEnabled());

        SHOW_CONFIG_BUTTON = addClientSetting("show_config_button", () -> ClientSettingsConfig.getInstance().isConfigButtonEnabled());
        CONFIG_BUTTON_POS = addClientSetting("config_button_pos", () -> new Vec2i(ClientSettingsConfig.getInstance().getConfigButtonPos().get(0),
                                                                          ClientSettingsConfig.getInstance().getConfigButtonPos().get(1)));

        DISTORTION_EFFECTS = addClientSetting("distortion_effects", () -> ClientSettingsConfig.getInstance().areDistortionsEnabled());

        HIGH_CONTRAST = addClientSetting("high_contrast", () -> ClientSettingsConfig.getInstance().isHighContrast());

        SHOW_CREATIVE_WARNING = addClientSetting("show_creative_warning", () -> ClientSettingsConfig.getInstance().isCreativeWarningEnabled());

        boolean ssLoaded = CompatManager.isSereneSeasonsLoaded();
        SUMMER_TEMPS = addSetting("summer_temps", ssLoaded ? () -> WorldSettingsConfig.getInstance().getSummerTemps() : () -> new Double[3]);
        AUTUMN_TEMPS = addSetting("autumn_temps", ssLoaded ? () -> WorldSettingsConfig.getInstance().getAutumnTemps() : () -> new Double[3]);
        WINTER_TEMPS = addSetting("winter_temps", ssLoaded ? () -> WorldSettingsConfig.getInstance().getWinterTemps() : () -> new Double[3]);
        SPRING_TEMPS = addSetting("spring_temps", ssLoaded ? () -> WorldSettingsConfig.getInstance().getSpringTemps() : () -> new Double[3]);
    }

    public enum Difficulty
    {
        SUPER_EASY(CSMath.mapOf(
            "min_temp", () -> Temperature.convert(40, Temperature.Units.F, Temperature.Units.MC, true),
            "max_temp", () -> Temperature.convert(120, Temperature.Units.F, Temperature.Units.MC, true),
            "temp_rate", () -> 0.5,
            "require_thermometer", () -> false,
            "fire_resistance_enabled", () -> true,
            "ice_resistance_enabled", () -> true,
            "damage_scaling", () -> false
        )),

        EASY(CSMath.mapOf(
            "min_temp", () -> Temperature.convert(45, Temperature.Units.F, Temperature.Units.MC, true),
            "max_temp", () -> Temperature.convert(110, Temperature.Units.F, Temperature.Units.MC, true),
            "temp_rate", () -> 0.75,
            "require_thermometer", () -> false,
            "fire_resistance_enabled", () -> true,
            "ice_resistance_enabled", () -> true,
            "damage_scaling", () -> false
        )),

        NORMAL(CSMath.mapOf(
            "min_temp", () -> Temperature.convert(50, Temperature.Units.F, Temperature.Units.MC, true),
            "max_temp", () -> Temperature.convert(100, Temperature.Units.F, Temperature.Units.MC, true),
            "temp_rate", () -> 1.0,
            "require_thermometer", () -> true,
            "fire_resistance_enabled", () -> true,
            "ice_resistance_enabled", () -> true,
            "damage_scaling", () -> true
        )),

        HARD(CSMath.mapOf(
            "min_temp", () -> Temperature.convert(60, Temperature.Units.F, Temperature.Units.MC, true),
            "max_temp", () -> Temperature.convert(90, Temperature.Units.F, Temperature.Units.MC, true),
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

    public static <T> DynamicHolder<T> addSyncedSetting(String id, Supplier<T> supplier, Function<T, CompoundNBT> writer, Function<CompoundNBT, T> reader, Consumer<T> saver)
    {   DynamicHolder<T> loader = DynamicHolder.createSynced(supplier, writer, reader, saver);
        CONFIG_SETTINGS.put(id, loader);
        return loader;
    }

    public static <T> DynamicHolder<T> addSetting(String id, Supplier<T> supplier)
    {   DynamicHolder<T> loader = DynamicHolder.create(supplier);
        CONFIG_SETTINGS.put(id, loader);
        return loader;
    }

    public static <T> DynamicHolder<T> addClientSetting(String id, Supplier<T> supplier)
    {
        return FMLEnvironment.dist == Dist.CLIENT
             ? addSetting(id, supplier)
             : new DynamicHolder<>(() -> null);
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

    public static void load(DynamicRegistries registryAccess)
    {   CONFIG_SETTINGS.values().forEach(DynamicHolder::load);
        if (registryAccess != null)
        {   ConfigRegistryHandler.collectConfigRegistries(registryAccess);
        }
        else
        {   ColdSweat.LOGGER.warn("Loading Cold Sweat config settings without loading registries. This is normal during startup.");
        }
    }
}
