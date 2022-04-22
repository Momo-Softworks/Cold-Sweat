package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.*;
import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.util.entity.TempHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
                /*
                if (ModList.get().isLoaded("betterweather"))
                    PlayerHelper.addModifier(player, TempModifierEntries.getEntries().getEntryFor("betterweather:season"), PlayerHelper.Types.AMBIENT, false);
                */

                // Hearth
                if (player.hasEffect(ModEffects.INSULATION))
                {
                    MobEffectInstance effect = player.getEffect(ModEffects.INSULATION);
                    int potionLevel = effect.getAmplifier() + 1;

                    TempHelper.removeModifiers(player, Temperature.Types.CORE, 1, (modifier) -> modifier instanceof HearthTempModifier);
                    TempHelper.addModifier(player, new HearthTempModifier(potionLevel).expires(20), Temperature.Types.WORLD, 1, false);
                }
            }

            if (player.tickCount % 5 == 0)
            {
                // Water / Rain
                if (player.isInWaterRainOrBubble())
                {
                    TempHelper.addModifier(player, new WaterTempModifier(0.01), Temperature.Types.WORLD, false);
                }
                else
                {
                    TempHelper.removeModifiers(player, Temperature.Types.WORLD, 999, modifier ->
                            modifier instanceof WaterTempModifier && (double) modifier.getArgument("strength") <= 0);
                }
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

    @SubscribeEvent
    public static void onTrySleep(PlayerInteractEvent.RightClickBlock event)
    {
        Player player = event.getPlayer();
        if (player.level.getBlockState(event.getPos()).getBlock() instanceof BedBlock)
        {
            double bodyTemp = TempHelper.getTemperature(player, Temperature.Types.BODY).get();
            double worldTemp = TempHelper.getTemperature(player, Temperature.Types.WORLD).get();
            double minTemp = ConfigCache.getInstance().minTemp;
            double maxTemp = ConfigCache.getInstance().maxTemp;

            if (!CSMath.isBetween((int) bodyTemp, -99, 99))
            {
                player.displayClientMessage(new TranslatableComponent("cold_sweat.message.sleep.body",
                                            new TranslatableComponent(bodyTemp > 99 ? "cold_sweat.message.sleep.hot" : "cold_sweat.message.sleep.cold").getString()), true);
                event.setCanceled(true);
            }
            else if (!CSMath.isBetween(worldTemp, minTemp, maxTemp))
            {
                player.displayClientMessage(new TranslatableComponent("cold_sweat.message.sleep.world",
                                            new TranslatableComponent(worldTemp > maxTemp ? "cold_sweat.message.sleep.hot" : "cold_sweat.message.sleep.cold").getString()), true);
                event.setCanceled(true);
            }
        }
    }
}