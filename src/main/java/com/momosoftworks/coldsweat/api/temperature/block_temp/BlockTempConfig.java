package com.momosoftworks.coldsweat.api.temperature.block_temp;

import com.momosoftworks.coldsweat.data.codec.configuration.BlockTempData;
import com.momosoftworks.coldsweat.data.codec.requirement.BlockRequirement;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Wrapper BlockTemp for {@link BlockTempData} configurations.
 */
public class BlockTempConfig extends BlockTemp
{
    private final BlockTempData data;

    public BlockTempConfig(BlockTempData data)
    {
        super(data.getTemperature() < 0 ? -data.getMaxEffect() : Double.NEGATIVE_INFINITY,
              data.getTemperature() > 0 ? data.getMaxEffect() : Double.POSITIVE_INFINITY,
              data.getMinTemp(),
              data.getMaxTemp(),
              data.range(),
              data.fade(),
              data.logarithmic(),
              RegistryHelper.mapTaggableList(data.block().flatten(BlockRequirement::blocks)).toArray(new Block[0]));
        this.data = data;
    }

    @Override
    public boolean isValid(World level, BlockPos pos, BlockState state)
    {   return this.data.block().test(req -> req.test(level, pos, state));
    }

    @Override
    public double getTemperature(World level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        if (data.location().test(req -> req.test(level, pos))
        && data.entity().test(req -> req.test(entity)))
        {   return data.getTemperature();
        }
        return 0;
    }

    public BlockTempData getData()
    {   return this.data;
    }

    public boolean isInGroup(List<ResourceLocation> group)
    {   return this.data.effectGroup().map(list -> list.stream().anyMatch(group::contains)).orElse(false);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof BlockTempConfig)) return false;
        BlockTempConfig that = (BlockTempConfig) obj;
        return this.data.equals(that.data);
    }
}
