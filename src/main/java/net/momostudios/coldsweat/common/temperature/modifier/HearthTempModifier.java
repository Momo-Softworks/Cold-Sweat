package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.util.registrylists.ModEffects;

public class HearthTempModifier extends TempModifier
{
    @Override
    public double getValue(Temperature temp, PlayerEntity player)
    {
        ColdSweatConfig config = ColdSweatConfig.getInstance();

        double min = config.getMinTempHabitable();
        double max = config.getMaxTempHabitable();
        double mid = (min + max) / 2;

        int hearthEffect = player.isPotionActive(ModEffects.INSULATION) ?
                (player.getActivePotionEffect(ModEffects.INSULATION).getAmplifier() + 1) * 2 : 1;
        return mid + ((temp.get() - mid) / hearthEffect);
    }

    public String getID()
    {
        return "cold_sweat:hearth_insulation";
    }
}