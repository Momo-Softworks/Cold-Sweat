package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.common.temperature.modifier.*;
import dev.momostudios.coldsweat.common.world.TempModifierEntries;
import dev.momostudios.coldsweat.util.entity.PlayerHelper;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AddTempModifiers
{
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            Player player = event.player;

            /*
             * Add TempModifiers if not present
             */
            if (player.tickCount % 20 == 0)
            {
                PlayerHelper.addModifier(player, new BiomeTempModifier(), Temperature.Types.AMBIENT, false);
                PlayerHelper.addModifier(player, new TimeTempModifier(), Temperature.Types.AMBIENT, false);
                PlayerHelper.addModifier(player, new DepthTempModifier(), Temperature.Types.AMBIENT, false);
                PlayerHelper.addModifier(player, new BlockTempModifier(), Temperature.Types.AMBIENT, false);
                if (ModList.get().isLoaded("sereneseasons"))
                    PlayerHelper.addModifier(player, TempModifierEntries.getEntries().getEntryFor("sereneseasons:season"), Temperature.Types.AMBIENT, false);
                /*
                if (ModList.get().isLoaded("betterweather"))
                    PlayerHelper.addModifier(player, TempModifierEntries.getEntries().getEntryFor("betterweather:season"), PlayerHelper.Types.AMBIENT, false);
                */

                // Hearth
                if (player.hasEffect(ModEffects.INSULATION))
                {
                    MobEffectInstance effect = player.getEffect(ModEffects.INSULATION);
                    int potionLevel = effect.getAmplifier() + 1;
                    PlayerHelper.addModifier(player, new HearthTempModifier(potionLevel).expires(effect.getDuration()), Temperature.Types.AMBIENT, false);
                }
            }

            // Water / Rain
            if (player.tickCount % 5 == 0)
            {
                if (player.isInWaterRainOrBubble())
                {
                    PlayerHelper.addModifier(player, new WaterTempModifier(0.01), Temperature.Types.AMBIENT, false);
                }
                else
                {
                    PlayerHelper.removeModifiers(player, Temperature.Types.AMBIENT, 999, modifier ->
                            modifier instanceof WaterTempModifier && (double) modifier.getArgument("strength") <= 0);
                }
            }

            // Nether Lamp
            if (player.getPersistentData().getInt("soulLampTimeout") <= 0 && PlayerHelper.hasModifier(player, HellLampTempModifier.class, Temperature.Types.AMBIENT))
            {
                PlayerHelper.removeModifiers(player, Temperature.Types.AMBIENT, 1, modifier -> modifier instanceof HellLampTempModifier);
            }
            else
            {
                player.getPersistentData().putInt("soulLampTimeout", player.getPersistentData().getInt("soulLampTimeout") - 1);
            }
        }
    }

    @SubscribeEvent
    public static void onSleep(SleepFinishedTimeEvent event)
    {
        event.getWorld().players().forEach(player ->
        {
            if (player.isSleeping())
            {
                dev.momostudios.coldsweat.common.temperature.Temperature temp = PlayerHelper.getTemperature(player, Temperature.Types.BODY);
                PlayerHelper.setTemperature(player, new dev.momostudios.coldsweat.common.temperature.Temperature(temp.get() / 4), Temperature.Types.BODY);
            }
        });
    }
}