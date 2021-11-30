package net.momostudios.coldsweat.core.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import java.util.ArrayList;
import java.util.List;

public class PlayerTempCapability
{
    public static Capability<PlayerTempCapability> TEMPERATURE;

    @CapabilityInject(PlayerTempCapability.class)
    private static void onTempInit(Capability<PlayerTempCapability> capability)
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

    public boolean hasModifier(PlayerTemp.Types type, Class<? extends TempModifier> mod)
    {
        switch (type)
        {
            case AMBIENT:  { return this.ambientModifiers.stream().anyMatch(mod::isInstance); }
            case BODY:     { return this.bodyModifiers.stream().anyMatch(mod::isInstance); }
            case BASE:     { return this.baseModifiers.stream().anyMatch(mod::isInstance); }
            case RATE:     { return this.rateModifiers.stream().anyMatch(mod::isInstance); }
            default: throw new IllegalArgumentException("Illegal type for PlayerTempCapability.hasModifier(): " + type);
        }
    }


    /**
     * Do NOT use this! <br>
     */
    public void clearModifiers(PlayerTemp.Types type)
    {
        switch (type)
        {
            case AMBIENT:  { this.ambientModifiers.clear(); break; }
            case BODY:     { this.bodyModifiers.clear(); break; }
            case BASE:     { this.baseModifiers.clear(); break; }
            case RATE:     { this.rateModifiers.clear(); break; }
            default: throw new IllegalArgumentException("Illegal type for PlayerTempCapability.clearModifiers(): " + type);
        }
    }
}
