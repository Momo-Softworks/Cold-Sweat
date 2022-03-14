package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.util.entity.PlayerHelper;
import net.minecraft.world.entity.player.Player;
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
        if (event.getEntityLiving() instanceof Player player && event.getItem().isEdible() && !event.getEntityLiving().level.isClientSide)
        {
            for (List<?> list : ItemSettingsConfig.getInstance().temperatureFoods())
            {
                if (list.get(0).equals(event.getItem().getItem().getRegistryName().toString()))
                {
                    PlayerHelper.addModifier(player, new FoodTempModifier(((Number) list.get(1)).doubleValue()).expires(1), Temperature.Types.CORE, true);
                    break;
                }
            }
        }
    }
}
