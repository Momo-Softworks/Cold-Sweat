package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.common.blockentity.BoilerBlockEntity;
import com.momosoftworks.coldsweat.core.itemgroup.ColdSweatGroup;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.util.registries.ModBlockEntities;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BoilerBlock extends Block implements EntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public static Properties getProperties()
    {
        return Properties
                .of(Material.STONE)
                .sound(SoundType.STONE)
                .destroyTime(2f)
                .explosionResistance(10f)
                .lightLevel(state -> state.getValue(LIT) ? 13 : 0)
                .isRedstoneConductor(BoilerBlock::conductsRedstone)
                .requiresCorrectToolForDrops();
    }

    public static Item.Properties getItemProperties()
    {
        return new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT);
    }

    private static boolean conductsRedstone(BlockState state, BlockGetter level, BlockPos pos)
    {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof HearthBlockEntity hearthLike)
        {   return !hearthLike.hasSmokestack();
        }
        return false;
    }

    public BoilerBlock(Block.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.BOILER ? BoilerBlockEntity::tick : null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        ItemStack stack = player.getItemInHand(hand);
        // If the player is trying to put a smokestack on top, don't do anything
        if (stack.getItem() == ModItems.SMOKESTACK && rayTraceResult.getDirection() == Direction.UP
        && level.getBlockState(pos.above()).canBeReplaced(new BlockPlaceContext(player, hand, stack, rayTraceResult)))
        {   return InteractionResult.FAIL;
        }
        if (!level.isClientSide)
        {
            if (level.getBlockEntity(pos) instanceof BoilerBlockEntity blockEntity)
            {
                int itemFuel = blockEntity.getItemFuel(stack);

                if (itemFuel != 0 && blockEntity.getFuel() + itemFuel * 0.75 < blockEntity.getMaxFuel())
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
                        {   stack.shrink(1);
                        }
                    }
                    blockEntity.setFuel(blockEntity.getFuel() + itemFuel);

                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1.0F, 0.9f + new Random().nextFloat() * 0.2F);
                }
                else
                {   NetworkHooks.openGui((ServerPlayer) player, blockEntity, pos);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos)
    {
        if (direction == Direction.UP && level.getBlockEntity(pos) instanceof BoilerBlockEntity boiler)
        {
            boolean hadSmokestack = boiler.hasSmokestack();
            if (hadSmokestack != boiler.checkForSmokestack())
            {   level.blockUpdated(pos, this);
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean isMoving)
    {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, isMoving);
        // Check for redstone power to this block
        BoilerBlockEntity hearth = (BoilerBlockEntity) level.getBlockEntity(pos);
        if (hearth != null)
        {   hearth.checkInputSignal();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BoilerBlockEntity te)
            {
                Containers.dropContents(level, pos, te);
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
    {   builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {   return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(LIT, false);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random rand)
    {
        ParticleStatus status = Minecraft.getInstance().options.particles;
        if (state.getValue(LIT) && status != ParticleStatus.MINIMAL)
        {   createFlameParticles(level, pos, state, 0.52, 0);
        }
    }

    public static void createFlameParticles(Level level, BlockPos pos, BlockState state, double zOffset, double yOffset)
    {
        Random rand = level.getRandom();
        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;
        Direction direction = state.getValue(FACING);
        Direction.Axis direction$axis = direction.getAxis();

        double randH = rand.nextDouble() * 0.6D - 0.3D;
        double randV = rand.nextDouble() * 3.0D / 16.0D + 3 / 16.0;
        double randPosX = direction$axis == Direction.Axis.X ? direction.getStepX() * zOffset : randH;
        double randPosZ = direction$axis == Direction.Axis.Z ? direction.getStepZ() * zOffset : randH;
        level.addParticle(ParticleTypes.SMOKE, x + randPosX, y + randV + yOffset, z + randPosZ, 0.0D, 0.0D, 0.0D);
        level.addParticle(ParticleTypes.FLAME, x + randPosX, y + randV + yOffset, z + randPosZ, 0.0D, 0.0D, 0.0D);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {   return new BoilerBlockEntity(pos, state);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction)
    {
        return direction != null
            && direction.getAxis() != Direction.Axis.Y
            && direction != state.getValue(FACING).getOpposite()
            && level.getBlockState(pos.above()).is(ModBlocks.SMOKESTACK);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState)
    {   return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level level, BlockPos pos)
    {   return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
}
