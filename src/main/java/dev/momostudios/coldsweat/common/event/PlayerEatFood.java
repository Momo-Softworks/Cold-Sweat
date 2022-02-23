package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.util.PlayerHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.common.temperature.modifier.FoodTempModifier;

import java.util.List;

@Mod.EventBusSubscriber
public class PlayerEatFood
{
    @SubscribeEvent
    public static void onEatFood(LivingEntityUseItemEvent.Finish event)
    {
        if (event.getEntityLiving() instanceof PlayerEntity && event.getItem().isFood() && !event.getEntityLiving().world.isRemote)
        {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            for (List<?> list : ItemSettingsConfig.getInstance().temperatureFoods())
            {
                if (list.get(0).equals(event.getItem().getItem().getRegistryName().toString()))
                {
                    PlayerHelper.addModifier(player, new FoodTempModifier(((Number) list.get(1)).doubleValue()).expires(1), PlayerHelper.Types.BODY, true);
                    break;
                }
            }
        }
    }
}
