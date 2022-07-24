package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.util.TempHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.util.config.ConfigCache;
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
    public Function<Temperature, Temperature> calculate(Player player)
    {
        double worldTemp = TempHelper.getTemperature(player, Temperature.Type.WORLD).get();
        player.getPersistentData().putDouble("preHearthTemp", worldTemp);

        ConfigCache config = ConfigCache.getInstance();

        double min = config.minTemp;
        double max = config.maxTemp;
        double mid = (min + max) / 2;
        double hearthStrength = ColdSweatConfig.getInstance().getHearthEffect();

        int insulationStrength = this.<Integer>getArgument("strength");

        return temp -> new Temperature(CSMath.blend(temp.get(), CSMath.weightedAverage(temp.get(), mid, 1 - hearthStrength, 1.0), insulationStrength, 0, 10));
    }

    public String getID()
    {
        return "cold_sweat:hearth_insulation";
    }
}