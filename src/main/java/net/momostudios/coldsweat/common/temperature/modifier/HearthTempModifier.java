package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.util.registrylists.ModEffects;

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
    public double getResult(Temperature temp, PlayerEntity player)
    {
        ColdSweatConfig config = ColdSweatConfig.getInstance();

        double min = config.getMinTempHabitable();
        double max = config.getMaxTempHabitable();
        double mid = (min + max) / 2;

        int hearthEffect = (int) this.getArgument("strength");
        return temp.get() + ((mid - temp.get()) / 10) * hearthEffect;
    }

    public String getID()
    {
        return "cold_sweat:hearth_insulation";
    }
}