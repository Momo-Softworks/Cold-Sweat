package dev.momostudios.coldsweat.util.world;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class WorldHelper
{
    /**
     * Iterates through every block until it reaches minecraft:air, then returns the Y value<br>
     * Ignores minecraft:cave_air<br>
     * This is different from {@code level.getHeight()} because it attempts to ignore floating blocks
     */
    public static int getGroundLevel(BlockPos pos, Level level)
    {
        // If Minecraft's height calculation is correct, use that
        int mcHeight = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        if (pos.getY() >= mcHeight)
            return mcHeight;

        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null) return mcHeight;

        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++)
        {
            BlockPos pos2 = new BlockPos(pos.getX(), y, pos.getZ());

            int chunkY = chunk.getSectionIndex(pos2.getY());
            if (chunkY >= 0 && chunkY < chunk.getSections().length)
            {
                // Get the subchunk
                LevelChunkSection levelchunksection = chunk.getSections()[chunkY];

                // If this subchunk is only air, skip it
                if (levelchunksection.hasOnlyAir())
                {
                    y += 16 - (y % 16);
                    continue;
                }

                // Get the block state from this subchunk
                BlockState state = levelchunksection.getBlockState(pos2.getX() & 15, pos2.getY() & 15, pos2.getZ() & 15);
                // If this block is a surface block, return the Y
                if (state.isAir() && state.getBlock() != Blocks.CAVE_AIR)
                {
                    return y;
                }
            }
        }
        return mcHeight;
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
        return canSeeSky(world.getChunk(pos.getX() >> 4, pos.getZ() >> 4), world, pos);
    }

    public static boolean canSeeSky(LevelChunk chunk, Level world, BlockPos pos)
    {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (chunk != null)
        {
            for (int i = 1; i < 319 - y; i++)
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

    public static boolean canSpreadThrough(Level level, BlockPos pos, Direction toDir)
    {
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null) return false;

        return canSpreadThrough(chunk, chunk.getBlockState(pos), pos, toDir);
    }

    public static boolean canSpreadThrough(LevelChunk chunk, @Nonnull BlockPos pos, @Nonnull Direction toDir)
    {
        PalettedContainer<BlockState> palette = chunk.getSection((pos.getY() >> 4) - chunk.getMinSection()).getStates();
        BlockState state = palette.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);

        return canSpreadThrough(chunk, state, pos, toDir);
    }

    public static boolean canSpreadThrough(@Nonnull LevelChunk chunk, BlockState state, @Nonnull BlockPos pos, @Nonnull Direction toDir)
    {
        Level level = chunk.getLevel();

        if (state.isAir() || state.getCollisionShape(level, pos.relative(toDir)).isEmpty())
            return true;

        if (state.isFaceSturdy(level, pos, toDir))
            return false;

        return !isFullSide(state, toDir, pos.relative(toDir), level) && !state.isFaceSturdy(level, pos, toDir.getOpposite());
    }

    public static double distance(Vec3i pos1, Vec3i pos2)
    {
        return Math.sqrt(pos1.distSqr(pos2));
    }

    public static boolean isFullSide(BlockState state, Direction dir, BlockPos pos, Level level)
    {
        if (state.isFaceSturdy(level, pos, dir))
            return true;
        if (state.isAir())
            return false;

        VoxelShape shape = state.getShape(level, pos);
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
        try
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
        catch (Exception e)
        {
            ColdSweat.LOGGER.error("Error scheduling task", e);
            e.printStackTrace();
        }
    }

    public static BlockState getBlockState(LevelChunk chunk, BlockPos blockpos)
    {
        int x = blockpos.getX();
        int y = blockpos.getY();
        int z = blockpos.getZ();
        return chunk.getSection((blockpos.getY() >> 4) - chunk.getMinSection()).getStates().get(x & 15, y & 15, z & 15);
    }

    public static boolean isEvenPosition(BlockPos pos)
    {
        return pos.getX() % 2 == 0 && pos.getY() % 2 == 0 && pos.getZ() % 2 == 0;
    }
}
