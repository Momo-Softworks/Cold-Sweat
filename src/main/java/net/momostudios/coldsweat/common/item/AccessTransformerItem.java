package net.momostudios.coldsweat.common.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.world.World;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;

public class AccessTransformerItem extends Item
{
    public AccessTransformerItem()
    {
        super(new Properties().group(ColdSweatGroup.COLD_SWEAT).maxStackSize(16));
    }

    public static BlockRayTraceResult getRayTrace(World worldIn, PlayerEntity player, RayTraceContext.FluidMode fluidMode)
    {
        return rayTrace(worldIn, player, fluidMode);
    }
}
