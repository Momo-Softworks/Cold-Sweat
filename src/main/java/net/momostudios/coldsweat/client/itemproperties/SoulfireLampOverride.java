package net.momostudios.coldsweat.client.itemproperties;

import net.minecraft.client.Minecraft;
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
        if (world == null && Minecraft.getInstance().player != null)
            world = Minecraft.getInstance().player.worldClient;

        if (world != null && world.getDimensionKey().getLocation().getPath().equals("the_nether"))
        {
            return stack.getOrCreateTag().getInt("fuel") > 43 ? 1 :
                   stack.getOrCreateTag().getInt("fuel") > 22 ? 2 :
                   stack.getOrCreateTag().getInt("fuel") > 0 ? 3 : 0;
        }
        else
            return 0;
    }
}
