package dev.momosoftworks.coldsweat.common.block;

import dev.momosoftworks.coldsweat.core.itemgroup.ColdSweatGroup;
import dev.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.Collections;
import java.util.List;

public class MinecartInsulationBlock extends Block
{
    public static Properties getProperties()
    {
        return Properties
                .of(Material.WOOL)
                .sound(SoundType.WOOL)
                .strength(0f, 0f);
    }

    public static Item.Properties getItemProperties()
    {
        return new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT);
    }

    public Item asItem()
    {
        return ModItems.MINECART_INSULATION;
    }

    public MinecartInsulationBlock(Block.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
    {   return Collections.emptyList();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState otherState, LevelAccessor level, BlockPos blockPos, BlockPos otherBlockPos)
    {
        return Blocks.AIR.defaultBlockState();
    }
}
