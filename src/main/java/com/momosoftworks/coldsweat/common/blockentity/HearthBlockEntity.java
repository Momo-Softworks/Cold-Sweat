package com.momosoftworks.coldsweat.common.blockentity;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.common.BlockStateChangedEvent;
import com.momosoftworks.coldsweat.api.temperature.modifier.BlockInsulationTempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.block.HearthBottomBlock;
import com.momosoftworks.coldsweat.common.block.SmokestackBlock;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.common.container.HearthContainer;
import com.momosoftworks.coldsweat.common.event.HearthSaveDataHandler;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.*;
import com.momosoftworks.coldsweat.core.network.ModPacketHandlers;
import com.momosoftworks.coldsweat.core.network.message.HearthResetMessage;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.SpreadPath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.StreamSupport;

public class HearthBlockEntity extends RandomizableContainerBlockEntity
{
    // List of SpreadPaths, which determine where the Hearth is affecting and how it spreads through/around blocks
    List<SpreadPath> paths = new ArrayList<>();
    // Used as a lookup table for detecting duplicate paths (faster than ArrayList#contains())
    Set<BlockPos> pathLookup = new HashSet<>();
    Map<Pair<Integer, Integer>, Pair<Integer, Boolean>> seeSkyMap = new HashMap<>();

    List<MobEffectInstance> effects = new ArrayList<>();

    FluidStack coldFuel = new FluidStack(Fluids.WATER, 0);
    FluidStack hotFuel = new FluidStack(Fluids.LAVA, 0);


    public static final BlockCapability<IFluidHandler, @Nullable Direction> FLUID_HANDLER =
            BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "fuel_handler"), IFluidHandler.class);

    NonNullList<ItemStack> items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    Pair<BlockPos, ResourceLocation> levelPos = Pair.of(null, null);
    int x = 0;
    int y = 0;
    int z = 0;

    int lastHotFuel = 0;
    int lastColdFuel = 0;
    boolean shouldUseHotFuel = false;
    boolean shouldUseColdFuel = false;
    boolean hasHotFuel = false;
    boolean hasColdFuel = false;
    int insulationLevel = 0;

    boolean isPlayerNearby = false;
    List<Player> players = new ArrayList<>();
    int rebuildCooldown = 0;
    boolean forceRebuild = false;
    boolean isRebuildQueued = false;
    public int ticksExisted = 0;

    boolean registeredLocation = false;

    boolean showParticles = true;
    int frozenPaths = 0;
    boolean spreading = true;

    boolean hasSmokestack = false;
    int smokestackHeight = 2;

    static final Direction[] DIRECTIONS = Direction.values();

    static Method TICK_DOWN_EFFECT;
    static
    {
        try
        {   TICK_DOWN_EFFECT = ObfuscationReflectionHelper.findMethod(MobEffectInstance.class, "tickDownDuration");
            TICK_DOWN_EFFECT.setAccessible(true);
        }
        catch (Exception ignored) {}
    }

    public HearthBlockEntity(BlockEntityType type, BlockPos pos, BlockState state)
    {   super(type, pos, state);
        NeoForge.EVENT_BUS.addListener(this::onBlockUpdate);
    }

    public HearthBlockEntity(BlockPos pos, BlockState state)
    {   this(ModBlockEntities.HEARTH.get(), pos, state);
    }

    @SubscribeEvent
    public void onBlockUpdate(BlockStateChangedEvent event)
    {
        BlockPos pos = event.getPosition();
        Level level = event.getLevel();
        if (level == this.level
        && CSMath.withinCubeDistance(pos, this.getBlockPos(), this.getMaxRange())
        && !event.getOldState().getCollisionShape(level, pos).equals(event.getNewState().getCollisionShape(level, pos)))
        {   this.sendBlockUpdate();
        }
    }

    /**
     * Range of the Hearth starting from an exit point
     */
    public int getSpreadRange()
    {   return 20;
    }

    /**
     * Range of the Hearth starting from the Hearth's position
     */
    public int getMaxRange()
    {   return 96;
    }

    public int getMaxPaths()
    {   return 9000;
    }

    public int getMaxFuel()
    {   return 1000;
    }

    public int getInsulationTime()
    {   return 1200;
    }

    public int getInsulationLevel()
    {   return insulationLevel;
    }

    public int getMaxInsulationLevel()
    {   return 10;
    }

    public boolean hasSmokeStack()
    {   return true;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container." + ColdSweat.MOD_ID + ".hearth");
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
        // Register the hearth's position to the global map
        this.registerLocation();

        // Easy access to clientside testList::stream
        boolean isClient = level.isClientSide;

        this.ticksExisted++;

        if (rebuildCooldown > 0) rebuildCooldown--;

        // Locate nearby players
        if (this.level != null && this.ticksExisted % 20 == 0)
        {
            this.isPlayerNearby = false;
            players.clear();
            for (Player player : this.level.players())
            {
                if (player.blockPosition().closerThan(pos, this.getMaxRange()))
                {   players.add(player);
                    this.isPlayerNearby = true;
                }
            }
        }

        // Tick down the time for each effect
        this.tickPotionEffects();

        // Clear paths every 5 minutes to account for calculation errors
        if (this.ticksExisted % 6000 == 0)
        {   this.replacePaths(new ArrayList<>(Collections.singletonList(new SpreadPath(pos).setOrigin(pos))));
        }

        // Reset if a nearby block has been updated
        if (forceRebuild || (rebuildCooldown <= 0 && isRebuildQueued))
        {   this.resetPaths();
        }

        if (this.getColdFuel() > 0 || this.getHotFuel() > 0)
        {
            // Gradually increases insulation amount
            if (insulationLevel < this.getInsulationTime())
            {   insulationLevel++;
            }

            if (this.isPlayerNearby)
            {
                // Determine whether particles are enabled
                if (this.ticksExisted % 20 == 0)
                {
                    showParticles = isClient
                            && Minecraft.getInstance().options.particles().get() == ParticleStatus.ALL
                            && !HearthSaveDataHandler.DISABLED_HEARTHS.contains(levelPos);
                }

                if (paths.isEmpty())
                {   this.addPath(new SpreadPath(pos.above(2)).setOrigin(pos.above(2)));
                    pathLookup.add(pos.above());
                }

                // Mark as not spreading if all paths are frozen
                if (this.frozenPaths >= paths.size())
                {   this.spreading = false;
                }

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
                this.trySpreading(pathCount, firstIndex, lastIndex);

                // Give insulation to players
                if (!isClient && this.ticksExisted % 20 == 0)
                {
                    for (int i = 0; i < players.size(); i++)
                    {   Player player = players.get(i);
                        if (player != null && pathLookup.contains(player.blockPosition()))
                        {   this.insulatePlayer(player);
                        }
                    }
                    players.clear();
                }

                // Drain fuel
                this.tickDrainFuel();
            }
        }
        
        // Input fuel
        if (this.ticksExisted % 20 == 0)
        {   this.checkForFuel();
        }

        // Update fuel
        if (this.isFuelChanged())
        {   this.updateFuelState();
        }

        // Particles
        if (isClient)
        {   this.tickParticles();
        }
    }

    protected void trySpreading(int pathCount, int firstIndex, int lastIndex)
    {
        for (int i = firstIndex; i < Math.min(paths.size(), lastIndex); i++)
        {   // This operation is really fast because it's an ArrayList
            SpreadPath spreadPath = paths.get(i);
            BlockPos pathPos = spreadPath.pos;
            if (spreadPath.origin == null)
            {   spreadPath.setOrigin(this.getBlockPos());
            }

            int spX = spreadPath.x;
            int spY = spreadPath.y;
            int spZ = spreadPath.z;

            // Use try-finally because there's still stuff to do even if {continue} skips the rest of the loop
            try
            {   // Don't try to spread if the path is frozen
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
                    BlockState state = level.getBlockState(pathPos);

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

                    if (!canSeeSky || isPipe(state))
                    {
                        // Try to spread in every direction from the current position
                        for (int d = 0; d < DIRECTIONS.length; d++)
                        {
                            Direction direction = DIRECTIONS[d];

                            // Don't try to spread backwards
                            Direction pathDir = spreadPath.direction;
                            if (direction.getAxis() == pathDir.getAxis() && direction != pathDir) continue;

                            BlockPos tryPos = pathPos.relative(direction);

                            // Avoid duplicate paths (ArrayList isn't duplicate-safe like Sets/Maps)
                            // .add() functions to both add the path and check if it's already in the list
                            if (pathLookup.add(tryPos))
                            {
                                SpreadPath newPath = new SpreadPath(tryPos, direction).setOrigin(spreadPath.origin);

                                // If the BlockState is a pipe, check if the new path is following the direction of the pipe
                                if (!WorldHelper.isSpreadBlocked(level, state, pathPos, direction, spreadPath.direction)
                                && this.isValidPipeAt(tryPos, state, newPath, direction))
                                {   // Add the new path to the list
                                    paths.add(newPath);
                                }
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

            /*
             Give insulation & spawn particles
             */
            finally
            {
                // Air Particles
                if (this.getLevel().isClientSide && showParticles)
                {   Random rand = new Random();
                    if (!(Minecraft.getInstance().getDebugOverlay().showDebugScreen() && ConfigSettings.HEARTH_DEBUG.get()))
                    {   this.spawnAirParticle(spX, spY, spZ, rand);
                    }
                }
            }
        }
    }

    public void checkForFuel()
    {
        BlockPos pos = this.getBlockPos();
        ItemStack fuelStack = this.getItems().get(0);
        if (!fuelStack.isEmpty())
        {   // Potion items
            List<MobEffectInstance> itemEffects = CSMath.getIfNotNull(fuelStack.get(DataComponents.POTION_CONTENTS),
                                                                      potions -> StreamSupport.stream(potions.getAllEffects().spliterator(), true).toList(),
                                                                      new ArrayList<>());
            if (ConfigSettings.HEARTH_POTIONS_ENABLED.get()
            && !itemEffects.isEmpty() && !itemEffects.equals(effects)
            && itemEffects.stream().noneMatch(eff -> ConfigSettings.HEARTH_POTION_BLACKLIST.get().contains(eff.getEffect())))
            {
                if (fuelStack.getItem() instanceof PotionItem)
                {   this.getItems().set(0, Items.GLASS_BOTTLE.getDefaultInstance());
                }
                else if (!fuelStack.hasCraftingRemainingItem() || fuelStack.getCount() > 1)
                {   fuelStack.shrink(1);
                }
                else
                {   this.getItems().set(0, fuelStack.getCraftingRemainingItem());
                }

                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1, 1);
                effects.clear();
                // Convert to NBT and back again to create new instances of the effects (otherwise we would be ticking down the global instances)
                effects.addAll(itemEffects.stream().map(MobEffectInstance::save).map(nbt -> MobEffectInstance.load(((CompoundTag) nbt))).toList());
                WorldHelper.syncBlockEntityData(this);
            }
            else if (fuelStack.is(Items.MILK_BUCKET) && !effects.isEmpty())
            {
                this.getItems().set(0, fuelStack.getCraftingRemainingItem());
                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1, 1);
                effects.clear();
                WorldHelper.syncBlockEntityData(this);
            }
            // Normal fuel items
            else
            {
                int itemFuel = getItemFuel(fuelStack);
                if (itemFuel != 0)
                {
                    this.storeFuel(fuelStack, itemFuel);
                }
            }
        }
    }

    protected boolean isFuelChanged()
    {
        return Math.abs(this.getColdFuel() - lastColdFuel) >= this.getMaxFuel()/36 || Math.abs(this.getHotFuel() - lastHotFuel) >= this.getMaxFuel()/36
            || this.ticksExisted % 5 == 0;
    }

    protected void storeFuel(ItemStack stack, int amount)
    {
        int fuel = amount > 0 ? this.getHotFuel() : this.getColdFuel();
        if (fuel < this.getMaxFuel() - Math.abs(amount) * 0.75)
        {
            if (!stack.hasCraftingRemainingItem() || stack.getCount() > 1)
            {   int consumeCount = Math.min((int) Math.floor((this.getMaxFuel() - fuel) / (double) Math.abs(amount)), stack.getCount());
                stack.shrink(consumeCount);
                addFuel(amount * consumeCount);
            }
            else
            {   this.setItem(0, stack.getCraftingRemainingItem());
                addFuel(amount);
            }
        }
    }

    protected void drainFuel()
    {
        if (shouldUseColdFuel)
        {   this.setColdFuel(this.getColdFuel() - 1, true);
        }
        if (shouldUseHotFuel)
        {   this.setHotFuel(this.getHotFuel() - 1, true);
        }

        shouldUseColdFuel = false;
        shouldUseHotFuel = false;
    }

    protected void tickDrainFuel()
    {
        if (this.ticksExisted % 40 == 0)
        {   this.drainFuel();
        }
    }

    void insulatePlayer(Player player)
    {
        for (int i = 0; i < effects.size(); i++)
        {   MobEffectInstance effect = effects.get(i);
            player.addEffect(new MobEffectInstance(effect.getEffect(), effect.getEffect() == MobEffects.NIGHT_VISION ? 399 : 119,
                                                   effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
        }
        // Apply the insulation effect
        if (!shouldUseColdFuel || !shouldUseHotFuel)
        {
            ITemperatureCap cap = EntityTempManager.getTemperatureCap(player);
            double temp = cap.getTrait(Temperature.Trait.WORLD);
            double min = cap.getTrait(Temperature.Trait.FREEZING_POINT);
            double max = cap.getTrait(Temperature.Trait.BURNING_POINT);

            // If the player is habitable, check the input temperature reported by their HearthTempModifier (if they have one)
            if (CSMath.betweenInclusive(temp, min, max))
            {
                // Find the player's HearthTempModifier
                TempModifier modifier = null;
                for (TempModifier tempModifier : cap.getModifiers(Temperature.Trait.WORLD))
                {   if (tempModifier instanceof BlockInsulationTempModifier)
                    {   modifier = tempModifier;
                        break;
                    }
                }
                // If they have one, refresh it
                if (modifier != null)
                {   if (modifier.getExpireTime() - modifier.getTicksExisted() > 20)
                    {   return;
                    }
                    temp = modifier.getLastInput();
                }
                // This means the player is not insulated, and they are habitable without it
                else return;
            }

            // Tell the hearth to use hot fuel
            shouldUseHotFuel |= this.getHotFuel() > 0 && temp < min;
            // Tell the hearth to use cold fuel
            shouldUseColdFuel |= this.getColdFuel() > 0 && temp > max;

        }
        if (shouldUseHotFuel || shouldUseColdFuel)
        {   int maxEffect = this.getMaxInsulationLevel() - 1;
            int effectLevel = (int) Math.min(maxEffect, (insulationLevel / (double) this.getInsulationTime()) * maxEffect);
            player.addEffect(new MobEffectInstance(ModEffects.INSULATED, 120, effectLevel, false, false, true));
        }
    }

    protected boolean isValidPipeAt(BlockPos newPos, BlockState fromState, SpreadPath newPath, Direction direction)
    {
        if (!isPipe(fromState)) return true;
        if (CompatManager.isCreateLoaded())
        {
            // TODO: Create compatibility
            //Block block = fromState.getBlock();
            //if ((block instanceof FluidPipeBlock && fromState.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction)))
            //|| (block instanceof GlassFluidPipeBlock && fromState.getValue(RotatedPillarBlock.AXIS) == direction.getAxis())
            //|| (block instanceof EncasedPipeBlock && fromState.getValue(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(direction))))
            //{   newPath.setOrigin(newPos);
            //    return true;
            //}
            //return false;
        }
        return true;
    }

    protected boolean isPipe(BlockState state)
    {
        // TODO: Create compatibility
        //return CompatManager.isCreateLoaded() && (state.getBlock() instanceof FluidPipeBlock
        //                                       || state.getBlock() instanceof GlassFluidPipeBlock
        //                                       || state.getBlock() instanceof EncasedPipeBlock);
        return false;
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

    void resetPaths()
    {   // Reset cooldown
        this.rebuildCooldown = 100;

        // Clear paths & lookup
        paths.clear();
        pathLookup.clear();
        seeSkyMap.clear();

        // Un-freeze paths so areas can be re-checked
        frozenPaths = 0;
        spreading = true;

        // Tell client to reset paths too
        this.sendResetPacket();

        forceRebuild = false;
        this.isRebuildQueued = false;
    }

    public List<MobEffectInstance> getEffects()
    {   return effects;
    }

    public int getItemFuel(ItemStack item)
    {   return CSMath.getIfNotNull(ConfigSettings.HEARTH_FUEL.get().get(item.getItem()),
                                   fuel -> fuel.test(item) ? fuel.value() : 0,
                                   0).intValue();
    }

    public int getHotFuel()
    {   return this.hotFuel.getAmount();
    }

    public int getColdFuel()
    {   return this.coldFuel.getAmount();
    }

    public void setHotFuel(int amount, boolean update)
    {
        if (this.hotFuel.isEmpty())
        {   this.hotFuel = new FluidStack(Fluids.LAVA, amount);
        }
        else this.hotFuel.setAmount(amount);

        if (amount == 0 && hasHotFuel)
        {   hasHotFuel = false;
            if (level != null)
            {   level.playSound(null, this.getBlockPos(), this.getFuelDepleteSound(), SoundSource.BLOCKS, 1, (float) Math.random() * 0.2f + 0.9f);
            }
        }
        else hasHotFuel = true;

        if (update) this.updateFuelState();
    }

    public void setHotFuelAndUpdate(int amount)
    {   this.setHotFuel(amount, true);
    }

    public void setColdFuel(int amount, boolean update)
    {
        if (this.coldFuel.isEmpty())
        {   this.coldFuel = new FluidStack(Fluids.WATER, amount);
        }
        else this.coldFuel.setAmount(amount);

        if (amount <= 0 && hasColdFuel)
        {   hasColdFuel = false;
            if (level != null)
            {   level.playSound(null, this.getBlockPos(), this.getFuelDepleteSound(), SoundSource.BLOCKS, 1, (float) Math.random() * 0.2f + 0.9f);
            }
        }
        else hasColdFuel = true;

        if (update) this.updateFuelState();
    }

    public void setColdFuelAndUpdate(int amount)
    {   this.setColdFuel(amount, true);
    }

    /**
     * Negative numbers are cold, positive numbers are hot
     */
    public void addFuel(int amount)
    {   if (amount > 0)
        {   this.setHotFuelAndUpdate(this.getHotFuel() + amount);
        }
        else if (amount < 0)
        {   this.setColdFuelAndUpdate(this.getColdFuel() + Math.abs(amount));
        }
    }

    public void updateFuelState()
    {
        if (level != null && !level.isClientSide)
        {   this.setChanged();
            WorldHelper.syncBlockEntityData(this);
            this.lastColdFuel = this.getColdFuel();
            this.lastHotFuel = this.getHotFuel();
        }
    }

    protected SoundEvent getFuelDepleteSound()
    {   return ModSounds.HEARTH_FUEL.value();
    }

    public void checkForSmokestack()
    {
        if (level == null) return;

        boolean hadSmokestack = this.hasSmokestack;
        this.hasSmokestack = level.getBlockState(this.getBlockPos().above()).getBlock() == ModBlocks.SMOKESTACK.value();
        if (!this.hasSmokestack && hadSmokestack)
        {   this.resetPaths();
            HearthSaveDataHandler.HEARTH_POSITIONS.remove(Pair.of(this.getBlockPos(), this.getLevel().dimension().location()));
            if (this.level.isClientSide)
            {   ClientOnlyHelper.removeHearthPosition(this.getBlockPos());
            }
        }
        else if (this.hasSmokestack && !hadSmokestack)
        {   HearthSaveDataHandler.HEARTH_POSITIONS.add(Pair.of(this.getBlockPos(), this.getLevel().dimension().location()));
            if (this.level.isClientSide)
            {   ClientOnlyHelper.addHearthPosition(this.getBlockPos());
            }
        }
    }

    protected void tickParticles()
    {
        // Calculate the height of the smokestack (can be extended with walls)
        if (this.ticksExisted % 20 == 0)
        {   this.smokestackHeight = 2;
            BlockState state = level.getBlockState(this.getBlockPos().above(this.smokestackHeight));
            while (state.getBlock() instanceof WallBlock || state.getBlock() instanceof SmokestackBlock)
            {   smokestackHeight++;
                state = level.getBlockState(this.getBlockPos().above(this.smokestackHeight));
            }
        }

        Random rand = new Random();
        if (rand.nextDouble() < this.getColdFuel() / 3000d)
        {   double d0 = this.x + 0.5d;
            double d1 = this.y + smokestackHeight;
            double d2 = this.z + 0.5d;
            double d3 = (rand.nextDouble() - 0.5) / 4;
            double d4 = (rand.nextDouble() - 0.5) / 4;
            double d5 = (rand.nextDouble() - 0.5) / 4;
            level.addParticle(ModParticleTypes.STEAM.get(), d0 + d3, d1 + d4, d2 + d5, 0.0D, 0.04D, 0.0D);
        }
        if (rand.nextDouble() < this.getHotFuel() / 3000d)
        {   double d0 = this.x + 0.5d;
            double d1 = this.y + smokestackHeight;
            double d2 = this.z + 0.5d;
            double d3 = (rand.nextDouble() - 0.5) / 2;
            double d4 = (rand.nextDouble() - 0.5) / 2;
            double d5 = (rand.nextDouble() - 0.5) / 2;
            SimpleParticleType particle = Math.random() < 0.5 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE;
            level.addParticle(particle, d0 + d3, d1 + d4, d2 + d5, 0.0D, 0.0D, 0.0D);
        }
    }

    public ParticleOptions getAirParticle()
    {   return ModParticleTypes.HEARTH_AIR.get();
    }

    public void spawnAirParticle(int x, int y, int z, Random rand)
    {
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
    protected AbstractContainerMenu createMenu(int id, Inventory playerInv)
    {   return new HearthContainer(id, playerInv, this);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {   super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.loadEffects(tag);
        this.coldFuel = FluidStack.parse(registries, tag.getCompound("ColdFuel")).orElse(FluidStack.EMPTY);
        this.hotFuel = FluidStack.parse(registries, tag.getCompound("HotFuel")).orElse(FluidStack.EMPTY);
        this.insulationLevel = tag.getInt("InsulationLevel");
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {   super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        saveEffects(tag);
        if (!this.coldFuel.isEmpty())
            tag.put("ColdFuel", this.coldFuel.save(registries));
        if (!this.hotFuel.isEmpty())
            tag.put("HotFuel", this.hotFuel.save(registries));
        tag.putInt("InsulationLevel", this.insulationLevel);
    }

    void saveEffects(CompoundTag tag)
    {
        if (!this.effects.isEmpty())
        {   ListTag list = new ListTag();
            for (MobEffectInstance effect : this.effects)
            {   list.add(effect.save());
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
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("HotFuel",  this.getHotFuel());
        tag.putInt("ColdFuel", this.getColdFuel());
        tag.putInt("InsulationLevel", insulationLevel);
        this.saveEffects(tag);

        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries)
    {   this.setHotFuel(tag.getInt("HotFuel"), false);
        this.setColdFuel(tag.getInt("ColdFuel"), false);
        this.updateFuelState();
        this.insulationLevel = tag.getInt("InsulationLevel");
        this.loadEffects(tag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries)
    {   handleUpdateTag(pkt.getTag(), registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {   return ClientboundBlockEntityDataPacket.create(this);
    }

    public void replacePaths(ArrayList<SpreadPath> newPaths)
    {   this.frozenPaths = 0;
        this.paths = newPaths;
        this.pathLookup = newPaths.stream().map(path -> path.pos).collect(HashSet::new, HashSet::add, HashSet::addAll);
        this.spreading = true;
        if (this.level.isClientSide)
        {   ClientOnlyHelper.addHearthPosition(this.getBlockPos());
        }
        this.sendResetPacket();
    }

    public void addPath(SpreadPath path)
    {   paths.add(path);
    }

    public void addPaths(Collection<SpreadPath> newPaths)
    {   paths.addAll(newPaths);
    }

    public void sendResetPacket()
    {
        if (level instanceof ServerLevel serverLevel)
        {   PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(x >> 4, z >> 4), new HearthResetMessage(this.getBlockPos()));
        }
    }

    public void sendBlockUpdate()
    {   isRebuildQueued = true;
    }

    public void forceUpdate()
    {   this.forceRebuild = true;
        this.sendBlockUpdate();
    }

    @Override
    public void setRemoved()
    {   super.setRemoved();
        HearthSaveDataHandler.HEARTH_POSITIONS.remove(Pair.of(this.getBlockPos(), this.getLevel().dimension().location()));
        if (this.level.isClientSide)
        {   ClientOnlyHelper.removeHearthPosition(this.getBlockPos());
        }
    }

    public Set<BlockPos> getPathLookup()
    {   return this.pathLookup;
    }

    public static class FluidProvider implements ICapabilityProvider<HearthBlockEntity, Direction, FluidHandler>
    {
        @Nullable
        @Override
        public FluidHandler getCapability(HearthBlockEntity hearth, Direction facing)
        {
            return facing == Direction.DOWN
                           ? new BottomFluidHandler(hearth)
                 : facing != Direction.UP
                           ? new SidesFluidHandler(hearth)
                 : null;
        }
    }

    public abstract static class FluidHandler implements IFluidHandler
    {
        HearthBlockEntity hearth;

        public FluidHandler(HearthBlockEntity hearth)
        {   this.hearth = hearth;
        }

        @Override
        public int getTanks()
        {   return 2;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank)
        {   return tank == 0 ? hearth.coldFuel : hearth.hotFuel;
        }

        @Override
        public int getTankCapacity(int tank)
        {   return hearth.getMaxFuel();
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack fluidStack)
        {
            return tank == 0 ? fluidStack.getFluid() == Fluids.WATER 
                             : fluidStack.getFluid() == Fluids.LAVA;
        }

        @Override
        public int fill(FluidStack fluidStack, FluidAction fluidAction)
        {
            if (fluidStack.getFluid() == Fluids.WATER)
            {   int amount = Math.min(fluidStack.getAmount(), this.getTankCapacity(0) - hearth.coldFuel.getAmount());
                if (fluidAction.execute())
                {
                    if (hearth.coldFuel.isEmpty())
                    {   hearth.coldFuel = fluidStack.copy();
                    }
                    else hearth.coldFuel.grow(amount);
                }
                return amount;
            }
            else if (fluidStack.getFluid() == Fluids.LAVA)
            {   int amount = Math.min(fluidStack.getAmount(), this.getTankCapacity(1) - hearth.hotFuel.getAmount());
                if (fluidAction.execute())
                {
                    if (hearth.hotFuel.isEmpty())
                    {   hearth.hotFuel = fluidStack.copy();
                    }
                    else hearth.hotFuel.grow(amount);
                }
                return amount;
            }
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction fluidAction)
        {
            return this.isFluidValid(0, fluidStack) || this.isFluidValid(1, fluidStack)
                 ? this.drain(fluidStack.getAmount(), fluidAction)
                 : FluidStack.EMPTY;
        }

        @Override
        public abstract FluidStack drain(int amount, FluidAction fluidAction);
    }

    /**
     * Drains from water storage by default
     */
    public static class SidesFluidHandler extends FluidHandler
    {
        public SidesFluidHandler(HearthBlockEntity hearth)
        {   super(hearth);
        }

        @Override
        public FluidStack drain(int amount, FluidAction fluidAction)
        {
            int drained = Math.min(hearth.coldFuel.getAmount(), amount);

            FluidStack stack = new FluidStack(hearth.coldFuel.getFluidHolder(), drained);
            if (fluidAction.execute() && drained > 0)
            {   hearth.coldFuel.shrink(drained);
            }
            hearth.setChanged();

            return stack;
        }
    }

    /**
     * Drains from lava storage by default
     */
    public static class BottomFluidHandler extends FluidHandler
    {
        public BottomFluidHandler(HearthBlockEntity hearth)
        {   super(hearth);
        }

        @Override
        public FluidStack drain(int amount, FluidAction fluidAction)
        {
            int drained = Math.min(hearth.hotFuel.getAmount(), amount);

            FluidStack stack = new FluidStack(hearth.hotFuel.getFluidHolder(), drained);
            if (fluidAction.execute() && drained > 0)
            {   hearth.hotFuel.shrink(drained);
            }
            hearth.setChanged();

            return stack;
        }
    }
}