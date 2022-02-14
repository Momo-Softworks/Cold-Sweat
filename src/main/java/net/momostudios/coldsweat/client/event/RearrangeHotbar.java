package net.momostudios.coldsweat.client.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.config.ClientSettingsConfig;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RearrangeHotbar
{
    public static boolean customHotbar = ClientSettingsConfig.getInstance().customHotbar();
    static boolean hasShiftedUp = false;
    static boolean hasShiftedDown = false;

    @SubscribeEvent
    public static void onOverlayRenderPre(RenderGameOverlayEvent.Pre event)
    {
        if (ClientSettingsConfig.getInstance().customHotbar())
        {
            if (hasShiftedUp)
            {
                event.getMatrixStack().translate(0, 2, 0);
                hasShiftedUp = false;
            }

            if (event.getType() == RenderGameOverlayEvent.ElementType.HEALTH ||
                event.getType() == RenderGameOverlayEvent.ElementType.ARMOR ||
                event.getType() == RenderGameOverlayEvent.ElementType.FOOD ||
                event.getType() == RenderGameOverlayEvent.ElementType.HEALTHMOUNT ||
                event.getType() == RenderGameOverlayEvent.ElementType.AIR ||
                event.getType() == RenderGameOverlayEvent.ElementType.TEXT)
            {
                event.getMatrixStack().translate(0, -2, 0);
                hasShiftedUp = true;
            }
            if (event.getType() == RenderGameOverlayEvent.ElementType.JUMPBAR)
            {
                event.getMatrixStack().translate(0, -1, 0);
                hasShiftedDown = true;
            }
        }
        else
        {
            if (hasShiftedUp)
            {
                event.getMatrixStack().translate(0, 2, 0);
            }
            if (hasShiftedDown)
            {
                event.getMatrixStack().translate(0, 1, 0);
            }
            hasShiftedUp = false;
            hasShiftedDown = false;
        }
    }

    @SubscribeEvent
    public static void updateCustomHotbar(TickEvent.ClientTickEvent event)
    {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.ticksExisted % 20 == 0)
        {
            customHotbar = ClientSettingsConfig.getInstance().customHotbar();
        }
    }
}
