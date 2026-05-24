package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.data.codec.util.ValueGetter;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class FoodData extends ConfigData implements RequirementHolder
{
    final NegatableList<ItemRequirement> item;
    final ValueGetter<Double> temperature;
    final ValueGetter<Integer> duration;
    final ValueGetter<Integer> stackLimit;
    final NegatableList<EntityRequirement> entityRequirement;
    final Map<Temperature.Trait, List<TempModifier>> modifiers;

    public FoodData(NegatableList<ItemRequirement> item, ValueGetter<Double> temperature, ValueGetter<Integer> duration, ValueGetter<Integer> stackLimit,
                    NegatableList<EntityRequirement> entityRequirement, Map<Temperature.Trait, List<TempModifier>> modifiers, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.temperature = temperature;
        this.item = item;
        this.duration = duration;
        this.stackLimit = stackLimit;
        this.entityRequirement = entityRequirement;
        this.modifiers = modifiers;
    }

    public FoodData(NegatableList<ItemRequirement> item, ValueGetter<Double> temperature, ValueGetter<Integer> duration, ValueGetter<Integer> stackLimit,
                    NegatableList<EntityRequirement> entityRequirement, Map<Temperature.Trait, List<TempModifier>> modifiers)
    {
        this(item, temperature, duration, stackLimit, entityRequirement, modifiers, new NegatableList<>());
    }

    public static final Codec<FoodData> CODEC = createCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
            NegatableList.codec(ItemRequirement.CODEC).optionalFieldOf("item", new NegatableList<>()).forGetter(FoodData::item),
            ValueGetter.optionalFieldCodec("temperature", ExtraCodecs.DOUBLE, 0.0).forGetter(FoodData::temperature),
            ValueGetter.optionalFieldCodec("duration", Codec.INT, 0).forGetter(FoodData::duration),
            ValueGetter.optionalFieldCodec("stack_limit", Codec.INT, 1).forGetter(FoodData::stackLimit),
            NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("entity", new NegatableList<>()).forGetter(FoodData::entityRequirement),
            Codec.unboundedMap(Temperature.Trait.CODEC, TempModifier.CODEC.listOf()).optionalFieldOf("temp_modifiers", Map.of()).forGetter(FoodData::modifiers)
    ).apply(instance, FoodData::new)));

    public NegatableList<ItemRequirement> item()
    {   return item;
    }
    public ValueGetter<Double> temperature()
    {   return temperature;
    }
    public double temperature(ItemStack item, Entity entity)
    {   return temperature.get(Map.of("entity", entity, "item", item));
    }
    public ValueGetter<Integer> duration()
    {   return duration;
    }
    public int duration(ItemStack item, Entity entity)
    {   return duration.get(Map.of("entity", entity, "item", item));
    }
    public ValueGetter<Integer> stackLimit()
    {   return stackLimit;
    }
    public int stackLimit(ItemStack item, Entity entity)
    {   return stackLimit.get(Map.of("entity", entity, "item", item));
    }
    public NegatableList<EntityRequirement> entityRequirement()
    {   return entityRequirement;
    }
    public Map<Temperature.Trait, List<TempModifier>> modifiers()
    {   return modifiers;
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
        NegatableList<Either<TagKey<Item>, Item>> items = ConfigHelper.getItems((String) entry.get(0));
        if (items.isEmpty()) return null;
        ValueGetter<Double> temperature = ValueGetter.parse(() -> entry.get(1), ExtraCodecs.DOUBLE, 0.0);
        NbtRequirement nbtRequirement = entry.size() > 2
                                        ? new NbtRequirement(NBTHelper.parseCompoundNbt((String) entry.get(2)))
                                        : new NbtRequirement(new CompoundTag());
        ValueGetter<Integer> duration = ValueGetter.parse(() -> entry.get(3), Codec.INT, 0);
        ValueGetter<Integer> stackLimit = ValueGetter.parse(() -> entry.get(4), Codec.INT, 1);
        ItemRequirement itemRequirement = new ItemRequirement(items, nbtRequirement);

        FoodData result = new FoodData(new NegatableList<>(itemRequirement), temperature, duration, stackLimit, new NegatableList<>(), Map.of());
        result.setConfigType(Type.TOML);
        return result;
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
            && entityRequirement.equals(that.entityRequirement)
            && modifiers.equals(that.modifiers);
    }
}
