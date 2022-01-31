package net.momostudios.coldsweat.core.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.util.CSMath;
import net.momostudios.coldsweat.util.CustomDamageTypes;
import net.momostudios.coldsweat.util.registrylists.ModEffects;
import net.momostudios.coldsweat.util.PlayerTemp;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerTempUpdater
{
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            PlayerEntity player = event.player;
            ConfigCache config = ConfigCache.getInstance();

            double ambientTemp = new Temperature().with(PlayerTemp.getModifiers(player, PlayerTemp.Types.AMBIENT), player).get();
            Temperature bodyTemp = PlayerTemp.getTemperature(player, PlayerTemp.Types.BODY);

            // Apply ambient temperature modifiers
            PlayerTemp.setTemperature(player, new Temperature(ambientTemp), PlayerTemp.Types.AMBIENT);

            double maxTemp = config.maxTemp;
            double minTemp = config.minTemp;

            double tempRate = 7.0d;

            //Increase body temperature when ambientTemp is above maximum (with rate modifiers)
            if (ambientTemp > maxTemp && !player.isCreative() && !player.isSpectator())
            {
                bodyTemp.add(new Temperature((float) Math.abs(maxTemp - ambientTemp) / tempRate)
                        .with(PlayerTemp.getModifiers(player, PlayerTemp.Types.RATE), player).get() * config.rate);
            }
            //Return the player's temperature back to 0
            else if (bodyTemp.get() > 0)
            {
                // Limits the return rate to (config.rate / 10) per tick
                // Also sets the temperature to zero if it's close enough to not matter
                bodyTemp.add(-getBodyReturnRate(ambientTemp, maxTemp, config.rate, bodyTemp.get()));
            }

            //Decrease body temperature when ambientTemp is below minimum (with rate modifiers)
            if (ambientTemp < minTemp && !player.isCreative() && !player.isSpectator())
            {
                bodyTemp.add(-new Temperature((float) Math.abs(minTemp - ambientTemp) / tempRate)
                        .with(PlayerTemp.getModifiers(player, PlayerTemp.Types.RATE), player).get() * config.rate);
            }
            //Return the player's temperature back to 0
            else if (bodyTemp.get() < 0)
            {
                // Limits the return rate to (config.rate / 10) per tick
                // Also sets the temperature to zero if it's close enough to not matter
                bodyTemp.add(getBodyReturnRate(ambientTemp, minTemp, config.rate, bodyTemp.get()));
            }

            // Calculate body/base temperatures with modifiers
            Temperature body = bodyTemp.with(PlayerTemp.getModifiers(player, PlayerTemp.Types.BODY), player);
            Temperature base = new Temperature().with(PlayerTemp.getModifiers(player, PlayerTemp.Types.BASE), player);

            // Set the player's body temperature
            PlayerTemp.setTemperature(player, body, PlayerTemp.Types.BODY);

            // Set the player's base temperature
            PlayerTemp.setTemperature(player, base, PlayerTemp.Types.BASE);

            // Sets the player's composite temperature to BASE + BODY
            PlayerTemp.setTemperature
            (
                player,
                new Temperature(CSMath.clamp(base.add(body).get(), -150, 150)),
                PlayerTemp.Types.COMPOSITE
            );

            //Deal damage to the player if temperature is critical
            boolean hasFireResistance = player.isPotionActive(Effects.FIRE_RESISTANCE) && config.fireRes;
            boolean hasIceResistance = player.isPotionActive(ModEffects.ICE_RESISTANCE) && config.iceRes;
            if (player.ticksExisted % 40 == 0)
            {
                boolean damageScaling = config.damageScaling;
                Temperature composite = PlayerTemp.getTemperature(player, PlayerTemp.Types.COMPOSITE);

                if (composite.get() >= 100 && !hasFireResistance && !player.isPotionActive(ModEffects.GRACE))
                {
                    player.attackEntityFrom(damageScaling ? CustomDamageTypes.HOT_SCALED : CustomDamageTypes.HOT, 2f);
                }
                if (composite.get() <= -100 && !hasIceResistance && !player.isPotionActive(ModEffects.GRACE))
                {
                    player.attackEntityFrom(damageScaling ? CustomDamageTypes.COLD_SCALED : CustomDamageTypes.COLD, 2f);
                }
            }
        }
    }

    // Used for returning the player's temperature back to 0
    private static double getBodyReturnRate(double ambient, double cap, double rate, double bodyTemp)
    {
        double tempRate = 7.0d;
        double changeBy = Math.max((Math.abs(ambient - cap) / tempRate) * rate, Math.abs(rate / 10));
        return Math.min(Math.abs(bodyTemp), changeBy);
    }

    @SubscribeEvent
    public static void serverSyncConfigToCache(TickEvent.WorldTickEvent event)
    {
        // Syncs the server's config files to the cache
        if (!event.world.isRemote && event.world.getGameTime() % 20 == 0)
            ConfigCache.getInstance().writeValues(ColdSweatConfig.getInstance());
    }
}