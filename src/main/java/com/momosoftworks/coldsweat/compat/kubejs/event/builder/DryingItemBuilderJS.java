package com.momosoftworks.coldsweat.compat.kubejs.event.builder;

import com.momosoftworks.coldsweat.data.codec.configuration.DryingItemData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Predicate;

public class DryingItemBuilderJS
{
    public ItemStack result = ItemStack.EMPTY;
    public SoundEvent sound = SoundEvents.WET_GRASS_STEP;
    public NegatableList<ItemRequirement> itemPredicate = new NegatableList<>();
    public NegatableList<EntityRequirement> entityPredicate = new NegatableList<>();

    public DryingItemBuilderJS()
    {}

    public DryingItemBuilderJS items(String... items)
    {
        List<Item> itemList = RegistryHelper.mapTaggableList(ConfigHelper.getItems(items));
        this.itemPredicate.add(new ItemRequirement(itemList, null), false);
        return this;
    }

    public DryingItemBuilderJS result(ItemStack result)
    {
        this.result = result;
        return this;
    }

    public DryingItemBuilderJS sound(String soundId)
    {
        this.sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(soundId));
        return this;
    }

    public DryingItemBuilderJS itemPredicate(Predicate<ItemStack> itemPredicate)
    {
        this.itemPredicate.add(new ItemRequirement(itemPredicate), false);
        return this;
    }

    public DryingItemBuilderJS entityPredicate(Predicate<Entity> entityPredicate)
    {
        this.entityPredicate.add(new EntityRequirement(entityPredicate), false);
        return this;
    }

    public DryingItemData build()
    {
        DryingItemData data = new DryingItemData(this.itemPredicate, this.result, this.entityPredicate, this.sound);
        data.setType(ConfigData.Type.KUBEJS);
        return data;
    }
}
