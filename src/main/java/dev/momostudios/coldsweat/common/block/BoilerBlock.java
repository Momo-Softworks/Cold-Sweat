package dev.momostudios.coldsweat.common.block;

import dev.momostudios.coldsweat.common.te.BoilerBlockEntity;
import dev.momostudios.coldsweat.core.init.BlockEntityInit;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;

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
                .strength(1)
                .lightLevel(getLightValueLit(13));
    }

    private static ToIntFunction<BlockState> getLightValueLit(int lightValue)
    {
        return (state) -> state.getValue(BlockStateProperties.LIT) ? lightValue : 0;
    }

    public static Item.Properties getItemProperties()
    {
        return new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT);
    }

    public BoilerBlock()
    {
        super(getProperties());
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        if (!worldIn.isClientSide)
        {
            if (worldIn.getBlockEntity(pos) instanceof BoilerBlockEntity)
            {
                BoilerBlockEntity te = (BoilerBlockEntity) worldIn.getBlockEntity(pos);
                ItemStack stack = player.getItemInHand(hand);
                int itemFuel = te.getItemFuel(stack);

                if (itemFuel != 0 && te.getFuel() + itemFuel * 0.75 < BoilerBlockEntity.MAX_FUEL)
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
                    te.setFuel(te.getFuel() + itemFuel);

                    worldIn.playSound(null, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1.0F, 0.9f + new Random().nextFloat() * 0.2F);
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
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty())
            return dropsOriginal;
        return Collections.singletonList(new ItemStack(this, 1));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            BlockEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof BoilerBlockEntity te)
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
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(LIT, false);
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand)
    {
        if (stateIn.getValue(LIT))
        {
            double d0 = pos.getX() + 0.5D;
            double d1 = pos.getY();
            double d2 = pos.getZ() + 0.5D;
            Direction direction = stateIn.getValue(FACING);
            Direction.Axis direction$axis = direction.getAxis();

            double d4 = rand.nextDouble() * 0.6D - 0.3D;
            double d5 = direction$axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52D : d4;
            double d6 = rand.nextDouble() * 6.0D / 16.0D + 0.2;
            double d7 = direction$axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52D : d4;
            worldIn.addParticle(ParticleTypes.SMOKE, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
            worldIn.addParticle(ParticleTypes.FLAME, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new BoilerBlockEntity(BlockEntityInit.BOILER_TILE_ENTITY_TYPE.get(), pos, state);
    }
}
