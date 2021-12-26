package net.momostudios.coldsweat.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.core.util.PlayerHelper;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class HandleSoulLampAnim
{
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START && Minecraft.getInstance().player != null)
        {
            PlayerEntity player = Minecraft.getInstance().player;

            float rightArmRot = player.getPersistentData().getFloat("rightArmRot");

            if (PlayerHelper.holdingLamp(player, HandSide.RIGHT))
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

            if (PlayerHelper.holdingLamp(player, HandSide.LEFT))
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