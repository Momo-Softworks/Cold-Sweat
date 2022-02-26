package dev.momostudios.coldsweat.common.block;

import dev.momostudios.coldsweat.common.te.HearthBlockEntity;
import dev.momostudios.coldsweat.core.init.BlockInit;
import dev.momostudios.coldsweat.core.init.BlockEntityInit;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.HearthFuelSyncMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

public class HearthBlock extends Block
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty WATER = IntegerProperty.create("water", 0, 2);
    public static final IntegerProperty LAVA = IntegerProperty.create("lava", 0, 2);

    private static final Map<Direction, VoxelShape> SHAPES = new HashMap<Direction, VoxelShape>();

    public static Properties getProperties()
    {
        return Properties
                .of(Material.STONE)
                .sound(SoundType.STONE)
                .destroyTime(2.0F)
                .explosionResistance(2.0F)
                .dynamicShape()
                .lightLevel(s -> 0);
    }

    public static Item.Properties getItemProperties()
    {
        return new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1);
    }

    public HearthBlock(Properties properties)
    {
        super(HearthBlock.getProperties());
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(WATER, 0).setValue(LAVA, 0));
        runCalculation(Shapes.or(
            Block.box(3, 1, 4, 13, 19, 12), // Shell
            Block.box(8, 19, 6, 12, 31, 10), // Exhaust 1
            Block.box(6, 27.5, 5.5, 8, 31.5, 10.5), // Exhaust 2
            Block.box(13, 5, 6, 16, 13, 10), // Water Canister
            Block.box(0, 5, 6, 3, 13, 10))); // Lava Canister)
    }

    static void calculateShapes(Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[] { shape, Shapes.empty() };

        int times = (to.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1],
                Shapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }

        SHAPES.put(to, buffer[0]);
    }

    static void runCalculation(VoxelShape shape) {
        for (Direction direction : Direction.values()) {
            calculateShapes(direction, shape);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader reader, BlockPos pos)
    {
        return reader.getBlockState(pos).isAir() && reader.getBlockState(pos.above()).isAir();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context)
    {
        return SHAPES.get(state.getValue(FACING));
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        if (!worldIn.isClientSide)
        {
            if (worldIn.getBlockEntity(pos) instanceof HearthBlockEntity te)
            {
                ItemStack stack = player.getItemInHand(hand);
                int itemFuel = te.getItemFuel(stack);
                int hearthFuel = itemFuel > 0 ? te.getHotFuel() : te.getColdFuel();

                if (itemFuel != 0 && hearthFuel + Math.abs(itemFuel) * 0.75 < HearthBlockEntity.MAX_FUEL)
                {
                    if (!player.isCreative())
                    {
                        if (stack.hasContainerItem())
                        {
                            ItemStack container = stack.getContainerItem();
                            stack.shrink(1);
                            player.getInventory().add(container);
                        }
                        else
                        {
                            stack.shrink(1);
                        }
                    }
                    te.addFuel(itemFuel);
                    te.updateFuelState();

                    worldIn.playSound(null, pos, itemFuel > 0 ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY,
                            SoundSource.BLOCKS, 1.0F, 0.9f + new Random().nextFloat() * 0.2F);

                    ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                            new HearthFuelSyncMessage(te.getBlockPos(), te.getHotFuel(), te.getColdFuel()));
                }
                else
                {
                    NetworkHooks.openGui((ServerPlayer) player, te, pos);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving)
    {
        if (worldIn.isAirBlock(pos.up()))
        {
            worldIn.setBlockState(pos.up(), BlockInit.HEARTH_TOP.get().getDefaultState().with(HearthTopBlock.FACING, state.get(FACING)), 2);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
    {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        if (worldIn.getBlockState(pos.up()).getBlock() != BlockInit.HEARTH_TOP.get())
        {
            worldIn.destroyBlock(pos, false);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        return tileEntity instanceof INamedContainerProvider ? (INamedContainerProvider) tileEntity : null;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return BlockEntityInit.HEARTH_TILE_ENTITY_TYPE.get().create();
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty())
            return dropsOriginal;
        return Collections.singletonList(new ItemStack(this, 1));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (!state.matchesBlock(newState.getBlock()))
        {
            if (world.getBlockState(pos.up()).getBlock() == BlockInit.HEARTH_TOP.get())
            {
                world.destroyBlock(pos.up(), false);
            }

            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof HearthBlockEntity) {
                InventoryHelper.dropInventoryItems(world, pos, (HearthBlockEntity) tileentity);
                world.updateComparatorOutputLevel(pos, this);
            }
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation direction)
    {
        return state.with(FACING, direction.rotate(state.get(FACING)));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATER, LAVA);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing()).with(WATER, 0).with(LAVA, 0);
    }
}
