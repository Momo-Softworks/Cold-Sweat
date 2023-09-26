package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
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

        Player player = event.getPlayer();
        double bodyTemp = Temperature.get(player, Temperature.Type.BODY);
        double worldTemp = Temperature.get(player, Temperature.Type.WORLD);
        double minTemp = ConfigSettings.MIN_TEMP.get() + Temperature.get(player, Temperature.Type.BURNING_POINT);
        double maxTemp = ConfigSettings.MAX_TEMP.get() + Temperature.get(player, Temperature.Type.FREEZING_POINT);

        // If the player's body temperature is critical
        if (!CSMath.isBetween(bodyTemp, -100, 100))
        {
            player.displayClientMessage(new TranslatableComponent("cold_sweat.message.sleep.body." + (bodyTemp > 99 ? "hot" : "cold")), true);
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
        // If the player's world temperature is critical
        else if (!CSMath.withinRange(worldTemp, minTemp, maxTemp))
        {
            player.displayClientMessage(new TranslatableComponent("cold_sweat.message.sleep.world." + (worldTemp > maxTemp ? "hot" : "cold")), true);
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
    }
}
