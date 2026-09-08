# Cold-Sweat Batch Temperature Query API – Specification

## 1. Problem Statement

The existing `WorldHelper.getTemperatureAt(Level, BlockPos)` computes the world temperature for a **single** position. It performs:

- 25‑chunk scan for `HearthBlockEntity` insulation,
- full `(2·range)³` (≈2744 blocks) volume scan for block temperature modifiers,
- 49 biome sample queries,
- dummy player WORLD modifier chain assembly.

When many containers (e.g., in a storage room or ice‑box array) request temperatures individually, the cost scales as **N × S**, where N is the number of containers and S is the per‑position scan size. In clustered scenarios this is effectively O(N²). To mitigate this, we provide **batch APIs** that share expensive computations across many query positions.

---

## 2. Proposed API Additions (in `WorldHelper`)

### 2.1 Full‑precision batch API
Semantically equivalent to `getTemperatureAt`, but for multiple positions at once.

```java
/**
 * Computes the world temperature for a batch of BlockPositions in the same Level.
 * <p>
 * Reuses dummy player modifier chain, block temperature volume scan across the union of
 * queried areas, and chunk‑wise insulation lookups to reduce repeated work.
 *
 * @param level     the Level (must be server‑side, called on main thread)
 * @param positions a collection of BlockPos (duplicates are deduplicated)
 * @return an Object2DoubleMap mapping each unique position to its world temperature
 *         (in Minecraft temperature units)
 * @throws IllegalStateException if called on logical client or off the server thread
 */
public static Object2DoubleMap<BlockPos> getTemperaturesAt(
    Level level,
    Collection<BlockPos> positions
)
```

### 2.2 Coarse‑precision batch API
Semantically equivalent to `getRoughTemperatureAt`, reusing the 8‑block segment cache.

```java
/**
 * Computes rough world temperatures for a batch of positions, aligning with
 * getRoughTemperatureAt semantics and using the same segment cache.
 *
 * @param level     the Level
 * @param positions a collection of BlockPos
 * @param flags     cache flags (1=Sensitive, 2=Force Update) as in getRoughTemperatureAt
 * @return an Object2DoubleMap mapping each unique position to its rough temperature
 */
public static Object2DoubleMap<BlockPos> getRoughTemperaturesAt(
    Level level,
    Collection<BlockPos> positions,
    int flags
)
```

**Return type:** Both use `it.unimi.dsi.fastutil.objects.Object2DoubleMap<BlockPos>` (fastutil), aligned with project usage.

**Threading:** Must be called on the server main thread. Javadoc shall explicitly state this.

---

## 3. Internal Implementation – `BlockTempScanBatch`

The core of the batch optimisation is a **temporary, per‑call scanning context** that replaces the per‑position full‑volume scan with a **union scan + per‑source distribution**.

### 3.1 Design Principles

- **Not a singleton / cache:** It is instantiated fresh inside each `getTemperaturesAt` call, used only for that batch, and discarded after `buildResult()`.
- **No cross‑tick pollution:** No static fields; safe from memory leaks and concurrency issues (server main thread only).
- **Does not modify existing `BlockTempModifier` behaviour:** The original `BlockTempModifier.calculate()` remains unchanged; the batch uses a parallel code path.

### 3.2 Class Structure

```java
class BlockTempScanBatch {
    private final Level level;
    private final Collection<BlockPos> positions;   // deduplicated
    private final int range;                        // = BLOCK_RANGE
    private final Map<Long, ChunkAccess> chunkCache; // LRU, per‑batch
    private final Long2ObjectOpenHashMap<BlockState> stateCache; // per‑batch
    // Per‑target‑pos accumulators: BlockTemp → accumulated value
    private final Map<BlockPos, Map<BlockTemp, Double>> totals;
    // Group accumulators for group‑wise clamping
    private final Map<BlockPos, Map<TagKey<BlockTempData>, Double>> groupTotals;
    // Temporary: source blocks (non‑air with blockTemp) found in union scan
    private final Long2ObjectOpenHashMap<Collection<BlockTemp>> srcBlocks;

    void scan();   // runs the two‑phase algorithm
    Object2DoubleMap<BlockPos> buildResult(); // clamps and returns final temps
}
```

### 3.3 Two‑Phase Algorithm

**Phase A – Union volume scan (read blockstates once):**
- Iterate over the **union of chunks** covered by all target positions.
- For each block, read `blockstate` (via `stateCache`; shared across nearby positions).
- Skip air / blocks with no `BlockTemp` (or only `DEFAULT_BLOCK_TEMP`).
- Store matching source blocks and their `BlockTemp` collection into `srcBlocks`.
- **Cost:** O(volume of union), not O(N × volume).

**Phase B – Per‑source distribution (only within influence radius):**
- For each source block and each of its `BlockTemp`:
    - Pre‑filter: skip if no target position is within `blockTemp.range()`.
    - For each target position within range:
        - Compute distance (same as `WorldHelper.getClosestPointOnEntity` semantics).
        - Compute occlusion via `forBlocksInRay` (ray from target to source) – this uses the shared `stateCache` and is called only for actual (source, target) pairs that are within range.
        - Apply `fade()` and logarithmic rules, accumulate into `totals[pos][blockTemp]` and `groupTotals[pos][group]`.
- **Cost:** O(number of source–target pairs within influence radius), which is far less than N × full volume when containers are clustered.

**Clamping (`buildResult`):**
For each target position, take the accumulated `Map<BlockTemp, Double>` and apply the same clamping logic as the single‑pos `calculate()` (clamp each BlockTemp’s contribution according to its min/max temperature and effect, then sum and clamp final). Output to `Object2DoubleMap<BlockPos>`.

### 3.4 Comparison with Single‑pos `BlockTempModifier.calculate()`

| Aspect | Single‑pos | Batch (`BlockTempScanBatch`) |
|--------|------------|------------------------------|
| Volume traversal | Full `(2·range)³` per position | Union of all target positions once |
| BlockState reads | One read per block per position | One read per block (shared via cache) |
| Occlusion rays | For every (pos, source) combination | Same, but only for pairs within influence radius |
| Accumulation | Single `Map<BlockTemp,Double>` per call | One `Map<BlockTemp,Double>` per target position |
| Dummy player | Re‑assembled per call | Shared across the batch |

---

## 4. Insulation Batching (`getInsulationAtBatch`)

The existing `WorldHelper.getInsulationAt` scans 25 chunks per position. The batch version:

- Collects the union of all chunks touched by the batch positions.
- For each chunk, loads it once, iterates its `BlockEntities` once, and records `HearthBlockEntity` contributions (cooling/heating levels) into a map keyed by position.
- Returns an `Object2DoubleMap<BlockPos>` with the insulation‑derived temperature offset for each target position.

This is a **private internal method** used by `getTemperaturesAt`.

---

## 5. Coarse‑precision Batch Caching

`getRoughTemperatureAt` already uses a segmented cache (`TEMPERATURE_CHECKS`) with 8‑block granularity. The batch version simply iterates over the positions, checks the cache for each segment, and returns cached values (or computes and stores) – no additional shared structures needed.

---

## 6. Cache & Threading Considerations

- **Cache cleanup:** Existing `clearCachesOnUnload` (called on `ServerStoppedEvent`) remains responsible for cleaning any static caches. The `BlockTempScanBatch` is **ephemeral** and requires no static cleanup.
- **Thread safety:** All batch APIs are intended for server main thread only. They do not introduce new concurrency.
- **No new persistent caches** beyond the existing `TEMPERATURE_CHECKS`; all batch‑specific state is garbage‑collected after the call.

---

## 7. Acceptance Criteria

**Correctness:**
- For any given set of positions, the batch APIs produce results that are **functionally identical** (within floating‑point tolerance) to calling the single‑pos equivalents in a loop.
- Existing `getTemperatureAt` and `getRoughTemperatureAt` behaviour remains unchanged (regression tests required).

**Performance:**
- In clustered scenarios (e.g., many containers in a small area), the batch API shows a significant reduction in wall‑clock time compared to individual calls.
- Profiling (e.g., Spark) should show the volume scan cost dropping from O(N × V) to O(V + N × pairs_in_range), which is near O(N) for dense clusters.

**Regression:**
- No impact on insulation lookup, biome sampling, or other temperature modifiers.

---

## 8. Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| **Algorithm correctness** – phase B must match single‑pos semantics exactly, including fade/occlusion/logarithmic/group clamping. | Provide a side‑by‑side test harness that compares batch results to sequential results for random position sets. |
| **Main‑thread latency** – union scan could still be heavy for huge batches. | Add an optional batch size limit or deferral mechanism (future work); initial implementation can rely on the existing tick‑interval throttling (already applied by FIAHI). |
| **Memory** – per‑batch maps can grow with batch size. | Acceptable for typical container counts (< few thousand); large worlds may need budget controls (post‑launch). |

---

## 9. Delivery Plan

1. **Implement** `BlockTempScanBatch` and the public batch APIs in `WorldHelper`.
2. **Add** private batching helpers for insulation and dummy player reuse.
3. **Test** thoroughly against sequential equivalents; include unit tests and integration tests.
4. **Release** a new Cold‑Sweat version (with the new APIs) before FIAHI can upgrade its dependency.