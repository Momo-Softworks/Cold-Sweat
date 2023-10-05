package com.momosoftworks.coldsweat.util.world;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.Direction;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import javax.annotation.Nullable;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WorldHelper
{
    /**
     * Iterates through every block until it reaches minecraft:air, then returns the Y value<br>
     * Ignores minecraft:cave_air<br>
     * This is different from {@code level.getHeight()} because it attempts to ignore floating blocks
     */
    public static int getHeight(BlockPos pos, World world)
    {
        // If Minecraft's height calculation is good enough, use that
        int seaLevel = 63;

        // If chunk isn't loaded, return sea level
        if (!world.getChunkProvider().chunkExists(pos.getX(), pos.getZ())) return seaLevel;

        Chunk chunk = getChunk(world, pos);
        if (chunk == null) return seaLevel;

        return chunk.getHeightValue(pos.getX() & 15, pos.getZ() & 15);
    }

    /**
     * Returns all block positions in a grid of the specified size<br>
     * Search area scales with the number of samples
     * @param pos The center of the search area
     * @param samples The total number of checks performed.
     * @param interval How far apart each check is. Higher values = less dense and larger search area
     */
    public static List<BlockPos> getPositionGrid(BlockPos pos, int samples, int interval)
    {
        List<BlockPos> posList = new ArrayList<>();
        int sampleRoot = (int) Math.sqrt(samples);
        int radius = (sampleRoot * interval) / 2;

        for (int x = -radius; x < radius; x += interval)
        {
            for (int z = -radius; z < radius; z += interval)
            {   posList.add(pos.offset(x + interval / 2, 0, z + interval / 2));
            }
        }

        return posList;
    }


    public static boolean canSeeSky(World world, BlockPos pos, int maxDistance)
    {
        BlockPos.Mutable pos2 = pos.mutable();
        int iterations = Math.min(maxDistance, world.getActualHeight() - pos.getY());
        Chunk chunk = getChunk(world, pos);
        if (chunk == null) return true;
        for (int i = 0; i < iterations; i++)
        {
            BlockState state = WorldHelper.getBlockState(chunk, pos2);
            VoxelShape shape = getBlockShape(state);
            if (shape.isFullCube()) return false;

            if (isFullSide(shape.getFaceShape(Direction.Axis.Y), Direction.UP))
            {   return false;
            }

            pos2.move(0, 1, 0);
        }
        return true;
    }

    public static VoxelShape getBlockShape(BlockState state)
    {
        Block block = state.getBlock();
        return new VoxelShape(block.getBlockBoundsMinX(), block.getBlockBoundsMinY(), block.getBlockBoundsMinZ(),
                block.getBlockBoundsMaxX(), block.getBlockBoundsMaxY(), block.getBlockBoundsMaxZ());
    }

    public static boolean isSpreadBlocked(BlockState state, Direction toDir,Direction fromDir)
    {
        Block block = state.getBlock();
        if (state.isAir() || !state.getBlock().getMaterial().blocksMovement() || ConfigSettings.HEARTH_SPREAD_WHITELIST.get().contains(block))
        // TODO: 9/24/23 Add this back when blocks are registered
        //|| block == ModBlocks.HEARTH_BOTTOM || block == ModBlocks.HEARTH_TOP)
        {   return false;
        }
        if (ConfigSettings.HEARTH_SPREAD_BLACKLIST.get().contains(block)) return true;

        VoxelShape shape = getBlockShape(state);
        if (shape.isFullCube())
            return true;

        // Should it have spread here in the first place?
        return isFullSide(shape.getFaceShape(fromDir.getOpposite()), fromDir)
               // Can it spread out?
            || isFullSide(shape.getFaceShape(toDir.getAxis()), toDir);
    }

    public static boolean isFullSide(AxisAlignedBB shape, Direction dir)
    {
        if (shape.equals(AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1))) return true;

        switch (dir.getAxis())
        {
            case X : return (shape.maxY - shape.minY) == 1 && (shape.maxZ - shape.minZ) == 1;
            case Y : return (shape.maxX - shape.minX) == 1 && (shape.maxZ - shape.minZ) == 1;
            case Z : return (shape.maxX - shape.minX) == 1 && (shape.maxY - shape.minY) == 1;
        }
        return false;
    }

    public static Block getBlock(Chunk chunk, BlockPos blockpos)
    {
        int y = blockpos.getY();
        return getChunkSection(chunk, y).getBlockByExtId(blockpos.getX() & 15, y & 15, blockpos.getZ() & 15);
    }

    @Nullable
    public static Chunk getChunk(World world, BlockPos pos)
    {   return getChunk(world, pos.getX() >> 4, pos.getZ() >> 4);
    }

    @Nullable
    public static Chunk getChunk(World level, ChunkPos pos)
    {   return getChunk(level, pos.x, pos.z);
    }

    @Nullable
    public static Chunk getChunk(World world, int chunkX, int chunkZ)
    {   return world.getChunkFromChunkCoords(chunkX, chunkZ);
    }

    public static ExtendedBlockStorage getChunkSection(Chunk chunk, int y)
    {   return chunk.getBlockStorageArray()[y >> 4];
    }

    public static Block getBlock(Chunk chunk, int x, int y, int z)
    {   return getChunkSection(chunk, y).getBlockByExtId(x & 15, y & 15, z & 15);
    }

    public static Block getBlock(World world, BlockPos pos)
    {   Chunk chunk = getChunk(world, pos);
        if (chunk == null) return Blocks.air;
        return getBlock(chunk, pos);
    }

    public static BlockState getBlockState(Chunk chunk, BlockPos pos)
    {   int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (y < 0 || y >= 256) return BlockState.of(Blocks.air, 0);
        return BlockState.of(chunk.getBlock(x & 15, y, z & 15), chunk.getBlockMetadata(x & 15, y, z & 15));
    }

    public static BlockState getBlockState(World world, BlockPos pos)
    {   return BlockState.of(world.getBlock(pos.getX(), pos.getY(), pos.getZ()), world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ()));
    }

    public static AxisAlignedBB getBox(BlockPos pos)
    {   return AxisAlignedBB.getBoundingBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    public static boolean isInWater(Entity entity)
    {   return entity.isInWater();
    }

    public static boolean isRainingAt(World world, BlockPos pos)
    {
        BiomeGenBase biome = world.getBiomeGenForCoords(pos.getX(), pos.getZ());
        return world.isRaining() && !biome.getEnableSnow() && biome.temperature > 0.15f && canSeeSky(world, pos.up(), 256)
            || CompatManager.isWeather2RainingAt(world, pos);
    }

    // TODO: 9/24/23 Add this later
    ///**
    // * Plays a sound for all tracking clients that follows the source entity around.<br>
    // * Why this isn't in Vanilla Minecraft is beyond me
    // * @param sound The SoundEvent to play
    // * @param entity The entity to attach the sound to (all tracking entities will hear the sound)
    // * @param volume The volume of the sound
    // * @param pitch The pitch of the sound
    // */
    //public static void playEntitySound(SoundEvent sound, Entity entity, SoundCategory source, float volume, float pitch)
    //{
    //    ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
    //            new PlaySoundMessage(sound.getRegistryName().toString(), source, volume, pitch, entity.getId()));
    //}

    /**
     * Iterates through every block along the given vector
     * @param from The starting position
     * @param to The ending position
     * @param rayTracer function to run on each found block
     * @param maxHits the maximum number of blocks to act upon before the ray expires
     */
    public static void forBlocksInRay(Vec3 from, Vec3 to, World world, Chunk chunk, Map<BlockPos, BlockState> stateCache, BiConsumer<BlockState, BlockPos> rayTracer, int maxHits)
    {
        // Don't bother if the ray has no length
        if (!from.equals(to))
        {
            Vec3 ray = from.subtract(to);
            Vec3 normalRay = ray.normalize();
            BlockPos.Mutable pos = new BlockPos(from).mutable();
            Chunk workingChunk = chunk;

            // Iterate over every block-long segment of the ray
            for (int i = 0; i < ray.lengthVector(); i++)
            {
                // Get the position of the current segment
                Vec3 vec = from.addVector(normalRay.xCoord * i, normalRay.yCoord * i, normalRay.zCoord * i);

                // Skip if the position is the same as the last one
                if (new BlockPos(vec).equals(pos)) continue;
                pos.set(vec.xCoord, vec.yCoord, vec.zCoord);

                // Get the blockstate at the current position
                BlockState state = stateCache.get(pos);

                if (state == null)
                {   // Set new workingChunk if the ray travels outside the current one
                    if (workingChunk == null || !new ChunkPos(workingChunk).equals(new ChunkPos(pos)))
                    {   workingChunk = getChunk(world, pos);
                    }

                    if (workingChunk == null) continue;
                    state = getBlockState(workingChunk, pos);
                    stateCache.put(pos.immutable(), state);
                }

                // If the block isn't air, then we hit something
                if (!state.isAir())
                {
                    rayTracer.accept(state, pos);
                    if (--maxHits <= 0)
                    {   break;
                    }
                }
            }
        }
    }

    public static Entity raycastEntity(Vec3 from, Vec3 to, World world, Predicate<Entity> filter)
    {
        // Don't bother if the ray has no length
        if (!from.equals(to))
        {
            Vec3 ray = from.subtract(to);
            Vec3 normalRay = ray.normalize();
            BlockPos.Mutable pos = new BlockPos.Mutable();

            // Iterate over every block-long segment of the ray
            for (int i = 0; i < ray.lengthVector(); i++)
            {
                // Get the position of the current segment
                Vec3 vec = from.addVector(normalRay.xCoord * i, normalRay.yCoord * i, normalRay.zCoord * i);

                // Skip if the position is the same as the last one
                if (new BlockPos(vec).equals(pos)) continue;
                pos.set(vec.xCoord, vec.yCoord, vec.zCoord);

                // Return the first entity in the current block, or continue if there is none
                List<Entity> entities = (List<Entity>) world.getEntitiesWithinAABB(Entity.class, getBox(pos)).stream().filter(filter).collect(Collectors.toList());
                if (!entities.isEmpty()) return entities.get(0);
            }
        }
        return null;
    }
}
