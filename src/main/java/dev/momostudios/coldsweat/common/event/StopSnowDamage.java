package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.util.registries.ModEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class StopSnowDamage
{
    @SubscribeEvent
    public static void onSnowDamage(LivingAttackEvent event)
    {
        if (event.getSource() == DamageSource.FREEZE && event.getEntityLiving().hasEffect(ModEffects.ICE_RESISTANCE))
        {
            event.setCanceled(true);
        }
    }
}
