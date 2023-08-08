package dev.momostudios.coldsweat.common.world.feature;

import com.mojang.serialization.Codec;
import dev.momostudios.coldsweat.common.block.SoulStalkBlock;
import dev.momostudios.coldsweat.data.tags.ModBlockTags;
import dev.momostudios.coldsweat.util.registries.ModBlocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;

public class SoulStalkFeature extends Feature<NoFeatureConfig>
{
    public SoulStalkFeature(Codec<NoFeatureConfig> config)
    {   super(config);
    }

    public boolean isAirOrLeaves(IWorld world, BlockPos pos)
    {   return world.getBlockState(pos).isAir() || world.getBlockState(pos).is(BlockTags.LEAVES);
    }

    @Override
    public boolean place(ISeedReader world, ChunkGenerator chunkGenerator, Random rand, BlockPos blockPos, NoFeatureConfig config)
    {
        BlockPos.Mutable pos = blockPos.mutable();
        // Get ground level
        int startY = pos.getY();
        int minHeight = 0;
        int maxHeight = world.getMaxBuildHeight();
        for (int i = -10; i < 10; i++)
        {   pos.setY(startY + i);
            if (pos.getY() < minHeight) continue;
            if (pos.getY() > maxHeight) break;
            if (world.getBlockState(pos).isAir())
            {   break;
            }
        }
        // Place the soul stalk
        if (world.getBlockState(pos.below()).is(ModBlockTags.SOUL_STALK_PLACEABLE_ON) && isAirOrLeaves(world, pos) && isAirOrLeaves(world, pos.above()))
        {
            world.setBlock(pos, ModBlocks.SOUL_STALK.defaultBlockState(), 2);
            int height = new Random().nextInt(5) + 2;
            for (int i = 0; i < height && isAirOrLeaves(world, pos.above()); i++)
            {
                pos.move(0, 1, 0);
                world.setBlock(pos, ModBlocks.SOUL_STALK.defaultBlockState().setValue(SoulStalkBlock.SECTION, new Random().nextInt(2) + 1), 2);
            }
            world.setBlock(pos, ModBlocks.SOUL_STALK.defaultBlockState().setValue(SoulStalkBlock.SECTION, 3), 2);
            return true;
        }

        return false;
    }
}
