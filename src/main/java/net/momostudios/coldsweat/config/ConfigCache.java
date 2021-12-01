package net.momostudios.coldsweat.config;

public class ConfigCache
{

    public int difficulty;
    public double maxTemp;
    public double minTemp;
    public double rate;
    public boolean fireRes;
    public boolean iceRes;
    public boolean damageScaling;
    public boolean showAmbient;

    public WorldTemperatureConfig worldOptionsReference = WorldTemperatureConfig.INSTANCE;
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
        difficulty = config.difficulty();
        maxTemp = config.maxHabitable();
        minTemp = config.minHabitable();
        rate = config.rateMultiplier();
        fireRes = config.fireResistanceEffect();
        iceRes = config.iceResistanceEffect();
        damageScaling = config.damageScaling();
        showAmbient = config.showAmbient();
    }

    public void setDamageScaling(boolean damageScaling)
    {
        this.damageScaling = damageScaling;
    }

    public void setDifficulty(int difficulty)
    {
        this.difficulty = difficulty;
    }

    public void setFireResistanceEffect(boolean fireRes)
    {
        this.fireRes = fireRes;
    }

    public void setIceResistanceEffect(boolean iceRes)
    {
        this.iceRes = iceRes;
    }

    public void setMaxHabitable(double maxTemp)
    {
        this.maxTemp = maxTemp;
    }

    public void setMinHabitable(double minTemp)
    {
        this.minTemp = minTemp;
    }

    public void setRateMultiplier(double rate)
    {
        this.rate = rate;
    }

    public void setShowAmbient(boolean showAmbient)
    {
        this.showAmbient = showAmbient;
    }
}
