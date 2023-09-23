package dev.momostudios.coldsweat.common.world.feature;

import com.mojang.serialization.Codec;
import dev.momostudios.coldsweat.common.block.SoulStalkBlock;
import dev.momostudios.coldsweat.data.tags.ModBlockTags;
import dev.momostudios.coldsweat.util.registries.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.blockstateprovider.BlockStateProvider;
import net.minecraft.world.gen.feature.Feature;

import java.util.Random;

public class SoulStalkFeature extends Feature<SoulStalkFeatureConfig>
{
    public SoulStalkFeature(Codec<SoulStalkFeatureConfig> config)
    {
        super(config);
    }

    public boolean isAirOrLeaves(IWorld level, BlockPos pos)
    {   return level.getBlockState(pos).isAir() || level.getBlockState(pos).is(BlockTags.LEAVES);
    }

    @Override
    public boolean place(ISeedReader world, ChunkGenerator chunkGenerator, Random rand, BlockPos blockPos, SoulStalkFeatureConfig config)
    {
        IWorld level = world;
        BlockPos.Mutable pos = blockPos.mutable();

        int diskWidth = config.diskWidth;
        int diskHeight = config.diskHeight;
        BlockStateProvider diskProvider = config.diskStateProvider;
        ITag<Block> diskReplacer = config.replaceBlocks;

        int successes = 0;
        for (int t = 0; t < config.tries; t++)
        {   pos.set(blockPos).move(rand.nextInt(config.spreadXZ) - config.spreadXZ / 2,
                                           rand.nextInt(config.spreadY)  - config.spreadY / 2,
                                           rand.nextInt(config.spreadXZ) - config.spreadXZ / 2);
            // Get ground level
            int startY = pos.getY();
            int minHeight = 0;
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
                int minCount = config.minCount;
                int maxCount = config.maxCount;
                if (successes >= maxCount
                || (successes >= minCount && rand.nextInt(maxCount - minCount) == 0))
                {   break;
                }
            }
        }
        return successes > 0;
    }

    private static void placeDisk(IWorld level, BlockPos pos, int radiusX, int radiusY, int radiusZ, BlockStateProvider diskProvider, ITag<Block> diskReplacer)
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
                        if (level.getBlockState(diskPos).is(diskReplacer))
                        {   level.setBlock(diskPos, diskProvider.getState(level.getRandom(), diskPos), 2);
                        }
                    }
                }
            }
        }
    }
}
