package net.momostudios.coldsweat.core.event;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.temperature.PlayerTempHandler;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.config.ColdSweatConfig;

import java.util.List;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerTempUpdater
{
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        PlayerEntity player = event.player;

        /*
         * Runs the calculate() method for every TempModifier on the player
         */
        List<TempModifier> modList = PlayerTempHandler.getAmbientModifiers(player);
        PlayerTempHandler.setAmbient(player, new Temperature(new Temperature().with(modList, player).get()));

        double bodyTemp = PlayerTempHandler.getBody(player).get();
        double ambientTemp = PlayerTempHandler.getAmbient(player).get();
        double maxTemp = ColdSweatConfig.maxHabitable.get();
        double minTemp = ColdSweatConfig.minHabitable.get();

        //Change the player's temperature according to ambient
        if (ambientTemp > maxTemp)
        {
            PlayerTempHandler.setBody(player, new Temperature(Math.min(bodyTemp + new Temperature((maxTemp - ambientTemp) / 75)
            .with(PlayerTempHandler.getRateModifiers(player), player).get(), 150)));
        }
        else if (bodyTemp > 0)
        {
            PlayerTempHandler.setBody(player, new Temperature(bodyTemp - Math.min(Math.max(0.15, Math.abs(ambientTemp - maxTemp)), bodyTemp)));
        }
        if (ambientTemp < minTemp)
        {
            PlayerTempHandler.setBody(player, new Temperature(Math.max(bodyTemp - new Temperature((maxTemp - ambientTemp) / 75)
                    .with(PlayerTempHandler.getRateModifiers(player), player).get(), -150)));
        }
        else if (bodyTemp < 0)
        {
            PlayerTempHandler.setBody(player, new Temperature(bodyTemp + Math.min(Math.max(0.15, Math.abs(ambientTemp - maxTemp)), -bodyTemp)));
        }
    }
}