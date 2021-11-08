package net.momostudios.coldsweat.core.capabilities;

import net.momostudios.coldsweat.core.util.PlayerTemp;

public interface ITemperatureCapability
{
    float get(PlayerTemp.Types type);
    void set(PlayerTemp.Types type, float value);
}