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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FoodData extends ConfigData implements RequirementHolder, IForgeRegistryEntry<FoodData>
{
    final NegatableList<ItemRequirement> item;
    final Double temperature;
    final int duration;
    final int stackLimit;
    final NegatableList<EntityRequirement> entityRequirement;

    public FoodData(NegatableList<ItemRequirement> item, Double temperature, int duration, int stackLimit,
                    NegatableList<EntityRequirement> entityRequirement, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.temperature = temperature;
        this.item = item;
        this.duration = duration;
        this.stackLimit = stackLimit;
        this.entityRequirement = entityRequirement;
    }

    public FoodData(NegatableList<ItemRequirement> item, Double temperature, int duration, int stackLimit,
                    NegatableList<EntityRequirement> entityRequirement)
    {
        this(item, temperature, duration, stackLimit, entityRequirement, new NegatableList<>());
    }

    public static final Codec<FoodData> CODEC = createCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
            NegatableList.codec(ItemRequirement.CODEC).optionalFieldOf("item", new NegatableList<>()).forGetter(FoodData::item),
            Codec.DOUBLE.fieldOf("temperature").forGetter(FoodData::temperature),
            Codec.INT.optionalFieldOf("duration", 0).forGetter(FoodData::duration),
            Codec.INT.optionalFieldOf("stack_limit", 1).forGetter(data -> data.stackLimit),
            NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("entity", new NegatableList<>()).forGetter(FoodData::entityRequirement)
    ).apply(instance, FoodData::new)));

    public NegatableList<ItemRequirement> item()
    {   return item;
    }
    public Double temperature()
    {   return temperature;
    }
    public int duration()
    {   return duration;
    }
    public int stackLimit()
    {   return stackLimit;
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
        NegatableList<Either<TagKey<Item>, Item>> items = ConfigHelper.getItems((String) entry.get(0));
        if (items.isEmpty()) return null;
        double temperature = ((Number) entry.get(1)).doubleValue();
        NbtRequirement nbtRequirement = entry.size() > 2
                                        ? new NbtRequirement(NBTHelper.parseCompoundNbt((String) entry.get(2)))
                                        : new NbtRequirement(new CompoundTag());
        int duration = entry.size() > 3 ? ((Number) entry.get(3)).intValue() : 0;
        int stackLimit = entry.size() > 4 ? (Integer) entry.get(4) : 1;
        ItemRequirement itemRequirement = new ItemRequirement(items, nbtRequirement);

        return new FoodData(new NegatableList<>(itemRequirement), temperature, duration, stackLimit, new NegatableList<>());
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

    @Override
    public FoodData setRegistryName(ResourceLocation name)
    {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName()
    {
        return null;
    }

    @Override
    public Class<FoodData> getRegistryType()
    {
        return null;
    }
}
