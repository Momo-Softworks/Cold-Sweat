package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.*;
import dev.momostudios.coldsweat.util.entity.TempHelper;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AddTempModifiers
{
    @SubscribeEvent
    public static void onPlayerCreated(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof Player player)
        {
            /*
             * Add TempModifiers if not present
             */
            TempHelper.addModifier(player, new BiomeTempModifier().tickRate(5), Temperature.Types.WORLD, false);
            TempHelper.addModifier(player, new TimeTempModifier().tickRate(20), Temperature.Types.WORLD, false);
            TempHelper.addModifier(player, new DepthTempModifier().tickRate(5), Temperature.Types.WORLD, false);
            TempHelper.addModifier(player, new BlockTempModifier().tickRate(5), Temperature.Types.WORLD, false);
            if (ModList.get().isLoaded("sereneseasons"))
                TempHelper.addModifier(player, TempModifierRegistry.getEntryFor("sereneseasons:season").tickRate(20), Temperature.Types.WORLD, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;

        // Water / Rain
        if (player.tickCount % 5 == 0 && player.isInWaterRainOrBubble())
        {
            TempHelper.addModifier(player, new WaterTempModifier(0.01), Temperature.Types.WORLD, false);
        }
    }

    @SubscribeEvent
    public static void onInsulationUpdate(PotionEvent event)
    {
        if (event.getEntity() instanceof Player player && event.getPotionEffect() != null
                && event.getPotionEffect().getEffect() == ModEffects.INSULATION)
        {
            if (event instanceof PotionEvent.PotionAddedEvent)
            {
                MobEffectInstance effect = event.getPotionEffect();
                TempHelper.insertModifier(player, new HearthTempModifier(effect.getAmplifier() + 1).expires(effect.getDuration()), Temperature.Types.WORLD);
            }
            else if (event instanceof PotionEvent.PotionRemoveEvent || event instanceof PotionEvent.PotionExpiryEvent)
            {
                TempHelper.removeModifiers(player, Temperature.Types.WORLD, 1, mod -> mod instanceof HearthTempModifier);
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