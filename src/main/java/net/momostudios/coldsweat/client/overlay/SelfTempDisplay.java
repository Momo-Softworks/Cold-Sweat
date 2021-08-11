package net.momostudios.coldsweat.client.overlay;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.ResourceLocation;
import net.momostudios.coldsweat.common.temperature.PlayerTempHandler;

@Mod.EventBusSubscriber
public class SelfTempDisplay
{
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGameOverlayEvent event)
    {
        if (!event.isCancelable() && event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR &&
        !Minecraft.getInstance().player.abilities.isCreativeMode && !Minecraft.getInstance().player.isSpectator())
        {
            int scaleX = event.getWindow().getScaledWidth();
            int scaleY = event.getWindow().getScaledHeight();
            PlayerEntity entity = Minecraft.getInstance().player;
            double x = entity.getPosX();
            double y = entity.getPosY();
            double z = entity.getPosZ();

            double temp = PlayerTempHandler.getBody(Minecraft.getInstance().getIntegratedServer().getPlayerList().getPlayerByUUID(entity.getUniqueID())).get();


            int threatLevel = 0;

            ResourceLocation icon;
            if      (temp >= 100)   {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_2.png");   threatLevel = 2;}
            else if (temp >= 66)    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_1.png");   threatLevel = 1;}
            else if (temp >= 33)    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_0.png");}
            else if (temp >= 0)     {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_default.png");}
            else if (temp > -33)    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_default.png");}
            else if (temp > -66)    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_0.png");}
            else if (temp > -99)    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_1.png");  threatLevel = 1;}
            else                    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_2.png");  threatLevel = 2;}
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            int threatOffset = 0;
            if (threatLevel == 1) threatOffset = entity.ticksExisted % 10 == 0 && Math.random() < 0.5 ? 1 : 0;
            if (threatLevel == 2) threatOffset = entity.ticksExisted % 2 == 0 ? 1 : 0;

            Minecraft.getInstance().getTextureManager().bindTexture(icon);
            Minecraft.getInstance().ingameGUI.blit(event.getMatrixStack(), (scaleX / 2) - 5, scaleY - 48 + threatOffset, 0, 0, 10, 10, 10, 10);
        }
    }
}

