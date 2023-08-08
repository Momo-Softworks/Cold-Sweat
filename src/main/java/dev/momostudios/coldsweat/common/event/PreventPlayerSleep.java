package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PreventPlayerSleep
{
    @SubscribeEvent
    public static void onTrySleep(PlayerSleepInBedEvent event)
    {
        // There's already something blocking the player from sleeping
        if (event.getResultStatus() != null || !ConfigSettings.CHECK_SLEEP_CONDITIONS.get()) return;

        PlayerEntity player = event.getPlayer();
        double bodyTemp = Temperature.get(player, Temperature.Type.BODY);
        double worldTemp = Temperature.get(player, Temperature.Type.WORLD);
        double minTemp = ConfigSettings.MIN_TEMP.get() + Temperature.get(player, Temperature.Type.BURNING_POINT);
        double maxTemp = ConfigSettings.MAX_TEMP.get() + Temperature.get(player, Temperature.Type.FREEZING_POINT);

        // If the player's body temperature is critical
        if (!CSMath.isBetween(bodyTemp, -100, 100))
        {
            player.displayClientMessage(new TranslationTextComponent("cold_sweat.message.sleep.body." + (bodyTemp > 99 ? "hot" : "cold")), true);
            event.setResult(PlayerEntity.SleepResult.OTHER_PROBLEM);
        }
        // If the player's world temperature is critical
        else if (!CSMath.withinRange(worldTemp, minTemp, maxTemp))
        {
            player.displayClientMessage(new TranslationTextComponent("cold_sweat.message.sleep.world." + (worldTemp > maxTemp ? "hot" : "cold")), true);
            event.setResult(PlayerEntity.SleepResult.OTHER_PROBLEM);
        }
    }
}
