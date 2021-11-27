package net.momostudios.coldsweat.common.item;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.SoulLampTempModifier;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import java.util.List;

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
            PlayerEntity player = (PlayerEntity) entityIn;
            List<TempModifier> mods = PlayerTemp.getModifiers(player, PlayerTemp.Types.AMBIENT);
            mods.removeIf(mod -> mod instanceof SoulLampTempModifier);
            double temp = new Temperature().with(mods, player).get();

            if (!stack.getOrCreateTag().getBoolean("hasTicked"))
            {
                stack.getOrCreateTag().putBoolean("hasTicked", true);
                setFuel(stack, 64);
            }
            //System.out.println(isSelected);
            if ((isSelected || player.getHeldItemOffhand() == stack) && player.world.getDimensionKey().getLocation().getPath().equals("the_nether") &&
            temp > ColdSweatConfig.getInstance().maxHabitable() && player.ticksExisted % 10 == 0 && !(player.isCreative() || player.isSpectator()))
            {
                if (getFuel(stack) > 0)
                {
                    addFuel(stack, -0.03f * (float) Math.min(3, Math.max(1, (temp - ColdSweatConfig.getInstance().maxHabitable()))));
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
