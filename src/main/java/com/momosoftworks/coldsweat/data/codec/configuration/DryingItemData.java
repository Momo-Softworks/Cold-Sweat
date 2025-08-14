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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DryingItemData extends ConfigData implements RequirementHolder
{
    private final NegatableList<ItemRequirement> item;
    private final ItemStack result;
    private final NegatableList<EntityRequirement> entity;
    private final SoundEvent sound;

    public DryingItemData(NegatableList<ItemRequirement> item, ItemStack result, NegatableList<EntityRequirement> entity, SoundEvent sound,
                          NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.item = item;
        this.result = result;
        this.entity = entity;
        this.sound = sound;
    }

    public DryingItemData(NegatableList<ItemRequirement> item, ItemStack result, NegatableList<EntityRequirement> entity, SoundEvent sound)
    {
        this(item, result, entity, sound, new NegatableList<>(ForgeRegistries.ITEMS.getKey(result.getItem()).getNamespace()));
    }

    public static final Codec<DryingItemData> CODEC = createCodec(RecordCodecBuilder.create(builder -> builder.group(
            NegatableList.codec(ItemRequirement.CODEC).fieldOf("item").forGetter(data -> data.item),
            ItemStack.CODEC.optionalFieldOf("result", ItemStack.EMPTY).forGetter(data -> data.result),
            NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("entity", new NegatableList<>()).forGetter(data -> data.entity),
            SoundEvent.DIRECT_CODEC.optionalFieldOf("sound", SoundEvents.WET_GRASS_STEP).forGetter(data -> data.sound)
    ).apply(builder, DryingItemData::new)));

    public NegatableList<ItemRequirement> item()
    {   return item;
    }
    public ItemStack result()
    {   return result.copy();
    }
    public NegatableList<EntityRequirement> entity()
    {   return entity;
    }
    public SoundEvent sound()
    {   return sound;
    }

    @Nullable
    public static DryingItemData fromToml(List<?> entry)
    {
        if (entry.size() < 2)
        {   ColdSweat.LOGGER.error("Error parsing drying item config: not enough arguments");
            return null;
        }
        NegatableList<Either<TagKey<Item>, Item>> items = ConfigHelper.getItems((String) entry.get(0));
        if (items.isEmpty()) return null;
        Item result = ForgeRegistries.ITEMS.getValue(new ResourceLocation((String) entry.get(1)));
        if (result == null) return null;

        ResourceLocation sound = entry.size() > 2
                                 ? new ResourceLocation((String) entry.get(2))
                                 : new ResourceLocation("minecraft:block.wet_grass.step");

        ItemRequirement input = new ItemRequirement(items, new NbtRequirement());
        return new DryingItemData(new NegatableList<>(input), new ItemStack(result), new NegatableList<>(), ForgeRegistries.SOUND_EVENTS.getValue(sound));
    }

    @Override
    public boolean test(Entity entity)
    {   return this.entity.test(req -> req.test(entity));
    }

    @Override
    public boolean test(ItemStack stack)
    {   return this.item.test(req -> req.test(stack, true));
    }

    @Override
    public Codec<? extends ConfigData> getCodec()
    {   return CODEC;
    }
}
