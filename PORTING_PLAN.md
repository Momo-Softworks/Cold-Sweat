# Cold Sweat 1.16 → 1.7 Port Plan

## Part 0 — How to Use This Document

The 1.16 codebase is the **feature and behavioral gold standard**. Every system in 1.7 should match 1.16 in what it does; the only differences are in *how* it is implemented, due to Minecraft/Forge API differences. Before touching any phase, read the corresponding 1.16 classes first to understand the intended behavior, then implement that behavior using the 1.7 patterns described below.

---

## Part 1 — 1.7 vs 1.16 API Translation Reference

This is the single most important section. Consult it every time a 1.16 API call needs to be rewritten.

### Entity Storage: IExtendedEntityProperties (not Capabilities)
1.16 stores per-entity data using Forge's `ICapability` / `LazyOptional<T>` system. 1.7 uses `IExtendedEntityProperties`. The 1.7 codebase already uses this correctly (`IEntityTempProperty`, `PlayerTempProperty`). Continue this pattern for all new per-entity data.

| 1.16 | 1.7 |
|---|---|
| `AttachCapabilitiesEvent` | `EntityConstructing` event, call `entity.registerExtendedProperties(key, instance)` |
| `entity.getCapability(CAP, null).ifPresent(...)` | `(MyProp) entity.getExtendedProperties(key)` (cast, then null-check) |
| `LazyOptional<ITemperatureCap>` | Direct `IEntityTempProperty` reference |
| `ICapabilityProvider` | `IExtendedEntityProperties` |
| `ITemperatureCap` | `IEntityTempProperty` |
| Syncing via `getUpdatePacket` | Send a custom packet from `onLivingTick` when data changes |

### Block State: Metadata (not BlockState objects)
1.16 uses `BlockState` with typed properties. 1.7 uses a 4-bit integer (0–15) per block stored in the world. The 1.7 codebase already has a wrapper class `util/world/BlockState.java` but actual block state in the world is metadata.

| 1.16 | 1.7 |
|---|---|
| `state.getValue(FACING)` | `world.getBlockMetadata(x, y, z)` → decode facing with `ForgeDirection.getOrientation(meta)` |
| `state.with(LIT, true)` | `world.setBlockMetadataWithNotify(x, y, z, meta, 2)` |
| `IProperty<Direction> FACING` | Encode facing as int 2–5 (matches `ForgeDirection` ordinals) |
| `BlockStateContainer` | N/A — use `getIcon(int side, int meta)` for rendering |
| Blocks with >16 states | Use separate block instances (e.g., `BOILER` / `BOILER_LIT`) or store extra state in the tile entity NBT |

### Registration: FML Lifecycle (not DeferredRegister)
1.16 uses `DeferredRegister<T>` which fires at startup. 1.7 uses the FML event lifecycle.

| 1.16 | 1.7 |
|---|---|
| `DeferredRegister.create(...)` | Direct registration in `@Mod.EventHandler preInit` |
| `GameRegistry.register(block)` or `ForgeRegistries.BLOCKS.register(...)` | `GameRegistry.registerBlock(block, itemBlock, "modid:name")` |
| `ForgeRegistries.ITEMS.register(item)` | `GameRegistry.registerItem(item, "modid:name")` |
| `ForgeRegistries.TILE_ENTITIES.register(...)` | `GameRegistry.registerTileEntity(Clazz.class, "modid:name")` |
| `@ObjectHolder` | Read from `ModBlocks.FIELD` set manually during preInit |
| Block item registration is automatic | Must call `GameRegistry.registerItem(new ItemBlock(block), "name")` for most blocks |

### Serialization: Build a Manual Codec System
1.16 uses Mojang's `Codec` system (auto-serialization) pervasively — not just for save data but as the backbone of the data-driven config records (`BiomeTempData`, `DimensionTempData`, `DepthTempData`, `EntityTempData`, `ItemTempData`, `EntityClimateData`, `InsulationData`, etc.). 1.7 has no codecs.

**Build a manual codec system** that mirrors 1.16's `Codec`/`RecordCodecBuilder` API closely enough to port these data classes verbatim in structure. The intent is to keep 1.16's `data/codec/` file organization and the record classes intact, swapping only the codec *implementation*. A minimal manual codec should support: encode/decode to `NBTTagCompound` and to JSON, optional fields with defaults, list/map fields, and `ResourceLocation`/enum (`StringRepresentable`) fields. This lets the config records, the data-driven `*TempData` classes, and the `EntityClimateTempModifier`/`EntitiesTempModifier`/`InventoryItemsTempModifier` parity work all follow the 1.16 layout rather than ad-hoc `Map<Item, Pair<...>>` shortcuts. Where a manual codec is genuinely overkill (e.g. a single boolean config value), plain `readFromNBT`/`writeToNBT` is still fine.

| 1.16 | 1.7 |
|---|---|
| `RecordCodecBuilder.create(...)` | Manual `nbt.setInteger / nbt.getInteger`, etc. |
| `ResourceLocation` codec | `nbt.setString("id", rl.toString())`, parse back with `new ResourceLocation(str)` |
| `CompoundNBT` | `NBTTagCompound` |
| `ListNBT` | `NBTTagList` |
| `nbt.getList("key", Tag.TAG_COMPOUND)` | `nbt.getTagList("key", 10)` |
| `nbt.putInt / nbt.getInt` | `nbt.setInteger / nbt.getInteger` |
| `nbt.putBoolean / nbt.getBoolean` | `nbt.setBoolean / nbt.getBoolean` |

### Networking: SimpleNetworkWrapper (not SimpleChannel)
The 1.7 codebase already uses `SimpleNetworkWrapper`. Continue this pattern.

| 1.16 | 1.7 |
|---|---|
| `SimpleChannel.registerMessage(...)` | `HANDLER.registerMessage(MessageClass.class, MessageClass.class, id, side)` |
| `FriendlyByteBuf` | `ByteBuf` (via `PacketBuffer` wrapper if available, else raw `ByteBuf`) |
| Static `encode` / `decode` / `handle` | `IMessage.toBytes(ByteBuf)` / `fromBytes(ByteBuf)`, `IMessageHandler.onMessage` |
| `context.get().setPacketHandled(true)` | Return a response message or return null |
| `PacketDistributor.PLAYER.with(...)` | `HANDLER.sendTo(message, (EntityPlayerMP) player)` |
| `PacketDistributor.ALL.noArg()` | `HANDLER.sendToAll(message)` |

### GUI: IGuiHandler (not MenuTypes/Screens)
The 1.7 codebase already registers `ModGuiHandler`. Continue this pattern. Each GUI gets a unique integer ID passed to `player.openGui(modInstance, guiId, world, x, y, z)`.

| 1.16 | 1.7 |
|---|---|
| `MenuType<T>` registered via DeferredRegister | Integer GUI ID, dispatched through `IGuiHandler` |
| `Screen` (client) / `AbstractContainerMenu` (server) | `GuiContainer` (client) / `Container` (server) |
| `NetworkHooks.openGui(player, menuProvider, ...)` | `player.openGui(ColdSweat.INSTANCE, GUI_ID, world, x, y, z)` |
| `ContainerInit.TYPE.get()` | Integer constant in `ModGuiHandler` |

### Events: Forge Event Bus (compatible, syntax slightly different)
1.7 and 1.16 both use MinecraftForge event bus. The main difference is that `@Mod.EventBusSubscriber` did not exist until later; in 1.7 register handlers manually in `init`.

| 1.16 | 1.7 |
|---|---|
| `@Mod.EventBusSubscriber(modid = ..., bus = Mod.EventBusSubscriber.Bus.FORGE)` | `MinecraftForge.EVENT_BUS.register(new HandlerClass())` in `@Mod.EventHandler init` |
| `@Mod.EventBusSubscriber(bus = Bus.MOD)` | `FMLCommonHandler.instance().bus().register(new HandlerClass())` |
| `LivingEvent.LivingTickEvent` | `LivingUpdateEvent` |
| `EntityJoinWorldEvent` | `EntityJoinWorldEvent` (same) |
| `AttachCapabilitiesEvent<Entity>` | `EntityConstructing` |
| `PlayerEvent.Clone` | `PlayerRespawnEvent` (re-attach properties, copy from old entity) |

### Configuration: Forge Config Files (not TOML/Codecs)
1.16 uses `ForgeConfigSpec` and Mojang codecs for data-driven configs. 1.7 uses Forge's `.cfg` system and manual JSON/NBT parsing.

| 1.16 | 1.7 |
|---|---|
| `ForgeConfigSpec.Builder` | `Configuration cfg = new Configuration(file)` |
| `TOML` config files | `.cfg` files (Forge `Configuration` class) |
| Data-pack JSON (e.g., `data/cold_sweat/biome_temp/*.json`) | Custom JSON files loaded in `postInit` via `ConfigHelper` |
| `DynamicHolder<T>` | `ValueHolder<T>` (already in 1.7 codebase) |
| Tag-based item filtering (`ModItemTags`) | Lists of item/block names in config or inline code |
| `ConfigSettings.BOILER_FUEL` (BiMap with codec data) | `ConfigSettings.BOILER_FUEL` (Map<Item, Double>) populated from config |

### Item Capabilities: None in 1.7
1.16 attaches `IInsulatableCap` to armor items to track the insulation items sewn into each armor piece. 1.7 has no item capabilities.

**Do not over-simplify this.** The insulation system is per-item and nuanced — it is NOT a flat sum of cold/hot values. Each armor piece tracks the individual insulation items inserted into it, and each inserted item carries its own type (static vs adaptive), cold/hot split, and (for adaptive) an adaptation state. Read the 1.16 `api/insulation/` package and `ItemInsulationManager`/`ItemInsulationCap` in full before implementing, and mirror that structure.

For storage, **avoid reading "capability" data directly out of raw ItemStack NBT on every access** — decoding NBT per-tick/per-access is costly. Look for a cheaper alternative: maintain a server-side cache/manager (keyed by ItemStack or by a lightweight id) that holds the decoded insulation state and is only re-read from NBT when the stack actually changes (equipment-change events / sewing-table output), analogous to how the capability was a live object in 1.16. The NBT is the persistent backing store; the manager is the hot-path accessor.

### Mixins: Use UniMixins
1.16 uses SpongePowered Mixins extensively. For 1.7, **use UniMixins** ([LegacyModdingMC/UniMixins](https://github.com/LegacyModdingMC/UniMixins)), which backports a working Mixin framework to 1.7.10. Add it as a dependency and write real Mixins where 1.16 uses them. Prefer Forge hooks/events where an equivalent exists (they are simpler and more compatible), but a mixin via UniMixin is now a first-class option rather than a last resort — do NOT hand-roll a CoreMod/ASM transformer.

| 1.16 Mixin | 1.7 Alternative |
|---|---|
| `MixinXPBar` (render XP bar) | `RenderGameOverlayEvent` with priority |
| `MixinFogRender` | `EntityViewRenderEvent.FogColors` / `FogDensity` |
| `MixinHeartRender` | `RenderGameOverlayEvent.Pre` on HEALTH element |
| `MixinLivingLoad` | `EntityConstructing` event |
| `MixinEntityRiding` | `EntityMountEvent` |
| `MixinArmorBreak` | `LivingEquipmentChangeEvent` (or tick-based check) |
| `MixinCampfire` | `BlockTempRegistry` entry for campfire block |
| `MixinIce` | `BlockEvent` hooks or periodic check |
| `MixinMenuChanged` | `PlayerOpenContainerEvent` / container slot watching |

### Tags: Config Lists
1.16 uses item/block/entity tags (`ModItemTags`, `ModBlockTags`, `ModEntityTags`). 1.7 has no tag system. Replace every tag lookup with a config-populated list or `Set<Item>` / `Set<Block>`.

### Commands: CommandBase
1.16 uses Brigadier. 1.7 uses `CommandBase` registered via `ServerCommandManager`.

| 1.16 | 1.7 |
|---|---|
| `Commands.literal("temp")...` | `class TempCommand extends CommandBase` |
| `ServerLifecycleHooks.getCurrentServer()` | `MinecraftServer.getServer()` |
| `FMLEnvironment.dist` | `FMLCommonHandler.instance().getSide()` |

### Advancements: Achievements
1.16 uses advancements with `CriterionTrigger`. 1.7 uses the achievement system (`StatBase`, `AchievementPage`). Implement achievement triggers via events rather than advancement triggers. This is low priority.

### World Gen: BiomeDecorator / WorldGenMinable
1.16 uses `Feature` + `ConfiguredFeature` + `Placement`. 1.7 uses `WorldGeneratorInjectionHook` or subscribing to `DecorateBiomeEvent`.

### Fluids
1.16 has a slush fluid. 1.7 fluid API uses `BlockFluidBase` (Forge). Implement `SlushFluid extends BlockFluidClassic`.

### Sounds
1.16 registers sounds via `SoundInit` (DeferredRegister). 1.7 uses sound files in `resources/assets/modid/sounds/` + `sounds.json` and plays them via `world.playSoundAtEntity(...)` or `world.playSoundEffect(...)`.

---

## Part 2 — Current State of the 1.7 Port

### Already Implemented (verify each before assuming it's complete)
- `ColdSweat.java` — main mod class, FML lifecycle hooks
- `BoilerBlock` / `BoilerTileEntity` / `BoilerContainer` / `BoilerGui` — functional
- `Temperature.java` — Type enum, static utility methods
- `TempModifier.java` — abstract base with NBT, tick rate, expire
- `IEntityTempProperty` / `PlayerTempProperty` — per-player temp storage via IExtendedEntityProperties
- `EntityTempManager` — basic event hooks (EntityConstructing, LivingUpdateEvent, respawn)
- Base temperature modifiers: BiomeTempModifier, BlockTempModifier, `ElevationTempModifier` (renamed from DepthTempModifier), FireTempModifier, FoodTempModifier, FreezingTempModifier, `ArmorInsulationTempModifier` (renamed from InsulationTempModifier), MountTempModifier, SoulLampTempModifier, SoulSproutTempModifier, WaterTempModifier, WaterskinTempModifier, plus ShadeTempModifier/EntitiesTempModifier/InventoryItemsTempModifier/AcclimationTempModifier/SimpleTempModifier (Phase 1/2 work). **Note:** there is no `HearthTempModifier` — it is replaced by `WarmthTempModifier`/`FrigidnessTempModifier` + `ThermalSourceTempModifier` (still to port). `FireTempModifier`/`FreezingTempModifier`/`HearthTempModifier` are 1.7-only names to reconcile against the 1.16 architecture.
- `BlockTemp` system (`BlockTempRegistry`, `BlockTemp`, `BlockTempConfig`, `LavaBlockTemp`)
- `TempModifierRegistry`
- Config system — 5 config classes + `ValueHolder<T>` + `ConfigHelper`
- `ConfigSettings` — partial (many settings present but not all 1.16 settings)
- Networking — `SimpleNetworkWrapper`, `SyncTemperaturesMessage`, `SyncModifiersMessage`
- 4 events: TempModifierEvent, EnableTemperatureEvent, TempModifierRegisterEvent, BlockTempRegisterEvent
- 3 items: `WaterskinItem`, `FilledWaterskinItem`, `ThermometerItem`
- `Overlays.java` — temperature display HUD
- `ModGuiHandler` — GUI dispatch
- `GracePotion`, `ModPotion`
- `SereneSeasonsTempModifier` (compat **stub** — replace with real Serene Seasons API integration; the mod exists for 1.7.10, see 10.1)
- Utility classes: `CSMath`, `Direction`, `BlockPos`, `BlockState`, `WorldHelper`, `ItemHelper`, `EntityHelper`, `NBTHelper`, `ConfigHelper`, `ListBuilder`, `ChatColors`, `TaskScheduler`, `Vec3d/f/i`, `Pair`, `Triplet`, `MapN`, `InterruptableStreamer`, `BlockBounds`, `VoxelShape`, `ChunkPos`
- Registries: `ModBlocks`, `ModItems`, `ModTileEntities`, `ModPotions`, `ModProperties`, `ModDamageSources`, `ModRecipes`
- `RegisterDispenserBehaviors`, `ClientJoinSetup`, `CompatManager`

### Needs Updating (exists but incomplete relative to 1.16)
- `Temperature.java` — missing new traits (COLD_RESISTANCE, HEAT_RESISTANCE, COLD_DAMPENING, HEAT_DAMPENING), missing `Placement` class, missing `Matcher` class, missing `getNeutralWorldTemp`, missing `clearModifiers`, missing overloads
- `TempModifier.java` — missing `tick(entity)` method, missing lifecycle hooks (`onAdded`, `onRemoved`, `onSiblingAdded`, `onSiblingRemoved`), missing generic builder return type `<T extends TempModifier>`, single function (not per-trait array)
- `EntityTempManager` — missing waterskin handling, insulation integration, food handling, mount handling, minecart handling, enchantment handling, sleep handling, death/clone data copy, container change watching, full modifier pipeline
- `ConfigSettings` — missing ~60% of 1.16 settings (insulation maps, entity climate data, advanced biome/dimension configs, temp effects thresholds, etc.)
- `Overlays.java` — needs temperature effects overlays (heat blur, fog, vignette, shiver), match 1.16 visual style
- Networking — `SyncModifiersMessage` and `SyncTemperaturesMessage` need to handle new traits; additional messages needed
- Events — needs `TemperatureChangedEvent`, insulation events, `ItemSwappedInInventoryEvent`, `ContainerChangedEvent`, `DefaultTempModifiersEvent`
- `ModBlocks` / `ModItems` — placeholders for all missing blocks/items

### Missing Entirely (must be created from scratch in 1.7 style)

**Blocks & Tile Entities:**
- `HearthBlock` (two-block multi-block: top + bottom) + `HearthTileEntity` + `HearthContainer` + `HearthGui`
- `IceboxBlock` + `IceboxTileEntity` + `IceboxContainer` + `IceboxGui`
- `ThermolithBlock` + `ThermolithTileEntity`
- `SewingTableBlock` + `SewingContainer` + `SewingGui`
- ~~`SoulSpringLampBlock`~~ — **does not exist; the soulspring lamp is item-only (not placeable)**
- `SmokestackBlock` (attaches above Boiler/Icebox; makes it act like a Hearth — see 3.5)
- `SoulStalkBlock` (world gen plant)
- `SlushBlock` (technical fluid block for hearth/icebox only — **no world generation**)
- `MinecartInsulationBlock` (visual-in-minecart only, **not normally placeable**)

**Items:**
- `SoulSproutItem` (consumable, modifier already exists)
- `SoulspringLampItem` (**held item, not placeable; provides cold, consumes soul sprouts**)
- `MinecartInsulationItem`
- `InsulatedMinecartItem`
- Fur armor items: `ChameleonArmorItem`, `GoatArmorItem`, `HoglinArmorItem`

**Effects (Potions in 1.7):**
- `WarmthPotion` (heating effect)
- `FrigidnessPotion` (cooling effect)
- `IceResistancePotion`
- `GracePotion` — already partially exists

**Temperature Effects System:**
- `TempEffect` base class and `TempEffectType` enum
- Player effects: heat blur, heat fog, heat sway, heat vignette, freeze vignette, freeze hearts, freeze knockback, freeze mine speed, freeze move speed, freeze shiver, freeze healing
- Entity effects: decrease drops, prevent breeding
- `HandleTempEffects` event handler

**Modifiers from 1.16 (port to 1.7):**
- `AcclimationTempModifier` — gradual temperature acclimation over time *(done)*
- `ElevationTempModifier` — renamed from `DepthTempModifier` *(done as rename; full `DEPTH_REGIONS` data system still pending)*
- `ShadeTempModifier` — shade from blocks above *(done)*
- `EntitiesTempModifier` — nearby mobs affect temperature *(done, simplified — needs `EntityTempData` codec for full parity)*
- `InventoryItemsTempModifier` — held/worn items affect temperature *(done, simplified — needs `ItemTempData` codec for full parity)*
- `SimpleTempModifier` — generic command modifier *(done)*
- `EntityClimateTempModifier` — entity-defined climate (needs `ENTITY_CLIMATES` codec data)
- `WarmthTempModifier` / `FrigidnessTempModifier` — heating/cooling, replace the old single `HearthTempModifier`; driven by the warmth/frigidness potions and by hearth/boiler/icebox `ThermalSourceTempModifier`
- `ThermalSourceTempModifier` — applies hearth/boiler/icebox effect to players in range
- `FurnaceBlockTemp` — furnace adds heat to nearby blocks
- `NetherPortalBlockTemp` — nether portal adds heat

**Custom Entities:**
- `ChameleonEntity` — color-changing mob, shearable fur
- `GoatEntity` — ramming mob, shearable fur
- Custom Nether pig entity (substitute for Hoglin, which is post-1.16)

**World Gen:**
- `SoulStalkFeature` — plant generation in Nether
- ~~`SlushFluid` generation~~ — **none; slush does not generate (technical fluid only)**

**Commands:**
- Port the 1.16 temperature command **verbatim**, registered as **`/temperature`** and **`/temp`** (not `/cs temp`)

**NEI Compat:**
- NEI recipe handlers for boiler fuel, hearth fuel, sewing recipes

---

## Part 3 — Phased Implementation Plan

Phases are ordered by dependency. Do not start a phase until all prior phases are complete and verified.

---

### Phase 1 — Core Temperature System (Foundation)

Everything else depends on this being correct.

**1.1 — Expand `Temperature.Type` enum**
- Add `COLD_RESISTANCE`, `HEAT_RESISTANCE`, `COLD_DAMPENING`, `HEAT_DAMPENING` to the `Type` enum (mirroring 1.16's `Trait`)
- Each type needs a flag: `forTemperature` (stored as base temp), `forModifiers` (has modifier list), `forAttributes` (affects resistance/dampening)
- Reference: `api/util/Temperature.java` in 1.16

**1.2 — Expand `TempModifier` base class**
- Add `tick(EntityLivingBase entity)` — called each tick on the entity; default no-op
- Add lifecycle hooks: `onAdded(EntityLivingBase entity, Type type)`, `onRemoved(...)`, `onSiblingAdded(...)`, `onSiblingRemoved(...)`
- Change builder methods to return `<T extends TempModifier> T` so chains work without casts
- Function array: keep single-function approach for WORLD/CORE/BASE/BODY/RATE; the new attribute types (COLD_RESISTANCE, etc.) get their own separate modifier list in the property
- Reference: `api/temperature/modifier/TempModifier.java` in 1.16

**1.3 — Add `Placement` class to `Temperature.java`**
- Port `Placement`, `Mode`, `Order`, `Matcher` inner classes from 1.16 to 1.7
- Update `addModifier`, `addOrReplaceModifier`, `removeModifiers` signatures to use `Placement`
- Reference: `api/util/placement/` in 1.16

**1.4 — Expand `IEntityTempProperty` and `PlayerTempProperty`**
- Add storage for new trait types (COLD_RESISTANCE, HEAT_RESISTANCE, COLD_DAMPENING, HEAT_DAMPENING) as separate `EnumMap` entries
- Add NBT read/write for new types
- Ensure data is copied on player death/respawn (handled in EntityTempManager)

**1.5 — Expand `EntityTempManager`**
This is the largest single task. Reference `common/capability/handler/EntityTempManager.java` in 1.16 thoroughly.
- Add waterskin consumption handling: when `FilledWaterskinItem` is in hotbar/hand, apply `WaterskinTempModifier`
- Add food handling: `FoodData` config map; on `LivingEntityUseItemEvent.Finish`, apply `FoodTempModifier`
- Add insulation handling: on equipment change (`LivingEquipmentChangeEvent`), rebuild `InsulationTempModifier` from armor config
- Add mount handling: on `EntityMountEvent`, apply `MountTempModifier`
- Add sleep handling: on `SleepingLocationCheckEvent`, apply warmth
- Add death/respawn copy: on `PlayerRespawnEvent`, copy all property data from the dead player entity
- Add modifier lifecycle call sites: call `onAdded`/`onRemoved` when modifiers are added/removed
- Add `TemperatureChangedEvent` firing when a temperature value changes
- Add `DefaultTempModifiersEvent` firing on entity join so other mods can add default modifiers

**1.6 — Update all 13 existing TempModifiers**
- Call `super.tick(entity)` pattern where needed
- Verify each modifier's `calculate()` implementation matches 1.16 behavior by reading the corresponding 1.16 modifier file
- `BiomeTempModifier` — must use configurable biome temp map, not hardcoded values
- `DepthTempModifier` → rename/merge into `ElevationTempModifier` to match 1.16
- `FreezingTempModifier` → verify freeze damage threshold matches 1.16

**1.7 — Add missing modifiers**
- `AcclimationTempModifier` — reads/writes acclimation progress to modifier NBT, gradually shifts temp resistance
- `ElevationTempModifier` — reads world height, applies modifier based on config curve
- `ShadeTempModifier` — raycasts upward, checks if sky is blocked
- `EntitiesTempModifier` — scans nearby entities against `EntityClimateData` config map
- `InventoryItemsTempModifier` — scans inventory against `ItemTempData` config map
- `WarmthTempModifier` / `FrigidnessTempModifier` — check for potion effects

**1.8 — Networking updates**
- Update `SyncTemperaturesMessage` to handle new Type values (COLD_RESISTANCE, etc.)
- Update `SyncModifiersMessage` to handle new modifier types and lifecycle
- Add `SyncConfigSettingsMessage` — on player join, server sends config to client (fuel maps, insulation maps, biome temps, etc.) so the client can display correct UI values
- Add `SyncContainerSlotMessage` — needed for container item sync edge cases

---

### Phase 2 — Configuration System

**2.1 — Expand `ConfigSettings`**
Reference `config/ConfigSettings.java` in 1.16 for all missing entries. Add (using `ValueHolder<T>` pattern):
- `INSULATOR_ITEMS` — `Map<Item, Pair<Double,Double>>` (cold insulation, heat insulation per item)
- `INSULATING_ARMORS` — `Map<Item, Pair<Double,Double>>` (separate from item insulators)
- `ENTITY_CLIMATE_TEMPS` — `Map<Class<? extends Entity>, Pair<Double,Double>>` for `EntitiesTempModifier`
- `ITEM_TEMPS` — `Map<Item, Double>` for held-item temperature
- `FOOD_TEMPS` — `Map<Item, Double>` for food temperature effects
- `MOUNT_TEMPS` — `Map<Class<? extends Entity>, Double>` for mount temperature
- `BLOCK_TEMPS` — loaded from config, supplements registry
- `DIMENSION_TEMPS` / `BIOME_TEMPS` / `STRUCTURE_TEMPS` — additional biome/dim overrides
- `BOILER_FUEL`, `ICEBOX_FUEL`, `HEARTH_HOT_FUEL`, `HEARTH_COLD_FUEL` — fuel maps
- All threshold/range settings missing from 1.16 (acclimation time, elevation curve, etc.)
- `COLD_RESISTANCE_ENABLED`, `HEAT_RESISTANCE_ENABLED` — trait toggle flags

**2.2 — Config file loading**
- In 1.7, 1.16's data-driven JSON files become config JSON files loaded at `postInit`
- Expand `ConfigHelper` to load all new map types from JSON
- Use the existing `ConfigHelper` patterns already in the 1.7 codebase
- Config files live in `config/coldsweat/` in the game directory

**2.3 — Config GUI (low priority, do last in this phase)**
- Multi-page `GuiScreen` that lets players configure difficulty, temp thresholds, etc.
- Reference `client/gui/config/` pages in 1.16 for content; re-implement using 1.7 `GuiScreen` and `GuiButton`

---

### Phase 3 — Blocks and Tile Entities

For every block/tile entity in this phase: read the 1.16 implementation first to understand all behaviors, then implement in 1.7 style. All blocks use metadata for facing (2=north, 3=south, 4=west, 5=east, matching vanilla furnace convention).

**3.1 — HearthBlock + HearthTileEntity**
- Two-block structure: `HearthBottomBlock` + `HearthTopBlock` (same TileEntity class; bottom block is authoritative)
- Metadata: bits encode facing direction
- `HearthTileEntity extends TileEntity implements ISidedInventory`:
  - Hot fuel slot (coal, lava bucket, etc.) and cold fuel slot (snowballs, ice, packed ice)
  - `updateEntity()`: consume fuel, spread temperature in radius to players (the core hearth feature — warms/cools players in range)
  - Particle effects: fire particles from front
  - Block state switching (lit/unlit) — match whatever state-update mechanism the 1.16 `HearthBlockEntity`/`BoilerBlock` use; reference the 1.16 code rather than assuming an "UPDATING flag"
  - **`HearthTempModifier` no longer exists** — in 1.16 the hearth/boiler/icebox apply their effect via the `ThermalSourceTempModifier`, and the warming/cooling effects have been split into separate **`WarmthTempModifier`** (heating) and **`FrigidnessTempModifier`** (cooling). Use those, not a single `HearthTempModifier`.
- `HearthContainer` + `HearthGui` — two fuel slots, progress bars
- Register with GUI ID in `ModGuiHandler`
- Reference: `common/block/HearthBottomBlock.java` + `common/blockentity/HearthBlockEntity.java` in 1.16

**3.2 — IceboxBlock + IceboxTileEntity**
- Single block, same facing-metadata convention
- `IceboxTileEntity extends TileEntity implements ISidedInventory`:
  - Fuel slot (ice, packed ice, snowballs)
  - Waterskin slots (same layout as Boiler but for cooling)
  - `updateEntity()`: cool waterskins in slots (set temperature NBT tag to negative values)
  - Block state: **frosted (true/false)**, not lit/unlit
- `IceboxContainer` + `IceboxGui` — mirror of Boiler but for cooling
- Reference: `common/block/IceboxBlock.java` + `common/blockentity/IceboxBlockEntity.java` in 1.16

**3.3 — ThermolithBlock + ThermolithTileEntity**
- Single block. It does **not** render temperature on its face.
- `ThermolithTileEntity`: reads the world temperature at its position periodically and **emits a corresponding redstone signal** (stronger signal for more extreme temperatures). Reference the 1.16 code for the exact signal mapping.
- Reference: `common/block/ThermolithBlock.java` + `common/blockentity/ThermolithBlockEntity.java` in 1.16

**3.4 — Soulspring Lamp — NOT a placeable block (REMOVE this block)**
- The soulspring lamp is **not placeable** — there is no `SoulSpringLampBlock`. It is a held item only (see 4.2). Remove this sub-task.
- It emits **cold, not warmth**.
- It consumes **soul sprouts** as fuel (not soul sand/soil).
- Reference the 1.16 item behavior; do not create a block form.

**3.5 — SmokestackBlock**
- Block that attaches above a Boiler/Icebox.
- It does **not** give a "fuel efficiency bonus." What it actually does: when a smokestack (or stack of smokestacks) is connected above a **Boiler** or **Icebox**, it makes that device behave like a **Hearth** — spreading hot (Boiler) or cold (Icebox) temperature to players in a relatively **smaller** area than the Hearth. Reference the 1.16 code for the exact behavior and area.
- Implement the smokestack **connectivity** behavior (multiple smokestacks stacking/connecting, and the device detecting a valid connected smokestack column above it), matching 1.16.

**3.6 — SewingTableBlock + SewingContainer + SewingGui**
- 1-block crafting station for insulation recipes
- `SewingContainer`: accepts armor in one slot, insulating material in another, outputs insulated armor (with NBT insulation tags)
- `SewingGui`: shows the crafting grid + result slot
- There is **no `SEWING_RECIPES` setting** — that was an incorrect assumption. Sewing recipes are not a standalone config list; the sewing table derives valid insulation from the insulation-item config (`INSULATION_ITEMS` / `ADAPTIVE_INSULATION_ITEMS`) plus the per-armor slot limits. Read the 1.16 `SewingContainer` to see exactly how it determines what can be inserted and how the output is built.
- Reference: `common/block/SewingTableBlock.java` + `common/container/SewingContainer.java` in 1.16

**3.7 — SoulStalkBlock (plant)**
- Simple plant block, grows on soul sand/soil in the Nether
- No TileEntity; implement as a `BlockBush`-style block with nether placement rules

**3.8 — SlushBlock (fluid)**
- `SlushFluid extends BlockFluidClassic`
- Slush is a **purely technical fluid** used internally by the hearth/icebox. It does **NOT generate in the world** (see 7.3) and is not a naturally-occurring world block.
- Implement only the fluid/block itself as the hearth/icebox machinery requires; do not add world placement or "cold damage in cold biomes" behavior.

**3.9 — MinecartInsulationBlock**
- This block is **only the visual shown inside an insulated minecart** — it represents the insulation when rendered in a minecart. It is **not normally placeable** in the world.
- Implement alongside `MinecartInsulationItem`; do not give it standard block placement. Low priority.

---

### Phase 4 — Items

**4.1 — SoulSproutItem**
- Consumable item; on eat, applies `SoulSproutTempModifier` (already exists in 1.7)
- Drops from `SoulStalkBlock`
- Reference: `common/item/SoulSproutItem.java` in 1.16

**4.2 — SoulspringLampItem**
- **Not placeable** — it is a held item only (there is no lamp block).
- When held/active, acts as a portable **cold** source (it provides coldness, NOT warmth) — apply the soul-lamp temp modifier from the carried-item handling.
- Has fuel that is consumed from item NBT; fuel is **soul sprouts** (per `LAMP_FUEL_ITEMS`).
- Reference: `common/item/SoulspringLampItem.java` in 1.16

**4.3 — Fur Armor Items (ChameleonArmorItem, GoatArmorItem, HoglinArmorItem)**
- Custom armor items using `ItemArmor`
- Create custom `ArmorMaterial` enum values for each fur type
- Insulation values for each piece are registered in `ConfigSettings.INSULATING_ARMORS`
- Rendering: override `getArmorTexture(ItemStack, Entity, int, String)` to return custom texture paths
- Reference: `common/item/ChameleonArmorItem.java`, etc. in 1.16

**4.4 — MinecartInsulationItem + InsulatedMinecartItem**
- `MinecartInsulationItem`: placed in a special slot to insulate a rideable minecart
- `InsulatedMinecartItem`: pre-insulated minecart entity item
- Low priority

**4.5 — Update FilledWaterskinItem**
- Verify temperature NBT tag key matches `WaterskinTempModifier` lookup
- Add "Hot Waterskin" / "Cold Waterskin" distinction (temperature > 0 vs < 0) matching 1.16
- Fix tooltip to show temperature value

---

### Phase 5 — Insulation System

**5.1 — Insulation config maps**
- `ConfigSettings.INSULATOR_ITEMS` — map of item → (cold insulation, heat insulation)
- `ConfigSettings.INSULATING_ARMORS` — map of armor item → (cold insulation, heat insulation) per armor slot
- `ConfigSettings.MAX_INSULATION_SLOTS` — per-piece slot limits for sewing table
- Populate defaults matching 1.16's bundled data

**5.2 — ArmorInsulationTempModifier (update existing stub)**
- The modifier is now named `ArmorInsulationTempModifier` (id `cold_sweat:armor`), matching 1.16.
- It must **not** be a simple sum of cold/heat values. Mirror 1.16: it processes the list of insulation entries on each equipped armor piece, where each entry is either **static** (flat) or **adaptive** (shifts between cold/hot based on the environment, with its own stored adaptation state). Apply the result to `Type.COLD_RESISTANCE` / `Type.HEAT_RESISTANCE` (and the dampening traits) as 1.16 does.
- Reference: `api/insulation/` in 1.16 (`Insulation`, `StaticInsulation`, `AdaptiveInsulation`, `InsulationPair`, etc.) for the exact math and the per-item structure. Port these classes following 1.16's file organization.

**5.3 — Item insulation: per-item, not a flat sum (read 1.16 carefully)**
- The insulation system is **per-item and nuanced**, not just `cold_insulation`/`hot_insulation` totals. Each armor piece stores the list of distinct insulation items sewn into it; each of those carries its own static/adaptive type, cold/hot split, and adaptive state. Read the 1.16 `api/insulation/` package + `ItemInsulationCap` before implementing.
- **Avoid reading capability-style data straight from raw ItemStack NBT on the hot path** — decoding NBT every access is costly. Prefer a managed cache (see 5.4) that decodes once on change and is queried cheaply thereafter. NBT remains the persistent backing store, written when the sewing-table output is produced and when insulation changes.

**5.4 — Port `ItemInsulationManager` verbatim (keep 1.16 file organization)**
- Port 1.16's `ItemInsulationManager` (and its associated `ItemInsulationCap`/insulation data classes) as directly as possible, keeping the same class names and file layout under the same package structure 1.16 uses. Do not collapse it into ad-hoc logic inside `EntityTempManager`.
- It is the accessor/cache layer that holds decoded per-item insulation and is refreshed on equipment/inventory change (the 1.7 stand-in for the item capability). Drive the `ArmorInsulationTempModifier` from it.
- Reference: `common/capability/handler/ItemInsulationManager.java` (and `common/capability/insulation/`) in 1.16.

---

### Phase 6 — Temperature Effects System

This system creates visual and mechanical consequences for extreme temperatures.

**6.1 — TempEffect base + TempEffectType**
- Define which effects occur at which temperature threshold (configurable)
- Match 1.16's `TempEffectsData` — a list of effect → threshold pairs in config

**6.2 — Server-side effects**
- Via `HandleTempEffects` logic in `EntityTempManager`'s tick:
  - At cold extremes: reduce movement speed (attribute modifier), reduce mining speed, prevent breeding of nearby animals, reduce item drops from mobs
  - At hot extremes: similar debuffs on heat side
- Apply as vanilla attribute modifiers on the player

**6.3 — Client-side visual effects**
- Hook into `RenderGameOverlayEvent` for:
  - Heat vignette / heat blur (semi-transparent overlay)
  - Freeze vignette (icy vignette at extreme cold)
  - Shiver effect (camera shake — offset render origin slightly)
  - Heat sway (slow sinusoidal camera sway)
- Use `EntityViewRenderEvent.FogColors` and `FogDensity` for heat/cold fog
- Reference `client/gui/Overlays.java` in 1.16 for all visual logic; re-implement using 1.7 render event hooks

**6.4 — Freeze hearts**
- At extreme cold, some heart row becomes ice hearts
- Hook `RenderGameOverlayEvent.Pre` on the HEALTH element, render custom heart texture for "frozen" hearts
- Reference: `api/temperature/effect/player/FreezeHeartsEffect.java` in 1.16

---

### Phase 7 — Events, Commands, and World Gen

**7.1 — Missing events**
Add event classes and fire them at appropriate call sites:
- `TemperatureChangedEvent` — cancellable, fired when any temperature value changes; fire in `Temperature.set()`
- `DefaultTempModifiersEvent` — fired on entity join, lets mods add default modifiers
- `ItemSwappedInInventoryEvent` — fired when a player's hotbar item changes
- `ContainerChangedEvent` — fired when a container slot changes (for waterskin/insulation recalculation)
- `InsulateItemEvent` / `InsulationTickEvent` — fired during insulation application
- `RenderFogEvent` / `RenderWorldEvent` (client) — for fog and world overlay hooks

**7.2 — Commands**
- Port the 1.16 temperature command **verbatim** — do not change its structure, subcommands, or behavior if at all possible. Re-create the same get/set/add (and modifier) functionality using 1.7's `CommandBase`.
- Register it under the command names **`/temperature`** and **`/temp`** (alias). Do **NOT** use a `/cs temp` namespace.
- Register via `ServerCommandManager.addCommand(...)` in the server-start event.
- Reference: `common/command/impl/TempCommand.java` (and the command argument/builder classes) in 1.16.

**7.3 — World Gen**
- `SoulStalkFeature`: grow `SoulStalkBlock` in soul sand valleys (Nether); register via `DecorateBiomeEvent`.
- **No slush generation.** Slush is a purely technical fluid for the hearth/icebox (see 3.8) and must not be generated in the world. Remove any slush world-gen task.

**7.4 — Villager Trades — REMOVE**
- **Do not add villager trades.** Remove this sub-task entirely. (The wandering trader also does not exist in 1.7 and is likewise not ported.)

---

### Phase 8 — Custom Entities

**8.1 — Chameleon Entity**
- `ChameleonEntity extends EntityAnimal`
- Shearable: drops chameleon fur when sheared (crafting material for chameleon armor)
- Color changes based on biome temperature (cosmetic)
- AI: wander, look at player, tempted by certain foods, breed
- Renderer: `RenderChameleon extends RenderLiving` with color-changing texture
- Register via `EntityRegistry.registerModEntity(...)`
- Reference: `common/entity/ChameleonEntity.java` in 1.16

**8.2 — Goat Entity**
- `GoatEntity extends EntityAnimal`
- Shearable: drops goat fur → goat armor
- AI: wander, ram attack
- Reference: `common/entity/GoatEntity.java` in 1.16

**8.3 — Hoglin Fur Substitute**
- Hoglin doesn't exist in 1.7; add a custom `WildboareEntity extends EntityPig` in the Nether
- Drops hoglin-equivalent fur used for hoglin armor
- Alternatively, change the hoglin armor material to use an existing 1.7 mob

---

### Phase 9 — Rendering and UI Polish

**9.1 — Custom armor rendering**
- Each fur armor set needs textures at `textures/models/armor/material_layer_1.png` and `_layer_2.png`
- Override `getArmorTexture(ItemStack stack, Entity entity, int slot, String type)` on each `ItemArmor` subclass

**9.2 — Soul lamp held-item rendering**
- Render flame effect when soul lamp is held in hand
- Use `RenderPlayerEvent.Pre` to draw the flame overlay

**9.3 — Thermometer item tooltip — REMOVE**
- **Do not** add a tooltip showing world temperature when hovering the thermometer. This is not desired behavior and does not match 1.16. Remove this sub-task (and any such tooltip if already added).

**9.4 — Insulation item tooltips**
- Show cold/heat insulation values on armor with insulation NBT
- Implement in each armor item's `addInformation()` method

**9.5 — HUD overlays (final pass)**
- Ensure all visual effects from Phase 6 are polished and match 1.16's `Overlays.java` visual style
- Temperature bar styling should match 1.16

**9.6 — Block entity renderers**
- `TileEntitySpecialRenderer` for Hearth: render animated flame in opening
- `TileEntitySpecialRenderer` for Icebox: render frost/ice texture overlay on top face

---

### Phase 10 — Compatibility

**10.1 — Serene Seasons**
- Serene Seasons **does exist for 1.7.10** — build real compat against its API (do NOT settle for a time-of-world-time stub).
- Implement `SereneSeasonsTempModifier` to read the actual current season/sub-season from the Serene Seasons API and apply the configured seasonal temperature offsets (`SUMMER_TEMPS`/`AUTUMN_TEMPS`/`WINTER_TEMPS`/`SPRING_TEMPS`), matching 1.16's seasonal behavior.
- Gate registration behind `CompatManager.isSereneSeasonsLoaded()` and add the dependency to the build.

**10.2 — NEI (Not Enough Items)**
- 1.7 uses NEI, not JEI
- Implement `IUsageHandler` for boiler fuel, hearth fuel, and sewing recipes
- Reference `compat/jei/` in 1.16 for what to show; re-implement for NEI's API
- Register via `API.register(new BoilerNEIHandler())`

**10.3 — Baubles (equivalent of Curios)**
- If Baubles is available for 1.7, add charm/pendant slot support for insulation accessories
- Implement `IBauble` on relevant items
- Reference: `compat/curios/EquipableCurio.java` in 1.16

**10.4 — Achievement System**
- Add achievements for: first thermometer use, crafting each device, reaching temperature extremes, filling a waterskin
- Reference `data/advancements/` in 1.16 for what advancements exist; re-create as `Achievement` objects on an `AchievementPage`

---

### Phase 11 — Parity Audit of Pre-Existing Code (2.2 → 2.4)

**Why this phase exists:** the 1.7 port was originally based on an **older Cold Sweat (~2.2)**, while the `1.16.5-FG` gold standard is **2.4** and contains many behavioral/structural changes. Components ported before the strict-parity directive may silently retain 2.2 behavior. This phase is a deliberate, file-by-file sweep — NOT a passive "verify when you touch it."

**Method:** for each pre-existing component, read the corresponding `1.16.5-FG` class in full, diff behavior + appearance, log discrepancies, and bring the 1.7 version to 2.4 parity. Skip components written/rewritten *after* the parity directive (they're already 2.4-based) unless a problem is found.

**Known 2.2 → 2.4 deltas already discovered** (treat as a starting list, not exhaustive):
- `Type`/`Trait` enum expanded (FREEZING_POINT/BURNING_POINT + cold/heat resistance & dampening).
- `HearthTempModifier` removed → split into `WarmthTempModifier` + `FrigidnessTempModifier` + abstract `ThermalSourceTempModifier`.
- `DepthTempModifier` → `ElevationTempModifier` (and a data-driven `DEPTH_REGIONS` system not yet ported).
- `InsulationTempModifier` → `ArmorInsulationTempModifier`; insulation reworked to per-item static/adaptive entries.
- Modifier IDs renamed (`biomes`, `blocks`, `food`, `elevation`, `warming`, `cooling`, …).
- Codec-backed data records (`*TempData`) + `DynamicHolder` config.
- Command namespace (`/temperature` + `/temp`).
- New modifiers: Shade, Entities, InventoryItems, Acclimation, Simple, EntityClimate.

**Audit checklist** (mark each: ✅ parity / ⚠️ diverged-fixed / ⏭️ skip-recent / ❓ needs-review):
- Blocks/TE: `BoilerBlock`, `BoilerTileEntity`, `BoilerContainer`, `BoilerGui`
- Base modifiers (math + tick rates): `BiomeTempModifier`, `BlockTempModifier`, `WaterTempModifier`, `FireTempModifier`, `FreezingTempModifier`, `FoodTempModifier`, `MountTempModifier`, `SoulLampTempModifier`, `SoulSproutTempModifier`, `WaterskinTempModifier`
- BlockTemp system: `BlockTempRegistry`, `BlockTemp`, `BlockTempConfig`, `LavaBlockTemp`
- Items: `WaterskinItem`, `FilledWaterskinItem`, `ThermometerItem`
- Networking: `SyncTemperaturesMessage`, `SyncModifiersMessage`
- Events: `TempModifierEvent`, `EnableTemperatureEvent`, `TempModifierRegisterEvent`, `BlockTempRegisterEvent`
- Client/appearance: `Overlays`, `ModGuiHandler`, GUI sizes/textures, `ClientJoinSetup`
- Potions: `GracePotion`, `ModPotion`
- Misc: `RegisterDispenserBehaviors`, `CompatManager`, `TempModifierRegistry`
- Registries: `ModBlocks`, `ModItems`, `ModTileEntities`, `ModPotions`, `ModProperties`, `ModDamageSources`, `ModRecipes`
- Utility (verify only if a consumer reveals a gap): `CSMath`, `WorldHelper`, `EntityHelper`, `ItemHelper`, `NBTHelper`, `ConfigHelper`, math/world helpers

Track results in a living audit log (see `PARITY_AUDIT.md` or memory) so status survives across sessions.

**Sequencing principle (user directive, 2026-06-08): "nip it at the bud"** — bring outdated (~2.2) systems up to 2.4 parity *before* new features build on top of them, to avoid extensive rewrites later. In practice this means, for each audited gap:
1. **If the fix is cheap/contained** (a formula, a missing config read, a divide-by-zero) — fix it immediately during the audit. Done so far: `BiomeTempModifier` (Samples NBT + zero-biome NaN), `ArmorInsulationTempModifier` (math: divisor 60→40, `INSULATION_STRENGTH` multiplier — safe because nothing constructs it yet).
2. **If the fix requires a whole missing subsystem** (capability layer, data-driven codec config, requirement infrastructure) — do NOT half-port it. Schedule it as its own properly-sequenced phase/batch so it's built once, correctly, with its full dependency chain — not retrofitted twice. Examples found so far that need this treatment:
   - **Insulation capability system** (Phase 5, see below) — `Insulation`/`StaticInsulation`/`AdaptiveInsulation`/`ItemInsulationCap`/`ItemInsulationManager`/`InsulatorData` + a `NegatableList`/`*Requirement` config-data layer that doesn't exist in 1.7 at all yet. This is effectively "build the Sewing Table backend early."
   - **BlockTemp data-driven system** — `ConfiguredBlockTemp`/`BlockTempData` codec config + logarithmic accumulation + effect groups. Also: `LavaBlockTemp` may not exist in 2.4 at all (zero `Blocks.LAVA` references found in gold standard's BlockTemp registrations) — needs a user decision on whether to keep it as a 1.7-specific addition or remove it.
   - **Waterskin rewrite** — 2.4 replaced the simple NBT-temperature pour/fill model with fluid-handler/cauldron capability draining, durability-based uses, dispense behaviors, `Placement`-based modifier replacement. Linked to the already-logged `WaterTempModifier` soak/dry rewrite — do both together.
   - **FoodTempModifier override-stacking** — needs `NBTHelper.incrementTag` overload + onAdded/onSibling* hook usage.

See `PARITY_AUDIT.md` for full file-by-file findings and exact dependency chains for each deferred batch.

---

## Part 4 — Implementation Rules (for any AI following this plan)

1. **Read 1.16 first — even for "Already Implemented" files.** Before implementing OR modifying anything, read the corresponding 1.16 class(es) in full. The pre-existing 1.7 code is based on an older (~2.2) Cold Sweat and may have diverged from the 2.4 gold standard, so never assume an existing file is already correct — diff it against `1.16.5-FG` (see Phase 11).
2. **Keep existing 1.7 patterns.** The 1.7 codebase already has good patterns (IExtendedEntityProperties, SimpleNetworkWrapper, ValueHolder config). Extend them; don't replace them with 1.16 patterns.
3. **No direct 1.16/Mojang API calls.** If you find yourself calling `LazyOptional`, Mojang's `com.mojang...Codec`, `DeferredRegister`, or `BlockState.getValue()`, stop and use the 1.7 equivalent from Part 1. (Note: building/using *our own* manual codec system is encouraged — see rule 5 and the Serialization section.)
4. **Metadata over BlockState.** When a block needs multiple states (e.g., facing + lit), encode them into the 4-bit metadata integer if possible. If a block needs more than 16 states, use a second block type or store extra state in the tile entity NBT.
5. **Serialization: manual NBT for tile entities/properties; a manual codec system for data records.** Implement `readFromNBT`/`writeToNBT` explicitly for every tile entity and property class. For the data-driven config records (the `*TempData`/insulation classes that 1.16 defines with `Codec`), build and use a **manual codec system** that mirrors 1.16's `Codec` API so those classes can be ported following 1.16's file organization (see the Serialization section). Do not call Mojang's `Codec`; do not invent ad-hoc `Map<Item, Pair<...>>` substitutes where 1.16 uses a structured record.
6. **Config over data packs.** Any data that 1.16 stores in JSON data packs should be stored in either a `.cfg` file (simple values) or a custom JSON config file parsed by `ConfigHelper`.
7. **Test each phase.** After completing each phase, build and run the mod. Verify the features introduced in that phase work correctly before proceeding.
8. **Match 1.16 behavior, not code.** The goal is behavioral parity — the temperature numbers, insulation math, and game feel should match 1.16 exactly. The code structure will necessarily differ.
9. **Tag 1.16-only features clearly.** KubeJS, Create compat, and 1.16-only vanilla entities (Axolotl, etc.) have no 1.7 equivalent. Note these as N/A and skip them per the table in Part 5.

---

## Part 5 — Quick Reference: What's N/A for 1.7

| 1.16 Feature | 1.7 Status |
|---|---|
| KubeJS scripting compat | N/A — KubeJS doesn't exist for 1.7 |
| Curios accessory slots | Replace with Baubles if available, otherwise skip |
| Wandering trader trades | **Remove** — no wandering trader, and villager trades are also removed (see 7.4) |
| Villager trades | **Remove** — do not add (see 7.4) |
| Create mod compat | N/A |
| Serene Seasons integration | **Supported** — Serene Seasons exists for 1.7.10; build real compat (see 10.1) |
| Mixin framework | **Use UniMixins** (LegacyModdingMC/UniMixins); write real Mixins, prefer Forge hooks where simpler |
| Data packs | Replace with config (`.cfg` + JSON) loaded through the manual codec system |
| Advancement triggers (`CriterionTrigger`) | Replace with achievements (`Achievement`) |
| Codec serialization | **Build a manual codec system** that mirrors 1.16's `Codec` API; port `data/codec/` records following 1.16's layout (see Serialization section) |
| Axolotl / Goat / Hoglin (vanilla 1.17+ mobs) | Goat and Chameleon are custom mod entities — port them; Hoglin → custom Nether pig entity |
| Smithing table recipes (`MixinSmithingRecipe`) | Skip — smithing table is post-1.14 |
| Soul campfire block | Soul campfire doesn't exist in 1.7; add as bonus feature if time permits |
| BetterWeather / Primal Winter compat | Only if those mods have 1.7 versions |
| `ForgeConfigSpec` / TOML configs | Replace with Forge `Configuration` (`.cfg`) + manual JSON parsing |
