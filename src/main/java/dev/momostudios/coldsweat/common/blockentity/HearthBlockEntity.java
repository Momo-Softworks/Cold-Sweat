package dev.momostudios.coldsweat.common.blockentity;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.container.HearthContainer;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.core.init.BlockEntityInit;
import dev.momostudios.coldsweat.core.init.ParticleTypesInit;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.BlockDataUpdateMessage;
import dev.momostudios.coldsweat.util.entity.NBTHelper;
import dev.momostudios.coldsweat.util.entity.TempHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import dev.momostudios.coldsweat.util.registries.ModSounds;
import dev.momostudios.coldsweat.util.world.SpreadPath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.common.block.HearthBottomBlock;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;

import java.util.*;

public class HearthBlockEntity extends RandomizableContainerBlockEntity implements MenuProvider
{
    ConfigCache config = ConfigCache.getInstance();

    LinkedHashMap<BlockPos, SpreadPath> paths = new LinkedHashMap<>();
    HashMap<Pair<Integer, Integer>, LevelChunk> loadedChunks = new HashMap<>();
    public static final int MAX_PATHS = 6000;
    public static int MAX_DISTANCE = 20;

    static int INSULATION_TIME = 2400;

    public static int SLOT_COUNT = 1;
    protected NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    BlockPos pos = this.getBlockPos();

    boolean shouldUseHotFuel = false;
    boolean shouldUseColdFuel = false;
    boolean hasFuelItem = false;

    boolean isPlayerNearby = false;

    public int ticksExisted;

    private LevelChunk workingChunk = null;
    private Pair<Integer, Integer> workingCoords = new Pair<>(pos.getX() >> 4, pos.getZ() >> 4);

    public static final int MAX_FUEL = 1000;

    public HearthBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockEntityInit.HEARTH_BLOCK_ENTITY_TYPE.get(), pos, state);
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
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
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
        if (level.getBlockEntity(pos) instanceof HearthBlockEntity hearth)
        {
            hearth.tick();
        }
    }

    public void tick()
    {
        this.ticksExisted++;

        int coldFuel = this.getColdFuel();
        int hotFuel = this.getHotFuel();

        // Gradually increases insulation amount
        int insulationLevel = NBTHelper.incrementTag(this, "insulationLevel", 1, (nbt) -> nbt < 2400);

        if (this.ticksExisted % 20 == 0)
        {
            this.isPlayerNearby = level.players().stream().anyMatch(player -> player.distanceToSqr(this.pos.getX(), this.pos.getY(), this.pos.getZ()) < 400);
        }

        if (level != null && (hotFuel > 0 || coldFuel > 0) && this.isPlayerNearby)
        {
            boolean showParticles = level.isClientSide && this.getTileData().getBoolean("showRadius");

            // Reset every 1 minute
            if (this.ticksExisted % 1200 == 0 || this.shouldRebuild())
            {
                this.setShouldRebuild(false);
                this.resetPaths();
            }

            if (paths.isEmpty())
            {
                this.addPath(new SpreadPath(this.getBlockPos()));
            }

            // Create temporary list to add back to the NBT
            HashMap<BlockPos, SpreadPath> newPaths = new HashMap<>();

            /*
             Partition all points into multiple lists (max of 19)
            */
            int index = 0;
            // Size of each partition
            int partSize = 400;
            // Number of partitions
            int partCount = (int) Math.ceil(paths.size() / (double) partSize);
            // Index of the last point being worked on this tick
            int lastIndex = Math.min(paths.size(), partSize * (this.ticksExisted % partCount + 1) + 1);
            // Index of the first point being worked on this tick
            int firstIndex = Math.max(0, lastIndex - partSize);

            // Iterate over the specified partition of paths
            for (Map.Entry<BlockPos, SpreadPath> entry : paths.entrySet())
            {
                // Stop after we reach the maximum number of iterations for this partition
                if (index - firstIndex > partSize)
                {
                    break;
                }
                // Skip until we reach the first index of this partition
                if (index < firstIndex)
                {
                    index++;
                    continue;
                }

                index++;

                BlockPos blockPos = entry.getKey();

                int x = blockPos.getX();
                int y = blockPos.getY();
                int z = blockPos.getZ();

                // Air Particles
                if (showParticles && level instanceof ClientLevel clientLevel)
                {
                    // Spawn particles if enabled
                    if (Minecraft.getInstance().options.renderDebug)
                    {
                        clientLevel.addAlwaysVisibleParticle(ParticleTypes.FLAME, false, x + 0.5, y + 0.5, z + 0.5, 0, 0, 0);
                    }
                    else if (Math.random() < 0.016)
                    {
                        double xr = Math.random();
                        double yr = Math.random();
                        double zr = Math.random();
                        double xm = Math.random() / 20 - 0.025;
                        double zm = Math.random() / 20 - 0.025;

                        clientLevel.addAlwaysVisibleParticle(ParticleTypesInit.HEARTH_AIR.get(), false, x + xr, y + yr, z + zr, xm, 0, zm);
                    }
                }

                // Give insulation to players
                for (Player player : level.players())
                {
                    if (player.distanceToSqr(x, y, z) < 1)
                    {
                        MobEffectInstance effect = player.getEffect(ModEffects.INSULATION);
                        boolean hasEffect = effect != null;

                        if (!hasEffect || effect.getDuration() < 60)
                        {
                            // Get the player's temperature
                            double temp = hasEffect ? player.getPersistentData().getDouble("preHearthTemp") :
                                    TempHelper.getTemperature(player, Temperature.Types.WORLD).get();

                            // Tell the hearth to use hot fuel
                            shouldUseHotFuel = shouldUseHotFuel || (temp < config.minTemp);

                            // Tell the hearth to use cold fuel
                            shouldUseColdFuel = shouldUseColdFuel || (temp > config.maxTemp);

                            if (shouldUseHotFuel || shouldUseColdFuel)
                            {
                                int effectLevel = Math.max(0, insulationLevel / (INSULATION_TIME / 10) - 1);
                                player.addEffect(new MobEffectInstance(ModEffects.INSULATION, 100, effectLevel, false, false));
                            }
                        }
                    }
                }

                SpreadPath spreadPath = entry.getValue();

                if (spreadPath.isFrozen)
                {
                    continue;
                }

                // Try to spread to new blocks
                if (paths.size() < MAX_PATHS && spreadPath.withinDistance(pos, MAX_DISTANCE))
                {
                    // Get the chunk at this position
                    Pair<Integer, Integer> chunkPos = Pair.of(x >> 4, z >> 4);

                    LevelChunk chunk;

                    if (chunkPos.getFirst().equals(workingCoords.getFirst())
                            && chunkPos.getSecond().equals(workingCoords.getSecond()))
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
                        loadedChunks.put(chunkPos, chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4));
                        workingChunk = chunk;
                    }
                    if (chunk == null) continue;

                    if (!WorldHelper.canSeeSky(chunk, level, spreadPath.getPos()))
                    {
                        for (Direction direction : Direction.values())
                        {
                            SpreadPath tryPath = spreadPath.offset(direction);
                            BlockPos tryPos = tryPath.getPos();

                            if (!paths.containsKey(tryPos) && WorldHelper.canSpreadThrough(chunk, level, spreadPath.getPos(), tryPath.origin))
                            {
                                newPaths.put(tryPos, tryPath);
                            }
                        }
                    }
                }
                spreadPath.isFrozen = true;
            }
            // Add new positions
            paths.putAll(newPaths);

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

        // Input fuel types
        if (this.ticksExisted % 20 == 0)
        {
            this.hasFuelItem = !getItem(0).isEmpty();
        }
        if (this.hasFuelItem)
        {
            ItemStack fuelItem = this.getItem(0);
            int fuel = getItemFuel(fuelItem);
            if (fuel != 0)
            {
                if ((fuel > 0 ? hotFuel : coldFuel) <= MAX_FUEL - Math.abs(fuel) * 0.75)
                {
                    if (fuelItem.hasContainerItem())
                    {
                        if (fuelItem.getCount() == 1)
                        {
                            this.setItem(0, fuelItem.getContainerItem());
                        }
                    }
                    else fuelItem.shrink(1);
                    this.addFuel(fuel);
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

    public void resetPaths()
    {
        Map<BlockPos, SpreadPath> newlist = new HashMap<>();
        newlist.put(pos, new SpreadPath(pos));
        this.replacePaths(newlist);
    }

    public static int getItemFuel(ItemStack item)
    {
        for (List<?> iterator : new ItemSettingsConfig().hearthItems())
        {
            String testItem = (String) iterator.get(0);

            if (new ResourceLocation(testItem).equals(ForgeRegistries.ITEMS.getKey(item.getItem())))
            {
                return ((Number) iterator.get(1)).intValue();
            }
        }
        return 0;
    }

    public int getHotFuel()
    {
        return this.getTileData().getInt("hotFuel");
    }

    public int getColdFuel()
    {
        return this.getTileData().getInt("coldFuel");
    }

    public void setHotFuel(int amount)
    {
        int prevFuel = getHotFuel();

        this.getTileData().putInt("hotFuel", CSMath.clamp(amount, 0, MAX_FUEL));
        this.updateFuelState();

        if (amount == 0 && prevFuel > 0)
            level.playSound(null, pos, ModSounds.HEARTH_FUEL, SoundSource.BLOCKS, 1, (float) Math.random() * 0.2f + 0.9f);

    }

    public void setColdFuel(int amount)
    {
        int prevFuel = getColdFuel();

        this.getTileData().putInt("coldFuel", CSMath.clamp(amount, 0, MAX_FUEL));
        this.updateFuelState();

        if (amount == 0 && prevFuel > 0)
            level.playSound(null, pos, ModSounds.HEARTH_FUEL, SoundSource.BLOCKS, 1, (float) Math.random() * 0.2f + 0.9f);
    }

    public void addFuel(int amount)
    {
        if (amount > 0)
        {
            setHotFuel(getHotFuel() + Math.abs(amount));
        }
        else if (amount < 0)
        {
            setColdFuel(getColdFuel() + Math.abs(amount));
        }
    }

    public void updateFuelState()
    {
        if (level != null && !level.isClientSide)
        {
            BlockState state = level.getBlockState(pos);
            int waterLevel = this.getColdFuel() == 0 ? 0 : (this.getColdFuel() < MAX_FUEL / 2 ? 1 : 2);
            int lavaLevel = this.getHotFuel() == 0 ? 0 : (this.getHotFuel() < MAX_FUEL / 2 ? 1 : 2);

            BlockState desiredState = state.setValue(HearthBottomBlock.WATER, waterLevel).setValue(HearthBottomBlock.LAVA, lavaLevel);
            if (state.getValue(HearthBottomBlock.WATER) != waterLevel || state.getValue(HearthBottomBlock.LAVA) != lavaLevel)
            {
                level.setBlock(pos, desiredState, 3);
            }
            this.setChanged();

            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)),
                    new BlockDataUpdateMessage(pos, List.of("hotFuel", "coldFuel"), List.of(IntTag.valueOf(getHotFuel()), IntTag.valueOf(getColdFuel()))));
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
        this.setColdFuel(tag.getInt("coldFuel"));
        this.setHotFuel(tag.getInt("hotFuel"));
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("coldFuel", this.getColdFuel());
        tag.putInt("hotFuel", this.getHotFuel());
    }

    public void replacePaths(Map<BlockPos, SpreadPath> newPaths)
    {
        this.paths.clear();
        this.addPaths(newPaths);
    }

    public void addPath(SpreadPath path) {
        paths.putIfAbsent(path.getPos(), path);
    }

    public void addPath(BlockPos pos, SpreadPath path) {
        paths.putIfAbsent(pos, path);
    }

    public void addPaths(Map<BlockPos, SpreadPath> newPaths)
    {
        for (Map.Entry<BlockPos, SpreadPath> entry : newPaths.entrySet())
        {
            this.addPath(entry.getKey(), entry.getValue());
        }
    }

    public boolean shouldRebuild()
    {
        return this.getTileData().getBoolean("shouldRebuild");
    }

    public void setShouldRebuild(boolean shouldRebuild)
    {
        this.getTileData().putBoolean("shouldRebuild", shouldRebuild);
    }
}