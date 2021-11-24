package net.momostudios.coldsweat.core.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.energy.IEnergyStorage;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import java.util.ArrayList;
import java.util.List;

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
    List<TempModifier> ambientModifiers = new ArrayList<>();
    List<TempModifier> bodyModifiers = new ArrayList<>();
    List<TempModifier> baseModifiers = new ArrayList<>();
    List<TempModifier> rateModifiers = new ArrayList<>();

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

    @Override
    public void addModifier(PlayerTemp.Types type, TempModifier modifier)
    {
        switch (type)
        {
            case AMBIENT:  { this.ambientModifiers.add(modifier); break; }
            case BODY:     { this.bodyModifiers.add(modifier); break; }
            case BASE:     { this.baseModifiers.add(modifier); break; }
            case RATE:     { this.rateModifiers.add(modifier); break; }
            default: throw new IllegalArgumentException("Illegal type for PlayerTempCapability.addModifier(): " + type);
        }
    }

    @Override
    public void removeModifier(PlayerTemp.Types type, TempModifier modifier)
    {
        switch (type)
        {
            case AMBIENT:  { this.ambientModifiers.remove(modifier); break; }
            case BODY:     { this.bodyModifiers.remove(modifier); break; }
            case BASE:     { this.baseModifiers.remove(modifier); break; }
            case RATE:     { this.rateModifiers.remove(modifier); break; }
            default: throw new IllegalArgumentException("Illegal type for PlayerTempCapability.removeModifier(): " + type);
        }
    }

    @Override
    public List<TempModifier> getModifiers(PlayerTemp.Types type)
    {
        switch (type)
        {
            case AMBIENT:  { return ambientModifiers; }
            case BODY:     { return bodyModifiers; }
            case BASE:     { return baseModifiers; }
            case RATE:     { return rateModifiers; }
            default: throw new IllegalArgumentException("Illegal type for PlayerTempCapability.getModifiers(): " + type);
        }
    }
}
