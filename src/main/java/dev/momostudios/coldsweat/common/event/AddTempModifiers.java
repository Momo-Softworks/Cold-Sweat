package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.*;
import dev.momostudios.coldsweat.util.entity.TempHelper;
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
        if (event.phase == TickEvent.Phase.START)
        {
            Player player = event.player;

            /*
             * Add TempModifiers if not present
             */
            if (player.tickCount % 20 == 0)
            {
                TempHelper.addModifier(player, new BiomeTempModifier().tickRate(5), Temperature.Types.WORLD, false);
                TempHelper.addModifier(player, new TimeTempModifier().tickRate(10), Temperature.Types.WORLD, false);
                TempHelper.addModifier(player, new DepthTempModifier().tickRate(5), Temperature.Types.WORLD, false);
                TempHelper.addModifier(player, new BlockTempModifier().tickRate(5), Temperature.Types.WORLD, false);
                if (ModList.get().isLoaded("sereneseasons"))
                    TempHelper.addModifier(player, TempModifierRegistry.getEntryFor("sereneseasons:season"), Temperature.Types.WORLD, false);

                // Hearth
                MobEffectInstance effect = player.getEffect(ModEffects.INSULATION);
                if (effect != null)
                {
                    TempHelper.insertModifier(player, new HearthTempModifier(effect.getAmplifier() + 1).expires(20), Temperature.Types.WORLD);
                }
            }

            // Water / Rain
            if (player.tickCount % 5 == 0 && player.isInWaterRainOrBubble())
            {
                TempHelper.addModifier(player, new WaterTempModifier(0.01), Temperature.Types.WORLD, false);
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
                Temperature temp = TempHelper.getTemperature(player, Temperature.Types.CORE);
                TempHelper.setTemperature(player, new Temperature(temp.get() / 4), Temperature.Types.CORE);
            }
        });
    }
}