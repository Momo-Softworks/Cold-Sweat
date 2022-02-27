package dev.momostudios.coldsweat.common.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SoulFireDamage
{
    @SubscribeEvent
    public static void setNoSoulFireDamage(LivingAttackEvent event)
    {
        if (event.getEntityLiving().getPersistentData().getBoolean("isInSoulFire") && event.getSource() == DamageSource.IN_FIRE)
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void resetSoulFireNBT(LivingEvent.LivingUpdateEvent event)
    {
        if (event.getEntity().getPersistentData().getBoolean("isInSoulFire") && event.getEntityLiving().tickCount % 4 == 0)
        {
            Entity ent = event.getEntity();
            AABB bb = new AABB(ent.getX() - 0.7, ent.getY() - 0.5, ent.getZ() - 0.7, ent.getX() + 0.7, ent.getY() + 2.1, ent.getZ() + 0.7);
            if (ent.level.getBlockStates(bb).noneMatch(state -> state.getBlock() == Blocks.SOUL_FIRE) && event.getEntity().getPersistentData().getBoolean("isInSoulFire"))
            {
                event.getEntity().getPersistentData().putBoolean("isInSoulFire", false);
            }
        }
    }
}
