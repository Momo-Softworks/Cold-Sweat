package net.momostudios.coldsweat.core.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

import java.util.ArrayList;
import java.util.List;

public class WorldInfo
{
    /**
     * Iterates through every other block until it reaches minecraft:air, then returns the Y value
     * Ignores minecraft:cave_air
     * This is different from {@code world.getHeight()} because it attempts to ignore blocks that are floating in the air
     */
    public static int getGroundLevel(BlockPos pos, World world)
    {
        for (int c = 0; c < 255; c++)
        {
            BlockPos pos2 = new BlockPos(pos.getX(), c, pos.getZ());
            BlockState state = world.getBlockState(pos2);
            if (!state.isSolid() && state.getBlock() != Blocks.CAVE_AIR)
            {
                return c;
            }
        }
        return 0;
    }

    /**
     * Gets the average biome temperature in a grid of BlockPos 3 blocks apart
     * Search area scales with the number of samples
     * @param pos is the center of the search box
     * @param samples is the number of checks performed. Higher samples = more accurate but more resource-intensive too
     * @param interval is how far apart each check is. Higher values means less dense and larger search area
     */
    public static List<BlockPos> getNearbyPositions(BlockPos pos, int samples, int interval)
    {
        List<BlockPos> posList = new ArrayList<>();
        pos = new BlockPos(pos.getX() / 2, pos.getY(), pos.getZ() / 2);

        int lx = (int) (pos.getX() - (Math.sqrt(samples) * interval) / 2);
        for (int sx = 0; sx < Math.sqrt(samples); sx++)
        {
            int lz = (int) (pos.getZ() - (Math.sqrt(samples) * interval) / 2);
            for (int sz = 0; sz < Math.sqrt(samples); sz++)
            {
                posList.add(pos.add(lx, 0, lz));
                lz += interval;
            }
            lx += interval;
        }
        return posList;
    }

    public static List<BlockPos> getNearbyPositionsCubed(BlockPos pos, int samples, int interval)
    {
        List<BlockPos> posList = new ArrayList<>();
        pos = new BlockPos(pos.getX() / 2, pos.getY() / 2, pos.getZ() / 2);

        int lx = (int) (pos.getX() - (Math.cbrt(samples) * interval) / 2);
        for (int sx = 0; sx < Math.cbrt(samples); sx++)
        {
            int ly = (int) (pos.getY() - (Math.cbrt(samples) * interval) / 2);
            for (int sy = 0; sy < Math.cbrt(samples); sy++)
            {
                int lz = (int) (pos.getZ() - (Math.cbrt(samples) * interval) / 2);
                for (int sz = 0; sz < Math.cbrt(samples); sz++)
                {
                    posList.add(pos.add(lx, ly, lz));
                    lz += interval;
                }
                ly += interval;
            }
            lx += interval;
        }
        return posList;
    }
}
