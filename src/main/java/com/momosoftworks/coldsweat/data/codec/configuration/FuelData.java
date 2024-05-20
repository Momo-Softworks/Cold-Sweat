package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.StringRepresentable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record FuelData(FuelType type, Double fuel,
                       ItemRequirement data, Optional<List<String>> requiredMods) implements NbtSerializable
{
    public static final Codec<FuelData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FuelType.CODEC.fieldOf("type").forGetter(FuelData::type),
            Codec.DOUBLE.fieldOf("fuel").forGetter(FuelData::fuel),
            ItemRequirement.CODEC.fieldOf("data").forGetter(FuelData::data),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(FuelData::requiredMods)
    ).apply(instance, FuelData::new));

    @Override
    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type.getSerializedName());
        tag.putDouble("fuel", fuel);
        tag.put("data", data.serialize());
        ListTag mods = new ListTag();
        requiredMods.ifPresent(mods1 ->
        {   mods1.forEach(mod -> mods.add(StringTag.valueOf(mod)));
        });
        tag.put("required_mods", mods);
        return tag;
    }

    public static FuelData deserialize(CompoundTag nbt)
    {
        FuelType type = FuelType.byName(nbt.getString("type"));
        Double fuel = nbt.getDouble("fuel");
        ItemRequirement requirement = ItemRequirement.deserialize(nbt.getCompound("data"));
        Optional<List<String>> requiredMods = Optional.of(nbt.getList("required_mods", 8)).map(mods ->
        {   List<String> mods1 = new ArrayList<>();
            for (int i = 0; i < mods.size(); i++)
            {   mods1.add(mods.getString(i));
            }
            return mods1;
        });
        return new FuelData(type, fuel, requirement, requiredMods);
    }

    public enum FuelType implements StringRepresentable
    {
        BOILER("boiler"),
        ICEBOX("icebox"),
        HEARTH("hearth"),
        SOUL_LAMP("soul_lamp");

        public static Codec<FuelType> CODEC = StringRepresentable.fromEnum(FuelType::values);

        private final String name;

        FuelType(String name)
        {   this.name = name;
        }

        @Override
        public String getSerializedName()
        {   return name;
        }

        public static FuelType byName(String name)
        {   for (FuelType type : values())
            {   if (type.name.equals(name)) return type;
            }
            return null;
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("FuelData{type=").append(type).append(", fuel=").append(fuel).append(", data=").append(data);
        requiredMods.ifPresent(mods -> builder.append(", requiredMods=").append(mods));
        builder.append("}");

        return builder.toString();
    }
}
