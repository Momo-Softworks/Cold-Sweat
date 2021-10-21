package net.momostudios.coldsweat.core.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.energy.IEnergyStorage;
import net.momostudios.coldsweat.core.util.PlayerTemp;

public class PlayerTempCapability implements ITemperatureCapability
{
    public static Capability<ITemperatureCapability> TEMPERATURE;

    @CapabilityInject(ITemperatureCapability.class)
    private static void onTempInit(Capability<ITemperatureCapability> capability)
    {
        TEMPERATURE = capability;
    }

    double ambiTemp;
    double bodyTemp;
    double baseTemp;
    double compTemp;

    @Override
    public double get(PlayerTemp.Types type)
    {
        switch (type)
        {
            case AMBIENT:  return ambiTemp;
            case BODY:     return bodyTemp;
            case BASE:     return baseTemp;
            case COMPOSITE:return compTemp;
            default: throw new IllegalArgumentException("Illegal type for PlayerTempCapability.getValue(): " + type);
        }
    }

    @Override
    public void set(PlayerTemp.Types type, double value)
    {
        switch (type)
        {
            case AMBIENT:  { this.ambiTemp = value; break; }
            case BODY:     { this.bodyTemp = value; break; }
            case BASE:     { this.baseTemp = value; break; }
            case COMPOSITE:{ this.compTemp = value; break; }
            default: throw new IllegalArgumentException("Illegal type for PlayerTempCapability.setValue(): " + type);
        }
    }
}
