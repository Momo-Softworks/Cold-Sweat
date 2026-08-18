package com.momosoftworks.coldsweat.common.blockentity;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.vanilla.BlockStateChangedEvent;
import com.momosoftworks.coldsweat.api.registry.SpreadRuleRegistry;
import com.momosoftworks.coldsweat.api.spread_rule.SpreadContext;
import com.momosoftworks.coldsweat.api.spread_rule.SpreadRule;
import com.momosoftworks.coldsweat.api.temperature.modifier.ThermalSourceTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.event.HearthDebugRenderer;
import com.momosoftworks.coldsweat.common.block.HearthBottomBlock;
import com.momosoftworks.coldsweat.common.block.SmokestackBlock;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.container.HearthContainer;
import com.momosoftworks.coldsweat.common.event.HearthSaveDataHandler;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.BlockEntityInit;
import com.momosoftworks.coldsweat.core.init.ParticleTypesInit;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.HearthResetMessage;
import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.data.tag.ModFluidTags;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.entity.DummyPlayer;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import com.momosoftworks.coldsweat.util.registries.ModFluids;
import com.momosoftworks.coldsweat.util.registries.ModSounds;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.world.SpreadPath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import com.simibubi.create.content.contraptions.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.GlassFluidPipeBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.state.Property;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class HearthBlockEntity extends LockableLootTileEntity implements ITickableTileEntity, ISidedInventory
{
    // List of SpreadPaths, which determine where the Hearth is affecting and how it spreads through/around blocks
    List<SpreadPath> paths = new ArrayList<>(this.getMaxPaths());
    // Maps every attempted position to its live SpreadPath, or null if the position was attempted and rejected (blocked by walls, etc.)
    Map<BlockPos, SpreadPath> pathLookup = new HashMap<>(this.getMaxPaths());
    Map<BlockPos2D, SkylightCheck> seeSkyMap = new HashMap<>(this.getMaxPaths());

    List<EffectInstance> effects = new ArrayList<>();

    AtomicInteger coldFuel = new AtomicInteger();
    AtomicInteger hotFuel = new AtomicInteger();
    FuelFluidHandler fuelFluidHandler = new FuelFluidHandler();
    final LazyOptional<IFluidHandler> fuelFluidHolder = LazyOptional.of(() -> this.fuelFluidHandler);

    NonNullList<ItemStack> items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    Pair<BlockPos, ResourceLocation> levelPos = Pair.of(null, null);
    int x = 0;
    int y = 0;
    int z = 0;

    int lastHotFuel = 0;
    int lastColdFuel = 0;
    boolean isCoolingOn = false;
    boolean isHeatingOn = false;
    boolean usingHotFuel = false;
    boolean usingColdFuel = false;
    int insulationLevel = 0;

    boolean isEntityNearby = false;
    List<LivingEntity> entities = new ArrayList<>();
    int rebuildCooldown = 0;
    boolean forceRebuild = false;
    Set<BlockPos> queuedUpdates = new LinkedHashSet<>();
    public int ticksExisted = 0;

    boolean registeredLocation = false;

    boolean showParticles = true;
    int frozenPaths = 0;
    boolean spreading = true;
    int spreadIndex = 0;
    int partitionSize = 100;

    boolean hasSmokestack = false;
    Map<BlockPos, Direction> pipeEnds = new HashMap<>();

    static final Direction[] DIRECTIONS = Direction.values();

    static Method TICK_DOWN_EFFECT;
    static
    {
        try
        {   TICK_DOWN_EFFECT = ObfuscationReflectionHelper.findMethod(EffectInstance.class, "func_76454_e");
            TICK_DOWN_EFFECT.setAccessible(true);
        }
        catch (Exception ignored) {}
    }

    public HearthBlockEntity()
    {   this(BlockEntityInit.HEARTH_BLOCK_ENTITY_TYPE.get());
    }

    public HearthBlockEntity(TileEntityType<? extends HearthBlockEntity> type)
    {
        super(type);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBlockUpdate(BlockStateChangedEvent event)
    {
        BlockPos pos = event.getPosition();
        World level = event.getWorld();
        BlockState oldState = event.getOldState();
        BlockState newState = event.getNewState();
        if (oldState == null || newState == null) return;

        if (level == this.level
            && (this.seeSkyMap.containsKey(new BlockPos2D(pos.getX(), pos.getZ())) || pos.distSqr(this.getBlockPos()) < Math.pow(this.getMaxRange() + 1, 2))
            && !oldState.getCollisionShape(level, pos).equals(newState.getCollisionShape(level, pos)))
        {   this.sendBlockUpdate(pos);
        }
    }

    /**
     * Range of the Hearth starting from an exit point
     */
    public int getSpreadRange()
    {   return ConfigSettings.HEARTH_RANGE.get();
    }

    /**
     * Range of the Hearth starting from the Hearth's position
     */
    public int getMaxRange()
    {   return ConfigSettings.HEARTH_MAX_RANGE.get();
    }

    /**
     * The maximum volume of blocks the Hearth can spread to
     */
    public int getMaxPaths()
    {   return ConfigSettings.HEARTH_MAX_VOLUME.get();
    }

    /**
     * The maximum fuel the Hearth can hold
     */
    public int getMaxFuel()
    {   return 1000;
    }

    /**
     * The "warm-up" time for the Hearth to reach maximum insulation
     */
    public int getInsulationTime()
    {   return ConfigSettings.HEARTH_WARM_UP_TIME.get();
    }

    public int getCoolingLevel()
    {   return usingColdFuel ? insulationLevel : 0;
    }
    public int getHeatingLevel()
    {   return usingHotFuel ? insulationLevel : 0;
    }

    /**
     * The maximum strength of the Warmth or Chill effect that the hearth can give
     */
    public int getMaxInsulationLevel()
    {   return ConfigSettings.HEARTH_MAX_INSULATION.get();
    }

    /**
     * This must be true for the hearth to calculate spreading
     */
    public boolean hasSmokestack()
    {   return true;
    }

    public boolean isSmartEnabled()
    {   return ConfigSettings.SMART_HEARTH.get();
    }

    public int getFuelDrainInterval()
    {   return ConfigSettings.HEARTH_FUEL_INTERVAL.get();
    }

    public boolean supportsHeating()
    {   return true;
    }
    public List<Direction> getHeatingSides()
    {   return Arrays.asList(Direction.EAST, Direction.SOUTH);
    }
    public boolean isHeatingSide(Direction side)
    {
        if (side == null) return false;
        Direction facing = this.getBlockState().getValue(HearthBottomBlock.FACING);
        Direction rotatedSide = CSMath.directionToRotation(facing).rotate(side);
        return this.getHeatingSides().contains(rotatedSide);
    }

    public boolean supportsCooling()
    {   return true;
    }
    public List<Direction> getCoolingSides()
    {   return Arrays.asList(Direction.WEST, Direction.DOWN);
    }
    public boolean isCoolingSide(Direction side)
    {
        if (side == null) return false;
        Direction facing = this.getBlockState().getValue(HearthBottomBlock.FACING);
        Direction rotatedSide = CSMath.directionToRotation(facing).rotate(side);
        return this.getCoolingSides().contains(rotatedSide);
    }

    @Override
    protected ITextComponent getDefaultName()
    {   return new TranslationTextComponent("container." + ColdSweat.MOD_ID + ".hearth");
    }

    @Override
    public ITextComponent getDisplayName()
    {   return this.getCustomName() != null ? this.getCustomName() : this.getDefaultName();
    }

    @Override
    protected NonNullList<ItemStack> getItems()
    {   return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemsIn)
    {   this.items = itemsIn;
    }

    @Override
    public void tick()
    {
        BlockPos pos = this.getBlockPos();
        // Init the hearth upon first tick
        if (this.ticksExisted == 0)
        {   this.init();
        }

        // Easy access to clientside testList::stream
        boolean isClient = level.isClientSide;

        this.ticksExisted++;

        if (rebuildCooldown > 0) rebuildCooldown--;

        if (this.ticksExisted % 200 == 0)
        {   this.ensurePathSynchronization();
        }

        // Locate nearby entities
        if (this.level != null && this.ticksExisted % 20 == 0)
        {
            this.isEntityNearby = false;
            entities.clear();
            AxisAlignedBB searchArea = new AxisAlignedBB(WorldHelper.sublevelToWorld(level, pos)).inflate(this.getMaxRange());

            for (Entity entity : WorldHelper.getEntities(this.level, searchArea, EntityTempManager::isTemperatureEnabled))
            {
                if (entity instanceof LivingEntity)
                {   this.entities.add((LivingEntity) entity);
                    this.isEntityNearby = true;
                }
            }
        }

        // Tick down the time for each effect
        this.tickPotionEffects();

        if (this.rebuildCooldown <= 0 && !this.queuedUpdates.isEmpty())
        {
            this.rebuildCooldown = 100;
            for (BlockPos updatedPos : this.queuedUpdates)
            {
                seeSkyMap.remove(new BlockPos2D(updatedPos.getX(), updatedPos.getZ()));

                SpreadPath path = this.pathLookup.get(updatedPos);
                if (path != null)
                {   // removePath/removePaths wake up whatever's adjacent to every freed position
                    this.removePaths(path.getChildrenRecursive());
                    this.removePath(path);
                }
                else
                {   // Not currently live — clear any stale rejection marker so it can be reattempted,
                    // and wake up whatever's adjacent so it reconsiders spreading here
                    this.pathLookup.remove(updatedPos);
                    this.wakeNeighbors(updatedPos);
                }
            }
            this.queuedUpdates.clear();
            if (isClient)
            {   HearthDebugRenderer.updatePaths(this);
            }
        }

        if (this.getColdFuel() > 0 || this.getHotFuel() > 0)
        {
            // Gradually increases insulation amount
            if (insulationLevel < this.getInsulationTime())
            {   insulationLevel++;
            }

            if ((this.usingColdFuel || this.usingHotFuel || (this.isSmartEnabled() && this.isEntityNearby)))
            {
                // Determine whether particles are enabled
                if (this.ticksExisted % 20 == 0)
                {
                    showParticles = isClient
                        && Minecraft.getInstance().options.particles == ParticleStatus.ALL
                        && !HearthSaveDataHandler.DISABLED_HEARTHS.contains(levelPos);
                }

                if (paths.isEmpty())
                {   this.traversePipes();
                }

                // Mark as not spreading if all paths are frozen
                this.spreading = this.frozenPaths < paths.size();

                int pathCount = paths.size();
                // Spread to new blocks
                // Only tick paths every 20 ticks for hearths with one or fewer paths (prevents hearths that can't spread causing undue lag)
                if (this.spreading && (this.hasSmokestack && this.paths.size() > 1 || this.ticksExisted % 20 == 0))
                {   this.tickPaths();
                }
                if (isClient && spreading && paths.size() != pathCount)
                {   HearthDebugRenderer.updatePaths(this);
                    this.traversePipes();
                }

                // Give insulation to players
                if (!isClient && this.ticksExisted % 20 == 0)
                {
                    boolean isProvidingInsulation = false;
                    // Provide insulation to players & calculate fuel usage
                    for (int i = 0; i < entities.size(); i++)
                    {
                        LivingEntity entity = entities.get(i);
                        if (entity == null || entity instanceof DummyPlayer) continue;
                        BlockPos entityPos = WorldHelper.worldToSublevel(this.level, entity.blockPosition());
                        // Check if entity is in valid position & insulate
                        if (this.areaContainsEntity(entity) && !WorldHelper.canSeeSky(level, entityPos, 64))
                        {   isProvidingInsulation |= this.insulateEntity(entity);
                        }
                    }
                    if (this.isSmartEnabled() && !isProvidingInsulation)
                    {   this.clearFuelUsage();
                    }
                }

                // Drain fuel
                if (!isClient)
                {   this.tickDrainFuel();
                }

                // Spawn air particles
                if (level.isClientSide)
                {   this.spawnRandomAirParticles();
                }
            }
        }
        // Periodically check for fuel input
        if (this.ticksExisted % 40 == 0)
        {   this.checkForFuel();
        }
        // Ensure correct block state
        if (!isClient)
        {   this.checkForStateChange();
        }
        // Update fuel
        if (!isClient && this.isFuelChanged())
        {   this.updateFuelState();
        }
        // Particles
        if (isClient)
        {   this.tickParticles();
        }
    }

    protected void ensureHasPaths()
    {
        if (this.paths.isEmpty())
        {   SpreadPath startPath = new SpreadPath(this.getBlockPos().above(1), Direction.UP);
            this.addPath(startPath);
        }
    }

    protected <T extends Comparable<T>> void ensureState(Property<T> property, T value)
    {
        BlockState state = this.getBlockState();
        if (state.hasProperty(property) && state.getValue(property) != value)
        {   this.level.setBlock(this.getBlockPos(), state.setValue(property, value), 2);
        }
    }

    public void checkForStateChange()
    {
        // Update state
        if (this.getBlockState().is(ModBlocks.HEARTH_BOTTOM))
        {
            this.ensureState(HearthBottomBlock.SMART, this.isSmartEnabled());
            this.ensureState(HearthBottomBlock.LIT, this.isUsingHotFuel());
            this.ensureState(HearthBottomBlock.FROSTED, this.getColdFuel() > 0);
            this.ensureState(HearthBottomBlock.HEATING, this.isHeatingOn);
            this.ensureState(HearthBottomBlock.COOLING, this.isCoolingOn);
        }
    }

    IChunk workingChunk = null;
    SimpleChunkPos workingChunkPos = null;

    protected void tickPaths()
    {
        this.partitionSize = this.spreading ? CSMath.clamp(this.paths.size() / 3, 100, 1000)
                                            : CSMath.clamp(this.paths.size() / 20, 10, 100);

        // paths.size() can shrink below spreadIndex (e.g. after a large prune), which would otherwise
        // make the loop below never enter its body again, permanently stalling all spreading
        if (this.spreadIndex >= paths.size())
        {   this.spreadIndex = 0;
        }
        int index = this.spreadIndex;
        for (; this.spreadIndex < Math.min(paths.size(), index + partitionSize); this.spreadIndex++)
        {
            // This operation is really fast because it's an ArrayList
            SpreadPath spreadPath = paths.get(this.spreadIndex);
            // Don't try to spread if the path is frozen
            if (spreadPath.frozen)
            {   continue;
            }

            BlockPos pathPos = spreadPath.pos;
            int spX = spreadPath.x;
            int spY = spreadPath.y;
            int spZ = spreadPath.z;

            /*
             Try to spread to new blocks
             */

            if (this.paths.size() < this.getMaxPaths() && spreadPath.withinDistance(spreadPath.origin, this.getSpreadRange())
                && CSMath.withinCubeDistance(spreadPath.pos, this.getBlockPos(), this.getMaxRange()))
            {
                /*
                 Spreading algorithm
                 */
                BlockState state = this.getBlockStateAt(pathPos);

                // Build a map of what positions can see the sky
                BlockPos2D flatPos = new BlockPos2D(spX, spZ);
                SkylightCheck skylightCheck = seeSkyMap.get(flatPos);
                boolean canSeeSky;
                // Recomputes the cached check if the new path is lower than the skylight check and canSeeSky is true (there might be a roof at this lower position)
                // Or the new path is higher than the skylight check and canSeeSky is false (this higher position might be above the roof)
                if (skylightCheck == null || (skylightCheck.y < spY != skylightCheck.canSeeSky()))
                {   seeSkyMap.put(flatPos, new SkylightCheck(spY, canSeeSky = WorldHelper.canSeeSky(level, pathPos.above(), 64)));
                }
                else
                {   canSeeSky = skylightCheck.canSeeSky();
                }

                if (!canSeeSky || SpreadRuleRegistry.get(state).isTransferMedium())
                {
                    // Try to spread in every direction from the current position
                    for (int d = 0; d < DIRECTIONS.length; d++)
                    {
                        Direction direction = DIRECTIONS[d];

                        // Don't try to spread backwards
                        Direction pathDir = spreadPath.direction;
                        if (direction == pathDir.getOpposite()) continue;

                        BlockPos tryPos = pathPos.relative(direction);

                        // Check if this position hasn't been tried before, and if it's spread-able
                        if (!pathLookup.containsKey(tryPos))
                        {
                            BlockState toState = this.getBlockStateAt(tryPos);
                            SpreadContext ctx = new SpreadContext(level, pathPos, state, tryPos, toState, spreadPath.direction, direction);

                            // Add the new path to the list
                            if (SpreadRuleRegistry.get(state).canSpreadTo(ctx))
                            {   SpreadPath newPath = spreadPath.spreadTo(direction);
                                if (SpreadRuleRegistry.get(toState).isTransferMedium())
                                {   newPath.setOrigin(newPath.pos);
                                }
                                this.addPath(newPath);
                            }
                            else
                            {   pathLookup.put(tryPos, null);
                            }
                        }
                    }
                }
                // Remove this path if it has skylight access
                else
                {   pathLookup.remove(pathPos);
                    int last = paths.size() - 1;
                    if (this.spreadIndex < last) paths.set(this.spreadIndex, paths.get(last));
                    paths.remove(last);
                    spreadIndex--;
                    continue;
                }
            }
            // Track frozen paths to know when the Hearth is done spreading
            spreadPath.frozen = true;
            this.frozenPaths++;
        }
    }

    protected void spawnRandomAirParticles()
    {
        if (this.level != null && this.level.isClientSide && showParticles
            && !(Minecraft.getInstance().options.renderDebug && ConfigSettings.HEARTH_DEBUG.get()))
        {
            if (this.paths.isEmpty()) return;
            Random random = this.level.random;
            int count = Math.max(1, this.paths.size() / 100);
            for (int i = 0; i < count; i++)
            {
                SpreadPath path = this.paths.get(random.nextInt(this.paths.size()));
                if (path.pos.equals(this.getBlockPos().above())) continue;
                this.spawnAirParticle(path.x, path.y, path.z, random);
            }
        }
    }

    public void checkInputSignal()
    {
        if (!this.level.isClientSide() && !this.isSmartEnabled())
        {
            // Get signals
            boolean wasHeatingOn = this.isHeatingOn;
            boolean wasCoolingOn = this.isCoolingOn;
            this.isHeatingOn = this.hasHeatingSignal();
            this.isCoolingOn = this.hasCoolingSignal();
            // Calculate fuel usage
            this.usingColdFuel = this.hasSmokestack && this.isCoolingOn && this.getColdFuel() > 0;
            this.usingHotFuel = this.hasSmokestack && this.isHeatingOn && this.getHotFuel() > 0;
            // Update signals for client
            this.syncInputSignal(wasHeatingOn, wasCoolingOn);
        }
    }

    protected boolean hasCoolingSignal()
    {
        Direction facing = this.getBlockState().getValue(HearthBottomBlock.FACING);
        for (Direction side : this.getCoolingSides())
        {
            Direction rotatedSide = CSMath.directionToRotation(facing).rotate(side);
            if (this.level.hasSignal(this.getBlockPos().relative(rotatedSide), rotatedSide))
            {   return true;
            }
        }
        return false;
    }

    protected boolean hasHeatingSignal()
    {
        Direction facing = this.getBlockState().getValue(HearthBottomBlock.FACING);
        for (Direction side : this.getHeatingSides())
        {
            Direction rotatedSide = CSMath.directionToRotation(facing).rotate(side);
            if (this.level.hasSignal(this.getBlockPos().relative(rotatedSide), rotatedSide))
            {   return true;
            }
        }
        return false;
    }

    protected void syncInputSignal(boolean wasHeatingOn, boolean wasCoolingOn)
    {
        // Update signals for client
        if (this.level instanceof ServerWorld && (wasHeatingOn != this.isHeatingOn || wasCoolingOn != this.isCoolingOn))
        {
            ServerWorld serverLevel = (ServerWorld) this.level;
            serverLevel.getChunkSource().blockChanged(this.getBlockPos());
        }
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
        this.checkForFuel();
    }

    public void checkForFuel()
    {
        BlockPos pos = this.getBlockPos();
        ItemStack fuelStack = this.getItems().get(0);
        if (!fuelStack.isEmpty())
        {   // Potion items
            List<EffectInstance> itemEffects = PotionUtils.getMobEffects(fuelStack);
            if (ConfigSettings.HEARTH_POTIONS_ENABLED.get()
                && !itemEffects.isEmpty() && !itemEffects.equals(effects)
                && itemEffects.stream().noneMatch(eff -> ConfigSettings.HEARTH_POTION_BLACKLIST.get().contains(eff.getEffect())))
            {
                if (fuelStack.getItem() instanceof PotionItem)
                {   this.getItems().set(0, Items.GLASS_BOTTLE.getDefaultInstance());
                }
                else if (!fuelStack.hasContainerItem() || fuelStack.getCount() > 1)
                {   fuelStack.shrink(1);
                }
                else
                {   this.getItems().set(0, fuelStack.getContainerItem());
                }

                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BREWING_STAND_BREW, SoundCategory.BLOCKS, 1, 1);
                effects.clear();
                // Convert to NBT and back again to create new instances of the effects (otherwise we would be ticking down the global instances)
                effects.addAll(itemEffects.stream().map(eff -> eff.save(new CompoundNBT())).map(EffectInstance::load).collect(Collectors.toList()));
                WorldHelper.syncBlockEntityData(this);
            }
            else if (fuelStack.getItem() == Items.MILK_BUCKET && !effects.isEmpty())
            {
                this.getItems().set(0, fuelStack.getContainerItem());
                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BUCKET_EMPTY, SoundCategory.BLOCKS, 1, 1);
                effects.clear();
                WorldHelper.syncBlockEntityData(this);
            }
            // Normal fuel items
            else
            {
                int itemFuel = getItemFuel(fuelStack);
                if (itemFuel != 0)
                {   this.storeFuel(fuelStack, itemFuel);
                }
            }
        }
    }

    protected boolean isFuelChanged()
    {
        return this.getColdFuel() != lastColdFuel || this.getHotFuel() != lastHotFuel;
    }

    protected void storeFuel(ItemStack stack, int amount)
    {
        int fuel = amount > 0 ? this.getHotFuel() : this.getColdFuel();
        if (fuel < this.getMaxFuel() - Math.abs(amount) * 0.75)
        {
            if (!stack.hasContainerItem() || stack.getCount() > 1)
            {   int consumeCount = Math.min((int) Math.floor((this.getMaxFuel() - fuel) / (double) Math.abs(amount)), stack.getCount());
                stack.shrink(consumeCount);
                addFuel(amount * consumeCount);
            }
            else
            {   this.setItem(0, stack.getContainerItem());
                addFuel(amount);
            }
        }
    }

    protected void drainFuel()
    {
        if (this.isUsingColdFuel())
        {   this.drainColdFuel(1, true);
        }
        if (this.isUsingHotFuel())
        {   this.drainHotFuel(1, true);
        }
    }

    protected void tickDrainFuel()
    {
        int fuelInterval = this.getFuelDrainInterval();
        if (fuelInterval > 0 && this.ticksExisted % fuelInterval == 0)
        {   this.drainFuel();
        }
    }

    protected void clearFuelUsage()
    {
        if (this.level == null || !this.level.isClientSide)
        {
            this.usingColdFuel = false;
            this.usingHotFuel = false;
        }
    }

    boolean insulateEntity(LivingEntity entity)
    {
        for (int i = 0; i < this.effects.size(); i++)
        {
            EffectInstance effect = this.effects.get(i);
            entity.addEffect(new EffectInstance(effect.getEffect(),
                                                effect.getEffect() == Effects.NIGHT_VISION ? 399 : 119,
                                                effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
        }

        if (!this.isSmartEnabled() || this.shouldInsulateEntity(entity))
        {
            int maxEffect = this.getMaxInsulationLevel() - 1;
            int effectLevel = (int) Math.min(maxEffect, (insulationLevel / (double) this.getInsulationTime()) * maxEffect);
            if (usingColdFuel)
            {   entity.addEffect(new EffectInstance(ModEffects.FRIGIDNESS, 60, effectLevel, false, false, true));
            }
            if (usingHotFuel)
            {   entity.addEffect(new EffectInstance(ModEffects.WARMTH, 60, effectLevel, false, false, true));
            }
            return this.usingColdFuel || this.usingHotFuel;
        }
        return false;
    }

    protected boolean shouldInsulateEntity(LivingEntity entity)
    {
        AtomicBoolean shouldInsulate = new AtomicBoolean(false);
        EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
                                                              {
                                                                  double min = cap.getTrait(Temperature.Trait.FREEZING_POINT);
                                                                  double max = cap.getTrait(Temperature.Trait.BURNING_POINT);
                                                                  double temp = cap.getTrait(Temperature.Trait.WORLD);
                                                                  if (CSMath.betweenInclusive(temp, min, max))
                                                                  {
                                                                      Optional<ThermalSourceTempModifier> existingMod = Temperature.getModifier(entity, Temperature.Trait.WORLD, ThermalSourceTempModifier.class);
                                                                      if (existingMod.isPresent())
                                                                      {
                                                                          double lastInput = existingMod.get().getLastInput(Temperature.Trait.WORLD);
                                                                          double lastOutput = existingMod.get().getLastOutput(Temperature.Trait.WORLD);
                                                                          if (!(lastInput == lastOutput && lastInput == 0))
                                                                          {   temp = lastInput;
                                                                          }
                                                                      }
                                                                  }

                                                                  // Tell the hearth to use hot fuel
                                                                  usingHotFuel |= this.getHotFuel() > 0 && temp < min;
                                                                  // Tell the hearth to use cold fuel
                                                                  usingColdFuel |= this.getColdFuel() > 0 && temp > max;
                                                                  shouldInsulate.set(!CSMath.betweenInclusive(temp, min, max));
                                                              });
        return shouldInsulate.get();
    }

    protected void traversePipes()
    {
        if (this.hasSmokestack && this.level != null)
        {   this.pipeEnds.clear();
            this.ensureHasPaths();
            SpreadPath startPath = this.getPaths().get(0);
            traversePipesRecursive(startPath, this.getBlockStateAt(startPath.pos), new HashSet<>());
        }
    }

    protected void traversePipesRecursive(SpreadPath currentPath, BlockState state, Set<BlockPos> visited)
    {
        visited.add(currentPath.pos);
        SpreadRule fromRule = SpreadRuleRegistry.get(state);

        for (int d = 0; d < DIRECTIONS.length; d++)
        {
            Direction direction = DIRECTIONS[d];
            Direction backwardsDirection = currentPath.direction.getOpposite();
            if (direction == backwardsDirection) continue;

            BlockPos tryPos = currentPath.pos.relative(direction);
            if (visited.contains(tryPos) || !CSMath.withinCubeDistance(this.getBlockPos(), tryPos, this.getMaxRange())) continue;

            BlockState otherState = this.getBlockStateAt(tryPos);
            SpreadRule toRule = SpreadRuleRegistry.get(otherState);
            SpreadContext ctx = new SpreadContext(level, currentPath.pos, state, tryPos, otherState, currentPath.direction, direction);

            if (toRule.isTransferMedium() && fromRule.canSpreadTo(ctx))
            {   SpreadPath tryPath = currentPath.spreadTo(tryPos, direction);
                tryPath.setOrigin(tryPath.pos);
                traversePipesRecursive(tryPath, otherState, visited);
                this.addPath(tryPath);
            }
            else if (!WorldHelper.isSpreadBlocked(level, otherState, tryPos, backwardsDirection, direction)
                && fromRule.canSpreadTo(ctx))
            {   this.pipeEnds.put(tryPos, direction.getOpposite());
            }
        }
    }

    protected void init()
    {
        this.registerLocation();
        this.checkForSmokestack();
        this.checkInputSignal();
        this.level.getLightEngine().checkBlock(this.getBlockPos());
        // Immersive Engineering compat
        if (CompatManager.isImmersiveEngineeringLoaded() && this.supportsHeating())
        {   CompatManager.ImmersiveEngineering.registerHeatableAdapter(this);
        }
    }

    @Override
    public void setRemoved()
    {   super.setRemoved();
        this.cleanup();
    }

    private void registerLocation()
    {
        if (!this.registeredLocation)
        {   levelPos = Pair.of(this.getBlockPos(), level.dimension().location());
            HearthSaveDataHandler.HEARTH_POSITIONS.add(levelPos);
            this.x = this.getBlockPos().getX();
            this.y = this.getBlockPos().getY();
            this.z = this.getBlockPos().getZ();
            this.registeredLocation = true;
        }
    }

    private void unregisterLocation()
    {
        if (this.registeredLocation)
        {   HearthSaveDataHandler.HEARTH_POSITIONS.remove(levelPos);
            this.registeredLocation = false;
        }
    }

    protected void tickPotionEffects()
    {
        if (!effects.isEmpty())
        {
            effects.removeIf(effect ->
                             {
                                 try
                                 {   TICK_DOWN_EFFECT.invoke(effect);
                                     if (effect.getDuration() <=0) return true;
                                 }
                                 catch (Exception ignored) {}
                                 return false;
                             });
        }
    }

    public boolean areaContainsEntity(Entity entity)
    {
        AxisAlignedBB entityBB = entity.getBoundingBox().inflate(-0.1);
        if (entityBB.getSize() <= 1)
        {   entityBB = entityBB.inflate(0.5);
        }
        List<BlockPos> entityPositions = WorldHelper.getPositionsInAABB(entityBB);
        // Valkyrien & Sable compat; translates player's world coords to contraption space
        if (WorldHelper.isInSublevel(this.level, this.getBlockPos()))
        {   entityPositions = entityPositions.stream().map(p -> WorldHelper.worldToSublevel(level, p)).collect(Collectors.toList());
        }
        return this.areaContainsPos(entityPositions);
    }

    public boolean areaContainsPos(List<BlockPos> positions)
    {
        if (positions.isEmpty()) return false;
        for (int i = 0; i < positions.size(); i++)
        {
            BlockPos pos = positions.get(i);
            if (pathLookup.get(pos) != null)
            {   return true;
            }
        }
        return false;
    }

    public void resetPaths()
    {   // Reset cooldown
        this.rebuildCooldown = 100;

        // Clear paths & lookup
        this.paths.clear();
        this.pathLookup.clear();
        this.seeSkyMap.clear();

        // Un-freeze paths so areas can be re-checked
        this.frozenPaths = 0;
        this.spreading = true;

        // Tell client to reset paths too
        this.sendResetPacket();
        if (this.level.isClientSide)
        {   HearthDebugRenderer.updatePaths(this);
        }

        this.queuedUpdates.clear();
    }

    protected void ensurePathSynchronization()
    {
        int trackedLiveCount = 0;
        for (SpreadPath path : this.pathLookup.values())
        {   if (path != null)
        {   trackedLiveCount++;
        }
        }

        Map<BlockPos, SpreadPath> rebuilt = new HashMap<>(this.paths.size());
        int actualFrozen = 0;
        for (SpreadPath path : this.paths)
        {   rebuilt.put(path.pos, path);
            if (path.frozen)
            {   actualFrozen++;
            }
        }

        this.pathLookup = rebuilt;
        this.frozenPaths = actualFrozen;
    }

    public List<EffectInstance> getEffects()
    {   return effects;
    }

    public int getItemFuel(ItemStack item)
    {
        FuelData fuelData = ConfigHelper.getFirstOrNull(ConfigSettings.HEARTH_FUEL, item.getItem(), data -> data.test(item));
        return CSMath.getIfNotNull(fuelData, data -> data.fuel(item), 0);
    }

    public AtomicInteger getFuel(FuelType fuelType)
    {   return fuelType == FuelType.HOT ? this.hotFuel : this.coldFuel;
    }

    public int getHotFuel()
    {   return this.hotFuel.get();
    }

    public int getColdFuel()
    {   return this.coldFuel.get();
    }

    public boolean isUsingColdFuel()
    {   return this.usingColdFuel;
    }

    public boolean isUsingHotFuel()
    {   return this.usingHotFuel;
    }

    protected void addFuel(FuelType fuelType, int amount, boolean update)
    {
        AtomicInteger fuel = this.getFuel(fuelType);
        int oldAmount = fuel.get();
        fuel.set(CSMath.clamp(fuel.get() + amount, 0, this.getMaxFuel()));
        // Update
        if (oldAmount != fuel.get() && update)
        {   this.onFuelChanged(fuelType);
        }
    }

    protected void drainFuel(FuelType fuelType, int amount, boolean update)
    {
        AtomicInteger fuel = this.getFuel(fuelType);
        int oldAmount = fuel.get();
        fuel.set(CSMath.clamp(fuel.get() - amount, 0, this.getMaxFuel()));
        // Update
        if (oldAmount != fuel.get() && update)
        {   this.onFuelChanged(fuelType);
        }
    }

    protected void setFuel(FuelType fuelType, int amount, boolean update)
    {
        AtomicInteger fuel = this.getFuel(fuelType);
        int oldAmount = fuel.get();
        fuel.set(CSMath.clamp(amount, 0, this.getMaxFuel()));
        // Update
        if (oldAmount != amount && update)
        {   this.onFuelChanged(fuelType);
        }
    }

    protected void onFuelChanged(FuelType fuelType)
    {
        boolean nowEmpty = this.getFuel(fuelType).get() == 0;
        if (nowEmpty && this.level != null)
        {   this.level.playSound(null, this.getBlockPos(), this.getFuelDepleteSound(), SoundCategory.BLOCKS, 1, (float) Math.random() * 0.2f + 0.9f);
        }
        this.updateFuelState();
        this.checkInputSignal();
    }

    public void addHotFuel(int amount, boolean update)
    {   this.addFuel(FuelType.HOT, amount, update);
    }
    public void addColdFuel(int amount, boolean update)
    {   this.addFuel(FuelType.COLD, amount, update);
    }

    public void drainHotFuel(int amount, boolean update)
    {   this.drainFuel(FuelType.HOT, amount, update);
    }
    public void drainColdFuel(int amount, boolean update)
    {   this.drainFuel(FuelType.COLD, amount, update);
    }

    public void setHotFuel(int amount, boolean update)
    {   this.setFuel(FuelType.HOT, amount, update);
    }
    public void setColdFuel(int amount, boolean update)
    {   this.setFuel(FuelType.COLD, amount, update);
    }

    /**
     * Negative numbers are cold, positive numbers are hot
     */
    public void addFuel(int amount)
    {   if (amount > 0)
    {   this.addHotFuel(amount, true);
    }
    else if (amount < 0)
    {   this.addColdFuel(Math.abs(amount), true);
    }
    }

    public void updateFuelState()
    {
        if (level != null && !level.isClientSide)
        {   WorldHelper.syncBlockEntityData(this);
            this.lastColdFuel = this.getColdFuel();
            this.lastHotFuel = this.getHotFuel();
        }
    }

    protected SoundEvent getFuelDepleteSound()
    {   return ModSounds.HEARTH_DEPLETE;
    }

    public boolean checkForSmokestack()
    {
        if (level == null) return false;

        BlockState aboveState = this.getBlockStateAt(this.getBlockPos().above());
        boolean hadSmokestack = this.hasSmokestack;
        this.hasSmokestack = aboveState.getBlock() instanceof SmokestackBlock;
        // A smokestack has been added
        if (this.hasSmokestack && !hadSmokestack)
        {   this.registerLocation();
            if (this.level.isClientSide)
            {   ClientOnlyHelper.addHearthPosition(this.getBlockPos());
            }
            this.getBlockState().updateNeighbourShapes(this.level, this.getBlockPos(), 3);
        }
        // A smokestack has been removed
        else if (!this.hasSmokestack && hadSmokestack)
        {
            this.resetPaths();
            this.unregisterLocation();
            if (this.level.isClientSide)
            {   ClientOnlyHelper.removeHearthPosition(this.getBlockPos());
            }
        }
        return this.hasSmokestack;
    }

    @OnlyIn(Dist.CLIENT)
    protected void tickParticles()
    {
        ParticleStatus status = Minecraft.getInstance().options.particles;
        if (!this.hasSmokestack() || status == ParticleStatus.MINIMAL) return;

        Random rand = this.level.random;
        for (Map.Entry<BlockPos, Direction> entry : this.pipeEnds.entrySet())
        {
            BlockPos pos = entry.getKey();
            Direction face = entry.getValue();
            if (this.usingColdFuel)
            {
                if (rand.nextDouble() < CSMath.blend(0, 0.2, this.getColdFuel(), 0, this.getMaxFuel()))
                {   double d0 = pos.getX() + 0.5 + face.getStepX() * 0.35;
                    double d1 = pos.getY() + 0.5 + face.getStepY() * 0.35;
                    double d2 = pos.getZ() + 0.5 + face.getStepZ() * 0.35;
                    double d3 = (rand.nextDouble() - 0.5) / 4;
                    double d4 = (rand.nextDouble() - 0.5) / 4;
                    double d5 = (rand.nextDouble() - 0.5) / 4;
                    level.addParticle(ParticleTypesInit.SMOKESTACK_COLD.get(), d0 + d3, d1 + d4, d2 + d5, 0.0D, 0.04D, 0.0D);
                }
            }
            if (this.usingHotFuel)
            {
                if (rand.nextDouble() < CSMath.blend(0, 0.2, this.getHotFuel(), 0, this.getMaxFuel()))
                {   double d0 = pos.getX() + 0.5 + face.getStepX() * 0.35;
                    double d1 = pos.getY() + 0.5 + face.getStepY() * 0.35;
                    double d2 = pos.getZ() + 0.5 + face.getStepZ() * 0.35;
                    double d3 = (rand.nextDouble() - 0.5) / 4;
                    double d4 = (rand.nextDouble() - 0.5) / 4;
                    double d5 = (rand.nextDouble() - 0.5) / 4;
                    BasicParticleType particle = ParticleTypesInit.SMOKESTACK_WARM.get();
                    level.addParticle(particle, d0 + d3, d1 + d4, d2 + d5, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @Nullable
    public BasicParticleType getAirParticle()
    {
        List<BasicParticleType> options = new ArrayList<>();
        if (this.usingColdFuel)
        {   options.add(ParticleTypesInit.COLD_AIR.get());
        }
        if (this.usingHotFuel)
        {   options.add(ParticleTypesInit.WARM_AIR.get());
        }
        if (options.isEmpty()) return null;
        return options.get(this.level.random.nextInt(options.size()));
    }

    public void spawnAirParticle(int x, int y, int z, Random rand)
    {
        ParticleStatus status = Minecraft.getInstance().options.particles;
        if (status != ParticleStatus.ALL)
        {   return;
        }
        double particleChance = CSMath.blend(0.2f, 0.04f, this.pathLookup.size(), 0, this.getMaxPaths());
        if (rand.nextFloat() > particleChance) return;

        float xr = rand.nextFloat();
        float yr = rand.nextFloat();
        float zr = rand.nextFloat();
        float xm = rand.nextFloat() / 20 - 0.025f;
        float zm = rand.nextFloat() / 20 - 0.025f;

        IParticleData particle = this.getAirParticle();
        if (particle == null) return;
        level.addParticle(particle, false, x + xr, y + yr, z + zr, xm, 0, zm);
    }

    @Override
    public int getContainerSize()
    {   return 1;
    }

    @Override
    protected Container createMenu(int id, PlayerInventory playerInv)
    {   return new HearthContainer(id, playerInv, this);
    }

    @Override
    public void load(BlockState state, CompoundNBT tag)
    {   super.load(state, tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tag, this.items);
        this.loadEffects(tag);
        if (!this.loadFuelOld(tag)) // Legacy handler for old FluidStack fuel storage
        {   this.coldFuel.set(tag.getInt("ColdFuel"));
            this.hotFuel.set(tag.getInt("HotFuel"));
        }
        this.insulationLevel = tag.getInt("InsulationLevel");
    }

    private boolean loadFuelOld(CompoundNBT tag)
    {
        if (tag.get("ColdFuel") instanceof CompoundNBT || tag.get("HotFuel") instanceof CompoundNBT)
        {   this.coldFuel.set(FluidStack.loadFluidStackFromNBT(tag.getCompound("ColdFuel")).getAmount());
            this.hotFuel.set(FluidStack.loadFluidStackFromNBT(tag.getCompound("HotFuel")).getAmount());
            return true;
        }
        return false;
    }

    @Override
    public CompoundNBT save(CompoundNBT tag)
    {   super.save(tag);
        ItemStackHelper.saveAllItems(tag, this.items);
        saveEffects(tag);
        tag.putInt("ColdFuel", this.getColdFuel());
        tag.putInt("HotFuel", this.getHotFuel());
        tag.putInt("InsulationLevel", this.insulationLevel);

        return tag;
    }

    void saveEffects(CompoundNBT tag)
    {
        if (!this.effects.isEmpty())
        {   ListNBT list = new ListNBT();
            for (EffectInstance effect : this.effects)
            {   list.add(effect.save(new CompoundNBT()));
            }
            tag.put("Effects", list);
        }
    }

    void loadEffects(CompoundNBT tag)
    {   this.effects.clear();
        if (tag.contains("Effects"))
        {   ListNBT list = tag.getList("Effects", 10);
            for (int i = 0; i < list.size(); i++)
            {   this.effects.add(EffectInstance.load(list.getCompound(i)));
            }
        }
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        CompoundNBT tag = super.getUpdateTag();
        tag.putInt("HotFuel",  this.getHotFuel());
        tag.putInt("ColdFuel", this.getColdFuel());
        tag.putBoolean("ShouldUseColdFuel", this.usingColdFuel);
        tag.putBoolean("ShouldUseHotFuel", this.usingHotFuel);
        tag.putInt("InsulationLevel", insulationLevel);
        tag.putBoolean("IsCooling", this.isCoolingOn);
        tag.putBoolean("IsHeating", this.isHeatingOn);
        tag.putBoolean("HasSmokestack", this.hasSmokestack);
        this.saveEffects(tag);

        return tag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag)
    {   this.setHotFuel(tag.getInt("HotFuel"), false);
        this.setColdFuel(tag.getInt("ColdFuel"), false);
        this.usingColdFuel = tag.getBoolean("ShouldUseColdFuel");
        this.usingHotFuel = tag.getBoolean("ShouldUseHotFuel");
        this.insulationLevel = tag.getInt("InsulationLevel");
        this.isCoolingOn = tag.getBoolean("IsCooling");
        this.isHeatingOn = tag.getBoolean("IsHeating");
        this.hasSmokestack = tag.getBoolean("HasSmokestack");
        this.loadEffects(tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {   handleUpdateTag(null, pkt.getTag());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {   return new SUpdateTileEntityPacket(this.getBlockPos(), 0, this.getUpdateTag());
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction face)
    {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && face != null
               ? this.isHeatingSide(face) || this.isCoolingSide(face)
                 ? fuelFluidHolder.cast()
                 : super.getCapability(capability, face)
               : super.getCapability(capability, face);
    }

    public void addPath(SpreadPath path)
    {
        // putIfAbsent treats a position previously marked invalid (mapped to null) as claimable too
        if (pathLookup.putIfAbsent(path.pos, path) == null)
        {   paths.add(path);
        }
    }

    public void addPaths(Collection<SpreadPath> newPaths)
    {   paths.addAll(newPaths);
    }

    public void removePath(SpreadPath path)
    {   pathLookup.remove(path.pos);
        paths.remove(path);
        if (path.frozen)
        {   this.frozenPaths--;
        }
        this.wakeNeighbors(path.pos);
    }

    public void removePaths(Collection<SpreadPath> removePaths)
    {
        Set<SpreadPath> toRemove = removePaths instanceof Set ? ((Set<SpreadPath>) removePaths) : new HashSet<>(removePaths);
        for (SpreadPath path : toRemove)
        {   pathLookup.remove(path.pos);
            if (path.frozen) this.frozenPaths--;
        }
        paths.removeAll(toRemove); // now O(n + m) instead of O(n*m)
        for (SpreadPath path : toRemove)
        {   this.wakeNeighbors(path.pos);
        }
    }

    protected void wakeNeighbors(BlockPos pos)
    {
        for (Direction dir : DIRECTIONS)
        {
            BlockPos neighborPos = pos.relative(dir);
            SpreadPath neighbor = this.pathLookup.get(neighborPos);
            if (neighbor != null)
            {   if (neighbor.frozen)
            {   neighbor.frozen = false;
                this.frozenPaths--;
            }
            }
            else this.pathLookup.remove(neighborPos);
        }
    }

    public void sendResetPacket()
    {   if (level instanceof ServerWorld)
    {   ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() ->
                                                                                       (Chunk) WorldHelper.getChunk(level, this.getBlockPos())), new HearthResetMessage(this.getBlockPos()));
    }
    }

    public void sendBlockUpdate(BlockPos pos)
    {   this.queuedUpdates.add(pos);
    }

    protected void cleanup()
    {
        HearthSaveDataHandler.HEARTH_POSITIONS.remove(Pair.of(this.getBlockPos(), this.getLevel().dimension().location()));
        MinecraftForge.EVENT_BUS.unregister(this);
        if (this.level.isClientSide)
        {   ClientOnlyHelper.removeHearthPosition(this.getBlockPos());
        }
    }

    public Map<BlockPos, SpreadPath> getPathLookup()
    {   return this.pathLookup;
    }

    public List<SpreadPath> getPaths()
    {   return this.paths;
    }

    public boolean isSpreading()
    {   return this.spreading;
    }

    public boolean isCoolingOn()
    {   return this.isCoolingOn;
    }

    public boolean isHeatingOn()
    {   return this.isHeatingOn;
    }

    public void setCooling(boolean isPowered)
    {   this.isCoolingOn = isPowered;
    }

    public void setHeating(boolean isPowered)
    {   this.isHeatingOn = isPowered;
    }

    public Map<BlockPos, Direction> getPipeEnds()
    {   return this.pipeEnds;
    }

    @Override
    public int[] getSlotsForFace(Direction side)
    {   return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction pDirection)
    {   return getItemFuel(stack) != 0;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction)
    {   return true;
    }

    public enum FuelType
    {
        COLD(0),
        HOT(1);

        private final int tankIndex;

        FuelType(int tankIndex)
        {   this.tankIndex = tankIndex;
        }

        public int getTankIndex()
        {   return tankIndex;
        }

        public Fluid getFluid()
        {   return this == COLD ? ModFluids.SLUSH : Fluids.LAVA;
        }

        public ITag<Fluid> getValidFluidTag()
        {   return this == COLD ? ModFluidTags.COLD : ModFluidTags.HOT;
        }

        public static FuelType byIndex(int index)
        {
            for (FuelType type : values())
            {
                if (type.getTankIndex() == index)
                {   return type;
                }
            }
            return null;
        }

        public static boolean isValidFluid(Fluid fluid)
        {
            for (FuelType fuelType : values())
            {   if (fluid.is(fuelType.getValidFluidTag())) return true;
            }
            return false;
        }
    }

    public class FuelFluidHandler implements IFluidHandler
    {
        @Override
        public int getTanks()
        {   return 2;
        }

        @Override
        @NotNull
        public FluidStack getFluidInTank(int tank)
        {
            FuelType fuelType = FuelType.byIndex(tank);
            if (fuelType == null) return FluidStack.EMPTY;
            int amount = HearthBlockEntity.this.getFuel(fuelType).get();
            return new FluidStack(fuelType.getFluid(), amount);
        }

        @Override
        public int getTankCapacity(int tank)
        {   return HearthBlockEntity.this.getMaxFuel();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack fluidStack)
        {   return FuelType.isValidFluid(fluidStack.getFluid());
        }

        public int fill(FluidStack fluidStack, FluidAction fluidAction, boolean update)
        {
            FuelType fuelType = fluidStack.getFluid().is(ModFluidTags.COLD) ? FuelType.COLD
                                                                            : fluidStack.getFluid().is(ModFluidTags.HOT)  ? FuelType.HOT
                                                                                                                          : null;
            if (fuelType == null) return 0;

            int space = HearthBlockEntity.this.getMaxFuel() - HearthBlockEntity.this.getFuel(fuelType).get();
            int fillAmount = Math.min(space, fluidStack.getAmount());
            if (fillAmount == 0) return 0;

            if (fluidAction.execute() && fillAmount > 0)
            {   HearthBlockEntity.this.addFuel(fuelType, fillAmount, update);
            }
            return fillAmount;
        }

        @Override
        public int fill(FluidStack fluidStack, FluidAction fluidAction)
        {   return this.fill(fluidStack, fluidAction, true);
        }

        @Override
        public FluidStack drain(int amount, FluidAction fluidAction)
        {   return FluidStack.EMPTY;
        }

        @Override
        @NotNull
        public FluidStack drain(FluidStack fluidStack, FluidAction fluidAction)
        {   return FluidStack.EMPTY;
        }
    }

    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();
        fuelFluidHolder.invalidate();
        if (CompatManager.isImmersiveEngineeringLoaded())
        {   ExternalHeaterHandler.adapterMap.remove(this.getClass());
        }
    }

    protected BlockState getBlockStateAt(BlockPos pos)
    {
        SimpleChunkPos chunkPos = new SimpleChunkPos(pos);
        if (workingChunk == null || workingChunkPos == null || !workingChunkPos.equals(chunkPos))
        {   workingChunk = WorldHelper.getChunk(level, pos);
            workingChunkPos = chunkPos;
        }
        if (workingChunk == null) return level.getBlockState(pos); // Fallback if chunk is still null
        return workingChunk.getBlockState(pos);
    }

    protected static final class SimpleChunkPos
    {
        private final int x;
        private final int z;

        protected SimpleChunkPos(int x, int z)
        {   this.x = x;
            this.z = z;
        }

        public SimpleChunkPos(BlockPos pos)
        {   this(pos.getX() >> 4, pos.getZ() >> 4);
        }

        public int x()
        {   return x;
        }
        public int z()
        {   return z;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass())
                return false;
            SimpleChunkPos that = (SimpleChunkPos) obj;
            return this.x == that.x &&
                this.z == that.z;
        }

        @Override
        public int hashCode()
        {   return Objects.hash(x, z);
        }
    }

    protected static final class BlockPos2D
    {
        private final int x;
        private final int z;

        protected BlockPos2D(int x, int z)
        {   this.x = x;
            this.z = z;
        }

        public int x()
        {   return x;
        }

        public int z()
        {   return z;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass())
                return false;
            BlockPos2D that = (BlockPos2D) obj;
            return this.x == that.x &&
                this.z == that.z;
        }

        @Override
        public int hashCode()
        {   return Objects.hash(x, z);
        }
    }

    protected static final class SkylightCheck
    {
        private final int y;
        private final boolean canSeeSky;

        protected SkylightCheck(int y, boolean canSeeSky)
        {   this.y = y;
            this.canSeeSky = canSeeSky;
        }

        public int y()
        {   return y;
        }
        public boolean canSeeSky()
        {   return canSeeSky;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass())
                return false;
            SkylightCheck that = (SkylightCheck) obj;
            return this.y == that.y &&
                this.canSeeSky == that.canSeeSky;
        }

        @Override
        public int hashCode()
        {   return Objects.hash(y, canSeeSky);
        }
    }
}