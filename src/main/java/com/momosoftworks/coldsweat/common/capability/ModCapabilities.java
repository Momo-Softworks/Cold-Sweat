package com.momosoftworks.coldsweat.common.capability;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.capability.shearing.IShearableCap;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.EntityCapability;

public class ModCapabilities
{
    public static final EntityCapability<ITemperatureCap, Void> PLAYER_TEMPERATURE = EntityCapability.createVoid(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "player_temperature"), ITemperatureCap.class);
    public static final EntityCapability<ITemperatureCap, Void> ENTITY_TEMPERATURE = EntityCapability.createVoid(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "entity_temperature"), ITemperatureCap.class);
    public static final EntityCapability<IShearableCap, Void> SHEARABLE_FUR = EntityCapability.createVoid(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "shearable_fur"), IShearableCap.class);
}
