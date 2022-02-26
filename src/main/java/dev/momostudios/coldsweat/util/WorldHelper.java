package dev.momostudios.coldsweat.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WorldHelper
{
    /**
     * Iterates through every other block until it reaches minecraft:air, then returns the Y value<br>
     * Ignores minecraft:cave_air<br>
     * This is different from {@code world.getHeight()} because it attempts to ignore blocks that are floating in the air
     */
    public static int getGroundLevel(BlockPos pos, Level world)
    {
        // If Minecraft's height calculation is correct, use that
        int mcHeight = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        if (pos.getY() >= mcHeight)
            return mcHeight;

        for (int c = 0; c < 255; c++)
        {
            BlockPos pos2 = new BlockPos(pos.getX(), c, pos.getZ());
            BlockState state = world.getBlockState(pos2);
            if (state.getMaterial() == Material.AIR && state.getBlock() != Blocks.CAVE_AIR)
            {
                return c;
            }
        }
        return 0;
    }

    /**
     * Gets the average biome temperature in a grid of BlockPos 3 blocks apart<br>
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
                posList.add(pos.offset(sx * interval - (length / 2), 0, sz * interval - (length / 2)));
            }
        }

        return posList;
    }

    public static boolean canSeeSky(Level world, BlockPos pos)
    {
        LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (chunk != null)
        {
            for (int i = 1; i < 255 - y; i++)
            {
                BlockState state = chunk.getBlockState(new BlockPos(x, y + i, z));

                if (state.isAir())
                {
                    continue;
                }

                if (isFullSide(state, Direction.DOWN, pos.above(i), world) || isFullSide(state, Direction.UP, pos.above(i), world))
                    return false;
            }
        }
        return true;
    }

    public static boolean canSpreadThrough(Level world, @Nonnull SpreadPath path, @Nonnull Direction toDir, @Nullable Direction fromDir)
    {
        BlockPos pos = path.getPos();
        LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk != null)
        {
            BlockState state = chunk.getBlockState(pos);

            if (state.isFaceSturdy(world, pos, toDir))
                return false;

            return !isFullSide(state, toDir, pos.relative(toDir), world) && !state.isFaceSturdy(world, pos, toDir.getOpposite());
        }
        return false;
    }

    public static double distance(Vec3i pos1, Vec3i pos2)
    {
        return Math.sqrt(pos1.distSqr(pos2));
    }

    public static boolean isFullSide(BlockState state, Direction dir, BlockPos pos, Level world)
    {
        if (state.isFaceSturdy(world, pos, dir))
            return true;
        if (state.isAir())
            return false;

        VoxelShape shape = state.getShape(world, pos);
        final double[] area = {0};
        if (!shape.isEmpty())
        {
            shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
            {
                if (area[0] < 1)
                    switch (dir.getAxis())
                    {
                        case X:
                            area[0] += (maxY - minY) * (maxZ - minZ);
                            break;
                        case Y:
                            area[0] += (maxX - minX) * (maxZ - minZ);
                            break;
                        case Z:
                            area[0] += (maxX - minX) * (maxY - minY);
                            break;
                    }
            });
            return area[0] >= 1;
        }
        return false;
    }

    public static void schedule(Runnable runnable, int delayTicks)
    {
        new Object()
        {
            private int ticks = 0;

            public void start()
            {
                MinecraftForge.EVENT_BUS.register(this);
            }

            @SubscribeEvent
            public void tick(TickEvent.ServerTickEvent event)
            {
                if (event.phase == TickEvent.Phase.END)
                {
                    ticks++;
                    if (ticks >= delayTicks)
                        run();
                }
            }

            private void run()
            {
                runnable.run();
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }.start();
    }
}
