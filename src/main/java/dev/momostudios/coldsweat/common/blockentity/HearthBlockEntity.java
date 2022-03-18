package dev.momostudios.coldsweat.common.blockentity;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.container.HearthContainer;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.core.init.BlockEntityInit;
import dev.momostudios.coldsweat.core.init.BlockInit;
import dev.momostudios.coldsweat.core.init.ParticleTypesInit;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.HearthFuelSyncMessage;
import dev.momostudios.coldsweat.util.entity.TempHelper;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.common.block.HearthBlock;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class HearthBlockEntity extends RandomizableContainerBlockEntity implements MenuProvider, ISpreadingArea
{
    public static int slots = 1;
    protected NonNullList<ItemStack> items = NonNullList.withSize(slots, ItemStack.EMPTY);
    BlockPos pos = this.getBlockPos();

    public int ticksExisted;

    public static final int MAX_FUEL = 1000;

    public HearthBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockEntityInit.HEARTH_TILE_ENTITY_TYPE.get(), pos, state);
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
        // Set to 'true' if a player in the area needs insulation
        boolean shouldUseHotFuel = false;
        boolean shouldUseColdFuel = false;

        this.ticksExisted = (this.ticksExisted + 1) % 1000;

        // Gradually increases insulation amount
        int insulationLevel = this.getTileData().getInt("insulationLevel");
        if (insulationLevel < 2400)
            this.getTileData().putInt("insulationLevel", insulationLevel + 1);

        if (level != null && (this.getHotFuel() > 0 || this.getColdFuel() > 0))
        {
            List<Player> affectedPlayers = new ArrayList<>();

            // Represents the NBT list
            ConcurrentHashMap<BlockPos, SpreadPath> pathList = this.getPathMap();

            if (pathList.isEmpty())
            {
                this.addPath(new SpreadPath(this.getBlockPos(), Direction.UP));
            }

            if (pathList.size() > 0)
            {
                // Create temporary list to add back to the NBT
                ConcurrentHashMap<BlockPos, SpreadPath> newPaths = new ConcurrentHashMap<>();

                /*
                 Partition all points into multiple lists (max of 19)
                */
                int index = 0;
                // Size of each partition
                int partSize = 350;
                // Number of partitions
                int partitionCount = (int) Math.ceil(pathList.size() / (double) partSize);
                // Index of the last point being worked on this tick
                int lastIndex = Math.min(pathList.size(), partSize * (this.ticksExisted % partitionCount + 1) + 1);
                // Index of the first point being worked on this tick
                int firstIndex = Math.max(0, lastIndex - partSize);

                // Iterates over the specified partition of the list of BlockPos
                for (Map.Entry<BlockPos, SpreadPath> entry : pathList.entrySet())
                {
                    // Stop after we reach the maximum number of iterations for this partition
                    if (index < firstIndex || index - firstIndex >= partSize)
                    {
                        index++;
                        continue;
                    }

                    // Reset every 4 seconds
                    if (this.ticksExisted % 80 == 0)
                    {
                        this.resetPaths();
                        break;
                    }

                    SpreadPath spreadPath = entry.getValue();

                    // Create detection box for PlayerEntities
                    int x = spreadPath.getX();
                    int y = spreadPath.getY();
                    int z = spreadPath.getZ();
                    AABB aabb = new AABB(x, y, z, x + 1, y + 1, z + 1);

                    // Add players to affectedPlayers
                    for(Player player : level.players())
                    {
                        if (aabb.intersects(player.getBoundingBox()))
                        {
                            affectedPlayers.add(player);
                        }
                    }

                    index++;
                    if (spreadPath.isFrozen) continue;

                    // Check in all 6 directions
                    for (Direction direction : Direction.values())
                    {
                        if (pathList.size() < 6000)
                        {
                            // Create new BlockPos with an offset of [direction] from [blockPos]
                            SpreadPath testpos = spreadPath.offset(direction);

                            if (testpos.withinDistance(pos, 20))
                            {
                                if (!newPaths.containsKey(testpos.getPos()) && !pathList.containsKey(testpos.getPos())
                                        && !WorldHelper.canSeeSky(level, testpos.getPos())
                                        && WorldHelper.canSpreadThrough(level, entry.getKey(), direction))
                                {
                                    newPaths.put(testpos.getPos(), testpos);
                                }
                            }
                        }
                        else
                        {
                            this.resetPaths();
                        }
                    }
                    spreadPath.isFrozen = true;
                }
                // Add new positions
                this.addPaths(newPaths);

                if (!level.isClientSide && !affectedPlayers.isEmpty())
                {
                    ConfigCache config = ConfigCache.getInstance();

                    for (Player player : affectedPlayers)
                    {
                        // Get the player's temperature
                        double temp = player.hasEffect(ModEffects.INSULATION) ? player.getPersistentData().getDouble("preHearthTemp") :
                                TempHelper.getTemperature(player, Temperature.Types.WORLD).get();

                        if ((temp < config.minTemp && this.getHotFuel() > 0))
                        {
                            // Tell the hearth to use hot fuel
                            shouldUseHotFuel = true;
                        }
                        if (temp > config.maxTemp && this.getColdFuel() > 0)
                        {
                            // Tell the hearth to use cold fuel
                            shouldUseColdFuel = true;
                        }

                        if (shouldUseHotFuel || shouldUseColdFuel)
                        {
                            int effectLevel = Math.max(0, insulationLevel / 240 - 1);
                            MobEffectInstance effect = player.getEffect(ModEffects.INSULATION);

                            if (effect == null || effect.getDuration() < 90 || effect.getAmplifier() != effectLevel)
                            {
                                player.addEffect(new MobEffectInstance(ModEffects.INSULATION, 100, effectLevel, false, false));
                            }
                        }
                    }
                }
            }
        }

        // Input fuel types
        if (getItemFuel(this.getItemInSlot(0)) != 0)
        {
            ItemStack fuel = this.getItemInSlot(0);
            int amount = getItemFuel(this.getItemInSlot(0));
            if ((amount > 0 ? this.getHotFuel() : this.getColdFuel()) <= MAX_FUEL - Math.abs(amount) * 0.75)
            {
                if (fuel.hasContainerItem() && fuel.getCount() == 1)
                {
                    this.setItemInSlot(0, fuel.getContainerItem());
                }
                else this.getItemInSlot(0).shrink(1);
                this.addFuel(amount);
            }
        }

        // Drain fuel
        if (!level.isClientSide && this.ticksExisted % 80 == 0)
        {
            if (this.getColdFuel() > 0 && shouldUseColdFuel)
            {
                this.setColdFuel(this.getColdFuel() - 1);
            }
            if (this.getHotFuel() > 0 && shouldUseHotFuel)
            {
                this.setHotFuel(this.getHotFuel() - 1);
            }
        }

        // Update fuel for clients
        if (!level.isClientSide && this.ticksExisted % 40 == 0)
        {
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new HearthFuelSyncMessage(pos, this.getHotFuel(), this.getColdFuel()));
        }

        // Update BlockState
        if (this.ticksExisted % 5 == 0 && level.getBlockState(pos).getBlock() == BlockInit.HEARTH.get())
        {
            this.updateFuelState();
        }

        // Particles
        int coldFuel = this.getColdFuel();
        int hotFuel = this.getHotFuel();

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

        // Air Particles
        if (level.isClientSide && this.getTileData().getBoolean("showRadius"))
        {
            // Spawn particles if enabled
            for (BlockPos blockPos : this.getPathMap().keySet())
            {
                if (Minecraft.getInstance().options.renderDebug)
                {
                    if (this.ticksExisted % 5 == 0)
                        level.addParticle(ParticleTypes.FLAME, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 0, 0, 0);
                }
                else if (Math.random() < 0.016)
                {
                    double xr = Math.random();
                    double yr = Math.random();
                    double zr = Math.random();
                    double xm = Math.random() / 20 - 0.025;
                    double zm = Math.random() / 20 - 0.025;

                    level.addParticle(ParticleTypesInit.HEARTH_AIR.get(), blockPos.getX() + xr, blockPos.getY() + yr, blockPos.getZ() + zr, xm, 0, zm);
                }
            }
        }
    }

    public void resetPaths()
    {
        ConcurrentHashMap<BlockPos, SpreadPath> map = new ConcurrentHashMap<>();
        map.put(pos, new SpreadPath(pos));
        this.setPathMap(map);
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

    public ItemStack getItemInSlot(int index)
    {
        AtomicReference<ItemStack> stack = new AtomicReference<>(ItemStack.EMPTY);
        this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability ->
        {
            stack.set(capability.getStackInSlot(index));
        });
        return stack.get();
    }

    public void setItemInSlot(int index, ItemStack stack)
    {
        this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability ->
        {
            if (stack != null && stack != capability.getStackInSlot(index))
            {
                capability.extractItem(index, capability.getStackInSlot(index).getCount(), false);
            }
            capability.insertItem(index, stack, false);
        });
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

        this.getTileData().putInt("hotFuel", Math.min(amount, MAX_FUEL));

        if (amount == 0 && prevFuel > 0)
            level.playSound(null, pos, ModSounds.HEARTH_FUEL, SoundSource.BLOCKS, 1, (float) Math.random() * 0.2f + 0.9f);
    }

    public void setColdFuel(int amount)
    {
        int prevFuel = getColdFuel();

        this.getTileData().putInt("coldFuel", Math.min(amount, MAX_FUEL));

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
        BlockState state = level.getBlockState(pos);
        int waterLevel = this.getColdFuel() == 0 ? 0 : (this.getColdFuel() < MAX_FUEL / 2 ? 1 : 2);
        int lavaLevel = this.getHotFuel() == 0 ? 0 : (this.getHotFuel() < MAX_FUEL / 2 ? 1 : 2);

        BlockState desiredState = state.setValue(HearthBlock.WATER, waterLevel).setValue(HearthBlock.LAVA, lavaLevel);
        if (state.getValue(HearthBlock.WATER) != waterLevel || state.getValue(HearthBlock.LAVA) != lavaLevel)
        {
            level.setBlock(pos, desiredState, 3);
        }
    }

    @Override
    public int getContainerSize()
    {
        return slots;
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

    ConcurrentHashMap<BlockPos, SpreadPath> paths = new ConcurrentHashMap<>();

    @Nonnull
    @Override
    public ConcurrentHashMap<BlockPos, SpreadPath> getPathMap()
    {
        return paths;
    }

    @Override
    public void setPathMap(ConcurrentHashMap<BlockPos, SpreadPath> map)
    {
        this.paths.clear();
        this.paths.putAll(map);
    }

    @Override
    public void addPath(SpreadPath pos) {
        paths.put(pos.getPos(), pos);
    }

    @Override
    public void addPaths(ConcurrentHashMap<BlockPos, SpreadPath> map)
    {
        paths.putAll(map);
    }

    @Override
    public void remove(SpreadPath pos) {
        paths.remove(pos.getPos());
    }

    @Override
    public void clear() {
        paths.clear();
    }
}