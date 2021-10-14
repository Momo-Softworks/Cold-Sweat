package net.momostudios.coldsweat.core.capabilities;

import net.momostudios.coldsweat.core.util.PlayerTemp;

public interface ITemperatureCapability
{
    double get(PlayerTemp.Types type);
    void set(PlayerTemp.Types type, double value);
}