package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AttributeInit
{
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, ColdSweat.MOD_ID);

    /*
     Changes the rate at which the player's body temperature changes
     */
    public static final RegistryObject<Attribute> COLD_DAMPENING = ATTRIBUTES.register("cold_dampening", () -> new RangedAttribute("attribute.cold_dampening", 0.0, -1024d, 1.0).setSyncable(true));
    public static final RegistryObject<Attribute> HEAT_DAMPENING = ATTRIBUTES.register("heat_dampening", () -> new RangedAttribute("attribute.heat_dampening", 0.0, -1024d, 1.0).setSyncable(true));

    /*
     Changes the min/max habitable temperatures
     */
    public static final RegistryObject<Attribute> BURNING_POINT_OFFSET = ATTRIBUTES.register("burning_point_offset", () -> new RangedAttribute("attribute.burning_point_offset", 0.0, -1024d, 1024d).setSyncable(true));
    public static final RegistryObject<Attribute> FREEZING_POINT_OFFSET = ATTRIBUTES.register("freezing_point_offset", () -> new RangedAttribute("attribute.freezing_point_offset", 0.0, -1024d, 1024d).setSyncable(true));

    /*
     Decreases damage dealt by overheating/freezing
     */
    public static final RegistryObject<Attribute> COLD_RESISTANCE = ATTRIBUTES.register("cold_resistance", () -> new RangedAttribute("attribute.cold_resistance", 0.0, 0.0, 1.0).setSyncable(true));
    public static final RegistryObject<Attribute> HEAT_RESISTANCE = ATTRIBUTES.register("heat_resistance", () -> new RangedAttribute("attribute.heat_resistance", 0.0, 0.0, 1.0).setSyncable(true));

    /*
     Used to change body temperature
     */
    // CORE_BODY_TEMPERATURE_OFFSET is applied every tick!
    public static final RegistryObject<Attribute> CORE_BODY_TEMPERATURE_OFFSET = ATTRIBUTES.register("core_temperature_offset", () -> new RangedAttribute("attribute.core_temperature_offset", 0.0, -150d, 150d).setSyncable(true));
    public static final RegistryObject<Attribute> BASE_BODY_TEMPERATURE_OFFSET = ATTRIBUTES.register("base_temperature_offset", () -> new RangedAttribute("attribute.base_temperature_offset", 0.0, -150d, 150d).setSyncable(true));

    /*
     World temperature
     */
    public static final RegistryObject<Attribute> WORLD_TEMPERATURE_OFFSET = ATTRIBUTES.register("world_temperature_offset", () -> new RangedAttribute("attribute.world_temperature_offset", 0.0, -1024, 1024).setSyncable(true));

    /*
     Read-only attributes (hold the entity's temperature values)
     */
    public static final RegistryObject<Attribute> WORLD_TEMPERATURE = ATTRIBUTES.register("world_temperature", () -> new RangedAttribute("attribute.world_temperature", 0.0, -1024d, 1024d).setSyncable(true));
    public static final RegistryObject<Attribute> BURNING_POINT = ATTRIBUTES.register("burning_point", () -> new RangedAttribute("attribute.burning_point", 0.0, -1024d, 1024d).setSyncable(true));
    public static final RegistryObject<Attribute> FREEZING_POINT = ATTRIBUTES.register("freezing_point", () -> new RangedAttribute("attribute.freezing_point", 0.0, -1024d, 1024d).setSyncable(true));
    public static final RegistryObject<Attribute> CORE_BODY_TEMPERATURE = ATTRIBUTES.register("core_temperature", () -> new RangedAttribute("attribute.core_temperature", 0.0, -150d, 150d).setSyncable(true));
    public static final RegistryObject<Attribute> BASE_BODY_TEMPERATURE = ATTRIBUTES.register("base_temperature", () -> new RangedAttribute("attribute.base_temperature", 0.0, -150d, 150d).setSyncable(true));
}
