package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
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
        if (event.getResultStatus() != null) return;

        Player player = event.getPlayer();
        double bodyTemp = Temperature.get(player, Temperature.Type.BODY);
        double worldTemp = Temperature.get(player, Temperature.Type.WORLD);
        double minTemp = ConfigSettings.getInstance().minTemp + Temperature.get(player, Temperature.Type.MIN);
        double maxTemp = ConfigSettings.getInstance().maxTemp + Temperature.get(player, Temperature.Type.MAX);

        // If the player's body temperature is critical
        if (!CSMath.isBetween(bodyTemp, -100, 100))
        {
            player.displayClientMessage(new TranslatableComponent("cold_sweat.message.sleep.body." + (bodyTemp > 99 ? "hot" : "cold")), true);
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
        // If the player's world temperature is critical
        else if (!CSMath.isInRange(worldTemp, minTemp, maxTemp))
        {
            player.displayClientMessage(new TranslatableComponent("cold_sweat.message.sleep.world." + (worldTemp > maxTemp ? "hot" : "cold")), true);
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
    }
}
