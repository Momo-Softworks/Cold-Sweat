package com.momosoftworks.coldsweat.util.world;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.serialization.DynamicHolder;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.BlockDataUpdateMessage;
import com.momosoftworks.coldsweat.core.network.message.ParticleBatchMessage;
import com.momosoftworks.coldsweat.core.network.message.PlaySoundMessage;
import com.momosoftworks.coldsweat.core.network.message.SyncForgeDataMessage;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.serialization.ObjectBuilder;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import oshi.util.tuples.Triplet;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public abstract class WorldHelper
{
    /**
     * Iterates through every block until it reaches minecraft:air, then returns the Y value<br>
     * Ignores minecraft:cave_air<br>
     * This is different from {@code level.getHeight()} because it attempts to ignore floating blocks
     */
    public static int getHeight(BlockPos pos, Level level)
    {   // If Minecraft's height calculation is good enough, use that
        int seaLevel = level.getSeaLevel();
        // If chunk isn't loaded, return sea level
        if (!level.isLoaded(pos)) return seaLevel;

        ChunkAccess chunk = getChunk(level, pos);
        if (chunk == null) return seaLevel;

        return chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX() & 15, pos.getZ() & 15);
    }

    public static ResourceLocation getBiomeID(Biome biome)
    {   ResourceLocation biomeID = ForgeRegistries.BIOMES.getKey(biome);
        if (biomeID == null) biomeID = RegistryHelper.getRegistry(Registry.BIOME_REGISTRY).getKey(biome);

        return biomeID;
    }

    public static Biome getBiome(ResourceLocation biomeID)
    {   Biome biome = ForgeRegistries.BIOMES.getValue(biomeID);
        if (biome == null) biome = RegistryHelper.getRegistry(Registry.BIOME_REGISTRY).get(biomeID);

        return biome;
    }

    public static ResourceLocation getDimensionTypeID(DimensionType dimType)
    {   return RegistryHelper.getRegistry(Registry.DIMENSION_TYPE_REGISTRY).getKey(dimType);
    }

    public static DimensionType getDimensionType(ResourceLocation dimID)
    {   return RegistryHelper.getRegistry(Registry.DIMENSION_TYPE_REGISTRY).get(dimID);
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
     * Returns a cube of BlockPos of the specified size and density
     * @param pos The center of the cube
     * @param size The side length of the cube, in intervals
     * @param interval The length of one interval (distance between each BlockPos)
     * @return A list of BlockPos
     */
    public static List<BlockPos> getPositionCube(BlockPos pos, int size, int interval)
    {
        List<BlockPos> posList = new ArrayList<>();
        int radius = (size * interval) / 2;

        for (int x = -radius; x < radius; x += interval)
        {
            for (int y = -radius; y < radius; y += interval)
            {
                for (int z = -radius; z < radius; z += interval)
                {   posList.add(pos.offset(x + interval / 2, y + interval / 2, z + interval / 2));
                }
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
    public static boolean canSeeSky(LevelAccessor level, BlockPos pos, int maxDistance)
    {   BlockPos.MutableBlockPos pos2 = pos.mutable();
        int iterations = Math.min(maxDistance, level.getMaxBuildHeight() - pos.getY());
        ChunkAccess chunk = getChunk(level, pos);
        if (chunk == null) return true;

        for (int i = 0; i < iterations; i++)
        {
            BlockState state = chunk.getBlockState(pos2);
            VoxelShape shape = state.getShape(level, pos, CollisionContext.empty());
            if (Block.isShapeFullBlock(shape)) return false;

            if (isFullSide(CSMath.flattenShape(Direction.Axis.Y, shape), Direction.UP))
            {   return false;
            }
            pos2.move(0, 1, 0);
        }

        return true;
    }

    public static boolean isSpreadBlocked(LevelAccessor level, BlockState state, BlockPos pos, Direction toDir, Direction fromDir)
    {
        Block block = state.getBlock();
        if (state.isAir() || ConfigSettings.HEARTH_SPREAD_WHITELIST.get().contains(block)
        || block == ModBlocks.HEARTH_BOTTOM || block == ModBlocks.HEARTH_TOP)
        {   return false;
        }
        if (ConfigSettings.HEARTH_SPREAD_BLACKLIST.get().contains(block)) return true;

        VoxelShape shape = state.getShape(level, pos, CollisionContext.empty());
        if (Block.isShapeFullBlock(shape)) return true;

               // Should it have spread here in the first place?
        return isFullSide(shape.getFaceShape(fromDir.getOpposite()), fromDir)
               // Can it spread out?
            || isFullSide(CSMath.flattenShape(toDir.getAxis(), shape), toDir);
    }

    public static boolean isFullSide(VoxelShape shape, Direction dir)
    {
        if (shape.isEmpty()) return false;

        // Return true if the 2D x/y area of the shape is >= 1
        double[] area = new double[1];
        switch (dir.getAxis())
        {
            case X -> shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> area[0] += Math.abs(y2 - y1) * Math.abs(z2 - z1));
            case Y -> shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> area[0] += Math.abs(x2 - x1) * Math.abs(z2 - z1));
            case Z -> shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> area[0] += Math.abs(x2 - x1) * Math.abs(y2 - y1));
        }

        return area[0] >= 1;
    }

    @Nullable
    public static ChunkAccess getChunk(LevelAccessor level, BlockPos pos)
    {   return getChunk(level, pos.getX() >> 4, pos.getZ() >> 4);
    }

    @Nullable
    public static ChunkAccess getChunk(LevelAccessor level, ChunkPos pos)
    {   return getChunk(level, pos.x, pos.z);
    }

    @Nullable
    public static ChunkAccess getChunk(LevelAccessor level, int chunkX, int chunkZ)
    {   return level.getChunkSource().getChunkNow(chunkX, chunkZ);
    }

    public static LevelChunkSection getChunkSection(ChunkAccess chunk, int y)
    {   LevelChunkSection[] sections = chunk.getSections();

        return sections[CSMath.clamp(chunk.getSectionIndex(y), 0, sections.length - 1)];
    }

    @Nullable
    public static ConfiguredStructureFeature<?,?> getStructureAt(Level level, BlockPos pos)
    {
        if (!(level instanceof ServerLevel serverLevel)) return null;

        StructureFeatureManager structureManager = serverLevel.structureFeatureManager();

        // Iterate over all structures at the position (ignores Y level)
        for (Map.Entry<ConfiguredStructureFeature<?, ?>, LongSet> entry : structureManager.getAllStructuresAt(pos).entrySet())
        {
            ConfiguredStructureFeature<?, ?> structure = entry.getKey();
            LongSet strucCoordinates = entry.getValue();

            // Iterate over all chunk coordinates within the structures
            for (long coordinate : strucCoordinates)
            {
                SectionPos sectionpos = SectionPos.of(new ChunkPos(coordinate), level.getMinSection());
                // Get the structure start
                StructureStart structurestart = structureManager.getStartForFeature(sectionpos, structure, level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_STARTS));

                if (structurestart != null && structurestart.isValid() && structureManager.structureHasPieceAt(pos, structurestart))
                {   return structure;
                }
            }
        }
        return null;
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
            {   ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                        new PlaySoundMessage(ForgeRegistries.SOUND_EVENTS.getKey(sound).toString(), source, volume, pitch, entity.getId()));
            }
        }
    }

    public static boolean isInWater(Entity entity)
    {   BlockPos pos = entity.blockPosition();
        ChunkAccess chunk = WorldHelper.getChunk(entity.level, pos);
        if (chunk == null) return false;

        return entity.isInWater() || chunk.getBlockState(pos).getBlock() == Blocks.BUBBLE_COLUMN;
    }

    public static boolean isRainingAt(Level level, BlockPos pos)
    {   var biome = DynamicHolder.create(() -> level.getBiomeManager().getBiome(pos).value());

        return level.isRaining() && biome.get().getPrecipitation() == Biome.Precipitation.RAIN
            && biome.get().warmEnoughToRain(pos) && canSeeSky(level, pos.above(), 256)
            || CompatManager.isRainingAt(level, pos);
    }

    /**
     * Iterates through every block along the given vector
     * @param from The starting position
     * @param to The ending position
     * @param rayTracer function to run on each found block
     * @param maxHits the maximum number of blocks to act upon before the ray expires
     */
    public static void forBlocksInRay(Vec3 from, Vec3 to, Level level, ChunkAccess chunk, Map<BlockPos, BlockState> stateCache, BiConsumer<BlockState, BlockPos> rayTracer, int maxHits)
    {
        // Don't bother if the ray has no length
        if (!from.equals(to))
        {
            Vec3 ray = to.subtract(from);
            Vec3 normalRay = ray.normalize();
            BlockPos.MutableBlockPos pos = new BlockPos(from).mutable();
            ChunkAccess workingChunk = chunk;

            // Iterate over every block-long segment of the ray
            for (int i = 0; i < ray.length(); i++)
            {
                // Get the position of the current segment
                Vec3 vec = from.add(normalRay.scale(i));

                // Skip if the position is the same as the last one
                if (new BlockPos(vec).equals(pos)) continue;
                pos.set(vec.x, vec.y, vec.z);

                // Get the blockstate at the current position
                BlockState state = stateCache.get(pos);

                if (state == null)
                {   // Set new workingChunk if the ray travels outside the current one
                    if (workingChunk == null || !workingChunk.getPos().equals(new ChunkPos(pos)))
                    {   workingChunk = getChunk(level, pos);
                    }
                    if (workingChunk == null) continue;

                    state = workingChunk.getBlockState(pos);
                    stateCache.put(pos.immutable(), state);
                }


                // If the block isn't air, then we hit something
                if (!state.isAir() && --maxHits <= 0)
                {   break;
                }

                rayTracer.accept(state, pos);
            }
        }
    }

    public static BlockPos raycastBlock(Vec3 from, Vec3 rotation, double distance, Level level)
    {
        Vec3 to = from.add(rotation.normalize().scale(distance));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Don't bother if the ray has no length
        if (!from.equals(to))
        {
            Vec3 ray = to.subtract(from);
            Vec3 normalRay = ray.normalize();

            // Iterate over every block-long segment of the ray
            for (int i = 0; i < ray.length(); i++)
            {
                // Get the position of the current segment
                Vec3 vec = from.add(normalRay.scale(i));

                // Skip if the position is the same as the last one
                if (new BlockPos(vec).equals(pos)) continue;
                pos.set(vec.x, vec.y, vec.z);

                // Return the first non-air block in the current block, or continue if there is none
                if (!level.getBlockState(pos).isAir()) return pos;
            }
        }
        return pos;
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
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> (LevelChunk) getChunk(level, (int) x >> 4, (int) z >> 4)), particles);
        }
        else
        {   level.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
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
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), particles);
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
    {   return entityDropItem(entity, stack, 6000);
    }

    public static ItemEntity entityDropItem(Entity entity, ItemStack stack, int lifeTime)
    {
        Random rand = new Random();
        ItemEntity item = entity.spawnAtLocation(stack, entity.getBbHeight());
        if (item != null)
        {   item.setDeltaMovement(item.getDeltaMovement().add(((rand.nextFloat() - rand.nextFloat()) * 0.1F), (rand.nextFloat() * 0.05F), ((rand.nextFloat() - rand.nextFloat()) * 0.1F)));
            Field age = ObfuscationReflectionHelper.findField(ItemEntity.class, "f_31985_");
            age.setAccessible(true);
            try
            {   age.set(item, 6000 - lifeTime);
            }
            catch (Exception e)
            {   e.printStackTrace();
            }
        }
        return item;
    }

    public static void syncEntityForgeData(Entity entity, ServerPlayer destination)
    {
        ColdSweatPacketHandler.INSTANCE.send(destination != null ? PacketDistributor.PLAYER.with(() -> destination)
                                                                 : PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                                             new SyncForgeDataMessage(entity));
    }

    public static void syncBlockEntityData(BlockEntity be)
    {
        if (be.getLevel() == null || be.getLevel().isClientSide) return;

        ChunkAccess ichunk = getChunk(be.getLevel(), be.getBlockPos());
        if (ichunk instanceof LevelChunk chunk)
        {   ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new BlockDataUpdateMessage(be));
        }
    }

    /**
     * Allows the server world to be accessed from any thread
     */
    public static ServerLevel getServerLevel(Level level)
    {   return ServerLifecycleHooks.getCurrentServer().getLevel(level.dimension());
    }

    public static MinecraftServer getServer()
    {   return ServerLifecycleHooks.getCurrentServer();
    }

    public static Pair<Double, Double> getBiomeTemperature(Holder<Biome> biome)
    {
        double biomeTemp = biome.value().getBaseTemperature();
        Triplet<Double, Double, Temperature.Units> tempConfig = CSMath.orElse(ConfigSettings.BIOME_TEMPS.get().get(biome.value()),
                                                                              ObjectBuilder.build(() ->
                                                                              {
                                                                                  Triplet<Double, Double, Temperature.Units> offset = ConfigSettings.BIOME_OFFSETS.get().get(biome.value());
                                                                                  if (offset == null) return null;
                                                                                  return new Triplet<>(biomeTemp + offset.getA(),
                                                                                                       biomeTemp + offset.getB(),
                                                                                                       Temperature.Units.MC);
                                                                              }),
                                                                              new Triplet<>(biomeTemp, biomeTemp, Temperature.Units.MC));
        return Pair.of(tempConfig.getA(), tempConfig.getB());
    }
}
