package net.momostudios.coldsweat.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.util.world.WorldInfo;
import net.momostudios.coldsweat.temperature.PlayerTempHandler;
import net.momostudios.coldsweat.temperature.Temperature;

public class DepthTempModifier extends TempModifier
{
    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        double depth = WorldInfo.getGroundLevel(player.getPosition(), player.world);
        double worldTemp = PlayerTempHandler.getAmbient(player).get();

        //Brings the temperature closer to 0.8 (temperate) depending on the depth. Temp = 0.8 at a depth of 20
        return temp.get() + ((0.8 - worldTemp) / Math.max(20 / depth, 1));
    }
}
