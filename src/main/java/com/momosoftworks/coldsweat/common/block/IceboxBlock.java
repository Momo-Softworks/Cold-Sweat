package com.momosoftworks.coldsweat.common.block;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.redstone.Orientation;
import com.momosoftworks.coldsweat.util.item.ItemStackHelper;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.IceboxBlockEntity;
import com.momosoftworks.coldsweat.core.init.*;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class IceboxBlock extends Block implements EntityBlock
{
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FROSTED = HearthBottomBlock.FROSTED;
    public static final BooleanProperty SMOKESTACK = BooleanProperty.create("smokestack");

    public static final VoxelShape SHAPE = Shapes.block();
    private static final VoxelShape SHAPE_OPEN = Shapes.box(0f, 0f, 0f, 1f, 13/16f, 1f);

    public static Properties getProperties()
    {
        return Properties
                .of()
                .sound(SoundType.WOOD)
                .strength(2f, 5f)
                .isRedstoneConductor(IceboxBlock::conductsRedstone)
                .noOcclusion();
    }

    public static Item.Properties getItemProperties()
    {
        return new Item.Properties();
    }

    private static boolean conductsRedstone(BlockState state, BlockGetter level, BlockPos pos)
    {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof HearthBlockEntity hearthLike)
        {   return !hearthLike.hasSmokestack();
        }
        return false;
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
    {   return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext selection)
    {
        if (level.getBlockEntity(pos) instanceof IceboxBlockEntity icebox)
        {   return icebox.getOpenNess(0) > 0 ? SHAPE_OPEN : SHAPE;
        }
        return SHAPE;
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param)
    {
        BlockEntity blockentity = level.getBlockEntity(pos);
        return blockentity != null && blockentity.triggerEvent(id, param);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        if (level.getBlockEntity(pos) instanceof IceboxBlockEntity icebox)
        {
            // If the player is trying to put a smokestack on top, don't do anything
            if (stack.getItem() == ModItems.SMOKESTACK.value() && rayTraceResult.getDirection() == Direction.UP
            && level.getBlockState(pos.above()).canBeReplaced())
            {   return InteractionResult.FAIL;
            }
            int itemFuel = icebox.getItemFuel(stack);

            if (itemFuel != 0 && icebox.getFuel() + itemFuel * 0.75 < icebox.getMaxFuel())
            {
                if (!player.isCreative())
                {
                    if (ItemStackHelper.hasCraftingRemainder(stack))
                    {
                        ItemStack container = ItemStackHelper.getCraftingRemainder(stack);
                        stack.shrink(1);
                        player.getInventory().add(container);
                    }
                    else
                    {   stack.shrink(1);
                    }
                }
                icebox.addColdFuel(itemFuel, true);

                level.playSound(null, pos, ModSounds.BUCKET_EMPTY_SLUSH.value(), SoundSource.BLOCKS, 1.0F, 0.9f + new Random().nextFloat() * 0.2F);
            }
            else if (!level.isClientSide() && !ChestBlock.isChestBlockedAt(level, pos))
            {   player.openMenu(icebox, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new IceboxBlockEntity(pos, state);
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random)
    {
        if (neighborPos.equals(pos.above()) && level.getBlockEntity(pos) instanceof IceboxBlockEntity icebox)
        {
            boolean hadSmokestack = icebox.hasSmokestack();
            boolean hasSmokeStack = icebox.checkForSmokestack();
            if (hadSmokestack != hasSmokeStack)
            {
                // The returned state is placed by the caller
                state = state.setValue(SMOKESTACK, hasSmokeStack);
                if (level instanceof Level realLevel) realLevel.updateNeighborsAt(pos, this);
            }
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation fromPos, boolean isMoving)
    {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, isMoving);
        // Check for redstone power to this block
        HearthBlockEntity hearth = (HearthBlockEntity) level.getBlockEntity(pos);
        if (hearth != null)
        {   hearth.checkInputSignal();
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston)
    {
        // Contents are dropped by BlockEntity#preRemoveSideEffects
        level.updateNeighborsAt(pos, this);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
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
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockPos pos = context.getClickedPos();
        boolean belowSmokestack = context.getLevel().getBlockState(pos.above()).getBlock() instanceof SmokestackBlock;
        return this.defaultBlockState()
               .setValue(FACING, context.getHorizontalDirection().getOpposite())
               .setValue(SMOKESTACK, belowSmokestack);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand)
    {
        ParticleStatus status = Minecraft.getInstance().options.particles().get();
        if (status == ParticleStatus.MINIMAL) return;
        IceboxBlockEntity icebox = (IceboxBlockEntity) level.getBlockEntity(pos);
        if (icebox == null) return;

        if (icebox.isUsingColdFuel())
        {   createMistParticles(level, pos);
        }
    }

    public static void createMistParticles(Level level, BlockPos pos)
    {
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
    public boolean hasAnalogOutputSignal(BlockState state)
    {   return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction)
    {   return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
}
