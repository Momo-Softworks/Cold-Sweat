package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;
import java.util.Optional;

public record ItemCarryTempData(ItemRequirement data, List<Either<IntegerBounds, EquipmentSlot>> slots, double temp,
                                Optional<Temperature.Trait> trait,
                                Optional<IntegerBounds> bounds,
                                Optional<EntityRequirement> entityRequirement,
                                Optional<List<String>> requiredMods)
{
    public static final Codec<ItemCarryTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemRequirement.CODEC.fieldOf("data").forGetter(ItemCarryTempData::data),
            Codec.either(IntegerBounds.CODEC, Codec.STRING.xmap(EquipmentSlot::byName, EquipmentSlot::getName))
                 .listOf().fieldOf("slots").forGetter(ItemCarryTempData::slots),
            Codec.DOUBLE.fieldOf("temperature").forGetter(ItemCarryTempData::temp),
            Temperature.Trait.CODEC.optionalFieldOf("trait").forGetter(ItemCarryTempData::trait),
            IntegerBounds.CODEC.optionalFieldOf("bounds").forGetter(ItemCarryTempData::bounds),
            EntityRequirement.getCodec().optionalFieldOf("entity").forGetter(ItemCarryTempData::entityRequirement),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(ItemCarryTempData::requiredMods)
    ).apply(instance, ItemCarryTempData::new));
}
