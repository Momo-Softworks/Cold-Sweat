package com.momosoftworks.coldsweat.common.world.feature;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.common.block.SoulStalkBlock;
import com.momosoftworks.coldsweat.data.tags.ModBlockTags;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.Random;

public class SoulStalkFeature extends Feature<SoulStalkFeatureConfig>
{
    public SoulStalkFeature(Codec<SoulStalkFeatureConfig> config)
    {
        super(config);
    }

    public boolean isAirOrLeaves(WorldGenLevel level, BlockPos pos)
    {   return level.getBlockState(pos).isAir() || level.getBlockState(pos).is(BlockTags.LEAVES);
    }

    @Override
    public boolean place(FeaturePlaceContext<SoulStalkFeatureConfig> context)
    {
        SoulStalkFeatureConfig config = context.config();
        WorldGenLevel level = context.level();
        BlockPos.MutableBlockPos pos = context.origin().mutable();

        int diskWidth = config.diskWidth();
        int diskHeight = config.diskHeight();
        BlockStateProvider diskProvider = config.diskStateProvider();
        BlockPredicate diskReplacer = config.replaceBlocks();

        int successes = 0;
        for (int t = 0; t < config.tries(); t++)
        {   pos.set(context.origin()).move(context.random().nextInt(config.spreadXZ()) - config.spreadXZ() / 2,
                                           context.random().nextInt(config.spreadY())  - config.spreadY() / 2,
                                           context.random().nextInt(config.spreadXZ()) - config.spreadXZ() / 2);
            // Get ground level
            int startY = pos.getY();
            int minHeight = level.getMinBuildHeight();
            int maxHeight = level.getMaxBuildHeight();
            // scan down 10 blocks, then up 10 blocks, to find valid ground
            boolean found = false;
            for (int i = 0; Math.abs(i) < 20; i = i >= 0 ? -i - 1 : -i)
            {
                pos.setY(startY + i);
                if (pos.getY() < minHeight) continue;
                if (pos.getY() > maxHeight) break;
                if (level.getBlockState(pos).isAir())
                {
                    BlockState below = level.getBlockState(pos.below());
                    if (below.is(ModBlockTags.SOUL_SAND_REPLACEABLE) || below.is(ModBlockTags.SOUL_STALK_PLACEABLE_ON))
                    {   found = true;
                        break;
                    }
                }
            }
            // Valid ground wasn't found; abort
            if (!found) return false;

            // Place the soul stalk
            if (level.getBlockState(pos.above()).isAir())
            {
                // Spawn a disk of soul sand under the soul stalk if needed
                if (!level.getBlockState(pos.below()).is(ModBlockTags.SOUL_STALK_PLACEABLE_ON) && diskWidth > 0 && diskHeight > 0)
                {   placeDisk(level, pos.below(diskHeight), diskWidth, diskHeight, diskWidth, diskProvider, diskReplacer);
                }

                level.setBlock(pos, ModBlocks.SOUL_STALK.defaultBlockState(), 2);
                int height = new Random().nextInt(5) + 2;
                for (int i = 0; i < height && isAirOrLeaves(level, pos.above()); i++)
                {   pos.move(0, 1, 0);
                    level.setBlock(pos, ModBlocks.SOUL_STALK.defaultBlockState().setValue(SoulStalkBlock.SECTION, new Random().nextInt(2) + 1), 2);
                }
                level.setBlock(pos, ModBlocks.SOUL_STALK.defaultBlockState().setValue(SoulStalkBlock.SECTION, 3), 2);

                successes++;
                int minCount = config.minCount();
                int maxCount = config.maxCount();
                if (successes >= maxCount
                || (successes >= minCount && context.random().nextInt(0, maxCount - minCount) == 0))
                {   break;
                }
            }
        }
        return successes > 0;
    }

    private static void placeDisk(WorldGenLevel level, BlockPos pos, int radiusX, int radiusY, int radiusZ, BlockStateProvider diskProvider, BlockPredicate diskReplacer)
    {
        for (int x = -radiusX; x <= radiusX; x++)
        {
            for (int y = -radiusY; y <= radiusY; y++)
            {
                for (int z = -radiusZ; z <= radiusZ; z++)
                {
                    if (Math.pow((double) x / radiusX, 2) + Math.pow((double) y / radiusY, 2) + Math.pow((double) z / radiusZ, 2) < 1)
                    {
                        BlockPos diskPos = pos.offset(x, y, z);
                        if (diskReplacer.test(level, diskPos))
                        {   level.setBlock(diskPos, diskProvider.getState(level.getRandom(), diskPos), 2);
                        }
                    }
                }
            }
        }
    }
}
