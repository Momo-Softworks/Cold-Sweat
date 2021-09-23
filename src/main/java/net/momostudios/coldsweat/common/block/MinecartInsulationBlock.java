package net.momostudios.coldsweat.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.momostudios.coldsweat.common.container.SewingContainer;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;

import java.util.Collections;
import java.util.List;

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
