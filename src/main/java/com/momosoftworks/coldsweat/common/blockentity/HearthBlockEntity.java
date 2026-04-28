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
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.BlockEntityInit;
import com.momosoftworks.coldsweat.core.init.ParticleTypesInit;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.HearthResetMessage;
import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.data.tag.ModFluidTags;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.util.entity.DummyPlayer;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import com.momosoftworks.coldsweat.util.registries.ModFluids;
import com.momosoftworks.coldsweat.util.registries.ModSounds;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.world.SpreadPath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
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
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber
public class HearthBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer
{
    // List of SpreadPaths, which determine where the Hearth is affecting and how it spreads through/around blocks
    List<SpreadPath> paths = new ArrayList<>(this.getMaxPaths());
    // Used as a lookup table for detecting duplicate paths (faster than ArrayList#contains())
    Set<BlockPos> pathLookup = new HashSet<>(this.getMaxPaths());
    Map<Pair<Integer, Integer>, Pair<Integer, Boolean>> seeSkyMap = new HashMap<>(this.getMaxPaths());

    List<MobEffectInstance> effects = new ArrayList<>();

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
        {   TICK_DOWN_EFFECT = ObfuscationReflectionHelper.findMethod(MobEffectInstance.class, "m_19579_");
            TICK_DOWN_EFFECT.setAccessible(true);
        }
        catch (Exception ignored) {}
    }

    public HearthBlockEntity(BlockEntityType type, BlockPos pos, BlockState state)
    {   super(type, pos, state);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public HearthBlockEntity(BlockPos pos, BlockState state)
    {   this(BlockEntityInit.HEARTH_BLOCK_ENTITY_TYPE.get(), pos, state);
    }

    @SubscribeEvent
    public void onBlockUpdate(BlockStateChangedEvent event)
    {
        BlockPos pos = event.getPosition();
        Level level = event.getLevel();
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
    protected Component getDefaultName() {
        return new TranslatableComponent("container." + ColdSweat.MOD_ID + ".hearth");
    }

    @Override
    public Component getDisplayName()
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

    public static <T extends BlockEntity> void tickSelf(Level level, BlockPos pos, BlockState state, T te)
    {
        if (te instanceof HearthBlockEntity hearth)
        {   hearth.tick(level, pos);
        }
    }

    public void tick(Level level, BlockPos pos)
    {
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
            AABB searchArea = new AABB(WorldHelper.shipyardToWorld(level, pos)).inflate(this.getMaxRange());

            for (Entity entity : this.level.getEntities((Entity) null, searchArea, EntityTempManager::isTemperatureEnabled))
            {
                if (entity instanceof LivingEntity living)
                {   this.entities.add(living);
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
                        AABB playerBB = entity.getBoundingBox();
                        // Ensure height is at least 2 blocks tall
                        playerBB = playerBB.setMaxY(Math.max(playerBB.maxY, playerBB.minY + 2));
                        if (this.isAffectingPos(WorldHelper.getPositionsInAABB(WorldHelper.worldToShipyard(level, playerBB)))
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

    ChunkAccess workingChunk = null;

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
        if (this.level instanceof ServerLevel serverLevel && (wasHeatingOn != this.isHeatingOn || wasCoolingOn != this.isCoolingOn))
        {
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
            List<MobEffectInstance> itemEffects = PotionUtils.getMobEffects(fuelStack);
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

                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1, 1);
                effects.clear();
                // Convert to NBT and back again to create new instances of the effects (otherwise we would be ticking down the global instances)
                effects.addAll(itemEffects.stream().map(eff -> eff.save(new CompoundTag())).map(MobEffectInstance::load).toList());
                WorldHelper.syncBlockEntityData(this);
            }
            else if (fuelStack.is(Items.MILK_BUCKET) && !effects.isEmpty())
            {
                this.getItems().set(0, fuelStack.getContainerItem());
                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1, 1);
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
            MobEffectInstance effect = this.effects.get(i);
            entity.addEffect(new MobEffectInstance(effect.getEffect(),
                                                   effect.getEffect() == MobEffects.NIGHT_VISION ? 399 : 119,
                                                   effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
        }

        if (!this.isSmartEnabled() || this.shouldInsulateEntity(entity))
        {
            int maxEffect = this.getMaxInsulationLevel() - 1;
            int effectLevel = (int) Math.min(maxEffect, (insulationLevel / (double) this.getInsulationTime()) * maxEffect);
            if (usingColdFuel)
            {   entity.addEffect(new MobEffectInstance(ModEffects.FRIGIDNESS, 60, effectLevel, false, false, true));
            }
            if (usingHotFuel)
            {   entity.addEffect(new MobEffectInstance(ModEffects.WARMTH, 60, effectLevel, false, false, true));
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

    protected boolean canSpread(Level level, BlockPos fromPos, BlockPos toPos, BlockState fromState, Direction fromDirection, Direction toDirection, SpreadPath newPath)
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
            if ((fromBlock instanceof FluidPipeBlock && fromState.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(toDirection)))
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
            {   return state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction));
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
        if (positions.isEmpty()) return false;
        for (int i = 0; i < this.paths.size(); i++)
        {
            SpreadPath path = this.paths.get(i);
            for (int j = 0; j < positions.size(); j++)
            {
                BlockPos pos = positions.get(j);
                if (pos.equals(path.pos))
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

    public List<MobEffectInstance> getEffects()
    {   return effects;
    }

    public int getItemFuel(ItemStack item)
    {   return CSMath.getIfNotNull(ConfigHelper.getFirstOrNull(ConfigSettings.HEARTH_FUEL, item.getItem(), data -> data.test(item)), FuelData::fuel, 0d).intValue();
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
        {   this.level.playSound(null, this.getBlockPos(), this.getFuelDepleteSound(), SoundSource.BLOCKS, 1, (float) Math.random() * 0.2f + 0.9f);
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
                    SimpleParticleType particle = ParticleTypesInit.SMOKESTACK_WARM.get();
                    level.addParticle(particle, d0 + d3, d1 + d4, d2 + d5, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @Nullable
    public ParticleOptions getAirParticle()
    {
        List<ParticleOptions> options = new ArrayList<>();
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

        ParticleOptions particle = this.getAirParticle();
        if (particle == null) return;
        level.addParticle(particle, false, x + xr, y + yr, z + zr, xm, 0, zm);
    }

    @Override
    public int getContainerSize()
    {   return 1;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInv)
    {   return new HearthContainer(id, playerInv, this);
    }

    @Override
    public void load(CompoundTag tag)
    {   super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
        this.loadEffects(tag);
        if (!this.loadFuelOld(tag)) // Legacy handler for old FluidStack fuel storage
        {   this.coldFuel.set(tag.getInt("ColdFuel"));
            this.hotFuel.set(tag.getInt("HotFuel"));
        }
        this.insulationLevel = tag.getInt("InsulationLevel");
    }

    private boolean loadFuelOld(CompoundTag tag)
    {
        if (tag.get("ColdFuel") instanceof CompoundTag || tag.get("HotFuel") instanceof CompoundTag)
        {   this.coldFuel.set(FluidStack.loadFluidStackFromNBT(tag.getCompound("ColdFuel")).getAmount());
            this.hotFuel.set(FluidStack.loadFluidStackFromNBT(tag.getCompound("HotFuel")).getAmount());
            return true;
        }
        return false;
    }

    @Override
    public void saveAdditional(CompoundTag tag)
    {   super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
        saveEffects(tag);
        tag.putInt("ColdFuel", this.getColdFuel());
        tag.putInt("HotFuel", this.getHotFuel());
        tag.putInt("InsulationLevel", this.insulationLevel);
    }

    void saveEffects(CompoundTag tag)
    {
        if (!this.effects.isEmpty())
        {   ListTag list = new ListTag();
            for (MobEffectInstance effect : this.effects)
            {   list.add(effect.save(new CompoundTag()));
            }
            tag.put("Effects", list);
        }
    }

    void loadEffects(CompoundTag tag)
    {   this.effects.clear();
        if (tag.contains("Effects"))
        {   ListTag list = tag.getList("Effects", 10);
            for (int i = 0; i < list.size(); i++)
            {   this.effects.add(MobEffectInstance.load(list.getCompound(i)));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag tag = super.getUpdateTag();
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
    public void handleUpdateTag(CompoundTag tag)
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {   handleUpdateTag(pkt.getTag());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {   return ClientboundBlockEntityDataPacket.create(this);
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
    {   paths.add(path);
    }

    public void addPaths(Collection<SpreadPath> newPaths)
    {   paths.addAll(newPaths);
    }

    public void sendResetPacket()
    {   if (level instanceof ServerLevel)
        {   ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() ->
                                 (LevelChunk) WorldHelper.getChunk(level, this.getBlockPos())), new HearthResetMessage(this.getBlockPos()));
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

        public TagKey<Fluid> getValidFluidTag()
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
        public boolean isFluidValid(int tank, @NotNull FluidStack fluidStack)
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
}