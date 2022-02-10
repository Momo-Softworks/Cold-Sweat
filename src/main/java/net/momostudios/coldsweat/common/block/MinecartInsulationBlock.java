package net.momostudios.coldsweat.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.momostudios.coldsweat.util.registrylists.ModItems;

public class MinecartInsulationBlock extends Block
{
    public static Properties getProperties()
    {
        return Properties
                .create(Material.WOOL)
                .sound(SoundType.CLOTH)
                .hardnessAndResistance(2f, 5f)
                .harvestTool(ToolType.HOE)
                .harvestLevel(1);
    }

    public static Item.Properties getItemProperties()
    {
        return new Item.Properties().group(ColdSweatGroup.COLD_SWEAT);
    }

    public Item asItem()
    {
        return ModItems.MINECART_INSULATION;
    }

    public MinecartInsulationBlock(Properties properties)
    {
        super(MinecartInsulationBlock.getProperties());
        this.setDefaultState(this.stateContainer.getBaseState());
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState();
    }
}
