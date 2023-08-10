package dev.momostudios.coldsweat.common.block;

import dev.momostudios.coldsweat.common.tileentity.IceboxTileEntity;
import dev.momostudios.coldsweat.core.init.TileEntityInit;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.List;
import java.util.Random;

public class IceboxBlock extends Block
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FROSTED = BooleanProperty.create("frosted");
    public static Properties getProperties()
    {
        return Properties
                .of(Material.WOOD)
                .sound(SoundType.WOOD)
                .strength(2f, 5f);
    }

    public static Item.Properties getItemProperties()
    {
        return new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT);
    }

    public IceboxBlock(Block.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(FROSTED, false));
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult)
    {
        if (world.getBlockEntity(pos) instanceof IceboxTileEntity)
        {
            IceboxTileEntity te = (IceboxTileEntity) world.getBlockEntity(pos);
            ItemStack stack = player.getItemInHand(hand);
            int itemFuel = te.getItemFuel(stack);

            if (itemFuel != 0 && te.getFuel() + itemFuel * 0.75 < IceboxTileEntity.MAX_FUEL)
            {
                if (!player.isCreative())
                {
                    if (stack.hasContainerItem())
                    {
                        ItemStack container = stack.getContainerItem();
                        stack.shrink(1);
                        player.inventory.add(container);
                    }
                    else
                    {
                        stack.shrink(1);
                    }
                }
                te.setFuel(te.getFuel() + itemFuel);

                world.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 0.9f + new Random().nextFloat() * 0.2F);
            }
            else if (!world.isClientSide)
            {
                NetworkHooks.openGui((ServerPlayerEntity) player, te, pos);
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {   return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {   return TileEntityInit.ICEBOX_BLOCK_ENTITY_TYPE.get().create();
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
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof IceboxTileEntity)
            {   IceboxTileEntity icebox = (IceboxTileEntity) te;
                InventoryHelper.dropContents(world, pos, icebox);
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
    {   builder.add(FACING, FROSTED);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {   return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(FROSTED, false);
    }
}
