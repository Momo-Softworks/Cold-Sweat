package dev.momostudios.coldsweat.client.event;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.util.entity.TempHelper;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class HandleHellLampAnim
{
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            Player player = event.player;

            float rightArmRot = player.getPersistentData().getFloat("rightArmRot");

            if (TempHelper.holdingLamp(player, HumanoidArm.RIGHT))
            {
                if (rightArmRot < 70)
                    player.getPersistentData().putFloat("rightArmRot", rightArmRot + (71 - rightArmRot) / 2);
            }
            else
            {
                if (rightArmRot > 0)
                    player.getPersistentData().putFloat("rightArmRot", rightArmRot + (0 - rightArmRot) / 2);
            }

            float leftArmRot = player.getPersistentData().getFloat("leftArmRot");

            if (TempHelper.holdingLamp(player, HumanoidArm.LEFT))
            {
                if (leftArmRot < 70)
                    player.getPersistentData().putFloat("leftArmRot", leftArmRot + (71 - leftArmRot) / 2);
            }
            else
            {
                if (leftArmRot > 0)
                    player.getPersistentData().putFloat("leftArmRot", leftArmRot + (0 - leftArmRot) / 2);
            }
        }
    }
}