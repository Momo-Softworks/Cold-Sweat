package net.momostudios.coldsweat.common.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.temperature.modifier.*;
import net.momostudios.coldsweat.core.util.PlayerTemp;
import net.momostudios.coldsweat.config.ColdSweatConfig;

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
            PlayerTemp.applyModifier(player, new BiomeTempModifier(), PlayerTemp.Types.AMBIENT, false);
            PlayerTemp.applyModifier(player, new TimeTempModifier(), PlayerTemp.Types.AMBIENT, false);
            PlayerTemp.applyModifier(player, new WeatherTempModifier(), PlayerTemp.Types.AMBIENT, false);
            PlayerTemp.applyModifier(player, new DepthTempModifier(), PlayerTemp.Types.AMBIENT, false);
            PlayerTemp.applyModifier(player, new BlockTempModifier(), PlayerTemp.Types.AMBIENT, false);
        }

        double ambientTemp = PlayerTemp.getTemperature(player, PlayerTemp.Types.AMBIENT).get();
        //double bodyTemp = PlayerTemp.getTemperature(player, PlayerTemp.Types.BODY).get();

        player.sendStatusMessage(new StringTextComponent((int) (ambientTemp * 42 + 32 + ColdSweatConfig.getInstance().tempOffset()) + ""), true);
    }
}