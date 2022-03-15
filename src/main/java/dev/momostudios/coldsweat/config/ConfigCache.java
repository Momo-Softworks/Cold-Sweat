package dev.momostudios.coldsweat.config;

import java.util.List;
import java.util.Map;

public class ConfigCache
{
    public int difficulty;
    public double maxTemp;
    public double minTemp;
    public double rate;
    public boolean fireRes;
    public boolean iceRes;
    public boolean damageScaling;
    public boolean showWorldTemp;
    public int gracePeriodLength;
    public boolean gracePeriodEnabled;

    public Map<String, List<? extends List<Object>>> worldOptionsReference = WorldTemperatureConfig.INSTANCE.getConfigMap();
    public ItemSettingsConfig itemSettingsReference = ItemSettingsConfig.INSTANCE;

    private static ConfigCache INSTANCE = new ConfigCache();

    public ConfigCache() {}

    public static ConfigCache getInstance()
    {
        return INSTANCE;
    }

    public static void setInstance(ConfigCache instance)
    {
        INSTANCE = instance;
    }

    public ConfigCache(ColdSweatConfig config)
    {
        writeValues(config);
    }

    public void writeValues(ColdSweatConfig config)
    {
        difficulty = config.getDifficulty();
        maxTemp = config.getMaxTempHabitable();
        minTemp = config.getMinTempHabitable();
        rate = config.getRateMultiplier();
        fireRes = config.isFireResistanceEnabled();
        iceRes = config.isIceResistanceEnabled();
        damageScaling = config.doDamageScaling();
        showWorldTemp = config.showWorldTempGauge();
        gracePeriodLength = config.getGracePeriodLength();
        gracePeriodEnabled = config.isGracePeriodEnabled();
    }
}
