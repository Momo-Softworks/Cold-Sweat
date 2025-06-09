package com.momosoftworks.coldsweat.common.world.feature;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.common.block.SoulStalkBlock;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;

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

        int successes = 0;
        for (int t = 0; t < config.tries(); t++)
        {   pos.set(context.origin()).move(context.random().nextInt(config.spreadXZ()) - config.spreadXZ() / 2,
                                           context.random().nextInt(config.spreadY())  - config.spreadY() / 2,
                                           context.random().nextInt(config.spreadXZ()) - config.spreadXZ() / 2);
            // Get ground level
            int startY = pos.getY();
            int minHeight = level.getMinBuildHeight();
            int maxHeight = level.getMaxBuildHeight();
            for (int i = -10; i < 10; i++)
            {   pos.setY(startY + i);
                if (pos.getY() < minHeight) continue;
                if (pos.getY() > maxHeight) break;
                if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isCollisionShapeFullBlock(level, pos.below()))
                {   break;
                }
            }
            // Spawn a disk of soul sand under the soul stalk if needed
            if (!level.getBlockState(pos.below()).is(ModBlockTags.SOUL_STALK_PLACEABLE_ON))
            {   placeDisk(level, pos, config);
            }

            // Place the soul stalk
            if (level.getBlockState(pos.below()).is(ModBlockTags.SOUL_STALK_PLACEABLE_ON) && level.getBlockState(pos.above()).isAir())
            {
                level.setBlock(pos, ModBlocks.SOUL_STALK.defaultBlockState().setValue(SoulStalkBlock.SECTION, SoulStalkBlock.Section.BASE), 2);
                int height = new Random().nextInt(5) + 2;
                for (int i = 0; i < height && isAirOrLeaves(level, pos.above()); i++)
                {   pos.move(0, 1, 0);
                    level.setBlock(pos, ModBlocks.SOUL_STALK.defaultBlockState().setValue(SoulStalkBlock.SECTION, SoulStalkBlock.getRandomMidsection()), 2);
                }
                level.setBlock(pos, ModBlocks.SOUL_STALK.defaultBlockState().setValue(SoulStalkBlock.SECTION, SoulStalkBlock.Section.TOP), 2);

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

    private static void placeDisk(WorldGenLevel level, BlockPos pos, SoulStalkFeatureConfig config)
    {
        int diskWidth = config.diskWidth();
        int diskHeight = config.diskHeight();
        double diskDecay = config.diskDecay();
        RuleBasedBlockStateProvider diskProvider = config.diskStateProvider();
        BlockPredicate diskReplacer = config.replaceBlocks();

        if (diskWidth <= 0 || diskHeight <= 0 || diskProvider == null || diskReplacer == null)
        {   return;
        }
        for (int x = -diskWidth; x <= diskWidth; x++)
        {
            for (int y = -diskHeight; y <= diskHeight; y++)
            {
                for (int z = -diskWidth; z <= diskWidth; z++)
                {
                    if (Math.pow((double) x / diskWidth, 2) + Math.pow((double) y / diskHeight, 2) + Math.pow((double) z / diskWidth, 2) < 1)
                    {
                        BlockPos diskPos = pos.offset(x, y, z);
                        double distance = Math.sqrt(diskPos.distSqr(pos));
                        double distFactor = CSMath.blend(level.getRandom().nextDouble(), 0, distance, 0, diskWidth);
                        // Place block
                        if ((distance <= 1 || distFactor > diskDecay)
                        && diskReplacer.test(level, diskPos))
                        {   level.setBlock(diskPos, diskProvider.getState(level, level.getRandom(), diskPos), 2);
                        }
                    }
                }
            }
        }
    }
}
