package net.momostudios.coldsweat.helpers;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldInfo
{
    //Iterates through every other block until it reaches minecraft:air, then returns the Y value
    //Ignores minecraft:cave_air
    //This is different from world.getHeight() because it attemps to ignore blocks that are floating in the air
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

    //Gets the average biome temperature in a grid of BlockPos 3 blocks apart
    //Search area scales with the number of samples
    public static float getBiomeTemperature(BlockPos pos, World world, int samples)
    {
        samples = (int) Math.pow(samples, 0.25);
        pos = new BlockPos(pos.getX() / 2, pos.getY(), pos.getZ() / 2);
        double totalTemp = 0;

        int lx = pos.getX() - (int) samples;
        for (int sx = 0; sx < samples; sx++)
        {
            int lz = pos.getZ() - (int) samples;
            for (int sz = 0; sz < samples; sz++)
            {
                totalTemp += world.getBiome(pos.add(lx, 0, lz)).getTemperature(pos.add(lx, 0, lz));
                lz += 4;
            }
            lx += 4;
        }
        return (float) (totalTemp / samples);
    }
}
