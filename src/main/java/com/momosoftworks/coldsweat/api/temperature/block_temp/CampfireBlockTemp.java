package com.momosoftworks.coldsweat.api.temperature.block_temp;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.core.BlockPos;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class CampfireBlockTemp extends BlockTemp
{
    public CampfireBlockTemp()
    {
        super(ForgeRegistries.BLOCKS.tags().getTag(BlockTags.CAMPFIRES).stream().filter(block -> block instanceof CampfireBlock && block != Blocks.SOUL_CAMPFIRE).toArray(Block[]::new));
    }

    @Override
    public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        if (state.getValue(CampfireBlock.LIT))
        {   return CSMath.blend(0.476, 0, distance, 0.5, 7);
        }
        return 0;
    }

    @Override
    public double maxEffect() {
        return Temperature.convert(40, Temperature.Units.F, Temperature.Units.MC, false);
    }

    @Override
    public double maxTemperature() {
        return Temperature.convert(400, Temperature.Units.F, Temperature.Units.MC, true);
    }
}
