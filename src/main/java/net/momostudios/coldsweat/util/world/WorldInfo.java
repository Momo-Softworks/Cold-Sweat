package net.momostudios.coldsweat.util.world;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldInfo
{
    /**
     * Iterates through every other block until it reaches minecraft:air, then returns the Y value
     * Ignores minecraft:cave_air
     * This is different from {@code world.getHeight()} because it attempts to ignore blocks that are floating in the air
     */
    public static int getGroundLevel(BlockPos pos, World world)
    {
        int iterateY = 0;
        int successes = 0;
        for (int c = 0; c < 127; c++)
        {
            if (world.getBlockState(new BlockPos(pos.getX(), iterateY, pos.getZ())).getBlock() == Blocks.AIR)
            {
                return iterateY;
            }
            iterateY += 2;
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
    public static float getBiomeTemperature(BlockPos pos, World world, int samples, int interval)
    {
        pos = new BlockPos(pos.getX() / 2, pos.getY(), pos.getZ() / 2);
        double totalTemp = 0;

        int lx = (int) (pos.getX() - (Math.sqrt(samples) * interval) / 2);
        for (int sx = 0; sx < Math.sqrt(samples); sx++)
        {
            int lz = (int) (pos.getZ() - (Math.sqrt(samples) * interval) / 2);
            for (int sz = 0; sz < Math.sqrt(samples); sz++)
            {
                totalTemp += world.getBiome(pos.add(lx, 0, lz)).getTemperature(pos.add(lx, 0, lz));
                //Just for testing... world.setBlockState(pos.add(lx, 0, lz), Blocks.GLASS.getDefaultState(), 2);
                lz += interval;
            }
            lx += interval;
        }
        return (float) (totalTemp / samples);
    }
}
