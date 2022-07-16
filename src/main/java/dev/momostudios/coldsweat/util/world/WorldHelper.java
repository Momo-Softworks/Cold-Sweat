package dev.momostudios.coldsweat.util.world;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.PlaySoundMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class WorldHelper
{
    /**
     * Iterates through every block until it reaches minecraft:air, then returns the Y value<br>
     * Ignores minecraft:cave_air<br>
     * This is different from {@code level.getHeight()} because it attempts to ignore floating blocks
     */
    public static int getGroundLevel(BlockPos pos, Level level)
    {
        // If Minecraft's height calculation is good enough, use that
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
                LevelChunkSection subchunk = chunk.getSections()[chunkY];
                if (subchunk == null) return mcHeight;

                // If this subchunk is only air, skip it
                if (subchunk.hasOnlyAir())
                {
                    y += 16 - (y % 16);
                    continue;
                }

                // Get the block state from this subchunk
                BlockState state = subchunk.getBlockState(pos2.getX() & 15, pos2.getY() & 15, pos2.getZ() & 15);
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
        LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        return chunk == null || canSeeSky(chunk, world, pos);
    }

    public static boolean canSeeSky(LevelChunk chunk, Level level, BlockPos pos)
    {
        Vec3 startPos = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        ClipContext clipContext = new ClipContext(startPos, startPos.add(0, level.getHeight() - startPos.y, 0), ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, null);
        return !gatherRTResults(clipContext, (ctx, bpos) ->
        {
            BlockState rayState = chunk.getBlockState(bpos);
            return isFullSide(rayState, Direction.DOWN, bpos, level) || isFullSide(rayState, Direction.UP, bpos, level);
        }).contains(true);
    }

    public static boolean isSpreadBlocked(Level level, BlockPos pos, Direction toDir)
    {
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        return chunk == null || isSpreadBlocked(level, chunk.getBlockState(pos), pos, toDir);
    }

    public static boolean isSpreadBlocked(LevelChunk chunk, BlockPos pos, Direction toDir)
    {
        return isSpreadBlocked(chunk.getLevel(), chunk.getBlockState(pos), pos, toDir);
    }

    public static boolean isSpreadBlocked(Level level, BlockState state, BlockPos pos, Direction toDir)
    {
        BlockPos offsetPos = pos.relative(toDir);

        if (state.isAir() || state.getCollisionShape(level, offsetPos).isEmpty())
            return false;

        if (state.isFaceSturdy(level, pos, toDir))
            return true;

        return isFullSide(state, toDir, offsetPos, level) || state.isFaceSturdy(level, pos, toDir.getOpposite());
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
                        case X -> area[0] += (maxY - minY) * (maxZ - minZ);
                        case Y -> area[0] += (maxX - minX) * (maxZ - minZ);
                        case Z -> area[0] += (maxX - minX) * (maxY - minY);
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

        try
        {
            return chunk.getSection((blockpos.getY() >> 4) - chunk.getMinSection()).getStates().get(x & 15, y & 15, z & 15);
        }
        catch (Exception e) { return Blocks.AIR.defaultBlockState(); }
    }

    /**
     * Plays a sound for all tracking clients that follows the source entity around.<br>
     * Why this isn't in Vanilla Minecraft is beyond me
     * @param sound The SoundEvent to play
     * @param entity The entity to attach the sound to (all tracking entities will hear the sound)
     * @param volume The volume of the sound
     * @param pitch The pitch of the sound
     */
    public static void playEntitySound(SoundEvent sound, SoundSource source, Entity entity, float volume, float pitch)
    {
        ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new PlaySoundMessage(sound.getRegistryName().toString(), source, volume, pitch, entity.getId()));
    }

    public static <T> List<T> gatherRTResults(ClipContext context, BiFunction<ClipContext, BlockPos, T> rayTracer)
    {
        List<T> rayTraces = new ArrayList<>();
        Vec3 startVec = context.getFrom();
        Vec3 endVec = context.getTo();

        if (!startVec.equals(endVec))
        {
            double d0 = Mth.lerp(-1.0E-7D, endVec.x, startVec.x);
            double d1 = Mth.lerp(-1.0E-7D, endVec.y, startVec.y);
            double d2 = Mth.lerp(-1.0E-7D, endVec.z, startVec.z);
            double d3 = Mth.lerp(-1.0E-7D, startVec.x, endVec.x);
            double d4 = Mth.lerp(-1.0E-7D, startVec.y, endVec.y);
            double d5 = Mth.lerp(-1.0E-7D, startVec.z, endVec.z);
            int i = Mth.floor(d3);
            int j = Mth.floor(d4);
            int k = Mth.floor(d5);
            BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos(i, j, k);

            double d6 = d0 - d3;
            double d7 = d1 - d4;
            double d8 = d2 - d5;
            int l = Mth.sign(d6);
            int i1 = Mth.sign(d7);
            int j1 = Mth.sign(d8);
            double d9 = l == 0 ? Double.MAX_VALUE : (double) l / d6;
            double d10 = i1 == 0 ? Double.MAX_VALUE : (double) i1 / d7;
            double d11 = j1 == 0 ? Double.MAX_VALUE : (double) j1 / d8;
            double d12 = d9 * (l > 0 ? 1.0D - Mth.frac(d3) : Mth.frac(d3));
            double d13 = d10 * (i1 > 0 ? 1.0D - Mth.frac(d4) : Mth.frac(d4));
            double d14 = d11 * (j1 > 0 ? 1.0D - Mth.frac(d5) : Mth.frac(d5));

            while (d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D)
            {
                if (d12 < d13)
                {
                    if (d12 < d14)
                    {
                        i += l;
                        d12 += d9;
                    }
                    else
                    {
                        k += j1;
                        d14 += d11;
                    }
                }
                else if (d13 < d14)
                {
                    j += i1;
                    d13 += d10;
                }
                else
                {
                    k += j1;
                    d14 += d11;
                }

                T result1 = rayTracer.apply(context, blockpos$mutable.set(i, j, k));
                if (result1 != null)
                {
                    rayTraces.add(result1);
                }
            }
        }
        return rayTraces;
    }
}
