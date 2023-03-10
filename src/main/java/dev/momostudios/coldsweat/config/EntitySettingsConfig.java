package dev.momostudios.coldsweat.config;

import dev.momostudios.coldsweat.util.compat.CompatManager;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EntitySettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<List<? extends List<Object>>> insulatedEntities;
    public static final ForgeConfigSpec.ConfigValue<List<?>> goatFurGrowth;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> chameleonTameItems;

    static final EntitySettingsConfig INSTANCE = new EntitySettingsConfig();
    public static ForgeConfigSpec.ConfigValue<List<? extends List<?>>> chameleonBiomes;

    static
    {
        /*
         Insulated Entities
         */
        BUILDER.push("Entity Settings");
        insulatedEntities = BUILDER
                .comment("List of entities that will insulate the player when riding them",
                        "Format: [\"entity_id\", insulation]",
                        "(1 is complete insulation, 0 is no insulation)")
                .defineList("Insulated Entities", List.of(),
                        it -> it instanceof List list && list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Number);

        goatFurGrowth = BUILDER
                .comment("Defines how often a goat will try to grow its fur, the growth cooldown after shearing, and the chance of it succeeding",
                        "Format: [ticks, cooldown, chance]")
                .defineList("Goat Fur Growth Timings", List.of(
                        1200, 2400, 0.20
                    ), it -> it instanceof Number);

        chameleonTameItems = BUILDER
                .comment("Defines the items that can be used to tame a chameleon",
                        "Format: [\"item_id\", chance]")
                .defineList("Chameleon Tame Items", List.of(
                        List.of("minecraft:spider_eye", 0.3)
                    ), it -> it instanceof List<?> list && list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Number);

        BUILDER.pop();

        BUILDER.push("Mob Spawning");
        List<List<?>> chameleonBiomes = new ArrayList<>(List.of(
                List.of("minecraft:jungle", 8),
                List.of("minecraft:sparse_jungle", 8),
                List.of("minecraft:bamboo_jungle", 8),
                List.of("minecraft:desert", 8),
                List.of("minecraft:desert_hills", 8),
                List.of("minecraft:desert_lakes", 8)
        ));
        if (CompatManager.isBiomesOPlentyLoaded())
        {
            chameleonBiomes.addAll(List.of(
                List.of("biomesoplenty:rainforest", 16),
                List.of("biomesoplenty:rocky_rainforest", 16),
                List.of("biomesoplenty:fungal_jungle", 16),
                List.of("biomesoplenty:lush_desert", 8),
                List.of("biomesoplenty:tropics", 16),
                List.of("biomesoplenty:outback", 8),
                List.of("biomesoplenty:lush_desert", 8)));
        }
        if (CompatManager.isBiomesYoullGoLoaded())
        {
            chameleonBiomes.addAll(List.of(
                List.of("byg:tropical_rainforest", 16),
                List.of("byg:atacama_desert", 8),
                List.of("byg:cypress_swamplands", 8),
                List.of("byg:guiana_shield", 16),
                List.of("byg:mojave_desert", 8),
                List.of("byg:mojave_desert", 8),
                List.of("byg:windswept_desert", 8)
            ));
        }
        EntitySettingsConfig.chameleonBiomes = BUILDER
                .comment("Defines the biomes that Chameleons can spawn in")
                .defineList("Chameleon Spawn Biomes", chameleonBiomes,
                        it -> it instanceof List<?> list && list.get(0) instanceof String && list.get(1) instanceof Number);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void setup()
    {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path csConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "coldsweat");

        // Create the config folder
        try
        {
            Files.createDirectory(csConfigPath);
        }
        catch (Exception e)
        {
            // Do nothing
        }

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, SPEC, "coldsweat/entity_settings.toml");
    }

    public static EntitySettingsConfig getInstance()
    {
        return INSTANCE;
    }

    /*
     * Non-private values for use elsewhere
     */

    public List<? extends List<Object>> getInsulatedEntities() {
        return insulatedEntities.get();
    }
    public void setInsulatedEntities(List<? extends List<Object>> list) {
        insulatedEntities.set(list);
    }

    public List<?> getGoatFurStats() {
        return goatFurGrowth.get();
    }

    public List<? extends List<?>> getChameleonSpawnBiomes() {
        return chameleonBiomes.get();
    }

    public List<? extends List<?>> getChameleonTameItems() {
        return chameleonTameItems.get();
    }

    public void setGoatFurStats(List<? extends Number> list)
    {
        goatFurGrowth.set(list);
    }
}
