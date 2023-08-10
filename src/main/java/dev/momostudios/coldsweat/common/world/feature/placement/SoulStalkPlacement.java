package dev.momostudios.coldsweat.common.world.feature.placement;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class SoulStalkPlacement extends Placement<NoPlacementConfig>
{
    int xzSpread = 8;
    int ySpread = 8;
    int count = 1;

    public SoulStalkPlacement()
    {   super(NoPlacementConfig.CODEC);
    }

    @Override
    public Stream<BlockPos> getPositions(WorldDecoratingHelper world, Random rand, NoPlacementConfig config, BlockPos origin)
    {
        List<BlockPos> positions = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {   positions.add(origin.offset(rand.nextInt(xzSpread) - rand.nextInt(xzSpread),
                                        rand.nextInt(ySpread) - rand.nextInt(ySpread),
                                        rand.nextInt(xzSpread) - rand.nextInt(xzSpread)));
        }
        return positions.stream();
    }

    public SoulStalkPlacement spread(int xzSpread, int ySpread, int count)
    {   this.xzSpread = xzSpread;
        this.ySpread = ySpread;
        this.count = count;
        return this;
    }
}
