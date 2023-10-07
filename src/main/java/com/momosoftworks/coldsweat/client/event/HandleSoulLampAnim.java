package com.momosoftworks.coldsweat.client.event;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class HandleSoulLampAnim
{
    public static Map<LivingEntity, Pair<Float, Float>> RIGHT_ARM_ROTATIONS = new HashMap<>();
    public static Map<LivingEntity, Pair<Float, Float>> LEFT_ARM_ROTATIONS = new HashMap<>();
    static boolean LEFT_HANDED = false;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;
        if (player.level().isClientSide && event.phase == TickEvent.Phase.START)
        {
            if (player.tickCount % 20 == 0)
                LEFT_HANDED = player.getMainArm() == HumanoidArm.LEFT;

            Pair<Float, Float> rightArmRot = RIGHT_ARM_ROTATIONS.getOrDefault(player, Pair.of(0f, 0f));

            if (player.getItemInHand(LEFT_HANDED ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND).getItem() == ModItems.SOULSPRING_LAMP)
            {
                float prevRot = rightArmRot.getFirst();
                if (prevRot < 69.99)
                {   RIGHT_ARM_ROTATIONS.put(player, Pair.of(prevRot + (70 - prevRot) / 3f, prevRot));
                }
            }
            else
            {
                float prevRot = rightArmRot.getFirst();
                if (prevRot > 0.01)
                {   RIGHT_ARM_ROTATIONS.put(player, Pair.of(prevRot + (0 - prevRot) / 3f, prevRot));
                }
            }

            Pair<Float, Float> leftArmRot = LEFT_ARM_ROTATIONS.getOrDefault(player, Pair.of(0f, 0f));

            if (player.getItemInHand(LEFT_HANDED ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND).getItem() == ModItems.SOULSPRING_LAMP)
            {
                float prevRot = leftArmRot.getFirst();
                if (prevRot < 69.99)
                {   LEFT_ARM_ROTATIONS.put(player, Pair.of(prevRot + (70 - prevRot) / 3f, prevRot));
                }
            }
            else
            {
                float prevRot = leftArmRot.getFirst();
                if (prevRot > 0.01)
                {   LEFT_ARM_ROTATIONS.put(player, Pair.of(prevRot + (0 - prevRot) / 3f, prevRot));
                }
            }
        }
    }
}