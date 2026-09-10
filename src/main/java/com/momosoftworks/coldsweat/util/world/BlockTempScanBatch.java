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
 * One-shot batch computation context carrying out the "scan the union of each position's influence box once,
 * dispatch per-position" block-temperature calculation for a batch of BlockPos positions within the same world.
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

	// LRU chunk cache shared across this batch (chunks may host parts of several disjoint clusters).
	final Map<Long, ChunkAccess> chunkCache = new LinkedHashMap<>(16, 0.75f, true);
	// Blockstate cache used only by phase B occlusion rays (phase A reads states directly, since no block is
	// ever visited twice across disjoint cluster boxes).
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
	 * Runs the two-phase scan: phase A scans the union of the per-position influence boxes once and collects the
	 * candidate source blocks, phase B dispatches accumulation to the target positions within each source block's
	 * affected radius.
	 */
	public void scan()
	{
		this.scanSources();
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

	/**
	 * Phase A: partition the target positions into clusters whose {@code ±range} boxes overlap, then scan each
	 * cluster's own bounding box once. Disjoint clusters never share a block, so every block is read at most once
	 * per batch and no block-state cache is needed here. This keeps the scanned volume proportional to the union of
	 * the per-position boxes instead of the (potentially much larger) bounding box of all positions.
	 */
	private void scanSources()
	{
		List<BlockPos> positionList = Lists.newArrayList(this.positions);
		int count = positionList.size();
		if (count == 0) return;

		// Union-find over positions: two positions belong to the same cluster iff their influence boxes overlap,
		// i.e. their Chebyshev distance is at most 2 * range. Pair checks are limited to positions whose block
		// chunks are within chunkRadius of each other, since farther positions can never overlap.
		int[] parent = new int[count];
		for (int i = 0; i < count; i++)
		{   parent[i] = i;
		}

		Map<Long, List<Integer>> bucket = Maps.newHashMap();
		for (int i = 0; i < count; i++)
		{
			BlockPos pos = positionList.get(i);
			long chunkKey = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
			bucket.computeIfAbsent(chunkKey, key -> Lists.newArrayList()).add(i);
		}

		int chunkRadius = (this.range * 2 + 15) / 16;
		for (Map.Entry<Long, List<Integer>> entry : bucket.entrySet())
		{
			long chunkKey = entry.getKey();
			int chunkX = ChunkPos.getX(chunkKey);
			int chunkZ = ChunkPos.getZ(chunkKey);
			for (int dx = -chunkRadius; dx <= chunkRadius; dx++)
			for (int dz = -chunkRadius; dz <= chunkRadius; dz++)
			{
				long neighborKey = ChunkPos.asLong(chunkX + dx, chunkZ + dz);
				// Only process each unordered pair of buckets once (self-pairs included).
				if (neighborKey < chunkKey) continue;
				List<Integer> neighbor = bucket.get(neighborKey);
				if (neighbor == null) continue;

				for (int i : entry.getValue())
				for (int j : neighbor)
				{
					if (i == j || this.find(parent, i) == this.find(parent, j)) continue;
					BlockPos a = positionList.get(i);
					BlockPos b = positionList.get(j);
					if (Math.abs(a.getX() - b.getX()) <= this.range * 2 &&
							Math.abs(a.getY() - b.getY()) <= this.range * 2 &&
							Math.abs(a.getZ() - b.getZ()) <= this.range * 2)
					{   this.union(parent, i, j);
					}
				}
			}
		}

		// Accumulate cluster bounds (keyed by root index), then scan each cluster's bounding box once.
		int[] minX = new int[count];
		int[] minY = new int[count];
		int[] minZ = new int[count];
		int[] maxX = new int[count];
		int[] maxY = new int[count];
		int[] maxZ = new int[count];
		Arrays.fill(minX, Integer.MAX_VALUE);
		Arrays.fill(minY, Integer.MAX_VALUE);
		Arrays.fill(minZ, Integer.MAX_VALUE);
		Arrays.fill(maxX, Integer.MIN_VALUE);
		Arrays.fill(maxY, Integer.MIN_VALUE);
		Arrays.fill(maxZ, Integer.MIN_VALUE);
		for (int i = 0; i < count; i++)
		{
			int root = this.find(parent, i);
			BlockPos pos = positionList.get(i);
			minX[root] = Math.min(minX[root], pos.getX());
			minY[root] = Math.min(minY[root], pos.getY());
			minZ[root] = Math.min(minZ[root], pos.getZ());
			maxX[root] = Math.max(maxX[root], pos.getX());
			maxY[root] = Math.max(maxY[root], pos.getY());
			maxZ[root] = Math.max(maxZ[root], pos.getZ());
		}
		for (int i = 0; i < count; i++)
		{
			if (this.find(parent, i) != i) continue;
			this.scanClusterBox(minX[i] - this.range, minY[i] - this.range, minZ[i] - this.range,
								maxX[i] + this.range, maxY[i] + this.range, maxZ[i] + this.range);
		}
	}

	/** Scans one cluster's bounding box chunk by chunk, reading every blockstate directly and collecting sources. */
	private void scanClusterBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();
		for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++)
		for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++)
		{
			long chunkKey = ChunkPos.asLong(chunkX, chunkZ);
			ChunkAccess chunk = this.chunkCache.get(chunkKey);
			if (chunk == null)
			{
				chunk = WorldHelper.getChunk(this.level, new ChunkPos(chunkKey));
				if (chunk == null) continue;
				this.chunkCache.put(chunkKey, chunk);
			}

			int xStart = Math.max(minX, chunkX << 4);
			int xEnd = Math.min(maxX, (chunkX << 4) | 15);
			int zStart = Math.max(minZ, chunkZ << 4);
			int zEnd = Math.min(maxZ, (chunkZ << 4) | 15);

			for (int y = minY; y <= maxY; y++)
			{
				LevelChunkSection section = WorldHelper.getChunkSection(chunk, y);
				int ly = y & 15;
				for (int x = xStart; x <= xEnd; x++)
				{
					int lx = x & 15;
					for (int z = zStart; z <= zEnd; z++)
					{
						BlockState state = section.getBlockState(lx, ly, z & 15);
						if (state.isAir()) continue;

						Collection<BlockTemp> blockTemps = BlockTempRegistry.getBlockTempsFor(state);
						if (blockTemps.isEmpty() || (blockTemps.size() == 1 && blockTemps.contains(BlockTempRegistry.DEFAULT_BLOCK_TEMP)))
						{   continue;
						}
						blockpos.set(x, y, z);
						this.sources.add(new Source(blockpos.immutable(), state));
					}
				}
			}
		}
	}

	private int find(int[] parent, int i)
	{
		while (parent[i] != i)
		{   parent[i] = parent[parent[i]];
			i = parent[i];
		}
		return i;
	}

	private void union(int[] parent, int a, int b)
	{
		int rootA = this.find(parent, a);
		int rootB = this.find(parent, b);
		if (rootA != rootB)
		{   parent[rootA] = rootB;
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

	// Candidate source blocks hit during phase A (state already read, so phase B does not read it again).
	private record Source(BlockPos pos, BlockState state) {
	}
}
