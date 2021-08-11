package net.momostudios.coldsweat.common.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.temperature.PlayerTempHandler;
import net.momostudios.coldsweat.common.temperature.modifier.BiomeTempModifier;
import net.momostudios.coldsweat.common.temperature.modifier.TimeTempModifier;

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

        double ambientTemp = PlayerTempHandler.getAmbient(player).get();
        double bodyTemp = PlayerTempHandler.getBody(player).get();

        player.sendStatusMessage(new StringTextComponent((int) (ambientTemp * 40 + 40) + ""), true);
    }
}