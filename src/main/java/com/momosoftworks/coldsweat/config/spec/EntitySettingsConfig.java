package com.momosoftworks.coldsweat.config.spec;

import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.util.serialization.ListBuilder;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class EntitySettingsConfig
{
    private static final CSConfigSpec SPEC;
    private static final CSConfigSpec.Builder BUILDER = new CSConfigSpec.Builder();

    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> INSULATED_MOUNTS;

    public static final CSConfigSpec.ConfigValue<List<?>> GOAT_FUR_GROWTH_STATS;
    public static final CSConfigSpec.ConfigValue<List<?>> CHAMELEON_SHED_STATS;

    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> CHAMELEON_SPAWN_BIOMES;
    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> GOAT_SPAWN_BIOMES;

    public static final CSConfigSpec.BooleanValue CHAMELEON_SHED_AUTOMATICALLY;
    public static final CSConfigSpec.IntValue CHAMELEON_SHED_TIME_LIMIT;

    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> ENTITY_TEMPERATURES;
    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> ENTITY_CLIMATES;
    public static final CSConfigSpec.BooleanValue ENABLE_ENTITY_CLIMATES;
    public static final CSConfigSpec.BooleanValue ADVANCED_ENTITY_TEMPERATURE;

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
                .defineListAllowEmpty(List.of("Insulated Mounts"), () -> Arrays.asList(),
                it -> it instanceof List<?> list
                      && list.size() == 3
                      && list.get(0) instanceof String
                      && (list.get(1) instanceof Number || list.get(1) instanceof String)
                      && (list.get(2) instanceof Number || list.get(2) instanceof String));

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
                .defineListAllowEmpty(List.of("Entity Temperatures"), () -> Arrays.asList(),
                it -> it instanceof List<?> list
                      && list.size() >= 3
                      && list.get(0) instanceof String
                      && (list.get(1) instanceof Number || list.get(1) instanceof String)
                      && (list.get(2) instanceof Number || list.get(2) instanceof String)
                      && (list.size() < 4 || list.get(3) instanceof String)
                      && (list.size() < 5 || list.get(4) instanceof Number || list.get(4) instanceof String));

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
                .defineListAllowEmpty(List.of("Entity Climate Settings"), () -> Arrays.asList(
                        List.of("minecraft:chicken", 10, 10, 0.6, "F"),
                        List.of("minecraft:pig", 0, 0, 0.5, "F"),
                        List.of("minecraft:cow", 0, 20, 0.2, "F"),
                        List.of("minecraft:mooshroom", 0, 20, 0.2, "F"),
                        List.of("minecraft:sheep", -20, -10, 0.3, "F"),
                        List.of("minecraft:goat", -40, -15, 0.4, "F"),
                        List.of("minecraft:horse", -5, 10, 0.3, "F"),
                        List.of("minecraft:donkey", -5, 10, 0.3, "F"),
                        List.of("minecraft:mule", -5, 10, 0.3, "F"),
                        List.of("minecraft:llama", -10, 0, 0.4, "F")
                ),
                it -> it instanceof List<?> list
                      && list.size() >= 3
                      && list.get(0) instanceof String
                      && list.get(1) instanceof Number
                      && list.get(2) instanceof Number
                      && (list.size() < 4 || list.get(3) instanceof Number)
                      && (list.size() < 5 || list.get(4) instanceof String));

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
                .defineList("Goat Fur Growth Timings", List.of(
                        1200, 2400, 0.20
                ),
                it -> it instanceof Number);

        CHAMELEON_SHED_STATS = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Defines how often a chameleon will try to shed its skin, the cooldown after shedding, and the chance of it succeeding",
                         " Format: [interval, cooldown, chance]",
                         " ⌄ ")
                .defineList("Chameleon Shedding Timings", List.of(
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
                .defineListAllowEmpty(List.of("Chameleon Spawn Biomes"), () -> ListBuilder.begin(
                                List.of("minecraft:bamboo_jungle", 80),
                                List.of("minecraft:jungle", 80),
                                List.of("minecraft:sparse_jungle", 35),
                                List.of("minecraft:desert", 1))
                            .addIf(CompatManager.isBiomesOPlentyLoaded(),
                                () -> List.of("biomesoplenty:lush_desert", 3),
                                () -> List.of("biomesoplenty:rainforest", 20),
                                () -> List.of("biomesoplenty:rocky_rainforest", 15),
                                () -> List.of("biomesoplenty:fungal_jungle", 10),
                                () -> List.of("biomesoplenty:tropics", 8),
                                () -> List.of("biomesoplenty:outback", 2))
                            .addIf(CompatManager.isBiomesYoullGoLoaded(),
                                () -> List.of("byg:tropical_rainforest", 60),
                                () -> List.of("byg:jacaranda_forest", 3),
                                () -> List.of("byg:guiana_shield", 3),
                                () -> List.of("byg:crag_gardens", 4),
                                () -> List.of("byg:atacama_desert", 1),
                                () -> List.of("byg:cypress_swamplands", 3),
                                () -> List.of("byg:mojave_desert", 1),
                                () -> List.of("byg:windswept_desert", 2))
                            .addIf(CompatManager.isBiomesWeveGoneLoaded(),
                                () -> List.of("biomeswevegone:atacama_outback", 2),
                                () -> List.of("biomeswevegone:lush_stacks", 8),
                                () -> List.of("biomeswevegone:crag_gardens", 40),
                                () -> List.of("biomeswevegone:jacaranda_jungle", 30),
                                () -> List.of("biomeswevegone:fragment_jungle", 60),
                                () -> List.of("biomeswevegone:tropical_rainforest", 60),
                                () -> List.of("biomeswevegone:mojave_desert", 3),
                                () -> List.of("biomeswevegone:windswept_desert", 2),
                                () -> List.of("biomeswevegone:red_rock_valley", 1),
                                () -> List.of("biomeswevegone:ironwood_gour", 1),
                                () -> List.of("biomeswevegone:rugged_badlands", 1),
                                () -> List.of("biomeswevegone:sierra_badlands", 2),
                                () -> List.of("biomeswevegone:sierra_badlands", 2))
                            .addIf(CompatManager.isAtmosphericLoaded(),
                                () -> List.of("atmospheric:dunes", 0.75),
                                () -> List.of("atmospheric:flourishing_dunes", 1.5),
                                () -> List.of("atmospheric:rocky_dunes", 0.75),
                                () -> List.of("atmospheric:petrified_dunes", 0.5),
                                () -> List.of("atmospheric:rainforest", 50),
                                () -> List.of("atmospheric:sparse_rainforest", 40),
                                () -> List.of("atmospheric:rainforest_basin", 50),
                                () -> List.of("atmospheric:sparse_rainforest_basin", 30))
                           .addIf(CompatManager.isTerralithLoaded(),
                                () -> List.of("terralith:red_oasis", 3),
                                () -> List.of("terralith:desert_oasis", 3),
                                () -> List.of("terralith:tropical_jungle", 80),
                                () -> List.of("terralith:arid_highlands", 1.5),
                                () -> List.of("terralith:rocky_jungle", 80),
                                () -> List.of("terralith:brushland", 1.5))
                           .addIf(CompatManager.isWythersLoaded(),
                                () -> List.of("wythers:cactus_desert", 1),
                                () -> List.of("wythers:tropical_forest", 10),
                                () -> List.of("wythers:tropical_rainforest", 80)
                        ).build(),
                        it -> it instanceof List<?> list && list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Number);

        GOAT_SPAWN_BIOMES = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Defines the biomes that Chameleons can spawn in",
                         " └── Format: [[\"biome_id\", weight], [...], etc]",
                         " • biome_id: The ID of the biome, e.g. \"minecraft:jungle\"",
                         " • weight: The likelihood for the entity to spawn compared to other entities, higher values mean more common spawns",
                         " ⌄ ")
                .defineListAllowEmpty(List.of("Goat Spawn Biomes"), () -> ListBuilder.begin(
                                List.of("minecraft:frozen_peaks", 8),
                                List.of("minecraft:jagged_peaks", 8),
                                List.of("minecraft:snowy_slopes", 8),
                                List.of("minecraft:meadow", 3),
                                List.of("minecraft:windswept_hills", 6),
                                List.of("minecraft:windswept_forest", 6),
                                List.of("minecraft:windswept_gravelly_hills", 4),
                                List.of("minecraft:grove", 5),
                                List.of("minecraft:stony_peaks", 8))
                            .addIf(CompatManager.isBiomesOPlentyLoaded(),
                                () -> List.of("biomesoplenty:boreal_forest", 5),
                                () -> List.of("biomesoplenty:jade_cliffs", 4),
                                () -> List.of("biomesoplenty:crag", 3))
                            .addIf(CompatManager.isBiomesYoullGoLoaded(),
                                () -> List.of("byg:canadian_shield", 3),
                                () -> List.of("byg:guiana_shield", 3),
                                () -> List.of("byg:fragment_forest", 128),
                                () -> List.of("byg:howling_peaks", 6),
                                () -> List.of("byg:shattered_glacier", 6),
                                () -> List.of("byg:dacite_ridges", 5))
                            .addIf(CompatManager.isBiomesWeveGoneLoaded(),
                                () -> List.of("biomeswevegone:howling_peaks", 6),
                                () -> List.of("biomeswevegone:canadian_shield", 3),
                                () -> List.of("biomeswevegone:shattered_glacier", 2),
                                () -> List.of("biomeswevegone:dacite_ridges", 5),
                                () -> List.of("biomeswevegone:zelkova_forest", 3),
                                () -> List.of("biomeswevegone:frosted_coniferous_forest", 3),
                                () -> List.of("biomeswevegone:skyrise_vale", 5),
                                () -> List.of("biomeswevegone:coconino_meadow", 3))
                            .addIf(CompatManager.isTerralithLoaded(),
                                () -> List.of("terralith:blooming_plateau", 5),
                                () -> List.of("terralith:rocky_mountains", 6),
                                () -> List.of("terralith:alpine_grove", 6),
                                () -> List.of("terralith:scarlet_mountains", 4),
                                () -> List.of("terralith:windswept_spires", 16),
                                () -> List.of("terralith:cloud_forest", 4),
                                () -> List.of("terralith:haze_mountain", 4)
                        ).build(),
                        it -> it instanceof List<?> list && list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Number);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void setup(ModContainer modContainer)
    {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path csConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "coldsweat");

        // Create the config folder
        try
        {   Files.createDirectory(csConfigPath);
        }
        catch (Exception ignored) {}

        modContainer.registerConfig(ModConfig.Type.COMMON, SPEC, "coldsweat/entity.toml");
    }

    public static void save()
    {   SPEC.save();
    }
}
