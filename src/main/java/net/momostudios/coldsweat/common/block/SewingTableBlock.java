package net.momostudios.coldsweat.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;
import net.momostudios.coldsweat.common.container.SewingContainer;
import net.momostudios.coldsweat.core.init.TileEntityInit;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;

import java.util.Collections;
import java.util.List;

public class SewingTableBlock extends Block
{
    public static Properties getProperties()
    {
        return Properties
                .create(Material.WOOD)
                .sound(SoundType.WOOD)
                .hardnessAndResistance(2f, 5f)
                .harvestTool(ToolType.AXE)
                .harvestLevel(1);
    }

    public static Item.Properties getItemProperties()
    {
        return new Item.Properties().group(ColdSweatGroup.COLD_SWEAT);
    }

    public SewingTableBlock(Properties properties)
    {
        super(SewingTableBlock.getProperties());
        this.setDefaultState(this.stateContainer.getBaseState());
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult)
    {
        if (worldIn.isRemote)
        {
            return ActionResultType.SUCCESS;
        }
        else
        {
            player.openContainer(state.getContainer(worldIn, pos));
            return ActionResultType.CONSUME;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos)
    {
        return new SimpleNamedContainerProvider((id, inventory, player) -> {
            return new SewingContainer(id, inventory, IWorldPosCallable.of(worldIn, pos));
        }, new TranslationTextComponent("container.cold_sweat.sewing_table"));
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty())
            return dropsOriginal;
        return Collections.singletonList(new ItemStack(this, 1));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState();
    }
}
