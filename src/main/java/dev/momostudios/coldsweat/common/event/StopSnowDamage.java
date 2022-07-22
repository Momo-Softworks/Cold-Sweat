package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.InsulationTempModifier;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.util.config.ConfigCache;
import dev.momostudios.coldsweat.util.entity.TempHelper;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class StopSnowDamage
{
    @SubscribeEvent
    public static void onSnowDamage(LivingAttackEvent event)
    {
        if (event.getSource() == DamageSource.FREEZE && event.getEntityLiving().hasEffect(ModEffects.ICE_RESISTANCE) && ConfigCache.getInstance().iceRes)
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void playerFreezingTick(TickEvent.PlayerTickEvent event)
    {
        if (!event.player.level.isClientSide() && event.player.getTicksFrozen() > 0 && event.player.getTicksFrozen() < event.player.getTicksRequiredToFreeze())
        {
            TempModifier insulationModifier;
            int insulation;
            if ((event.player.hasEffect(ModEffects.ICE_RESISTANCE) && ConfigCache.getInstance().iceRes)
            || ((insulation = ((insulationModifier = TempHelper.getModifier(event.player, Temperature.Types.RATE, InsulationTempModifier.class)) == null ? 0
            : insulationModifier.<Integer>getArgument("warmth"))) > 0 && (event.player.tickCount % Math.max(1, 37 - insulation)) == 0))
            {
                event.player.setTicksFrozen(event.player.getTicksFrozen() - 1);
            }
        }
    }
}
