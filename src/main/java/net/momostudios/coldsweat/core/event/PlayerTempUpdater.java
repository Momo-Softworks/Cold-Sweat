package net.momostudios.coldsweat.core.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.temperature.PlayerTemp;
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
        List<TempModifier> modList = PlayerTemp.getModifiers(player, PlayerTemp.Types.AMBIENT);
        PlayerTemp.setTemperature(player, new Temperature(new Temperature().with(modList, player).get()), PlayerTemp.Types.AMBIENT);

        double bodyTemp = PlayerTemp.getTemperature(player, PlayerTemp.Types.BODY).get();
        double ambientTemp = PlayerTemp.getTemperature(player, PlayerTemp.Types.AMBIENT).get();
        double maxTemp = ColdSweatConfig.maxHabitable.get();
        double minTemp = ColdSweatConfig.minHabitable.get();

        //Increase body temperature when ambientTemp is above maximum (with rate modifiers)
        if (ambientTemp > maxTemp)
        {
            PlayerTemp.setTemperature
            (
                player,
                new Temperature
                (
                    Math.min(bodyTemp +
                    new Temperature((maxTemp - ambientTemp) / 75)
                        .with(PlayerTemp.getModifiers(player, PlayerTemp.Types.RATE), player).get(), 150)
                ),
                PlayerTemp.Types.BODY
            );
        }
        //Return the player's temperature back to 0
        else if (bodyTemp > 0)
        {
            PlayerTemp.setTemperature
            (
                player,
                new Temperature(bodyTemp - Math.min(Math.max(0.15, Math.abs(ambientTemp - maxTemp)), bodyTemp)),
                PlayerTemp.Types.BODY
            );
        }

        //Decrease body temperature when ambientTemp is below minimum (with rate modifiers)
        if (ambientTemp < minTemp)
        {
            PlayerTemp.setTemperature
            (
                player,
                new Temperature
                (
                    Math.max(bodyTemp - new Temperature((maxTemp - ambientTemp) / 75)
                        .with(PlayerTemp.getModifiers(player, PlayerTemp.Types.RATE), player).get(), -150)
                ),
                PlayerTemp.Types.BODY
            );
        }
        //Return the player's temperature back to 0
        else if (bodyTemp < 0)
        {
            PlayerTemp.setTemperature
            (
                player,
                new Temperature(bodyTemp + Math.min(Math.max(0.15, Math.abs(ambientTemp - maxTemp)), -bodyTemp)),
                PlayerTemp.Types.BODY
            );
        }

        //Sets the player's overall temperature (base + modifiers)
        PlayerTemp.setTemperature
        (
            player,
            new Temperature
            (
                PlayerTemp.getTemperature(player, PlayerTemp.Types.BASE).with(PlayerTemp.getModifiers(player, PlayerTemp.Types.BASE), player).get() +
                PlayerTemp.getTemperature(player, PlayerTemp.Types.BODY).with(PlayerTemp.getModifiers(player, PlayerTemp.Types.BODY), player).get()
            ),
            PlayerTemp.Types.COMPOSITE
        );
    }
}