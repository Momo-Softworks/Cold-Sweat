package dev.momostudios.coldsweat.client.event;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
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
        PlayerEntity player = event.player;
        if (player.level.isClientSide && event.phase == TickEvent.Phase.START)
        {
            if (player.tickCount % 20 == 0)
                LEFT_HANDED = player.getMainArm() == HandSide.LEFT;

            Pair<Float, Float> rightArmRot = RIGHT_ARM_ROTATIONS.getOrDefault(player, Pair.of(0f, 0f));

            if (player.getItemInHand(LEFT_HANDED ? Hand.OFF_HAND : Hand.MAIN_HAND).getItem() == ModItems.SOULSPRING_LAMP)
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

            if (player.getItemInHand(LEFT_HANDED ? Hand.MAIN_HAND : Hand.OFF_HAND).getItem() == ModItems.SOULSPRING_LAMP)
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