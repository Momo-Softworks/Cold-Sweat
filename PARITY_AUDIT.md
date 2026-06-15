# Parity Audit Log (1.7 port vs 1.16.5-FG / 2.4)

Phase 11 of PORTING_PLAN.md. Diffing pre-existing (≈2.2-era) 1.7 components against the `1.16.5-FG` (2.4) gold standard.
Legend: ✅ parity · ⚠️ diverged→fixed · 🔧 diverged→follow-up (logged) · ⏭️ skip (added post-directive, already 2.4) · ❓ not yet reviewed

## Temperature Modifiers
| Component | Status | Notes |
|---|---|---|
| WaterskinTempModifier | ✅ | Aligned NBT key `temperature`→`Temperature`. Logic matches (temp + value). |
| MountTempModifier | ⚠️ | Rewritten to 2.4: ctor `(double coldInsul, double heatInsul)`, NBT `ColdInsulation`/`HeatInsulation`, uses `INSULATION_STRENGTH` + `CSMath.blend(temp,0,insul*strength,0,1)` (was int warming/cooling + `temp/(1+x)`). |
| SoulLampTempModifier | ⚠️ | Rewritten to 2.4: threshold now `(MAX_TEMP + BURNING_POINT offset)*0.99`, strength from `SOULSPRING_LAMP_STRENGTH` (was hardcoded MAX_TEMP*0.99 and *0.4). Provides cooling ✓. |
| FoodTempModifier | 🔧 | 2.4 adds an "Overridden" stacking mechanism (weaker food modifier suppressed when a stronger one is present) via onAdded/onSibling* hooks + `equals()` ignoring the `Overridden` tag. 1.7 is plain `temp+effect`. FOLLOW-UP: port the override logic (needs an `NBTHelper.incrementTag(NBTTagCompound,...)` overload). |
| SoulSproutTempModifier | 🔧 | 2.4 `extends FoodTempModifier` (configurable effect) + spawns SOUL particles. 1.7 is standalone `temp-20` + `magicCrit` particles. FOLLOW-UP: restructure to extend FoodTempModifier; tie effect to item/config (Phase 4); fix particle type. |
| WaterTempModifier | 🔧 | MAJOR divergence. 2.4 = soak/dry-off model: `WATER_SOAK_SPEED`/`RAIN_SOAK_SPEED`/`MAX_RAIN_SOAK`/`DRYOFF_SPEED` config, `WorldHelper.getWaterTemperatureAt`, `CSMath.blendExp`/`shrink`, falling-water particles, fire-extinguish in tick(). 1.7 = simpler strength accumulation + splash particles. FOLLOW-UP: port full system (add 4 config settings + helper methods). |
| FireTempModifier | ✅ (1.7-only) | No 1.16 counterpart — 1.16 handles burning via tick logic, not a registered modifier. Keep as-is. |
| FreezingTempModifier | ✅ (1.7-only) | No 1.16 counterpart. Keep as-is. |
| BiomeTempModifier | ⚠️ | Fixed 2 real bugs: (1) `Samples` NBT was stored but never read — grid sampling was hardcoded to 36; now reads `getNBT().getInteger("Samples")`. (2) divide-by-zero → NaN when `biomeCount==0` (no matching biomes in grid); now falls back to `average(MIN_TEMP,MAX_TEMP)` like 2.4. Core min/max/time-blend math is pre-2.4 style (computed inline) vs 2.4's `WorldHelper.getBiomeTemperature` — equivalent result, just organized differently; left as-is (refactor-only, no behavior diff). NOT ported: structure-temperature override system (`StructureTempData`/`STRUCTURE_TEMPS`/`STRUCTURE_OFFSETS`) and Primal Winter compat — both are new 2.4 features requiring their own config/codec data, logged as outstanding (not a regression). |
| BlockTempModifier | 🔧 | 1.7 is pre-2.4 (~2.2) style: simple per-BlockTemp sum+clamp. 2.4 adds: (a) **logarithmic accumulation** (`LOG_FACTOR`, `Math.pow` curve) for `blockTemp.logarithmic()`, (b) **effect groups** (`ConfiguredBlockTemp`/`BlockTempData.effectGroup()` — multiple BlockTemps share a clamped pool via `groupTotals`/`getGroupTotal`/`updateGroupTotal`), (c) `fade()`/`range()` distance falloff via `CSMath.blend`, (d) `blockTemp.isValid()` gate, (e) advancement triggers (1.7 has these stubbed/commented same as before — fine, matches "no achievements yet"). All of (a)-(d) require the `ConfiguredBlockTemp`/`BlockTempData` codec-config system, which isn't ported — this is a data-driven **feature port**, not a bug fix; deferred alongside BlockTemp system review (already in pending list below). **RESOLVED: `LavaBlockTemp` removed — lava moved to config (matches 2.4).** 2.4 has no hardcoded lava BlockTemp; lava/fire/ice/magma live in the `BLOCK_TEMPERATURES` config default list (per user: "some block temps are defined in config in 1.16 instead of hardcoded Java; 1.7 should match"). 1.7 already had the config-driven path (`WorldSettingsConfig.blockTemps` → `BlockTempConfig` in `TempModifierInit.registerBlockTemps`); only lava was still a Java class. Fix: deleted `LavaBlockTemp.java` + its import/registration; added lava (`minecraft:lava,minecraft:flowing_lava`) to the config default and aligned fire/ice/packed_ice default values to 2.4's (relative MC units = °F/45: lava 0.667/maxEff 4.444, fire 0.556/1.111, ice -0.222/0.533, packed_ice -0.333/1.067). Behavioral note: 2.4 dropped lava's old meta-level scaling (flat value + distance fade now) — 1.7 now matches. Format-parity gaps that 1.7's simpler `.cfg` format still can't express (and which belong to the larger BlockTempData batch, not this fix): per-entry `units` column, `tempLimit`/world-temp min-max gating (meaningful for ice in 2.4), tag (`#`) block selectors, NBT predicates, `logarithmic` flag. Lava's 2.4 `tempLimit=1000°F` is effectively unlimited, so omitting it here is a non-issue.
FOLLOW-UP (smaller, worth checking when resuming): ray direction looks inverted — 1.7 computes `direction = Direction.getNearest(playerClosest.subtract(pos))` (player→block) while 2.4 uses `pos.subtract(playerClosest)` (block→player); verify against `WorldHelper.isSpreadBlocked` semantics before "fixing" since 1.7's version may have been intentionally adapted. |
| ElevationTempModifier | 🔧 | Renamed from Depth (done). Full 2.4 `DEPTH_REGIONS` data system NOT ported (logged in port memory). |

Config added this pass: `INSULATION_STRENGTH` (synced, default 1.0), `SOULSPRING_LAMP_STRENGTH` (default 0.6) + backing fields in ItemSettingsConfig.

## Insulation System (foundational — blocks armor/Sewing Table)
**Finding:** 1.7's `ArmorInsulationTempModifier` is a stub — reads precomputed `cold`/`hot` doubles straight from its own NBT, with formula `temp * (insul>=0 ? 0.1^(insul/60) : -(insul/20)+1)`. 2.4 instead reads from an `ItemInsulationCap` capability on the armor itemstack via `ItemInsulationManager`, applying `INSULATION_STRENGTH` + adaptive blending + armor-defense contribution.

**Re-scoped after reviewing the existing stub:** `ArmorInsulationTempModifier` already carries an explicit comment — *"this is still the early stub; full insulation math (static vs adaptive, slot scanning) is implemented in Phase 5."* This is a deliberate placeholder, not an unintended gap. Per user direction ("port only what's necessary for existing features to have parity — outstanding features come later"), the full `Insulation`/`StaticInsulation`/`AdaptiveInsulation`/`ItemInsulationCap`/`ItemInsulationManager` capability port (~8 files + capability plumbing + `INSULATION_STRENGTH`/armor-defense/adaptive-blend math) belongs to **Phase 5**, not this audit pass.

**UPDATE — fixed the modifier's own math now (cheap win):** Despite the surrounding capability system being complex, `ArmorInsulationTempModifier.calculate()` itself is nearly identical between versions — only the divisor (60→40) and a missing `INSULATION_STRENGTH` multiplier differed. Fixed both (verified nothing constructs this modifier yet, so zero behavioral risk). The *inputs* (`cold`/`hot` NBT values) still come from the future capability scan — that part stays Phase 5.

**Decision: capability/slot system still deferred to Phase 5 as originally planned.** No fix needed now — the stub's simplified formula is a known, intentional placeholder that nothing currently depends on for correctness. Full dependency chain (for when Phase 5 starts) recorded above for reference: `Insulation` (base, codec `either(Static,Adaptive)`, Slot/Type enums, split/combine/sort) → `StaticInsulation` (cold/heat pair) → `AdaptiveInsulation` (factor blend + `InsulationAdaptation` NBT tag + `calculateChange`) → `IInsulatableCap`/`ItemInsulationCap` (List&lt;Pair&lt;ItemStack,List&lt;InsulatorData&gt;&gt;&gt; + adaptive ticking) → `ItemInsulationManager` (static accessors) → capability registration plumbing → rewritten `ArmorInsulationTempModifier`. Skip `ItemInsulationSlotsData`/tooltip/itemgroup/Curio/`ProcessEquipmentInsulation` until the Sewing Table feature itself is ported.

Status: ⏭️ deferred to Phase 5 (intentional, confirmed not a regression).

## Unit Conversion (Temperature.convertUnits) — ⚠️ fixed (high-impact)
1.7's `convertUnits` had the **wrong MC scale factor and ignored the `absolute` flag** on C↔F:
- F↔MC used `/42` / `*42` → should be `/45` / `*45` (2.4: `(value - (absolute?32:0)) / 45`).
- C↔MC used `23.333` → should be `25` (`value / 25d`).
- C→F was unconditional `*1.8 + 32`, F→C unconditional `(value-32)/1.8` → 2.4 gates the `32` offset on `absolute` (delta conversions must not add/subtract 32).

This affected **every** °F/°C ↔ °MC conversion in the mod (config parsing display, all unit-tagged temps), so all such values were silently ~7% off and deltas were mis-offset by 32. Now matches 2.4 verbatim. Also fixed the stale "1°MC = 42°F" comment in `WorldSettingsConfig` block-temp help → "45°F / 25°C". Verified no other hardcoded `42`/`23.33` conversion constants remain in the codebase.

## CSMath — ⚠️ blend swap-guard fixed
1.7's `blend(from,to,factor,min,max)` was missing 2.4's leading `if (rangeMin > rangeMax) return blend(to,from,factor,max,min);` guard — a reversed-range call returned wrong values instead of swapping. Fixed (formula also rewritten to 2.4's identical-but-cleaner form). Rest of CSMath spot-checked (shrink/clamp/sign/minAbs/average/ceil) = consistent. Naming note: 1.7 uses `getSign()`, 2.4 uses `sign()` — behavior identical; rename deferred (churny, many call sites) but flagged for strict-parity.

## Core temperature damage (PlayerTempProperty vs 2.4 AbstractTempCap.tickHurting) — ✅ FIXED
- **Damage hardcoded `2f`** → now reads `ConfigSettings.TEMP_DAMAGE` (new synced setting, default 2) and blends down by HEAT/COLD_RESISTANCE traits (`CSMath.blend(damage,0,resistance,0,1)`).
- **Fixed `% 40` interval** → now reads `ConfigSettings.TEMPERATURE_HURT_INTERVAL` (new synced setting, default 40) AND accelerates via `rateInterval = blend(1,4,rateFactor,0,0.7)` using the rate of temp change captured this tick (only accelerates when body temp is still worsening, matching 2.4).
- Added both config settings (field + `addSyncedSetting` in ConfigSettings, backing fields in ColdSweatConfig under "general"). Not difficulty-scaled (matches 2.4).
- Note: `hurtInterval / rateInterval` can be 0 (modulo-by-zero) if someone sets the interval to 1–3 — left identical to 2.4 (which has the same latent edge case); default 40 is safe.

## Core temperature ACCUMULATION (PlayerTempProperty.tick vs 2.4 AbstractTempCap.tick) — ✅ FIXED (discovered while fixing damage)
Three divergences in the core temp-change loop:
- **COLD/HEAT_DAMPENING never applied.** 1.7 computed the dampening traits but ignored them in the rate calc. Now applies 2.4's dampening logic to `changeBy` (negative dampening amplifies, positive dampening reduces via `blend(changeBy,0,dampening,0,1)`).
- **Dead NBT-resistance multiply removed.** 1.7 multiplied the rate by `(100 - entityData.getInteger("HeatResistance"/"ColdResistance"))/100` — but those NBT ints are *never written* anywhere (grep-confirmed), so it was a permanent `*1.0` no-op left over from ~2.2. Replaced by the dampening system that superseded it.
- **Equilibrium/neutral-return upgraded to 2.4's `getEquilibriumDelta`.** Ported as a private helper: adds the fully-cold/heat-dampened "push body back to 0" cases (1.7 only had the world/core-disagree case), and switched the guard from `getModifiers(CORE).isEmpty()` to 2.4's sign-based guard (`coreDeltaSign == 0 || coreDeltaSign == equilibriumSign`).
- Also fixed `TEMP_RATE.get().floatValue()` → `.get()` (double) to match 2.4's precision.
- Minor deferred deviations (non-behavioral): 2.4 stores RATE as a synced trait (1.7 keeps it local — nothing reads getTemp(RATE)); 2.4's `getEquilibriumDelta` reads previous-tick stored WORLD/min/max traits (1-tick lag quirk) while 1.7 uses fresh locals (cleaner, negligible difference); entity-climate rate multiplier not applied (new 2.4 feature, deferred).

## Temperature.apply (modifier application core) — ⚠️ fixed
- **`MODIFIER_TICK_RATE` multiplier missing.** 2.4 divides each modifier's tick rate by `ConfigSettings.MODIFIER_TICK_RATE` (a synced perf setting, default 1.0); 1.7 used the raw tick rate with no such config. Added the setting (ConfigSettings + ColdSweatConfig "general", default 1.0) and the `ignoreTickMultiplier` overloads, applying the multiplier. At default 1.0 behavior is unchanged; players can now tune it (parity).
- **First-tick recompute guard missing.** Added `|| entity.ticksExisted <= 1` to match 2.4 (forces modifiers to compute on the entity's first tick).
- Added `Math.max(1, tickRate)` modulo guard — intentional safe deviation: prevents a divide-by-zero crash 2.4 is actually vulnerable to when `MODIFIER_TICK_RATE` > a modifier's tick rate. Default-safe and only differs from 2.4 in the crash case.
- Naming: 1.7 `modifier.getResult()` == 2.4 `modifier.apply()` (cached-result accessor) — behavior identical, rename deferred.
- `WorldHelper`-style `getTemperatureAt` probe now passes `ignoreTickMultiplier=true` (matches 2.4; moot here since fresh modifiers always recompute, but exact).

## getTemperatureAt (dummy-entity world-temp probe) — 🔧 approach divergence, partially blocked
2.4's `WorldHelper.getTemperatureAt` reads the dummy's *registered* WORLD modifiers (`Temperature.getModifiers(dummy, WORLD)`) **plus hearth insulation** (`getInsulationAt`). 1.7 hardcodes `[BiomeTempModifier(9), ElevationTempModifier, BlockTempModifier]` (9 biome samples vs the 16 a real player uses). Consequence: waterskin/thermometer readings can differ slightly from the player's actual world temp, and won't reflect hearths. FOLLOW-UP: rewrite to use registered WORLD modifiers; the hearth-insulation portion is **blocked on Hearth** (unported, Phase 3) — revisit when Hearth lands.

## Not yet reviewed (pending — STILL UNAUDITED, do not assume parity)
- Blocks/TE: BoilerBlock, BoilerTileEntity, BoilerContainer, BoilerGui
- BlockTemp system: BlockTempRegistry, BlockTemp, BlockTempConfig, LavaBlockTemp
- ~~Items: WaterskinItem, FilledWaterskinItem, ThermometerItem~~ → reviewed: 2.4 versions are full feature rewrites (fluid-handler/cauldron capability draining, durability-based uses, dispense behaviors, `Placement`-based modifier replacement, tooltips) vs 1.7's simple NBT-temperature pour/fill model (~2.2-era). Not a parity bug — it's the same gap pattern as Waterskin/Mount/SoulLamp already logged: old feature needs a full rewrite to the new system. **Deferred as a feature-port batch** (Waterskin rewrite + WaterTempModifier soak/dry system are linked — do together later). ThermometerItem is fine as-is (just icon/texture lookup, no 2.4 structural changes found).
- Networking: SyncTemperaturesMessage, SyncModifiersMessage
- Events: TempModifierEvent, EnableTemperatureEvent, TempModifierRegisterEvent, BlockTempRegisterEvent
- Client/appearance: Overlays, ModGuiHandler, GUI sizes/textures, ClientJoinSetup
- Potions: GracePotion, ModPotion
- Misc: RegisterDispenserBehaviors, CompatManager, TempModifierRegistry
- Registries: ModBlocks, ModItems, ModTileEntities, ModPotions, ModProperties, ModDamageSources, ModRecipes
