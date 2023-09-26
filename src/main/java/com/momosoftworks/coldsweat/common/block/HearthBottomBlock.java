package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.common.tileentity.HearthTileEntity;
import com.momosoftworks.coldsweat.core.init.TileEntityInit;
import com.momosoftworks.coldsweat.core.itemgroup.ColdSweatGroup;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.*;
import net.minecraft.loot.LootContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.*;

public class HearthBottomBlock extends Block
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty WATER = IntegerProperty.create("water", 0, 2);
    public static final IntegerProperty LAVA = IntegerProperty.create("lava", 0, 2);

    private static final Map<Direction, VoxelShape> SHAPES = new HashMap<>();

    public static Properties getProperties()
    {
        return Properties
                .of(Material.STONE)
                .sound(SoundType.STONE)
                .strength(2)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .dynamicShape()
                .lightLevel(state -> state.getValue(LAVA) * 3);
    }

    public static Item.Properties getItemProperties()
    {   return new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1);
    }

    public HearthBottomBlock(Block.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(WATER, 0).setValue(LAVA, 0));
        calculateFacingShapes(VoxelShapes.or(
            Block.box(3, 0, 3.5, 13, 18, 12.5), // Shell
            Block.box(4, 18, 5, 9, 27, 10), // Exhaust
            Block.box(-1, 3, 6, 17, 11, 10))); // Canisters
    }

    static void calculateFacingShapes(VoxelShape shape)
    {
        for (Direction direction : Direction.values())
        {   SHAPES.put(direction, CSMath.rotateShape(direction, shape));
        }
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader level, BlockPos pos)
    {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader getter, BlockPos pos, ISelectionContext context)
    {   return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {   return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {   return TileEntityInit.HEARTH_BLOCK_ENTITY_TYPE.get().create();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult)
    {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof HearthTileEntity)
        {
            HearthTileEntity hearth = (HearthTileEntity) te;
            ItemStack stack = player.getItemInHand(hand);

            // If the held item is a bucket, try to extract fluids
            if (player.getItemInHand(hand).getItem() == Items.BUCKET)
            {
                Vector3d clickedPos = rayTraceResult.getLocation();

                Vector3i lavaSideOffset = state.getValue(FACING).getClockWise().getNormal();
                Vector3d lavaSidePos = CSMath.getCenterPos(pos).add(lavaSideOffset.getX() * 0.65, lavaSideOffset.getY() * 0.65, lavaSideOffset.getZ() * 0.65);

                Vector3i waterSideOffset = state.getValue(FACING).getCounterClockWise().getNormal();
                Vector3d waterSidePos = CSMath.getCenterPos(pos).add(waterSideOffset.getX() * 0.65, waterSideOffset.getY() * 0.65, waterSideOffset.getZ() * 0.65);

                boolean isLava = clickedPos.distanceTo(lavaSidePos) < clickedPos.distanceTo(waterSidePos);
                Vector3d sidePos = isLava ? lavaSidePos : waterSidePos;
                BucketItem filledBucket = isLava ? ((BucketItem) Items.LAVA_BUCKET)
                                                 : ((BucketItem) Items.WATER_BUCKET);
                int itemFuel = Math.abs(HearthTileEntity.getItemFuel(filledBucket.getDefaultInstance()));
                int hearthFuel = isLava ? hearth.getHotFuel() : hearth.getColdFuel();

                if (hearthFuel >= itemFuel * 0.99)
                {
                    if (rayTraceResult.getLocation().distanceTo(sidePos) < 0.4)
                    {
                        if (itemFuel > 0)
                        {
                            // Remove fuel
                            if (isLava) hearth.setHotFuelAndUpdate(hearthFuel - itemFuel);
                            else        hearth.setColdFuelAndUpdate(hearthFuel - itemFuel);
                            // Give filled bucket item
                            stack.shrink(1);
                            player.addItem(filledBucket.getDefaultInstance());
                            // Play bucket sound
                            world.playSound(null, pos, filledBucket.getFluid().getAttributes().getFillSound(), SoundCategory.BLOCKS, 1.0F, 0.9f + new Random().nextFloat() * 0.2F);

                            return ActionResultType.SUCCESS;
                        }
                    }
                }
                // Open the GUI
                if (!world.isClientSide)
                    NetworkHooks.openGui((ServerPlayerEntity) player, hearth, pos);
            }
            else
            {
                // If the held item is fuel, try to insert the fuel
                int itemFuel = HearthTileEntity.getItemFuel(stack);
                int hearthFuel = itemFuel > 0 ? hearth.getHotFuel() : hearth.getColdFuel();

                if (itemFuel != 0 && hearthFuel + Math.abs(itemFuel) * 0.75 < HearthTileEntity.MAX_FUEL)
                {
                    // Consume the item if not in creative
                    if (!player.isCreative())
                    {
                        if (stack.hasContainerItem())
                        {   ItemStack container = stack.getContainerItem();
                            player.setItemInHand(hand, container);
                        }
                        else
                        {   stack.shrink(1);
                        }
                    }
                    // Add the fuel
                    hearth.addFuel(itemFuel);

                    // Play the fuel filling sound
                    world.playSound(null, pos, itemFuel > 0 ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY,
                            SoundCategory.BLOCKS, 1.0F, 0.9f + new Random().nextFloat() * 0.2F);
                }
                // Open the GUI
                else if (!world.isClientSide)
                {   NetworkHooks.openGui((ServerPlayerEntity) player, hearth, pos);
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState lastState, boolean p_60570_)
    {
        if (world.getBlockState(pos.above()).isAir())
        {   world.setBlock(pos.above(), ModBlocks.HEARTH_TOP.defaultBlockState().setValue(HearthTopBlock.FACING, state.getValue(FACING)), 2);
        }
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
    {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
        if (world.getBlockState(pos.above()).getBlock() != ModBlocks.HEARTH_TOP)
        {   this.destroy(world, pos, state);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
    {   List<ItemStack> drops = super.getDrops(state, builder);
        if (!drops.isEmpty())
            return drops;
        drops.add(new ItemStack(this, 1));
        return drops;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            if (world.getBlockState(pos.above()).getBlock() == ModBlocks.HEARTH_TOP)
            {   world.destroyBlock(pos.above(), false);
            }

            TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof HearthTileEntity)
            {   InventoryHelper.dropContents(world, pos, (HearthTileEntity) tileentity);
                world.updateNeighborsAt(pos, this);
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation direction)
    {   return state.setValue(FACING, direction.rotate(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {   builder.add(FACING, WATER, LAVA);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        World world = context.getLevel();
        return world.getBlockState(context.getClickedPos().above()).isAir()
                ? this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATER, 0).setValue(LAVA, 0)
                : null;
    }
}
