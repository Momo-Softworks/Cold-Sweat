# Cold-Sweat Batch Temperature Query API – Specification

> **Scope:** this document specifies the batch world-temperature API shipped in Cold‑Sweat, covering **two rounds of updates** (see §1).
> **Implementation status:** both rounds are implemented; Update 1 is committed, Update 2 is present in the working tree.

---

## 1. Update History

### Update 1 — Batch API foundation (committed)

| Item | Value |
|---|---|
| Commit | `811dab1d9` — *feat(API): new APIs for batch temperature query* (current `HEAD`) |
| Base for comparison | `ebf6bc52b` — *Remove boatload compat* |
| Diff of the round | `ebf6bc52..811dab1d9`: 4 files changed, **691 insertions, 7 deletions** |

Files added/changed in this round:

| File | Change (`git diff --numstat ebf6bc52 HEAD`) |
|---|---|
| `docs/performance-batch-temperature-query-cs.md` | +186 / −0 (this specification) |
| `api/temperature/modifier/BatchBlockTempModifier.java` | +33 / −0 (new) |
| `util/world/BlockTempScanBatch.java` | +299 / −0 (new, first version) |
| `util/world/WorldHelper.java` | +173 / −7 (`getTemperaturesAt`, `getRoughTemperaturesAt`, `getInsulationAtBatch`, `replaceBlockTempsWithBatch`) |

Content of the round:

- Public batch entry points on `WorldHelper` (full‑precision and coarse‑precision).
- A per‑call scan context, `BlockTempScanBatch`, replacing the per‑position volume scan with a single region scan plus per‑source distribution.
- Chunk‑de‑duplicated Hearth insulation lookup.
- A public `TempModifier` adapter used to splice the pre‑computed contribution function into the existing WORLD modifier chain.

### Update 2 — Scan region & per‑block overhead fix (working tree)

| Item | Value (`git diff --numstat`, working tree) |
|---|---|
| `util/world/BlockTempScanBatch.java` | +148 / −59 — the whole of Update 2 |
| `docs/performance-batch-temperature-query-cs.md` | revision of this document to match both rounds |
| Commit state | both files are uncommitted at the time of writing; Update 1's commit `811dab1d9` remains `HEAD` |

Motivation — in‑game profiling of the first version showed the remaining cost concentrated in `BlockTempScanBatch.scanUnionRegion`:

| Hot spot | Share | Cause |
|---|---|---|
| `Long2ObjectOpenHashMap.get` | 17.66% | per‑block cache probe, but a single AABB pass visits each block exactly once → the probe always missed |
| `LevelChunkSection.getBlockState` | 12.87% | proportional to the number of scanned blocks |
| `Long2ObjectOpenHashMap.put` | 10.58% | paired with the useless probe above |
| `HashMap.computeIfAbsent` | 2.88% | per‑column chunk lookup |

Diagnosis: the first version scanned the **bounding box of all positions** (expanded by `range`). With containers scattered over the loaded area, that box covers large stretches of terrain containing no usable source at all, so the scanned volume — and therefore the `getBlockState` cost — grew with the spread of the positions rather than with the volume that can actually contribute.

Content of the round (see §4.3):

- Phase A now scans the **union of the per‑position influence boxes** (positions clustered by box overlap) instead of the bounding box of all positions — volumes between distant clusters are never touched.
- Phase A reads blockstates **directly**, with no blockstate cache; disjoint clusters never share a block, so no block is read twice and the per‑block map traffic disappears.
- The blockstate cache is retained but now serves **Phase B occlusion rays only**.
- Refactor summary: `scanUnionRegion()` (single AABB) + `getRegionBounds()` are gone, replaced by `scanSources()` (union‑find clustering + per‑cluster bounds) and `scanClusterBox(...)` (chunk‑wise direct reads); `find`/`union` helpers were added.

Measured outcome of both rounds: the periodic main‑thread spike is gone and the load is smooth in the reported scenario (verified in‑game by the maintainer).

### Downstream consumer (different repository)

`Freeze-It-And-Heat-It` (FIAHI) consumes this API from `ForgeEventHandler.onLevelTick`: it throttles per dimension, collects every eligible container position once per check tick, performs **one** batch query, then ticks the food stacks. It currently uses the coarse‑precision batch (`getRoughTemperaturesAt(..., 0)`) so that most throttled ticks are plain segment‑cache hits.

---

## 2. Problem Statement

The pre‑existing `WorldHelper.getTemperatureAt(Level, BlockPos)` computes the world temperature for a **single** position. Each call performs:

- a 25‑chunk scan for `HearthBlockEntity` insulation,
- a full `(2·range)³` volume scan (≈2744 blocks at `range = 7`) for block‑temperature contributions,
- biome sampling and elevation/shade queries,
- a dummy player WORLD modifier chain assembly.

The `(2·range)³` scan enumerates **every** block in the box, because ordinary blocks that carry a `BlockTemp` (fire, lava, ice, snow, configured heat/cold sources, …) have no spatial index to look them up by: the only way to find them is to read the volume. Minecraft provides such enumeration only for **block entities** (`ChunkAccess.getBlockEntities()`), which is why Hearth insulation can be batched by iterating chunk block entities while block temperatures cannot.

When many containers (storage rooms, food arrays) request temperatures individually, cost scales as **N × S** (N = positions, S = per‑position scan) — effectively O(N²) for clustered layouts. The batch API shares the expensive parts across positions.

---

## 3. Public API Additions (`WorldHelper`)

### 3.1 Full‑precision batch API

```java
/**
 * Computes the world temperature for a batch of positions in the same Level.
 * Reuses the dummy player modifier chain, replaces the per‑position volume scan with a single
 * scan over the union of the influence boxes, and de‑duplicates Hearth insulation lookups per chunk.
 *
 * @param level     the Level (server side; call from the server main thread)
 * @param positions a collection of BlockPos (duplicates are de‑duplicated internally)
 * @return an Object2DoubleMap mapping each de‑duplicated position to its world temperature
 * @throws IllegalStateException if called on the logical client
 */
public static Object2DoubleMap<BlockPos> getTemperaturesAt(Level level, Collection<BlockPos> positions)
```

Behaviour:

- Positions are normalised through `sublevelToWorld` and de‑duplicated into a `LinkedHashSet` (insertion order preserved, so results are deterministic across calls); an empty batch returns an empty map immediately.
- The shared batch scan runs once (`new BlockTempScanBatch(level, dummy, normalized, ConfigSettings.BLOCK_RANGE.get()).scan()`), and `getInsulationAtBatch(level, normalized, 2)` is queried once.
- For each position the dummy is moved to the block centre and the WORLD chain is **cloned** (`Temperature.getModifiers` returns an immutable list, so the clone is required); the original `BlockTempModifier` step is replaced in place by the batch function, then `FrigidnessTempModifier` / `WarmthTempModifier` are appended when the position has cooling/heating levels > 0, and finally `Temperature.apply(0, dummy, Trait.WORLD, modifiers, true)` yields the value.

### 3.2 Coarse‑precision batch API

```java
/**
 * Computes rough world temperatures for a batch of positions, reusing the 8‑block segment cache.
 *
 * @param level     the Level
 * @param positions a collection of BlockPos (duplicates collapse onto the same key)
 * @param flags     the same cache flags as getRoughTemperatureAt (1 = Sensitive, 2 = Force Update)
 * @return an Object2DoubleMap mapping each position to its rough temperature
 */
public static Object2DoubleMap<BlockPos> getRoughTemperaturesAt(Level level, Collection<BlockPos> positions, int flags)
```

Behaviour: loops the positions through the existing `getRoughTemperatureAt(level, pos, flags)` cache path, writing one entry per position. No deduplication and no client‑side guard (the underlying method has none either).

### 3.3 Insulation batch API

```java
/**
 * De‑duplicated‑per‑chunk Hearth insulation query.
 *
 * @return the (max cooling level, max heating level) for each target position
 */
public static Map<BlockPos, Pair<Integer, Integer>> getInsulationAtBatch(Level level, Collection<BlockPos> positions, int chunkRadius)
```

> Note: this returns a **pair of levels**, not a temperature offset — a single `double` cannot carry both the cooling and the heating level.

### 3.4 Modifier adapter

```java
public final class BatchBlockTempModifier extends TempModifier
{
    public BatchBlockTempModifier(Function<Double, Double> temperatureGetter);

    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait);
}
```

A `public` adapter in `api.temperature.modifier` whose `calculate` returns the pre‑computed contribution function, so the existing chain semantics (ordering, clamping position in the chain) are preserved. It is **not** registered in `TempModifierRegistry`.

---

## 4. Internal Implementation — `BlockTempScanBatch`

A **temporary, per‑call scanning context**: instantiated inside a single `getTemperaturesAt` call, used for that batch only, then garbage‑collected. No static state, no global cache, no cleanup hook needed.

### 4.1 Design Principles

- **Not a singleton / cache:** created per call and discarded; nothing is retained across calls or ticks.
- **No cross‑tick or cross‑dimension pollution:** fully local state (server main thread only).
- **Existing behaviour untouched:** `BlockTempModifier.calculate()`, `getTemperatureAt` and `getRoughTemperatureAt` are unchanged; the batch is a parallel path.

### 4.2 Class Structure (as implemented)

```java
public final class BlockTempScanBatch
{
    private static final double LOG_FACTOR = 0.52;

    private final Level level;
    private final LivingEntity entity;          // the dummy player (observer)
    private final Set<BlockPos> positions;      // already de‑duplicated and sublevel‑mapped
    private final int range;                    // = ConfigSettings.BLOCK_RANGE

    final Map<Long, ChunkAccess> chunkCache;    // LRU, shared across the batch
    final Long2ObjectOpenHashMap<BlockState> stateCache;   // Phase B occlusion rays only

    private final Map<BlockPos, Map<BlockTemp, Double>> totals;                    // per‑pos accumulators
    private final Map<BlockPos, Map<TagKey<BlockTempData>, Double>> groupTotals;   // per‑pos group totals
    private final List<Source> sources;         // candidate source blocks found in Phase A

    private record Source(BlockPos pos, BlockState state) {}

    public void scan();                                  // Phase A + Phase B
    public Function<Double, Double> getFunction(BlockPos pos);   // final clamp closure

    private void scanSources();                          // Phase A
    private void scanClusterBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
    private int find(int[] parent, int i);               // union‑find
    private void union(int[] parent, int a, int b);
    private void dispatchToPositions();                  // Phase B
    private void accumulate(BlockPos pos, BlockTemp blockTemp, double temperature, int blocksOccluding, double distance);
    private double getGroupTotal(BlockPos pos, BlockTemp blockTemp);
    private boolean areAnyBlockTempsInRange(BlockPos pos, Collection<BlockTemp> blockTemps);
    private void updateGroupTotal(BlockPos pos, BlockTemp blockTemp, double delta);
    private ChunkAccess getChunk(BlockPos pos);
}
```

> There is no `buildResult()` method: accumulation is finalised inside `getFunction(pos)`, which returns the same clamping closure shape used by the single‑position `calculate()`.

### 4.3 Phase A — Union‑of‑boxes scan (Update 2)

Phase A collects the candidate source blocks: non‑air blocks whose state carries a `BlockTemp` other than `DEFAULT_BLOCK_TEMP`.

1. **Clustering.** Positions are partitioned with a union‑find over pair overlap: two positions share a cluster iff their `±range` boxes intersect, i.e. their **Chebyshev distance ≤ 2·range**. Pair checks are restricted to positions whose block chunks are within `chunkRadius = (2·range + 15) / 16` of one another (farther positions can never overlap), using a per‑chunk bucket so the number of comparisons stays proportional to local density rather than N².
2. **Per‑cluster bounding box.** Each cluster is scanned over its own bounding box (member min/max ± `range`). Because disjoint clusters have disjoint (non‑intersecting) boxes, **no block is ever visited twice in a batch**, so Phase A needs no blockstate cache at all — each block is read once directly through `LevelChunkSection.getBlockState(lx, ly, lz)`.
3. **Chunk‑wise traversal.** A cluster box is walked chunk by chunk (intersecting the box with each chunk's 16×16 column, fetching each chunk once through the batch LRU `chunkCache`), then `y` ascending with one section lookup per `y`, then the local `x`/`z` ranges. This avoids a per‑column chunk lookup and a per‑column packed‑key computation.
4. Air states are skipped, and states whose `BlockTempRegistry.getBlockTempsFor(state)` is empty or only `DEFAULT_BLOCK_TEMP` are skipped; the rest are recorded as `Source(pos, state)`.

**Why this is equivalent to the previous (bounding‑box) version:** a block can influence a position only if it lies inside that position's `±range` Chebyshev box (Euclidean distance ≤ range implies every axis difference ≤ range). The union of the per‑position boxes is therefore exactly the set of blocks that can contribute. Blocks in the old bounding box but outside every box were collected, then discarded by the Phase B `distance > range` test — dropping them changes no result.

**Cost:** O(volume of the union of the per‑position boxes) blockstate reads, with no per‑block map traffic; plus one pass of cluster bookkeeping. The scan volume no longer grows with the empty space between distant positions.

### 4.4 Phase B — Per‑source distribution

For every source block, and for every target position:

1. **Pre‑filter** (`areAnyBlockTempsInRange`): skip the pair if the position's accumulated value for these block temps can no longer change (all block temps already present and saturated within `minEffect`/`maxEffect`).
2. The dummy is moved to the position's block centre (the observer), preserving the `entity` argument semantics of `BlockTemp.getTemperature`.
3. Distance is measured from the closest point on the dummy's bounding box to the source block centre (`WorldHelper.getClosestPointOnEntity` + `CSMath.getDistance`); pairs beyond `range` are skipped.
4. Occlusion is counted with `WorldHelper.forBlocksInRay(from, to, level, chunk, stateCache, tracer, 3)`, counting blocks for which `isSpreadBlocked` holds (excluding the source block itself). The shared `stateCache` is filled lazily by the ray helper, which is why it is kept for this phase.
5. For each `BlockTemp` of the source state: `isValid` → `getTemperature(level, entity, state, src, distance)` → accumulate, then **break** — matching `BlockTempModifier`, which takes only the first effective `BlockTemp` per source block.

Accumulation mirrors `BlockTempModifier` exactly: optional `fade()` blending over `(0.5, range)`, logarithmic growth with `LOG_FACTOR = 0.52`, damping by `(blocksOccluding + 1)`, and group‑aware clamping against `minEffect + groupDelta` / `maxEffect − groupDelta`.

### 4.5 Result assembly

`getFunction(pos)` returns a closure that walks the position's accumulated `Map<BlockTemp, Double>` and applies the same rule as the single‑position path: for each `BlockTemp`, if the running temperature is within `[minTemperature, maxTemperature]`, add its effect and clamp. Positions with no contributions get an identity closure, so chain order and behaviour are unchanged.

### 4.6 Comparison with single‑position `BlockTempModifier.calculate()`

| Aspect | Single‑position | Batch (`BlockTempScanBatch`) |
|---|---|---|
| Scan region | full `(2·range)³` per position | union of the per‑position boxes (clustered), once per batch |
| BlockState reads | every block, per position | every block of the union, once per batch |
| BlockState cache in scan | per‑call, cleared each call (always misses within one box) | none needed (no block is visited twice) |
| Occlusion rays | every (position, source) pair within the box | same, but only for pairs within `range` |
| Accumulation | one `Map<BlockTemp, Double>` per call | one `Map<BlockTemp, Double>` per target position |
| Dummy player | re‑assembled per call | shared, repositioned per position |
| Empty‑zone cost | scans regions with no sources | scans only the boxes that can contain sources |

---

## 5. Insulation Batching (`getInsulationAtBatch`)

The single‑position `getInsulationAt(level, pos, 2)` scans `(2·2+1)² = 25` chunks per position. The batch version:

1. normalises and de‑duplicates the positions;
2. builds the **union of touched chunk columns** (each position's chunk ± `chunkRadius`);
3. loads each chunk once, iterates `chunk.getBlockEntitiesPos()` once, and for every `HearthBlockEntity` compares `hearth.getPathLookup().containsKey(pos)` against the batch positions, keeping the **max** cooling/heating level per position;
4. fills `(0, 0)` for positions with no Hearth coverage.

This is a `public static` helper (usable on its own), not a private detail.

---

## 6. Coarse‑Precision Batch Caching

`getRoughTemperatureAt` already caches per 8‑block segment (`TEMPERATURE_CHECKS`, keyed by dimension) with a validity window of `interval / tickSpeedMultiplier` (`interval` = 1000 ticks, or 200 with the Sensitive flag; `tickSpeedMultiplier = 1 + randomTickSpeed / 20`). `getRoughTemperaturesAt` simply walks the batch through this path, so repeated queries for nearby positions and repeated ticks reuse snapshots; expired snapshots recompute the chain for that one position and are stored back. Consumers that do not need per‑block precision (e.g. slow food‑temperature drift) should prefer this API.

---

## 7. Semantics: Threshold Equivalence

The batch path is **threshold‑equivalent** to the single‑position path, not bit‑for‑bit identical:

- the cold/hot direction and magnitude of the result agree in normal scenarios;
- exact floating‑point equality is not guaranteed, because block contributions are accumulated in **dispatch order** (per source block) rather than per‑position scan order, and floating‑point addition is not associative.

Two deliberate, documented differences in evaluation domain:

1. **Distance cut‑off.** The batch accumulates a source only for positions whose Euclidean distance (measured from the observer's closest point to the source centre) is **≤ `range`**. The single‑position path has no explicit cut‑off: it accumulates every source found inside its `(2·range)³` scan box. For `fade()`‑enabled block temps the difference vanishes (the fade blend returns exactly `0` at `distance ≥ range`); for non‑fade block temps it is confined to the box corners, where the single‑position path would still apply the full effect.
2. **Scan box size.** The single‑position scan box is `(2·range)³` per axis‑aligned window `[-range, range)`, while the batch scans the closed `±range` box (i.e. `2·range + 1` blocks per axis) of each cluster, so the batch may additionally consider blocks on the far face of that window.

This contract is stated in the Javadoc of `getTemperaturesAt`, `BlockTempScanBatch` and `getRoughTemperaturesAt`, and is the acceptance basis for consumers such as FIAHI (which only depends on the sign and increments of the temperature).

---

## 8. Performance Characteristics

- **Single position:** unchanged (`getTemperatureAt` keeps its original behaviour and cost).
- **Batch, clustered positions:** scan cost drops from N × (2·range)³ to the volume of the union of the boxes, plus O(source–position pairs within range) for Phase B.
- **Batch, scattered positions (Update 2):** previously the scan covered the full bounding box of all positions, so scattered containers were scanned including all the empty terrain between them; now only the per‑cluster boxes are scanned, and per‑block cache traffic in Phase A is gone entirely.
- **Consumers using the coarse API:** steady‑state cost per tick is a segment‑cache lookup per position, with recomputation spread naturally over ticks as individual segments expire.

---

## 9. Cache & Threading Considerations

- **Cache cleanup:** the existing `clearCachesOnUnload` (on `ServerStoppedEvent`) remains responsible for static caches (`DUMMY_PLAYERS`, `TEMPERATURE_CHECKS`, …). `BlockTempScanBatch` is ephemeral and needs no cleanup.
- **Threading:** the batch APIs are contracted for the **server main thread**; `getTemperaturesAt` additionally throws `IllegalStateException` when called on the logical client. No new concurrency is introduced (existing methods perform no thread check either).
- **No new persistent caches** beyond the existing `TEMPERATURE_CHECKS`; all batch state is collected after the call.

---

## 10. Risks & Mitigations

| Risk | Mitigation |
|---|---|
| **Algorithmic correctness** — Phase B must reproduce single‑position semantics for fade/occlusion/logarithmic/group clamping. | The Phase B logic mirrors `BlockTempModifier` step by step; provide a side‑by‑side harness comparing batch and sequential results over random position sets on a threshold basis. |
| **Clustering edge cases** — diagonal members, differing `y`, single‑position clusters, empty batches. | Union‑find uses the exact `Chebyshev ≤ 2·range` overlap test; a single‑position cluster degenerates to the single‑position box; empty input returns early. |
| **Residual main‑thread latency** — a very large batch still runs in one tick. | Consumers should throttle per dimension (as FIAHI does) and/or use the coarse API; a work‑budget/deferral scheme remains future work. |
| **Memory** — per‑batch maps grow with batch size. | Acceptable for typical container counts; budget controls are post‑launch work. |

---

## 11. Delivery Status

| Round | State |
|---|---|
| Update 1 (batch API, `BlockTempScanBatch` v1, insulation batching, spec) | committed as `811dab1d9` |
| Update 2 (Phase A cluster scan + removal of Phase A blockstate cache) | implemented and verified in‑game; present in the working tree, pending commit |
| Downstream consumption (FIAHI `ForgeEventHandler`, coarse batch per check tick) | implemented in the FIAHI repository |

Verification performed: `compileJava` in both repositories succeeds; in‑game profiling after Update 2 shows the periodic spike removed.
