package dev.momosoftworks.coldsweat.common.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ModCapabilities
{
    public static final Capability<ITemperatureCap> PLAYER_TEMPERATURE = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<ITemperatureCap> ENTITY_TEMPERATURE = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IInsulatableCap> ITEM_INSULATION = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IShearableCap> SHEARABLE_FUR = CapabilityManager.get(new CapabilityToken<>() {});
}
