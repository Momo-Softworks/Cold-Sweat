package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FoodData implements NbtSerializable
{
    public ItemRequirement data;
    public Double value;
    public Optional<Integer> duration;
    public Optional<EntityRequirement> entityRequirement;
    public Optional<List<String>> requiredMods;
    
    public static final Codec<FoodData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemRequirement.CODEC.fieldOf("data").forGetter(data -> data.data),
            Codec.DOUBLE.fieldOf("value").forGetter(data -> data.value),
            Codec.INT.optionalFieldOf("duration").forGetter(data -> data.duration),
            EntityRequirement.getCodec().optionalFieldOf("entity_requirement").forGetter(data -> data.entityRequirement),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(data -> data.requiredMods)
    ).apply(instance, FoodData::new));
    
    public FoodData(ItemRequirement data, Double value, Optional<Integer> duration, Optional<EntityRequirement> entityRequirement, Optional<List<String>> requiredMods)
    {
        this.data = data;
        this.value = value;
        this.duration = duration;
        this.entityRequirement = entityRequirement;
        this.requiredMods = requiredMods;
    }

    @Override
    public CompoundNBT serialize()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.put("data", data.serialize());
        tag.putDouble("value", value);
        duration.ifPresent(aDouble -> tag.putDouble("duration", aDouble));
        entityRequirement.ifPresent(entityRequirement1 -> tag.put("entity_requirement", entityRequirement1.serialize()));
        ListNBT mods = new ListNBT();
        requiredMods.ifPresent(mods1 ->
        {   mods1.forEach(mod -> mods.add(StringNBT.valueOf(mod)));
        });
        tag.put("required_mods", mods);
        return tag;
    }

    public static FoodData deserialize(CompoundNBT nbt)
    {
        ItemRequirement items = ItemRequirement.deserialize(nbt.getCompound("data"));
        Double value = nbt.getDouble("value");
        Optional<Integer> duration = Optional.ofNullable(nbt.contains("duration") ? nbt.getInt("duration") : null);
        Optional<EntityRequirement> entityRequirement = java.util.Optional.ofNullable(nbt.contains("entity_requirement")
                                                                            ? EntityRequirement.deserialize(nbt.getCompound("entity_requirement"))
                                                                            : null);
        Optional<List<String>> requiredMods = Optional.of(nbt.getList("required_mods", 8)).map(mods ->
        {   List<String> mods1 = new ArrayList<>();
            for (int i = 0; i < mods.size(); i++)
            {   mods1.add(mods.getString(i));
            }
            return mods1;
        });
        return new FoodData(items, value, duration, entityRequirement, requiredMods);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ItemData{data=").append(data).append(", value=").append(value);
        duration.ifPresent(aDouble -> builder.append(", duration=").append(aDouble));
        entityRequirement.ifPresent(entityRequirement1 -> builder.append(", entityRequirement=").append(entityRequirement1));
        requiredMods.ifPresent(mods -> builder.append(", requiredMods=").append(mods));
        builder.append("}");

        return builder.toString();
    }
}
