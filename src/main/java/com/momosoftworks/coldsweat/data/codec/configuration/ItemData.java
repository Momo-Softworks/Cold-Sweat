package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;
import java.util.Optional;

public record ItemData(ItemRequirement data, Double value, Optional<EntityRequirement> entityRequirement,
                       Optional<List<String>> requiredMods) implements NbtSerializable, IForgeRegistryEntry<ItemData>
{
    public static final Codec<ItemData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemRequirement.CODEC.fieldOf("data").forGetter(ItemData::data),
            Codec.DOUBLE.fieldOf("value").forGetter(ItemData::value),
            EntityRequirement.getCodec().optionalFieldOf("entity").forGetter(ItemData::entityRequirement),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(ItemData::requiredMods)
    ).apply(instance, ItemData::new));

    @Override
    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        tag.put("data", data.serialize());
        tag.putDouble("value", value);
        entityRequirement.ifPresent(entityRequirement1 -> tag.put("entity_requirement", entityRequirement1.serialize()));
        ListTag mods = new ListTag();
        requiredMods.ifPresent(mods1 ->
        {   mods1.forEach(mod -> mods.add(StringTag.valueOf(mod)));
        });
        tag.put("required_mods", mods);
        return tag;
    }

    public static ItemData deserialize(CompoundTag nbt)
    {
        ItemRequirement items = ItemRequirement.deserialize(nbt.getCompound("data"));
        Double value = nbt.getDouble("value");
        Optional<EntityRequirement> entityRequirement = Optional.ofNullable(nbt.contains("entity_requirement")
                                                                            ? EntityRequirement.deserialize(nbt.getCompound("entity_requirement"))
                                                                            : null);
        Optional<List<String>> requiredMods = Optional.of(nbt.getList("required_mods", 8).stream().map(Tag::getAsString).toList());
        return new ItemData(items, value, entityRequirement, requiredMods);
    }

    @Override
    public ItemData setRegistryName(ResourceLocation name)
    {
        return null;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return null;
    }

    @Override
    public Class<ItemData> getRegistryType()
    {
        return ItemData.class;
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
