package net.momostudios.coldsweat.core.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.momostudios.coldsweat.common.te.HearthTileEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * Returns the adjacent 6 positions to the BlockPos
     */
    public static List<BlockPos> adjacentPositions(BlockPos pos)
    {
        return new ArrayList<>(Arrays.asList(pos, pos.up(), pos.down(), pos.east(), pos.west(), pos.north(), pos.south()));
    }

    public static boolean canSeeSky(World world, BlockPos pos)
    {
        for (int i = 0; i < 255 - pos.getY(); i++)
        {
            if (!isBlockSpreadable(world, pos.up(i), Direction.UP) && !isBlockSpreadable(world, pos.up(i), Direction.DOWN))
            {
                //if (Math.random() < 0.01) System.out.println(world.getBlockState(pos).getBlock());
                return false;
            }
        }
        return true;
    }

    public static boolean isBlockSpreadable(World world, BlockPos pos, @Nullable Direction dir)
    {
        BlockState state = world.getBlockState(pos);

        return (dir == null || (!state.isSolidSide(world, pos, dir) && !state.isSolidSide(world, pos, dir.getOpposite()))) &&
                (world.isAirBlock(pos) || (state.isSolid() && !state.getShape(world, pos).equals(VoxelShapes.create(0, 0, 0, 1, 1, 1))) ||
                        (state.hasProperty(DoorBlock.OPEN) && state.get(DoorBlock.OPEN)) ||
                        (state.hasProperty(TrapDoorBlock.OPEN) && state.get(TrapDoorBlock.OPEN)));
    }
}
