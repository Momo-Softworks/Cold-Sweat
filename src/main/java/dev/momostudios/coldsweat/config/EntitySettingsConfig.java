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

    public static final EntitySettingsConfig INSTANCE = new EntitySettingsConfig();

    static
    {
        /*
         Insulated Entities
         */
        BUILDER.push("Insulated Entities");
        insulatedEntities = BUILDER
            .comment("List of entities that will insulate the player when riding them",
                     "Format: [Entity ID, Insulation Amount]",
                     "(1 is complete insulation, 0 is no insulation)")
            .defineList("InsulatedEntities", Arrays.asList(
            ), it -> it instanceof List<?> && ((List<?>) it).size() == 2 && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);
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

    /*
     * Non-private values for use elsewhere
     */

    public List<? extends List<Object>> insulatedEntities() {
        return insulatedEntities.get();
    }

    public void setInsulatedEntities(List<? extends List<Object>> list) {
        insulatedEntities.set(list);
    }
}
