package com.momosoftworks.coldsweat.api.temperature.block_temp;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.BlockTempData;
import com.momosoftworks.coldsweat.data.codec.requirement.BlockRequirement;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wrapper BlockTemp for {@link BlockTempData} configurations.
 */
public class ConfiguredBlockTemp extends BlockTemp
{
    private final BlockTempData data;

    public ConfiguredBlockTemp(BlockTempData data)
    {
        super(data.getTemperature() < 0 ? -data.getMaxEffect() : Double.NEGATIVE_INFINITY,
              data.getTemperature() > 0 ? data.getMaxEffect() : Double.POSITIVE_INFINITY,
              data.getMinTemp(),
              data.getMaxTemp(),
              data.range(),
              data.fade(),
              data.logarithmic(),
              RegistryHelper.mapBuiltinRegistryTagList(BuiltInRegistries.BLOCK, data.block().flatten(BlockRequirement::blocks)).toArray(new Block[0]));
        this.data = data;
    }

    @Override
    public boolean isValid(Level level, BlockPos pos, BlockState state)
    {   return this.data.block().test(req -> req.test(level, pos, state));
    }

    @Override
    public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        if (data.location().test(req -> req.test(level, pos))
        && data.entity().test(req -> req.test(entity)))
        {
            double temp = data.getTemperature();
            if (ConfigSettings.COLD_SOUL_FIRE.get() && state.is(ModBlockTags.SOUL_FIRE))
            {   temp *= -1;
            }
            return temp;
        }
        return 0;
    }

    public BlockTempData getData()
    {   return this.data;
    }

    public boolean isInGroup(TagKey<BlockTempData>  group)
    {   return this.data.effectGroup().map(g -> g.equals(group)).orElse(false);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof ConfiguredBlockTemp)) return false;
        ConfiguredBlockTemp that = (ConfiguredBlockTemp) obj;
        return this.data.equals(that.data);
    }
}
