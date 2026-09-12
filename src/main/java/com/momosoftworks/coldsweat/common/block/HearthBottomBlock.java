package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.core.itemgroup.ColdSweatGroup;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModBlockEntities;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModSounds;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.*;

public class HearthBottomBlock extends Block implements EntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty COOLING = BooleanProperty.create("cooling");
    public static final BooleanProperty HEATING = BooleanProperty.create("heating");
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty FROSTED = BooleanProperty.create("frosted");
    public static final BooleanProperty SMART = BooleanProperty.create("smart");

    public static Properties getProperties()
    {
        return Properties
                .of(Material.STONE)
                .sound(SoundType.STONE)
                .destroyTime(2.0F)
                .explosionResistance(10.0F)
                .requiresCorrectToolForDrops()
                .isRedstoneConductor((state, level, pos) -> false)
                .lightLevel(state -> state.getValue(LIT) ? 13 : 0)
                .noOcclusion();
    }

    public static Item.Properties getItemProperties()
    {   return new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1);
    }

    public HearthBottomBlock(Block.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH)
                                                          .setValue(COOLING, false)
                                                          .setValue(HEATING, false)
                                                          .setValue(LIT, false)
                                                          .setValue(FROSTED, false)
                                                          .setValue(SMART, false));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {   return type == ModBlockEntities.HEARTH ? HearthBlockEntity::tickSelf : null;
    }

    public RenderShape getRenderShape(BlockState pState)
    {   return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {   return new HearthBlockEntity(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        if (level.getBlockEntity(pos) instanceof HearthBlockEntity te)
        {
            ItemStack stack = player.getItemInHand(hand);

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
                level.playSound(null, pos, itemFuel > 0
                                             ? SoundEvents.BUCKET_EMPTY_LAVA
                                             : ModSounds.BUCKET_EMPTY_SLUSH,
                                  SoundSource.BLOCKS, 1.0F, 0.9f + new Random().nextFloat() * 0.2F);
            }
            // Open the GUI
            else if (!level.isClientSide)
            {   NetworkHooks.openScreen((ServerPlayer) player, te, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
    {
        HearthBlockEntity hearth = (HearthBlockEntity) level.getBlockEntity(pos);
        if (hearth != null && hearth.isUsingColdFuel())
        {   IceboxBlock.createMistParticles(level, pos);
        }
        if (state.getValue(LIT))
        {   BoilerBlock.createFlameParticles(level, pos, state, 0.6, 0.1);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack item)
    {   level.setBlock(pos.above(), WorldHelper.waterlog(ModBlocks.HEARTH_TOP.defaultBlockState(), level, pos.above()), 3);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean isMoving)
    {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, isMoving);
        if (level.getBlockState(pos.above()).getBlock() != ModBlocks.HEARTH_TOP)
        {   level.destroyBlock(pos, false);
        }
        else
        {   // Check for redstone power to this block
            HearthBlockEntity hearth = (HearthBlockEntity) level.getBlockEntity(pos);
            if (hearth != null)
            {   hearth.checkInputSignal();
                hearth.checkForStateChange();
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (!isMoving && state.getBlock() != newState.getBlock() && !isMoving)
        {
            BlockEntity tileentity = level.getBlockEntity(pos);
            if (tileentity instanceof HearthBlockEntity)
            {   Containers.dropContents(level, pos, (HearthBlockEntity) tileentity);
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
    {   builder.add(FACING, COOLING, HEATING, LIT, FROSTED, SMART);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Level level = context.getLevel();
        return level.getBlockState(context.getClickedPos().above()).canBeReplaced(context)
               ? this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                                         .setValue(SMART, ConfigSettings.SMART_HEARTH.get())
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
    public boolean hasAnalogOutputSignal(BlockState pState)
    {   return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level level, BlockPos pos)
    {   return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
}
