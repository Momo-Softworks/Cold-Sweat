package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class AttributeInit
{
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, ColdSweat.MOD_ID);

    public static final RegistryObject<Attribute> WORLD_TEMPERATURE = ATTRIBUTES.register("world_temperature", () -> new RangedAttribute("attribute.world_temperature", Double.NaN, Double.NaN, Double.POSITIVE_INFINITY).setSyncable(true));
    public static final RegistryObject<Attribute> BASE_BODY_TEMPERATURE = ATTRIBUTES.register("base_temperature", () -> new RangedAttribute("attribute.base_temperature", Double.NaN, Double.NaN, Double.POSITIVE_INFINITY).setSyncable(true));

    public static final RegistryObject<Attribute> BURNING_POINT = ATTRIBUTES.register("burning_point", () -> new RangedAttribute("attribute.burning_point", 0.0, Double.NaN, Double.POSITIVE_INFINITY).setSyncable(true));
    public static final RegistryObject<Attribute> FREEZING_POINT = ATTRIBUTES.register("freezing_point", () -> new RangedAttribute("attribute.freezing_point", 0.0, Double.NaN, Double.POSITIVE_INFINITY).setSyncable(true));
    public static final RegistryObject<Attribute> HEAT_RESISTANCE = ATTRIBUTES.register("heat_resistance", () -> new RangedAttribute("attribute.heat_resistance", 0.0, 0d, 1d).setSyncable(true));
    public static final RegistryObject<Attribute> COLD_RESISTANCE = ATTRIBUTES.register("cold_resistance", () -> new RangedAttribute("attribute.cold_resistance", 0.0, 0d, 1d).setSyncable(true));
    public static final RegistryObject<Attribute> HEAT_DAMPENING = ATTRIBUTES.register("heat_dampening", () -> new RangedAttribute("attribute.heat_dampening", 0.0, Double.NEGATIVE_INFINITY, 1d).setSyncable(true));
    public static final RegistryObject<Attribute> COLD_DAMPENING = ATTRIBUTES.register("cold_dampening", () -> new RangedAttribute("attribute.cold_dampening", 0.0, Double.NEGATIVE_INFINITY, 1d).setSyncable(true));

    // Read-only attributes for getting temperature values
    public static final RegistryObject<Attribute> WORLD_TEMP_IMMUTABLE = ATTRIBUTES.register("world_temp_immutable", () -> new RangedAttribute("attribute.world_temp_immutable", Double.NaN, Double.NaN, Double.POSITIVE_INFINITY).setSyncable(true));
    public static final RegistryObject<Attribute> BODY_TEMP_IMMUTABLE = ATTRIBUTES.register("body_temp_immutable", () -> new RangedAttribute("attribute.body_temp_immutable", Double.NaN, Double.NaN, Double.POSITIVE_INFINITY).setSyncable(true));
}
