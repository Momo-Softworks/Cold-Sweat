package net.momostudios.coldsweat.common.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.temperature.modifier.*;
import net.momostudios.coldsweat.common.world.TempModifierEntries;
import net.momostudios.coldsweat.core.util.registrylists.ModEffects;
import net.momostudios.coldsweat.core.util.PlayerHelper;
import net.momostudios.coldsweat.core.util.PlayerTemp;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AddTempModifiers
{
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        PlayerEntity player = event.player;

        /*
         * Add TempModifiers if not present
         */
        if (player.ticksExisted % 20 == 4)
        {
            PlayerTemp.applyModifier(player, new BiomeTempModifier(), PlayerTemp.Types.AMBIENT, false);
            PlayerTemp.applyModifier(player, new TimeTempModifier(), PlayerTemp.Types.AMBIENT, false);
            PlayerTemp.applyModifier(player, new WeatherTempModifier(), PlayerTemp.Types.AMBIENT, false);
            PlayerTemp.applyModifier(player, new DepthTempModifier(), PlayerTemp.Types.AMBIENT, false);
            PlayerTemp.applyModifier(player, new BlockTempModifier(), PlayerTemp.Types.AMBIENT, false);;
            PlayerTemp.applyModifier(player, TempModifierEntries.getEntries().getEntryFor("sereneseasons:season"), PlayerTemp.Types.AMBIENT, false);

            // Hearth
            if (player.isPotionActive(ModEffects.INSULATION))
                PlayerTemp.applyModifier(player, new HearthTempModifier(), PlayerTemp.Types.AMBIENT, false);
            else
                PlayerTemp.removeModifier(player, HearthTempModifier.class, PlayerTemp.Types.AMBIENT, 1);

            // Soul Lamp
            if (PlayerHelper.holdingLamp(player, HandSide.RIGHT) || PlayerHelper.holdingLamp(player, HandSide.LEFT))
                PlayerTemp.applyModifier(player, new SoulLampTempModifier(), PlayerTemp.Types.AMBIENT, false);
            else
                PlayerTemp.removeModifier(player, SoulLampTempModifier.class, PlayerTemp.Types.AMBIENT, 1);
        }
    }
}