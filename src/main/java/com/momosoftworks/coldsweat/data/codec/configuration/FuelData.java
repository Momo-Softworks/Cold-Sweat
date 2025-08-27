package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;

import javax.annotation.Nullable;
import java.util.List;

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

    public static final Codec<FuelData> CODEC = createCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
            NegatableList.codec(ItemRequirement.CODEC).optionalFieldOf("item", new NegatableList<>()).forGetter(FuelData::item),
            FuelType.CODEC.fieldOf("type").forGetter(FuelData::fuelType),
            Codec.DOUBLE.fieldOf("fuel").forGetter(FuelData::fuel)
    ).apply(instance, FuelData::new)));

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
        NegatableList<Either<ITag<Item>, Item>> items = ConfigHelper.getItems((String) entry.get(0));
        if (items.isEmpty()) return null;

        double fuel = ((Number) entry.get(1)).doubleValue();
        NbtRequirement nbtRequirement = entry.size() > 2
                                        ? new NbtRequirement(NBTHelper.parseCompoundNbt((String) entry.get(3)))
                                        : new NbtRequirement(new CompoundNBT());
        ItemRequirement itemRequirement = new ItemRequirement(items, nbtRequirement);

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
