package com.momosoftworks.coldsweat.config;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.config.spec.ItemSettingsConfig;
import com.momosoftworks.coldsweat.config.spec.MainSettingsConfig;
import com.momosoftworks.coldsweat.config.spec.WorldSettingsConfig;
import net.minecraftforge.fml.ModList;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigUpdater
{

    public static void updateConfigs()
    {
        String configVersion = MainSettingsConfig.getInstance().getVersion();
        ItemSettingsConfig itemSettings = ItemSettingsConfig.getInstance();
        WorldSettingsConfig worldSettings = WorldSettingsConfig.getInstance();

        /*
         2.3-b06a
         */
        if (compareVersions(configVersion, "2.3-b06a") < 0)
        {
            // Update magma block temperature
            replaceConfigSetting(worldSettings::getBlockTemps, "minecraft:magma_block", blockTemp -> {
                blockTemp.set(1, 0.25);
                blockTemp.set(4, 1.0);
            }, worldSettings::setBlockTemps);

            // Update ice fuel value
            replaceConfigSetting(itemSettings::getIceboxFuelItems, "minecraft:ice", iceFuel -> {
                iceFuel.set(1, 250);
            }, itemSettings::setIceboxFuelItems);
            replaceConfigSetting(itemSettings::getHearthFuelItems, "minecraft:ice", iceFuel -> {
                iceFuel.set(1, -250);
            }, itemSettings::setHearthFuelItems);

            // Update snow fuel value
            replaceConfigSetting(itemSettings::getIceboxFuelItems, "minecraft:snow_block", snowFuel -> {
                snowFuel.set(1, 100);
            }, itemSettings::setIceboxFuelItems);
            replaceConfigSetting(itemSettings::getHearthFuelItems, "minecraft:snow_block", snowFuel -> {
                snowFuel.set(1, -100);
            }, itemSettings::setHearthFuelItems);

            // Update powder snow fuel value
            replaceConfigSetting(itemSettings::getIceboxFuelItems, "minecraft:powder_snow_bucket", powderSnowFuel -> {
                powderSnowFuel.set(1, 100);
            }, itemSettings::setIceboxFuelItems);
            replaceConfigSetting(itemSettings::getHearthFuelItems, "minecraft:powder_snow_bucket", powderSnowFuel -> {
                powderSnowFuel.set(1, -100);
            }, itemSettings::setHearthFuelItems);

            // Update snowball fuel value
            replaceConfigSetting(itemSettings::getIceboxFuelItems, "minecraft:snowball", snowballFuel -> {
                snowballFuel.set(1, 10);
            }, itemSettings::setIceboxFuelItems);
            replaceConfigSetting(itemSettings::getHearthFuelItems, "minecraft:snowball", snowballFuel -> {
                snowballFuel.set(1, -10);
            }, itemSettings::setHearthFuelItems);
        }

        /*
         2.3-b05b
         */
        if (compareVersions(configVersion, "2.3-b05b") < 0)
        {
            // Remove water as fuel item for hearth
            removeConfigSetting(itemSettings::getHearthFuelItems, "minecraft:water_bucket", itemSettings::setHearthFuelItems);
            // Remove water as fuel item for icebox
            removeConfigSetting(itemSettings::getIceboxFuelItems, "minecraft:water_bucket", itemSettings::setIceboxFuelItems);
        }

        /*
         2.3-b04a
         */
        if (compareVersions(configVersion, "2.3-b04a") < 0)
        {
            // Update soul sprout food item
            addConfigSetting(itemSettings::getFoodTemperatures, itemSettings::setFoodTemperatures,
                             Arrays.asList("cold_sweat:soul_sprout", 0.0, "{}", 1200));
        }
        itemSettings.save();
        worldSettings.save();
        // Update config version
        MainSettingsConfig.getInstance().setVersion(getVersionString(ModList.get().getModContainerById(ColdSweat.MOD_ID).get().getModInfo().getVersion()));
    }

    public static String getVersionString(ArtifactVersion version)
    {
        String ver = version.getMajorVersion() + "." + version.getMinorVersion() + "." + version.getIncrementalVersion();
        if (!version.getQualifier().isEmpty())
        {   ver += "-" + version.getQualifier();
        }
        return ver;
    }

    public static int compareVersions(String version, String comparedTo)
    {
        String[] v1Parts = version.split("\\.|\\-");
        String[] v2Parts = comparedTo.split("\\.|\\-");

        int finalValue = 0;
        try
        {
            // If there's no version defined, return -1
            if (version.isEmpty())
            {   finalValue = -1;
                return -1;
            }
            // Compare the versions
            int i = 0;
            while (i < v1Parts.length && i < v2Parts.length)
            {
                if (v1Parts[i].matches("\\d+") && v2Parts[i].matches("\\d+"))
                {
                    int num1 = Integer.parseInt(v1Parts[i]);
                    int num2 = Integer.parseInt(v2Parts[i]);
                    if (num1 != num2)
                    {   finalValue = Integer.compare(num1, num2);
                        return finalValue;
                    }
                }
                else
                {
                    int result = comparePreReleaseVersions(v1Parts[i], v2Parts[i]);
                    if (result != 0)
                    {   finalValue = result;
                        return finalValue;
                    }
                }
                i++;
            }
            finalValue = Integer.compare(v1Parts.length, v2Parts.length);
            return finalValue;
        }
        // Log config updates
        finally
        {
            if (finalValue < 0)
            {   ColdSweat.LOGGER.warn("Last launched version is less than {}. Updating config settings...", comparedTo);
            }
        }
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
                {  return result;
                }
            }
            i++;
        }

        return Integer.compare(parts1.length, parts2.length);
    }

    public static void replaceConfigSetting(Supplier<List<? extends List<?>>> getter, String key,
                                            Consumer<List<Object>> modifier, Consumer<List<? extends List<Object>>> setter)
    {
        List<List<Object>> setting = (List<List<Object>>) new ArrayList<>(getter.get());
        for (int i = 0; i < setting.size(); i++)
        {
            List<Object> element = new ArrayList<>(setting.get(i));
            if (!element.isEmpty() && element.get(0).equals(key))
            {
                modifier.accept(element);
                setting.set(i, element);
                setter.accept(setting);
                break;
            }
        }
    }

    public static void addConfigSetting(Supplier<List<? extends List<?>>> getter, Consumer<List<? extends List<? extends Object>>> setter,
                                        List<? extends Object> newSetting)
    {
        List<List<? extends Object>> setting = new ArrayList<>(getter.get());
        if (setting.stream().noneMatch(entry -> !entry.isEmpty() && entry.get(0).equals(newSetting.get(0))))
        {
            setting.add(newSetting);
            setter.accept(setting);
        }
    }

    public static void removeConfigSetting(Supplier<List<? extends List<? extends Object>>> getter, String key, Consumer<List<? extends List<? extends Object>>> setter)
    {
        List<? extends List<? extends Object>> setting = new ArrayList<>(getter.get());
        setting.removeIf(entry -> !entry.isEmpty() && entry.get(0).equals(key));
        setter.accept(setting);
    }
}
