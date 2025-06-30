package com.momosoftworks.coldsweat.api.temperature.block_temp;

import com.momosoftworks.coldsweat.data.codec.configuration.BlockTempData;
import com.momosoftworks.coldsweat.data.codec.requirement.BlockRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockTempConfig extends BlockTemp
{
    private final NegatableList<BlockRequirement> predicates;

    public BlockTempConfig(double minEffect, double maxEffect, double minTemp, double maxTemp, double range, boolean fade, boolean logarithmic,
                           NegatableList<BlockRequirement> predicates, Block... blocks)
    {
        super(minEffect, maxEffect, minTemp, maxTemp, range, fade, logarithmic, blocks);
        this.predicates = predicates;
    }

    public BlockTempConfig(BlockTempData data)
    {
        super(data.getTemperature() < 0 ? -data.getMaxEffect() : Double.NEGATIVE_INFINITY,
              data.getTemperature() > 0 ? data.getMaxEffect() : Double.POSITIVE_INFINITY,
              data.getMinTemp(),
              data.getMaxTemp(),
              data.range(),
              data.fade(),
              data.logarithmic(),
              RegistryHelper.mapBuiltinRegistryTagList(BuiltInRegistries.BLOCK, data.block().flatten(BlockRequirement::blocks)).toArray(new Block[0]));
        this.predicates = data.block();
    }

    @Override
    public boolean isValid(Level level, BlockPos pos, BlockState state)
    {
        return this.predicates.test(req -> req.test(level, pos, state));
    }

    public boolean comparePredicates(BlockTempConfig other)
    {   return predicates.equals(other.predicates);
    }

    public NegatableList<BlockRequirement> getPredicates()
    {   return predicates;
    }
}
