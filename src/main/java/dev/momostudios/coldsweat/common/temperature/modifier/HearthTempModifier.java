package dev.momostudios.coldsweat.common.temperature.modifier;

import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.config.ConfigCache;
import net.minecraft.world.entity.player.Player;

public class HearthTempModifier extends TempModifier
{
    public HearthTempModifier()
    {
        addArgument("strength", 0);
    }

    public HearthTempModifier(int strength)
    {
        addArgument("strength", strength);
    }

    @Override
    public double getResult(Temperature temp, Player player)
    {
        player.getPersistentData().putDouble("preHearthTemp", temp.get());

        ConfigCache config = ConfigCache.getInstance();

        double min = config.minTemp;
        double max = config.maxTemp;
        double mid = (min + max) / 2;

        int hearthEffect = (int) this.getArgument("strength");
        return CSMath.blend(temp.get(), CSMath.weightedAverage(temp.get(), mid, 1 - ColdSweatConfig.getInstance().getHearthEffect(), 1.0), hearthEffect, 0, 10);
    }

    public String getID()
    {
        return "cold_sweat:hearth_insulation";
    }
}