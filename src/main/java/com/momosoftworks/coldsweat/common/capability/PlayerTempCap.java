package com.momosoftworks.coldsweat.common.capability;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effects;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Holds all the information regarding the entity's temperature. This should very rarely be used directly.
 */
public class PlayerTempCap extends AbstractTempCap
{
    @Override
    public void tickHurting(LivingEntity entity, double heatResistance, double coldResistance)
    {
        if ((!(entity instanceof Player player) || !player.isCreative()) && !entity.isSpectator())
        {   super.tickHurting(entity, heatResistance, coldResistance);
        }
    }

    @Override
    public void tick(LivingEntity entity)
    {
        super.tick(entity);
        if (entity.tickCount % 20 == 0 && entity instanceof Player player)
        {   calculateHudVisibility(player);
        }
    }

    @Override
    public void tickDummy(LivingEntity entity)
    {
        super.tickDummy(entity);
        if (entity.tickCount % 20 == 0 && entity instanceof Player player)
        {   calculateHudVisibility(player);
        }
    }

    public void calculateHudVisibility(Player player)
    {
        showWorldTemp = !ConfigSettings.REQUIRE_THERMOMETER.get()
                || player.inventory.items.stream().limit(9).anyMatch(stack -> stack.getItem() == ModItems.THERMOMETER)
                || player.getOffhandItem().getItem() == ModItems.THERMOMETER
                || CompatManager.isCuriosLoaded() && CuriosApi.getCuriosHelper().findFirstCurio(player, ModItems.THERMOMETER).isPresent();
        showBodyTemp = !player.isCreative() && !player.isSpectator();
    }
}
