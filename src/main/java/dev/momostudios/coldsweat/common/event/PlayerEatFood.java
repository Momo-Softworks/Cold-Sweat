package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.FoodTempModifier;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.config.DynamicValue;
import dev.momostudios.coldsweat.api.util.TempHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber
public class PlayerEatFood
{
    public static DynamicValue<Map<Item, Number>> VALID_FOODS = DynamicValue.of(() -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().temperatureFoods()));

    @SubscribeEvent
    public static void onEatFood(LivingEntityUseItemEvent.Finish event)
    {
        if (event.getEntityLiving() instanceof Player player && event.getItem().isEdible() && !event.getEntityLiving().level.isClientSide)
        {
            double foodTemp = VALID_FOODS.get().getOrDefault(event.getItem().getItem(), 0).doubleValue();
            if (foodTemp != 0)
            {
                TempHelper.addModifier(player, new FoodTempModifier(foodTemp).expires(1), Temperature.Type.CORE, true);
            }
        }
    }
}
