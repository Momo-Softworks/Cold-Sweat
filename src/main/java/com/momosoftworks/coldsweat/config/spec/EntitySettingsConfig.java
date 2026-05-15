package com.momosoftworks.coldsweat.config.spec;

import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.util.serialization.ListBuilder;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class EntitySettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> INSULATED_MOUNTS;

    public static final ForgeConfigSpec.ConfigValue<List<?>> GOAT_FUR_GROWTH_STATS;
    public static final ForgeConfigSpec.ConfigValue<List<?>> CHAMELEON_SHED_STATS;

    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> CHAMELEON_SPAWN_BIOMES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> GOAT_SPAWN_BIOMES;

    public static final ForgeConfigSpec.BooleanValue CHAMELEON_SHED_AUTOMATICALLY;
    public static final ForgeConfigSpec.IntValue CHAMELEON_SHED_TIME_LIMIT;

    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> ENTITY_TEMPERATURES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> ENTITY_CLIMATES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ENTITY_CLIMATES;
    public static final ForgeConfigSpec.BooleanValue ADVANCED_ENTITY_TEMPERATURE;

    static
    {
        BUILDER.comment("─────────────────────────────────────────────────────────────────────────",
                        " Anywhere that uses entity IDs also supports:",
                        " • Tags (e.g. \"#minecraft:skeletons\")",
                        " • Comma-separated lists (e.g. \"minecraft:zombie,#minecraft:skeletons\")",
                        "     Applies the setting to all listed IDs. Can use tags, regular IDs, and negation interchangeably",
                        " • Negation (e.g. \"!minecraft:evoker\")",
                        "     Useful with lists/tags. Excludes the listed IDs from the setting",
                        "     i.e. \"#minecraft:raiders,!minecraft:evoker\" (all raid mobs EXCEPT evokers)",
                        " Settings with \"//v\" will list elements vertically. Removing \"//v\" will list elements in one line",
                        "─────────────────────────────────────────────────────────────────────────");

        /*
         Insulated Entities
         */
        BUILDER.push("Entity Temperature");
        INSULATED_MOUNTS = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " List of entities that will insulate the player when riding them",
                         " A value of 0 provides no insulation; 1 provides full insulation",
                         " └── Format: [[\"entity_id\", coldResistance, heatResistance], [...], etc]",
                         " ⌄ ")
                .defineListAllowEmpty(Arrays.asList("Insulated Mounts"), () -> Arrays.asList(
                ),
                it ->
                {
                    if (it instanceof List<?>)
                    {   List<?> list = ((List<?>) it);
                        return list.size() == 3
                               && list.get(0) instanceof String
                               && (list.get(1) instanceof Number || list.get(1) instanceof String)
                               && (list.get(2) instanceof Number || list.get(2) instanceof String);
                    }
                    return false;
                });

        ENTITY_TEMPERATURES = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Defines temperature-emitting properties for entities",
                         " ├── Format: [[\"entity_id\", temperature, range, *units, *tempLimit], [...], etc]",
                         " └── [* = optional]",
                         " • temperature: The temperature emitted by the entity",
                         " • range: The range of the effect, in blocks",
                         " • units: (Optional) The units of the temperature value (MC, F, or C). Defaults to MC",
                         " • *tempLimit: The maximum world temperature at which this entity temp will have any effect.",
                         "   (Represents the minimum temp if the entity temp is negative)",
                         " ⌄ ")
                .defineListAllowEmpty(Arrays.asList("Entity Temperatures"), () -> Arrays.asList(),
                it ->
                {
                    if (it instanceof List<?>)
                    {
                        List<?> list = ((List<?>) it);
                        return list.size() >= 3
                               && list.get(0) instanceof String
                               && (list.get(1) instanceof Number || list.get(1) instanceof String)
                               && (list.get(2) instanceof Number || list.get(2) instanceof String)
                               && (list.size() < 4 || list.get(3) instanceof String
                               && (list.size() < 5 || list.get(4) instanceof Number || list.get(4) instanceof String));
                    }
                    return false;
                });

        ENTITY_CLIMATES = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Defines entities that are affected by temperature and the climates they can live in",
                         " ├── Format: [[\"entity_id\", minTemp, maxTemp, *rate, *units], [...], etc]",
                         " └── [* = optional]",
                         " • min_temp: The minimum livable temperature, as an offset to the minimum temperature for players",
                         " • max_temp: The maximum livable temperature, as an offset to the maximum temperature for players",
                         " • *rate: A multiplier to the rate at which the entity overheats or freezes, based on players' rate",
                         " • *units: The units used for the min/max temperature values (MC, F, or C). Defaults to MC",
                         " ⌄ ")
                .defineListAllowEmpty(Arrays.asList("Entity Climate Settings"), () -> Arrays.asList(
                        Arrays.asList("minecraft:chicken", 10, 10, 0.6, "F"),
                        Arrays.asList("minecraft:pig", 0, 0, 0.5, "F"),
                        Arrays.asList("minecraft:cow", 0, 20, 0.2, "F"),
                        Arrays.asList("minecraft:mooshroom", 0, 20, 0.2, "F"),
                        Arrays.asList("minecraft:sheep", -20, -10, 0.3, "F"),
                        Arrays.asList("cold_sweat:goat", -40, -15, 0.4, "F"),
                        Arrays.asList("minecraft:horse", -5, 10, 0.3, "F"),
                        Arrays.asList("minecraft:donkey", -5, 10, 0.3, "F"),
                        Arrays.asList("minecraft:mule", -5, 10, 0.3, "F"),
                        Arrays.asList("minecraft:llama", -10, 0, 0.4, "F")
                ),
                it -> it instanceof List<?>
                      && ((List<?>) it).size() >= 3
                      && ((List<?>) it).get(0) instanceof String
                      && ((List<?>) it).get(1) instanceof Number
                      && ((List<?>) it).get(2) instanceof Number
                      && (((List<?>) it).size() < 4 || ((List<?>) it).get(3) instanceof Number)
                      && (((List<?>) it).size() < 5 || ((List<?>) it).get(4) instanceof String));

        ENABLE_ENTITY_CLIMATES = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Enables the temperature system for entities",
                         " If disabled, entities will not be harmed by temperature",
                         " ⌄ ")
                .define("Enable Entity Climates", false);

        ADVANCED_ENTITY_TEMPERATURE = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Enables more precise temperature calculations for entities",
                         " This option might cause performance issues for large amounts of entities",
                         " ⌄ ")
                .define("Use Advanced Entity Climates", false);

        BUILDER.pop();

        BUILDER.push("Fur Growth & Shedding");

        GOAT_FUR_GROWTH_STATS = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Defines how often a goat will try to grow its fur, the growth cooldown after shearing, and the chance of it succeeding",
                         " └── Format: [interval, cooldown, chance]",
                         " ⌄ ")
                .defineList("Goat Fur Growth Timings", Arrays.asList(
                        1200, 2400, 0.20
                ),
                it -> it instanceof Number);

        CHAMELEON_SHED_STATS = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Defines how often a chameleon will try to shed its skin, the cooldown after shedding, and the chance of it succeeding",
                         " Format: [interval, cooldown, chance]",
                         " ⌄ ")
                .defineList("Chameleon Shedding Timings", Arrays.asList(
                        400, 36000, 0.10
                ),
                it -> it instanceof Number);

        CHAMELEON_SHED_AUTOMATICALLY = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " If true, chameleons will automatically drop molt when ready to shed",
                         " If false, chameleons will only drop molt when a player interacts with them",
                         " ⌄ ")
                .define("Chameleons Shed Automatically", false);

        CHAMELEON_SHED_TIME_LIMIT = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The time limit for how long a chameleon can wait to be shed by a player",
                         " Has no effect if 'Chameleons Shed Automatically' is enabled",
                         " ⌄ ")
                .defineInRange("Chameleon Interact Time Limit", 6000, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Mob Spawning");
        CHAMELEON_SPAWN_BIOMES = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Defines the biomes that Chameleons can spawn in",
                         " └── Format: [[\"biome_id\", weight], [...], etc]",
                         " • biome_id: The ID of the biome, e.g. \"minecraft:jungle\"",
                         " • weight: The likelihood for the entity to spawn compared to other entities, higher values mean more common spawns",
                         " ⌄ ")
                .defineListAllowEmpty(Arrays.asList("Chameleon Spawn Biomes"), () -> ListBuilder.begin(
                                Arrays.asList("minecraft:bamboo_jungle", 80),
                                Arrays.asList("minecraft:jungle", 80),
                                Arrays.asList("minecraft:desert", 1))
                            .addIf(CompatManager.isBiomesOPlentyLoaded(),
                                () -> Arrays.asList("biomesoplenty:lush_desert", 3),
                                () -> Arrays.asList("biomesoplenty:rainforest", 40),
                                () -> Arrays.asList("biomesoplenty:rainforest_cliffs", 15),
                                () -> Arrays.asList("biomesoplenty:rainforest_floodplain", 7),
                                () -> Arrays.asList("biomesoplenty:fungal_jungle", 10),
                                () -> Arrays.asList("biomesoplenty:tropics", 8),
                                () -> Arrays.asList("biomesoplenty:outback", 2))
                            .addIf(CompatManager.isBiomesYoullGoLoaded(),
                                () -> Arrays.asList("byg:tropical_rainforest", 60),
                                () -> Arrays.asList("byg:jacaranda_forest", 3),
                                () -> Arrays.asList("byg:guiana_shield", 10),
                                () -> Arrays.asList("byg:guiana_clearing", 3),
                                () -> Arrays.asList("byg:crag_gardens", 8),
                                () -> Arrays.asList("byg:lush_red_desert", 3),
                                () -> Arrays.asList("byg:red_desert", 2),
                                () -> Arrays.asList("byg:red_rock_mountains", 2),
                                () -> Arrays.asList("byg:mojave_desert", 1))
                            .addIf(CompatManager.isAtmosphericLoaded(),
                                () -> Arrays.asList("atmospheric:dunes", 0.75),
                                () -> Arrays.asList("atmospheric:flourishing_dunes", 1.5),
                                () -> Arrays.asList("atmospheric:rocky_dunes", 0.75),
                                () -> Arrays.asList("atmospheric:petrified_dunes", 0.5),
                                () -> Arrays.asList("atmospheric:rainforest", 70),
                                () -> Arrays.asList("atmospheric:rainforest_mountains", 70),
                                () -> Arrays.asList("atmospheric:rainforest_plateau", 70),
                                () -> Arrays.asList("atmospheric:rainforest_basin", 50),
                                () -> Arrays.asList("atmospheric:sparse_rainforest_plateau", 40),
                                () -> Arrays.asList("atmospheric:sparse_rainforest_basin", 30)
                        ).build(),
                        it ->
                        {
                            if (it instanceof List<?>)
                            {   List<?> list = ((List<?>) it);
                                return list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Number;
                            }
                            return false;
                        });

        GOAT_SPAWN_BIOMES = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Defines the biomes that Chameleons can spawn in",
                         " └── Format: [[\"biome_id\", weight], [...], etc]",
                         " • biome_id: The ID of the biome, e.g. \"minecraft:jungle\"",
                         " • weight: The likelihood for the entity to spawn compared to other entities, higher values mean more common spawns",
                         " ⌄ ")
                .defineListAllowEmpty(Arrays.asList("Goat Spawn Biomes"), () -> ListBuilder.begin(
                                Arrays.asList("minecraft:mountains", 10),
                                Arrays.asList("minecraft:wooded_mountains", 12),
                                Arrays.asList("minecraft:snowy_taiga_mountains", 12),
                                Arrays.asList("minecraft:taiga_mountains", 10),
                                Arrays.asList("minecraft:gravelly_mountains", 8))
                            .addIf(CompatManager.isBiomesOPlentyLoaded(),
                                () -> Arrays.asList("biomesoplenty:boreal_forest", 5),
                                () -> Arrays.asList("biomesoplenty:snowy_coniferous_forest", 5),
                                () -> Arrays.asList("biomesoplenty:jade_cliffs", 4))
                            .addIf(CompatManager.isBiomesYoullGoLoaded(),
                                () -> Arrays.asList("byg:alps", 16),
                                () -> Arrays.asList("byg:bluff_steeps", 8),
                                () -> Arrays.asList("byg:bluff_peaks", 8),
                                () -> Arrays.asList("byg:grassland_plateau", 4),
                                () -> Arrays.asList("byg:guiana_clearing", 3),
                                () -> Arrays.asList("byg:stone_forest", 128),
                                () -> Arrays.asList("byg:shattered_glacier", 6),
                                () -> Arrays.asList("byg:skyris_highlands", 6),
                                () -> Arrays.asList("byg:dover_mountains", 5)
                        ).build(),
                        it ->
                        {
                            if (it instanceof List<?>)
                            {   List<?> list = ((List<?>) it);
                                return list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Number;
                            }
                            return false;
                        });
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void setup()
    {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path csConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "coldsweat");

        // Create the config folder
        try
        {   Files.createDirectory(csConfigPath);
        }
        catch (Exception ignored) {}

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "coldsweat/entity.toml");
    }

    public static void save()
    {   SPEC.save();
    }
}
