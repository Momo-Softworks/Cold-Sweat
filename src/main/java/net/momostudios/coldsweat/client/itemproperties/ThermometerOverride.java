package net.momostudios.coldsweat.client.itemproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.util.PlayerTemp;

import javax.annotation.Nullable;

public class ThermometerOverride implements IItemPropertyGetter
{
    @Override
    public float call(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity)
    {
        PlayerEntity player = Minecraft.getInstance().player;
        ConfigCache config = ConfigCache.getInstance();
        float minTemp = (float) config.minTemp;
        float maxTemp = (float) config.maxTemp;

        float ambientTemp = (float) PlayerTemp.getTemperature(player, PlayerTemp.Types.AMBIENT).get();

        float ambientAdjusted = ambientTemp - minTemp;
        float tempScaleFactor = 1 / ((maxTemp - minTemp) / 2);

        return ambientAdjusted * tempScaleFactor - 1;
    }
}
