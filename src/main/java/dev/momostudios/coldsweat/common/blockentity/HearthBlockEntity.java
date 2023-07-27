package dev.momostudios.coldsweat.common.blockentity;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.event.common.BlockStateChangedEvent;
import dev.momostudios.coldsweat.api.temperature.modifier.HearthTempModifier;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.common.block.HearthBottomBlock;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.container.HearthContainer;
import dev.momostudios.coldsweat.common.event.HearthSaveDataHandler;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.core.init.BlockEntityInit;
import dev.momostudios.coldsweat.core.init.ParticleTypesInit;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.HearthResetMessage;
import dev.momostudios.coldsweat.util.ClientOnlyHelper;
import dev.momostudios.coldsweat.util.compat.CompatManager;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import dev.momostudios.coldsweat.util.registries.ModSounds;
import dev.momostudios.coldsweat.util.world.SpreadPath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.*;

@Mod.EventBusSubscriber
public class HearthBlockEntity extends RandomizableContainerBlockEntity
{
    // List of SpreadPaths, which determine where the Hearth is affecting and how it spreads through/around blocks
    List<SpreadPath> paths = new ArrayList<>();
    // Used as a lookup table for detecting duplicate paths (faster than ArrayList#contains())
    Set<BlockPos> pathLookup = new HashSet<>();
    Map<Pair<Integer, Integer>, Pair<Integer, Boolean>> seeSkyMap = new HashMap<>();

    List<MobEffectInstance> effects = new ArrayList<>();

    private static final int INSULATION_TIME = 1200;
    public static final int MAX_FUEL = 1000;
    public static final int SLOT_COUNT = 1;
    private static final boolean CREATE_LOADED = CompatManager.isCreateLoaded();

    protected NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    Pair<BlockPos, ResourceLocation> levelPos = Pair.of(null, null);
    int x = 0;
    int y = 0;
    int z = 0;

    int hotFuel = 0;
    int lastHotFuel = 0;
    int coldFuel = 0;
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

    private boolean registeredLocation = false;

    boolean showParticles = true;
    int frozenPaths = 0;
    boolean spreading = true;

    private static final Direction[] DIRECTIONS = Direction.values();

    static Method TICK_DOWN_EFFECT;
    static
    {
        try
        {   TICK_DOWN_EFFECT = ObfuscationReflectionHelper.findMethod(MobEffectInstance.class, "m_19579_");
            TICK_DOWN_EFFECT.setAccessible(true);
        }
        catch (Exception ignored) {}
    }

    public HearthBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockEntityInit.HEARTH_BLOCK_ENTITY_TYPE.get(), pos, state);
        this.addPath(new SpreadPath(pos).setOrigin(this.getBlockPos()));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBlockUpdate(BlockStateChangedEvent event)
    {
        BlockPos pos = event.getPosition();
        Level level = event.getLevel();
        if (level == this.level
        && CSMath.withinCube(pos, this.getBlockPos(), this.getMaxRange()) && pathLookup.contains(pos)
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

    public int maxPaths()
    {   return 9000;
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
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemsIn)
    {
        this.items = itemsIn;
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
        if (!this.registeredLocation)
        {   levelPos = Pair.of(this.getBlockPos(), level.dimension().location());
            HearthSaveDataHandler.HEARTH_POSITIONS.add(levelPos);
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            this.registeredLocation = true;
        }

        // Easy access to clientside testList::stream
        boolean isClient = level.isClientSide;

        this.ticksExisted++;

        if (rebuildCooldown > 0) rebuildCooldown--;

        if (this.level != null && this.ticksExisted % 20 == 0)
        {
            this.isPlayerNearby = false;
            players.clear();
            for (Player player : this.level.players())
            {
                if (player.blockPosition().closerThan(pos, this.getMaxRange()))
                {
                    players.add(player);
                    this.isPlayerNearby = true;
                }
            }
        }

        // Tick down the time for each effect
        if (!effects.isEmpty())
        {
            effects.removeIf(effect ->
            {
                try
                {
                    TICK_DOWN_EFFECT.invoke(effect);
                    if (effect.getDuration() <=0) return true;
                } catch (Exception ignored) {}
                return false;
            });
        }

        // Clear paths every 5 minutes to account for calculation errors
        if (this.ticksExisted % 6000 == 0)
        {   this.replacePaths(new ArrayList<>(Collections.singletonList(new SpreadPath(pos).setOrigin(pos))));
        }

        // Reset if a nearby block has been updated
        if (forceRebuild || (rebuildCooldown <= 0 && isRebuildQueued))
        {   this.updateNotifiedPaths();
        }

        if (hotFuel > 0 || coldFuel > 0)
        {
            // Gradually increases insulation amount
            if (insulationLevel < INSULATION_TIME)
                insulationLevel++;

            if (this.isPlayerNearby)
            {
                if (this.ticksExisted % 10 == 0)
                {
                    showParticles = isClient
                            && Minecraft.getInstance().options.particles == ParticleStatus.ALL
                            && !HearthSaveDataHandler.DISABLED_HEARTHS.contains(levelPos);
                }

                if (paths.isEmpty())
                    this.addPath(new SpreadPath(pos).setOrigin(pos));

                // Mark as not spreading if all paths are frozen
                if (this.frozenPaths >= paths.size())
                    this.spreading = false;

                /*
                 Partition the points into logical "sub-maps" to be iterated over separately each tick
                */
                int pathCount = paths.size();
                // Size of each partition (defaults to 1/30th of the total paths)
                int partSize = spreading ? CSMath.clamp(pathCount / 3, 100, 4000)
                                         : CSMath.clamp(pathCount / 20, 10, 100);
                // Number of partitions
                int partCount = (int) Math.ceil(pathCount / (float) partSize);
                // Index of the last point being worked on this tick
                int lastIndex = partSize * ((this.ticksExisted % partCount) + 1);
                // Index of the first point being worked on this tick
                int firstIndex = Math.max(0, lastIndex - partSize);

                /*
                 Iterate over the specified partition of paths
                 */
                for (int i = firstIndex; i < Math.min(paths.size(), lastIndex); i++)
                {   // This operation is really fast because it's an ArrayList
                    SpreadPath spreadPath = paths.get(i);
                    BlockPos pathPos = spreadPath.pos;
                    if (spreadPath.origin == null)
                        spreadPath.setOrigin(pos);

                    int spX = spreadPath.x;
                    int spY = spreadPath.y;
                    int spZ = spreadPath.z;

                    // Use try-finally because there's still stuff to do even if "continue;" skips the rest of the code
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
                        if (pathCount < this.maxPaths() && spreadPath.withinDistance(spreadPath.origin, this.getSpreadRange())
                        && CSMath.withinCube(spreadPath.origin, pos, this.getMaxRange()))
                        {
                            /*
                             Get the chunk at this position
                             */

                            /*
                             Spreading algorithm
                             */
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

                            if (!canSeeSky)
                            {
                                BlockState state = level.getBlockState(pathPos);

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
                                        if (this.canSpreadThroughPipes(tryPos, state, newPath, direction)
                                        && !WorldHelper.isSpreadBlocked(level, state, pathPos, direction, spreadPath.direction))
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
                        if (isClient && showParticles)
                        {
                            Random rand = new Random();
                            if (!(Minecraft.getInstance().options.renderDebug && ClientSettingsConfig.getInstance().isHearthDebugEnabled()) && rand.nextFloat() < (spreading ? 0.016f : 0.032f))
                            {   float xr = rand.nextFloat();
                                float yr = rand.nextFloat();
                                float zr = rand.nextFloat();
                                float xm = rand.nextFloat() / 20 - 0.025f;
                                float zm = rand.nextFloat() / 20 - 0.025f;

                                level.addParticle(ParticleTypesInit.HEARTH_AIR.get(), false, spX + xr, spY + yr, spZ + zr, xm, 0, zm);
                            }
                        }
                    }
                }

                // Give insulation to players
                if (!isClient && this.ticksExisted % 20 == 0)
                {
                    for (int i = 0; i < players.size(); i++)
                    {
                        Player player = players.get(i);
                        if (player != null && pathLookup.contains(player.blockPosition()))
                        {   this.insulatePlayer(player);
                        }
                    }
                    players.clear();
                }

                // Drain fuel
                if (this.ticksExisted % 40 == 0)
                {
                    if (shouldUseColdFuel)
                    {   this.setColdFuel(coldFuel - 1, true);
                    }
                    if (shouldUseHotFuel)
                    {   this.setHotFuel(hotFuel - 1, true);
                    }
                    if (Math.abs(coldFuel - lastColdFuel) > 27|| Math.abs(hotFuel - lastHotFuel) > 27)
                    {   this.updateFuelState();
                        this.lastColdFuel = coldFuel;
                        this.lastHotFuel = hotFuel;
                    }

                    shouldUseColdFuel = false;
                    shouldUseHotFuel = false;
                }
            }
        }
        // Input fuel
        if (this.ticksExisted % 100 == 0)
        {   this.checkForFuel();
        }

        // Particles
        if (isClient)
        {
            Random rand = new Random();
            if (rand.nextDouble() < coldFuel / 3000d)
            {   double d0 = this.x + 0.5d;
                double d1 = this.y + 1.8d;
                double d2 = this.z + 0.5d;
                double d3 = (rand.nextDouble() - 0.5) / 4;
                double d4 = (rand.nextDouble() - 0.5) / 4;
                double d5 = (rand.nextDouble() - 0.5) / 4;
                level.addParticle(ParticleTypesInit.STEAM.get(), d0 + d3, d1 + d4, d2 + d5, 0.0D, 0.04D, 0.0D);
            }
            if (rand.nextDouble() < hotFuel / 3000d)
            {   double d0 = this.x + 0.5d;
                double d1 = this.y + 1.8d;
                double d2 = this.z + 0.5d;
                double d3 = (rand.nextDouble() - 0.5) / 2;
                double d4 = (rand.nextDouble() - 0.5) / 2;
                double d5 = (rand.nextDouble() - 0.5) / 2;
                SimpleParticleType particle = Math.random() < 0.5 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE;
                level.addParticle(particle, d0 + d3, d1 + d4, d2 + d5, 0.0D, 0.0D, 0.0D);
            }
        }
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
            && itemEffects.stream().noneMatch(eff -> ConfigSettings.BLACKLISTED_POTIONS.get().contains(ForgeRegistries.MOB_EFFECTS.getKey(eff.getEffect()))))
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
                ColdSweatPacketHandler.syncBlockEntityData(this);
            }
            else if (fuelStack.is(Items.MILK_BUCKET) && !effects.isEmpty())
            {
                this.getItems().set(0, fuelStack.getContainerItem());
                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.WITCH_DRINK, SoundSource.BLOCKS, 1, 1);
                effects.clear();
                ColdSweatPacketHandler.syncBlockEntityData(this);
            }
            // Normal fuel items
            else
            {
                int itemFuel = getItemFuel(fuelStack);
                if (itemFuel != 0)
                {
                    int fuel = itemFuel > 0 ? hotFuel : coldFuel;
                    if (fuel < MAX_FUEL - Math.abs(itemFuel) * 0.75)
                    {
                        if (!fuelStack.hasContainerItem() || fuelStack.getCount() > 1)
                        {   int consumeCount = Math.min((int) Math.floor((MAX_FUEL - fuel) / (double) Math.abs(itemFuel)), fuelStack.getCount());
                            fuelStack.shrink(consumeCount);
                            addFuel(itemFuel * consumeCount);
                        }
                        else
                        {   this.setItem(0, fuelStack.getContainerItem());
                            addFuel(itemFuel);
                        }
                    }
                }
            }
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
        player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
        {   double temp = cap.getTemp(Temperature.Type.WORLD);
            double min = ConfigSettings.MIN_TEMP.get() + cap.getTemp(Temperature.Type.BURNING_POINT);
            double max = ConfigSettings.MAX_TEMP.get() + cap.getTemp(Temperature.Type.FREEZING_POINT);

            // If the player is habitable, check the input temperature reported by their HearthTempModifier (if they have one)
            if (CSMath.withinRange(temp, min, max))
            {
                // Find the player's HearthTempModifier
                TempModifier modifier = null;
                for (TempModifier tempModifier : cap.getModifiers(Temperature.Type.WORLD))
                {   if (tempModifier instanceof HearthTempModifier)
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
            shouldUseHotFuel |= hotFuel > 0 && temp < min;
            // Tell the hearth to use cold fuel
            shouldUseColdFuel |= coldFuel > 0 && temp > max;

            if (shouldUseHotFuel || shouldUseColdFuel)
            {   int effectLevel = Math.min(9, (insulationLevel / INSULATION_TIME) * 9);
                player.addEffect(new MobEffectInstance(ModEffects.INSULATION, 120, effectLevel, false, false, true));
            }
        });
    }

    private boolean canSpreadThroughPipes(BlockPos newPos, BlockState fromState, SpreadPath newPath, Direction direction)
    {
        if (CREATE_LOADED)
        {
            Block block = fromState.getBlock();
            if (!(block instanceof FluidPipeBlock) && !(block instanceof GlassFluidPipeBlock))
            {   return true;
            }
            if ((block instanceof FluidPipeBlock && fromState.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction)))
            || (block instanceof GlassFluidPipeBlock && fromState.getValue(RotatedPillarBlock.AXIS) == direction.getAxis()))
            {   newPath.setOrigin(newPos);
                return true;
            }
            return false;
        }
        return true;
    }

    void updateNotifiedPaths()
    {
        // Reset cooldown
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

    public static int getItemFuel(ItemStack item)
    {   return ConfigSettings.HEARTH_FUEL.get().getOrDefault(item.getItem(), 0d).intValue();
    }

    public int getHotFuel()
    {   return this.hotFuel;
    }

    public int getColdFuel()
    {   return this.coldFuel;
    }

    public void setHotFuel(int amount, boolean update)
    {
        this.hotFuel = CSMath.clamp(amount, 0, MAX_FUEL);

        if (amount == 0 && hasHotFuel)
        {   hasHotFuel = false;
            level.playSound(null, this.getBlockPos(), ModSounds.HEARTH_FUEL, SoundSource.BLOCKS, 1, (float) Math.random() * 0.2f + 0.9f);
        }
        else hasHotFuel = true;

        if (update) this.updateFuelState();
    }
    public void setHotFuelAndUpdate(int amount)
    {   setHotFuel(amount, true);
    }

    public void setColdFuel(int amount, boolean update)
    {
        this.coldFuel = CSMath.clamp(amount, 0, MAX_FUEL);

        if (amount <= 0 && hasColdFuel)
        {   hasColdFuel = false;
            level.playSound(null, this.getBlockPos(), ModSounds.HEARTH_FUEL, SoundSource.BLOCKS, 1, (float) Math.random() * 0.2f + 0.9f);
        }
        else hasColdFuel = true;

        if (update) this.updateFuelState();
    }
    public void setColdFuelAndUpdate(int amount)
    {   setColdFuel(amount, true);
    }

    public void addFuel(int amount)
    {   if (amount > 0)
            setHotFuelAndUpdate(this.hotFuel + amount);
        else if (amount < 0)
            setColdFuelAndUpdate(this.coldFuel + Math.abs(amount));
    }

    public void updateFuelState()
    {
        if (level != null && !level.isClientSide)
        {   int hotFuel = this.getHotFuel();
            int coldFuel = this.getColdFuel();

            BlockState state = level.getBlockState(this.getBlockPos());
            int waterLevel = coldFuel == 0 ? 0 : (coldFuel < MAX_FUEL / 2 ? 1 : 2);
            int lavaLevel = hotFuel == 0 ? 0 : (hotFuel < MAX_FUEL / 2 ? 1 : 2);

            BlockState desiredState = state.setValue(HearthBottomBlock.WATER, waterLevel).setValue(HearthBottomBlock.LAVA, lavaLevel);
            if (state.getValue(HearthBottomBlock.WATER) != waterLevel || state.getValue(HearthBottomBlock.LAVA) != lavaLevel)
                level.setBlock(this.getBlockPos(), desiredState, 3);

            this.setChanged();
            ColdSweatPacketHandler.syncBlockEntityData(this);
        }
    }

    @Override
    public int getContainerSize()
    {   return SLOT_COUNT;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInv)
    {   return new HearthContainer(id, playerInv, this);
    }

    @Override
    public void load(CompoundTag tag)
    {   super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        this.handleUpdateTag(tag);
        ContainerHelper.loadAllItems(tag, this.items);
    }

    @Override
    public void saveAdditional(CompoundTag tag)
    {   super.saveAdditional(tag);
        tag.merge(this.getUpdateTag());
        ContainerHelper.saveAllItems(tag, this.items);
    }

    void saveEffects(CompoundTag tag)
    {
        if (this.effects.size() > 0)
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
        tag.putInt("InsulationLevel", insulationLevel);
        this.saveEffects(tag);

        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag)
    {   this.setHotFuel(tag.getInt("HotFuel"), false);
        this.setColdFuel(tag.getInt("ColdFuel"), false);
        this.updateFuelState();
        this.insulationLevel = tag.getInt("InsulationLevel");
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
    {   if (level instanceof ServerLevel)
        {   ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() ->
                                 (LevelChunk) WorldHelper.getChunk(level, this.getBlockPos())), new HearthResetMessage(this.getBlockPos()));
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
        HearthSaveDataHandler.HEARTH_POSITIONS.remove(this.getBlockPos());
        if (this.level.isClientSide)
        {   ClientOnlyHelper.removeHearthPosition(this.getBlockPos());
        }
    }

    public Set<BlockPos> getPathLookup()
    {   return this.pathLookup;
    }
}