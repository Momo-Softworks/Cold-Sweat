package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemComponentsRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public class FuelData extends ConfigData implements RequirementHolder
{
    final NegatableList<ItemRequirement> item;
    final FuelType type;
    final Double fuel;

    public FuelData(NegatableList<ItemRequirement> item, FuelType type, Double fuel, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.type = type;
        this.fuel = fuel;
        this.item = item;
    }

    public FuelData(NegatableList<ItemRequirement> item, FuelType type, Double fuel)
    {   this(item, type, fuel, new NegatableList<>());
    }

    public static final Codec<FuelData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.codec(ItemRequirement.CODEC).optionalFieldOf("item", new NegatableList<>()).forGetter(FuelData::item),
            FuelType.CODEC.fieldOf("type").forGetter(FuelData::fuelType),
            Codec.DOUBLE.fieldOf("fuel").forGetter(FuelData::fuel),
            NegatableList.listCodec(Codec.STRING).optionalFieldOf("required_mods", new NegatableList<>()).forGetter(ConfigData::requiredMods)
    ).apply(instance, FuelData::new));

    public NegatableList<ItemRequirement> item()
    {   return item;
    }
    public FuelType fuelType()
    {   return type;
    }
    public Double fuel()
    {   return fuel;
    }

    @Override
    public boolean test(ItemStack stack)
    {   return item.test(req -> req.test(stack, true));
    }

    @Nullable
    public static FuelData fromToml(List<?> entry, FuelType fuelType)
    {
        if (entry.size() < 2)
        {   ColdSweat.LOGGER.error("Error parsing fuel config: not enough arguments");
            return null;
        }
        List<Either<TagKey<Item>, Item>> items = ConfigHelper.getItems((String) entry.get(0));
        if (items.isEmpty()) return null;

        double fuel = ((Number) entry.get(1)).doubleValue();
        ItemComponentsRequirement componentsRequirement = entry.size() > 2
                                                          ? ItemComponentsRequirement.parse((String) entry.get(2))
                                                          : new ItemComponentsRequirement();
        ItemRequirement itemRequirement = new ItemRequirement(items, componentsRequirement);

        return new FuelData(new NegatableList<>(itemRequirement), fuelType, fuel);
    }

    @Override
    public Codec<FuelData> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        FuelData that = (FuelData) obj;
        return super.equals(obj)
            && fuel.equals(that.fuel)
            && item.equals(that.item);
    }

    public enum FuelType implements StringRepresentable
    {
        BOILER("boiler"),
        ICEBOX("icebox"),
        HEARTH("hearth"),
        SOUL_LAMP("soulspring_lamp");

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
}
