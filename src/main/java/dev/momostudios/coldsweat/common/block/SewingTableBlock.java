package dev.momostudios.coldsweat.common.block;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import dev.momostudios.coldsweat.common.container.SewingContainer;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SewingTableBlock extends Block implements MenuProvider
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
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        if (worldIn.isClientSide)
        {   return InteractionResult.SUCCESS;
        }
        else
        {   NetworkHooks.openScreen((ServerPlayer)player, this, pos);
            return InteractionResult.CONSUME;
        }
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    public Component getDisplayName()
    {   return Component.translatable("container." + ColdSweat.MOD_ID + ".sewing_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory inv, Player player)
    {   return new SewingContainer(windowID, inv);
    }
}
