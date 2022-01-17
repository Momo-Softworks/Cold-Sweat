package net.momostudios.coldsweat.common.te;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.block.HearthBlock;
import net.momostudios.coldsweat.common.container.HearthContainer;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.config.ItemSettingsConfig;
import net.momostudios.coldsweat.core.capabilities.HearthRadiusCapability;
import net.momostudios.coldsweat.core.capabilities.IBlockStorageCap;
import net.momostudios.coldsweat.core.init.BlockInit;
import net.momostudios.coldsweat.core.init.ParticleTypesInit;
import net.momostudios.coldsweat.core.init.TileEntityInit;
import net.momostudios.coldsweat.util.*;
import net.momostudios.coldsweat.util.registrylists.ModEffects;
import net.momostudios.coldsweat.util.registrylists.ModSounds;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class HearthTileEntity extends LockableLootTileEntity implements ITickableTileEntity
{
    public static int slots = 10;
    protected NonNullList<ItemStack> items = NonNullList.withSize(slots, ItemStack.EMPTY);

    // List of all hearths in loaded chunks
    private final Map<BlockPos, LazyOptional<IBlockStorageCap>> cache = new HashMap<>();

    public int ticksExisted;

    // Set to 'true' if a player in the area needs insulation
    private boolean shouldUseHotFuel = false;
    private boolean shouldUseColdFuel = false;

    protected static int MAX_FUEL = 1000;

    public HearthTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container." + ColdSweat.MOD_ID + ".hearth");
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

    public HearthTileEntity()
    {
        this(TileEntityInit.HEARTH_TILE_ENTITY_TYPE.get());
    }

    public void tick()
    {
        // Add this tile entity's capability to the list of hearths
        LazyOptional<IBlockStorageCap> cap = cache.get(pos);
        if (cap == null)
        {
            ICapabilityProvider provider = world.getTileEntity(pos);
            cap = provider.getCapability(HearthRadiusCapability.HEARTH_BLOCKS);
            cache.put(pos, cap);
            cap.addListener(self -> cache.put(pos, self));
        }
        if (cap.orElse(new HearthRadiusCapability()).getMap().isEmpty())
        {
            cap.ifPresent(cap2 -> cap2.set(new SpreadPath(pos)));
        }

        this.ticksExisted = (this.ticksExisted + 1) % 1000;

        // Gradually increases insulation amount
        int insulationLevel = this.getTileData().getInt("insulationLevel");
        if (insulationLevel < 2400)
            this.getTileData().putInt("insulationLevel", insulationLevel + 1);

        if ((this.getHotFuel() > 0 || this.getColdFuel() > 0))
        {
            List<PlayerEntity> affectedPlayers = new ArrayList<>();

            // Represents the NBT list
            LinkedHashMap<BlockPos, SpreadPath> pathList = cap.orElse(new HearthRadiusCapability()).getMap();

            if (pathList.size() > 0)
            {
                // Create temporary list to add back to the NBT
                LinkedHashMap<BlockPos, SpreadPath> newPaths = new LinkedHashMap<>();

                /*
                 Partition all points into multiple lists (max of 19)
                */
                int index = 0;
                // Size of each partition
                int partSize = 300;
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
                        reset();
                        break;
                    }

                    SpreadPath spreadPath = entry.getValue();

                    // Create detection box for PlayerEntities
                    int x = spreadPath.getX();
                    int y = spreadPath.getY();
                    int z = spreadPath.getZ();
                    AxisAlignedBB aabb = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);

                    // Add players to affectedPlayers
                    for (PlayerEntity player : world.getPlayers())
                    {
                        if (aabb.contains(player.getPosX(), player.getPosY(), player.getPosZ()))
                            affectedPlayers.add(player);
                    }

                    // Check in all 6 directions
                    for (Direction direction : Direction.values())
                    {
                        if (pathList.size() < 4000)
                        {
                            // Create new BlockPos with an offset of [direction] from [blockPos]
                            SpreadPath testpos = spreadPath.offset(direction);

                            if (testpos.withinDistance(pos, 20))
                            {
                                if (!newPaths.containsKey(testpos.getPos()) && !pathList.containsKey(testpos.getPos())
                                && !WorldInfo.canSeeSky(world, testpos.getPos())
                                && WorldInfo.canSpreadThrough(world, spreadPath, direction, spreadPath.origin))
                                {
                                    newPaths.put(testpos.getPos(), testpos);
                                }
                            }
                        }
                        else
                        {
                            reset();
                        }
                    }
                    index++;
                }
                // Add new positions
                cap.ifPresent(cap2 -> cap2.addPaths(newPaths));

                if (world != null && !world.isRemote && !affectedPlayers.isEmpty())
                {
                    shouldUseHotFuel = false;
                    shouldUseColdFuel = false;

                    ConfigCache config = ConfigCache.getInstance();

                    for (PlayerEntity player : affectedPlayers)
                    {
                        // Get the player's temperature
                        double temp = player.isPotionActive(ModEffects.INSULATION) ? player.getPersistentData().getDouble("preHearthTemp") :
                                PlayerTemp.getTemperature(player, PlayerTemp.Types.AMBIENT).get();

                        if ((temp < config.minTemp && this.getHotFuel() > 0))
                        {
                            // If the player doesn't have the effect or the time left is less than 90 ticks, add the effect
                            if (!player.isPotionActive(ModEffects.INSULATION) || player.getActivePotionEffect(ModEffects.INSULATION).getDuration() < 90)
                                player.addPotionEffect(new EffectInstance(ModEffects.INSULATION, 100, Math.max(0, insulationLevel / 240 - 1), false, false));

                            // Tell the hearth to use hot fuel
                            shouldUseHotFuel = true;
                        }
                        if (temp > config.maxTemp && this.getColdFuel() > 0)
                        {
                            // If the player doesn't have the effect or the time left is less than 90 ticks, add the effect
                            if (!player.isPotionActive(ModEffects.INSULATION) || player.getActivePotionEffect(ModEffects.INSULATION).getDuration() < 90)
                                player.addPotionEffect(new EffectInstance(ModEffects.INSULATION, 100, Math.max(0, insulationLevel / 240 - 1), false, false));

                            // Tell the hearth to use cold fuel
                            shouldUseColdFuel = true;
                        }
                    }
                }
            }
        }

        // Input fuel types
        if (!getFuelItem(this.getItemInSlot(0)).isEmpty())
        {
            ItemStack fuel = this.getItemInSlot(0);
            int amount = (int) getFuelItem(this.getItemInSlot(0)).get(1);
            if (this.getHotFuel() <= MAX_FUEL - amount * 0.75 && amount > 0)
            {
                if (fuel.hasContainerItem() && fuel.getCount() == 1)
                {
                    this.setItemInSlot(0, fuel.getContainerItem());
                }
                else this.getItemInSlot(0).shrink(1);
                this.setHotFuel(this.getHotFuel() + amount);
            }

            if (this.getColdFuel() <= MAX_FUEL + amount * 0.75 && amount < 0)
            {
                if (fuel.hasContainerItem() && fuel.getCount() == 1)
                {
                    this.setItemInSlot(0, fuel.getContainerItem());
                }
                else this.getItemInSlot(0).shrink(1);
                this.setColdFuel(this.getColdFuel() - amount);
            }
        }

        // Drain fuel
        if (this.ticksExisted % 40 == 0)
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

        // Update BlockState
        if (this.ticksExisted % 5 == 0 && world.getBlockState(pos).getBlock() == BlockInit.HEARTH.get())
        {
            BlockState state = world.getBlockState(pos);
            int waterLevel = this.getColdFuel() == 0 ? 0 : (this.getColdFuel() < MAX_FUEL / 2 ? 1 : 2);
            int lavaLevel = this.getHotFuel() == 0 ? 0 : (this.getHotFuel() < MAX_FUEL / 2 ? 1 : 2);

            BlockState desiredState = state.with(HearthBlock.WATER, waterLevel).with(HearthBlock.LAVA, lavaLevel);
            if (state.get(HearthBlock.WATER) != waterLevel || state.get(HearthBlock.LAVA) != lavaLevel)
            {
                world.setBlockState(pos, desiredState);
            }
        }

        // Particles
        int coldFuel = getColdFuel();
        int hotFuel = getHotFuel();

        if (Math.random() < coldFuel / 3000d)
        {
            double d0 = pos.getX() + 0.5d;
            double d1 = pos.getY() + 2d;
            double d2 = pos.getZ() + 0.5d;
            double d3 = (Math.random() - 0.5) / 4;
            double d4 = (Math.random() - 0.5) / 4;
            double d5 = (Math.random() - 0.5) / 4;
            world.addParticle(ParticleTypesInit.STEAM.get(), d0 + d3, d1 + d4, d2 + d5, 0.0D, 0.04D, 0.0D);
        }
        if (Math.random() < hotFuel / 2000d)
        {
            double d0 = pos.getX() + 0.5d;
            double d1 = pos.getY() + 2d;
            double d2 = pos.getZ() + 0.5d;
            double d3 = (Math.random() - 0.5) / 2;
            double d4 = (Math.random() - 0.5) / 2;
            double d5 = (Math.random() - 0.5) / 2;
            world.addParticle(ParticleTypes.LARGE_SMOKE, d0 + d3, d1 + d4, d2 + d5, 0.0D, 0.0D, 0.0D);
        }

        // Air Particles
        if (world.isRemote && world.getTileEntity(pos).getTileData().getBoolean("showRadius"))
        cap.ifPresent(c ->
        {
            // Spawn particles if enabled
            for (BlockPos blockPos : c.getMap().keySet())
            {
                if (Minecraft.getInstance().gameSettings.showDebugInfo)
                {
                    if (this.ticksExisted % 5 == 0)
                        world.addParticle(ParticleTypes.FLAME, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 0, 0, 0);
                }
                else if (Math.random() < 0.016)
                {
                    double xr = Math.random();
                    double yr = Math.random();
                    double zr = Math.random();
                    double xm = Math.random() / 20 - 0.025;
                    double zm = Math.random() / 20 - 0.025;

                    world.addParticle(ParticleTypesInit.HEARTH_AIR.get(), blockPos.getX() + xr, blockPos.getY() + yr, blockPos.getZ() + zr, xm, 0, zm);
                }
            }
        });
    }

    private void reset()
    {
        LinkedHashMap<BlockPos, SpreadPath> map = new LinkedHashMap<>();
        map.put(pos, new SpreadPath(pos));
        getCapability(HearthRadiusCapability.HEARTH_BLOCKS).ifPresent(cap2 -> cap2.setPaths(map));
    }

    public List getFuelItem(ItemStack item)
    {
        List returnList = Arrays.asList(item, 0);
        for (List<String> iterator : new ItemSettingsConfig().hearthItems())
        {
            String testItem = iterator.get(0);

            if (new ResourceLocation(testItem).equals(ForgeRegistries.ITEMS.getKey(item.getItem())))
            {
                returnList = Arrays.asList(item, Integer.parseInt(iterator.get(1)));
            }
        }
        return returnList;
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
        return this.getTileData().getInt("hot_fuel");
    }

    public int getColdFuel()
    {
        return this.getTileData().getInt("cold_fuel");
    }

    public void setHotFuel(int amount)
    {
        int prevFuel = getHotFuel();

        this.getTileData().putInt("hot_fuel", Math.min(amount, MAX_FUEL));

        if (amount == 0 && prevFuel > 0)
            world.playSound(null, pos, ModSounds.HEARTH_FUEL, SoundCategory.BLOCKS, 1, (float) Math.random() * 0.2f + 0.9f);
    }

    public void setColdFuel(int amount)
    {
        int prevFuel = getColdFuel();

        this.getTileData().putInt("cold_fuel", Math.min(amount, MAX_FUEL));

        if (amount == 0 && prevFuel > 0)
            world.playSound(null, pos, ModSounds.HEARTH_FUEL, SoundCategory.BLOCKS, 1, (float) Math.random() * 0.2f + 0.9f);
    }

    @Override
    public int getSizeInventory() {
        return slots;
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player)
    {
        return new HearthContainer(id, player, this);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        this.items = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        if (!this.checkLootAndRead(nbt))
        {
            ItemStackHelper.loadAllItems(nbt, this.items);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        super.write(compound);
        if (!this.checkLootAndWrite(compound))
        {
            ItemStackHelper.saveAllItems(compound, items);
        }
        return compound;
    }
}
