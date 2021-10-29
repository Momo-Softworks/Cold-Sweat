package net.momostudios.coldsweat.client.itemproperties;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class SoulfireLampOverride implements IItemPropertyGetter
{
    @Override
    public float call(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity)
    {
        if (world == null)
            return 0;
        else
            return world.getDimensionKey().getLocation().getPath().equals("the_nether") ? 1 : 0;
    }
}
