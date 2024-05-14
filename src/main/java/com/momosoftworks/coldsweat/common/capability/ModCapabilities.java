package com.momosoftworks.coldsweat.common.capability;

import com.momosoftworks.coldsweat.common.capability.insulation.IInsulatableCap;
import com.momosoftworks.coldsweat.common.capability.shearing.IShearableCap;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class ModCapabilities
{
    @CapabilityInject(ITemperatureCap.class)
    public static Capability<ITemperatureCap> PLAYER_TEMPERATURE = null;

    @CapabilityInject(ITemperatureCap.class)
    public static final Capability<ITemperatureCap> ENTITY_TEMPERATURE = null;

    @CapabilityInject(IInsulatableCap.class)
    public static final Capability<IInsulatableCap> ITEM_INSULATION = null;

    @CapabilityInject(IShearableCap.class)
    public static final Capability<IShearableCap> SHEARABLE_FUR = null;
}
