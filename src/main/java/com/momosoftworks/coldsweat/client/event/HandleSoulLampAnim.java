package com.momosoftworks.coldsweat.client.event;

import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.HandSide;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class HandleSoulLampAnim
{
    private static final Map<PlayerEntity, ArmRaise> ARM_RAISES = new WeakHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        PlayerEntity player = event.player;
        if (!player.level.isClientSide || event.phase != TickEvent.Phase.START) return;

        ArmRaise raise = ARM_RAISES.computeIfAbsent(player, p -> new ArmRaise());
        raise.right.tick(EntityHelper.holdingLamp(player, HandSide.RIGHT));
        raise.left.tick(EntityHelper.holdingLamp(player, HandSide.LEFT));
    }

    /**
     * @return How far the entity's arm is raised to hold the lamp, from 0 to 1
     */
    public static float getArmRaise(LivingEntity entity, HandSide arm, float partialTick)
    {
        ArmRaise raise = ARM_RAISES.get(entity);
        if (raise == null) return 0;
        return (arm == HandSide.RIGHT ? raise.right : raise.left).get(partialTick);
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
        {   return MathHelper.lerp(partialTick, prev, current);
        }
    }
}
