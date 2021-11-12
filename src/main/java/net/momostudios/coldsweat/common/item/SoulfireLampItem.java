package net.momostudios.coldsweat.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.momostudios.coldsweat.core.util.PlayerTemp;

public class SoulfireLampItem extends Item
{
    public SoulfireLampItem()
    {
        super(new Properties().group(ColdSweatGroup.COLD_SWEAT).maxStackSize(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (entityIn instanceof PlayerEntity)
        {
            double temp = PlayerTemp.getTemperature((PlayerEntity) entityIn, PlayerTemp.Types.AMBIENT).get();
            if (!stack.getOrCreateTag().getBoolean("hasTicked"))
            {
                stack.getOrCreateTag().putBoolean("hasTicked", true);
                setFuel(stack, 0);
            }
            if (isSelected && entityIn.world.getDimensionKey().getLocation().getPath().equals("the_nether") && temp > ColdSweatConfig.getInstance().maxHabitable() && entityIn.ticksExisted % 10 == 0)
            {
                if (getFuel(stack) > 0)
                {
                    addFuel(stack, -0.026666667f * (float) Math.min(3, Math.max(1, (temp - ColdSweatConfig.getInstance().maxHabitable()) / 5)));
                }
            }
        }
    }

    private void setFuel(ItemStack stack, float fuel)
    {
        stack.getOrCreateTag().putFloat("fuel", fuel);
    }
    private void addFuel(ItemStack stack, float fuel)
    {
        stack.getOrCreateTag().putFloat("fuel", stack.getOrCreateTag().getFloat("fuel") + fuel);
    }
    private float getFuel(ItemStack stack)
    {
        return stack.getOrCreateTag().getFloat("fuel");
    }
}
