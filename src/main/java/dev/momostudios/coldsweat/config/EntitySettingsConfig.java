package dev.momostudios.coldsweat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class EntitySettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<List<? extends List<Object>>> insulatedEntities;
    public static final ForgeConfigSpec.ConfigValue<List<?>> goatFurGrowth;

    static final EntitySettingsConfig INSTANCE = new EntitySettingsConfig();

    static
    {
        /*
         Insulated Entities
         */
        BUILDER.push("Mount Settings");
        insulatedEntities = BUILDER
                .comment("List of entities that will insulate the player when riding them",
                        "Format: [\"entity-id\", insulation-amount]",
                        "(1 is complete insulation, 0 is no insulation)")
                .defineList("Insulated Entities", Arrays.asList(
                ), it -> it instanceof List && ((List<?>) it).size() == 2 && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);
        BUILDER.pop();

        goatFurGrowth = BUILDER
                .comment("Defines how often a goat will try to grow its fur, the growth cooldown after shearing, and the chance of it succeeding",
                        "Format: [ticks, cooldown, chance]")
                .defineList("Goat Fur Growth Timings", List.of(
                        1200, 2400, 0.20
                ), it -> it instanceof Number);

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

    public List<? extends List<Object>> insulatedEntities() {
        return insulatedEntities.get();
    }

    public List<?> goatFurGrowth() {
        return goatFurGrowth.get();
    }

    public void setInsulatedEntities(List<? extends List<Object>> list) {
        insulatedEntities.set(list);
    }
}
