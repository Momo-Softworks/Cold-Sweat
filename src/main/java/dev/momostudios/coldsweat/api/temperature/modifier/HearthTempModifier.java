package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

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
    public Function<Double, Double> calculate(Player player)
    {
        double worldTemp = Temperature.get(player, Temperature.Type.WORLD);
        player.getPersistentData().putDouble("preHearthTemp", worldTemp);

        ConfigSettings config = ConfigSettings.getInstance();

        double min = config.minTemp;
        double max = config.maxTemp;
        double mid = (min + max) / 2;
        double hearthStrength = ColdSweatConfig.getInstance().getHearthEffect();

        int insulationStrength = this.<Integer>getArgument("strength");

        return temp -> CSMath.blend(temp, CSMath.weightedAverage(temp, mid, 1 - hearthStrength, 1.0), insulationStrength, 0, 10);
    }

    public String getID()
    {
        return "cold_sweat:hearth_insulation";
    }
}