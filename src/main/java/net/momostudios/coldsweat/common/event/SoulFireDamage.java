package net.momostudios.coldsweat.common.event;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
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
        if (event.getEntity().getPersistentData().getBoolean("isInSoulFire") && event.getEntityLiving().ticksExisted % 4 == 0)
        {
            Entity ent = event.getEntity();
            AxisAlignedBB bb = new AxisAlignedBB(ent.getPosX() - 0.7, ent.getPosY() - 0.5, ent.getPosZ() - 0.7, ent.getPosX() + 0.7, ent.getPosY() + 2.1, ent.getPosZ() + 0.7);
            if (ent.world.getStatesInArea(bb).noneMatch(state -> state.getBlock() == Blocks.SOUL_FIRE) && event.getEntity().getPersistentData().getBoolean("isInSoulFire"))
            {
                event.getEntity().getPersistentData().putBoolean("isInSoulFire", false);
            }
        }
    }
}
