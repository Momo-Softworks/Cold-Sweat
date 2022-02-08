package net.momostudios.coldsweat.core.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.util.CSMath;
import net.momostudios.coldsweat.util.CustomDamageTypes;
import net.momostudios.coldsweat.util.PlayerHelper;
import net.momostudios.coldsweat.util.registrylists.ModEffects;

import java.util.List;

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

            // Tick expiration time for ambient modifiers
            Temperature ambient = tickModifiers(new Temperature(), player, PlayerHelper.getModifiers(player, PlayerHelper.Types.AMBIENT));
            double ambientTemp = ambient.get();

            // Apply ambient temperature modifiers
            PlayerHelper.setTemperature(player, ambient, PlayerHelper.Types.AMBIENT);

            if (!event.player.world.isRemote)
            {
                Temperature bodyTemp = PlayerHelper.getTemperature(player, PlayerHelper.Types.BODY);
                Temperature baseTemp = PlayerHelper.getTemperature(player, PlayerHelper.Types.BASE);

                double maxTemp = config.maxTemp;
                double minTemp = config.minTemp;

                double tempRate = 7.0d;

                if (ambientTemp > maxTemp && bodyTemp.get() >= 0 ||
                        ambientTemp < minTemp && bodyTemp.get() <= 0)
                {
                    boolean isOver = ambientTemp > maxTemp;
                    double difference = Math.abs(ambientTemp - (isOver ? maxTemp : minTemp));
                    double changeBy = Math.max((difference / tempRate) * config.rate, Math.abs(config.rate / 50)) * (isOver ? 1 : -1);
                    bodyTemp.add(new Temperature(changeBy).with(PlayerHelper.getModifiers(player, PlayerHelper.Types.RATE), player).get());
                }
                else
                {
                    // Return the player's body temperature to 0
                    bodyTemp.add(getBodyReturnRate(ambientTemp, bodyTemp.get() > 0 ? maxTemp : minTemp, config.rate, bodyTemp.get()));
                }

                // Calculate body/base temperatures with modifiers
                Temperature body = tickModifiers(bodyTemp, player, PlayerHelper.getModifiers(player, PlayerHelper.Types.BODY));
                Temperature base = tickModifiers(new Temperature(), player, PlayerHelper.getModifiers(player, PlayerHelper.Types.BASE));

                // Set the player's body temperature
                PlayerHelper.setTemperature(player, body, PlayerHelper.Types.BODY);

                // Set the player's base temperature
                PlayerHelper.setTemperature(player, base, PlayerHelper.Types.BASE);

                // Sets the player's composite temperature to BASE + BODY
                PlayerHelper.setTemperature
                (
                    player,
                    new Temperature(CSMath.clamp(base.add(body).get(), -150, 150)),
                    PlayerHelper.Types.COMPOSITE
                );

                //Deal damage to the player if temperature is critical
                boolean hasFireResistance = player.isPotionActive(Effects.FIRE_RESISTANCE) && config.fireRes;
                boolean hasIceResistance = player.isPotionActive(ModEffects.ICE_RESISTANCE) && config.iceRes;
                if (player.ticksExisted % 40 == 0)
                {
                    boolean damageScaling = config.damageScaling;
                    Temperature composite = PlayerHelper.getTemperature(player, PlayerHelper.Types.COMPOSITE);

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
    }

    // Used for returning the player's temperature back to 0
    private static double getBodyReturnRate(double ambient, double cap, double rate, double bodyTemp)
    {
        double tempRate = 7.0d;
        double changeBy = Math.max((Math.abs(ambient - cap) / tempRate) * rate, Math.abs(rate / 30));
        return Math.min(Math.abs(bodyTemp), changeBy) * (bodyTemp > 0 ? -1 : 1);
    }

    private static Temperature tickModifiers(Temperature temp, PlayerEntity player, List<TempModifier> modifiers)
    {
        modifiers.removeIf(modifier ->
        {
            modifier.setTicksExisted(modifier.getTicksExisted() + 1);
            return (modifier.getExpireTicks() != -1 && modifier.getTicksExisted() > modifier.getExpireTicks());
        });

        return temp.with(modifiers, player);
    }

    @SubscribeEvent
    public static void serverSyncConfigToCache(TickEvent.WorldTickEvent event)
    {
        // Syncs the server's config files to the cache
        if (!event.world.isRemote && event.world.getGameTime() % 20 == 0)
            ConfigCache.getInstance().writeValues(ColdSweatConfig.getInstance());
    }
}