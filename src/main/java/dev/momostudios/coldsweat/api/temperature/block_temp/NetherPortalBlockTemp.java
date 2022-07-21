package dev.momostudios.coldsweat.api.temperature.block_temp;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.world.level.dimension.DimensionType;

public class NetherPortalBlockTemp extends BlockTemp
{
    public NetherPortalBlockTemp()
    {
        super(Blocks.NETHER_PORTAL);
    }

    @Override
    public double getTemperature(Player player, BlockState state, BlockPos pos, double distance)
    {
        boolean isInOverworld = player.level.dimension().location().equals(DimensionType.OVERWORLD_LOCATION.location());
        return CSMath.blend(isInOverworld ? 0.3 : -0.2, 0, distance, 0, 3);
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
