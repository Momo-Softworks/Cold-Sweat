package dev.momostudios.coldsweat.common.blockentity;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.util.TempHelper;
import dev.momostudios.coldsweat.common.block.HearthBottomBlock;
import dev.momostudios.coldsweat.common.container.HearthContainer;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.core.init.BlockEntityInit;
import dev.momostudios.coldsweat.core.init.ParticleTypesInit;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.BlockDataUpdateMessage;
import dev.momostudios.coldsweat.core.network.message.HearthResetMessage;
import dev.momostudios.coldsweat.util.config.ConfigCache;
import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.config.LoadedValue;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import dev.momostudios.coldsweat.util.registries.ModSounds;
import dev.momostudios.coldsweat.util.world.SpreadPath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HearthBlockEntity extends RandomizableContainerBlockEntity
{
    ConfigCache config = ConfigCache.getInstance();

    ArrayList<SpreadPath> paths = new ArrayList<>();
    HashMap<ChunkPos, LevelChunk> loadedChunks = new HashMap<>();

    public static final int MAX_PATHS = 6000;
    public static int MAX_DISTANCE = 32;

    static int INSULATION_TIME = 1200;

    public static int SLOT_COUNT = 1;
    protected NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    BlockPos blockPos = this.getBlockPos();

    int hotFuel = 0;
    int coldFuel = 0;
    int insulationLevel = 0;

    boolean shouldUseHotFuel = false;
    boolean shouldUseColdFuel = false;
    boolean hasHotFuel = false;
    boolean hasColdFuel = false;

    boolean isPlayerNearby = false;
    boolean shouldRebuild = false;
    int rebuildCooldown = 0;

    public int ticksExisted = 0;
    int pathTicker = 0;

    private LevelChunk workingChunk = null;
    private ChunkPos workingCoords = new ChunkPos(this.getBlockPos().getX() >> 4, this.getBlockPos().getZ() >> 4);

    public static final int MAX_FUEL = 1000;
    public static LoadedValue<Map<Item, Number>> VALID_FUEL = LoadedValue.of(() -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().hearthItems()));

    public HearthBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockEntityInit.HEARTH_BLOCK_ENTITY_TYPE.get(), pos, state);
        this.addPath(new SpreadPath(this.getBlockPos()));
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container." + ColdSweat.MOD_ID + ".hearth");
    }

    @Override
    public Component getDisplayName()
    {
        return this.getCustomName() != null ? this.getCustomName() : this.getDefaultName();
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("hotFuel",  this.getHotFuel());
        tag.putInt("coldFuel", this.getColdFuel());
        tag.putInt("insulationLevel", insulationLevel);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag)
    {
        super.handleUpdateTag(tag);
        this.setHotFuel(tag.getInt("hotFuel"));
        this.setColdFuel(tag.getInt("coldFuel"));
        insulationLevel = tag.getInt("insulationLevel");
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
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
        {
            hearth.tick(level, pos);
        }
    }

    public void tick(Level level, BlockPos pos)
    {
        addPath(pos, new SpreadPath(pos));

        this.ticksExisted++;
        this.pathTicker++;

        if (rebuildCooldown > 0) rebuildCooldown--;

        // Gradually increases insulation amount
        if (insulationLevel < INSULATION_TIME)
            insulationLevel++;

        if (this.ticksExisted % 20 == 0)
        {
            this.isPlayerNearby = level.players().stream().anyMatch(player -> player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 400);
        }

        // Reset if a nearby block has been updated
        if (rebuildCooldown <= 0 && this.shouldRebuild)
        {
            this.shouldRebuild = false;
            this.rebuildCooldown = 100;

            this.replacePaths(Map.of(pos, new SpreadPath(pos)));

            if (!level.isClientSide)
            {
                ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4)), new HearthResetMessage(pos));
            }
        }

        if (level != null && (hotFuel > 0 || coldFuel > 0) && this.isPlayerNearby)
        {
            boolean showParticles = level.isClientSide && !this.getTileData().getBoolean("hideParticles");
            int pathCount = paths.size();

            // Create temporary list to add back to the master path list
            ArrayList<SpreadPath> newPaths = new ArrayList<>();

            /*
             Partition the points into logical "sub-maps" to be iterated over separately each tick
            */
            // Starting index (-1 because it is incremented before it is gotten)
            int index = -1;
            // Size of each partition
            int partSize = 200;
            // Number of partitions
            int partCount = (int) Math.ceil(pathCount / (double) partSize);
            // Index of the last point being worked on this tick
            int lastIndex = Math.min(pathCount, partSize * (this.pathTicker % partCount + 1) + 1);
            // Index of the first point being worked on this tick
            int firstIndex = Math.max(0, lastIndex - partSize);

            if (lastIndex >= pathCount - 1) this.pathTicker = 0;

            // Iterate over the specified partition of paths
            for (int i = firstIndex; i < lastIndex; i++)
            {   SpreadPath spreadPath = paths.get(i);

                BlockPos blockPos = entry.getKey();

                int x = blockPos.getX();
                int y = blockPos.getY();
                int z = blockPos.getZ();

                // Air Particles
                if (showParticles)
                {
                    // Spawn particles if enabled
                    if (Minecraft.getInstance().options.renderDebug)
                    {
                        level.addAlwaysVisibleParticle(ParticleTypes.FLAME, false, x + 0.5, y + 0.5, z + 0.5, 0, 0, 0);
                    }
                    else if (Math.random() < 0.016)
                    {
                        double xr = Math.random();
                        double yr = Math.random();
                        double zr = Math.random();
                        double xm = Math.random() / 20 - 0.025;
                        double zm = Math.random() / 20 - 0.025;

                        level.addAlwaysVisibleParticle(ParticleTypesInit.HEARTH_AIR.get(), false, x + xr, y + yr, z + zr, xm, 0, zm);
                    }
                }

                // Give insulation to players
                if (!level.isClientSide)
                {
                    for (Player player : level.players())
                    {
                        if (CSMath.getDistance(player, x + 0.5, y + 0.5, z + 0.5) > 0.6) continue;

                        MobEffectInstance effect = player.getEffect(ModEffects.INSULATION);
                        boolean hasEffect = effect != null;

                        if (!hasEffect || effect.getDuration() < 60)
                        {
                            // Get the player's temperature
                            double temp = hasEffect ? player.getPersistentData().getDouble("preHearthTemp") :
                                    TempHelper.getTemperature(player, Temperature.Type.WORLD).get();

                            // Tell the hearth to use hot fuel
                            shouldUseHotFuel = shouldUseHotFuel || (temp < config.minTemp);

                            // Tell the hearth to use cold fuel
                            shouldUseColdFuel = shouldUseColdFuel || (temp > config.maxTemp);

                            if (shouldUseHotFuel || shouldUseColdFuel)
                            {
                                int effectLevel = Math.max(0, (int) ((insulationLevel / (double) INSULATION_TIME) * 9));
                                player.addEffect(new MobEffectInstance(ModEffects.INSULATION, 100, effectLevel, false, false));
                            }
                        }
                        break;
                    }
                }

                SpreadPath spreadPath = entry.getValue();

                // Don't try to spread if the path is frozen
                if (spreadPath.isFrozen()) continue;

                /*
                 Try to spread to new blocks
                 */
                if (pathCount < MAX_PATHS && spreadPath.withinDistance(pos, MAX_DISTANCE))
                {
                    /*
                     Get the chunk at this position
                     */
                    ChunkPos chunkPos = new ChunkPos(x >> 4, z >> 4);

                    LevelChunk chunk;

                    if (chunkPos == workingCoords)
                    {
                        chunk = workingChunk;
                    }
                    else
                    {
                        workingChunk = chunk = loadedChunks.get(chunkPos);
                        workingCoords = chunkPos;
                    }

                    if (chunk == null)
                    {
                        loadedChunks.put(chunkPos, workingChunk = chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4));
                    }
                    if (chunk == null) continue;

                    /*
                     Spreading algorithm
                     */
                    LevelChunk finalChunk = chunk;
                    if (!WorldHelper.canSeeSky(finalChunk, level, blockPos.above()))
                    {
                        for (Direction direction : Direction.values())
                        {
                            SpreadPath tryPath = spreadPath.offset(direction);
                            newPaths.computeIfAbsent(tryPath.getPos(), pos2 ->
                            {
                                if (!WorldHelper.isSpreadBlocked(finalChunk, blockPos, direction))
                                    return tryPath;
                                return null;
                            });
                        }
                    }
                }
                spreadPath.freeze();
            }

            // Add new positions
            addPaths(newPaths);

            // Drain fuel
            if (!level.isClientSide && this.ticksExisted % 80 == 0)
            {
                if (shouldUseColdFuel)
                {
                    this.setColdFuel(coldFuel - 1);
                }
                if (shouldUseHotFuel)
                {
                    this.setHotFuel(hotFuel - 1);
                }

                shouldUseColdFuel = false;
                shouldUseHotFuel = false;
            }
        }

        // Input fuel
        if (this.ticksExisted % 10 == 0)
        {
            ItemStack fuelStack = this.getItem(0);
            int itemFuel = getItemFuel(fuelStack);
            if (itemFuel != 0)
            {
                int fuel = itemFuel > 0 ? hotFuel : coldFuel;
                if (fuel < MAX_FUEL - Math.abs(itemFuel) * 0.75)
                {
                    if (fuelStack.hasContainerItem())
                    {
                        if (fuelStack.getCount() == 1)
                        {
                            this.setItem(0, fuelStack.getContainerItem());
                            addFuel(itemFuel, hotFuel, coldFuel);
                        }
                    }
                    else
                    {
                        int consumeCount = Math.min((int) Math.floor((MAX_FUEL - fuel) / (double) Math.abs(itemFuel)), fuelStack.getCount());
                        fuelStack.shrink(consumeCount);
                        addFuel(itemFuel * consumeCount, hotFuel, coldFuel);
                    }
                }
            }
        }

        // Particles
        if (Math.random() < coldFuel / 3000d)
        {
            double d0 = pos.getX() + 0.5d;
            double d1 = pos.getY() + 1.8d;
            double d2 = pos.getZ() + 0.5d;
            double d3 = (Math.random() - 0.5) / 4;
            double d4 = (Math.random() - 0.5) / 4;
            double d5 = (Math.random() - 0.5) / 4;
            level.addParticle(ParticleTypesInit.STEAM.get(), d0 + d3, d1 + d4, d2 + d5, 0.0D, 0.04D, 0.0D);
        }
        if (Math.random() < hotFuel / 3000d)
        {
            double d0 = pos.getX() + 0.5d;
            double d1 = pos.getY() + 1.8d;
            double d2 = pos.getZ() + 0.5d;
            double d3 = (Math.random() - 0.5) / 2;
            double d4 = (Math.random() - 0.5) / 2;
            double d5 = (Math.random() - 0.5) / 2;
            SimpleParticleType particle = Math.random() < 0.5 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE;
            level.addParticle(particle, d0 + d3, d1 + d4, d2 + d5, 0.0D, 0.0D, 0.0D);
        }
    }

    public static int getItemFuel(ItemStack item)
    {
        return VALID_FUEL.get().getOrDefault(item.getItem(), 0).intValue();
    }

    public int getHotFuel()
    {
        return this.hotFuel;
    }

    public int getColdFuel()
    {
        return this.coldFuel;
    }

    public void setHotFuel(int amount)
    {
        this.hotFuel = (int) CSMath.clamp(amount, 0, MAX_FUEL);
        this.updateFuelState();

        if (amount == 0 && hasHotFuel)
        {
            hasHotFuel = false;
            level.playSound(null, blockPos, ModSounds.HEARTH_FUEL, SoundSource.BLOCKS, 1, (float) Math.random() * 0.2f + 0.9f);
        }
        else hasHotFuel = true;
    }

    public void setColdFuel(int amount)
    {
        this.coldFuel = (int) CSMath.clamp(amount, 0, MAX_FUEL);
        this.updateFuelState();

        if (amount == 0 && hasColdFuel)
        {
            hasColdFuel = false;
            level.playSound(null, blockPos, ModSounds.HEARTH_FUEL, SoundSource.BLOCKS, 1, (float) Math.random() * 0.2f + 0.9f);
        }
        else hasColdFuel = true;
    }

    public void addFuel(int amount)
    {
        this.addFuel(amount, this.getHotFuel(), this.getColdFuel());
    }

    public void addFuel(int amount, int hotFuel, int coldFuel)
    {
        if (amount > 0)
        {
            setHotFuel(hotFuel + Math.abs(amount));
        }
        else if (amount < 0)
        {
            setColdFuel(coldFuel + Math.abs(amount));
        }
    }

    public void updateFuelState()
    {
        if (level != null && !level.isClientSide)
        {
            int hotFuel = this.getHotFuel();
            int coldFuel = this.getColdFuel();

            BlockState state = level.getBlockState(blockPos);
            int waterLevel = coldFuel == 0 ? 0 : (coldFuel < MAX_FUEL / 2 ? 1 : 2);
            int lavaLevel = hotFuel == 0 ? 0 : (hotFuel < MAX_FUEL / 2 ? 1 : 2);

            BlockState desiredState = state.setValue(HearthBottomBlock.WATER, waterLevel).setValue(HearthBottomBlock.LAVA, lavaLevel);
            if (state.getValue(HearthBottomBlock.WATER) != waterLevel || state.getValue(HearthBottomBlock.LAVA) != lavaLevel)
            {
                level.setBlock(blockPos, desiredState, 3);
            }
            this.setChanged();

            CompoundTag tag = new CompoundTag();
            this.saveAdditional(tag);
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(blockPos)),
                    new BlockDataUpdateMessage(this));
        }
    }

    @Override
    public int getContainerSize()
    {
        return SLOT_COUNT;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInv)
    {
        return new HearthContainer(id, playerInv, this);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        this.setColdFuel(tag.getInt("coldFuel"));
        this.setHotFuel(tag.getInt("hotFuel"));
        this.insulationLevel = tag.getInt("insulationLevel");
        ContainerHelper.loadAllItems(tag, this.items);
    }

    @Override
    public void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("coldFuel", this.getColdFuel());
        tag.putInt("hotFuel", this.getHotFuel());
        tag.putInt("insulationLevel", insulationLevel);
        ContainerHelper.saveAllItems(tag, this.items);
    }

    public void replacePaths(Map<BlockPos, SpreadPath> newPaths)
    {
        paths.clear();
        this.addPaths(newPaths);
    }

    public void addPath(SpreadPath path) {
        paths.put(path.blockPos(), path);
    }

    public void addPath(BlockPos pos, SpreadPath path) {
        paths.put(pos, path);
    }

    public void addPaths(Map<BlockPos, SpreadPath> newPaths)
    {
        paths.putAll(newPaths);
    }

    public void resetPaths()
    {
        this.shouldRebuild = true;
    }
}