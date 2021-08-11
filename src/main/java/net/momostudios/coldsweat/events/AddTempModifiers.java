package net.momostudios.coldsweat.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.temperature.PlayerTempHandler;
import net.momostudios.coldsweat.temperature.modifier.BiomeTempModifier;
import net.momostudios.coldsweat.temperature.modifier.TimeTempModifier;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AddTempModifiers
{
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        PlayerEntity player = event.player;

        /*
         * Add TempModifies if not present
         */
        if (player.ticksExisted % 20 == 0)
        {
            PlayerTempHandler.applyToAmbient(player, new BiomeTempModifier(), false);
            PlayerTempHandler.applyToAmbient(player, new TimeTempModifier(), false);
        }

        player.sendStatusMessage(new StringTextComponent
        (
            (int) (PlayerTempHandler.getAmbient(player).get() * 40 + 40) + " " +
            (int) (PlayerTempHandler.getBody(player).get())
        ), true);
    }
}