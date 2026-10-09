package com.momosoftworks.coldsweat.client.event;

import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(Dist.CLIENT)
public class HandleSoulLampAnim
{
    private static final Map<Player, ArmRaise> ARM_RAISES = new WeakHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event)
    {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) return;

        ArmRaise raise = ARM_RAISES.computeIfAbsent(player, p -> new ArmRaise());
        raise.right.tick(EntityHelper.holdingLamp(player, HumanoidArm.RIGHT));
        raise.left.tick(EntityHelper.holdingLamp(player, HumanoidArm.LEFT));
    }

    /**
     * @return How far the entity's arm is raised to hold the lamp, from 0 to 1
     */
    public static float getArmRaise(LivingEntity entity, HumanoidArm arm, float partialTick)
    {
        ArmRaise raise = ARM_RAISES.get(entity);
        if (raise == null) return 0;
        return (arm == HumanoidArm.RIGHT ? raise.right : raise.left).get(partialTick);
    }

    private static class ArmRaise
    {
        final Progress right = new Progress();
        final Progress left = new Progress();
    }

    private static class Progress
    {
        float prev;
        float current;

        void tick(boolean raised)
        {
            prev = current;
            current += ((raised ? 1 : 0) - current) / 3f;
        }

        float get(float partialTick)
        {   return Mth.lerp(partialTick, prev, current);
        }
    }
}
