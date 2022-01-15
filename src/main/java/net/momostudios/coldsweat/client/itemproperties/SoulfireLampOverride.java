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
        if (stack.getOrCreateTag().getBoolean("isOn"))
        {
            return stack.getOrCreateTag().getInt("fuel") > 43 ? 1 :
                   stack.getOrCreateTag().getInt("fuel") > 22 ? 2 : 3;
        }
        else
        {
            return 0;
        }
    }
}
