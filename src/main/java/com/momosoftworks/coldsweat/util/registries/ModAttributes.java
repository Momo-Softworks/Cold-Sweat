package com.momosoftworks.coldsweat.util.registries;

import com.momosoftworks.coldsweat.core.init.AttributeInit;
import net.minecraft.entity.ai.attributes.Attribute;

public class ModAttributes
{
    public static final Attribute WORLD_TEMPERATURE = AttributeInit.WORLD_TEMPERATURE.get();
    public static final Attribute BASE_BODY_TEMPERATURE = AttributeInit.BASE_BODY_TEMPERATURE.get();

    public static final Attribute BURNING_POINT   = AttributeInit.BURNING_POINT.get();
    public static final Attribute FREEZING_POINT  = AttributeInit.FREEZING_POINT.get();
    public static final Attribute HEAT_RESISTANCE = AttributeInit.HEAT_RESISTANCE.get();
    public static final Attribute COLD_RESISTANCE = AttributeInit.COLD_RESISTANCE.get();
    public static final Attribute HEAT_DAMPENING  = AttributeInit.HEAT_DAMPENING.get();
    public static final Attribute COLD_DAMPENING  = AttributeInit.COLD_DAMPENING.get();
}
