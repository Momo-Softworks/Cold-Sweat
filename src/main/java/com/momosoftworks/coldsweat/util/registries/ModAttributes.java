package com.momosoftworks.coldsweat.util.registries;

import com.momosoftworks.coldsweat.core.init.AttributeInit;
import net.minecraft.entity.ai.attributes.Attribute;

public class ModAttributes
{
    // Changes the rate at which the player's body temperature changes
    public static final Attribute COLD_DAMPENING = AttributeInit.COLD_DAMPENING.get();
    public static final Attribute HEAT_DAMPENING = AttributeInit.HEAT_DAMPENING.get();

    // Changes the min/max habitable temperatures
    public static final Attribute BURNING_POINT_OFFSET = AttributeInit.BURNING_POINT_OFFSET.get();
    public static final Attribute FREEZING_POINT_OFFSET = AttributeInit.FREEZING_POINT_OFFSET.get();

    // Decreases damage dealt by overheating/freezing
    public static final Attribute COLD_RESISTANCE = AttributeInit.COLD_RESISTANCE.get();
    public static final Attribute HEAT_RESISTANCE = AttributeInit.HEAT_RESISTANCE.get();

    // Used to change core/base body temperature
    public static final Attribute CORE_BODY_TEMPERATURE_OFFSET = AttributeInit.CORE_BODY_TEMPERATURE_OFFSET.get();
    public static final Attribute BASE_BODY_TEMPERATURE_OFFSET = AttributeInit.BASE_BODY_TEMPERATURE_OFFSET.get();

    // Used to change the entity's perceived world temperature
    public static final Attribute WORLD_TEMPERATURE_OFFSET = AttributeInit.WORLD_TEMPERATURE_OFFSET.get();

    // Read-only attributes that hold the entity's temperature values
    public static final Attribute BURNING_POINT = AttributeInit.BURNING_POINT.get();
    public static final Attribute FREEZING_POINT = AttributeInit.FREEZING_POINT.get();
    public static final Attribute CORE_BODY_TEMPERATURE = AttributeInit.CORE_BODY_TEMPERATURE.get();
    public static final Attribute BASE_BODY_TEMPERATURE = AttributeInit.BASE_BODY_TEMPERATURE.get();
    public static final Attribute WORLD_TEMPERATURE = AttributeInit.WORLD_TEMPERATURE.get();
}
