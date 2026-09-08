package com.momosoftworks.coldsweat.util.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.api.temperature.block_temp.ConfiguredBlockTemp;
import com.momosoftworks.coldsweat.data.codec.configuration.BlockTempData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Function;

/**
 * One-shot batch computation context carrying out the "scan the region union once, dispatch per-position"
 * block-temperature calculation for a batch of BlockPos positions within the same world.
 * <br/>
 * Its lifetime is strictly bounded to a single {@link WorldHelper#getTemperaturesAt} call: the caller creates it
 * on the fly and discards it once the scan finishes. It is not held in any global cache, so it needs no cleanup
 * on server stop and cannot leak across dimensions or ticks.
 * <br/>
 * Semantics: compared with the single-pos {@code BlockTempModifier.calculate()}, this class is threshold-equivalent
 * in normal scenarios (same cold/hot direction and magnitude), but not bit-for-bit identical because the
 * accumulation order is affected by the dispatch. Addons should validate against the "threshold-equivalent" contract.
 *
 * @author liudongyu
 */
public final class BlockTempScanBatch
{
	private static final double LOG_FACTOR = 0.52;

	private final Level level;
	private final LivingEntity entity;
	private final Set<BlockPos> positions;
	private final int range;

	// Chunk and blockstate caches shared across this batch.
	final Map<Long, ChunkAccess> chunkCache = new LinkedHashMap<>(16, 0.75f, true);
	final Long2ObjectOpenHashMap<BlockState> stateCache = new Long2ObjectOpenHashMap<>(3000);

	// Per-target-pos BlockTemp accumulated values / group-accumulated values.
	private final Map<BlockPos, Map<BlockTemp, Double>> totals = Maps.newHashMap();
	private final Map<BlockPos, Map<TagKey<BlockTempData>, Double>> groupTotals = Maps.newHashMap();

	private final List<Source> sources = Lists.newArrayList();

	/**
	 * Constructs a batch computation context.
	 *
	 * @param level     the world the temperatures belong to (must be the same Level; call from the server main thread)
	 * @param entity    the entity acting as the "observer" (the dummy player, preserving getTemperature semantics)
	 * @param positions the target positions to probe (the caller should have de-duplicated and completed sublevel transforms)
	 * @param range     the scan radius (== BLOCK_RANGE)
	 */
	public BlockTempScanBatch(Level level, LivingEntity entity, Set<BlockPos> positions, int range)
	{
		this.level = level;
		this.entity = entity;
		this.positions = positions;
		this.range = range;
	}

	/**
	 * Runs the two-phase scan: phase A scans the region union and caches blockstates, phase B dispatches
	 * accumulation to the target positions within each source block's affected radius.
	 */
	public void scan()
	{
		this.scanUnionRegion();
		this.dispatchToPositions();
	}

	/**
	 * Returns the block-temperature contribution function for a given target position,
	 * semantically aligned with the closure returned by {@code BlockTempModifier.calculate()}.
	 *
	 * @param pos the target position
	 * @return the block-temperature function participating in the temperature chain (clamps the running temperature)
	 */
	public Function<Double, Double> getFunction(BlockPos pos)
	{
		Map<BlockTemp, Double> posTotals = this.totals.getOrDefault(pos, Map.of());
		return temp ->
		{
			for (Map.Entry<BlockTemp, Double> entry : posTotals.entrySet())
			{
				BlockTemp blockTemp = entry.getKey();
				double min = blockTemp.minTemperature();
				double max = blockTemp.maxTemperature();
				if (!CSMath.betweenInclusive(temp, min, max)) continue;
				double effectValue = entry.getValue();
				temp = CSMath.clamp(temp + effectValue, min, max);
			}
			return temp;
		};
	}

	/** Phase A: scan the AABB union of all positions (expanded by range), reading and caching each blockstate. */
	private void scanUnionRegion()
	{
		long[] bounds = this.getRegionBounds();
		int minX = (int) bounds[0];
		int minY = (int) bounds[1];
		int minZ = (int) bounds[2];
		int maxX = (int) bounds[3];
		int maxY = (int) bounds[4];
		int maxZ = (int) bounds[5];
		BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();

		for (int x = minX; x <= maxX; x++)
		{
			int chunkX = x >> 4;
			for (int z = minZ; z <= maxZ; z++)
			{
				int chunkZ = z >> 4;
				long chunkPos = ChunkPos.asLong(chunkX, chunkZ);
				ChunkAccess chunk = this.chunkCache.computeIfAbsent(
						chunkPos,
						cp -> WorldHelper.getChunk(this.level, new ChunkPos(cp))
				);
				if (chunk == null) continue;

				for (int y = minY; y <= maxY; y++)
				{
					blockpos.set(x, y, z);
					long blockPosLong = blockpos.asLong();
					BlockState state = this.stateCache.get(blockPosLong);
					if (state == null)
					{
						LevelChunkSection section = WorldHelper.getChunkSection(chunk, y);
						state = section.getBlockState(x & 15, y & 15, z & 15);
						this.stateCache.put(blockPosLong, state);
					}
					if (state.isAir()) continue;

					Collection<BlockTemp> blockTemps = BlockTempRegistry.getBlockTempsFor(state);
					if (blockTemps.isEmpty() || (blockTemps.size() == 1 && blockTemps.contains(BlockTempRegistry.DEFAULT_BLOCK_TEMP)))
					{   continue;
					}
					this.sources.add(new Source(blockpos.immutable(), state));
				}
			}
		}
	}

	/** Phase B: for each source block, dispatch distance / occlusion / accumulation only to target positions within its affected radius. */
	private void dispatchToPositions()
	{
		for (Source source : this.sources)
		{
			BlockPos src = source.pos();
			BlockState state = source.state();

			for (BlockPos pos : this.positions)
			{
				Collection<BlockTemp> blockTemps = BlockTempRegistry.getBlockTempsFor(state);

				// Pre-filter: whether any BlockTemp can still affect this position (mirrors BlockTempModifier.areAnyBlockTempsInRange).
				if (!this.areAnyBlockTempsInRange(pos, blockTemps)) continue;

				// The observer is this target position: move the dummy there to preserve getTemperature(entity) semantics.
				this.entity.setPos(CSMath.getCenterPos(pos));

				// Source block center, closest point on the observer, and the distance between them.
				Vec3 srcPos = CSMath.getCenterPos(src);
				Vec3 playerClosest = WorldHelper.getClosestPointOnEntity(this.entity, srcPos);
				Vec3 ray = srcPos.subtract(playerClosest);
				Direction direction = Direction.getNearest(ray.x, ray.y, ray.z);
				double distance = CSMath.getDistance(playerClosest, srcPos);
				if (distance < 0 || distance > this.range) continue;

				// Occlusion count: blocks blocking between the observer and the source block.
				int[] blocksOccluding = new int[1];
				WorldHelper.forBlocksInRay(playerClosest, srcPos, this.level, this.getChunk(src), this.stateCache,
						(rayState, bpos) ->
						{
							if (!bpos.equals(src) && WorldHelper.isSpreadBlocked(this.level, rayState, bpos, direction, direction))
							{   blocksOccluding[0]++;
							}
						}, 3);

				for (BlockTemp blockTemp : blockTemps)
				{
					if (!blockTemp.isValid(this.level, src, state)) continue;
					double temperature = blockTemp.getTemperature(this.level, this.entity, state, src, distance);
					if (temperature == 0) continue;
					this.accumulate(pos, blockTemp, temperature, blocksOccluding[0], distance);
					break; // As in BlockTempModifier: each source block takes only the first effective BlockTemp.
				}
			}
		}
	}

	/** Performs one accumulation for a (source block, target pos) pair; clamping semantics align with BlockTempModifier. */
	private void accumulate(BlockPos pos, BlockTemp blockTemp, double temperature, int blocksOccluding, double distance)
	{
		Map<BlockTemp, Double> posTotals = this.totals.computeIfAbsent(pos, p -> Maps.newHashMap());
		double blockTempTotal = posTotals.getOrDefault(blockTemp, 0d);
		double blockGroupTotal = this.getGroupTotal(pos, blockTemp);
		double blockGroupDelta = blockGroupTotal - blockTempTotal;

		double tempToAdd = blockTemp.fade()
				? CSMath.blend(temperature, 0, distance, 0.5, blockTemp.range())
				: temperature;

		double newVal;
		if (blockTemp.logarithmic())
		{
			double newTotal = Math.pow(Math.pow(blockTempTotal, 1 / LOG_FACTOR) + tempToAdd, LOG_FACTOR);
			double delta = newTotal - blockTempTotal;
			delta /= (blocksOccluding + 1);
			newVal = CSMath.clamp(blockTempTotal + delta,
					blockTemp.minEffect() + blockGroupDelta,
					blockTemp.maxEffect() - blockGroupDelta);
		}
		else
		{
			tempToAdd /= (blocksOccluding + 1);
			newVal = CSMath.clamp(blockTempTotal + tempToAdd,
					blockTemp.minEffect() + blockGroupDelta,
					blockTemp.maxEffect() - blockGroupDelta);
		}

		posTotals.put(blockTemp, newVal);
		this.updateGroupTotal(pos, blockTemp, newVal - blockTempTotal);
	}

	private double getGroupTotal(BlockPos pos, BlockTemp blockTemp)
	{
		if (!(blockTemp instanceof ConfiguredBlockTemp config) || config.getData().effectGroup().isEmpty())
		{   return this.totals.getOrDefault(pos, Map.of()).getOrDefault(blockTemp, 0d);
		}
		return this.groupTotals.getOrDefault(pos, Map.of()).getOrDefault(config.getData().effectGroup().get(), 0D);
	}

	/** Pre-filter: whether any BlockTemp can still affect a position (mirrors BlockTempModifier.areAnyBlockTempsInRange). */
	private boolean areAnyBlockTempsInRange(BlockPos pos, Collection<BlockTemp> blockTemps)
	{
		Map<BlockTemp, Double> posTotals = this.totals.getOrDefault(pos, Map.of());
		for (BlockTemp blockTemp : blockTemps)
		{
			if (!posTotals.containsKey(blockTemp))
			{   return true;
			}
			double effectTotal = this.getGroupTotal(pos, blockTemp);
			if (CSMath.betweenInclusive(effectTotal, blockTemp.minEffect(), blockTemp.maxEffect()))
			{   return true;
			}
		}
		return false;
	}

	private void updateGroupTotal(BlockPos pos, BlockTemp blockTemp, double delta)
	{
		if (blockTemp instanceof ConfiguredBlockTemp config) {
			config.getData().effectGroup().ifPresent(tempDataTagKey -> this.groupTotals.computeIfAbsent(
					pos, p -> Maps.newHashMap()
			).merge(tempDataTagKey, delta, Double::sum));
		}
	}

	private ChunkAccess getChunk(BlockPos pos)
	{   return WorldHelper.getChunk(this.level, pos);
	}

	/** Computes the AABB union covered by all positions, expanded outward by range. */
	private long[] getRegionBounds()
	{
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		for (BlockPos pos : this.positions)
		{
			minX = Math.min(minX, pos.getX()); maxX = Math.max(maxX, pos.getX());
			minY = Math.min(minY, pos.getY()); maxY = Math.max(maxY, pos.getY());
			minZ = Math.min(minZ, pos.getZ()); maxZ = Math.max(maxZ, pos.getZ());
		}
		return new long[] { minX - this.range, minY - this.range, minZ - this.range,
				maxX + this.range, maxY + this.range, maxZ + this.range };
	}

	// Candidate source blocks hit during phase A (state already read, so phase B does not read it again).
	private record Source(BlockPos pos, BlockState state) {
	}
}