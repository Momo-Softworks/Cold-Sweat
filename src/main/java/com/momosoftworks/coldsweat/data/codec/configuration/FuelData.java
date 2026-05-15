package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemComponentsRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.data.codec.util.ValueGetter;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.EnumHelper;
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
    final ValueGetter<Integer> fuel;

    public FuelData(NegatableList<ItemRequirement> item, FuelType type, ValueGetter<Integer> fuel, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.type = type;
        this.fuel = fuel;
        this.item = item;
    }

    public FuelData(NegatableList<ItemRequirement> item, FuelType type, ValueGetter<Integer> fuel)
    {   this(item, type, fuel, new NegatableList<>());
    }

    public static final Codec<FuelData> CODEC = createCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
            NegatableList.codec(ItemRequirement.CODEC).optionalFieldOf("item", new NegatableList<>()).forGetter(FuelData::item),
            FuelType.CODEC.fieldOf("type").forGetter(FuelData::fuelType),
            ValueGetter.fieldCodec("fuel", Codec.INT, 0).forGetter(FuelData::fuel)
    ).apply(instance, FuelData::new)));

    public NegatableList<ItemRequirement> item()
    {   return item;
    }
    public FuelType fuelType()
    {   return type;
    }
    public ValueGetter<Integer> fuel()
    {   return fuel;
    }
    public int fuel(ItemStack stack)
    {   return fuel.get(Map.of("item", stack));
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
        NegatableList<Either<TagKey<Item>, Item>> items = ConfigHelper.getItems((String) entry.get(0));
        if (items.isEmpty()) return null;

        ValueGetter<Integer> fuel = ValueGetter.parse(() -> entry.get(1), Codec.INT, 0);
        ItemComponentsRequirement componentsRequirement = entry.size() > 2
                                                          ? ItemComponentsRequirement.parse((String) entry.get(2))
                                                          : new ItemComponentsRequirement();
        ItemRequirement itemRequirement = new ItemRequirement(items, componentsRequirement);

        FuelData result = new FuelData(new NegatableList<>(itemRequirement), fuelType, fuel);
        result.setConfigType(Type.TOML);
        return result;
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

        public static Codec<FuelType> CODEC = ExtraCodecs.enumIgnoreCase(values());

        private final String name;

        FuelType(String name)
        {   this.name = name;
        }

        @Override
        public String getSerializedName()
        {   return name;
        }

        public static FuelType byName(String name)
        {   return EnumHelper.byName(values(), name);
        }
    }
}
