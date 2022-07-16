package dev.momostudios.coldsweat.util.config;

import dev.momostudios.coldsweat.config.ColdSweatConfig;

public class ConfigCache
{
    public int difficulty;
    public double maxTemp;
    public double minTemp;
    public double rate;
    public boolean fireRes;
    public boolean iceRes;
    public boolean damageScaling;
    public boolean requireThermometer;
    public int graceLength;
    public boolean graceEnabled;

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
        readValues(config);
    }

    public void readValues(ColdSweatConfig config)
    {
        difficulty = config.getDifficulty();
        maxTemp = config.getMaxTempHabitable();
        minTemp = config.getMinTempHabitable();
        rate = config.getRateMultiplier();
        fireRes = config.isFireResistanceEnabled();
        iceRes = config.isIceResistanceEnabled();
        damageScaling = config.doDamageScaling();
        requireThermometer = config.thermometerRequired();
        graceLength = config.getGracePeriodLength();
        graceEnabled = config.isGracePeriodEnabled();
    }
}
