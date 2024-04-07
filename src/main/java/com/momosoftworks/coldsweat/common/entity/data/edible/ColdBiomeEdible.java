package com.momosoftworks.coldsweat.common.entity.data.edible;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;

public class ColdBiomeEdible extends BiomeSearchingEdible
{
    public ColdBiomeEdible()
    {
        super(biome ->
        {
            Pair<Double, Double> minMaxTemp = WorldHelper.getBiomeTemperature(biome);
            double biomeTemp = CSMath.averagePair(minMaxTemp);

            return biomeTemp <= 0.2;
        });
    }

    @Override
    public int getCooldown()
    {   return (int) (Math.random() * 400 + 1200);
    }

    @Override
    public ITag.INamedTag<Item> associatedItems()
    {   return ModItemTags.CHAMELEON_COLD;
    }
}
