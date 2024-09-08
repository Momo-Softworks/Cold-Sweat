package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemData implements NbtSerializable
{
    public final ItemRequirement data;
    public final Double value;
    public final Optional<EntityRequirement> entityRequirement;
    public final Optional<List<String>> requiredMods;

    public ItemData(ItemRequirement data, Double value, Optional<EntityRequirement> entityRequirement, Optional<List<String>> requiredMods)
    {
        this.data = data;
        this.value = value;
        this.entityRequirement = entityRequirement;
        this.requiredMods = requiredMods;
    }
    public static final Codec<ItemData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemRequirement.CODEC.fieldOf("data").forGetter(data -> data.data),
            Codec.DOUBLE.fieldOf("value").forGetter(data -> data.value),
            EntityRequirement.getCodec().optionalFieldOf("entity_requirement").forGetter(data -> data.entityRequirement),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(data -> data.requiredMods)
    ).apply(instance, ItemData::new));

    @Override
    public CompoundNBT serialize()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.put("data", data.serialize());
        tag.putDouble("value", value);
        entityRequirement.ifPresent(entityRequirement1 -> tag.put("entity_requirement", entityRequirement1.serialize()));
        ListNBT mods = new ListNBT();
        requiredMods.ifPresent(mods1 ->
        {   mods1.forEach(mod -> mods.add(StringNBT.valueOf(mod)));
        });
        tag.put("required_mods", mods);
        return tag;
    }

    public static ItemData deserialize(CompoundNBT nbt)
    {
        ItemRequirement items = ItemRequirement.deserialize(nbt.getCompound("data"));
        Double value = nbt.getDouble("value");
        Optional<EntityRequirement> entityRequirement = Optional.ofNullable(nbt.contains("entity_requirement")
                                                                            ? EntityRequirement.deserialize(nbt.getCompound("entity_requirement"))
                                                                            : null);
        Optional<List<String>> requiredMods = Optional.of(nbt.getList("required_mods", 8).stream().map(INBT::getAsString).collect(Collectors.toList()));
        return new ItemData(items, value, entityRequirement, requiredMods);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ItemData{data=").append(data).append(", value=").append(value);
        entityRequirement.ifPresent(entityRequirement1 -> builder.append(", entityRequirement=").append(entityRequirement1));
        requiredMods.ifPresent(mods -> builder.append(", requiredMods=").append(mods));
        builder.append("}");

        return builder.toString();
    }
}
