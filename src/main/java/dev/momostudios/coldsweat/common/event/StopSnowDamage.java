package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.temperature.modifier.InsulationTempModifier;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
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
        if (event.getSource() == DamageSource.FREEZE && event.getEntityLiving().hasEffect(ModEffects.ICE_RESISTANCE) && ConfigSettings.ICE_RESISTANCE_ENABLED.get())
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void playerFreezingTick(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;
        if (!player.level.isClientSide() && event.phase == TickEvent.Phase.END && player.getTicksFrozen() > 0)
        {
            TempModifier insulationModifier;
            double insulation = 0;
            boolean hasIcePotion = player.hasEffect(ModEffects.ICE_RESISTANCE) && ConfigSettings.ICE_RESISTANCE_ENABLED.get();
            if (hasIcePotion
            || ((insulation = ((insulationModifier = Temperature.getModifier(player, Temperature.Type.RATE, InsulationTempModifier.class)) == null ? 0
            : insulationModifier.getNBT().getDouble("chill"))) > 0 && (player.tickCount % Math.max(1, 37 - insulation)) == 0))
            {
                if (player.getTicksFrozen() < event.player.getTicksRequiredToFreeze() || insulation >= 37 || hasIcePotion)
                {
                    player.setTicksFrozen(player.getTicksFrozen() - 1);
                }
            }
        }
    }
}
