package dev.momostudios.coldsweat.common.world.feature;

import com.mojang.serialization.Codec;
import dev.momostudios.coldsweat.common.block.SoulStalkBlock;
import dev.momostudios.coldsweat.util.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Random;

public class SoulStalkFeature extends Feature<NoneFeatureConfiguration>
{
    public SoulStalkFeature(Codec<NoneFeatureConfiguration> config)
    {
        super(config);
    }

    public boolean isAirOrLeaves(WorldGenLevel level, BlockPos pos)
    {
        return level.getBlockState(pos).isAir() || level.getBlockState(pos).is(BlockTags.LEAVES);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> placement)
    {
        WorldGenLevel level = placement.level();
        BlockPos.MutableBlockPos pos = placement.origin().mutable();
        // Get ground level
        int startY = pos.getY();
        int minHeight = level.getMinBuildHeight();
        int maxHeight = level.getMaxBuildHeight();
        for (int i = -10; i < 10; i++)
        {   pos.setY(startY + i);
            if (pos.getY() < minHeight) continue;
            if (pos.getY() > maxHeight) break;
            if (level.getBlockState(pos).isAir())
            {   break;
            }
        }
        // Place the soul stalk
        if (level.getBlockState(pos.below()).is(BlockTags.SOUL_FIRE_BASE_BLOCKS) && isAirOrLeaves(level, pos) && isAirOrLeaves(level, pos.above()))
        {
            level.setBlock(pos, ModBlocks.SOUL_STALK.defaultBlockState(), 2);
            int height = new Random().nextInt(5) + 2;
            for (int i = 0; i < height && isAirOrLeaves(level, pos.above()); i++)
            {
                pos.move(0, 1, 0);
                level.setBlock(pos, ModBlocks.SOUL_STALK.defaultBlockState().setValue(SoulStalkBlock.SECTION, new Random().nextInt(2) + 1), 2);
            }
            level.setBlock(pos, ModBlocks.SOUL_STALK.defaultBlockState().setValue(SoulStalkBlock.SECTION, 3), 2);
            return true;
        }

        return false;
    }
}
