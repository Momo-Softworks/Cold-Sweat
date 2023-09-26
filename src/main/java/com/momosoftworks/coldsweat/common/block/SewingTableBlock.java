package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.common.container.SewingContainer;
import com.momosoftworks.coldsweat.core.itemgroup.ColdSweatGroup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.List;

public class SewingTableBlock extends Block implements INamedContainerProvider
{
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

    public SewingTableBlock(Block.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState());
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult)
    {
        if (world.isClientSide)
        {   return ActionResultType.SUCCESS;
        }
        else
        {   NetworkHooks.openGui((ServerPlayerEntity) player, this, pos);
            return ActionResultType.CONSUME;
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

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState();
    }

    @Override
    public ITextComponent getDisplayName()
    {   return null;
    }

    @Override
    public Container createMenu(int windowID, PlayerInventory inv, PlayerEntity player)
    {   return new SewingContainer(windowID, inv);
    }
}
