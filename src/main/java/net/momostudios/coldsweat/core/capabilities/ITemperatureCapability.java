package net.momostudios.coldsweat.core.capabilities;

import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import java.util.List;

public interface ITemperatureCapability
{
    double get(PlayerTemp.Types type);
    void set(PlayerTemp.Types type, double value);
    void addModifier(PlayerTemp.Types type, TempModifier modifier);
    void removeModifier(PlayerTemp.Types type, TempModifier modifier);
    List<TempModifier> getModifiers(PlayerTemp.Types type);
}