package net.momostudios.coldsweat.core.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.block.material.Material;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.momostudios.coldsweat.common.te.HearthTileEntity;
import net.momostudios.coldsweat.core.util.registrylists.ModBlocks;

import javax.annotation.Nonnull;
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
        int sampleRoot = (int) Math.sqrt(samples);

        for (int sx = 0; sx < sampleRoot; sx++)
        {
            for (int sz = 0; sz < sampleRoot; sz++)
            {
                int length = interval * sampleRoot;
                posList.add(pos.add(sx * interval - (length / 2), 0, sz * interval - (length / 2)));
            }
        }

        return posList;
    }

    public static boolean canSeeSky(World world, BlockPos pos)
    {
        if (!world.canSeeSky(pos)) return false;

        for (int i = 0; i < 255 - pos.getY(); i++)
        {
            if (!isBlockSpreadable(world, pos.up(i), pos.up(i + 1)))
            {
                return false;
            }
        }
        return true;
    }

    public static boolean isBlockSpreadable(World world, @Nonnull BlockPos fromPos, @Nonnull BlockPos toPos)
    {
        BlockState state = world.getBlockState(fromPos);
        BlockState state2 = world.getBlockState(toPos);
        Direction dir = Direction.getFacingFromVector(toPos.getX() - fromPos.getX(), toPos.getY() - fromPos.getY(), toPos.getZ() - fromPos.getZ());

        return
            // Outlet side of original block is not solid
            !state.isSolidSide(world, fromPos, dir) &&

            // Inlet side of new pos is not solid
            (!state2.isSolidSide(world, toPos, dir.getOpposite()) ||

            // Block is air or no sides solid
            state2.getMaterial() == Material.AIR || !state2.getShape(world, toPos).equals(VoxelShapes.create(0, 0, 0, 1, 1, 1)) ||

            // Block is a hearth
            state2.getBlock() == ModBlocks.HEARTH || state2.getBlock() == ModBlocks.HEARTH_TOP);
    }

    public static double distance(Vector3i pos1, Vector3i pos2)
    {
        return Math.sqrt(pos1.distanceSq(pos2));
    }
}
