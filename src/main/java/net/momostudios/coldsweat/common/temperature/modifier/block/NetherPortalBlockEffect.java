package net.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.momostudios.coldsweat.util.CSMath;
import net.momostudios.coldsweat.util.Units;

public class NetherPortalBlockEffect extends BlockEffect
{

    @Override
    public double getTemperature(PlayerEntity player, BlockState state, BlockPos pos, double distance)
    {
        boolean isInOverworld = player.world.getDimensionKey().getLocation().toString().equals(DimensionType.OVERWORLD.getLocation().toString());
        return CSMath.blend(isInOverworld ? 0.3 : -0.2, 0, distance, 0, 3);
    }

    @Override
    public boolean hasBlock(BlockState block)
    {
        return block.getBlock() == net.minecraft.block.Blocks.NETHER_PORTAL;
    }

    @Override
    public double maxEffect()
    {
        return 1;
    }

    @Override
    public double minEffect()
    {
        return -1;
    }
}
