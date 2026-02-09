package com.momosoftworks.coldsweat.util.world;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.api.temperature.modifier.*;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.BlockDataUpdateMessage;
import com.momosoftworks.coldsweat.core.network.message.ParticleBatchMessage;
import com.momosoftworks.coldsweat.core.network.message.PlayEntityAttachedSoundMessage;
import com.momosoftworks.coldsweat.core.network.message.SyncForgeDataMessage;
import com.momosoftworks.coldsweat.data.codec.configuration.BiomeTempData;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.entity.DummyEntity;
import com.momosoftworks.coldsweat.util.entity.DummyPlayer;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.DynamicHolder;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.IParticleData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Mod.EventBusSubscriber
public abstract class WorldHelper
{
    static Map<RegistryKey<World>, DummyPlayer> DUMMY_PLAYERS = new HashMap<>();
    static Map<RegistryKey<World>, DummyEntity> DUMMY_ENTITIES = new HashMap<>();
    static Map<RegistryKey<World>, Map<BlockPos, TempSnapshot>> TEMPERATURE_CHECKS = new HashMap<>();

    @SubscribeEvent
    public static void clearCachesOnUnload(FMLServerStoppedEvent event)
    {   DUMMY_PLAYERS.clear();
        DUMMY_ENTITIES.clear();
        TEMPERATURE_CHECKS.clear();
    }

    public static int getHeight(BlockPos pos, World level, Heightmap.Type heightmap)
    {
        int minHeight = 0;
        int maxHeight = level.getMaxBuildHeight();
        int seaLevel = level.getSeaLevel();
        // If chunk isn't loaded, return sea level
        if (!level.isLoaded(pos)) return seaLevel;

        IChunk chunk = getChunk(level, pos);
        if (chunk == null) return seaLevel;

        if (level.isClientSide())
        {
            int y = level.getMaxBuildHeight();
            BlockPos.Mutable mutable = pos.mutable();
            mutable.setY(CSMath.clamp(mutable.getY(), minHeight, maxHeight));
            BlockState state = null;

            for (; state == null || !heightmap.isOpaque().test(state); y--)
            {
                if (!CSMath.betweenInclusive(mutable.getY(), minHeight, maxHeight))
                {   return seaLevel;
                }
                mutable.setY(y);
                state = chunk.getBlockState(mutable);
            }
            return y;
        }
        else return chunk.getHeight(heightmap, pos.getX(), pos.getZ());
    }

    public static int getHeight(BlockPos pos, World level)
    {   return getHeight(pos, level, Heightmap.Type.MOTION_BLOCKING);
    }

    public static int getAverageHeight(BlockPos pos, World level, Heightmap.Type... heightmaps)
    {
        int totalHeight = 0;
        for (Heightmap.Type heightmap : heightmaps)
        {   totalHeight += getHeight(pos, level, heightmap);
        }
        return totalHeight / heightmaps.length;
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
        int halfInterval = interval / 2;

        for (int x = -radius + halfInterval; x < radius + halfInterval; x += interval)
        {
            for (int y = -radius + halfInterval; y < radius + halfInterval; y += interval)
            {
                for (int z = -radius + halfInterval; z < radius + halfInterval; z += interval)
                {   posList.add(pos.offset(x, y, z));
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
    public static boolean canSeeSky(IWorld level, BlockPos pos, int maxDistance)
    {
        BlockPos.Mutable pos2 = pos.mutable();
        int iterations = Math.min(maxDistance, level.getMaxBuildHeight() - pos.getY());
        IChunk chunk = getChunk(level, pos);
        if (chunk == null) return true;

        for (int i = 0; i < iterations; i++)
        {
            try
            {
                BlockState state = chunk.getBlockState(pos2);
                if (ConfigSettings.THERMAL_SOURCE_SPREAD_BLACKLIST.get().contains(state.getBlock()))
                {   return false;
                }
                if (state.isAir() || state.getMaterial().isLiquid() || ConfigSettings.THERMAL_SOURCE_SPREAD_WHITELIST.get().contains(state.getBlock()))
                {   continue;
                }
                VoxelShape shape = state.getShape(level, pos, ISelectionContext.empty());
                if (shape.equals(VoxelShapes.block())) return false;

                if (isFullSide(CSMath.flattenShape(Direction.Axis.Y, shape), Direction.UP))
                {   return false;
                }
            }
            finally
            {   pos2.move(0, 1, 0);
            }
        }
        return true;
    }

    public static boolean isSpreadBlocked(IWorld world, BlockState state, BlockPos pos, Direction fromDir, Direction toDir)
    {
        Block block = state.getBlock();

        if (state.isAir() || ConfigSettings.THERMAL_SOURCE_SPREAD_WHITELIST.get().contains(block))
        {   return false;
        }
        if (ConfigSettings.THERMAL_SOURCE_SPREAD_BLACKLIST.get().contains(block)) return true;

        VoxelShape shape = state.getCollisionShape(world, pos, ISelectionContext.empty());
        if (shape.equals(VoxelShapes.block())) return true;

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
            case X : shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> area[0] += Math.abs(y2 - y1) * Math.abs(z2 - z1));
            case Y : shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> area[0] += Math.abs(x2 - x1) * Math.abs(z2 - z1));
            case Z : shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> area[0] += Math.abs(x2 - x1) * Math.abs(y2 - y1));
        }

        return area[0] >= 1;
    }

    @Nullable
    public static IChunk getChunk(IWorld level, BlockPos pos)
    {   return getChunk(level, pos.getX() >> 4, pos.getZ() >> 4);
    }

    @Nullable
    public static IChunk getChunk(IWorld level, ChunkPos pos)
    {   return getChunk(level, pos.x, pos.z);
    }

    @Nullable
    public static IChunk getChunk(IWorld level, int chunkX, int chunkZ)
    {   return level.getChunkSource().getChunkNow(chunkX, chunkZ);
    }

    public static ChunkSection getChunkSection(IChunk chunk, int y)
    {   ChunkSection[] sections = chunk.getSections();

        return sections[CSMath.clamp(y >> 4, 0, sections.length - 1)];
    }

    public static Optional<StructureFeature<?, ?>> getStructureAt(World level, BlockPos pos)
    {
        if (!(level instanceof ServerWorld) || !level.isLoaded(pos)) return Optional.empty();

        ServerWorld serverLevel = ((ServerWorld) level);
        StructureManager structureManager = serverLevel.structureFeatureManager();

        // Iterate over all structures at the position (ignores Y level)
        for (Map.Entry<Structure<?>, LongSet> entry : level.getChunk(pos).getAllReferences().entrySet())
        {
            Structure<?> structure = entry.getKey();
            LongSet strucCoordinates = entry.getValue();

            // Iterate over all chunk coordinates within the structures
            for (long coordinate : strucCoordinates)
            {
                SectionPos sectionpos = SectionPos.of(new ChunkPos(coordinate), SectionPos.blockToSectionCoord(0));
                // Get the structure start
                StructureStart<?> structurestart = structureManager.getStartForFeature(sectionpos, structure, level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_STARTS));

                if (structurestart != null && structurestart.isValid() && structurestart.getBoundingBox().isInside(pos))
                {
                    // If the structure has a piece at the position, get the temperature
                    if (structurestart.getPieces().stream().anyMatch(piece -> piece.getBoundingBox().isInside(pos)))
                    {
                        ResourceLocation structureId = structure.delegate.name();
                        if (structureId == null)
                        {   return Optional.empty();
                        }
                        return Optional.ofNullable(serverLevel.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY).get(Registry.STRUCTURE_FEATURE.getKey(structure)));
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Plays a sound for all tracking clients that follows the source entity around.<br>
     * Why this isn't in Vanilla Minecraft is beyond me
     * @param sound The SoundEvent to play
     * @param entity The entity to attach the sound to (all tracking entities will hear the sound)
     * @param volume The volume of the sound
     * @param pitch The pitch of the sound
     */
    public static void playEntitySound(SoundEvent sound, Entity entity, SoundCategory source, float volume, float pitch)
    {
        if (!entity.isSilent())
        {
            if (entity.level.isClientSide)
            {   ClientOnlyHelper.playEntitySound(sound, source, volume, pitch, entity);
            }
            else
            {   ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                        new PlayEntityAttachedSoundMessage(sound, source, volume, pitch, entity.getId()));
            }
        }
    }

    public static boolean isInWater(Entity entity)
    {   BlockPos pos = entity.blockPosition();
        IChunk chunk = WorldHelper.getChunk(entity.level, pos);
        if (chunk == null) return false;

        return entity.isInWater() || chunk.getBlockState(pos).getBlock() == Blocks.BUBBLE_COLUMN;
    }

    public static boolean isRainingAt(World level, BlockPos pos)
    {   DynamicHolder<Biome> biome = DynamicHolder.create(null, () -> level.getBiomeManager().getBiome(pos));

        return (level.isRaining() && biome.get().getPrecipitation() == Biome.RainType.RAIN)
            && canSeeSky(level, pos.above(), level.getMaxBuildHeight())
            && biome.get().getTemperature(pos) >= 0.15f
            && !CompatManager.SereneSeasons.isColdEnoughToSnow(level, pos);
    }

    /**
     * Iterates through every block along the given vector
     * @param from The starting position
     * @param to The ending position
     * @param rayTracer function to run on each found block
     * @param maxHits the maximum number of blocks to act upon before the ray expires
     */
    public static void forBlocksInRay(Vector3d from, Vector3d to, World level, IChunk chunk, Map<BlockPos, BlockState> stateCache,
                                      BiConsumer<BlockState, BlockPos> rayTracer, int maxHits)
    {
        // Don't bother if the ray has no length
        if (!from.equals(to))
        {
            Vector3d ray = to.subtract(from);
            Vector3d normalRay = ray.normalize();
            BlockPos.Mutable pos = new BlockPos(from).mutable();
            IChunk workingChunk = chunk;

            // Iterate over every block-long segment of the ray
            for (int i = 0; i < ray.length(); i++)
            {
                // Get the position of the current segment
                Vector3d vec = from.add(normalRay.scale(i));

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

    /**
     * Overload for {@link #forBlocksInRay(Vector3d, Vector3d, World, IChunk, Map, BiConsumer, int)} with less bloated params
     */
    public static void forBlocksInRay(Vector3d from, Vector3d to, World level, BiConsumer<BlockState, BlockPos> rayTracer, int maxHits)
    {   forBlocksInRay(from, to, level, getChunk(level, new BlockPos(from)), new HashMap<>(), rayTracer, maxHits);
    }

    public static Entity raycastEntity(Vector3d from, Vector3d to, World level, Predicate<Entity> filter)
    {
        // Don't bother if the ray has no length
        if (!from.equals(to))
        {
            Vector3d ray = to.subtract(from);
            Vector3d normalRay = ray.normalize();
            BlockPos.Mutable pos = new BlockPos(from).mutable();

            // Iterate over every block-long segment of the ray
            for (int i = 0; i < ray.length(); i++)
            {
                // Get the position of the current segment
                Vector3d vec = from.add(normalRay.scale(i));

                // Skip if the position is the same as the last one
                if (new BlockPos(vec).equals(pos)) continue;
                pos.set(vec.x, vec.y, vec.z);

                // Return the first entity in the current block, or continue if there is none
                List<Entity> entities = level.getEntitiesOfClass(Entity.class, new AxisAlignedBB(pos), filter);
                if (!entities.isEmpty()) return entities.get(0);
            }
        }
        return null;
    }

    public static void spawnParticle(World world, IParticleData particle, double x, double y, double z,
                                     double xSpeed, double ySpeed, double zSpeed)
    {
        if (!world.isClientSide)
        {
            ParticleBatchMessage particles = new ParticleBatchMessage();
            particles.addParticle(particle, new ParticleBatchMessage.ParticlePlacement(x, y, z, xSpeed, ySpeed, zSpeed));
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> (Chunk) getChunk(world, (int) x >> 4, (int) z >> 4)), particles);
        }
        else
        {   world.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    public static void spawnParticleBatch(World level, IParticleData particle, double x, double y, double z,
                                          double xSpread, double ySpread, double zSpread, double count, double speed)
    {
        Random rand = new Random();

        if (!level.isClientSide)
        {
            ParticleBatchMessage particles = new ParticleBatchMessage();
            for (int i = 0; i < count; i++)
            {
                Vector3d vec = new Vector3d(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).normalize().scale(speed);
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
                Vector3d vec = new Vector3d(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).normalize().scale(speed);
                level.addParticle(particle,
                        x + xSpread - rand.nextDouble() * (xSpread * 2),
                        y + ySpread - rand.nextDouble() * (ySpread * 2),
                        z + zSpread - rand.nextDouble() * (zSpread * 2), vec.x, vec.y, vec.z);
            }
        }
    }

    public static ItemEntity dropItem(World level, BlockPos pos, ItemStack stack)
    {   return dropItem(level, pos, stack, 6000);
    }

    public static ItemEntity dropItem(World level, BlockPos pos, ItemStack stack, int lifeTime)
    {
        Random rand = new Random();
        ItemEntity item = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), stack);

        item.setDeltaMovement(item.getDeltaMovement().add(((rand.nextFloat() - rand.nextFloat()) * 0.1F), (rand.nextFloat() * 0.05F), ((rand.nextFloat() - rand.nextFloat()) * 0.1F)));
        Field age = ObfuscationReflectionHelper.findField(ItemEntity.class, "f_31985_");
        age.setAccessible(true);
        try
        {   age.set(item, 6000 - lifeTime);
        }
        catch (Exception e)
        {   e.printStackTrace();
        }
        return item;
    }

    /**
     * Drops an item with random velocity from the entity's position
     * @return The dropped item entity
     */
    public static ItemEntity entityDropItem(Entity entity, ItemStack stack)
    {   return entityDropItem(entity, stack, 6000);
    }

    /**
     * Drops an item with random velocity from the entity's position
     * @param lifeTime The despawn time of the item, in ticks
     * @return The dropped item entity
     */
    public static ItemEntity entityDropItem(Entity entity, ItemStack stack, int lifeTime)
    {
        Random rand = new Random();
        ItemEntity item = entity.spawnAtLocation(stack, entity.getBbHeight());
        if (item != null)
        {   item.setDeltaMovement(item.getDeltaMovement().add(((rand.nextFloat() - rand.nextFloat()) * 0.1F), (rand.nextFloat() * 0.05F), ((rand.nextFloat() - rand.nextFloat()) * 0.1F)));
            item.lifespan = lifeTime;
        }
        return item;
    }

    /**
     * @return The closest Vec3, contained in the entity's bounding box, to the given pos.
     */
    public static Vector3d getClosestPointOnEntity(LivingEntity entity, Vector3d pos)
    {
        double playerRadius = entity.getBbWidth() / 2;
        return new Vector3d(CSMath.clamp(pos.x, entity.getX() - playerRadius, entity.getX() + playerRadius),
                            CSMath.clamp(pos.y, entity.getY(), entity.getY() + entity.getBbHeight()),
                            CSMath.clamp(pos.z, entity.getZ() - playerRadius, entity.getZ() + playerRadius));
    }

    /**
     * Merges the entity's server-side persistent data into the client-side persistent data
     * @param destination The player to send the data to. If null, sends to all tracking entities
     */
    public static void syncEntityForgeData(Entity entity, ServerPlayerEntity destination)
    {
        ColdSweatPacketHandler.INSTANCE.send(destination != null ? PacketDistributor.PLAYER.with(() -> destination)
                                                                 : PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                                             new SyncForgeDataMessage(entity));
    }

    /**
     * Manually sends a block entity's update tag to tracking clients
     */
    public static void syncBlockEntityData(TileEntity te)
    {
        if (te.getLevel() == null || te.getLevel().isClientSide) return;

        IChunk ichunk = getChunk(te.getLevel(), te.getBlockPos());
        if (ichunk instanceof Chunk)
        {   ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> ((Chunk) ichunk)), new BlockDataUpdateMessage(te));
        }
    }

    /**
     * Allows the server world to be accessed if you only have a generic "Level" object<br>
     * ONLY USE ON THE SERVER THREAD
     * @return The server world
     */
    public static ServerWorld getServerLevel(World level)
    {   return ServerLifecycleHooks.getCurrentServer().getLevel(level.dimension());
    }

    /**
     * Gets the server instance<br>
     * ONLY USE ON THE SERVER THREAD
     * @return The server instance
     */
    public static MinecraftServer getServer()
    {   return ServerLifecycleHooks.getCurrentServer();
    }

    /**
     * Gets the min and max temperature of the biome.
     * @return A pair of the min and max temperature of the biome
     */
    public static Pair<Double, Double> getBiomeTemperatureRange(IWorld level, Biome biome)
    {   return getBiomeTemperatureRange(level.registryAccess(), biome);
    }

    /**
     * Gets the min and max temperature of the biome.
     * @return A pair of the min and max temperature of the biome
     */
    public static Pair<Double, Double> getBiomeTemperatureRange(DynamicRegistries registryAccess, Biome biome)
    {
        double variance = 1 / Math.max(1, 2 + biome.getDownfall() * 2);
        double baseTemp = biome.getBaseTemperature();

        BiomeTempData biomeTemp = ConfigSettings.BIOME_TEMPS.get(registryAccess)
                                  .getOrDefault(biome, new BiomeTempData(biome, baseTemp - variance, baseTemp + variance, Temperature.Units.MC, Double.NaN, true, false));
        if (biomeTemp.isDisabled())
        {   return Pair.of(0.0, 0.0);
        }
        BiomeTempData configOffset = ConfigSettings.BIOME_OFFSETS.get(registryAccess)
                                     .getOrDefault(biome, new BiomeTempData(biome, 0d, 0d, Temperature.Units.MC, Double.NaN, false, false));
        return CSMath.addPairs(Pair.of(biomeTemp.minTemp(), biomeTemp.maxTemp()),
                               Pair.of(configOffset.minTemp(), configOffset.maxTemp()));
    }

    /**
     * Gets the temperature of the biome at the specified position, including time of day.
     * @return The temperature of the biome at the specified position
     */
    public static double getBiomeTemperature(IWorld level, Biome biome)
    {
        Pair<Double, Double> temps = getBiomeTemperatureRange(level, biome);
        return CSMath.blend(temps.getFirst(), temps.getSecond(), Math.sin(level.dayTime() / (12000 / Math.PI)), -1, 1);
    }

    public static double getTimeMultiplier(IWorld level)
    {
        if (level.dimensionType().hasCeiling())
        {   return 0.5;
        }
        if (level.dimensionType().hasFixedTime())
        {   return CSMath.<OptionalLong>getField(level.dimensionType(), "field_236010_o_").getAsLong();
        }
        return Math.sin(level.dayTime() / (12000 / Math.PI));
    }

    public static double getWaterTemperatureAt(World level, BlockPos pos)
    {
        double defaultWaterTemp = ConfigSettings.DEFAULT_WATER_TEMPERATURE.get();
        BiomeTempData biomeTemp = ConfigSettings.BIOME_TEMPS.get(level.registryAccess()).get(level.getBiome(pos));
        if (biomeTemp == null)
        {   return defaultWaterTemp;
        }
        return biomeTemp.isOffset() ? defaultWaterTemp + biomeTemp.waterTemp()
                                    : biomeTemp.waterTemp();
    }

    /**
     * Returns a cached world temperature value for the position. Returns a "rough" value with reduced positional accuracy (within 8 blocks).<br>
     * Cached values are kept and reused for at most 10 seconds.<br>
     * <br>
     * <b>Flags:</b><br>
     * 1 = Sensitive (generates a fresh value if the cache is > 5 seconds old; 10 seconds otherwise)<br>
     * 2 = Force Update (always generate fresh value; the new value will be cached)<br>
     */
    public static double getRoughTemperatureAt(World level, BlockPos pos, int flags)
    {
        boolean sensitive = (flags & 1) != 0;
        boolean forceUpdate = (flags & 2) != 0;

        Map<BlockPos, TempSnapshot> snapshots = TEMPERATURE_CHECKS.computeIfAbsent(level.dimension(), dim -> new HashMap<>());
        int tickSpeedMultiplier = 1 + level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING) / 20;

        BlockPos segment = new BlockPos(pos.getX() >> 3, pos.getY() >> 3, pos.getZ() >> 3);

        // Use cached value if not forced update
        if (!forceUpdate)
        {
            int interval = sensitive ? 200 : 1000;
            TempSnapshot snapshot = snapshots.get(segment);
            if (snapshot != null)
            {
                long gameTime = level.getGameTime();
                if (gameTime - snapshot.timestamp < interval / tickSpeedMultiplier)
                {   return snapshot.temperature();
                }
            }
        }

        /* Get temperature based on dummy entity */
        // Init dummy at this location
        DummyEntity dummy = getDummyEntity(level);
        Vector3d newPos = CSMath.getCenterPos(pos);
        dummy.setPos(newPos.x, newPos.y, newPos.z);
        // Get modifiers
        List<TempModifier> modifiers = new ArrayList<>(Temperature.getModifiers(dummy, Temperature.Trait.WORLD));

        // Get insulation from hearths
        Pair<Integer, Integer> maxCoolingHeating = getInsulationAt(level, pos, 2);
        if (maxCoolingHeating.getFirst() > 0)
        {   modifiers.add(new FrigidnessTempModifier(maxCoolingHeating.getFirst()));
        }
        if (maxCoolingHeating.getSecond() > 0)
        {   modifiers.add(new WarmthTempModifier(maxCoolingHeating.getSecond()));
        }
        // Get & store temperature
        double tempAt = Temperature.apply(0, dummy, Temperature.Trait.WORLD, modifiers, true);
        snapshots.put(segment, new TempSnapshot(level.getGameTime(), tempAt));

        return tempAt;
    }

    public static double getRoughTemperatureAt(World level, BlockPos pos)
    {   return getRoughTemperatureAt(level, pos, 0);
    }

    /**
     * Gets the "raw" temperature for a block, ignoring distance and occlusion
     */
    public static double getBlockTemperature(World level, BlockState block)
    {
        Collection<BlockTemp> blockTemps = BlockTempRegistry.getBlockTempsFor(block);
        for (BlockTemp blockTemp : blockTemps)
        {
            if (blockTemp.isValid(level, BlockPos.ZERO, block))
            {   return blockTemp.getTemperature(level, null, block, BlockPos.ZERO, 0);
            }
        }
        return 0;
    }

    public static double getTemperatureAt(World level, BlockPos pos)
    {
        DummyPlayer dummy = getDummyPlayer(level);
        // Move the dummy to the position being tested
        Vector3d newPos = CSMath.getCenterPos(pos);
        dummy.setPos(newPos.x, newPos.y, newPos.z);
        List<TempModifier> modifiers = new ArrayList<>(Temperature.getModifiers(dummy, Temperature.Trait.WORLD));

        // Get insulation from hearths
        Pair<Integer, Integer> maxCoolingHeating = getInsulationAt(level, pos, 2);
        if (maxCoolingHeating.getFirst() > 0)
        {   modifiers.add(new FrigidnessTempModifier(maxCoolingHeating.getFirst()));
        }
        if (maxCoolingHeating.getSecond() > 0)
        {   modifiers.add(new WarmthTempModifier(maxCoolingHeating.getSecond()));
        }
        return Temperature.apply(0, dummy, Temperature.Trait.WORLD, modifiers, true);
    }

    public static DummyPlayer getDummyPlayer(World level)
    {
        RegistryKey<World> dimension = level.dimension();
        // There is one "dummy" entity per world, which TempModifiers are applied to
        DummyPlayer dummy = DUMMY_PLAYERS.get(dimension);
        // If the dummy for this dimension is invalid, make a new one
        if (dummy == null || dummy.level != level)
        {
            WorldHelper.DUMMY_PLAYERS.put(dimension, dummy = new DummyPlayer(level));
            // Use default player modifiers to determine the temperature
            Map<Temperature.Trait, List<TempModifier>> defaultModifiers = EntityTempManager.gatherTempModifiers(dummy);
            defaultModifiers.get(Temperature.Trait.WORLD).forEach(mod -> mod.tickRate(1));
            Temperature.getModifiers(dummy).putAll(defaultModifiers);
        }
        return dummy;
    }

    public static DummyEntity getDummyEntity(World level)
    {
        RegistryKey<World> dimension = level.dimension();
        // There is one "dummy" entity per world, which TempModifiers are applied to
        DummyEntity dummy = DUMMY_ENTITIES.get(dimension);
        // If the dummy for this dimension is invalid, make a new one
        if (dummy == null || dummy.level != level)
        {
            WorldHelper.DUMMY_ENTITIES.put(dimension, dummy = new DummyEntity(level));
            // Use default player modifiers to determine the temperature
            Map<Temperature.Trait, List<TempModifier>> defaultModifiers = EntityTempManager.gatherTempModifiers(dummy);
            defaultModifiers.get(Temperature.Trait.WORLD).forEach(mod -> mod.tickRate(1));
            Temperature.getModifiers(dummy).putAll(defaultModifiers);
        }
        return dummy;
    }

    public Map<RegistryKey<World>, DummyPlayer> getDummyPlayers()
    {   return DUMMY_PLAYERS;
    }

    public Map<RegistryKey<World>, DummyEntity> getDummyEntities()
    {   return DUMMY_ENTITIES;
    }

    public Map<RegistryKey<World>, Map<BlockPos, TempSnapshot>> getWorldTempCache()
    {   return TEMPERATURE_CHECKS;
    }

    public static boolean allAdjacentBlocksMatch(BlockPos pos, Predicate<BlockPos> predicate)
    {
        BlockPos.Mutable pos2 = pos.mutable();
        for (int i = 0; i < Direction.values().length; i++)
        {
            BlockPos offset = pos2.setWithOffset(pos, Direction.values()[i]);
            if (!predicate.test(offset)) return false;
        }
        return true;
    }

    public static BlockState waterlog(BlockState state, World level, BlockPos pos)
    {
        boolean waterAt = level.getFluidState(pos).getType() == Fluids.WATER;
        return state.setValue(BlockStateProperties.WATERLOGGED, waterAt);
    }

    public static boolean shouldFreeze(IWorld levelReader, BlockPos pos, boolean mustBeAtEdge)
    {
        if (pos.getY() >= 0 && pos.getY() < levelReader.getMaxBuildHeight()
        && levelReader instanceof ServerWorld)
        {
            if (surroundedByBlock(levelReader, pos, Blocks.ICE))
            {   return true;
            }
            Lazy<Boolean> freezingTemp = Lazy.of(() -> getRoughTemperatureAt((ServerWorld) levelReader, pos) < 0f);

            if (!mustBeAtEdge)
            {   return freezingTemp.get();
            }
            return !surroundedByFluid(levelReader, pos, Fluids.WATER) && freezingTemp.get();
        }
        return false;
    }

    public static boolean shouldMelt(IWorld levelReader, BlockPos pos, boolean mustBeAtEdge)
    {
        if (pos.getY() >= 0 && pos.getY() < levelReader.getMaxBuildHeight()
        && levelReader instanceof ServerWorld)
        {
            if (mustBeAtEdge && surroundedByBlock(levelReader, pos, Blocks.ICE))
            {   return false;
            }
            return getRoughTemperatureAt((ServerWorld) levelReader, pos) >= 0f;
        }
        return false;
    }

    public static boolean surroundedByBlock(IWorld level, BlockPos pos, Block block)
    {
        BlockPos.Mutable pos2 = pos.mutable();
        return level.getBlockState(pos2.setWithOffset(pos, Direction.NORTH)).is(block)
            && level.getBlockState(pos2.setWithOffset(pos, Direction.SOUTH)).is(block)
            && level.getBlockState(pos2.setWithOffset(pos, Direction.EAST)).is(block)
            && level.getBlockState(pos2.setWithOffset(pos, Direction.WEST)).is(block);
    }

    public static boolean surroundedByFluid(IWorld level, BlockPos pos, Fluid fluid)
    {
        BlockPos.Mutable pos2 = pos.mutable();
        return level.getBlockState(pos2.setWithOffset(pos, Direction.NORTH)).getFluidState().getType() == fluid
            && level.getBlockState(pos2.setWithOffset(pos, Direction.SOUTH)).getFluidState().getType() == fluid
            && level.getBlockState(pos2.setWithOffset(pos, Direction.EAST)).getFluidState().getType() == fluid
            && level.getBlockState(pos2.setWithOffset(pos, Direction.WEST)).getFluidState().getType() == fluid;
    }

    public static boolean nextToSoulFire(IWorld level, BlockPos pos)
    {
        BlockPos.Mutable pos2 = pos.mutable();
        for (int x = -1; x <= 1; x++)
        for (int y =  0; y <= 1; y++)
        for (int z = -1; z <= 1; z++)
        {
            BlockState state = level.getBlockState(pos2.setWithOffset(pos, x, y, z));
            if (state.is(Blocks.SOUL_FIRE) || state.is(Blocks.SOUL_CAMPFIRE) && state.getValue(CampfireBlock.LIT))
            {   return true;
            }
        }
        return false;
    }

    public static Pair<Integer, Integer> getInsulationAt(World level, BlockPos pos, int chunkRadius)
    {
        int maxCoolingLevel = 0;
        int maxHeatingLevel = 0;
        ChunkPos chunkPos = new ChunkPos(pos);
        for (int x = -chunkRadius; x <= chunkRadius; x++)
        for (int z = -chunkRadius; z <= chunkRadius; z++)
        {
            IChunk ichunk = getChunk(level, chunkPos.x + x, chunkPos.z + z);
            if (!(ichunk instanceof Chunk)) continue;
            Chunk chunk = (Chunk) ichunk;

            for (BlockPos bePos : chunk.getBlockEntitiesPos())
            {
                TileEntity be = chunk.getBlockEntity(bePos);
                if (be instanceof HearthBlockEntity && ((HearthBlockEntity) be).getPathLookup().contains(pos))
                {
                    HearthBlockEntity hearth = (HearthBlockEntity) be;
                    maxCoolingLevel = Math.max(maxCoolingLevel, hearth.getCoolingLevel());
                    maxHeatingLevel = Math.max(maxHeatingLevel, hearth.getHeatingLevel());
                }
            }
        }
        return Pair.of(maxCoolingLevel, maxHeatingLevel);
    }

    public static List<BlockPos> getOccupiedPositions(AxisAlignedBB bb)
    {
        List<BlockPos> positions = new ArrayList<>();
        int minX = (int) Math.floor(bb.minX);
        int minY = (int) Math.floor(bb.minY);
        int minZ = (int) Math.floor(bb.minZ);
        int maxX = (int) Math.ceil(bb.maxX);
        int maxY = (int) Math.ceil(bb.maxY);
        int maxZ = (int) Math.ceil(bb.maxZ);

        for (int x = minX; x < maxX; x++)
        for (int y = minY; y < maxY; y++)
        for (int z = minZ; z < maxZ; z++)
        {   positions.add(new BlockPos(x, y, z));
        }
        return positions;
    }

    public static class TempSnapshot
    {
        private final long timestamp;
        private final double temperature;

        public TempSnapshot(long timestamp, double temperature)
        {
            this.timestamp = timestamp;
            this.temperature = temperature;
        }

        public long timestamp()
        {   return timestamp;
        }
        public double temperature()
        {   return temperature;
        }
    }
}
