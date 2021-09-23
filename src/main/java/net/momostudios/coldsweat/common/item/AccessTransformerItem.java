package net.momostudios.coldsweat.common.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.BiomeTempModifier;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.momostudios.coldsweat.core.util.ModItems;

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
