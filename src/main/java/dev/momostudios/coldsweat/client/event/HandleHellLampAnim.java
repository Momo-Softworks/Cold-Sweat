package dev.momostudios.coldsweat.client.event;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.util.entity.PlayerHelper;
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
public class HandleHellLampAnim
{
    public static Map<LivingEntity, Pair<Float, Float>> RIGHT_ARM_ROTATIONS = new HashMap<>();
    public static Map<LivingEntity, Pair<Float, Float>> LEFT_ARM_ROTATIONS = new HashMap<>();
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            Player player = event.player;

            Pair<Float, Float> rightArmRot = RIGHT_ARM_ROTATIONS.getOrDefault(player, Pair.of(0f, 0f));

            if (PlayerHelper.holdingLamp(player, HumanoidArm.RIGHT))
            {
                float post = rightArmRot.getFirst();
                if (post < 69.99)
                {
                    RIGHT_ARM_ROTATIONS.put(player, Pair.of(post + (70 - post) / 3f, post));
                }
            }
            else
            {
                float post = rightArmRot.getFirst();
                if (post > 0.01)
                {
                    RIGHT_ARM_ROTATIONS.put(player, Pair.of(post + (0 - post) / 3f, post));
                }
            }

            Pair<Float, Float> leftArmRot = LEFT_ARM_ROTATIONS.getOrDefault(player, Pair.of(0f, 0f));

            if (PlayerHelper.holdingLamp(player, HumanoidArm.LEFT))
            {
                float post = leftArmRot.getFirst();
                if (post < 69.99)
                {
                    LEFT_ARM_ROTATIONS.put(player, Pair.of(post + (71 - post) / 3f, post));
                }
            }
            else
            {
                float post = leftArmRot.getFirst();
                if (post > 0.01)
                {
                    LEFT_ARM_ROTATIONS.put(player, Pair.of(post + (0 - post) / 3f, post));
                }
            }
        }
    }
}