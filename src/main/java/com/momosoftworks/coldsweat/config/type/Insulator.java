package com.momosoftworks.coldsweat.config.type;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.data.codec.util.CommonStreamCodecs;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public record Insulator(Insulation insulation, Insulation.Slot slot, ItemRequirement data,
                        EntityRequirement predicate, AttributeModifierMap attributes,
                        Map<ResourceLocation, Double> immuneTempModifiers) implements NbtSerializable
{
    public static final Codec<Insulator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Insulation.getCodec().fieldOf("insulation").forGetter(Insulator::insulation),
            Insulation.Slot.CODEC.fieldOf("slot").forGetter(Insulator::slot),
            ItemRequirement.CODEC.fieldOf("data").forGetter(Insulator::data),
            EntityRequirement.getCodec().optionalFieldOf("predicate", EntityRequirement.NONE).forGetter(Insulator::predicate),
            AttributeModifierMap.CODEC.optionalFieldOf("attributes", new AttributeModifierMap()).forGetter(Insulator::attributes),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE).optionalFieldOf("immune_temp_modifiers", new HashMap<>()).forGetter(Insulator::immuneTempModifiers)
    ).apply(instance, Insulator::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Insulator> STREAM_CODEC = StreamCodec.of(
    (buf, insulator) ->
    {
        Insulation.getNetworkCodec().encode(buf, insulator.insulation());
        buf.writeEnum(insulator.slot());
        ItemRequirement.STREAM_CODEC.encode(buf, insulator.data());
        buf.writeNbt(EntityRequirement.getCodec().encode(insulator.predicate(), NbtOps.INSTANCE, new CompoundTag()).result().orElse(new CompoundTag()));
        AttributeModifierMap.STREAM_CODEC.encode(buf, insulator.attributes());
        buf.writeMap(insulator.immuneTempModifiers(), ResourceLocation.STREAM_CODEC, CommonStreamCodecs.DOUBLE);
    },
    (buf) ->
    {
        Insulation insulation = Insulation.getNetworkCodec().decode(buf);
        Insulation.Slot slot = buf.readEnum(Insulation.Slot.class);
        ItemRequirement data = ItemRequirement.STREAM_CODEC.decode(buf);
        EntityRequirement predicate = EntityRequirement.getCodec().decode(NbtOps.INSTANCE, buf.readNbt()).result().orElse(Pair.of(EntityRequirement.NONE, new CompoundTag())).getFirst();
        AttributeModifierMap attributes = AttributeModifierMap.STREAM_CODEC.decode(buf);
        Map<ResourceLocation, Double> immuneTempModifiers = buf.readMap(ResourceLocation.STREAM_CODEC, CommonStreamCodecs.DOUBLE);
        return new Insulator(insulation, slot, data, predicate, attributes, immuneTempModifiers);
    });

    public boolean test(Entity entity, ItemStack stack)
    {   return predicate.test(entity) && data.test(stack, true);
    }

    @Override
    public CompoundTag serialize()
    {   return (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElse(new CompoundTag());
    }

    public static Insulator deserialize(CompoundTag tag)
    {   return CODEC.decode(NbtOps.INSTANCE, tag).result().orElseThrow(() -> new IllegalArgumentException("Could not deserialize Insulator")).getFirst();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Insulator insulator = (Insulator) obj;

        return insulation.equals(insulator.insulation)
            && data.equals(insulator.data)
            && predicate.equals(insulator.predicate)
            && attributes.equals(insulator.attributes);
    }
}
