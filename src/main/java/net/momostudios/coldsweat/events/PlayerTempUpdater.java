package net.momostudios.coldsweat.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.temperature.PlayerTempHandler;
import net.momostudios.coldsweat.temperature.Temperature;
import net.momostudios.coldsweat.temperature.modifier.TempModifier;
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
    }
}