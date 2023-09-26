package com.momosoftworks.coldsweat.common.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class ModCapabilities
{
    @CapabilityInject(ITemperatureCap.class)
    public static Capability<ITemperatureCap> PLAYER_TEMPERATURE = null;

    @CapabilityInject(ITemperatureCap.class)
    public static final Capability<ITemperatureCap> ENTITY_TEMPERATURE = null;

    @CapabilityInject(ItemInsulationCap.class)
    public static final Capability<IInsulatableCap> ITEM_INSULATION = null;

    @CapabilityInject(IShearableCap.class)
    public static final Capability<IShearableCap> SHEARABLE_FUR = null;
}
