package com.momosoftworks.coldsweat.common.blockentity;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.vanilla.BlockStateChangedEvent;
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
import com.momosoftworks.coldsweat.util.math.FastMap;
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
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class HearthBlockEntity extends LockableLootTileEntity implements ITickableTileEntity, ISidedInventory
                                                                         {
    // List of SpreadPaths, which determine where the Hearth is affecting and how it spreads through/around blocks
    List<SpreadPath> paths = new ArrayList<>(this.getMaxPaths());
    // Used as a lookup table for detecting duplicate paths (faster than ArrayList#contains())
    Set<BlockPos> pathLookup = new HashSet<>(this.getMaxPaths());
    Map<Pair<Integer, Integer>, Pair<Integer, Boolean>> seeSkyMap = new FastMap<>(this.getMaxPaths());

    List<EffectInstance> effects = new ArrayList<>();

    FuelFluidHandler hotFuelHandler = new FuelFluidHandler(FuelType.HOT);
    final LazyOptional<IFluidHandler> hotFuelHolder = LazyOptional.of(() -> {
        return this.hotFuelHandler;
    });
    FuelFluidHandler coldFuelHandler = new FuelFluidHandler(FuelType.COLD);
    final LazyOptional<IFluidHandler> coldFuelHolder = LazyOptional.of(() -> {
        return this.coldFuelHandler;
    });

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
    List<BlockPos> queuedUpdates = new ArrayList<>();
    public int ticksExisted = 0;

    boolean registeredLocation = false;

    boolean showParticles = true;
    int frozenPaths = 0;
    boolean spreading = true;

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
        && this.pathLookup.contains(pos)
        && !oldState.getCollisionShape(level, pos).equals(newState.getCollisionShape(level, pos)))
        {
            if (!level.isClientSide())
            {   this.sendBlockUpdate(pos);
            }
            if (isTransferPipe(oldState) || isTransferPipe(newState))
            {   this.searchForPipeEnds(this.getBlockPos().above(), Direction.UP);
            }
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

        // Locate nearby entities
        if (this.level != null && this.ticksExisted % 20 == 0)
        {
            this.isEntityNearby = false;
            entities.clear();
            AxisAlignedBB searchArea = new AxisAlignedBB(pos).inflate(this.getMaxRange());
            if (CompatManager.isValkyrienSkiesLoaded())
            {   searchArea = CompatManager.Valkyrien.transformIfShipPos(level, searchArea);
            }

            for (Entity entity : this.level.getEntities((Entity) null, searchArea, EntityTempManager::isTemperatureEnabled))
            {
                if (entity instanceof LivingEntity)
                {   this.entities.add((LivingEntity) entity);
                    this.isEntityNearby = true;
                }
            }
        }

        // Tick down the time for each effect
        this.tickPotionEffects();

        // Determine what types of fuel to use
        if (!this.usingColdFuel && !this.usingHotFuel && !this.paths.isEmpty())
        {   this.forceUpdate();
        }

        // Reset if a nearby block has been updated
        if (forceRebuild || (rebuildCooldown <= 0 && !this.queuedUpdates.isEmpty()))
        {   this.resetPaths();
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
                {   this.addPath(new SpreadPath(pos.above(1)).setOrigin(pos.above(1)));
                    pathLookup.add(pos.above(1));
                    this.searchForPipeEnds(this.getBlockPos().above(), Direction.UP);
                }

                // Mark as not spreading if all paths are frozen
                this.spreading = this.frozenPaths < paths.size();

                /*
                 Partition the points into logical "sub-maps" to be iterated over separately each tick
                */
                int pathCount = paths.size();
                // Size of each partition (sub-list) of paths
                int partSize = spreading ? CSMath.clamp(pathCount / 3, 100, 4000)
                                         : CSMath.clamp(pathCount / 20, 10, 100);
                // Number of partitions
                int partCount = (int) Math.ceil(pathCount / (float) partSize);
                // Index of the last point being worked on this tick
                int lastIndex = partSize * ((this.ticksExisted % partCount) + 1);
                // Index of the first point being worked on this tick
                int firstIndex = Math.max(0, lastIndex - partSize);

                // Spread to new blocks
                // Only tick paths every 20 ticks or if there is only one or fewer paths (prevents hearths that can't spread causing undue lag)
                if (this.paths.size() > 1 || this.ticksExisted % 20 == 0)
                {   this.tickPaths(firstIndex, lastIndex);
                }
                if (isClient && spreading && paths.size() != pathCount)
                {   HearthDebugRenderer.updatePaths(this);
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
                        AxisAlignedBB playerBB = entity.getBoundingBox();
                        // Ensure height is at least 2 blocks tall
                        playerBB = new AxisAlignedBB(playerBB.minX, playerBB.minY, playerBB.minZ, playerBB.maxX, Math.max(playerBB.maxY, playerBB.minY + 2), playerBB.maxZ);
                        if (CompatManager.isValkyrienSkiesLoaded())
                        {   playerBB = CompatManager.Valkyrien.transformIfShipPos(level, playerBB);
                        }
                        if (this.isAffectingPos(WorldHelper.getOccupiedPositions(playerBB))
                        && !WorldHelper.canSeeSky(level, new BlockPos(playerBB.getCenter()), 64))
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

    protected void tickPaths(int firstIndex, int lastIndex)
    {
        int pathCount = paths.size();
        for (int i = firstIndex; i < Math.min(paths.size(), lastIndex); i++)
        {
            // This operation is really fast because it's an ArrayList
            SpreadPath spreadPath = paths.get(i);
            BlockPos pathPos = spreadPath.pos;
            if (spreadPath.origin == null)
            {   spreadPath.setOrigin(this.getBlockPos());
            }

            int spX = spreadPath.x;
            int spY = spreadPath.y;
            int spZ = spreadPath.z;

            // Don't try to spread if the path is frozen
            if (spreadPath.frozen)
            {
                // Remove a 3D-checkerboard of paths after the Hearth is finished spreading to reduce pointless iteration overhead
                // The Hearth is "finished spreading" when all paths are frozen
                if (!spreading && (Math.abs(spY % 2) == 0) == (Math.abs(spX % 2) == Math.abs(spZ % 2)))
                {   paths.remove(i);
                    // Go back and reiterate over the new path at this index
                    i--;
                }
                // Don't do anything else with this path
                continue;
            }

            /*
             Try to spread to new blocks
             */

            // The origin of the path is usually the hearth's position,
            // but if it's spreading through Create pipes then the origin is the end of the pipe
            if (pathCount < this.getMaxPaths() && spreadPath.withinDistance(spreadPath.origin, this.getSpreadRange())
            && CSMath.withinCubeDistance(spreadPath.origin, this.getBlockPos(), this.getMaxRange()))
            {
                /*
                 Spreading algorithm
                 */
                if (workingChunk == null || !workingChunk.getPos().equals(new ChunkPos(pathPos)))
                {   workingChunk = WorldHelper.getChunk(level, pathPos);
                }
                BlockState state = workingChunk != null ? workingChunk.getBlockState(pathPos) : level.getBlockState(pathPos);

                // Build a map of what positions can see the sky
                Pair<Integer, Integer> flatPos = Pair.of(spX, spZ);
                Pair<Integer, Boolean> seeSkyState = seeSkyMap.get(flatPos);
                boolean canSeeSky;
                if (seeSkyState == null || (seeSkyState.getFirst() < spY != seeSkyState.getSecond()))
                {   seeSkyMap.put(flatPos, Pair.of(spY, canSeeSky = WorldHelper.canSeeSky(level, pathPos.above(), 64)));
                }
                else
                {   canSeeSky = seeSkyState.getSecond();
                }

                if (!canSeeSky || isTransferPipe(state))
                {
                    // Try to spread in every direction from the current position
                    for (int d = 0; d < DIRECTIONS.length; d++)
                    {
                        Direction direction = DIRECTIONS[d];

                        // Don't try to spread backwards
                        Direction pathDir = spreadPath.direction;
                        if (direction == pathDir.getOpposite()) continue;

                        BlockPos tryPos = pathPos.relative(direction);

                        SpreadPath newPath = new SpreadPath(tryPos, direction).setOrigin(spreadPath.origin);

                        // Check if this position hasn't been tried before, and if it's spread-able
                        if (pathLookup.add(tryPos) && this.canSpread(level, pathPos, tryPos, state, spreadPath.direction, direction, newPath))
                        {   // Add the new path to the list
                            this.addPath(newPath);
                        }
                    }
                }
                // Remove this path if it has skylight access
                else
                {   pathLookup.remove(pathPos);
                    paths.remove(i);
                    i--;
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
                Optional<ThermalSourceTempModifier> existingMod = Temperature.getModifier(cap, Temperature.Trait.WORLD, ThermalSourceTempModifier.class);
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

    protected boolean canSpread(World level, BlockPos fromPos, BlockPos toPos, BlockState fromState, Direction fromDirection, Direction toDirection, SpreadPath newPath)
    {
        Block fromBlock = fromState.getBlock();
        if (fromBlock instanceof SmokestackBlock)
        {
            SmokestackBlock.Facing facing = fromState.getValue(SmokestackBlock.FACING);
            boolean isJunction = facing == SmokestackBlock.Facing.BEND;

            BlockState toState = level.getBlockState(toPos);
            boolean isToSmokestack = toState.getBlock() instanceof SmokestackBlock;
            SmokestackBlock.Facing toFacing = isToSmokestack ? toState.getValue(SmokestackBlock.FACING) : null;

            // Spreading from a junction
            if (isJunction)
            {   return isToSmokestack && (toFacing == SmokestackBlock.Facing.BEND || toFacing.getAxis() == toDirection.getAxis());
            }
            // Spreading from a directional smokestack
            else if (facing.getAxis() == toDirection.getAxis())
            {
                newPath.setOrigin(toPos);
                return true;
            }
            return false;
        }
        else if (CompatManager.isCreateLoaded())
        {
            if ((fromBlock instanceof FluidPipeBlock && fromState.getValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(toDirection)))
            || (fromBlock instanceof GlassFluidPipeBlock && fromState.getValue(RotatedPillarBlock.AXIS) == toDirection.getAxis())
            || (fromBlock instanceof EncasedPipeBlock && fromState.getValue(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(toDirection))))
            {
                newPath.setOrigin(toPos);
                return true;
            }
        }
        return !WorldHelper.isSpreadBlocked(level, fromState, fromPos, fromDirection, toDirection);
    }

    protected boolean isTransferPipe(BlockState state)
    {   return state.getBlock() instanceof SmokestackBlock || CompatManager.Create.isFluidPipe(state);
    }

    protected boolean connectsTo(BlockState state, BlockState otherState, Direction direction)
    {
        boolean otherIsSamePipe = state.getBlock() instanceof SmokestackBlock == otherState.getBlock() instanceof SmokestackBlock
                               && CompatManager.Create.isFluidPipe(otherState) == CompatManager.Create.isFluidPipe(state);
        return pipePointingTo(state, otherState, direction) && otherIsSamePipe;
    }

    protected boolean pipePointingTo(BlockState state, BlockState otherState, Direction direction)
    {
        if (state.getBlock() instanceof SmokestackBlock)
        {
            SmokestackBlock.Facing facing = state.getValue(SmokestackBlock.FACING);
            return facing == SmokestackBlock.Facing.BEND
                   ? otherState.getBlock() instanceof SmokestackBlock
                   : facing.getAxis() == direction.getAxis();
        }
        else if (CompatManager.isCreateLoaded())
        {
            if (state.getBlock() instanceof FluidPipeBlock)
            {   return state.getValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(direction));
            }
            else if (state.getBlock() instanceof GlassFluidPipeBlock)
            {   return state.getValue(RotatedPillarBlock.AXIS) == direction.getAxis();
            }
            else if (state.getBlock() instanceof EncasedPipeBlock)
            {   return state.getValue(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(direction));
            }
        }
        return false;
    }

    protected void searchForPipeEnds(BlockPos startPos, Direction fromDir)
    {
        if (this.hasSmokestack && this.level != null)
        {   this.pipeEnds.clear();
            searchForPipeEndsRecursive(startPos, level.getBlockState(startPos), fromDir, new HashSet<>());
        }
    }

    protected void searchForPipeEndsRecursive(BlockPos pos, BlockState state, Direction fromDir, Set<BlockPos> visited)
    {
        visited.add(pos);

        for (int d = 0; d < DIRECTIONS.length; d++)
        {
            Direction direction = DIRECTIONS[d];
            if (direction == fromDir.getOpposite()) continue;

            BlockPos tryPos = pos.relative(direction);
            if (visited.contains(tryPos) || !CSMath.withinCubeDistance(this.getBlockPos(), tryPos, this.getMaxRange())) continue;

            BlockState otherState = level.getBlockState(tryPos);
            if (isTransferPipe(otherState) && connectsTo(state, otherState, direction))
            {   searchForPipeEndsRecursive(tryPos, otherState, direction, visited);
            }
            else if (!WorldHelper.isSpreadBlocked(level, otherState, tryPos, fromDir.getOpposite(), direction)
            && pipePointingTo(state, otherState, direction))
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

    public boolean isAffectingPos(List<BlockPos> positions)
    {
        boolean isSmall = positions.size() <= 1;
        BlockPos.Mutable checkerboardPos = new BlockPos.Mutable();
        for (int i = 0; i < this.paths.size(); i++)
        {
            SpreadPath path = this.paths.get(i);
            for (int j = 0; j < positions.size(); j++)
            {
                BlockPos pos = positions.get(j);
                if (pos.equals(path.pos))
                {   return true;
                }
                if (isSmall && pos.equals(checkerboardPos.set(path.pos).offset(1, 1, 1)))
                {   return true;
                }
            }
        }
        return false;
    }

    void resetPaths()
    {   // Reset cooldown
        this.rebuildCooldown = 100;

        // Clear paths & lookup
        this.paths.clear();
        this.pathLookup.clear();
        if (this.forceRebuild)
        {   seeSkyMap.clear();
        }
        else for (int i = 0; i < this.queuedUpdates.size(); i++)
        {
            BlockPos pos = this.queuedUpdates.get(i);
            seeSkyMap.remove(Pair.of(pos.getX(), pos.getZ()));
        }

        // Un-freeze paths so areas can be re-checked
        this.frozenPaths = 0;
        this.spreading = true;

        // Tell client to reset paths too
        this.sendResetPacket();
        if (this.level.isClientSide)
        {   HearthDebugRenderer.updatePaths(this);
        }

        this.forceRebuild = false;
        this.queuedUpdates.clear();
        this.searchForPipeEnds(this.getBlockPos().above(), Direction.UP);
    }

    public List<EffectInstance> getEffects()
    {   return effects;
    }

    public int getItemFuel(ItemStack item)
    {   return CSMath.getIfNotNull(ConfigHelper.getFirstOrNull(ConfigSettings.HEARTH_FUEL, item.getItem(), data -> data.test(item)), FuelData::fuel, 0d).intValue();
    }

    public int getHotFuel()
    {   return this.hotFuelHandler.getFuelAmount();
    }

    public int getColdFuel()
    {   return this.coldFuelHandler.getFuelAmount();
    }

    public boolean isUsingColdFuel()
    {   return this.usingColdFuel;
    }

    public boolean isUsingHotFuel()
    {   return this.usingHotFuel;
    }

    protected FuelFluidHandler getFuelHandler(FuelType fuelType)
    {   return fuelType == FuelType.COLD ? this.coldFuelHandler : this.hotFuelHandler;
    }

    protected void addFuel(FuelType fuelType, int amount, boolean update)
    {
        FuelFluidHandler handler = this.getFuelHandler(fuelType);
        FluidStack fillStack = new FluidStack(handler.getFluidOrDefault(), amount);
        handler.fill(fillStack, IFluidHandler.FluidAction.EXECUTE, update);
    }

    protected void drainFuel(FuelType fuelType, int amount, boolean update)
    {
        FuelFluidHandler handler = this.getFuelHandler(fuelType);
        handler.drain(amount, IFluidHandler.FluidAction.EXECUTE, update);
    }

    protected void setFuel(FuelType fuelType, int amount, boolean update)
    {
        FuelFluidHandler handler = this.getFuelHandler(fuelType);
        FluidStack fluidStack = handler.getFluidStack();
        int oldAmount = fluidStack.getAmount();
        // Set fluid amount
        if (fluidStack.isEmpty())
        {   fluidStack = new FluidStack(handler.getFluidOrDefault(), amount);
            handler.setFluidStack(fluidStack);
        }
        else fluidStack.setAmount(amount);
        // Update
        if (oldAmount != amount && update)
        {   this.onFuelChanged(fuelType);
        }
    }

    protected void onFuelChanged(FuelType fuelType)
    {
        boolean nowEmpty = this.getFuelHandler(fuelType).getFluidStack().isEmpty();
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

        BlockState aboveState = level.getBlockState(this.getBlockPos().above());
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
            this.forceUpdate();
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
                if (rand.nextDouble() < this.getColdFuel() / 3000d)
                {   double d0 = pos.getX() + 0.5 + face.getStepX() * 0.35;
                    double d1 = pos.getY() + 0.5 + face.getStepY() * 0.35;
                    double d2 = pos.getZ() + 0.5 + face.getStepZ() * 0.35;
                    double d3 = (rand.nextDouble() - 0.5) / 4;
                    double d4 = (rand.nextDouble() - 0.5) / 4;
                    double d5 = (rand.nextDouble() - 0.5) / 4;
                    level.addParticle(ParticleTypesInit.STEAM.get(), d0 + d3, d1 + d4, d2 + d5, 0.0D, 0.04D, 0.0D);
                }
            }
            if (this.usingHotFuel)
            {
                if (rand.nextDouble() < this.getHotFuel() / 3000d)
                {   double d0 = pos.getX() + 0.5 + face.getStepX() * 0.35;
                    double d1 = pos.getY() + 0.5 + face.getStepY() * 0.35;
                    double d2 = pos.getZ() + 0.5 + face.getStepZ() * 0.35;
                    double d3 = (rand.nextDouble() - 0.5) / 2;
                    double d4 = (rand.nextDouble() - 0.5) / 2;
                    double d5 = (rand.nextDouble() - 0.5) / 2;
                    BasicParticleType particle = rand.nextDouble() < 0.5
                                                  ? ParticleTypes.LARGE_SMOKE
                                                  : ParticleTypes.SMOKE;
                    level.addParticle(particle, d0 + d3, d1 + d4, d2 + d5, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    public BasicParticleType getAirParticle()
    {   return ParticleTypesInit.HEARTH_AIR.get();
    }

    public void spawnAirParticle(int x, int y, int z, Random rand)
    {
        ParticleStatus status = Minecraft.getInstance().options.particles;
        if (status != ParticleStatus.ALL)
        {   return;
        }
        if (rand.nextFloat() > (spreading ? 0.016f : 0.032f)) return;

        float xr = rand.nextFloat();
        float yr = rand.nextFloat();
        float zr = rand.nextFloat();
        float xm = rand.nextFloat() / 20 - 0.025f;
        float zm = rand.nextFloat() / 20 - 0.025f;

        level.addParticle(this.getAirParticle(), false, x + xr, y + yr, z + zr, xm, 0, zm);
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
        this.getFuelHandler(FuelType.COLD).setFluidStack(FluidStack.loadFluidStackFromNBT(tag.getCompound("ColdFuel")));
        this.getFuelHandler(FuelType.HOT).setFluidStack(FluidStack.loadFluidStackFromNBT(tag.getCompound("HotFuel")));
        this.insulationLevel = tag.getInt("InsulationLevel");
    }

    @Override
    public CompoundNBT save(CompoundNBT tag)
    {   super.save(tag);
        ItemStackHelper.saveAllItems(tag, this.items);
        saveEffects(tag);
        tag.put("ColdFuel", this.getFuelHandler(FuelType.COLD).getFluidStack().writeToNBT(new CompoundNBT()));
        tag.put("HotFuel", this.getFuelHandler(FuelType.HOT).getFluidStack().writeToNBT(new CompoundNBT()));
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
               ? this.isHeatingSide(face)
                       ? hotFuelHolder.cast()
               : this.isCoolingSide(face)
                       ? coldFuelHolder.cast()
               : super.getCapability(capability, face)
             : super.getCapability(capability, face);
    }

    public void addPath(SpreadPath path)
    {   paths.add(path);
    }

    public void addPaths(Collection<SpreadPath> newPaths)
    {   paths.addAll(newPaths);
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

    public void forceUpdate()
    {   this.forceRebuild = true;
        this.sendBlockUpdate(this.getBlockPos());
    }

    protected void cleanup()
    {
        hotFuelHolder.invalidate();
        coldFuelHolder.invalidate();
        HearthSaveDataHandler.HEARTH_POSITIONS.remove(Pair.of(this.getBlockPos(), this.getLevel().dimension().location()));
        MinecraftForge.EVENT_BUS.unregister(this);
        if (this.level.isClientSide)
        {   ClientOnlyHelper.removeHearthPosition(this.getBlockPos());
        }
    }

    public Set<BlockPos> getPathLookup()
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
    }

    public class FuelFluidHandler implements IFluidHandler
    {
        private FluidStack fuel;
        private final FuelType fuelType;

        protected FuelFluidHandler(FuelType fuelType)
        {
            this.fuelType = fuelType;
            this.fuel = new FluidStack(this.fuelType.getFluid(), 0);
        }

        @Override
        public int getTanks()
        {   return 1;
        }

        public FuelType getFuelType()
        {   return fuelType;
        }

        public int getFuelAmount()
        {   return fuel.getAmount();
        }

        public FluidStack getFluidStack()
        {   return fuel;
        }
        public void setFluidStack(FluidStack fluidStack)
        {   this.fuel = fluidStack;
        }

        public Fluid getFluid()
        {   return this.fuel.getFluid();
        }
        public Fluid getFluidOrDefault()
        {
            Fluid fluid = this.getFluid();
            if (fluid == Fluids.EMPTY)
            {   return this.getDefaultFluid();
            }
            return fluid;
        }
        public Fluid getDefaultFluid()
        {   return this.fuelType.getFluid();
        }

        @Override
        @NotNull
        public FluidStack getFluidInTank(int tank)
        {   return fuel;
        }

        @Override
        public int getTankCapacity(int tank)
        {   return HearthBlockEntity.this.getMaxFuel();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack fluidStack)
        {   return fluidStack.getFluid().is(this.fuelType.getValidFluidTag());
        }

        public int fill(FluidStack fluidStack, FluidAction fluidAction, boolean update)
        {
            if (fluidStack.getFluid().is(this.fuelType.getValidFluidTag()))
            {
                int amount = Math.min(fluidStack.getAmount(), this.getTankCapacity(0) - this.fuel.getAmount());
                if (fluidAction.execute())
                {
                    if (this.fuel.isEmpty())
                    {   this.fuel = fluidStack.copy();
                    }
                    else this.fuel.grow(amount);
                }
                if (amount > 0 && update)
                {   HearthBlockEntity.this.onFuelChanged(this.fuelType);
                }
                return amount;
            }
            return 0;
        }

        @Override
        public int fill(FluidStack fluidStack, FluidAction fluidAction)
        {   return this.fill(fluidStack, fluidAction, true);
        }

        public FluidStack drain(int amount, FluidAction fluidAction, boolean update)
        {
            int drained = Math.min(this.fuel.getAmount(), amount);
            if (drained == 0)
            {   return new FluidStack(this.fuelType.getFluid(), 0);
            }

            FluidStack stack = new FluidStack(this.fuel, drained);
            if (fluidAction.execute() && drained > 0)
            {   this.fuel.shrink(drained);
            }

            if (drained > 0 && update)
            {   HearthBlockEntity.this.onFuelChanged(this.fuelType);
            }
            return stack;
        }

        @Override
        public FluidStack drain(int amount, FluidAction fluidAction)
        {   return this.drain(amount, fluidAction, true);
        }

        @Override
        @NotNull
        public FluidStack drain(FluidStack fluidStack, FluidAction fluidAction)
        {
            return fluidStack.getFluid() == this.fuel.getFluid()
                 ? this.drain(fluidStack.getAmount(), fluidAction, true)
                 : FluidStack.EMPTY;
        }
    }
}