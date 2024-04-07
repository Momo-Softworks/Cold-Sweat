package com.momosoftworks.coldsweat.common.entity.data.edible;

import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class HumidBiomeEdible extends BiomeSearchingEdible
{
    public HumidBiomeEdible()
    {   super(holder -> holder.value().getDownfall() > 0.85f);
    }

    @Override
    public int getCooldown()
    {   return (int) (Math.random() * 400 + 1200);
    }

    @Override
    public TagKey<Item> associatedItems()
    {   return ModItemTags.CHAMELEON_HUMID;
    }
}
