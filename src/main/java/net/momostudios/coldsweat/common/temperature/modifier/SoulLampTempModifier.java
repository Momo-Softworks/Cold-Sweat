package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.DimensionType;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.util.ModItems;

public class SoulLampTempModifier extends TempModifier
{

    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        if ((player.getHeldItemMainhand().getItem() == ModItems.SOULFIRE_LAMP || player.getHeldItemOffhand().getItem() == ModItems.SOULFIRE_LAMP) &&
        player.world.getDimensionKey().getLocation().getPath().equals("the_nether") && temp.get() > ColdSweatConfig.getInstance().maxHabitable())
        {
            return temp.get() * 0.8;
        }
        return temp.get();
    }

    @Override
    public String getID() {
        return "cold_sweat:soulfire_lamp";
    }
}
