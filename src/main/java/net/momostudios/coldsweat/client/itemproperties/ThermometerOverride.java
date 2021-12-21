package net.momostudios.coldsweat.client.itemproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import javax.annotation.Nullable;

public class ThermometerOverride implements IItemPropertyGetter
{
    @Override
    public float call(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity)
    {
        PlayerEntity player = Minecraft.getInstance().player;
        ColdSweatConfig config = ColdSweatConfig.getInstance();
        float minTemp = (float) config.getMinTempHabitable();
        float maxTemp = (float) config.getMaxTempHabitable();

        float ambientTemp = (float) PlayerTemp.getTemperature(player, PlayerTemp.Types.AMBIENT).get();

        float ambientAdjusted = ambientTemp - minTemp;
        float tempScaleFactor = 1 / ((maxTemp - minTemp) / 2);

        return ambientAdjusted * tempScaleFactor - 1;
    }
}
