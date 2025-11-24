package com.momosoftworks.coldsweat.config;

import com.google.common.io.Files;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.modifier.InventoryItemsTempModifier;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.config.spec.ItemSettingsConfig;
import com.momosoftworks.coldsweat.config.spec.MainSettingsConfig;
import com.momosoftworks.coldsweat.config.spec.WorldSettingsConfig;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ModUpdater
{
    public static void updateEntity(LivingEntity entity)
    {
        String entityVersion = entity.getPersistentData().getString("cs:version");
        if (compareVersions(entityVersion, "2.3-b03a") < 0)
        {
            EntityTempManager.getTemperatureCap(entity).ifPresent(cap -> cap.getModifiers().forEach((trait, list) ->
            {
                list.removeIf(mod -> mod.getClass() == InventoryItemsTempModifier.class);
            }));
        }
        entity.getPersistentData().putString("cs:version", ColdSweat.getVersion());
    }

    public static void updateConfigs()
    {
        // Do not run if auto-update is disabled
        if (!MainSettingsConfig.AUTO_UPDATE.get()) return;

        String version = ColdSweat.getVersion();

        String configVersion = MainSettingsConfig.VERSION.get();

        if (isBehind(configVersion, "2.3-b05b"))
        {
            ColdSweat.LOGGER.error("Cancelling config auto-updater. Version {} is older than minimum supported version: 2.3-b05b", configVersion);
            return;
        }

        /*
         2.4-b02a
         */
        if (isBehind(configVersion, "2.4-b02a"))
        {
            // Update old insulation items
            List<? extends List> itemInsulations = new ArrayList<>(ItemSettingsConfig.INSULATION_ITEMS.get());
            for (List entry : itemInsulations)
            {
                if (!entry.contains("adaptive")) CSMath.setOrAppend(entry, 3, "static");
                CSMath.setOrAppend(entry, 4, "");
                CSMath.setOrAppend(entry, 5, true);
            }
            ItemSettingsConfig.INSULATION_ITEMS.set((List) itemInsulations);

            // Chameleon armor
            addConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, Arrays.asList("cold_sweat:chameleon_scale_helmet", 8, 0.0085, "adaptive", "", true));
            addConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, Arrays.asList("cold_sweat:chameleon_scale_chestplate", 12, 0.0085, "adaptive", "", true));
            addConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, Arrays.asList("cold_sweat:chameleon_scale_leggings", 10, 0.0085, "adaptive", "", true));
            addConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, Arrays.asList("cold_sweat:chameleon_scale_boots", 8, 0.0085, "adaptive", "", true));

            addConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, Arrays.asList("cold_sweat:chameleon_scale_helmet", 8, 0.0085, "adaptive"));
            addConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, Arrays.asList("cold_sweat:chameleon_scale_chestplate", 12, 0.0085, "adaptive"));
            addConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, Arrays.asList("cold_sweat:chameleon_scale_leggings", 10, 0.0085, "adaptive"));
            addConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, Arrays.asList("cold_sweat:chameleon_scale_boots", 8, 0.0085, "adaptive"));

            // Remove hearth bottom as a whitelisted block
            List whitelist = new ArrayList<>(WorldSettingsConfig.SOURCE_SPREAD_WHITELIST.get());
            whitelist.remove("cold_sweat:hearth_bottom");
            WorldSettingsConfig.SOURCE_SPREAD_WHITELIST.set(whitelist);

            // Add modded dimension temperature offsets
            if (CompatManager.isTwilightForestLoaded())
            {   addConfigSetting(WorldSettingsConfig.DIMENSION_TEMP_OFFSETS, Arrays.asList("twilightforest:twilight_forest_type", 0.2));
            }
            if (CompatManager.isAetherLoaded())
            {   addConfigSetting(WorldSettingsConfig.DIMENSION_TEMP_OFFSETS, Arrays.asList("aether:the_aether", 0.7));
            }
        }

        /*
         2.4-b02b
         */
        if (isBehind(configVersion, "2.4-b02b"))
        {
            addConfigSetting(WorldSettingsConfig.BLOCK_TEMPERATURES, Arrays.asList("minecraft:lava", 0.25, 7, "mc", 4, "", "", 21.5));
        }

        /*
         2.4-b02c
         */
        if (isBehind(configVersion, "2.4-b02c"))
        {
            removeConfigSetting(WorldSettingsConfig.BLOCK_TEMPERATURES, "minecraft:lava");
            addConfigSetting(WorldSettingsConfig.BLOCK_TEMPERATURES, Arrays.asList("minecraft:lava", 0.25, 7, "mc", 4, "", "", 21.5, true));
        }

        /*
         2.4-b03a
         */
        if (isBehind(configVersion, "2.4-b03a"))
        {
            removeConfigSetting(WorldSettingsConfig.BLOCK_TEMPERATURES, "cold_sweat:soul_stalk");
        }

        /*
         2.4-b04a
         */
        if (isBehind(configVersion, "2.4-b04a"))
        {
            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "minecraft:leather_helmet");
            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "minecraft:leather_chestplate");
            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "minecraft:leather_leggings");
            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "minecraft:leather_boots");

            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "cold_sweat:hoglin_headpiece");
            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "cold_sweat:hoglin_tunic");
            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "cold_sweat:hoglin_trousers");
            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "cold_sweat:hoglin_hooves");

            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "cold_sweat:goat_fur_cap");
            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "cold_sweat:goat_fur_parka");
            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "cold_sweat:goat_fur_pants");
            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "cold_sweat:goat_fur_boots");

            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "cold_sweat:chameleon_scale_helmet");
            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "cold_sweat:chameleon_scale_chestplate");
            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "cold_sweat:chameleon_scale_leggings");
            removeConfigSetting(ItemSettingsConfig.INSULATION_ITEMS, "cold_sweat:chameleon_scale_boots");

            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "minecraft:leather_helmet", list ->
            {   list.clear();
                list.addAll(Arrays.asList("minecraft:leather_helmet",      5,  5));
            });
            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "minecraft:leather_chestplate", list ->
            {   list.clear();
                list.addAll(Arrays.asList("minecraft:leather_chestplate", 7,  7));
            });
            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "minecraft:leather_leggings", list ->
            {   list.clear();
                list.addAll(Arrays.asList("minecraft:leather_leggings",    6,  6));
            });
            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "minecraft:leather_boots", list ->
            {   list.clear();
                list.addAll(Arrays.asList("minecraft:leather_boots",      5,  5));
            });

            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "cold_sweat:hoglin_headpiece", list ->
            {   list.clear();
                list.addAll(Arrays.asList("cold_sweat:hoglin_headpiece",  0, 10));
            });
            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "cold_sweat:hoglin_tunic", list ->
            {   list.clear();
                list.addAll(Arrays.asList("cold_sweat:hoglin_tunic",      0, 14));
            });
            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "cold_sweat:hoglin_trousers", list ->
            {   list.clear();
                list.addAll(Arrays.asList("cold_sweat:hoglin_trousers",   0, 12));
            });
            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "cold_sweat:hoglin_hooves", list ->
            {   list.clear();
                list.addAll(Arrays.asList("cold_sweat:hoglin_hooves",     0, 10));
            });

            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "cold_sweat:goat_fur_cap", list ->
            {   list.clear();
                list.addAll(Arrays.asList("cold_sweat:goat_fur_cap",      10, 0));
            });
            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "cold_sweat:goat_fur_parka", list ->
            {   list.clear();
                list.addAll(Arrays.asList("cold_sweat:goat_fur_parka",    14, 0));
            });
            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "cold_sweat:goat_fur_pants", list ->
            {   list.clear();
                list.addAll(Arrays.asList("cold_sweat:goat_fur_pants",    12, 0));
            });
            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "cold_sweat:goat_fur_boots", list ->
            {   list.clear();
                list.addAll(Arrays.asList("cold_sweat:goat_fur_boots",    10, 0));
            });

            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "cold_sweat:chameleon_scale_helmet", list ->
            {   list.clear();
                list.addAll(Arrays.asList("cold_sweat:chameleon_scale_helmet", 10, 0.0085, "adaptive"));
            });
            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "cold_sweat:chameleon_scale_chestplate", list ->
            {   list.clear();
                list.addAll(Arrays.asList("cold_sweat:chameleon_scale_chestplate", 14, 0.0085, "adaptive"));
            });
            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "cold_sweat:chameleon_scale_leggings", list ->
            {   list.clear();
                list.addAll(Arrays.asList("cold_sweat:chameleon_scale_leggings", 12, 0.0085, "adaptive"));
            });
            replaceConfigSetting(ItemSettingsConfig.INSULATING_ARMOR, "cold_sweat:chameleon_scale_boots", list ->
            {   list.clear();
                list.addAll(Arrays.asList("cold_sweat:chameleon_scale_boots", 10, 0.0085, "adaptive"));
            });
        }

        /*
         2.4-b05a
         */
        if (isBehind(configVersion, "2.4-b05a"))
        {
            addConfigSetting(ItemSettingsConfig.BOILER_FUELS, Arrays.asList("minecraft:dried_kelp_block", 92));
            addConfigSetting(ItemSettingsConfig.HEARTH_FUELS, Arrays.asList("minecraft:dried_kelp_block", 92));
        }

        /*
         2.4-b05b
         */
        if (isBehind(configVersion, "2.4-b05b"))
        {
            replaceConfigSetting(WorldSettingsConfig.BLOCK_TEMPERATURES, "minecraft:fire", list ->
            {   list.set(0, "#minecraft:fire");
            });
            replaceConfigSetting(WorldSettingsConfig.BLOCK_TEMPERATURES, "#minecraft:campfires,!#forge:soul_campfires", list ->
            {   list.set(0, "#minecraft:campfires");
            });
        }

        /*
         2.4-b06c
         */
        if (isBehind(configVersion, "2.4-b06c"))
        {
            replaceConfigSetting(ItemSettingsConfig.HEARTH_FUELS, "#minecraft:coals", list -> list.set(1, 55));
            replaceConfigSetting(ItemSettingsConfig.HEARTH_FUELS, "#minecraft:logs_that_burn", list -> list.set(1, 40));
            replaceConfigSetting(ItemSettingsConfig.HEARTH_FUELS, "minecraft:dried_kelp_block", list -> list.set(1, 40));
            replaceConfigSetting(ItemSettingsConfig.HEARTH_FUELS, "minecraft:coal_block", list -> list.set(1, 500));
            replaceConfigSetting(ItemSettingsConfig.HEARTH_FUELS, "minecraft:snow_block", list -> list.set(1, -40));

            replaceConfigSetting(ItemSettingsConfig.BOILER_FUELS, "#minecraft:coals", list -> list.set(1, 55));
            replaceConfigSetting(ItemSettingsConfig.BOILER_FUELS, "#minecraft:logs_that_burn", list -> list.set(1, 40));
            replaceConfigSetting(ItemSettingsConfig.BOILER_FUELS, "minecraft:dried_kelp_block", list -> list.set(1, 40));
            replaceConfigSetting(ItemSettingsConfig.BOILER_FUELS, "minecraft:coal_block", list -> list.set(1, 500));

            replaceConfigSetting(ItemSettingsConfig.ICEBOX_FUELS, "minecraft:snow_block", list -> list.set(1, 40));
        }

        // Update config version
        MainSettingsConfig.VERSION.set(version);

        MainSettingsConfig.save();
        ItemSettingsConfig.save();
        WorldSettingsConfig.save();
    }

    public static void updateFileNames()
    {
        for (File file : ConfigLoadingHandler.findFilesRecursive(FMLPaths.CONFIGDIR.get().resolve("coldsweat").toFile()))
        {
            if (!file.isFile()) continue;
            switch (file.getName())
            {
                case "world_settings.toml"  : renameFile(file, "world.toml"); break;
                case "entity_settings.toml" : renameFile(file, "entity.toml"); break;
                case "item_settings.toml"   : renameFile(file, "item.toml"); break;
            }
        }
    }

    private static void renameFile(File file, String newName)
    {
        File newFile = file.toPath().resolveSibling(newName).toFile();
        try
        {   Files.move(file, newFile);
        }
        catch (Exception e)
        {   ColdSweat.LOGGER.error("Failed to rename file {} to {}", file, newFile, e);
        }
    }

    public static String getVersionString(ArtifactVersion version)
    {
        String ver = version.getMajorVersion() + "." + version.getMinorVersion();
        if (version.getIncrementalVersion() != 0)
        {   ver += "." + version.getIncrementalVersion();
        }
        if (version.getQualifier() != null && !version.getQualifier().isEmpty())
        {   ver += "-" + version.getQualifier();
        }
        return ver;
    }

    private static boolean isBehind(String version, String comparedTo)
    {
        boolean isBehind = compareVersions(version, comparedTo) < 0;
        if (isBehind)
        {   ColdSweat.LOGGER.info("Last launched version {} is less than {}. Updating config settings...", version, comparedTo);
        }
        return isBehind;
    }

    public static int compareVersions(String version, String comparedTo)
    {
        String[] v1Parts = version.split("\\.|\\-");
        String[] v2Parts = comparedTo.split("\\.|\\-");

        int i = 0;
        while (i < v1Parts.length && i < v2Parts.length)
        {
            if (v1Parts[i].matches("\\d+") && v2Parts[i].matches("\\d+"))
            {
                int num1 = Integer.parseInt(v1Parts[i]);
                int num2 = Integer.parseInt(v2Parts[i]);
                if (num1 != num2)
                {   return Integer.compare(num1, num2);
                }
            }
            else
            {
                // If one version is a full release and the other is a beta, the full release is greater
                if (!v1Parts[i].startsWith("b") && v2Parts[i].startsWith("b"))
                {   return 1;
                }
                else if (v1Parts[i].startsWith("b") && !v2Parts[i].startsWith("b"))
                {   return -1;
                }

                int result = comparePreReleaseVersions(v1Parts[i], v2Parts[i]);
                if (result != 0)
                {   return result;
                }
            }
            i++;
        }

        // If all parts are equal so far, but one version has more parts, it's greater
        // unless the shorter one is a full release and the longer one is a beta
        if (v1Parts.length != v2Parts.length)
        {
            if (i == v1Parts.length && i < v2Parts.length && v2Parts[i].startsWith("b"))
            {   return 1;
            }
            else if (i == v2Parts.length && i < v1Parts.length && v1Parts[i].startsWith("b"))
            {   return -1;
            }
        }

        return Integer.compare(v1Parts.length, v2Parts.length);
    }

    private static int comparePreReleaseVersions(String v1, String v2)
    {
        if (v1.startsWith("b") && v2.startsWith("b"))
        {   return compareWithSubVersions(v1.substring(1), v2.substring(1));
        }
        return v1.compareTo(v2);
    }

    private static int compareWithSubVersions(String v1, String v2)
    {
        String[] parts1 = v1.split("(?<=\\d)(?=\\D)|(?<=\\D)(?=\\d)");
        String[] parts2 = v2.split("(?<=\\d)(?=\\D)|(?<=\\D)(?=\\d)");

        int i = 0;
        while (i < parts1.length && i < parts2.length)
        {
            if (parts1[i].matches("\\d+") && parts2[i].matches("\\d+"))
            {
                int num1 = Integer.parseInt(parts1[i]);
                int num2 = Integer.parseInt(parts2[i]);
                if (num1 != num2)
                {   return Integer.compare(num1, num2);
                }
            }
            else
            {
                int result = parts1[i].compareTo(parts2[i]);
                if (result != 0)
                {   return result;
                }
            }
            i++;
        }

        return Integer.compare(parts1.length, parts2.length);
    }

    public static boolean replaceConfigSetting(ForgeConfigSpec.ConfigValue<List<? extends List<?>>> config, String key,
                                               Consumer<List<Object>> modifier)
    {
        List<List<?>> setting = new ArrayList<>(config.get());
        for (int i = 0; i < setting.size(); i++)
        {
            List<Object> element = new ArrayList<>(setting.get(i));
            if (!element.isEmpty() && element.get(0).equals(key))
            {
                try
                {
                    modifier.accept(element);
                    setting.set(i, element);
                    config.set(setting);
                    return true;
                }
                catch (Exception e)
                {   ColdSweat.LOGGER.error("Failed to update config setting {} for key '{}'", config.getPath(), key, e);
                    return false;
                }
            }
        }
        return false;
    }

    public static void addConfigSetting(ForgeConfigSpec.ConfigValue<List<? extends List<?>>> config, List<?> newSetting)
    {
        List<List<? extends Object>> setting = new ArrayList<>(config.get());
        if (setting.stream().noneMatch(entry -> !entry.isEmpty() && entry.get(0).equals(newSetting.get(0))))
        {
            config.clearCache();
            setting.add(newSetting);
            config.set(setting);
        }
    }

    public static void removeConfigSetting(ForgeConfigSpec.ConfigValue<List<? extends List<? extends Object>>> config, String key)
    {
        List<? extends List<? extends Object>> setting = new ArrayList<>(config.get());
        setting.removeIf(entry -> !entry.isEmpty() && entry.get(0).equals(key));
        config.set(setting);
    }
}
