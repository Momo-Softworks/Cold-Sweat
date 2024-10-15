package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.IceboxBlockEntity;
import com.momosoftworks.coldsweat.core.init.ModBlockEntities;
import com.momosoftworks.coldsweat.core.init.ModBlocks;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.core.init.ModParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class IceboxBlock extends Block implements EntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FROSTED = BooleanProperty.create("frosted");
    public static final BooleanProperty SMOKESTACK = BooleanProperty.create("smokestack");

    public static final VoxelShape SHAPE = Shapes.block();
    private static final VoxelShape SHAPE_OPEN = Shapes.box(0f, 0f, 0f, 1f, 13/16f, 1f);

    public static Properties getProperties()
    {
        return Properties
                .of()
                .sound(SoundType.WOOD)
                .strength(2f, 5f)
                .noOcclusion();
    }

    public static Item.Properties getItemProperties()
    {
        return new Item.Properties();
    }

    public IceboxBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(FROSTED, false).setValue(SMOKESTACK, false));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.ICEBOX.value() ? IceboxBlockEntity::tick : null;
    }

    public RenderShape getRenderShape(BlockState pState)
    {   return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext selection)
    {
        if (level.getBlockEntity(pos) instanceof IceboxBlockEntity icebox)
        {   return icebox.getOpenNess(0) > 0 ? SHAPE_OPEN : SHAPE;
        }
        return SHAPE;
    }

    public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam)
    {
        super.triggerEvent(pState, pLevel, pPos, pId, pParam);
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);

        return blockentity != null && blockentity.triggerEvent(pId, pParam);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        if (level.getBlockEntity(pos) instanceof IceboxBlockEntity icebox)
        {
            // If the player is trying to put a smokestack on top, don't do anything
            if (stack.getItem() == ModItems.SMOKESTACK.value() && rayTraceResult.getDirection() == Direction.UP
            && level.getBlockState(pos.above()).getBlock() instanceof AirBlock)
            {   return ItemInteractionResult.FAIL;
            }
            int itemFuel = icebox.getItemFuel(stack);

            if (itemFuel != 0 && icebox.getFuel() + itemFuel * 0.75 < icebox.getMaxFuel())
            {
                if (!player.isCreative())
                {
                    if (stack.hasCraftingRemainingItem())
                    {
                        ItemStack container = stack.getCraftingRemainingItem();
                        stack.shrink(1);
                        player.getInventory().add(container);
                    }
                    else
                    {   stack.shrink(1);
                    }
                }
                icebox.setFuel(icebox.getFuel() + itemFuel);

                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 0.9f + new Random().nextFloat() * 0.2F);
            }
            else if (!level.isClientSide && !ChestBlock.isChestBlockedAt(level, pos))
            {   player.openMenu(icebox, pos);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new IceboxBlockEntity(pos, state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos)
    {
        if (neighborPos.equals(pos.above()) && level.getBlockEntity(pos) instanceof IceboxBlockEntity icebox)
        {
            boolean hasSmokestack = icebox.checkForSmokestack();
            if (hasSmokestack != state.getValue(SMOKESTACK))
            {
                state = state.setValue(SMOKESTACK, hasSmokestack);
                level.setBlock(pos, state, 3);
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean isMoving)
    {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, isMoving);
        // Check for redstone power to this block
        HearthBlockEntity hearth = (HearthBlockEntity) level.getBlockEntity(pos);
        if (hearth != null)
        {   hearth.checkInputSignal();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            BlockEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof IceboxBlockEntity te)
            {
                Containers.dropContents(world, pos, te);
                world.updateNeighborsAt(pos, this);
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation direction)
    {
        return state.setValue(FACING, direction.rotate(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FROSTED, SMOKESTACK);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand)
    {
        ParticleStatus status = Minecraft.getInstance().options.particles().get();
        if (!state.getValue(FROSTED) || status == ParticleStatus.MINIMAL) return;

        double d0 = pos.getX() + 0.5;
        double d1 = pos.getY();
        double d2 = pos.getZ() + 0.5;
        boolean side = new Random().nextBoolean();
        double d5 = side ? Math.random() - 0.5 : (Math.random() < 0.5 ? 0.55 : -0.55);
        double d6 = Math.random() * 0.3;
        double d7 = !side ? Math.random() - 0.5 : (Math.random() < 0.5 ? 0.55 : -0.55);
        level.addParticle(ModParticleTypes.GROUND_MIST.get(), d0 + d5, d1 + d6, d2 + d7, d5 / 40, 0.0D, d7 / 40);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction)
    {
        return direction != null
            && direction.getAxis() != Direction.Axis.Y
            && level.getBlockState(pos.above()).is(ModBlocks.SMOKESTACK);
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side)
    {   return true;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state)
    {   return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos)
    {   return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
}
