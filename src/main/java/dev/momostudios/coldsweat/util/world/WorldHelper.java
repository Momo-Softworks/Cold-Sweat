package dev.momostudios.coldsweat.util.world;

import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.ParticleBatchMessage;
import dev.momostudios.coldsweat.core.network.message.PlaySoundMessage;
import dev.momostudios.coldsweat.core.network.message.SyncForgeDataMessage;
import dev.momostudios.coldsweat.util.ClientOnlyHelper;
import dev.momostudios.coldsweat.util.compat.CompatManager;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class WorldHelper
{
    private WorldHelper() {}

    /**
     * Iterates through every block until it reaches minecraft:air, then returns the Y value<br>
     * Ignores minecraft:cave_air<br>
     * This is different from {@code level.getHeight()} because it attempts to ignore floating blocks
     */
    public static int getHeight(BlockPos pos, Level level)
    {
        // If Minecraft's height calculation is good enough, use that
        int seaLevel = level.getSeaLevel();

        // If chunk isn't loaded, return sea level
        if (!level.isLoaded(pos)) return seaLevel;

        ChunkAccess chunk = level.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.HEIGHTMAPS, false);
        if (chunk == null) return seaLevel;

        return chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX() & 15, pos.getZ() & 15);
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

    /**
     * More accurate method for detecting skylight access. Relies on block hitbox shape instead of light level.
     * @param pos The position to check
     * @param maxDistance The maximum distance to check
     * @return True if the specified position can see the sky (if no full y-axis block faces are within the detection range)
     */
    public static boolean canSeeSky(ChunkAccess chunk, LevelAccessor level, BlockPos pos, int maxDistance)
    {
        BlockPos.MutableBlockPos pos2 = pos.mutable();
        for (int i = 0; i < Math.min(maxDistance, level.getHeight() - pos.getY()); i++)
        {
            pos2.move(0, 1, 0);

            BlockState state = chunk.getBlockState(pos2);
            if (isSpreadBlocked(level, state, pos2, Direction.UP, Direction.UP))
            {   return false;
            }
        }
        return true;
    }

    public static boolean canSeeSky(LevelAccessor level, BlockPos pos, int maxDistance)
    {
        LevelChunk chunk = (LevelChunk) level.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        return chunk == null || canSeeSky(chunk, level, pos, maxDistance);
    }

    public static boolean isSpreadBlocked(LevelAccessor level, BlockState state, BlockPos pos, Direction toDir, Direction fromDir)
    {
        if (state.isAir() || !state.getMaterial().blocksMotion() || state.is(BlockTags.LEAVES)
        || state.is(ModBlocks.HEARTH_BOTTOM) || state.is(ModBlocks.HEARTH_TOP))
        {   return false;
        }
        VoxelShape shape = state.getShape(level, pos, CollisionContext.empty());

        return isFullSide(shape.getFaceShape(fromDir.getOpposite()), fromDir)
            || isFullSide(CSMath.flattenShape(toDir.getAxis(), shape), toDir);
    }

    public static boolean isFullSide(VoxelShape shape, Direction dir)
    {
        if (shape.isEmpty()) return false;
        if (shape.equals(Shapes.block())) return true;

        // Return true if the 2D x/y area of the shape is >= 1
        double[] area = new double[1];
        switch (dir.getAxis())
        {
            case X -> shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> area[0] += (y2 - y1) * (z2 - z1));
            case Y -> shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> area[0] += (x2 - x1) * (z2 - z1));
            case Z -> shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> area[0] += (x2 - x1) * (y2 - y1));
        }

        return area[0] >= 1;
    }

    public static BlockState getBlockState(ChunkAccess chunk, BlockPos blockpos)
    {
        if (chunk == null) return Blocks.AIR.defaultBlockState();

        int x = blockpos.getX();
        int y = blockpos.getY();
        int z = blockpos.getZ();

        try
        {   return getChunkSection(chunk, y).getStates().get(x & 15, y & 15, z & 15);
        }
        catch (Exception e)
        {   return chunk.getBlockState(blockpos);
        }
    }

    public static ChunkAccess getChunk(LevelAccessor level, BlockPos pos)
    {   return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static ChunkAccess getChunk(LevelAccessor level, ChunkPos pos)
    {   return level.getChunkSource().getChunkNow(pos.x, pos.z);
    }

    public static LevelChunkSection getChunkSection(ChunkAccess chunk, int y)
    {   LevelChunkSection[] sections = chunk.getSections();
        return sections[CSMath.clamp(chunk.getSectionIndex(y), 0, sections.length - 1)];
    }

    /**
     * Plays a sound for all tracking clients that follows the source entity around.<br>
     * Why this isn't in Vanilla Minecraft is beyond me
     * @param sound The SoundEvent to play
     * @param entity The entity to attach the sound to (all tracking entities will hear the sound)
     * @param volume The volume of the sound
     * @param pitch The pitch of the sound
     */
    public static void playEntitySound(SoundEvent sound, Entity entity, SoundSource source, float volume, float pitch)
    {
        if (!entity.isSilent())
        {
            if (entity.level.isClientSide)
            {   ClientOnlyHelper.playEntitySound(sound, source, volume, pitch, entity);
            }
            else
            {
                ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                        new PlaySoundMessage(sound.getRegistryName().toString(), source, volume, pitch, entity.getId()));
            }
        }
    }

    public static boolean isInWater(Entity entity)
    {
        BlockPos pos = entity.blockPosition();
        ChunkAccess chunk = entity.level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        return entity.isInWater() || WorldHelper.getBlockState(chunk, pos).is(Blocks.BUBBLE_COLUMN);
    }

    public static boolean isRainingAt(Level level, BlockPos pos)
    {
        Biome biome = level.getBiome(pos).value();
        return level.isRaining() && biome.getPrecipitation() == Biome.Precipitation.RAIN && biome.warmEnoughToRain(pos) && canSeeSky(level, pos, 256)
            || CompatManager.isWeather2RainingAt(level, pos);
    }

    /**
     * Iterates through every block along the given vector
     * @param from The starting position
     * @param to The ending position
     * @param rayTracer function to run on each found block
     * @param maxHits the maximum number of blocks to act upon before the ray expires
     */
    public static void forBlocksInRay(Vec3 from, Vec3 to, Level level, ChunkAccess chunk, TriConsumer<ChunkAccess, BlockState, BlockPos> rayTracer, int maxHits)
    {
        // Don't bother if the ray has no length
        if (!from.equals(to))
        {
            Vec3 ray = to.subtract(from);
            Vec3 normalRay = ray.normalize();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            ChunkAccess workingChunk = chunk;

            // Iterate over every block-long segment of the ray
            for (int i = 0; i < ray.length(); i++)
            {
                // Get the position of the current segment
                Vec3 vec = from.add(normalRay.scale(i));

                // Skip if the position is the same as the last one
                if (new BlockPos(vec).equals(pos)) continue;
                pos.set(vec.x, vec.y, vec.z);

                // Set new workingChunk if the ray travels outside the current one
                if (workingChunk == null || !workingChunk.getPos().equals(new ChunkPos(pos)))
                    workingChunk = level.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);

                if (workingChunk == null) continue;

                // Get the blockstate at the current position
                BlockState state = getBlockState(workingChunk, pos);

                // If the block isn't air, then we hit something
                if (!state.isAir() && --maxHits <= 0)
                    break;

                rayTracer.accept(workingChunk, state, pos);
            }
        }
    }

    public static Entity raycastEntity(Vec3 from, Vec3 to, Level level, Predicate<Entity> filter)
    {
        // Don't bother if the ray has no length
        if (!from.equals(to))
        {
            Vec3 ray = to.subtract(from);
            Vec3 normalRay = ray.normalize();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

            // Iterate over every block-long segment of the ray
            for (int i = 0; i < ray.length(); i++)
            {
                // Get the position of the current segment
                Vec3 vec = from.add(normalRay.scale(i));

                // Skip if the position is the same as the last one
                if (new BlockPos(vec).equals(pos)) continue;
                pos.set(vec.x, vec.y, vec.z);

                // Return the first entity in the current block, or continue if there is none
                List<Entity> entities = level.getEntitiesOfClass(Entity.class, new AABB(pos), filter);
                if (!entities.isEmpty()) return entities.get(0);
            }
        }
        return null;
    }

    public static void spawnParticle(Level level, ParticleOptions particle, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
    {
        if (!level.isClientSide)
        {
            ParticleBatchMessage particles = new ParticleBatchMessage();
            particles.addParticle(particle, new ParticleBatchMessage.ParticlePlacement(x, y, z, xSpeed, ySpeed, zSpeed));
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> (LevelChunk) level.getChunk((int) x >> 4, (int) z >> 4, ChunkStatus.FULL)), particles);
        }
        else
        {
            level.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    public static void spawnParticleBatch(Level level, ParticleOptions particle, double x, double y, double z, double xSpread, double ySpread, double zSpread, double count, double speed)
    {
        Random rand = new Random();

        if (!level.isClientSide)
        {
            ParticleBatchMessage particles = new ParticleBatchMessage();
            for (int i = 0; i < count; i++)
            {
                Vec3 vec = new Vec3(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).normalize().scale(speed);
                particles.addParticle(particle, new ParticleBatchMessage.ParticlePlacement(
                        x + xSpread - rand.nextDouble() * (xSpread * 2),
                        y + ySpread - rand.nextDouble() * (ySpread * 2),
                        z + zSpread - rand.nextDouble() * (zSpread * 2), vec.x, vec.y, vec.z));
            }
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> (LevelChunk) level.getChunk((int) x >> 4, (int) z >> 4, ChunkStatus.FULL)), particles);
        }
        else
        {
            for (int i = 0; i < count; i++)
            {
                Vec3 vec = new Vec3(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).normalize().scale(speed);
                level.addParticle(particle,
                        x + xSpread - rand.nextDouble() * (xSpread * 2),
                        y + ySpread - rand.nextDouble() * (ySpread * 2),
                        z + zSpread - rand.nextDouble() * (zSpread * 2), vec.x, vec.y, vec.z);
            }
        }
    }

    public static ItemEntity entityDropItem(Entity entity, ItemStack stack)
    {
        Random rand = new Random();
        ItemEntity item = entity.spawnAtLocation(stack, entity.getBbHeight());
        if (item != null)
        {   item.setDeltaMovement(item.getDeltaMovement().add(((rand.nextFloat() - rand.nextFloat()) * 0.1F), (rand.nextFloat() * 0.05F), ((rand.nextFloat() - rand.nextFloat()) * 0.1F)));
        }
        return item;
    }

    public static void syncEntityForgeData(Entity entity)
    {
        ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new SyncForgeDataMessage(entity));
    }
}
