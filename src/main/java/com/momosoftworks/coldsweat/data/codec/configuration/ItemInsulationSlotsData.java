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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;

import javax.annotation.Nullable;
import java.util.List;

public class ItemInsulationSlotsData extends ConfigData implements RequirementHolder
{
    final NegatableList<ItemRequirement> item;
    final int slots;

    public ItemInsulationSlotsData(NegatableList<ItemRequirement> item, int slots, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.item = item;
        this.slots = slots;
    }

    public ItemInsulationSlotsData(NegatableList<ItemRequirement> item, int slots)
    {   this(item, slots, new NegatableList<>());
    }

    public static final Codec<ItemInsulationSlotsData> CODEC = createCodec(RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.codec(ItemRequirement.CODEC).optionalFieldOf("item", new NegatableList<>()).forGetter(ItemInsulationSlotsData::item),
            Codec.INT.fieldOf("slots").forGetter(ItemInsulationSlotsData::slots)
    ).apply(instance, ItemInsulationSlotsData::new)));

    public NegatableList<ItemRequirement> item()
    {   return item;
    }
    public int slots()
    {   return slots;
    }

    @Override
    public boolean test(ItemStack stack)
    {   return item.test(req -> req.test(stack, true));
    }

    @Nullable
    public static ItemInsulationSlotsData fromToml(List<?> entry)
    {
        if (entry.size() < 2)
        {   ColdSweat.LOGGER.error("Error parsing insulation slot override config: not enough arguments");
            return null;
        }
        NegatableList<Either<ITag<Item>, Item>> items = ConfigHelper.getItems((String) entry.get(0));
        if (items.isEmpty()) return null;
        int slots = ((Number) entry.get(1)).intValue();
        NbtRequirement nbtRequirement = entry.size() > 2
                                        ? new NbtRequirement(NBTHelper.parseCompoundNbt((String) entry.get(2)))
                                        : new NbtRequirement(new CompoundNBT());
        ItemRequirement itemRequirement = new ItemRequirement(items, nbtRequirement);

        return new ItemInsulationSlotsData(new NegatableList<>(itemRequirement), slots);
    }

    @Override
    public Codec<? extends ConfigData> getCodec()
    {   return CODEC;
    }
}
