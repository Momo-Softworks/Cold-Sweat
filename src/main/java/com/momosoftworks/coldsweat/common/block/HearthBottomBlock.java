package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;

import com.momosoftworks.coldsweat.core.init.ModBlockEntities;
import com.momosoftworks.coldsweat.core.init.ModBlocks;
import com.momosoftworks.coldsweat.util.item.ItemStackHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.*;

public class HearthBottomBlock extends Block implements EntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty SIDE_POWERED = BooleanProperty.create("side_powered");
    public static final BooleanProperty BACK_POWERED = BooleanProperty.create("back_powered");

    public static Properties getProperties()
    {
        return Properties
                .of()
                .sound(SoundType.STONE)
                .destroyTime(2.0F)
                .explosionResistance(10.0F)
                .requiresCorrectToolForDrops();
    }

    public static Item.Properties getItemProperties()
    {   return new Item.Properties().stacksTo(1);
    }

    public HearthBottomBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH)
                                                          .setValue(SIDE_POWERED, false)
                                                          .setValue(BACK_POWERED, false));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
    {
        return true;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {   return type == ModBlockEntities.HEARTH.value() ? HearthBlockEntity::tickSelf : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {   return new HearthBlockEntity(pos, state);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult rayTraceResult)
    {
        if (level.getBlockEntity(pos) instanceof HearthBlockEntity te)
        {   player.openMenu(te, pos);
        }
        return super.useWithoutItem(state, level, pos, player, rayTraceResult);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        if (worldIn.getBlockEntity(pos) instanceof HearthBlockEntity te)
        {
            // If the held item is a bucket, try to extract fluids
            if (player.getItemInHand(hand).getItem() == Items.BUCKET)
            {
                Vec3 clickedPos = rayTraceResult.getLocation();

                Vec3i lavaSideOffset = state.getValue(FACING).getClockWise().getNormal();
                Vec3 lavaSidePos = CSMath.getCenterPos(pos).add(lavaSideOffset.getX() * 0.65, lavaSideOffset.getY() * 0.65, lavaSideOffset.getZ() * 0.65);

                Vec3i waterSideOffset = state.getValue(FACING).getCounterClockWise().getNormal();
                Vec3 waterSidePos = CSMath.getCenterPos(pos).add(waterSideOffset.getX() * 0.65, waterSideOffset.getY() * 0.65, waterSideOffset.getZ() * 0.65);

                boolean isLava = clickedPos.distanceTo(lavaSidePos) < clickedPos.distanceTo(waterSidePos);
                Vec3 sidePos = isLava ? lavaSidePos : waterSidePos;
                BucketItem filledBucket = isLava ? ((BucketItem) Items.LAVA_BUCKET)
                                                 : ((BucketItem) Items.WATER_BUCKET);
                int itemFuel = Math.abs(te.getItemFuel(filledBucket.getDefaultInstance()));
                int hearthFuel = isLava ? te.getHotFuel() : te.getColdFuel();

                if (hearthFuel >= itemFuel * 0.99)
                {
                    if (rayTraceResult.getLocation().distanceTo(sidePos) < 0.4)
                    {
                        if (itemFuel > 0)
                        {
                            // Remove fuel
                            if (isLava) te.setHotFuelAndUpdate(hearthFuel - itemFuel);
                            else te.setColdFuelAndUpdate(hearthFuel - itemFuel);
                            // Give filled bucket item
                            stack.shrink(1);
                            player.addItem(filledBucket.getDefaultInstance());
                            // Play bucket sound
                            ItemStackHelper.playBucketEmptySound(stack, player, worldIn, pos);

                            return ItemInteractionResult.SUCCESS;
                        }
                    }
                }
                player.openMenu(te, pos);
            }
            else
            {
                // If the held item is fuel, try to insert the fuel
                int itemFuel = te.getItemFuel(stack);
                int hearthFuel = itemFuel > 0 ? te.getHotFuel() : te.getColdFuel();

                if (itemFuel != 0 && hearthFuel + Math.abs(itemFuel) * 0.75 < te.getMaxFuel())
                {
                    // Consume the item if not in creative
                    if (!player.isCreative())
                    {
                        if (stack.hasCraftingRemainingItem())
                        {
                            ItemStack container = stack.getCraftingRemainingItem();
                            player.setItemInHand(hand, container);
                        }
                        else
                        {   stack.shrink(1);
                        }
                    }
                    // Add the fuel
                    te.addFuel(itemFuel);

                    // Play the fuel filling sound
                    worldIn.playSound(null, pos, itemFuel > 0
                                                 ? SoundEvents.BUCKET_EMPTY_LAVA
                                                 : SoundEvents.BUCKET_EMPTY,
                                      SoundSource.BLOCKS, 1.0F, 0.9f + new Random().nextFloat() * 0.2F);
                }
                else player.openMenu(te, pos);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState lastState, boolean p_60570_)
    {
        if (level.getBlockState(pos.above()).isAir())
        {   level.setBlock(pos.above(), ModBlocks.HEARTH_TOP.value().defaultBlockState().setValue(HearthTopBlock.FACING, state.getValue(FACING)), 2);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
    {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.getBlockState(pos.above()).getBlock() != ModBlocks.HEARTH_TOP.value())
        {   this.destroy(level, pos, state);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            if (level.getBlockState(pos.above()).getBlock() == ModBlocks.HEARTH_TOP.value())
            {   level.destroyBlock(pos.above(), false);
            }

            BlockEntity tileentity = level.getBlockEntity(pos);
            if (tileentity instanceof HearthBlockEntity)
            {   Containers.dropContents(level, pos, (HearthBlockEntity) tileentity);
                level.updateNeighborsAt(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation direction)
    {   return state.setValue(FACING, direction.rotate(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {   builder.add(FACING, SIDE_POWERED, BACK_POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Level level = context.getLevel();
        return level.getBlockState(context.getClickedPos().above()).isAir()
               ? this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
               : null;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction)
    {
        return direction != null
            && direction.getAxis() != Direction.Axis.Y
            && direction != state.getValue(FACING).getOpposite();
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side)
    {   return true;
    }
}
