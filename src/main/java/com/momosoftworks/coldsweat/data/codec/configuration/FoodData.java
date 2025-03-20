package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class FoodData extends ConfigData implements RequirementHolder
{
    final NegatableList<ItemRequirement> item;
    final Double temperature;
    final int duration;
    final NegatableList<EntityRequirement> entityRequirement;

    public FoodData(NegatableList<ItemRequirement> item, Double temperature, int duration,
                    NegatableList<EntityRequirement> entityRequirement, List<String> requiredMods)
    {
        super(requiredMods);
        this.temperature = temperature;
        this.item = item;
        this.duration = duration;
        this.entityRequirement = entityRequirement;
    }

    public FoodData(NegatableList<ItemRequirement> item, Double temperature, int duration,
                    NegatableList<EntityRequirement> entityRequirement)
    {
        this(item, temperature, duration, entityRequirement, Arrays.asList());
    }

    public static final Codec<FoodData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.codec(ItemRequirement.CODEC).optionalFieldOf("item", new NegatableList<>()).forGetter(FoodData::item),
            Codec.DOUBLE.fieldOf("temperature").forGetter(FoodData::temperature),
            Codec.INT.optionalFieldOf("duration", 0).forGetter(FoodData::duration),
            NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("entity", new NegatableList<>()).forGetter(FoodData::entityRequirement),
            Codec.STRING.listOf().optionalFieldOf("required_mods", Arrays.asList()).forGetter(FoodData::requiredMods)
    ).apply(instance, FoodData::new));

    public NegatableList<ItemRequirement> item()
    {   return item;
    }
    public Double temperature()
    {   return temperature;
    }
    public int duration()
    {   return duration;
    }
    public NegatableList<EntityRequirement> entityRequirement()
    {   return entityRequirement;
    }

    @Override
    public boolean test(ItemStack stack)
    {   return item.test(req -> req.test(stack, true));
    }

    @Override
    public boolean test(Entity entity)
    {   return entityRequirement.test(req -> req.test(entity));
    }

    @Nullable
    public static FoodData fromToml(List<?> entry)
    {
        if (entry.size() < 2)
        {   ColdSweat.LOGGER.error("Error parsing food config: not enough arguments");
            return null;
        }
        List<Either<ITag<Item>, Item>> items = ConfigHelper.getItems((String) entry.get(0));
        if (items.isEmpty()) return null;
        double temperature = ((Number) entry.get(1)).doubleValue();
        NbtRequirement nbtRequirement = entry.size() > 2
                                        ? new NbtRequirement(NBTHelper.parseCompoundNbt((String) entry.get(2)))
                                        : new NbtRequirement(new CompoundNBT());
        int duration = entry.size() > 3 ? ((Number) entry.get(3)).intValue() : 0;
        ItemRequirement itemRequirement = new ItemRequirement(items, nbtRequirement);

        return new FoodData(new NegatableList<>(itemRequirement), temperature, duration, new NegatableList<>());
    }

    @Override
    public Codec<FoodData> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        FoodData that = (FoodData) obj;
        return super.equals(obj)
            && item.equals(that.item)
            && temperature.equals(that.temperature)
            && duration == that.duration
            && entityRequirement.equals(that.entityRequirement);
    }
}
