package net.momostudios.coldsweat.core.util;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerChunkProvider;
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
        Chunk chunk = world.getChunkProvider().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        for (int i = 0; i < 255 - y; i++)
        {
            BlockState state = chunk.getBlockState(new BlockPos(x, y + i, z));

            if (state.isAir())
            {
                continue;
            }

            if (isFullSide(state, Direction.DOWN, pos.up(i), world) || isFullSide(state, Direction.UP, pos.up(i), world))
                return false;
        }
        return true;
    }

    public static boolean canSpreadThrough(World world, @Nonnull SpreadPath path, @Nonnull Direction toDir, @Nullable Direction fromDir)
    {
        BlockPos pos = path.getPos();
        BlockState state = world.getChunkProvider().getChunk(pos.getX() >> 4, pos.getZ() >> 4, false).getBlockState(pos);

        if (state.isSolidSide(world, pos, toDir))
            return false;

        return fromDir == toDir ? !isFullSide(state, toDir, pos, world) : !state.isSolidSide(world, pos, fromDir.getOpposite());
    }

    public static double distance(Vector3i pos1, Vector3i pos2)
    {
        return Math.sqrt(pos1.distanceSq(pos2));
    }

    public static boolean isFullSide(BlockState state, Direction dir, BlockPos pos, World world)
    {
        if (state.isSolidSide(world, pos, dir))
            return true;
        if (state.isAir())
            return false;


        VoxelShape shape = state.getRenderShape(world, pos);
        final double[] area = {0};
        if (!shape.isEmpty())
        {
            for (AxisAlignedBB bb : shape.toBoundingBoxList())
            {
                switch (dir.getAxis())
                {
                    case X:
                        area[0] += (bb.maxY - bb.minY) * (bb.maxZ - bb.minZ);
                        break;
                    case Y:
                        area[0] += (bb.maxX - bb.minX) * (bb.maxZ - bb.minZ);
                        break;
                    case Z:
                        area[0] += (bb.maxX - bb.minX) * (bb.maxY - bb.minY);
                        break;
                }
                if (area[0] >= 1)
                    return true;
            }
        }
        return false;
    }
}
